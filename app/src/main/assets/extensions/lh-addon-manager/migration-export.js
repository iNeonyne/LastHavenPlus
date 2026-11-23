import { buildZip, LIMITS } from "./archive.js";

export const MIGRATION_FORMAT = "lasthavens-addon-manager-migration";
export const MIGRATION_SCHEMA = 1;

const encoder = new TextEncoder();
const folderPattern = /^[A-Za-z0-9_-]{1,120}$/;
const safeRelativePath = (value) => {
  if (typeof value !== "string" || !value || value.length > 1024) return false;
  if (value.includes("\\")) return false;
  const normalized = value;
  return !normalized.startsWith("/") &&
    !/[\x00-\x1f:]/.test(normalized) &&
    normalized.split("/").every((part) => part && part !== "." && part !== "..");
};
const asBytes = (value) => {
  if (value instanceof Uint8Array) return value;
  if (value instanceof ArrayBuffer) return new Uint8Array(value);
  if (ArrayBuffer.isView(value))
    return new Uint8Array(value.buffer, value.byteOffset, value.byteLength);
  throw new Error("Migration contains a file with an unsupported byte representation");
};
const sha256 = async (bytes) =>
  [...new Uint8Array(await crypto.subtle.digest("SHA-256", bytes))]
    .map((value) => value.toString(16).padStart(2, "0"))
    .join("");

/**
 * Creates a self-contained, checksummed migration archive from the real 1.0.6
 * library and its IndexedDB file records. It does not mutate source storage.
 */
export async function createMigrationArchive({
  library,
  preferences = {},
  getFiles,
  exportedAt = Date.now(),
}) {
  if (!library || !Array.isArray(library.addons))
    throw new Error("The installed Addon Manager library is unavailable");
  if (typeof getFiles !== "function")
    throw new Error("The migration file reader is unavailable");

  const seenFolders = new Set();
  const seenStorageFolders = new Set();
  const files = [];
  const entries = [];
  let expandedBytes = 0;

  for (const addon of library.addons) {
    if (!addon || !folderPattern.test(addon.folder || ""))
      throw new Error("The library contains an invalid add-on folder");
    const folderKey = addon.folder.toLowerCase();
    if (seenFolders.has(folderKey))
      throw new Error("The library contains duplicate add-on folders");
    seenFolders.add(folderKey);

    const storageFolder = addon.storageFolder || addon.folder;
    if (!folderPattern.test(storageFolder) || seenStorageFolders.has(storageFolder.toLowerCase()))
      throw new Error("The library contains an invalid or duplicate storage folder");
    seenStorageFolders.add(storageFolder.toLowerCase());

    const storedFiles = await getFiles(storageFolder);
    const paths = Object.keys(storedFiles || {});
    if (!paths.length)
      throw new Error(`No stored files were found for ${addon.folder}`);
    if (files.length + paths.length > LIMITS.count)
      throw new Error("The migration exceeds the add-on file-count limit");

    let addonBytes = 0;
    let hasToc = false;
    const seenPaths = new Set();
    for (const path of paths.sort()) {
      if (!safeRelativePath(path) || seenPaths.has(path.toLowerCase()))
        throw new Error(`Unsafe or duplicate file path in ${addon.folder}`);
      seenPaths.add(path.toLowerCase());
      const bytes = asBytes(storedFiles[path]);
      if (bytes.byteLength > LIMITS.file)
        throw new Error(`A file in ${addon.folder} exceeds the file-size limit`);
      addonBytes += bytes.byteLength;
      expandedBytes += bytes.byteLength;
      if (expandedBytes > LIMITS.total)
        throw new Error("The migration exceeds the total library-size limit");
      if (path.toLowerCase() === `${addon.folder.toLowerCase()}.toc`)
        hasToc = true;

      const archivePath = `files/${storageFolder}/${path}`;
      entries.push([archivePath, bytes]);
      files.push({
        addonFolder: addon.folder,
        storageFolder,
        path,
        archivePath,
        size: bytes.byteLength,
        sha256: await sha256(bytes),
      });
    }
    if (!hasToc)
      throw new Error(`The TOC file is missing for ${addon.folder}`);
    if (Number.isFinite(addon.size) && addon.size !== addonBytes)
      throw new Error(`Stored file size does not match library metadata for ${addon.folder}`);
    if (Number.isFinite(addon.fileCount) && addon.fileCount !== paths.length)
      throw new Error(`Stored file count does not match library metadata for ${addon.folder}`);
  }

  const manifest = {
    format: MIGRATION_FORMAT,
    schema: MIGRATION_SCHEMA,
    exportedAt,
    sourceManagerVersion: "1.0.6",
    library: {
      revision: String(library.revision || "unknown"),
      addons: library.addons.map((addon) => ({ ...addon })),
    },
    preferences: {
      settings: preferences.settings && typeof preferences.settings === "object"
        ? preferences.settings : {},
      language: typeof preferences.language === "string" ? preferences.language : "en",
      browseInstalled: preferences.browseInstalled && typeof preferences.browseInstalled === "object"
        ? preferences.browseInstalled : {},
    },
    files,
    totals: {
      addons: library.addons.length,
      files: files.length,
      expandedBytes,
    },
  };

  entries.unshift(["manifest.json", encoder.encode(JSON.stringify(manifest, null, 2))]);
  const archive = buildZip(entries);
  if (archive.byteLength > LIMITS.archive)
    throw new Error("The migration archive exceeds the compressed archive-size limit");
  return { archive, manifest };
}

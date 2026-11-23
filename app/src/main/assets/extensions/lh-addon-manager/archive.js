import { AddonError } from "./errors.js";
import { unzipSync, zipSync } from "./vendor/fflate.js";
import { LIMITS } from "./limits.js";
import { tocMeta } from "./toc.js";
export { LIMITS };
const decoder = new TextDecoder(),
  strictDecoder = new TextDecoder("utf-8", { fatal: true });
const latin1 = (b) => Array.from(b, (c) => String.fromCharCode(c)).join("");
// The unzip library reads names as UTF-8 only when the ZIP flags them as UTF-8, otherwise as
// Latin-1. `key` must match that exactly; `name` is the best readable form of the same bytes.
function entryName(raw, flags) {
  if (flags & 0x800) {
    const s = decoder.decode(raw);
    return { key: s, name: s };
  }
  const key = latin1(raw);
  try {
    return { key, name: strictDecoder.decode(raw) };
  } catch {
    return { key, name: key };
  }
}
const sameBytes = (a, b) =>
  a.length === b.length && a.every((v, i) => v === b[i]);
const CRC_TABLE = new Uint32Array(256).map((_, n) => {
  let c = n;
  for (let j = 0; j < 8; j++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
export function crc32(bytes) {
  let c = 0xffffffff;
  for (let i = 0; i < bytes.length; i++)
    c = CRC_TABLE[(c ^ bytes[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function safePath(name) {
  const p = name.replaceAll("\\", "/");
  if (
    p.startsWith("/") ||
    /[\x00-\x1f:]/.test(p) ||
    p.split("/").some((s) => s === ".." || s === ".")
  )
    throw new AddonError("errorPath", { detail: name });
  return p;
}
function inspectZip(bytes) {
  if (bytes.length > LIMITS.archive) throw new AddonError("errorArchiveSize");
  const v = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
  let e = -1;
  for (let i = bytes.length - 22; i >= Math.max(0, bytes.length - 65557); i--) {
    if (
      v.getUint32(i, true) === 0x06054b50 &&
      i + 22 + v.getUint16(i + 20, true) === bytes.length
    ) {
      e = i;
      break;
    }
  }
  if (e < 0) throw new AddonError("errorZip");
  if (v.getUint16(e + 4, true) || v.getUint16(e + 6, true))
    throw new AddonError("errorUnsupported");
  const count = v.getUint16(e + 10, true),
    start = v.getUint32(e + 16, true),
    length = v.getUint32(e + 12, true);
  if (count === 65535 || start === 0xffffffff || length === 0xffffffff)
    throw new AddonError("errorZip");
  if (count > LIMITS.count || start + length > e)
    throw new AddonError("errorZip");
  let at = start,
    total = 0;
  const meta = new Map(),
    seen = new Set();
  for (let i = 0; i < count; i++) {
    if (at + 46 > bytes.length || v.getUint32(at, true) !== 0x02014b50)
      throw new AddonError("errorZip");
    const flags = v.getUint16(at + 8, true),
      method = v.getUint16(at + 10, true),
      size = v.getUint32(at + 24, true),
      n = v.getUint16(at + 28, true),
      extra = v.getUint16(at + 30, true),
      comment = v.getUint16(at + 32, true),
      offset = v.getUint32(at + 42, true);
    if (flags & 1 || ![0, 8].includes(method))
      throw new AddonError("errorUnsupported");
    if (at + 46 + n + extra + comment > start + length)
      throw new AddonError("errorZip");
    const rawName = bytes.subarray(at + 46, at + 46 + n),
      { key, name } = entryName(rawName, flags),
      path = safePath(name);
    if (((v.getUint32(at + 38, true) >>> 16) & 0xf000) === 0xa000)
      throw new AddonError("errorUnsupported");
    if (offset + 30 > start || v.getUint32(offset, true) !== 0x04034b50)
      throw new AddonError("errorZip");
    const ln = v.getUint16(offset + 26, true),
      le = v.getUint16(offset + 28, true),
      compressed = v.getUint32(at + 20, true);
    if (
      offset + 30 + ln + le + compressed > start ||
      !sameBytes(bytes.subarray(offset + 30, offset + 30 + ln), rawName)
    )
      throw new AddonError("errorZip");
    total += size;
    if (size > LIMITS.file || total > LIMITS.expanded)
      throw new AddonError("errorExpanded");
    if (seen.has(path.toLowerCase()))
      throw new AddonError("errorDuplicate", { detail: path });
    seen.add(path.toLowerCase());
    meta.set(key, { path, size, crc: v.getUint32(at + 16, true) });
    at += 46 + n + extra + comment;
  }
  if (at !== start + length) throw new AddonError("errorZip");
  return meta;
}
// Branch downloads (GitHub/GitLab) wrap one addon in "Repo-main/". When that wrapper holds the
// addon's .toc directly under a different name, rename the wrapper to the .toc name.
function unwrapBranchFolder(files, crcOf) {
  const paths = [...files.keys()];
  if (!paths.length || !paths.every((p) => p.includes("/"))) return;
  const top = paths[0].split("/")[0];
  if (!paths.every((p) => p.split("/")[0] === top)) return;
  const tocs = paths.filter(
    (p) => p.split("/").length === 2 && /\.toc$/i.test(p),
  );
  if (!tocs.length || tocs.some((p) => p.toLowerCase() === `${top}/${top}.toc`.toLowerCase()))
    return;
  const name = tocs
    .map((p) => p.split("/")[1].replace(/\.toc$/i, ""))
    .sort((a, b) => a.length - b.length)[0];
  for (const p of paths) {
    const next = name + p.slice(top.length),
      data = files.get(p),
      crc = crcOf.get(p);
    files.delete(p);
    crcOf.delete(p);
    files.set(next, data);
    crcOf.set(next, crc);
  }
}
// A view into a bigger buffer would be cloned together with that whole buffer by
// IndexedDB, so such views are copied.
const compact = (b) =>
  b.byteOffset === 0 && b.byteLength === b.buffer.byteLength ? b : b.slice();
// Returns [{ ...metadata, files: { path: Uint8Array } }]
export async function importZip(bytes) {
  const meta = inspectZip(bytes),
    raw = unzipSync(bytes, { filter: (f) => meta.has(f.name) }),
    files = new Map(),
    crcOf = new Map();
  for (const [name, data] of Object.entries(raw)) {
    const m = meta.get(name);
    if (!m || data.length !== m.size || crc32(data) !== m.crc)
      throw new AddonError("errorIntegrity", { detail: name });
    if (!m.path.endsWith("/") && !m.path.startsWith("__MACOSX/"))
      (files.set(m.path, data), crcOf.set(m.path, m.crc));
  }
  unwrapBranchFolder(files, crcOf);
  const roots = [];
  for (const path of files.keys()) {
    const parts = path.split("/"),
      toc = parts.pop(),
      folder = parts.at(-1);
    if (folder && toc.toLowerCase() === folder.toLowerCase() + ".toc")
      roots.push({ root: parts.join("/"), folder, toc: path });
  }
  // Also accept a ZIP containing the contents of one addon folder.
  {
    const tocs = [...files.keys()].filter(
      (p) => !p.includes("/") && /\.toc$/i.test(p),
    );
    if (tocs.length === 1)
      roots.push({ root: "", folder: tocs[0].slice(0, -4), toc: tocs[0] });
  }
  if (!roots.length) throw new AddonError("errorToc");
  // Embedded libraries belong to their parent addon, not separate addons.
  const topRoots = roots.filter(
    (r) =>
      !roots.some(
        (parent) =>
          parent !== r &&
          (parent.root === "" ||
            r.root.toLowerCase().startsWith(parent.root.toLowerCase() + "/")),
      ),
  );
  roots.splice(0, roots.length, ...topRoots);
  const addons = [],
    seen = new Set();
  for (const r of roots) {
    if (
      !/^[A-Za-z0-9_-]+$/.test(r.folder) ||
      r.folder.toLowerCase().startsWith("blizzard_")
    )
      throw new AddonError("errorFolder", { detail: r.folder });
    if (seen.has(r.folder.toLowerCase()))
      throw new AddonError("errorFolder", { detail: r.folder });
    seen.add(r.folder.toLowerCase());
    const info = tocMeta(decoder.decode(files.get(r.toc))),
      own = {},
      sizes = {},
      crcs = {};
    let size = 0;
    for (const [p, data] of files) {
      if (r.root && !p.startsWith(r.root + "/")) continue;
      if (
        roots.some(
          (other) =>
            other !== r &&
            other.root.startsWith(r.root + "/") &&
            p.startsWith(other.root + "/"),
        )
      )
        continue;
      const relative = r.root ? p.slice(r.root.length + 1) : p;
      if (/(^|\/)\.(git|svn)(\/|$)/.test(relative)) continue;
      own[relative] = compact(data);
      sizes[relative] = data.length;
      crcs[relative] = crcOf.get(p);
      size += data.length;
    }
    if (
      !Object.keys(own).some(
        (p) => p.toLowerCase() === r.folder.toLowerCase() + ".toc",
      )
    )
      throw new AddonError("errorToc");
    const signature = new TextEncoder().encode(
      JSON.stringify(
        Object.keys(own)
          .sort()
          .map((p) => [p, sizes[p], crcs[p]]),
      ),
    );
    const digest = [
      ...new Uint8Array(await crypto.subtle.digest("SHA-256", signature)),
    ]
      .map((x) => x.toString(16).padStart(2, "0"))
      .join("");
    addons.push({
      ...info,
      title: info.title || r.folder,
      folder: r.folder,
      files: own,
      fileCount: Object.keys(own).length,
      size,
      digest,
      enabled: false,
      importedAt: Date.now(),
      warnings:
        info.interface && !info.interface.split(/[,\s]+/).includes("30300")
          ? ["interface"]
          : [],
    });
  }
  return addons;
}
// Build a validated ZIP for files fetched from a source repository. The normal
// importer still performs its complete path, size and CRC checks afterwards.
export function buildZip(entries) {
  return zipSync(Object.fromEntries(entries), { level: 6 });
}


// Read a response/File stream with hard byte limits; never trust Content-Length alone.
export async function readStreamLimited(stream, limit, errorCode = "errorArchiveSize", budget = null) {
  if (!stream || typeof stream.getReader !== "function")
    throw new AddonError("downloadFailed", { detail: "Streaming response unavailable" });
  const reader = stream.getReader();
  const chunks = [];
  let size = 0;
  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const nextSize = size + value.byteLength;
      if (nextSize > limit || (budget && budget.used + value.byteLength > budget.limit))
        throw new AddonError(errorCode);
      size = nextSize;
      if (budget) budget.used += value.byteLength;
      chunks.push(value);
    }
  } catch (error) {
    try { await reader.cancel(); } catch {}
    throw error;
  } finally {
    reader.releaseLock();
  }
  const bytes = new Uint8Array(size);
  let offset = 0;
  for (const chunk of chunks) {
    bytes.set(chunk, offset);
    offset += chunk.byteLength;
  }
  return bytes;
}

export function exportZip({ folder, files }) {
  return zipSync(
    Object.fromEntries(
      Object.entries(files).map(([p, bytes]) => [folder + "/" + p, bytes]),
    ),
    { level: 6 },
  );
}

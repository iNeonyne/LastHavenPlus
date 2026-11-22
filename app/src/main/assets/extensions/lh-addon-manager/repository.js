import { emptyLibrary } from "./library.js";
import { putAddons } from "./db.js";
import { tocMeta } from "./toc.js";

export const withLock = (name, fn) =>
  navigator.locks ? navigator.locks.request(name, fn) : fn();

const unbase64 = (s) => Uint8Array.from(atob(s), (c) => c.charCodeAt(0));
const key = (folder) => "addon:" + folder;
// Older layouts: v1 kept base64 files inside the library entry, v2 (0.7/0.8) under
// "addon:<folder>" keys in storage.local. Both are moved to IndexedDB as raw bytes.
const isLegacy = async (library) => {
  if (!library?.addons) return false;
  if (library.addons.some((a) => a.files)) return true;
  const got = await browser.storage.local.get(library.addons.map((a) => key(a.folder)));
  return Object.keys(got).length > 0;
};

async function migrate() {
  const { library } = await browser.storage.local.get("library");
  if (!library?.addons || !(await isLegacy(library))) return;
  const addons = [],
    drop = [];
  for (const old of library.addons) {
    const stored = old.files ? null : (await browser.storage.local.get(key(old.folder)))[key(old.folder)];
    const b64 = old.files || stored?.files;
    if (stored) drop.push(key(old.folder));
    if (!b64) {
      addons.push(old);
      continue;
    }
    const files = {};
    for (const [p, s] of Object.entries(b64)) files[p] = unbase64(s);
    await putAddons([{ folder: old.folder, files }]);
    const { files: _files, ...meta } = old;
    const tocPath = Object.keys(files).find(
      (p) => p.toLowerCase() === old.folder.toLowerCase() + ".toc",
    );
    const toc = tocPath ? tocMeta(new TextDecoder().decode(files[tocPath])) : {};
    addons.push({
      ...meta,
      fileCount: Object.keys(files).length,
      dependencies: toc.dependencies ?? meta.dependencies ?? [],
      optionalDeps: toc.optionalDeps ?? [],
      loadOnDemand: toc.loadOnDemand ?? false,
      warnings: meta.warnings?.length ? ["interface"] : [],
    });
  }
  // Same revision: the content did not change, so no "reload needed" notice.
  await browser.storage.local.set({ library: { ...library, addons } });
  if (drop.length) await browser.storage.local.remove(drop);
}

export async function readLibrary() {
  let { library } = await browser.storage.local.get("library");
  if (await isLegacy(library)) {
    await withLock("lh-migrate", migrate);
    ({ library } = await browser.storage.local.get("library"));
  }
  return library?.addons ? library : emptyLibrary();
}
export async function writeLibrary(next) {
  const saved = { ...next, revision: crypto.randomUUID() };
  await browser.storage.local.set({ library: saved });
  return saved;
}

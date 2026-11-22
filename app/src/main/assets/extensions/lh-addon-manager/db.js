// Addon file bytes live in IndexedDB (one record per file, raw bytes).
// Key: "<folder lowercase>/<path>". Folder names are [A-Za-z0-9_-], so the
// first "/" always ends the folder part.
const NAME = "lh-addon-files",
  STORE = "files";
let opened;
function open() {
  return (opened ??= new Promise((resolve, reject) => {
    const req = indexedDB.open(NAME, 1);
    req.onupgradeneeded = () => req.result.createObjectStore(STORE);
    req.onsuccess = () => {
      req.result.onversionchange = () => {
        req.result.close();
        opened = null;
      };
      resolve(req.result);
    };
    req.onerror = () => {
      opened = null;
      reject(req.error);
    };
  }));
}
const prefix = (folder) => folder.toLowerCase() + "/";
const folderRange = (folder) =>
  IDBKeyRange.bound(prefix(folder), prefix(folder) + "\uffff");
const finished = (tx) =>
  new Promise((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
    tx.onabort = () => reject(tx.error || new Error("Transaction aborted"));
  });

// Replaces the stored files of every listed addon in ONE transaction.
export async function putAddons(entries) {
  const db = await open();
  const tx = db.transaction(STORE, "readwrite");
  const store = tx.objectStore(STORE);
  for (const { folder, files } of entries) {
    store.delete(folderRange(folder));
    for (const [path, bytes] of Object.entries(files))
      store.put(bytes, prefix(folder) + path);
  }
  await finished(tx);
}

export async function deleteAddon(folder) {
  const db = await open();
  const tx = db.transaction(STORE, "readwrite");
  tx.objectStore(STORE).delete(folderRange(folder));
  await finished(tx);
}

export async function getAllFiles(folder) {
  const db = await open();
  const store = db.transaction(STORE).objectStore(STORE);
  const range = folderRange(folder);
  const [keys, values] = await Promise.all(
    [store.getAllKeys(range), store.getAll(range)].map(
      (r) =>
        new Promise((resolve, reject) => {
          r.onsuccess = () => resolve(r.result);
          r.onerror = () => reject(r.error);
        }),
    ),
  );
  const files = {};
  keys.forEach((k, i) => (files[k.slice(prefix(folder).length)] = values[i]));
  return files;
}

// Reads files of one addon in order, stopping once ~limit bytes were collected.
// `after` is the `next` value of the previous call (null for the first call).
export async function readBatch(folder, after, limit) {
  const db = await open();
  const pre = prefix(folder);
  const range = after
    ? IDBKeyRange.bound(after, pre + "\uffff", true, false)
    : folderRange(folder);
  return new Promise((resolve, reject) => {
    const files = {};
    let bytes = 0;
    const req = db.transaction(STORE).objectStore(STORE).openCursor(range);
    req.onsuccess = () => {
      const c = req.result;
      if (!c) return resolve({ files, next: null });
      files[c.key.slice(pre.length)] = c.value;
      bytes += c.value.length;
      if (bytes >= limit) return resolve({ files, next: c.key });
      c.continue();
    };
    req.onerror = () => reject(req.error);
  });
}

// Removes stored files of folders that are not in the library anymore.
export async function gc(keepFolders) {
  const keep = new Set(keepFolders.map((f) => f.toLowerCase()));
  const db = await open();
  const stale = await new Promise((resolve, reject) => {
    const found = new Set();
    const req = db.transaction(STORE).objectStore(STORE).openKeyCursor();
    req.onsuccess = () => {
      const c = req.result;
      if (!c) return resolve(found);
      const folder = c.key.slice(0, c.key.indexOf("/"));
      if (!keep.has(folder)) found.add(folder);
      c.continue(folder + "0"); // first key after every "folder/..." key
    };
    req.onerror = () => reject(req.error);
  });
  if (!stale.size) return 0;
  const tx = db.transaction(STORE, "readwrite");
  for (const folder of stale) tx.objectStore(STORE).delete(folderRange(folder));
  await finished(tx);
  return stale.size;
}

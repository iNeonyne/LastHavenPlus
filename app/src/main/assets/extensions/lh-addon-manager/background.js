import { readLibrary } from "./repository.js";
import { loadOrder } from "./library.js";
import { readBatch } from "./db.js";

const GAME = /^https:\/\/([a-z0-9-]+\.)*lasthavens\.com(\/|$)/;
const BATCH_BYTES = 8 * 1024 * 1024;
browser.action.onClicked.addListener(() => browser.runtime.openOptionsPage());

// Enabled addons, dependencies first. File bytes are fetched separately, in
// batches, so no single message has to carry the whole library.
async function list() {
  const library = await readLibrary();
  const { order } = loadOrder(library.addons.filter((a) => a.enabled));
  return {
    revision: library.revision,
    addons: order.map((a) => ({ folder: a.folder })),
  };
}
function files(m) {
  if (
    typeof m.folder !== "string" ||
    !/^[A-Za-z0-9_-]+$/.test(m.folder) ||
    (m.after != null && typeof m.after !== "string")
  )
    throw Error("Bad request");
  return readBatch(m.folder, m.after ?? null, BATCH_BYTES);
}

browser.runtime.onMessage.addListener((m, sender) => {
  if (!m || sender.id !== browser.runtime.id || !GAME.test(sender.url || ""))
    return;
  if (m.kind === "diagnostic")
    browser.storage.session.set({ lastStatus: m.status });
  else if (m.kind === "seen") {
    const entry = String(m.url).slice(0, 200) + (m.top ? " (page)" : " (frame)");
    browser.storage.session.get("seen").then(({ seen = [] }) =>
      browser.storage.session.set({
        seen: [entry, ...seen.filter((x) => x !== entry)].slice(0, 5),
      }),
    );
  } else if (m.kind === "list") return list();
  else if (m.kind === "files") return files(m);
});

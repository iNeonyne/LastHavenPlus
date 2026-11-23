// Runs in the extension's isolated world. Hands the enabled add-ons to inject.js.
(() => {
  "use strict";
  const CH = "lh-addons-v2",
    isTop = window.top === window;
  // Tell the manager page where the extension is actually running (helps when a page misbehaves).
  try {
    browser.runtime
      .sendMessage({
        kind: "seen",
        url: location.origin + location.pathname,
        top: isTop,
      })
      .catch(() => {});
  } catch (e) {}
  let payload = null,
    lastSend = 0,
    building = null;
  const send = (force) => {
    if (!payload) return;
    const now = Date.now();
    if (!force && now - lastSend < 1500) return;
    lastSend = now;
    window.postMessage({ channel: CH, kind: "payload", payload }, location.origin);
  };
  // Reads one addon at a time, in batches. A broken addon is reported on its own and
  // does not stop the others from loading.
  const build = () =>
    (building ||= (async () => {
      try {
        const [{ revision, addons }, got] = await Promise.all([
          browser.runtime.sendMessage({ kind: "list" }),
          browser.storage.local.get("settings"),
        ]);
        const out = [];
        for (const { folder, storageFolder } of addons) {
          try {
            const files = {};
            let after = null;
            do {
              const r = await browser.runtime.sendMessage({
                kind: "files",
                folder: storageFolder || folder,
                after,
              });
              Object.assign(files, r.files);
              after = r.next;
            } while (after);
            out.push({ folder, files });
          } catch (e) {
            out.push({ folder, error: String(e?.message || e) });
          }
        }
        payload = {
          revision: String(revision),
          badge: got.settings?.badge !== false,
          addons: out,
        };
      } catch (e) {
        payload = { revision: "error", badge: true, addons: [], error: String(e) };
      }
      send(true);
    })());
  // The top frame prepares right away; other frames only when their page asks.
  if (isTop) build();
  window.addEventListener("message", (event) => {
    const d = event.data;
    if (!d || d.channel !== CH || event.origin !== location.origin) return;
    if (d.kind === "hello") {
      build();
      send(false);
    } else if (d.kind === "status") {
      const s = d.status;
      if (!s || !["ready", "error", "pending"].includes(s.state)) return;
      // Page diagnostics are untrusted data; they can never change the library.
      browser.runtime
        .sendMessage({
          kind: "diagnostic",
          status: {
            state: s.state,
            message: String(s.message || "").slice(0, 1000),
            revision: String(s.revision || "").slice(0, 80),
            loaded: Array.isArray(s.loaded)
              ? s.loaded.slice(0, 100).map((x) => String(x).slice(0, 120))
              : [],
            prepared: Array.isArray(s.prepared)
              ? s.prepared.slice(0, 100).map((x) => String(x).slice(0, 120))
              : [],
            failed: Array.isArray(s.failed)
              ? s.failed.slice(0, 100).map((f) => ({
                  folder: String(f?.folder).slice(0, 120),
                  message: String(f?.message || "").slice(0, 300),
                }))
              : [],
            at: Date.now(),
          },
        })
        .catch(() => {});
    }
  });
  browser.storage.onChanged.addListener((changes, area) => {
    if (area === "local" && changes.library)
      window.postMessage({ channel: CH, kind: "changed" }, location.origin);
  });
})();

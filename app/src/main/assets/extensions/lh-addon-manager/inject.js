// Runs in the game page (MAIN world) at document_start.
(() => {
  "use strict";
  if (window.__lhAddonsActive) return;
  window.__lhAddonsActive = true;
  const O = globalThis.__lhOverlay;
  delete globalThis.__lhOverlay;
  if (!O) return;

  const CH = "lh-addons-v2",
    NativeWorker = window.Worker,
    nativeFetch = window.fetch.bind(window);

  // ---------- on-screen status badge (tap to open the log) ----------
  const lines = [],
    agg = {},
    requestedAddons = new Set();
  let activeRevision = "", activeFailed = [];
  let state = { kind: "wait", text: "waiting" },
    wrapped = 0,
    gameSeen = false, // set once the game creates its first worker; the badge never shows before
    badgeOn = null, // null = setting not known yet; nothing is drawn until it is
    hiddenNow = false,
    paintTimer = 0;
  const ui = {};
  const sum = (k) => Object.values(agg).reduce((n, c) => n + (c[k] || 0), 0);
  function summary() {
    if (state.kind === "err") return { text: "LH ✗ " + state.text, color: "#c0392b" };
    if (state.kind === "ok") {
      const files = sum("xhrLocal") + sum("fetchLocal");
      return {
        text:
          "LH ✓ " + state.count + " add-on" + (state.count === 1 ? "" : "s") + " · " + files + " files · " + wrapped + " workers" +
          (state.failed ? " · " + state.failed + " failed" : ""),
        color: state.failed ? "#9a6b00" : "#2e7d32",
      };
    }
    return { text: "LH … " + state.text, color: "#9a6b00" };
  }
  function log(msg, src) {
    lines.push(new Date().toTimeString().slice(0, 8) + " [" + (src || "page") + "] " + msg);
    if (lines.length > 300) lines.shift();
    try {
      console.log("[LH addons]", src || "page", msg);
    } catch (e) {}
    paint();
  }
  function handle(m) {
    if (!m) return;
    if (m.msg) log(m.msg, m.src);
    if (m.counters) {
      agg[m.src] = m.counters;
      paint();
    }
    if (m.addon && /^[A-Za-z0-9_-]{1,120}$/.test(String(m.addon))) {
      const folder = String(m.addon);
      const fresh = ![...requestedAddons].some((x) => x.toLowerCase() === folder.toLowerCase());
      requestedAddons.add(folder);
      if (fresh && activeRevision) {
        log("game requested addon file: " + folder, m.src || "worker");
        report("ready", requestedAddons.size + " addon(s) requested files by the game", [...requestedAddons], activeRevision, activeFailed, state.prepared || []);
      }
    }
  }
  function copyLog() {
    const text = lines.join("\n");
    const fallback = () => {
      const ta = document.createElement("textarea");
      ta.value = text;
      ui.panel.appendChild(ta);
      ta.select();
      try {
        document.execCommand("copy");
      } catch (e) {}
      ta.remove();
    };
    try {
      navigator.clipboard.writeText(text).catch(fallback);
    } catch (e) {
      fallback();
    }
  }
  function mount() {
    if (ui.root || hiddenNow || !gameSeen || !badgeOn || !document.documentElement) return;
    const root = document.createElement("div"),
      pill = document.createElement("div"),
      panel = document.createElement("div"),
      pre = document.createElement("pre"),
      bar = document.createElement("div");
    root.style.cssText = "position:fixed;top:4px;left:4px;z-index:2147483647;font:11px/1.35 monospace;color:#fff;max-width:92vw";
    pill.style.cssText = "display:inline-block;padding:3px 8px;border-radius:10px;background:#555;cursor:pointer;opacity:.85";
    panel.style.cssText = "display:none;margin-top:4px;padding:6px;border-radius:8px;background:rgba(0,0,0,.88);max-height:45vh;overflow:auto";
    pre.style.cssText = "margin:0;white-space:pre-wrap;word-break:break-all;font:inherit";
    bar.style.cssText = "display:flex;gap:6px;margin-bottom:6px";
    const button = (label, fn) => {
      const b = document.createElement("button");
      b.textContent = label;
      b.style.cssText = "font:inherit;padding:4px 8px;border-radius:6px;border:1px solid #888;background:#222;color:#fff";
      b.onclick = (e) => {
        e.stopPropagation();
        fn();
      };
      return b;
    };
    bar.append(
      button("Copy log", copyLog),
      button("Hide", () => {
        hiddenNow = true;
        root.remove();
        ui.root = null;
      }),
    );
    panel.append(bar, pre);
    root.append(pill, panel);
    pill.onclick = () => {
      panel.style.display = panel.style.display === "none" ? "block" : "none";
      paint();
    };
    document.documentElement.appendChild(root);
    Object.assign(ui, { root, pill, panel, pre });
  }
  function paint() {
    if (paintTimer) return;
    paintTimer = setTimeout(() => {
      paintTimer = 0;
      mount();
      if (!ui.root) return;
      const s = summary();
      ui.pill.textContent = s.text;
      ui.pill.style.background = s.color;
      if (ui.panel.style.display !== "none") {
        ui.pre.textContent = lines.join("\n");
        ui.panel.scrollTop = ui.panel.scrollHeight;
      }
    }, 120);
  }
  document.addEventListener("DOMContentLoaded", paint);
  try {
    new BroadcastChannel("lh-addons-dbg").onmessage = (e) => handle(e.data);
  } catch (e) {
    log("BroadcastChannel unavailable: worker logs will be missing");
  }
  log("script started in " + location.origin + location.pathname + " (v0.9.0)");

  // Report the game's own origin storage estimate; this excludes manager-origin data.
  async function reportGameStorage() {
    try {
      if (!navigator.storage || typeof navigator.storage.estimate !== "function") return;
      const estimate = await navigator.storage.estimate();
      const usage = Number(estimate.usage), quota = Number(estimate.quota);
      if (!Number.isFinite(usage) || usage < 0) return;
      window.postMessage({
        channel: CH, kind: "gameStorage", usage: Math.floor(usage),
        quota: Number.isFinite(quota) && quota >= usage ? Math.floor(quota) : null,
        at: Date.now()
      }, location.origin);
    } catch (error) {
      log("game storage estimate unavailable: " + String(error));
    }
  }
  reportGameStorage();
  setInterval(reportGameStorage, 5 * 60 * 1000);

  // ---------- talking to the extension bridge ----------
  let received = false,
    gotPayload,
    readyP = null;
  const payloadP = new Promise((r) => (gotPayload = r)); // the payload, or null on timeout
  const empty = { data: { buf: new ArrayBuffer(0), files: {} }, spans: [], used: [] };
  const report = (st, message, loaded = [], revision = "", failed = [], prepared = []) =>
    window.postMessage({ channel: CH, kind: "status", status: { state: st, message, revision, loaded, failed, prepared, at: Date.now() } }, location.origin);
  const hello = () => window.postMessage({ channel: CH, kind: "hello" }, location.origin);
  let helloTimer = 0;

  async function prepare(payload) {
    if (payload.error) throw new Error(payload.error);
    if (!(payload.addons || []).length) return { ...empty, fileCount: 0, bytes: 0, skipped: [] };
    let manifestText = "",
      idxText = "";
    try {
      manifestText = await (await nativeFetch("/game-manifest.txt")).text();
      log("game manifest read: " + manifestText.length + " chars");
    } catch (e) {
      log("could not read game manifest: " + e);
    }
    try {
      const root = await navigator.storage.getDirectory();
      idxText = await (await (await root.getFileHandle("blocks.idx")).getFile()).text();
      log("blocks.idx read: " + idxText.length + " chars");
    } catch (e) {
      log("blocks.idx not read (" + (e && e.name) + ") - continuing");
    }
    const useShared = typeof SharedArrayBuffer === "function" && globalThis.crossOriginIsolated === true;
    const pkg = O.buildPackage(payload, manifestText, idxText, useShared);
    log("package built: shared memory " + (useShared ? "yes" : "no") + ", mask spans " + pkg.spans.length);
    O.installOverlay(pkg.data, location.href, "page", handle);
    return pkg;
  }

  // Only runs once the game actually starts a worker, so ordinary site pages stay untouched.
  async function process(p) {
    if (!p) {
      badgeOn = true; // always show this failure
      state = { kind: "err", text: "no answer from native manager" };
      log("TIMEOUT: the native add-on bridge did not answer. Check that this WebView provider supports the required WebKit messaging features.");
      report("error", "Native add-on payload timeout. Starting without local add-ons.");
      return empty;
    }
    try {
      const pkg = await prepare(p);
      const msg =
        pkg.used.length + " add-on(s) ready, " + pkg.fileCount + " files, " + Math.round(pkg.bytes / 1024) + " KB" +
        (pkg.skipped.length ? "; skipped (already built in): " + pkg.skipped.join(", ") : "");
      activeRevision = p.revision;
      activeFailed = pkg.failed || [];
      requestedAddons.clear();
      state = { kind: "ok", count: pkg.used.length, failed: activeFailed.length, prepared: pkg.used };
      log(msg);
      for (const f of pkg.failed || []) log("FAILED " + f.folder + ": " + f.message);
      report("ready", msg + ". Waiting for the game to request addon files.", [], p.revision, pkg.failed || [], pkg.used);
      return pkg;
    } catch (err) {
      const m = String((err && err.message) || err);
      state = { kind: "err", text: m.slice(0, 60) };
      log("ERROR: " + m);
      report("error", m);
      return empty;
    }
  }
  const getReady = () => (readyP ||= payloadP.then(process));

  addEventListener("message", (e) => {
    const d = e.data;
    if (!d || d.channel !== CH || e.origin !== location.origin) return;
    if (d.kind === "payload" && !received) {
      received = true;
      clearInterval(helloTimer);
      const p = d.payload || {};
      badgeOn = p.badge !== false;
      log("data received from native storage: " + (p.addons || []).length + " enabled add-on(s)");
      gotPayload(p);
    } else if (d.kind === "changed" && gameSeen) {
      report("pending", "Library changed. Reload the game to apply.");
    }
  });
  // The top frame asks for the payload right away; other frames only once the game
  // starts a worker, so ordinary iframes never load the library.
  let talking = false;
  function startTalking() {
    if (talking) return;
    talking = true;
    helloTimer = setInterval(hello, 400);
    hello();
    setTimeout(() => {
      if (received) return;
      received = true;
      clearInterval(helloTimer);
      gotPayload(null);
    }, 20000);
  }
  if (window.top === window) startTalking();

  // ---------- game worker wrapping ----------
  const short = (u) => String(u).replace(location.origin, "").slice(0, 90);
  window.Worker = class extends NativeWorker {
    constructor(url, options) {
      const name = options && options.name ? String(options.name) : "";
      if (!/pthread/i.test(name) || (options && options.type === "module")) {
        super(url, options);
        log("worker not wrapped: name='" + name + "' type=" + ((options && options.type) || "classic") + " " + short(url));
        return;
      }
      const original = new URL(String(url), location.href).href;
      const id = ++wrapped;
      gameSeen = true;
      startTalking();
      super(URL.createObjectURL(new Blob([O.workerSource(original)], { type: "application/javascript" })), options);
      if (id <= 2) log("worker#" + id + " wrapped: " + short(original));
      else if (id === 3) log("further game workers are wrapped quietly (see the count above)");
      getReady().then((pkg) => {
        const msg = { __lhBoot: true, lh: pkg.data, spans: pkg.spans, who: "worker#" + id };
        try {
          this.postMessage(msg);
        } catch (err) {
          log("worker#" + id + " shared memory failed (" + err + "), copying instead");
          this.postMessage({ ...msg, lh: { files: pkg.data.files, buf: new Uint8Array(pkg.data.buf).slice().buffer } });
        }
        paint();
      });
    }
  };
})();

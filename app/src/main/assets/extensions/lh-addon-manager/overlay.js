// Page-side core (MAIN world). Defines helpers on a temporary global that inject.js picks up.
// installOverlay() is stringified into each game worker, so it must stay fully self-contained.
(() => {
  "use strict";

  // Turns the enabled add-ons (files as Uint8Array) into one shared memory block + an index, and works out which
  // cached blocks.idx records must be hidden while reading. Pure function: easy to test.
  function buildPackage(payload, manifestText, idxText, useShared) {
    const lower = String(manifestText || "").toLowerCase();
    const used = [],
      skipped = [],
      failed = [],
      chunks = [];
    let total = 0;
    for (const a of payload.addons || []) {
      try {
        if (a.error) throw new Error(a.error);
        if (lower.includes("interface/addons/" + a.folder.toLowerCase() + "/")) {
          skipped.push(a.folder);
          throw new Error("Built-in addon folder conflict");
        }
        const mine = [];
        let size = 0;
        for (const [p, u] of Object.entries(a.files || {})) {
          if (!(u instanceof Uint8Array)) throw new Error("Unreadable file data: " + p);
          mine.push(["Interface/AddOns/" + a.folder + "/" + p, u]);
          size += u.length;
        }
        used.push(a.folder);
        chunks.push(...mine);
        total += size;
      } catch (e) {
        failed.push({ folder: a.folder, message: String((e && e.message) || e) });
      }
    }
    const buf = useShared ? new SharedArrayBuffer(total) : new ArrayBuffer(total);
    const view = new Uint8Array(buf),
      files = {};
    let off = 0;
    for (const [path, u] of chunks) {
      view.set(u, off);
      files[path] = { off, size: u.length };
      off += u.length;
    }
    const spans = [];
    if (idxText && used.length) {
      const enc = new TextEncoder();
      let offset = 0;
      for (const line of idxText.split("\n")) {
        const cols = line.split("\t");
        if (
          cols.length === 5 &&
          used.some((f) =>
            cols[4].toLowerCase().startsWith("interface/addons/" + f.toLowerCase() + "/"),
          )
        ) {
          spans.push([offset + line.indexOf("\t") + 1, cols[1].length, 48]);
          spans.push([offset + line.lastIndexOf("\t") + 1, 1, 35]);
        }
        offset += enc.encode(line + "\n").length;
      }
    }
    return { data: { buf, files }, spans, used, skipped, failed, bytes: total, fileCount: chunks.length };
  }

  // Answers the game's requests for add-on files from memory, and appends the add-on files
  // to the game manifest. Works for XMLHttpRequest (sync + async) and fetch.
  function installOverlay(data, base, who, sink) {
    const files = data.files,
      paths = Object.keys(files),
      bytes = new Uint8Array(data.buf);
    const counters = {
      xhrLocal: 0,
      xhrManifest: 0,
      xhrBundle: 0,
      fetchLocal: 0,
      fetchManifest: 0,
      fetchBundle: 0,
    };
    if (!sink) {
      try {
        const bc = new BroadcastChannel("lh-addons-dbg");
        sink = (m) => bc.postMessage(m);
      } catch (e) {
        sink = () => {};
      }
    }
    const say = (msg) => {
      try {
        sink({ src: who, msg });
      } catch (e) {}
    };
    let flushing = false;
    const bump = (key, what, addon) => {
      const n = ++counters[key];
      if (n <= 3) say(key + " #" + n + ": " + what);
      if (!flushing) {
        flushing = true;
        setTimeout(() => {
          flushing = false;
          try {
            sink({ src: who, counters: Object.assign({}, counters), addon: addon || null });
          } catch (e) {}
        }, 150);
      } else if (addon) {
        // Send each newly observed addon at least once; the page aggregates this set.
        try { sink({ src: who, addon }); } catch (e) {}
      }
    };
    const extra = paths.map((p) => files[p].size + "\t" + p).join("\n");
    const patchManifest = (t) => (extra ? String(t).trimEnd() + "\n" + extra + "\n" : t);
    const dec = new TextDecoder(),
      enc = new TextEncoder();

    const fileSet = new Set(paths),
      addonRoots = new Set(paths.map((p) => p.split("/").slice(0, 3).join("/")));
    function findLocal(path) {
      if (path.startsWith("/game/")) {
        const p = path.slice(6);
        if (fileSet.has(p)) return p;
      }
      if (path.startsWith("/blocks/")) {
        // /blocks/<file path>/<block>.bin : try each folder level after "/blocks/"
        for (let i = path.indexOf("/", 8); i > 0; i = path.indexOf("/", i + 1)) {
          const p = path.slice(8, i);
          if (fileSet.has(p)) return p;
        }
      }
      return null;
    }
    function classify(pathname) {
      let path;
      try {
        path = decodeURIComponent(pathname);
      } catch (e) {
        path = pathname;
      }
      if (path === "/game-manifest.txt") return { kind: "manifest", path };
      if (
        /^\/blocks\/Interface\/AddOns\/[^/]+\/bundle-/.test(path) &&
        addonRoots.has("Interface/AddOns/" + path.split("/")[4])
      )
        return { kind: "bundle", path };
      const local = findLocal(path);
      if (local) return { kind: "local", path, local };
      return { kind: "other", path };
    }
    function localSlice(c, range) {
      const f = files[c.local];
      let start = 0,
        end = f.size;
      const blk = c.path.match(/\/(\d+)-(\d+)-(\d+)k\.bin$/);
      if (blk) {
        start = Number(blk[2]) * Number(blk[3]) * 1024;
        end = start + Number(blk[3]) * 1024;
      }
      if (range) {
        const m = String(range).match(/bytes=(\d+)-(\d+)/);
        if (m) {
          start = +m[1];
          end = +m[2] + 1;
        }
      }
      start = Math.min(start, f.size);
      end = Math.max(start, Math.min(end, f.size));
      return {
        u8: bytes.subarray(f.off + start, f.off + end).slice(),
        start,
        end,
        total: f.size,
      };
    }
    const rangeHeader = (s) => "bytes " + s.start + "-" + (s.end - 1) + "/" + s.total;
    const toBody = (u8, type) => {
      if (type === "arraybuffer") return u8.buffer;
      if (type === "blob") return new Blob([u8]);
      if (type === "json") {
        try {
          return JSON.parse(dec.decode(u8));
        } catch (e) {
          return null;
        }
      }
      return dec.decode(u8);
    };

    const NativeXHR = globalThis.XMLHttpRequest;
    if (NativeXHR) {
      globalThis.XMLHttpRequest = class extends NativeXHR {
        open(method, url, ...rest) {
          this._lhA = null;
          this._lhRange = null;
          this._lhC = null;
          this._lhManifest = false;
          let u = null;
          try {
            u = new URL(String(url), base);
          } catch (e) {}
          if (!u) return super.open(method, url, ...rest);
          this._lhM = String(method).toUpperCase();
          this._lhC = classify(u.pathname);
          this._lhAsync = rest[0] !== false;
          return super.open(method, u.href, ...rest);
        }
        setRequestHeader(k, v) {
          if (this._lhC && this._lhC.kind === "local" && String(k).toLowerCase() === "range") {
            this._lhRange = v;
            return;
          }
          return super.setRequestHeader(k, v);
        }
        send(body) {
          const c = this._lhC,
            m = this._lhM;
          if (c && c.kind === "bundle" && m === "GET") {
            bump("xhrBundle", c.path);
            return this._lhAnswer(404, new Uint8Array(0), null);
          }
          if (c && c.kind === "local" && (m === "GET" || m === "HEAD")) {
            bump("xhrLocal", c.path + (this._lhRange ? " " + this._lhRange : ""), c.local.split("/")[2]);
            const s = localSlice(c, this._lhRange);
            return this._lhAnswer(
              this._lhRange ? 206 : 200,
              m === "HEAD" ? new Uint8Array(0) : s.u8,
              this._lhRange ? rangeHeader(s) : null,
            );
          }
          if (c && c.kind === "manifest" && m === "GET") {
            this._lhManifest = true;
            bump("xhrManifest", "patched");
          }
          return super.send(body);
        }
        _lhAnswer(status, u8, range) {
          this._lhA = { status, u8, range, cache: {} };
          if (this._lhAsync)
            setTimeout(() => {
              const len = u8.length;
              const fire = (t, progress) => {
                try {
                  this.dispatchEvent(
                    progress
                      ? new ProgressEvent(t, { lengthComputable: true, loaded: len, total: len })
                      : new Event(t),
                  );
                } catch (e) {}
              };
              fire("readystatechange");
              fire("progress", true);
              fire("load", true);
              fire("loadend", true);
            }, 0);
        }
        abort() {
          if (this._lhA) {
            this._lhA = null;
            return;
          }
          return super.abort();
        }
        get status() {
          return this._lhA ? this._lhA.status : super.status;
        }
        get statusText() {
          if (!this._lhA) return super.statusText;
          return this._lhA.status === 404 ? "Not Found" : this._lhA.status === 206 ? "Partial Content" : "OK";
        }
        get readyState() {
          return this._lhA ? 4 : super.readyState;
        }
        get responseText() {
          if (this._lhA) return toBody(this._lhA.u8, "text");
          const t = super.responseText;
          return this._lhManifest && super.readyState === 4 && super.status === 200 ? patchManifest(t) : t;
        }
        get response() {
          const a = this._lhA;
          if (a) {
            const type = this.responseType || "text";
            return (a.cache[type] ??= toBody(a.u8, type));
          }
          const r = super.response;
          if (this._lhManifest && super.readyState === 4 && super.status === 200) {
            const rt = this.responseType;
            if (rt === "" || rt === "text") return patchManifest(r);
            if (rt === "arraybuffer") return enc.encode(patchManifest(dec.decode(r))).buffer;
          }
          return r;
        }
        getResponseHeader(n) {
          const a = this._lhA;
          if (!a) return super.getResponseHeader(n);
          const k = String(n).toLowerCase();
          if (k === "content-length") return String(a.u8.length);
          if (k === "accept-ranges") return "bytes";
          if (k === "content-range") return a.range;
          return null;
        }
        getAllResponseHeaders() {
          const a = this._lhA;
          if (!a) return super.getAllResponseHeaders();
          return (
            "content-length: " + a.u8.length + "\r\naccept-ranges: bytes\r\n" +
            (a.range ? "content-range: " + a.range + "\r\n" : "")
          );
        }
      };
    }

    const NativeFetch = globalThis.fetch;
    if (typeof NativeFetch === "function") {
      globalThis.fetch = function (input, init) {
        let c = null,
          method = "GET",
          range = null;
        try {
          const isReq = typeof Request === "function" && input instanceof Request;
          const url = isReq ? input.url : String(input);
          method = String((init && init.method) || (isReq && input.method) || "GET").toUpperCase();
          c = classify(new URL(url, base).pathname);
          if (c.kind === "local")
            range = new Headers((init && init.headers) || (isReq ? input.headers : undefined)).get("range");
        } catch (e) {
          c = null;
        }
        if (c && c.kind === "bundle" && method === "GET") {
          bump("fetchBundle", c.path);
          return Promise.resolve(new Response("", { status: 404 }));
        }
        if (c && c.kind === "local" && (method === "GET" || method === "HEAD")) {
          bump("fetchLocal", c.path + (range ? " " + range : ""), c.local.split("/")[2]);
          const s = localSlice(c, range);
          const h = { "Content-Length": String(s.u8.length), "Accept-Ranges": "bytes" };
          if (range) h["Content-Range"] = rangeHeader(s);
          return Promise.resolve(
            new Response(method === "HEAD" ? null : s.u8, { status: range ? 206 : 200, headers: h }),
          );
        }
        if (c && c.kind === "manifest" && method === "GET") {
          bump("fetchManifest", "patched");
          return NativeFetch.call(globalThis, input, init).then(async (r) => {
            if (!r.ok) return r;
            const h = new Headers(r.headers);
            h.delete("content-length");
            h.delete("content-encoding");
            return new Response(patchManifest(await r.text()), {
              status: r.status,
              statusText: r.statusText,
              headers: h,
            });
          });
        }
        return NativeFetch.call(globalThis, input, init);
      };
    }
    if (who === "page" || who === "worker#1") say("overlay installed: " + paths.length + " files (" + who + ")");
    return counters;
  }

  // Source of the replacement worker: waits for the boot message, installs the overlay,
  // loads the game's real worker script, then replays anything received in the meantime.
  function workerSource(original) {
    const o = JSON.stringify(original);
    return (
      "const __lhQ=[];function __lhBoot(e){var d=e.data;if(!d||!d.__lhBoot){__lhQ.push(e);return}" +
      "removeEventListener('message',__lhBoot);" +
      "try{if(Object.keys(d.lh.files).length)(" + installOverlay.toString() + ")(d.lh," + o + ",d.who)}" +
      "catch(err){try{new BroadcastChannel('lh-addons-dbg').postMessage({src:d.who,msg:'install failed: '+err})}catch(_){}}" +
      "importScripts(" + o + ");" +
      "if(d.spans.length&&typeof assignWasmImports==='function'){const a=assignWasmImports;" +
      "assignWasmImports=function(){a();const io=wasmImports.WebVfsStoreIo;" +
      "wasmImports.WebVfsStoreIo=function(file,write,ptr,count,at){const got=io(file,write,ptr,count,at);" +
      "if(file===1&&!write&&got>0){for(const s of d.spans){const lo=Math.max(s[0],at),hi=Math.min(s[0]+s[1],at+got);" +
      "if(hi>lo)HEAPU8.fill(s[2],ptr+lo-at,ptr+hi-at)}}return got}}}" +
      "for(const q of __lhQ)dispatchEvent(new MessageEvent('message',{data:q.data}))}" +
      "addEventListener('message',__lhBoot);"
    );
  }

  globalThis.__lhOverlay = { buildPackage, installOverlay, workerSource };
})();

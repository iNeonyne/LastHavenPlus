// Native read-only game bridge. Injected only into the approved Lasthavens HTTPS origins.
(() => {
  "use strict";
  if (window.__lhNativeGameBridge) return;
  window.__lhNativeGameBridge = true;

  const CHANNEL = "lh-addons-v2";
  const native = window.LastHavenNative;
  if (!native || typeof native.postMessage !== "function") return;

  let pending = null;
  let started = false;
  let completedPayload = null;

  native.addEventListener("message", (event) => {
    if (!pending) return;
    const current = pending;
    pending = null;
    clearTimeout(current.timer);
    if (current.expected === "buffer" && event.data instanceof ArrayBuffer) {
      current.resolve(new Uint8Array(event.data));
      return;
    }
    if (current.expected === "string" && typeof event.data === "string") {
      try {
        const response = JSON.parse(event.data);
        if (response && response.kind === "error") throw new Error(response.message || "Native bridge error");
        current.resolve(response);
      } catch (error) {
        current.reject(error);
      }
      return;
    }
    current.reject(new Error("Native bridge returned an unexpected response type"));
  });

  function request(message, expected) {
    if (pending) return Promise.reject(new Error("Native bridge request already in progress"));
    return new Promise((resolve, reject) => {
      const current = {
        expected,
        resolve,
        reject,
        timer: setTimeout(() => {
          if (pending !== current) return;
          pending = null;
          reject(new Error("Native add-on bridge timed out"));
        }, 20000),
      };
      pending = current;
      try {
        native.postMessage(JSON.stringify(message));
      } catch (error) {
        clearTimeout(current.timer);
        pending = null;
        reject(error);
      }
    });
  }

  async function loadPayload() {
    const response = await request({ kind: "payload" }, "string");
    if (!response || response.kind !== "payload" || !response.payload) {
      throw new Error("Native add-on library response is invalid");
    }
    const payload = response.payload;
    if (payload.error) throw new Error(String(payload.error));
    if (!Array.isArray(payload.addons)) throw new Error("Native add-on list is invalid");

    for (const addon of payload.addons) {
      const files = {};
      for (const file of addon.files || []) {
        const size = Number(file.size);
        if (!Number.isSafeInteger(size) || size < 0 || size > 64 * 1024 * 1024) {
          throw new Error("Invalid file size for " + addon.folder + "/" + file.path);
        }
        const bytes = new Uint8Array(size);
        for (let offset = 0; offset < size; offset += 512 * 1024) {
          const length = Math.min(512 * 1024, size - offset);
          const chunk = await request({
            kind: "chunk",
            storageFolder: addon.storageFolder,
            path: file.path,
            offset,
            length,
          }, "buffer");
          if (chunk.byteLength !== length) throw new Error("Incomplete file transfer: " + file.path);
          bytes.set(chunk, offset);
        }
        files[file.path] = bytes;
      }
      addon.files = files;
      delete addon.storageFolder;
    }
    completedPayload = payload;
    window.postMessage({ channel: CHANNEL, kind: "payload", payload }, location.origin);
  }

  window.addEventListener("message", (event) => {
    if (event.source !== window || event.origin !== location.origin) return;
    const message = event.data;
    if (!message || message.channel !== CHANNEL) return;
    if (message.kind === "hello") {
      if (completedPayload) {
        window.postMessage({ channel: CHANNEL, kind: "payload", payload: completedPayload }, location.origin);
      } else if (!started) {
        started = true;
        loadPayload().catch((error) => {
          const payload = {
            revision: "native-error",
            badge: true,
            addons: [],
            error: String(error && error.message ? error.message : error).slice(0, 500),
          };
          completedPayload = payload;
          window.postMessage({ channel: CHANNEL, kind: "payload", payload }, location.origin);
        });
      }
    } else if (message.kind === "status" || message.kind === "gameStorage") {
      // Diagnostics are intentionally not a privileged storage mutation API.
      try {
        console.info("[LastHavenPlus] game bridge:", message.kind);
      } catch (_) {}
    }
  });
})();

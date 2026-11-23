(() => {
  "use strict";
  if (window.top !== window || window.__lhChromiumProbeInstalled) return;
  window.__lhChromiumProbeInstalled = true;

  let pageErrors = 0;
  let rejectedPromises = 0;
  let contextLosses = 0;

  addEventListener("error", () => { pageErrors += 1; }, true);
  addEventListener("unhandledrejection", () => { rejectedPromises += 1; });
  addEventListener("webglcontextlost", () => { contextLosses += 1; }, true);

  function webglInfo(kind) {
    const canvas = document.createElement("canvas");
    let context = null;
    try {
      context = canvas.getContext(kind, { failIfMajorPerformanceCaveat: false });
      if (!context) return { available: false };
      const debug = context.getExtension("WEBGL_debug_renderer_info");
      const result = {
        available: true,
        version: String(context.getParameter(context.VERSION) || ""),
        shadingLanguage: String(context.getParameter(context.SHADING_LANGUAGE_VERSION) || ""),
        vendor: debug ? String(context.getParameter(debug.UNMASKED_VENDOR_WEBGL) || "") : null,
        renderer: debug ? String(context.getParameter(debug.UNMASKED_RENDERER_WEBGL) || "") : null
      };
      const lose = context.getExtension("WEBGL_lose_context");
      if (lose) lose.loseContext();
      return result;
    } catch (error) {
      return { available: false, error: String(error && error.name || "context-error") };
    }
  }

  function send() {
    const result = {
      kind: "capabilities",
      at: Date.now(),
      webgl2: webglInfo("webgl2"),
      webgl1: webglInfo("webgl"),
      webAssembly: typeof WebAssembly === "object",
      sharedArrayBuffer: typeof SharedArrayBuffer === "function",
      crossOriginIsolated: globalThis.crossOriginIsolated === true,
      offscreenCanvas: typeof OffscreenCanvas === "function",
      workers: typeof Worker === "function",
      hardwareConcurrency: Number(navigator.hardwareConcurrency || 0),
      pageErrors,
      rejectedPromises,
      contextLosses
    };
    try {
      if (window.LHProbe && typeof window.LHProbe.postMessage === "function") {
        window.LHProbe.postMessage(JSON.stringify(result));
      }
    } catch (_) {}
  }

  addEventListener("DOMContentLoaded", () => {
    send();
    setTimeout(send, 5000);
    setTimeout(send, 15000);
  }, { once: true });
})();

"use strict";

/**
 * Isolated-world relay for the ZIP 0.5.1 addon payload.
 *
 * This script never calls native messaging directly and never uses page-forgeable window messages.
 * It asks the built-in background script for a validated payload, then exposes only immutable
 * getter functions into the page world.
 */
(function installIsolatedAddonBridge() {
  const GAME_ORIGIN = "https://lasthavens.com";
  const BRIDGE_KEY = "__LASTHAVEN_ADDON_BRIDGE__";
  const core = globalThis.__LHAddonRuntimeCore;
  if (!core || typeof core.validatePayload !== "function") {
    return;
  }

  if (location.origin !== GAME_ORIGIN ||
      (location.pathname !== "/play" && location.pathname !== "/play/")) {
    return;
  }

  let currentBundle = null;
  let ready = false;
  let errorCode = null;

  function validatePayload(payload) {
    return core.validatePayload(payload);
  }

  function publishToPage() {
    try {
      const targetWindow = window;
      const pageGlobal = window.wrappedJSObject || window;
      const pageBridge = cloneInto({}, targetWindow);
      const getBundle = exportFunction(
        () => currentBundle === null ? null : cloneInto(currentBundle, targetWindow),
        targetWindow,
      );
      const getState = exportFunction(
        () => cloneInto({ ready: ready, error: errorCode }, targetWindow),
        targetWindow,
      );
      Object.defineProperty(pageBridge, "getBundle", {
        value: getBundle,
        configurable: false,
        enumerable: true,
        writable: false
      });
      Object.defineProperty(pageBridge, "getState", {
        value: getState,
        configurable: false,
        enumerable: true,
        writable: false
      });
      Object.defineProperty(pageGlobal, BRIDGE_KEY, {
        value: pageBridge,
        configurable: false,
        enumerable: false,
        writable: false
      });
      return true;
    } catch (_error) {
      return false;
    }
  }

  function delay(milliseconds) {
    return new Promise((resolve) => setTimeout(resolve, milliseconds));
  }

  async function fetchBundle() {
    for (let attempt = 0; attempt < 3; attempt += 1) {
      try {
        const response = await browser.runtime.sendMessage({ type: "getAddonBundle" });
        if (response && response.type === "error") {
          const code = typeof response.code === "string" ? response.code : "unavailable";
          if (code === "unavailable" && attempt < 2) {
            await delay(250 * (attempt + 1));
            continue;
          }
          errorCode = code.slice(0, 32);
          ready = true;
          return;
        }
        if (!validatePayload(response)) {
          errorCode = "invalid_payload";
          ready = true;
          return;
        }
        currentBundle = core.deepFreeze(response);
        ready = true;
        return;
      } catch (_error) {
        if (attempt < 2) {
          await delay(250 * (attempt + 1));
          continue;
        }
        errorCode = "bridge_unavailable";
        ready = true;
      }
    }
  }

  if (!publishToPage()) {
    return;
  }
  fetchBundle();
})();

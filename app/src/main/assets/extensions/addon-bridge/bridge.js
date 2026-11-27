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
  const MAX_RUNTIME_BUNDLE_BYTES = 16 * 1024 * 1024;
  const MAX_RUNTIME_FILE_BYTES = 8 * 1024 * 1024;
  const MAX_RUNTIME_FILES = 1024;
  const MAX_RUNTIME_ADDONS = 128;
  const MAX_RUNTIME_PATH_CHARS = 512;
  const FOLDER_PATTERN = /^[A-Za-z0-9_-]{1,128}$/;
  const BASE64_PATTERN = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;

  if (location.origin !== GAME_ORIGIN ||
      (location.pathname !== "/play" && location.pathname !== "/play/")) {
    return;
  }

  let currentBundle = null;
  let ready = false;
  let errorCode = null;

  function isSafeFolder(folder) {
    return typeof folder === "string" &&
      FOLDER_PATTERN.test(folder) &&
      !folder.toLowerCase().startsWith("blizzard_");
  }

  function isSafeRelativePath(path) {
    if (typeof path !== "string" || path.length === 0 || path.length > MAX_RUNTIME_PATH_CHARS) {
      return false;
    }
    if (path.startsWith("/") || path.includes("\\") || /[\u0000-\u001f\u007f-\u009f:]/.test(path)) {
      return false;
    }
    return path.split("/").every((part) => part.length > 0 && part !== "." && part !== "..");
  }

  function encodedByteLength(value) {
    if (typeof value !== "string" || !BASE64_PATTERN.test(value)) {
      return -1;
    }
    if (value.length === 0) {
      return 0;
    }
    const padding = value.endsWith("==") ? 2 : value.endsWith("=") ? 1 : 0;
    return Math.floor(value.length * 3 / 4) - padding;
  }

  function validatePayload(payload) {
    if (!payload || typeof payload !== "object" ||
        Object.keys(payload).sort().join(",") !== "addons,revision" ||
        typeof payload.revision !== "string" ||
        !/^[a-f0-9]{64}$/.test(payload.revision) ||
        !Array.isArray(payload.addons) ||
        payload.addons.length > MAX_RUNTIME_ADDONS) {
      return false;
    }

    const folders = new Set();
    const files = new Set();
    let fileCount = 0;
    let totalBytes = 0;
    for (const addon of payload.addons) {
      if (!addon || typeof addon !== "object" ||
          Object.keys(addon).sort().join(",") !== "files,folder" ||
          !isSafeFolder(addon.folder) ||
          folders.has(addon.folder.toLowerCase()) ||
          !addon.files || typeof addon.files !== "object") {
        return false;
      }
      folders.add(addon.folder.toLowerCase());
      for (const path of Object.keys(addon.files)) {
        if (!isSafeRelativePath(path)) {
          return false;
        }
        const key = addon.folder + "/" + path;
        if (files.has(key)) {
          return false;
        }
        files.add(key);
        const bytes = encodedByteLength(addon.files[path]);
        if (bytes < 0 || bytes > MAX_RUNTIME_FILE_BYTES) {
          return false;
        }
        fileCount += 1;
        totalBytes += bytes;
        if (fileCount > MAX_RUNTIME_FILES || totalBytes > MAX_RUNTIME_BUNDLE_BYTES) {
          return false;
        }
      }
    }
    return true;
  }

  function publishToPage() {
    try {
      const pageWindow = window.wrappedJSObject || window;
      const pageBridge = cloneInto({}, pageWindow);
      const getBundle = exportFunction(
        () => currentBundle === null ? null : cloneInto(currentBundle, pageWindow),
        pageWindow,
      );
      const getState = exportFunction(
        () => cloneInto({ ready: ready, error: errorCode }, pageWindow),
        pageWindow,
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
      Object.defineProperty(pageWindow, BRIDGE_KEY, {
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
        currentBundle = response;
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

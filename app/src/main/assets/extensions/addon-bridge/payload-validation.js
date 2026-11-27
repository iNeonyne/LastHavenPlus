"use strict";

/**
 * Isolated-world payload validation helpers for the ZIP 0.5.1 runtime bridge.
 *
 * XHR and Worker interception are deliberately implemented by issues #168 and #169. This file
 * only validates the already native-approved, read-only payload before the page runtime consumes it.
 */
(function installAddonRuntimeCore(global) {
  const CORE_KEY = "__LHAddonRuntimeCore";
  if (Object.prototype.hasOwnProperty.call(global, CORE_KEY)) {
    return;
  }

  const MAX_RUNTIME_BUNDLE_BYTES = 16 * 1024 * 1024;
  const MAX_RUNTIME_FILE_BYTES = 8 * 1024 * 1024;
  const MAX_RUNTIME_FILES = 1024;
  const MAX_RUNTIME_ADDONS = 128;
  const MAX_RUNTIME_PATH_CHARS = 512;
  const FOLDER_PATTERN = /^[A-Za-z0-9_-]{1,128}$/;
  const BASE64_PATTERN = /^[A-Za-z0-9+/]*={0,2}$/;

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
    if (typeof value !== "string" || value.length % 4 !== 0 || !BASE64_PATTERN.test(value)) {
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
      const normalizedFolder = addon.folder.toLowerCase();
      folders.add(normalizedFolder);
      let hasToc = false;

      for (const path of Object.keys(addon.files)) {
        if (!isSafeRelativePath(path)) {
          return false;
        }
        const normalizedPath = path.toLowerCase();
        if (normalizedPath === normalizedFolder + ".toc") {
          hasToc = true;
        }
        const key = normalizedFolder + "/" + normalizedPath;
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
      if (!hasToc) {
        return false;
      }
    }
    return true;
  }

  function deepFreeze(value) {
    if (!value || typeof value !== "object" || Object.isFrozen(value)) {
      return value;
    }
    Object.freeze(value);
    for (const key of Object.keys(value)) {
      deepFreeze(value[key]);
    }
    return value;
  }

  Object.defineProperty(global, CORE_KEY, {
    value: Object.freeze({
      validatePayload: validatePayload,
      deepFreeze: deepFreeze,
      isSafeFolder: isSafeFolder,
      isSafeRelativePath: isSafeRelativePath
    }),
    configurable: false,
    enumerable: false,
    writable: false
  });
})(globalThis);

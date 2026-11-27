"use strict";

/**
 * Launcher-owned addon bridge background script (issue #167).
 *
 * This is the only extension context allowed to connect to native messaging. It relays a validated,
 * bounded ZIP 0.5.1 addon payload to the exact game content script. It never writes extension
 * storage, enables addons, downloads packages or changes the app-private library.
 */
const NATIVE_APP = "neonyneAddonBridge";
const REQUEST_TIMEOUT_MS = 15000;
const INITIAL_RECONNECT_DELAY_MS = 250;
const MAX_RECONNECT_DELAY_MS = 30000;
const MAX_PENDING_REQUESTS = 8;
const MAX_RUNTIME_BUNDLE_BYTES = 16 * 1024 * 1024;
const MAX_RUNTIME_FILE_BYTES = 8 * 1024 * 1024;
const MAX_RUNTIME_FILES = 1024;
const MAX_RUNTIME_ADDONS = 128;
const MAX_RUNTIME_PATH_CHARS = 512;
const MAX_MANIFEST_PAGE_ITEMS = 32;
const MAX_FILE_CHUNK_BYTES = 32 * 1024;

const pending = new Map();
const portWaiters = [];
let port = null;
let reconnectTimer = null;
let reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;
let requestSequence = 0;

function scheduleReconnect() {
  if (reconnectTimer !== null) {
    return;
  }
  const delay = reconnectDelayMs;
  reconnectDelayMs = Math.min(reconnectDelayMs * 2, MAX_RECONNECT_DELAY_MS);
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    connect();
  }, delay);
}

function rejectPending(code) {
  for (const [requestId, entry] of pending) {
    clearTimeout(entry.timeout);
    pending.delete(requestId);
    entry.reject(new Error(code));
  }
}

function resetPort(code) {
  const stalePort = port;
  port = null;
  rejectPending(code);
  if (stalePort) {
    try {
      stalePort.disconnect();
    } catch (_error) {
      // The port is already gone; reconnect below.
    }
  }
  scheduleReconnect();
}

function resolvePortWaiters() {
  while (portWaiters.length > 0 && port) {
    const waiter = portWaiters.shift();
    clearTimeout(waiter.timeout);
    waiter.resolve(port);
  }
}

function waitForPort() {
  if (port) {
    return Promise.resolve(port);
  }
  return new Promise((resolve, reject) => {
    const waiter = { resolve: resolve, reject: reject, timeout: null };
    waiter.timeout = setTimeout(() => {
      const index = portWaiters.indexOf(waiter);
      if (index !== -1) {
        portWaiters.splice(index, 1);
      }
      reject(new Error("native_unavailable"));
    }, REQUEST_TIMEOUT_MS);
    portWaiters.push(waiter);
  });
}

function requestNative(type, fields = {}) {
  return waitForPort().then((activePort) => {
    if (activePort !== port || pending.size >= MAX_PENDING_REQUESTS) {
      throw new Error("native_unavailable");
    }
    requestSequence += 1;
    const requestId = "native-" + Date.now() + "-" + requestSequence;
    return new Promise((resolve, reject) => {
      const timeout = setTimeout(() => {
        if (!pending.has(requestId)) {
          return;
        }
        pending.delete(requestId);
        reject(new Error("native_timeout"));
        resetPort("native_disconnected");
      }, REQUEST_TIMEOUT_MS);
      pending.set(requestId, { resolve: resolve, reject: reject, timeout: timeout });
      try {
        activePort.postMessage(Object.assign({}, fields, { type: type, requestId: requestId }));
      } catch (_error) {
        clearTimeout(timeout);
        pending.delete(requestId);
        reject(new Error("native_disconnected"));
        resetPort("native_disconnected");
      }
    });
  });
}

function connect() {
  let nextPort;
  try {
    nextPort = browser.runtime.connectNative(NATIVE_APP);
  } catch (_error) {
    scheduleReconnect();
    return;
  }

  port = nextPort;
  resolvePortWaiters();

  nextPort.onMessage.addListener((message) => {
    if (!message || typeof message !== "object" || typeof message.requestId !== "string") {
      return;
    }
    const entry = pending.get(message.requestId);
    if (!entry) {
      return;
    }
    pending.delete(message.requestId);
    clearTimeout(entry.timeout);
    reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;
    entry.resolve(message);
  });

  nextPort.onDisconnect.addListener(() => {
    if (port !== nextPort) {
      return;
    }
    port = null;
    rejectPending("native_disconnected");
    scheduleReconnect();
  });

  requestNative("ping").then((response) => {
    if (response && response.type === "ack") {
      reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;
    }
  }).catch(() => {
    // The request timeout owns recovery and bounded reconnection.
  });
}

function isAllowedGameSender(sender) {
  if (!sender || sender.id !== browser.runtime.id || sender.frameId !== 0) {
    return false;
  }
  let url;
  try {
    url = new URL(sender.url);
  } catch (_error) {
    return false;
  }
  return url.protocol === "https:" &&
    url.hostname === "lasthavens.com" &&
    url.port === "" &&
    url.username === "" &&
    url.password === "" &&
    (url.pathname === "/play" || url.pathname === "/play/");
}

function isSafeFolder(folder) {
  return typeof folder === "string" &&
    folder.length > 0 &&
    folder.length <= 128 &&
    /^[A-Za-z0-9_-]+$/.test(folder) &&
    !folder.toLowerCase().startsWith("blizzard_");
}

function isSafeRelativePath(path) {
  if (typeof path !== "string" || path.length === 0 || path.length > MAX_RUNTIME_PATH_CHARS) {
    return false;
  }
  if (path.startsWith("/") || path.includes("\\") || /[\u0000-\u001f\u007f:]/.test(path)) {
    return false;
  }
  const segments = path.split("/");
  return segments.every((segment) => segment.length > 0 && segment !== "." && segment !== "..");
}

function decodeBase64Chunk(encoded, expectedLength) {
  if (typeof encoded !== "string" || encoded.length > 4 * Math.ceil(MAX_FILE_CHUNK_BYTES / 3) + 4) {
    throw new Error("invalid_response");
  }
  let binary;
  try {
    binary = atob(encoded);
  } catch (_error) {
    throw new Error("invalid_response");
  }
  if (binary.length !== expectedLength) {
    throw new Error("invalid_response");
  }
  const bytes = new Uint8Array(binary.length);
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index);
  }
  return bytes;
}

function encodeBase64(bytes) {
  let binary = "";
  for (let offset = 0; offset < bytes.length; offset += 0x4000) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + 0x4000));
  }
  return btoa(binary);
}

async function readAddonFile(revision, descriptor) {
  const bytes = new Uint8Array(descriptor.sizeBytes);
  let offset = 0;
  while (offset < descriptor.sizeBytes) {
    const response = await requestNative("getFileChunk", {
      revision: revision,
      folder: descriptor.folder,
      path: descriptor.path,
      offset: String(offset)
    });
    if (!response || response.type !== "fileChunk" ||
        response.revision !== revision ||
        response.folder !== descriptor.folder ||
        response.path !== descriptor.path ||
        response.offset !== offset ||
        response.totalBytes !== descriptor.sizeBytes) {
      throw new Error("invalid_response");
    }
    const expectedLength = Math.min(MAX_FILE_CHUNK_BYTES, descriptor.sizeBytes - offset);
    const chunk = decodeBase64Chunk(response.data, expectedLength);
    if (response.done !== (offset + chunk.length === descriptor.sizeBytes)) {
      throw new Error("invalid_response");
    }
    bytes.set(chunk, offset);
    offset += chunk.length;
  }
  return encodeBase64(bytes);
}

async function getAddonBundle() {
  const info = await requestNative("getBundleInfo");
  if (!info || info.type !== "bundleInfo" ||
      typeof info.revision !== "string" || !/^[a-f0-9]{64}$/.test(info.revision) ||
      !Array.isArray(info.addons) || info.addons.length > MAX_RUNTIME_ADDONS ||
      !Number.isSafeInteger(info.fileCount) || info.fileCount < 0 || info.fileCount > MAX_RUNTIME_FILES ||
      !Number.isSafeInteger(info.totalBytes) || info.totalBytes < 0 || info.totalBytes > MAX_RUNTIME_BUNDLE_BYTES ||
      !Number.isSafeInteger(info.pageCount) ||
      info.pageCount !== Math.ceil(info.fileCount / MAX_MANIFEST_PAGE_ITEMS) ||
      info.pageSize !== MAX_MANIFEST_PAGE_ITEMS ||
      info.chunkBytes !== MAX_FILE_CHUNK_BYTES) {
    throw new Error("invalid_response");
  }

  const addonMap = new Map();
  for (const folder of info.addons) {
    if (!isSafeFolder(folder) || addonMap.has(folder)) {
      throw new Error("invalid_response");
    }
    addonMap.set(folder, { folder: folder, files: Object.create(null) });
  }

  const seenFiles = new Set();
  let listedFiles = 0;
  let listedBytes = 0;
  for (let page = 0; page < info.pageCount; page += 1) {
    const response = await requestNative("getBundleManifestPage", {
      revision: info.revision,
      page: String(page)
    });
    if (!response || response.type !== "bundleManifestPage" ||
        response.revision !== info.revision ||
        response.page !== page ||
        !Array.isArray(response.files) ||
        response.files.length > MAX_MANIFEST_PAGE_ITEMS) {
      throw new Error("invalid_response");
    }
    for (const descriptor of response.files) {
      if (!descriptor || !isSafeFolder(descriptor.folder) ||
          !isSafeRelativePath(descriptor.path) ||
          !Number.isSafeInteger(descriptor.sizeBytes) ||
          descriptor.sizeBytes < 0 || descriptor.sizeBytes > MAX_RUNTIME_FILE_BYTES ||
          !addonMap.has(descriptor.folder)) {
        throw new Error("invalid_response");
      }
      const key = descriptor.folder + "/" + descriptor.path;
      if (seenFiles.has(key)) {
        throw new Error("invalid_response");
      }
      seenFiles.add(key);
      listedFiles += 1;
      listedBytes += descriptor.sizeBytes;
      if (listedFiles > MAX_RUNTIME_FILES || listedBytes > MAX_RUNTIME_BUNDLE_BYTES) {
        throw new Error("payload_too_large");
      }
      const encoded = descriptor.sizeBytes === 0
        ? ""
        : await readAddonFile(info.revision, descriptor);
      addonMap.get(descriptor.folder).files[descriptor.path] = encoded;
    }
  }

  if (listedFiles !== info.fileCount || listedBytes !== info.totalBytes) {
    throw new Error("invalid_response");
  }
  return {
    revision: info.revision,
    addons: Array.from(addonMap.values())
  };
}

browser.runtime.onMessage.addListener((message, sender) => {
  if (!isAllowedGameSender(sender) ||
      !message || typeof message !== "object" ||
      Object.keys(message).length !== 1 ||
      message.type !== "getAddonBundle") {
    return undefined;
  }
  return getAddonBundle().catch((error) => ({
    type: "error",
    code: typeof error?.message === "string" ? error.message.slice(0, 32) : "unavailable"
  }));
});

connect();

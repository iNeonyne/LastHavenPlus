"use strict";

/**
 * Launcher-owned addon bridge background script (launcher issue #167).
 *
 * Foundation only: it holds the native port to the launcher and relays bounded,
 * launcher-defined messages. It runs solely inside this bundled built-in extension;
 * it never installs, downloads, enables or updates anything, and it performs no file
 * access. Game-side wiring arrives with later launcher issues.
 */
const NATIVE_APP = "neonyneAddonBridge";
const REQUEST_TIMEOUT_MS = 15000;
const INITIAL_RECONNECT_DELAY_MS = 250;
const MAX_RECONNECT_DELAY_MS = 30000;

const pending = new Map();
let port = null;
let reconnectTimer = null;
let reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;
let startupSequence = 0;

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

function send(requestId, type) {
  if (!port || pending.size >= 8) {
    return;
  }
  pending.set(requestId, Date.now());
  try {
    port.postMessage({ type: type, requestId: requestId });
  } catch (_error) {
    const failedPort = port;
    port = null;
    pending.clear();
    try {
      if (failedPort) {
        failedPort.disconnect();
      }
    } catch (_disconnectError) {
      // The port is already gone; the reconnect timer remains authoritative.
    }
    scheduleReconnect();
  }
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
  nextPort.onMessage.addListener((message) => {
    if (!message || typeof message !== "object") {
      return;
    }
    const requestId = message.requestId;
    if (typeof requestId !== "string" || !pending.delete(requestId)) {
      return;
    }
    // Responses are consumed by later launcher issues; nothing leaves the extension here.
    reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;
  });

  nextPort.onDisconnect.addListener(() => {
    if (port !== nextPort) {
      return;
    }
    port = null;
    pending.clear();
    scheduleReconnect();
  });

  // Exercise the native messaging round-trip on every successful connection.
  startupSequence += 1;
  send("startup-" + startupSequence, "ping");
}

connect();

setInterval(() => {
  const now = Date.now();
  let expired = false;
  for (const [requestId, createdAt] of pending) {
    if (now - createdAt > REQUEST_TIMEOUT_MS) {
      pending.delete(requestId);
      expired = true;
    }
  }
  if (expired && port) {
    const stalePort = port;
    port = null;
    pending.clear();
    try {
      stalePort.disconnect();
    } catch (_error) {
      // The port is already gone; reconnect below.
    }
    scheduleReconnect();
  }
}, REQUEST_TIMEOUT_MS);

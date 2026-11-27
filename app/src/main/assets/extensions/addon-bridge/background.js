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

const port = browser.runtime.connectNative(NATIVE_APP);
const pending = new Map();

function send(requestId, type) {
  if (pending.size >= 8) {
    return;
  }
  pending.set(requestId, Date.now());
  port.postMessage({ type: type, requestId: requestId });
}

port.onMessage.addListener((message) => {
  if (!message || typeof message !== "object") {
    return;
  }
  const requestId = message.requestId;
  if (typeof requestId !== "string" || !pending.delete(requestId)) {
    return;
  }
  // Responses are consumed by later launcher issues; nothing leaves the extension here.
});

port.onDisconnect.addListener(() => pending.clear());

// Exercise the native messaging round-trip as soon as the bundled bridge connects.
send("startup", "ping");

setInterval(() => {
  const now = Date.now();
  for (const [requestId, createdAt] of pending) {
    if (now - createdAt > REQUEST_TIMEOUT_MS) {
      pending.delete(requestId);
    }
  }
}, REQUEST_TIMEOUT_MS);

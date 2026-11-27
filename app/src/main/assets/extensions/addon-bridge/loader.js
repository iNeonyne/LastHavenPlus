"use strict";

/**
 * Main-world handoff for the validated ZIP 0.5.1 addon payload.
 *
 * The isolated bridge owns native messaging and exposes only a read-only getter. Runtime resource
 * interception (issue #168) and pthread/OPFS handling (issue #169) consume this payload later.
 */
(function installAddonRuntimePayload(global) {
  const core = global.__LHAddonRuntimeCore;
  const MAX_WAIT_MS = 20000;
  const POLL_INTERVAL_MS = 50;
  const startedAt = Date.now();

  function publishStatus(status, error) {
    Object.defineProperty(global, "__LASTHAVEN_ADDON_RUNTIME_STATUS__", {
      value: status,
      configurable: false,
      enumerable: false,
      writable: false
    });
    if (error) {
      Object.defineProperty(global, "__LASTHAVEN_ADDON_RUNTIME_ERROR__", {
        value: String(error).slice(0, 32),
        configurable: false,
        enumerable: false,
        writable: false
      });
    }
  }

  if (!core || typeof core.validatePayload !== "function") {
    publishStatus("error", "runtime_core_missing");
    return;
  }

  const timer = setInterval(() => {
    const bridge = global.__LASTHAVEN_ADDON_BRIDGE__;
    if (!bridge || typeof bridge.getState !== "function" || typeof bridge.getBundle !== "function") {
      if (Date.now() - startedAt > MAX_WAIT_MS) {
        clearInterval(timer);
        publishStatus("error", "bridge_timeout");
      }
      return;
    }

    let state;
    try {
      state = bridge.getState();
    } catch (_error) {
      clearInterval(timer);
      publishStatus("error", "bridge_unavailable");
      return;
    }
    if (!state || state.ready !== true) {
      if (Date.now() - startedAt > MAX_WAIT_MS) {
        clearInterval(timer);
        publishStatus("error", "bridge_timeout");
      }
      return;
    }

    clearInterval(timer);
    if (typeof state.error === "string" && state.error.length > 0) {
      publishStatus("error", state.error);
      return;
    }

    let payload;
    try {
      payload = bridge.getBundle();
    } catch (_error) {
      publishStatus("error", "payload_unavailable");
      return;
    }
    if (!core.validatePayload(payload)) {
      publishStatus("error", "invalid_payload");
      return;
    }

    core.deepFreeze(payload);
    Object.defineProperty(global, "__LASTHAVEN_ADDON_RUNTIME_PAYLOAD__", {
      value: payload,
      configurable: false,
      enumerable: false,
      writable: false
    });
    publishStatus("ready", null);
  }, POLL_INTERVAL_MS);
})(globalThis);

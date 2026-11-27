package com.neonyne.lasthavenlauncher

import org.mozilla.geckoview.WebExtension

/**
 * Pure security gate for the addon-bridge native messaging foundation (issue #167).
 *
 * The contract is fail closed: a native message is accepted only when every sender attribute
 * matches the bundled, launcher-owned extension exactly. Content-script and content-page contexts
 * are rejected outright; a sender bound to a live game session must additionally present one of the
 * exact default or alternate game origin/paths (via [AddonRuntimeOriginPolicy], which already
 * rejects credentials, non-default ports and encoded-path tricks).
 */
object AddonBridgePolicy {
  /** Deterministic id of the APK-bundled bridge extension; also set in its manifest. */
  const val BRIDGE_EXTENSION_ID = "lasthaven-addon-bridge@neonyne"

  /** Asset location of the bundled extension, relative to the APK assets root. */
  const val BUILTIN_LOCATION = "/extensions/addon-bridge/"

  /** GeckoView built-in extensions resolve to `resource://android/assets` + location. */
  const val BUILTIN_LOCATION_URI = "resource://android/assets/extensions/addon-bridge/"

  /** Native-messaging namespace both sides must use. */
  const val NATIVE_MESSAGING_NAME = "neonyneAddonBridge"

  /**
   * Decides whether a native-message sender may talk to the bridge.
   *
   * @param extensionId id reported by [WebExtension.MessageSender.webExtension]
   * @param isBuiltIn true only for the app's own built-in extension; null means unknown
   * @param environmentType [WebExtension.MessageSender.environmentType]
   * @param senderUrl URL reported by the sender; checked only for session-bound contexts
   * @param senderSession the [WebExtension.MessageSender.session] token, if the sender is bound to
   *   one
   * @param expectedSession the game session the bridge is currently attached to, if any
   */
  fun allowsSender(
    extensionId: String?,
    isBuiltIn: Boolean?,
    environmentType: Int,
    senderUrl: String?,
    senderSession: Any?,
    expectedSession: Any?,
  ): Boolean {
    if (extensionId != BRIDGE_EXTENSION_ID) return false
    if (isBuiltIn != true) return false
    if (environmentType != WebExtension.MessageSender.ENV_TYPE_EXTENSION) return false
    if (senderSession != null) {
      if (expectedSession == null || senderSession !== expectedSession) return false
      if (senderUrl == null || !AddonRuntimeOriginPolicy.allows(senderUrl)) return false
    }
    return true
  }
}

/**
 * Pure timeout/back-pressure rules for in-flight bridge requests. The bridge fails closed when a
 * request id is reused, when the pending set is saturated, or when a request ages out.
 */
object AddonBridgeTimeoutPolicy {
  const val REQUEST_TIMEOUT_MS = 15_000L
  const val MAX_PENDING_REQUESTS = 8
  const val MAX_CONNECTED_PORTS = 1

  fun isExpired(createdAtMs: Long, nowMs: Long): Boolean = nowMs - createdAtMs > REQUEST_TIMEOUT_MS

  fun canAcceptPendingRequest(pendingCount: Int): Boolean = pendingCount < MAX_PENDING_REQUESTS

  fun canAcceptConnection(connectedCount: Int): Boolean = connectedCount < MAX_CONNECTED_PORTS
}

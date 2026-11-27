package com.neonyne.lasthavenlauncher

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import java.util.LinkedHashMap
import java.util.concurrent.Executor
import org.json.JSONObject
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

/**
 * Android boundary for the bundled addon-bridge WebExtension foundation (issue #167).
 *
 * The controller installs only the launcher-owned built-in extension, validates every native sender
 * through [AddonBridgePolicy], parses messages through [AddonBridgeProtocol] and serves read-only
 * answers from the Phase A local addon library snapshot, which remains the sole source of truth. It
 * never enables addons, touches the library, writes files or activates runtime injection. It is
 * intentionally unwired: session wiring happens after the GameSessionController boundary
 * integrates.
 *
 * All decision logic lives in the pure policy/protocol objects; this class only adapts GeckoView
 * types to them and fails closed on any mismatch. Library reads run on [worker]; the immutable
 * snapshot they produce is cached and served to native messages.
 */
@MainThread
class AddonBridgeController(
  private val snapshotProvider: () -> AddonLibrarySnapshot,
  private val worker: Executor,
) {
  private val mainHandler = Handler(Looper.getMainLooper())
  private var runtime: GeckoRuntime? = null
  private var bridgeExtension: WebExtension? = null
  private var gameSession: GeckoSession? = null
  private val pendingRequests = LinkedHashMap<String, Long>()
  private val connectedPorts = mutableListOf<WebExtension.Port>()

  @Volatile private var cachedSnapshot: AddonLibrarySnapshot? = null

  /** Refreshes the cached library snapshot off the main thread; native reads use this cache. */
  fun refreshSnapshot() {
    worker.execute { cachedSnapshot = snapshotProvider() }
  }

  /** Records the live game session for sender identity checks; drops pending state. */
  fun setGameSession(session: GeckoSession?) {
    gameSession = session
    pendingRequests.clear()
  }

  /** Installs the bundled extension once per runtime and registers the message delegate. */
  fun ensureInstalled(context: Context) {
    refreshSnapshot()
    if (bridgeExtension != null) return
    val currentRuntime = runtime ?: GeckoRuntimeProvider.get(context).also { runtime = it }
    currentRuntime.webExtensionController
      .ensureBuiltIn(
        AddonBridgePolicy.BUILTIN_LOCATION_URI,
        AddonBridgePolicy.BRIDGE_EXTENSION_ID,
      )
      .accept(
        { extension ->
          mainHandler.post {
            if (extension != null && bridgeExtension == null) {
              extension.setMessageDelegate(messageDelegate, AddonBridgePolicy.NATIVE_MESSAGING_NAME)
              bridgeExtension = extension
            }
          }
        },
        { error ->
          Diagnostics.error(
            context,
            TAG,
            "Addon bridge extension install failed: ${error?.message ?: "unknown"}",
          )
        },
      )
  }

  /** Detaches the delegate and drops all session/port/pending state. */
  fun teardown() {
    bridgeExtension?.setMessageDelegate(null, AddonBridgePolicy.NATIVE_MESSAGING_NAME)
    bridgeExtension = null
    connectedPorts.forEach { it.disconnect() }
    connectedPorts.clear()
    pendingRequests.clear()
    gameSession = null
  }

  /**
   * Registers a request id for back-pressure/timeout tracking. Returns false when the id is already
   * pending, expired entries were just evicted and the set is saturated, or the limit is reached.
   */
  private fun recordRequest(requestId: String, nowMs: Long): Boolean {
    pendingRequests.entries.removeAll { AddonBridgeTimeoutPolicy.isExpired(it.value, nowMs) }
    if (requestId in pendingRequests) return false
    if (!AddonBridgeTimeoutPolicy.canAcceptPendingRequest(pendingRequests.size)) return false
    pendingRequests[requestId] = nowMs
    return true
  }

  private fun completeRequest(requestId: String) {
    pendingRequests.remove(requestId)
  }

  private fun toFlatFields(message: Any?): Map<String, Any?>? {
    val json = message as? JSONObject ?: return null
    if (json.length() > AddonBridgeProtocol.MAX_MESSAGE_FIELDS) return null
    val fields = LinkedHashMap<String, Any?>()
    for (key in json.keys()) {
      when (val value = json.opt(key)) {
        null -> return null
        is String,
        is Boolean,
        is Int,
        is Long,
        is Double -> fields[key] = value
        else -> return null
      }
    }
    return fields
  }

  /** Builds the bounded read-only response for a validated request; null fails closed. */
  private fun buildResponse(
    request: AddonBridgeProtocol.AddonBridgeRequest
  ): AddonBridgeProtocol.AddonBridgeResponse? {
    return when (request.type) {
      "ping" -> AddonBridgeProtocol.ack(request.requestId)
      "listAddons" -> {
        val snapshot =
          cachedSnapshot
            ?: return AddonBridgeProtocol.error(
              request.requestId,
              "unavailable",
            )
        val summaries =
          snapshot.addons.map {
            AddonBridgeProtocol.AddonSummary(it.folder, it.version, it.enabled)
          }
        AddonBridgeProtocol.addonList(request.requestId, snapshot.revision, summaries)
          ?: AddonBridgeProtocol.error(request.requestId, "tooManyAddons")
      }
      "getLoadOrder" -> {
        val snapshot =
          cachedSnapshot
            ?: return AddonBridgeProtocol.error(
              request.requestId,
              "unavailable",
            )
        val plan = AddonRuntimePlanner.plan(snapshot)
        AddonBridgeProtocol.loadOrder(
          request.requestId,
          plan.bundleRevision,
          plan.orderedAddons.map { it.folder },
          plan.blockedAddons.keys.toList(),
        ) ?: AddonBridgeProtocol.error(request.requestId, "tooManyAddons")
      }
      else -> AddonBridgeProtocol.error(request.requestId, "unsupported")
    }
  }

  private fun respond(fields: Map<String, Any?>): Map<String, Any?>? {
    val request = AddonBridgeProtocol.parse(fields) ?: return null
    if (!recordRequest(request.requestId, System.currentTimeMillis())) return null
    val response = buildResponse(request)
    completeRequest(request.requestId)
    return response?.toFields()
  }

  private fun senderAllowed(sender: WebExtension.MessageSender?): Boolean {
    if (sender == null) return false
    val extension = sender.webExtension
    return AddonBridgePolicy.allowsSender(
      extensionId = extension?.id,
      isBuiltIn = extension?.isBuiltIn,
      environmentType = sender.environmentType,
      senderUrl = sender.url,
      senderSession = sender.session,
      expectedSession = gameSession,
    )
  }

  private val messageDelegate =
    object : WebExtension.MessageDelegate {
      override fun onMessage(
        nativeApp: String,
        message: Any,
        sender: WebExtension.MessageSender,
      ): GeckoResult<Any> {
        if (nativeApp != AddonBridgePolicy.NATIVE_MESSAGING_NAME || !senderAllowed(sender)) {
          return GeckoResult.fromValue(null)
        }
        val fields = toFlatFields(message) ?: return GeckoResult.fromValue(null)
        val response = respond(fields) ?: return GeckoResult.fromValue(null)
        return GeckoResult.fromValue(JSONObject(response))
      }

      override fun onConnect(port: WebExtension.Port) {
        if (!senderAllowed(port.sender)) {
          port.disconnect()
          return
        }
        port.setDelegate(portDelegate)
        connectedPorts += port
      }
    }

  private val portDelegate =
    object : WebExtension.PortDelegate {
      override fun onPortMessage(message: Any, port: WebExtension.Port) {
        if (!senderAllowed(port.sender)) {
          port.disconnect()
          return
        }
        val fields = toFlatFields(message)
        val response = fields?.let { respond(it) }
        if (response != null) {
          port.postMessage(JSONObject(response))
        }
      }

      override fun onDisconnect(port: WebExtension.Port) {
        connectedPorts.remove(port)
      }
    }

  private companion object {
    const val TAG = "AddonBridge"
  }
}

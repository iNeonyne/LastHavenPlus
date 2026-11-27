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
 * Android boundary for the bundled addon-bridge WebExtension (issue #167).
 *
 * The controller installs only the launcher-owned built-in extension, validates every native sender
 * through [AddonBridgePolicy], and serves read-only metadata/file chunks from the Phase A app-private
 * library. It never enables addons, writes library files or activates runtime interception. Issue
 * #170 owns production session/navigation wiring.
 *
 * Snapshot and file reads run on [worker]. Native responses are bounded; large payloads are exposed
 * only through revision-bound manifest pages and one bounded file chunk at a time.
 */
@MainThread
class AddonBridgeController(
  private val snapshotProvider: () -> AddonLibrarySnapshot,
  private val payloadProvider: AddonRuntimePayloadProvider,
  private val worker: Executor,
) {
  private data class PendingPortRequest(
    val port: WebExtension.Port,
    val generation: Long,
    val timeout: Runnable,
  )

  private val mainHandler = Handler(Looper.getMainLooper())
  private var runtime: GeckoRuntime? = null
  private var bridgeExtension: WebExtension? = null
  private var installInFlight = false
  private var installationGeneration = 0L
  private var snapshotGeneration = 0L
  private var requestGeneration = 0L
  private var gameSession: GeckoSession? = null
  private val pendingRequests = LinkedHashMap<String, PendingPortRequest>()
  private val connectedPorts = mutableListOf<WebExtension.Port>()

  @Volatile private var cachedSnapshot: AddonLibrarySnapshot? = null
  @Volatile private var cachedPayloadManifest: AddonRuntimePayloadProvider.Manifest? = null
  @Volatile private var payloadErrorCode: String? = null

  /** Refreshes the library snapshot and validated payload manifest off the main thread. */
  fun refreshSnapshot() {
    val generation = ++snapshotGeneration
    try {
      worker.execute {
        val snapshotResult = runCatching(snapshotProvider)
        val snapshot = snapshotResult.getOrNull()
        val manifestResult =
          snapshot?.let { value -> runCatching { payloadProvider.buildManifest(value) } }
        mainHandler.post {
          if (generation != snapshotGeneration) return@post
          val manifest = manifestResult?.getOrNull()
          if (cachedPayloadManifest?.revision != manifest?.revision) {
            requestGeneration += 1
            clearPendingRequests()
          }
          cachedSnapshot = snapshot
          cachedPayloadManifest = manifest
          payloadErrorCode =
            when {
              snapshot == null -> "unavailable"
              manifest != null -> null
              else ->
                (manifestResult?.exceptionOrNull() as? AddonRuntimePayloadException)?.code
                  ?: "unavailable"
            }
        }
      }
    } catch (_: Exception) {
      mainHandler.post {
        if (generation != snapshotGeneration) return@post
        requestGeneration += 1
        clearPendingRequests()
        cachedSnapshot = null
        cachedPayloadManifest = null
        payloadErrorCode = "unavailable"
      }
    }
  }

  /** Records the live game session for sender identity checks; drops pending chunk reads. */
  fun setGameSession(session: GeckoSession?) {
    gameSession = session
    requestGeneration += 1
    clearPendingRequests()
  }

  /** Installs the bundled extension once per runtime and registers the message delegate. */
  fun ensureInstalled(context: Context) {
    if (bridgeExtension != null || installInFlight) return
    installInFlight = true
    val generation = installationGeneration
    val currentRuntime =
      try {
        runtime ?: GeckoRuntimeProvider.get(context).also { runtime = it }
      } catch (error: Exception) {
        installInFlight = false
        Diagnostics.error(
          context,
          TAG,
          "Addon bridge runtime initialization failed: ${error.message}",
        )
        return
      }
    val installation =
      try {
        currentRuntime.webExtensionController.ensureBuiltIn(
          AddonBridgePolicy.BUILTIN_LOCATION_URI,
          AddonBridgePolicy.BRIDGE_EXTENSION_ID,
        )
      } catch (error: Exception) {
        if (generation == installationGeneration) installInFlight = false
        Diagnostics.error(context, TAG, "Addon bridge extension install failed: ${error.message}")
        return
      }
    refreshSnapshot()
    installation.accept(
      { extension ->
        mainHandler.post {
          if (generation != installationGeneration) return@post
          installInFlight = false
          if (extension != null && bridgeExtension == null) {
            try {
              extension.setMessageDelegate(messageDelegate, AddonBridgePolicy.NATIVE_MESSAGING_NAME)
              bridgeExtension = extension
            } catch (error: Exception) {
              Diagnostics.error(
                context,
                TAG,
                "Addon bridge message delegate registration failed: ${error.message}",
              )
            }
          } else if (extension == null) {
            Diagnostics.error(
              context,
              TAG,
              "Addon bridge extension install returned no extension",
            )
          }
        }
      },
      { error ->
        mainHandler.post {
          if (generation != installationGeneration) return@post
          installInFlight = false
          Diagnostics.error(
            context,
            TAG,
            "Addon bridge extension install failed: ${error?.message ?: "unknown"}",
          )
        }
      },
    )
  }

  /** Detaches the delegate and drops all session/port/pending state. */
  fun teardown() {
    installationGeneration += 1
    snapshotGeneration += 1
    requestGeneration += 1
    installInFlight = false
    cachedSnapshot = null
    cachedPayloadManifest = null
    payloadErrorCode = "unavailable"
    bridgeExtension?.setMessageDelegate(null, AddonBridgePolicy.NATIVE_MESSAGING_NAME)
    bridgeExtension = null
    val portsToDisconnect = connectedPorts.toList()
    connectedPorts.clear()
    clearPendingRequests()
    portsToDisconnect.forEach { it.disconnect() }
    gameSession = null
  }

  private fun clearPendingRequests() {
    pendingRequests.values.forEach { mainHandler.removeCallbacks(it.timeout) }
    pendingRequests.clear()
  }

  private fun registerPendingRequest(
    requestId: String,
    port: WebExtension.Port,
    generation: Long,
  ): PendingPortRequest? {
    if (requestId in pendingRequests) return null
    if (!AddonBridgeTimeoutPolicy.canAcceptPendingRequest(pendingRequests.size)) return null

    lateinit var pending: PendingPortRequest
    val timeout =
      Runnable {
        if (pendingRequests[requestId] !== pending) return@Runnable
        pendingRequests.remove(requestId)
        if (generation == requestGeneration && connectedPorts.contains(port)) {
          postPortResponse(
            port,
            AddonBridgeProtocol.error(requestId, "timeout")
              ?: return@Runnable,
          )
        }
      }
    pending = PendingPortRequest(port, generation, timeout)
    pendingRequests[requestId] = pending
    mainHandler.postDelayed(timeout, AddonBridgeTimeoutPolicy.REQUEST_TIMEOUT_MS)
    return pending
  }

  private fun completePendingRequest(requestId: String, pending: PendingPortRequest) {
    if (pendingRequests[requestId] === pending) {
      pendingRequests.remove(requestId)
      mainHandler.removeCallbacks(pending.timeout)
    }
  }

  private fun toFlatFields(message: Any?): Map<String, Any?>? {
    val json = message as? JSONObject ?: return null
    if (json.length() !in 2..AddonBridgeProtocol.MAX_MESSAGE_FIELDS) return null
    val fields = LinkedHashMap<String, Any?>()
    for (key in json.keys()) {
      if (
        key !in
          setOf(
            AddonBridgeProtocol.KEY_TYPE,
            AddonBridgeProtocol.KEY_REQUEST_ID,
            AddonBridgeProtocol.KEY_REVISION,
            AddonBridgeProtocol.KEY_PAGE,
            AddonBridgeProtocol.KEY_FOLDER,
            AddonBridgeProtocol.KEY_PATH,
            AddonBridgeProtocol.KEY_OFFSET,
          )
      ) {
        return null
      }
      val value = json.opt(key) as? String ?: return null
      val maxLength =
        when (key) {
          AddonBridgeProtocol.KEY_REQUEST_ID -> AddonBridgeProtocol.MAX_REQUEST_ID_CHARS
          AddonBridgeProtocol.KEY_PATH -> AddonBridgeProtocol.MAX_RUNTIME_PATH_CHARS
          else -> AddonBridgeProtocol.MAX_ITEM_CHARS
        }
      if (value.length > maxLength) return null
      fields[key] = value
    }
    return fields
  }

  /** Builds a synchronous, bounded response for metadata and manifest-page requests. */
  private fun buildResponse(
    request: AddonBridgeProtocol.AddonBridgeRequest
  ): AddonBridgeProtocol.AddonBridgeResponse? {
    return when (request.type) {
      "ping" -> AddonBridgeProtocol.ack(request.requestId)
      "listAddons" -> {
        val snapshot =
          cachedSnapshot
            ?: return AddonBridgeProtocol.error(request.requestId, "unavailable")
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
            ?: return AddonBridgeProtocol.error(request.requestId, "unavailable")
        val plan = AddonRuntimePlanner.plan(snapshot)
        AddonBridgeProtocol.loadOrder(
          request.requestId,
          plan.bundleRevision,
          plan.orderedAddons.map { it.folder },
          plan.blockedAddons.keys.toList(),
        ) ?: AddonBridgeProtocol.error(request.requestId, "tooManyAddons")
      }
      "getBundleInfo" -> {
        val manifest =
          cachedPayloadManifest
            ?: return AddonBridgeProtocol.error(
              request.requestId,
              payloadErrorCode ?: "unavailable",
            )
        AddonBridgeProtocol.bundleInfo(request.requestId, manifest)
          ?: AddonBridgeProtocol.error(request.requestId, "payloadTooLarge")
      }
      "getBundleManifestPage" -> {
        val manifest =
          cachedPayloadManifest
            ?: return AddonBridgeProtocol.error(
              request.requestId,
              payloadErrorCode ?: "unavailable",
            )
        if (request.revision != manifest.revision) {
          return AddonBridgeProtocol.error(request.requestId, "staleRevision")
        }
        val files =
          runCatching { payloadProvider.page(manifest, request.page ?: -1) }.getOrElse {
            val code = (it as? AddonRuntimePayloadException)?.code ?: "invalidRequest"
            return AddonBridgeProtocol.error(request.requestId, code)
          }
        AddonBridgeProtocol.manifestPage(
          request.requestId,
          manifest,
          request.page ?: -1,
          files,
        ) ?: AddonBridgeProtocol.error(request.requestId, "invalidRequest")
      }
      "getFileChunk" -> AddonBridgeProtocol.error(request.requestId, "unsupported")
      else -> AddonBridgeProtocol.error(request.requestId, "unsupported")
    }
  }

  private fun responseFields(
    response: AddonBridgeProtocol.AddonBridgeResponse
  ): Map<String, Any?>? {
    val fields = response.toFields()
    val encodedBytes = JSONObject(fields).toString().toByteArray(Charsets.UTF_8).size
    if (!AddonBridgeProtocol.isResponseWithinByteLimit(encodedBytes)) return null
    return fields
  }

  private fun respond(
    request: AddonBridgeProtocol.AddonBridgeRequest
  ): Map<String, Any?>? {
    val response = buildResponse(request) ?: return null
    return responseFields(response)
      ?: AddonBridgeProtocol.error(request.requestId, "payloadTooLarge")?.toFields()
  }

  private fun postPortResponse(
    port: WebExtension.Port,
    response: AddonBridgeProtocol.AddonBridgeResponse,
  ) {
    val fields =
      responseFields(response)
        ?: AddonBridgeProtocol.error(response.requestId, "payloadTooLarge")?.toFields()
        ?: return
    try {
      port.postMessage(JSONObject(fields))
    } catch (_: Exception) {
      port.disconnect()
    }
  }

  private fun sendPortError(port: WebExtension.Port, requestId: String, code: String) {
    AddonBridgeProtocol.error(requestId, code)?.let { postPortResponse(port, it) }
  }

  private fun handleFileChunk(
    request: AddonBridgeProtocol.AddonBridgeRequest,
    port: WebExtension.Port,
  ) {
    val manifest =
      cachedPayloadManifest ?: run {
        sendPortError(port, request.requestId, payloadErrorCode ?: "unavailable")
        return
      }
    if (request.revision != manifest.revision) {
      sendPortError(port, request.requestId, "staleRevision")
      return
    }
    val folder = request.folder ?: return
    val path = request.path ?: return
    val offset = request.offset ?: return
    val generation = requestGeneration
    val pending = registerPendingRequest(request.requestId, port, generation)
    if (pending == null) {
      port.disconnect()
      return
    }

    try {
      worker.execute {
        val response =
          runCatching {
              val bytes = payloadProvider.readChunk(manifest, folder, path, offset)
              AddonBridgeProtocol.fileChunk(
                request.requestId,
                manifest,
                folder,
                path,
                offset,
                bytes,
              ) ?: throw AddonRuntimePayloadException("invalidRequest")
            }
            .getOrElse { error ->
              AddonBridgeProtocol.error(
                request.requestId,
                (error as? AddonRuntimePayloadException)?.code ?: "readFailed",
              )
            }
        mainHandler.post {
          if (pendingRequests[request.requestId] !== pending) return@post
          completePendingRequest(request.requestId, pending)
          if (
            pending.generation != requestGeneration ||
              generation != requestGeneration ||
              !connectedPorts.contains(port)
          ) {
            return@post
          }
          response?.let { postPortResponse(port, it) }
        }
      }
    } catch (_: Exception) {
      completePendingRequest(request.requestId, pending)
      sendPortError(port, request.requestId, "unavailable")
    }
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
        val request = AddonBridgeProtocol.parse(fields) ?: return GeckoResult.fromValue(null)
        val response = respond(request) ?: return GeckoResult.fromValue(null)
        return GeckoResult.fromValue(JSONObject(response))
      }

      override fun onConnect(port: WebExtension.Port) {
        if (
          !senderAllowed(port.sender) ||
            !AddonBridgeTimeoutPolicy.canAcceptConnection(connectedPorts.size)
        ) {
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
        val fields = toFlatFields(message) ?: return
        val request = AddonBridgeProtocol.parse(fields) ?: return
        if (request.type == "getFileChunk") {
          handleFileChunk(request, port)
        } else {
          val response = respond(request)
          if (response != null) {
            try {
              port.postMessage(JSONObject(response))
            } catch (_: Exception) {
              port.disconnect()
            }
          }
        }
      }

      override fun onDisconnect(port: WebExtension.Port) {
        connectedPorts.remove(port)
        val abandoned =
          pendingRequests.filterValues { it.port === port }.toMap()
        for ((requestId, pending) in abandoned) {
          completePendingRequest(requestId, pending)
        }
      }
    }

  private companion object {
    const val TAG = "AddonBridge"
  }
}

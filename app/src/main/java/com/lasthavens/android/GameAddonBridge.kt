package com.lasthavens.android

import android.net.Uri
import android.util.Log
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import java.io.File
import org.json.JSONObject

/**
 * Bridges the native add-on repository to the game page using origin-restricted WebKit messaging.
 * The game receives read-only metadata and bounded binary chunks; no storage mutation is exposed.
 */
class GameAddonBridge private constructor(
    private val webView: WebView,
    private val repository: AddonRepository,
    private var scriptHandlers: List<androidx.webkit.ScriptHandler> = emptyList(),
) {
    private data class ActivePayload(
        val generation: String,
        val directory: File,
        val result: GameAddonPayload.Result,
    )

    @Volatile
    private var activePayload: ActivePayload? = null

    private val listener = object : WebViewCompat.WebMessageListener {
        override fun onPostMessage(
            view: WebView,
            message: WebMessageCompat,
            sourceOrigin: Uri,
            isMainFrame: Boolean,
            replyProxy: JavaScriptReplyProxy,
        ) {
            if (!isMainFrame || !isAllowedOrigin(sourceOrigin)) return
            if (message.type != WebMessageCompat.TYPE_STRING) {
                replyError(replyProxy, "Only JSON string requests are accepted")
                return
            }
            val raw = message.data ?: ""
            if (raw.length > MAX_REQUEST_CHARS) {
                replyError(replyProxy, "Native bridge request is too large")
                return
            }
            runCatching {
                val request = JSONObject(raw)
                when (request.optString("kind")) {
                    "payload" -> sendPayload(replyProxy)
                    "chunk" -> sendChunk(request, replyProxy)
                    "status", "gameStorage" -> Log.d(TAG, "Game reported ${request.optString("kind")}")
                    else -> replyError(replyProxy, "Unsupported native bridge request")
                }
            }.onFailure { error ->
                Log.w(TAG, "Rejected game bridge request", error)
                replyError(replyProxy, error.message ?: "Native add-on bridge failed")
            }
        }
    }

    private fun sendPayload(replyProxy: JavaScriptReplyProxy) {
        val active = repository.active()
        if (active == null) {
            activePayload = null
            replyProxy.postMessage(
                JSONObject()
                    .put("kind", "payload")
                    .put("payload", JSONObject().put("revision", "empty").put("badge", true).put("addons", org.json.JSONArray()))
                    .toString(),
            )
            return
        }
        val built = GameAddonPayload.build(active.manifest)
        activePayload = ActivePayload(active.generation, active.directory, built)
        replyProxy.postMessage(JSONObject().put("kind", "payload").put("payload", built.payload).toString())
    }

    private fun sendChunk(request: JSONObject, replyProxy: JavaScriptReplyProxy) {
        val active = activePayload ?: error("Request the add-on payload before requesting files")
        val current = repository.active() ?: error("Native add-on library is no longer active")
        require(current.generation == active.generation) { "Native add-on library changed; reload the game" }
        val storage = request.getString("storageFolder")
        val path = request.getString("path")
        require(storage.matches(Regex("[A-Za-z0-9_-]{1,120}"))) { "Invalid add-on storage identity" }
        val file = active.result.files["${storage.lowercase()}/${path.lowercase()}"]
            ?: error("Requested add-on file is not in the enabled library")
        val offset = request.getLong("offset")
        val length = request.getInt("length")
        val bytes = GameAddonPayload.readChunk(active.directory, file, offset, length)
        replyProxy.postMessage(bytes)
    }

    private fun replyError(replyProxy: JavaScriptReplyProxy, message: String) {
        runCatching {
            replyProxy.postMessage(
                JSONObject().put("kind", "error").put("message", message.take(500)).toString(),
            )
        }.onFailure { Log.w(TAG, "Could not send game bridge error", it) }
    }

    fun close() {
        runCatching { WebViewCompat.removeWebMessageListener(webView, JS_OBJECT_NAME) }
        scriptHandlers.forEach { runCatching { it.remove() } }
        activePayload = null
    }

    companion object {
        private const val TAG = "LastHavenGameBridge"
        private const val JS_OBJECT_NAME = "LastHavenNative"
        private const val MAX_REQUEST_CHARS = 4096
        private val ALLOWED_ORIGINS = setOf(
            "https://lasthavens.com",
            "https://*.lasthavens.com",
        )

        fun install(webView: WebView, repository: AddonRepository): GameAddonBridge? {
            val required = listOf(
                WebViewFeature.WEB_MESSAGE_LISTENER,
                WebViewFeature.WEB_MESSAGE_ARRAY_BUFFER,
                WebViewFeature.DOCUMENT_START_SCRIPT,
            )
            val missing = required.filterNot(WebViewFeature::isFeatureSupported)
            if (missing.isNotEmpty()) {
                Log.w(TAG, "Native add-on loader unavailable; WebView provider lacks: ${missing.joinToString()}")
                return null
            }

            val context = webView.context
            val overlay = context.assets.open("extensions/lh-addon-manager/overlay.js")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val inject = context.assets.open("extensions/lh-addon-manager/inject.js")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val bridge = context.assets.open("webview-game-bridge.js")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }

            val instance = GameAddonBridge(webView, repository)
            WebViewCompat.addWebMessageListener(webView, JS_OBJECT_NAME, ALLOWED_ORIGINS, instance.listener)
            val handlers = try {
                listOf(
                    WebViewCompat.addDocumentStartJavaScript(
                        webView,
                        overlay + "\n" + inject,
                        ALLOWED_ORIGINS,
                    ),
                    WebViewCompat.addDocumentStartJavaScript(webView, bridge, ALLOWED_ORIGINS),
                )
            } catch (error: Exception) {
                WebViewCompat.removeWebMessageListener(webView, JS_OBJECT_NAME)
                throw error
            }
            instance.scriptHandlers = handlers
            return instance
        }

        private fun isAllowedOrigin(origin: Uri): Boolean =
            origin.scheme.equals("https", ignoreCase = true) && GameUrls.isGameHost(origin)
    }
}

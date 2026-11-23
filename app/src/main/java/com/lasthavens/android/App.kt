package com.lasthavens.android

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.Proxy

/**
 * Owns the single GeckoRuntime, the bundled Addon Manager extension and the long-lived game session.
 *
 * One GeckoRuntime creates one shared Firefox profile, so manager storage and game add-on injection
 * use the same profile. The game session lives here so Home/Play navigation does not discard it.
 */
class App : Application() {

    companion object {
        private const val TAG = "LasthavensApp"
        private const val EXTENSION_URI = "resource://android/assets/${Versions.EXTENSION_DIR}/"
        private const val EXTENSION_ID = "lh-addon-manager@lasthavens.local"
        private val OPTIONAL_HOSTS = listOf(
            "github.com", "raw.githubusercontent.com", "codeload.github.com", "objects.githubusercontent.com",
            "release-assets.githubusercontent.com", "media.githubusercontent.com", "gitlab.com", "api.github.com",
        )

        lateinit var instance: App
            private set
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    lateinit var runtime: GeckoRuntime
        private set

    var gameLoaded = false
    var reloadGameOnNextPlay = false
    var managerRef: WeakReference<ManagerActivity>? = null

    private var gameSessionInternal: GeckoSession? = null

    private enum class ExtensionState { PENDING, READY, FAILED }
    private var extensionState = ExtensionState.PENDING
    private var extension: WebExtension? = null
    private val extensionCallbacks = mutableListOf<(WebExtension?) -> Unit>()

    override fun onCreate() {
        super.onCreate()
        instance = this
        // GeckoView child processes also instantiate Application; only the package's main process starts Gecko.
        if (!isMainProcess()) return

    }

    /** Start Gecko only when the legacy Addon Manager is opened; gameplay uses System WebView. */
    private fun ensureGeckoRuntime() {
        if (::runtime.isInitialized || !isMainProcess()) return
        runtime = GeckoRuntime.create(
            this,
            GeckoRuntimeSettings.Builder()
                .remoteDebuggingEnabled(BuildConfig.DEBUG)
                .build(),
        )
        installOptionalPermissionPrompt()
        installBuiltInExtension()
    }

    // ---- Game session ------------------------------------------------------------------------

    fun gameSession(): GeckoSession {
        ensureGeckoRuntime()
        val existing = gameSessionInternal
        if (existing != null && existing.isOpen) return existing
        return GeckoSession().also {
            it.open(runtime)
            gameSessionInternal = it
            gameLoaded = false
        }
    }

    fun discardGameSession() {
        gameSessionInternal?.close()
        gameSessionInternal = null
        gameLoaded = false
    }

    // ---- Addon Manager extension -------------------------------------------------------------

    /** Runs [callback] on the main thread once the extension is installed (or failed to install). */
    fun onExtensionReady(callback: (WebExtension?) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { onExtensionReady(callback) }
            return
        }
        ensureGeckoRuntime()
        when (extensionState) {
            ExtensionState.PENDING -> extensionCallbacks += callback
            ExtensionState.READY -> callback(extension)
            ExtensionState.FAILED -> callback(null)
        }
    }

    private fun installBuiltInExtension() {
        runtime.webExtensionController.ensureBuiltIn(EXTENSION_URI, EXTENSION_ID).then<Void>(
            GeckoResult.OnValueListener<WebExtension, Void> { ext ->
                mainHandler.post {
                    extension = ext
                    if (ext != null) ext.setTabDelegate(tabDelegate())
                    finishExtension(if (ext != null) ExtensionState.READY else ExtensionState.FAILED)
                }
                null
            },
            GeckoResult.OnExceptionListener<Void> { error ->
                mainHandler.post {
                    Log.e(TAG, "Addon Manager extension failed to install", error)
                    finishExtension(ExtensionState.FAILED)
                }
                null
            },
        )
    }

    private fun finishExtension(state: ExtensionState) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { finishExtension(state) }
            return
        }
        extensionState = state
        val callbacks = extensionCallbacks.toList()
        extensionCallbacks.clear()
        callbacks.forEach { callback -> runCatching { callback(extension) }
            .onFailure { Log.e(TAG, "Extension-ready callback failed", it) } }
    }

    /** Approve only the optional host origins declared by this bundled extension; reject other permission types. */
    private fun installOptionalPermissionPrompt() {
        val delegate = proxy(WebExtensionController.PromptDelegate::class.java) { name, args ->
            if (name == "onOptionalPrompt") {
                fun strings(index: Int) =
                    (args.getOrNull(index) as? Array<*>)?.mapNotNull { it as? String }.orEmpty()
                val permissions = strings(1)
                val origins = strings(2)
                val dataPermissions = strings(3)
                val allowed = permissions.isEmpty() &&
                    dataPermissions.isEmpty() &&
                    origins.isNotEmpty() &&
                    origins.all { origin ->
                        OPTIONAL_HOSTS.any { host ->
                            origin == "https://$host/*" || origin.startsWith("https://$host/")
                        }
                    }
                GeckoResult.fromValue(if (allowed) AllowOrDeny.ALLOW else AllowOrDeny.DENY)
            } else null
        }
        runtime.webExtensionController.promptDelegate = delegate
    }

    /** Routes browser.tabs.create from the extension to the game activity or the system browser. */
    private fun tabDelegate(): WebExtension.TabDelegate =
        proxy(WebExtension.TabDelegate::class.java) { name, args ->
            if (name == "onNewTab") {
                val details = args.getOrNull(1)
                val url = runCatching { details?.javaClass?.getField("url")?.get(details) as? String }.getOrNull()
                mainHandler.post { openFromExtension(url) }
            }
            null
        }

    private fun openFromExtension(url: String?) {
        if (url.isNullOrBlank()) return
        val uri = Uri.parse(url)
        if (GameUrls.isGameHost(uri)) {
            managerRef?.get()?.finish()
            startActivity(
                Intent(this, PlayActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(PlayActivity.EXTRA_RELOAD, true),
            )
        } else {
            GameUrls.openExternal(this, uri)
        }
    }

    // ---- helpers -----------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    private fun <T> proxy(iface: Class<T>, onCall: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(iface.classLoader, arrayOf(iface)) { self, method, rawArgs ->
            val args: Array<out Any?> = rawArgs ?: emptyArray()
            if (method.declaringClass == Any::class.java) {
                when (method.name) {
                    "hashCode" -> System.identityHashCode(self)
                    "equals" -> self === args.firstOrNull()
                    else -> "proxy:${iface.simpleName}"
                }
            } else onCall(method.name, args)
        } as T

    private fun isMainProcess(): Boolean = runCatching {
        File("/proc/self/cmdline").readText().trim('\u0000') == packageName
    }.getOrDefault(true)
}

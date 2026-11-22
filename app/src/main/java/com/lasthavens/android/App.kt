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
 * Architecture notes
 *  - One GeckoRuntime => one Firefox profile. The game page and the Addon Manager page therefore
 *    share the extension's storage (IndexedDB), which is how installed add-ons reach the game.
 *  - The Addon Manager XPI is bundled UNCHANGED in assets and installed as a *built-in* extension,
 *    so Mozilla signing is not required.
 *  - The game session lives here (not in an Activity) so leaving to the home screen keeps it loaded.
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

    lateinit var runtime: GeckoRuntime
        private set

    /** Set once the game page has been loaded into [gameSession]. */
    var gameLoaded = false

    /** Set when the Addon Manager was used, so the next Play reloads the game with the new add-ons. */
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
        // GeckoView spawns child processes that also instantiate Application; only the main one may start Gecko.
        if (!isMainProcess()) return

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
        when (extensionState) {
            ExtensionState.PENDING -> extensionCallbacks += callback
            ExtensionState.READY -> callback(extension)
            ExtensionState.FAILED -> callback(null)
        }
    }

    private fun installBuiltInExtension() {
        runtime.webExtensionController.ensureBuiltIn(EXTENSION_URI, EXTENSION_ID).then<Void>(
            GeckoResult.OnValueListener<WebExtension, Void> { ext ->
                extension = ext
                if (ext != null) ext.setTabDelegate(tabDelegate())
                finishExtension(if (ext != null) ExtensionState.READY else ExtensionState.FAILED)
                null
            },
            GeckoResult.OnExceptionListener<Void> { error ->
                Log.e(TAG, "Addon Manager extension failed to install", error)
                finishExtension(ExtensionState.FAILED)
                null
            },
        )
    }

    private fun finishExtension(state: ExtensionState) {
        extensionState = state
        val callbacks = extensionCallbacks.toList()
        extensionCallbacks.clear()
        callbacks.forEach { it(extension) }
    }

    /** The manager asks for GitHub/GitLab access at runtime; approve only the origins it declares. */
    private fun installOptionalPermissionPrompt() {
        // A dynamic proxy keeps this independent of small signature changes between GeckoView releases.
        val delegate = proxy(WebExtensionController.PromptDelegate::class.java) { name, args ->
            if (name == "onOptionalPrompt") {
                val origins = args.flatMap { (it as? Array<*>)?.map { s -> s.toString() } ?: emptyList() }
                    .filter { it.contains("://") }
                val ok = origins.all { o -> OPTIONAL_HOSTS.any { h -> o.startsWith("https://$h/") } }
                GeckoResult.fromValue(if (ok) AllowOrDeny.ALLOW else AllowOrDeny.DENY)
            } else null
        }
        runtime.webExtensionController.promptDelegate = delegate
    }

    /** `browser.tabs.create` from the extension: game URLs open the game, anything else the system browser. */
    private fun tabDelegate(): WebExtension.TabDelegate =
        proxy(WebExtension.TabDelegate::class.java) { name, args ->
            if (name == "onNewTab") {
                val details = args.getOrNull(1)
                val url = runCatching { details?.javaClass?.getField("url")?.get(details) as? String }.getOrNull()
                Handler(Looper.getMainLooper()).post { openFromExtension(url) }
            }
            null // decline: the extension gets no tab object, the app handles the URL itself
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

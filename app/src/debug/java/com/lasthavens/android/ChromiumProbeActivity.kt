package com.lasthavens.android

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * Debug-only Chromium comparison screen.
 *
 * This intentionally uses Android System WebView's own profile. It does not read, clear,
 * or migrate GeckoView cookies, website storage, add-ons, or login state.
 */
class ChromiumProbeActivity : AppCompatActivity() {

    companion object {
        private const val JS_OBJECT = "LHProbe"
        private val ALLOWED_ORIGINS = setOf(
            "https://lasthavens.com",
            "https://*.lasthavens.com",
        )
    }

    private lateinit var webView: WebView
    private lateinit var status: TextView
    private var consoleErrors = 0
    private var bridgeAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.safeBrowsingEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.allowFileAccessFromFileURLs = false
            settings.allowUniversalAccessFromFileURLs = false
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.mediaPlaybackRequiresUserGesture = false
        }
        status = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 11f
            setPadding(dp(10), dp(7), dp(10), dp(7))
            setBackgroundColor(0xB3000000.toInt())
            text = initialStatus()
            isClickable = false
            isFocusable = false
        }
        root.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))
        root.addView(status, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(26) })
        setContentView(root)

        installProbeBridge()
        installProbeScript()
        installWebClients()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
        webView.loadUrl(GameUrls.GAME_URL)
    }

    private fun initialStatus(): String {
        val version = WebView.getCurrentWebViewPackage()?.versionName ?: "unknown"
        return "Chromium probe · WebView $version · preparing diagnostics"
    }

    private fun installProbeBridge() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            status.text = initialStatus() + "\nSecure WebMessageListener unavailable"
            return
        }
        WebViewCompat.addWebMessageListener(webView, JS_OBJECT, ALLOWED_ORIGINS) {
                _, message, sourceOrigin, isMainFrame, _ ->
            if (!isMainFrame || !GameUrls.isGameHost(sourceOrigin)) return@addWebMessageListener
            bridgeAvailable = true
            val payload = message.data.orEmpty()
            runOnUiThread { showReport(payload) }
        }
    }

    private fun installProbeScript() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            status.text = initialStatus() + "\nDocument-start script injection unavailable"
            return
        }
        val script = assets.open("chromium_probe.js").bufferedReader().use { it.readText() }
        WebViewCompat.addDocumentStartJavaScript(webView, script, ALLOWED_ORIGINS)
    }

    private fun installWebClients() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    consoleErrors++
                    status.post {
                        status.text = status.text.toString().substringBefore("\nConsole errors:") +
                            "\nConsole errors: $consoleErrors"
                    }
                }
                return true
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (GameUrls.isInApp(uri)) return false
                GameUrls.openExternal(this@ChromiumProbeActivity, uri)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (GameUrls.isGameHost(Uri.parse(url))) {
                    status.text = status.text.toString().substringBefore("\nPage loaded") +
                        "\nPage loaded · waiting for capability report"
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: android.webkit.WebResourceError,
            ) {
                if (request.isForMainFrame) {
                    status.text = "Chromium page load failed · error ${error.errorCode}"
                }
            }

            override fun onRenderProcessGone(
                view: WebView,
                detail: android.webkit.RenderProcessGoneDetail,
            ): Boolean {
                status.text = "Chromium renderer exited · crashed=${detail.didCrash()}"
                return true
            }
        }
    }

    private fun showReport(payload: String) {
        val report = runCatching { org.json.JSONObject(payload) }.getOrNull() ?: return
        fun yes(key: String) = if (report.optBoolean(key)) "yes" else "no"
        fun gl(key: String): String {
            val item = report.optJSONObject(key) ?: return "unavailable"
            if (!item.optBoolean("available")) return "unavailable"
            val renderer = item.optString("renderer").takeIf { it.isNotBlank() }
            val version = item.optString("version").takeIf { it.isNotBlank() }
            return listOfNotNull(version, renderer).joinToString(" · ").ifBlank { "available" }
        }
        status.text = buildString {
            append("Chromium probe · ")
            append(WebView.getCurrentWebViewPackage()?.versionName ?: "WebView unknown")
            append("\nWebGL 2: ")
            append(gl("webgl2"))
            append("\nWebGL 1: ")
            append(gl("webgl1"))
            append("\nWebAssembly: ")
            append(yes("webAssembly"))
            append(" · SharedArrayBuffer: ")
            append(yes("sharedArrayBuffer"))
            append("\nCross-origin isolated: ")
            append(yes("crossOriginIsolated"))
            append(" · OffscreenCanvas: ")
            append(yes("offscreenCanvas"))
            append("\nWorkers: ")
            append(yes("workers"))
            append(" · hardware threads: ")
            append(report.optInt("hardwareConcurrency"))
            append("\nPage errors: ")
            append(report.optInt("pageErrors"))
            append(" · rejected promises: ")
            append(report.optInt("rejectedPromises"))
            append(" · context losses: ")
            append(report.optInt("contextLosses"))
            append(" · console errors: ")
            append(consoleErrors)
            if (!bridgeAvailable) append("\nNative diagnostics bridge unavailable")
        }
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                WebViewCompat.removeWebMessageListener(webView, JS_OBJECT)
            }
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

package com.lasthavens.android

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Production game screen backed by Android System WebView.
 *
 * WebView owns its own persistent Chromium profile. It is deliberately not cleared on
 * activity recreation, app updates, or normal navigation.
 */
class PlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RELOAD = "reload"
        private const val FILE_CHOOSER_REQUEST = 4817
    }

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var diagnosticsButton: Button
    private var fileChooserCallback: android.webkit.ValueCallback<Array<Uri>>? = null
    private var pageStarted = false
    private var addonBridge: GameAddonBridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_webview_game)
        applySystemBarPadding(findViewById(R.id.webGameRoot))
        webView = findViewById(R.id.gameWebView)
        progress = findViewById(R.id.gameProgress)
        diagnosticsButton = findViewById(R.id.gameDiagnostics)
        diagnosticsButton.setOnClickListener { runBrowserDiagnostics() }

        configureWebView()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = confirmLeave()
        })

        // The WebView profile persists independently of this Activity. Do not clear cache,
        // cookies, IndexedDB, Cache Storage or service-worker data here.
        if (savedInstanceState == null || intent.getBooleanExtra(EXTRA_RELOAD, false)) {
            webView.loadUrl(GameUrls.GAME_URL)
        } else {
            webView.restoreState(savedInstanceState)
            if (webView.url.isNullOrBlank()) webView.loadUrl(GameUrls.GAME_URL)
        }
    }

    private fun configureWebView() {
        webView.setBackgroundColor(android.graphics.Color.BLACK)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.isSaveEnabled = true

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            safeBrowsingEnabled = true
            mediaPlaybackRequiresUserGesture = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            setGeolocationEnabled(false)
            databaseEnabled = true
        }
        CookieManager.getInstance().setAcceptCookie(true)

        // Register origin-restricted document-start scripts before the game URL is loaded.
        addonBridge = runCatching {
            GameAddonBridge.install(webView, AddonRepository(filesDir))
        }.onFailure {
            android.util.Log.e("LastHavenWebView", "Native add-on bridge could not be installed", it)
        }.getOrNull()
        if (addonBridge == null) {
            android.util.Log.w("LastHavenWebView", "Game will start without native add-on injection")
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }

            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                if (BuildConfig.DEBUG && message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    android.util.Log.w("LastHavenWebView", "${message.message()} @ ${message.sourceId()}:${message.lineNumber()}")
                }
                return true
            }

            override fun onShowFileChooser(
                webView: WebView,
                callback: android.webkit.ValueCallback<Array<Uri>>,
                params: FileChooserParams,
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = callback
                return try {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = params.acceptTypes.filter { it.isNotBlank() && !it.startsWith(".") }
                            .firstOrNull() ?: "*/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.mode == FileChooserParams.MODE_OPEN_MULTIPLE)
                    }
                    @Suppress("DEPRECATION")
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.file_picker_title)), FILE_CHOOSER_REQUEST)
                    true
                } catch (error: Exception) {
                    fileChooserCallback = null
                    callback.onReceiveValue(null)
                    false
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (GameUrls.isInApp(uri)) return false
                GameUrls.openExternal(this@PlayActivity, uri)
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                pageStarted = true
                progress.visibility = View.VISIBLE
                progress.progress = 0
            }

            override fun onPageCommitVisible(view: WebView, url: String) {
                if (GameUrls.isGameHost(Uri.parse(url))) requestPersistentGameStorage()
            }

            override fun onPageFinished(view: WebView, url: String) {
                progress.visibility = View.GONE
                if (!pageStarted) return
                pageStarted = false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: android.webkit.WebResourceError,
            ) {
                if (request.isForMainFrame) {
                    progress.visibility = View.GONE
                    Toast.makeText(
                        this@PlayActivity,
                        getString(R.string.game_load_failed) + " (" + error.errorCode + ")",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }

            override fun onRenderProcessGone(
                view: WebView,
                detail: android.webkit.RenderProcessGoneDetail,
            ): Boolean {
                android.util.Log.e("LastHavenWebView", "Renderer gone; crashed=${detail.didCrash()}")
                if (!isFinishing && !isDestroyed) recreate()
                return true
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            runCatching {
                val uri = Uri.parse(url)
                require(uri.scheme == "https" && GameUrls.isGameHost(uri)) { "Blocked download from untrusted host" }
                val candidateName = contentDisposition
                    ?.substringAfter("filename=", "")
                    ?.trim('"', ' ', ';')
                    ?.takeIf { it.isNotBlank() }
                    ?: uri.lastPathSegment?.takeIf { it.isNotBlank() }
                    ?: "lasthavens-download.bin"
                val fileName = DownloadFileNamePolicy.sanitize(candidateName, "lasthavens-download.bin")
                val request = DownloadManager.Request(uri)
                    .setTitle(fileName)
                    .setMimeType(mimeType ?: "application/octet-stream")
                    .setDescription(getString(R.string.download_description))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Lasthavens/$fileName")
                    .addRequestHeader("User-Agent", userAgent ?: webView.settings.userAgentString)
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, R.string.download_failed, Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Collects browser capabilities from the live game origin without changing game settings.
     * The report helps distinguish site-side browser detection from missing WebView features.
     */
    private fun runBrowserDiagnostics() {
        val script = """
            (() => {
              const result = {
                secureContext: window.isSecureContext === true,
                webAssembly: typeof WebAssembly !== "undefined" &&
                  WebAssembly.validate(new Uint8Array([0,97,115,109,1,0,0,0])),
                workers: typeof Worker === "function",
                sharedArrayBuffer: typeof SharedArrayBuffer === "function",
                crossOriginIsolated: window.crossOriginIsolated === true,
                offscreenCanvas: typeof OffscreenCanvas === "function",
                hardwareConcurrency: navigator.hardwareConcurrency || null,
                userAgent: navigator.userAgent || "unavailable",
                webgl2: false,
                webgl2Renderer: "unavailable",
                webgl1: false
              };
              try {
                const canvas = document.createElement("canvas");
                const gl2 = canvas.getContext("webgl2");
                result.webgl2 = !!gl2;
                if (gl2) {
                  const debug = gl2.getExtension("WEBGL_debug_renderer_info");
                  result.webgl2Renderer = debug
                    ? gl2.getParameter(debug.UNMASKED_RENDERER_WEBGL)
                    : gl2.getParameter(gl2.RENDERER);
                }
                result.webgl1 = !!(canvas.getContext("webgl") || canvas.getContext("experimental-webgl"));
              } catch (_) {}
              return JSON.stringify(result);
            })();
        """.trimIndent()
        webView.evaluateJavascript(script) { encoded ->
            val report = runCatching {
                val json = org.json.JSONTokener(encoded).nextValue() as String
                org.json.JSONObject(json)
            }.getOrNull()
            if (report == null) {
                showDiagnosticsDialog("Could not read the browser capability report.")
                return@evaluateJavascript
            }
            val provider = WebView.getCurrentWebViewPackage()
            val lines = listOf(
                "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                "WebView provider: ${provider?.packageName ?: "unknown"} ${provider?.versionName ?: ""}".trim(),
                "WebGL 2: ${yesNo(report.optBoolean("webgl2"))}",
                "WebGL 2 renderer: ${report.optString("webgl2Renderer", "unknown")}",
                "WebGL 1: ${yesNo(report.optBoolean("webgl1"))}",
                "WebAssembly: ${yesNo(report.optBoolean("webAssembly"))}",
                "Web Workers: ${yesNo(report.optBoolean("workers"))}",
                "SharedArrayBuffer: ${yesNo(report.optBoolean("sharedArrayBuffer"))}",
                "Cross-origin isolated: ${yesNo(report.optBoolean("crossOriginIsolated"))}",
                "OffscreenCanvas: ${yesNo(report.optBoolean("offscreenCanvas"))}",
                "Secure context: ${yesNo(report.optBoolean("secureContext"))}",
                "CPU threads reported: ${report.optString("hardwareConcurrency", "unknown")}",
                "WebKit messaging: ${yesNo(androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.WEB_MESSAGE_LISTENER))}",
                "Document-start scripts: ${yesNo(androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.DOCUMENT_START_SCRIPT))}",
                "ArrayBuffer bridge: ${yesNo(androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.WEB_MESSAGE_ARRAY_BUFFER))}",
            )
            val text = lines.joinToString("\n") +
                "\n\nUser agent: " + report.optString("userAgent", "unknown")
            showDiagnosticsDialog(text)
        }
    }

    private fun yesNo(value: Boolean) = if (value) "Available" else "Unavailable"

    private fun showDiagnosticsDialog(report: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.game_diagnostics_title)
            .setMessage(report)
            .setNeutralButton(R.string.game_diagnostics_copy) { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("LastHavenPlus browser diagnostics", report))
                Toast.makeText(this, R.string.game_diagnostics_copied, Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton(R.string.game_diagnostics_chrome) { _, _ ->
                val chrome = Intent(Intent.ACTION_VIEW, Uri.parse(GameUrls.GAME_URL))
                    .setPackage("com.android.chrome")
                try {
                    startActivity(chrome)
                } catch (_: android.content.ActivityNotFoundException) {
                    GameUrls.openExternal(this, Uri.parse(GameUrls.GAME_URL))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun requestPersistentGameStorage() {
        val script = """
            (async () => {
              try {
                const persisted = navigator.storage && navigator.storage.persist
                  ? await navigator.storage.persist() : null;
                const estimate = navigator.storage && navigator.storage.estimate
                  ? await navigator.storage.estimate() : null;
                console.info("[LastHavenPlus] storage", JSON.stringify({
                  persistent: persisted,
                  usage: estimate && estimate.usage,
                  quota: estimate && estimate.quota
                }));
              } catch (error) {
                console.warn("[LastHavenPlus] storage estimate unavailable", String(error));
              }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    @Deprecated("Handled for the WebView file chooser")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != FILE_CHOOSER_REQUEST) return
        val callback = fileChooserCallback ?: return
        fileChooserCallback = null
        val results = if (resultCode == RESULT_OK) {
            WebChromeClient.FileChooserParams.parseResult(resultCode, data)
        } else null
        callback.onReceiveValue(results)
    }

    private fun confirmLeave() {
        AlertDialog.Builder(this)
            .setTitle(R.string.leave_title)
            .setMessage(R.string.leave_message)
            .setPositiveButton(R.string.leave_stay, null)
            .setNeutralButton(R.string.leave_reload) { _, _ -> webView.reload() }
            .setNegativeButton(R.string.leave_home) { _, _ -> finish() }
            .show()
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

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        if (::webView.isInitialized) {
            addonBridge?.close()
            addonBridge = null
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            webView.setDownloadListener(null)
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
    }
}

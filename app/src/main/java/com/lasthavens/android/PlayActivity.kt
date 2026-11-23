package com.lasthavens.android

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
 * activity recreation, app updates, or normal navigation. Gecko remains temporarily
 * available to the legacy Addon Manager until native migration is complete.
 */
class PlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RELOAD = "reload"
        private const val FILE_CHOOSER_REQUEST = 4817
    }

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private var fileChooserCallback: android.webkit.ValueCallback<Array<Uri>>? = null
    private var pageStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_webview_game)
        applySystemBarPadding(findViewById(R.id.webGameRoot))
        webView = findViewById(R.id.gameWebView)
        progress = findViewById(R.id.gameProgress)

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

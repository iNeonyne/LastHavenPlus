package com.lasthavens.android

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

/**
 * Full-screen game. Uses the app-wide game session, so the game keeps its state when the
 * user goes back to the home screen and returns.
 */
class PlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RELOAD = "reload"
    }

    private val app get() = App.instance
    private val picker = FilePicker(this)
    private lateinit var geckoView: GeckoView
    private lateinit var progress: ProgressBar
    private lateinit var session: GeckoSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_gecko)
        geckoView = findViewById(R.id.gecko)
        progress = findViewById(R.id.progress)

        // Back never navigates inside the game; it asks first so a stray swipe can't lose the session.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = confirmLeave()
        })

        attachSession()
        // The extension must be installed before the first load so its document_start scripts apply.
        app.onExtensionReady { ext ->
            if (isDestroyed) return@onExtensionReady
            if (ext == null) Toast.makeText(this, R.string.game_without_addons, Toast.LENGTH_LONG).show()
            startOrRefreshGame(intent.getBooleanExtra(EXTRA_RELOAD, false))
        }
    }

    private fun attachSession() {
        session = app.gameSession()
        session.contentDelegate = AppContentDelegate(
            context = this,
            onClose = { finish() },
            onProcessGone = {
                app.discardGameSession()
                recreate()
            },
            onFullScreenChanged = { hideSystemBars() },
        )
        session.promptDelegate = AppPromptDelegate(this, picker)
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest,
            ): GeckoResult<AllowOrDeny>? {
                val uri = Uri.parse(request.uri)
                return if (GameUrls.isInApp(uri)) {
                    GeckoResult.fromValue(AllowOrDeny.ALLOW)
                } else {
                    GameUrls.openExternal(this@PlayActivity, uri)
                    GeckoResult.fromValue(AllowOrDeny.DENY)
                }
            }

            // window.open / target=_blank: keep game URLs in the game view, send the rest to the browser.
            override fun onNewSession(session: GeckoSession, uri: String): GeckoResult<GeckoSession>? {
                val parsed = Uri.parse(uri)
                if (GameUrls.isGameHost(parsed)) session.loadUri(uri) else GameUrls.openExternal(this@PlayActivity, parsed)
                return null
            }
        }
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                progress.progress = 0
                progress.visibility = android.view.View.VISIBLE
            }

            override fun onProgressChange(session: GeckoSession, value: Int) {
                progress.progress = value
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                progress.visibility = android.view.View.GONE
            }
        }
        geckoView.setSession(session)
    }

    private fun startOrRefreshGame(reloadRequested: Boolean) {
        val needsReload = reloadRequested || app.reloadGameOnNextPlay
        app.reloadGameOnNextPlay = false
        if (!app.gameLoaded) {
            app.gameLoaded = true
            session.loadUri(GameUrls.GAME_URL)
        } else if (needsReload) {
            session.reload()
        }
    }

    private fun confirmLeave() {
        AlertDialog.Builder(this)
            .setTitle(R.string.leave_title)
            .setMessage(R.string.leave_message)
            .setPositiveButton(R.string.leave_stay, null)
            .setNeutralButton(R.string.leave_reload) { _, _ -> session.reload() }
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

    override fun onDestroy() {
        // Detach UI delegates; the session itself stays alive in App.
        if (::session.isInitialized) {
            session.contentDelegate = null
            session.promptDelegate = null
            session.navigationDelegate = null
            session.progressDelegate = null
        }
        geckoView.releaseSession()
        super.onDestroy()
    }
}

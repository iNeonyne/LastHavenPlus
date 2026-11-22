package com.lasthavens.android

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.lang.ref.WeakReference

/**
 * Hosts the bundled Addon Manager's own page (manager.html) in a separate GeckoSession.
 * It shares the runtime/profile with the game, so add-ons stored here are served to the game.
 */
class ManagerActivity : AppCompatActivity() {

    private val app get() = App.instance
    private val picker = FilePicker(this)
    private lateinit var geckoView: GeckoView
    private lateinit var progress: ProgressBar
    private var session: GeckoSession? = null
    private var canGoBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContentView(R.layout.activity_gecko)
        applySystemBarPadding(findViewById<View>(R.id.root))
        geckoView = findViewById(R.id.gecko)
        progress = findViewById(R.id.progress)
        app.managerRef = WeakReference(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val s = session
                if (canGoBack && s != null) s.goBack() else finish()
            }
        })

        app.onExtensionReady { ext ->
            if (isDestroyed) return@onExtensionReady
            val base = ext?.metaData?.baseUrl
            if (base == null) {
                Toast.makeText(this, R.string.manager_unavailable, Toast.LENGTH_LONG).show()
                finish()
                return@onExtensionReady
            }
            openManager(base + GameUrls.ADDON_MANAGER_PAGE)
        }
    }

    private fun openManager(url: String) {
        val s = GeckoSession()
        s.contentDelegate = AppContentDelegate(context = this, onClose = { finish() }, onProcessGone = { recreate() })
        s.promptDelegate = AppPromptDelegate(this, picker)
        s.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                this@ManagerActivity.canGoBack = canGoBack
            }
        }
        s.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                progress.visibility = View.VISIBLE
            }

            override fun onProgressChange(session: GeckoSession, value: Int) {
                progress.progress = value
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                progress.visibility = View.GONE
                if (!success && !isFinishing) {
                    Toast.makeText(this@ManagerActivity, R.string.manager_load_failed, Toast.LENGTH_LONG).show()
                }
            }
        }
        s.open(app.runtime)
        geckoView.setSession(s)
        s.loadUri(url)
        session = s
    }

    override fun onDestroy() {
        // Add-ons may have changed; make the next Play reload the game so they take effect.
        if (app.gameLoaded) app.reloadGameOnNextPlay = true
        if (app.managerRef?.get() === this) app.managerRef = null
        geckoView.releaseSession()
        session?.close()
        session = null
        super.onDestroy()
    }
}

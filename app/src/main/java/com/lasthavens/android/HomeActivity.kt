package com.lasthavens.android

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContentView(R.layout.activity_home)
        applySystemBarPadding(findViewById(R.id.content), top = false)

        findViewById<TextView>(R.id.versionApp).text = getString(R.string.version_app, Versions.app)
        findViewById<TextView>(R.id.versionAddons).text =
            getString(R.string.version_addons, Versions.addonManager(this))

        findViewById<android.view.View>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnManager).setOnClickListener {
            startActivity(Intent(this, ManagerActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnStorage).setOnClickListener {
            startActivity(Intent(this, StorageActivity::class.java))
        }
    }
}

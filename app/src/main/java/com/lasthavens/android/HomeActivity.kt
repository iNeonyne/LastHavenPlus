package com.lasthavens.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class HomeActivity : AppCompatActivity() {
    private lateinit var updateButton: View
    private val updateManager by lazy { UpdateManager(applicationContext) }

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
        findViewById<TextView>(R.id.versionAddons).text = getString(R.string.version_addons, Versions.addonManager(this))
        findViewById<View>(R.id.btnPlay).setOnClickListener { startActivity(Intent(this, PlayActivity::class.java)) }
        findViewById<View>(R.id.btnManager).setOnClickListener { startActivity(Intent(this, ManagerActivity::class.java)) }
        findViewById<View>(R.id.btnStorage).setOnClickListener { startActivity(Intent(this, StorageActivity::class.java)) }
        updateButton = findViewById(R.id.btnUpdate)
        updateButton.setOnClickListener { checkForUpdates() }
    }

    private fun checkForUpdates() {
        updateButton.isEnabled = false
        val checking = AlertDialog.Builder(this)
            .setTitle(R.string.update_checking)
            .setMessage(R.string.update_checking_message)
            .setCancelable(false)
            .create()
        checking.show()
        updateManager.checkAndDownload { result ->
            if (isFinishing || isDestroyed) return@checkAndDownload
            if (checking.isShowing) checking.dismiss()
            updateButton.isEnabled = true
            result.fold(
                onSuccess = { update ->
                    if (update == null) {
                        AlertDialog.Builder(this).setTitle(R.string.update_title)
                            .setMessage(R.string.update_none).setPositiveButton(android.R.string.ok, null).show()
                    } else {
                        val size = android.text.format.Formatter.formatShortFileSize(this, update.sizeBytes)
                        AlertDialog.Builder(this).setTitle(R.string.update_available)
                            .setMessage(getString(R.string.update_available_message, update.versionName, update.runNumber, size, update.commitSha))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.update_install) { _, _ ->
                                updateButton.isEnabled = false
                                val installing = AlertDialog.Builder(this)
                                    .setTitle(R.string.update_installing)
                                    .setMessage(R.string.update_installing_message)
                                    .setCancelable(false)
                                    .create()
                                installing.show()
                                updateManager.install(this, update) { installResult ->
                                    if (isFinishing || isDestroyed) return@install
                                    if (installing.isShowing) installing.dismiss()
                                    updateButton.isEnabled = true
                                    installResult
                                        .onSuccess {
                                            Toast.makeText(this, R.string.update_install_handoff, Toast.LENGTH_SHORT).show()
                                        }
                                        .onFailure { error ->
                                            AlertDialog.Builder(this).setTitle(R.string.update_install_title)
                                                .setMessage(error.message ?: getString(R.string.update_install_failed))
                                                .setPositiveButton(android.R.string.ok, null).show()
                                        }
                                }
                            }.show()
                    }
                },
                onFailure = { error ->
                    AlertDialog.Builder(this).setTitle(R.string.update_failed_title)
                        .setMessage(error.message ?: getString(R.string.update_failed))
                        .setPositiveButton(android.R.string.ok, null).show()
                },
            )
        }
    }
}

package com.lasthavens.android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
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
        val progressView = LayoutInflater.from(this).inflate(R.layout.dialog_update_progress, null)
        val progressDialog = AlertDialog.Builder(this)
            .setView(progressView)
            .setCancelable(false)
            .create()
        progressDialog.show()

        val stageView = progressView.findViewById<TextView>(R.id.updateProgressStage)
        val detailView = progressView.findViewById<TextView>(R.id.updateProgressDetail)
        val progressBar = progressView.findViewById<ProgressBar>(R.id.updateProgressBar)
        val percentView = progressView.findViewById<TextView>(R.id.updateProgressPercent)

        fun showStage(stage: UpdateStage, downloaded: Long = 0, total: Long = 0) {
            when (stage) {
                UpdateStage.CHECKING -> {
                    stageView.setText(R.string.update_stage_checking)
                    detailView.setText(R.string.update_stage_checking_detail)
                    progressBar.isIndeterminate = true
                    percentView.visibility = View.GONE
                }
                UpdateStage.FETCHING_CHECKSUM -> {
                    stageView.setText(R.string.update_stage_checking)
                    detailView.setText(R.string.update_stage_checksum_detail)
                    progressBar.isIndeterminate = true
                    percentView.visibility = View.GONE
                }
                UpdateStage.DOWNLOADING -> {
                    stageView.setText(R.string.update_stage_downloading)
                    detailView.text = getString(
                        R.string.update_stage_downloading_detail,
                        android.text.format.Formatter.formatShortFileSize(this, total),
                    )
                    progressBar.isIndeterminate = false
                    progressBar.max = 1000
                    val percent = if (total > 0) ((downloaded * 1000) / total).toInt().coerceIn(0, 1000) else 0
                    progressBar.progress = percent
                    percentView.visibility = View.VISIBLE
                    percentView.text = getString(R.string.update_progress_percent, percent / 10)
                }
                UpdateStage.VERIFYING -> {
                    stageView.setText(R.string.update_stage_verifying)
                    detailView.setText(R.string.update_stage_verifying_detail)
                    progressBar.isIndeterminate = true
                    percentView.visibility = View.GONE
                }
            }
        }

        updateManager.checkAndDownload({ progress ->
            if (!isFinishing && !isDestroyed && progressDialog.isShowing) {
                showStage(progress.stage, progress.downloadedBytes, progress.totalBytes)
            }
        }) { result ->
            if (isFinishing || isDestroyed) return@checkAndDownload
            if (progressDialog.isShowing) progressDialog.dismiss()
            updateButton.isEnabled = true
            result.fold(
                onSuccess = { update ->
                    if (update == null) {
                        AlertDialog.Builder(this).setTitle(R.string.update_title)
                            .setMessage(R.string.update_none)
                            .setPositiveButton(android.R.string.ok, null).show()
                    } else {
                        val availableView = LayoutInflater.from(this).inflate(R.layout.dialog_update_available, null)
                        availableView.findViewById<TextView>(R.id.updateAvailableVersion).text =
                            getString(R.string.update_available_version, update.versionName)
                        availableView.findViewById<TextView>(R.id.updateAvailableSize).text =
                            getString(R.string.update_available_size, android.text.format.Formatter.formatShortFileSize(this, update.sizeBytes))
                        AlertDialog.Builder(this)
                            .setView(availableView)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.update_install) { _, _ -> installUpdate(update) }
                            .show()
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

    private fun installUpdate(update: AvailableUpdate) {
        updateButton.isEnabled = false
        val progressView = LayoutInflater.from(this).inflate(R.layout.dialog_update_progress, null)
        progressView.findViewById<TextView>(R.id.updateProgressStage).setText(R.string.update_installing)
        progressView.findViewById<TextView>(R.id.updateProgressDetail).setText(R.string.update_installing_message)
        progressView.findViewById<ProgressBar>(R.id.updateProgressBar).isIndeterminate = true
        progressView.findViewById<TextView>(R.id.updateProgressPercent).visibility = View.GONE
        val installing = AlertDialog.Builder(this).setView(progressView).setCancelable(false).create()
        installing.show()
        updateManager.install(this, update) { result ->
            if (isFinishing || isDestroyed) return@install
            if (installing.isShowing) installing.dismiss()
            updateButton.isEnabled = true
            result.onSuccess {
                Toast.makeText(this, R.string.update_install_handoff, Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                AlertDialog.Builder(this).setTitle(R.string.update_install_title)
                    .setMessage(error.message ?: getString(R.string.update_install_failed))
                    .setPositiveButton(android.R.string.ok, null).show()
            }
        }
    }
}

package com.lasthavens.android

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import java.util.concurrent.Executors

/**
 * Safely verifies and stages a Gecko Addon Manager migration archive.
 *
 * This screen deliberately does not activate the staged library. The legacy Gecko profile
 * remains the source of truth until the native repository and rollback path are complete.
 */
class MigrationImportActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var choose: Button
    private lateinit var progress: ProgressBar

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) verifyArchive(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val density = resources.displayMetrics.density
        fun px(value: Int) = (value * density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(px(24), px(16), px(24), px(24))
            setBackgroundColor(getColor(R.color.bg))
        }
        setContentView(root)
        applySystemBarPadding(root, top = true, bottom = true)

        root.addView(TextView(this).apply {
            text = getString(R.string.migration_back)
            textSize = 16f
            setTextColor(getColor(R.color.gold_light))
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(-1, px(48)))

        root.addView(TextView(this).apply {
            text = getString(R.string.migration_title)
            textSize = 26f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(getColor(R.color.gold_light))
        })
        root.addView(TextView(this).apply {
            text = getString(R.string.migration_intro)
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, px(10), 0, px(18))
        })

        choose = Button(this).apply {
            text = getString(R.string.migration_choose)
            setOnClickListener { picker.launch(arrayOf("application/zip", "application/octet-stream")) }
        }
        root.addView(choose, LinearLayout.LayoutParams(-1, px(52)))

        progress = ProgressBar(this).apply { isIndeterminate = true; visibility = android.view.View.GONE }
        root.addView(progress, LinearLayout.LayoutParams(-1, px(4)).apply { topMargin = px(16) })
        status = TextView(this).apply {
            text = getString(R.string.migration_idle)
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, px(14), 0, 0)
        }
        root.addView(status)
    }

    private fun verifyArchive(uri: Uri) {
        choose.isEnabled = false
        progress.visibility = android.view.View.VISIBLE
        status.setText(R.string.migration_verifying)
        executor.execute {
            val result = runCatching {
                val declaredSize = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
                require(declaredSize <= MigrationArchiveReader.MAX_ARCHIVE_BYTES) {
                    "Migration archive exceeds the 128 MB limit"
                }
                if (declaredSize > 0L) {
                    val input = contentResolver.openInputStream(uri)
                        ?: error("Could not open the selected archive")
                    input.use {
                        MigrationArchiveReader.stage(
                            input = it,
                            archiveBytes = declaredSize,
                            stagingRoot = java.io.File(filesDir, "manager-migration/staging"),
                        )
                    }
                } else {
                    // Some document providers do not report a length. Spool with a hard bound
                    // so the validator always receives the real compressed size.
                    val temp = java.io.File(cacheDir, "migration-${java.util.UUID.randomUUID()}.zip")
                    try {
                        val input = contentResolver.openInputStream(uri)
                            ?: error("Could not open the selected archive")
                        input.use { source ->
                            java.io.FileOutputStream(temp).use { output ->
                                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                var total = 0L
                                while (true) {
                                    val read = source.read(buffer)
                                    if (read < 0) break
                                    total += read
                                    require(total <= MigrationArchiveReader.MAX_ARCHIVE_BYTES) {
                                        "Migration archive exceeds the 128 MB limit"
                                    }
                                    output.write(buffer, 0, read)
                                }
                                output.fd.sync()
                            }
                        }
                        MigrationArchiveReader.stage(
                            input = temp.inputStream(),
                            archiveBytes = temp.length(),
                            stagingRoot = java.io.File(filesDir, "manager-migration/staging"),
                        )
                    } finally {
                        temp.delete()
                    }
                }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                choose.isEnabled = true
                progress.visibility = android.view.View.GONE
                result.fold(
                    onSuccess = { stage ->
                        status.setText(R.string.migration_verified)
                        val addons = stage.manifest.getJSONObject("library").getJSONArray("addons")
                        val details = getString(
                            R.string.migration_preview,
                            addons.length(),
                            stage.fileCount,
                            android.text.format.Formatter.formatShortFileSize(this, stage.expandedBytes),
                        )
                        AlertDialog.Builder(this)
                            .setTitle(R.string.migration_verified_title)
                            .setMessage(details)
                            .setPositiveButton(R.string.migration_keep_stage, null)
                            .setNegativeButton(R.string.migration_discard_stage) { _, _ ->
                                stage.directory.deleteRecursively()
                                status.setText(R.string.migration_stage_discarded)
                            }
                            .show()
                    },
                    onFailure = { error ->
                        status.text = getString(R.string.migration_failed, error.message ?: getString(R.string.migration_unknown_error))
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}

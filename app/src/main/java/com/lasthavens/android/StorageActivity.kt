package com.lasthavens.android

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import java.util.Locale
import java.util.concurrent.Executors

class StorageActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var persistent: TextView
    private lateinit var cache: TextView
    private lateinit var available: TextView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var refresh: Button

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
            setPadding(px(24), px(16), px(24), px(24))
            setBackgroundColor(getColor(R.color.bg))
        }
        setContentView(ScrollView(this).apply { addView(root) })
        applySystemBarPadding(root, top = true, bottom = true)

        root.addView(TextView(this).apply {
            text = getString(R.string.storage_back)
            textSize = 16f
            setTextColor(getColor(R.color.gold_light))
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(-1, px(48)))
        root.addView(TextView(this).apply {
            text = getString(R.string.storage_title)
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.gold_light))
        })
        root.addView(bodyText(R.string.storage_intro, 14f).apply { setPadding(0, px(8), 0, px(12)) })
        root.addView(Button(this).apply {
            text = getString(R.string.migration_tool)
            setTextColor(getColor(R.color.text_primary))
            setOnClickListener { startActivity(android.content.Intent(this@StorageActivity, MigrationImportActivity::class.java)) }
        }, LinearLayout.LayoutParams(-1, px(52)).apply { bottomMargin = px(8) })
        persistent = addMetric(root, R.string.storage_persistent_label, R.string.storage_persistent_detail)
        cache = addMetric(root, R.string.storage_cache_label, R.string.storage_cache_detail)
        available = addMetric(root, R.string.storage_available_label, null)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            visibility = View.GONE
        }
        root.addView(progress, LinearLayout.LayoutParams(-1, px(4)).apply { topMargin = px(12) })
        status = bodyText(R.string.storage_scanning, 12f)
        root.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(12) })
        refresh = Button(this).apply {
            text = getString(R.string.storage_refresh)
            setTextColor(getColor(R.color.text_primary))
            setOnClickListener { refreshStorage() }
        }
        root.addView(refresh, LinearLayout.LayoutParams(-1, px(52)).apply { topMargin = px(12) })
        refreshStorage()
    }

    private fun bodyText(resource: Int, size: Float) = TextView(this).apply {
        setText(resource)
        textSize = size
        setTextColor(getColor(R.color.text_secondary))
    }

    private fun addMetric(parent: LinearLayout, label: Int, detail: Int?): TextView {
        val density = resources.displayMetrics.density
        fun px(value: Int) = (value * density).toInt()
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(16), px(14), px(16), px(14))
            setBackgroundColor(getColor(R.color.surface))
        }
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(10) })
        card.addView(TextView(this).apply {
            setText(label)
            textSize = 16f
            setTextColor(getColor(R.color.text_primary))
        })
        val value = TextView(this).apply {
            text = "—"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.gold_light))
            setPadding(0, px(5), 0, 0)
        }
        card.addView(value)
        if (detail != null) card.addView(bodyText(detail, 12f).apply { setPadding(0, px(5), 0, 0) })
        return value
    }

    private fun refreshStorage() {
        refresh.isEnabled = false
        progress.visibility = View.VISIBLE
        status.setText(R.string.storage_scanning)
        executor.execute {
            val result = runCatching { StorageUsage.measure(applicationContext) }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                progress.visibility = View.GONE
                refresh.isEnabled = true
                result.onSuccess { data ->
                    persistent.text = formatBytes(data.persistentProfileAndAppBytes)
                    cache.text = formatBytes(data.cacheBytes)
                    available.text = formatBytes(data.availableDeviceBytes)
                    status.setText(if (data.scanComplete) R.string.storage_scan_complete else R.string.storage_scan_partial)
                }.onFailure { status.setText(R.string.storage_scan_failed) }
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return getString(R.string.storage_bytes, bytes)
        val units = arrayOf("KB", "MB", "GB", "TB")
        var amount = bytes.toDouble()
        var index = -1
        do {
            amount /= 1024.0
            index++
        } while (amount >= 1024.0 && index < units.lastIndex)
        return String.format(Locale.getDefault(), "%.2f %s", amount, units[index])
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}

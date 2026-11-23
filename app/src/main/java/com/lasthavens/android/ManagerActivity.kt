package com.lasthavens.android

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.WindowCompat

/** Native add-on library UI. All changes are committed as atomic repository generations. */
class ManagerActivity : AppCompatActivity() {
    private val repository by lazy { AddonRepository(filesDir) }
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private lateinit var importButton: Button

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
            setPadding(px(20), px(12), px(20), px(20))
            setBackgroundColor(getColor(R.color.bg))
        }
        setContentView(root)
        applySystemBarPadding(root, top = true, bottom = true)

        root.addView(TextView(this).apply {
            text = "‹ Home"
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(getColor(R.color.gold_light))
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(-1, px(44)))

        root.addView(TextView(this).apply {
            text = getString(R.string.addon_manager)
            textSize = 27f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(getColor(R.color.gold_light))
        }, LinearLayout.LayoutParams(-1, -2))

        root.addView(TextView(this).apply {
            text = "Installed add-ons are stored in app-private native storage. Changes are committed atomically."
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, px(8), 0, px(14))
        }, LinearLayout.LayoutParams(-1, -2))

        importButton = Button(this).apply {
            text = getString(R.string.migration_choose)
            setOnClickListener { startActivity(Intent(this@ManagerActivity, MigrationImportActivity::class.java)) }
        }
        root.addView(importButton, LinearLayout.LayoutParams(-1, px(50)))

        status = TextView(this).apply {
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, px(14), 0, px(8))
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    override fun onResume() {
        super.onResume()
        refreshLibrary()
    }

    private fun refreshLibrary() {
        list.removeAllViews()
        val active = runCatching { repository.active() }.getOrElse {
            status.text = "Native library could not be read: ${it.message ?: "unknown error"}"
            return
        }
        if (active == null) {
            status.text = "No add-ons imported yet. Import a migration ZIP exported by Addon Manager 1.0.6."
            return
        }
        val addons = active.manifest.getJSONObject("library").getJSONArray("addons")
        val enabled = (0 until addons.length()).count { addons.getJSONObject(it).optBoolean("enabled", false) }
        status.text = "$enabled of ${addons.length()} add-ons enabled · Native storage"

        for (index in 0 until addons.length()) {
            val addon = addons.getJSONObject(index)
            val folder = addon.getString("folder")
            val version = addon.optString("version", "Unknown version")
            val switch = SwitchCompat(this).apply {
                text = "$folder  ·  $version"
                textSize = 15f
                setTextColor(getColor(R.color.text_primary))
                isChecked = addon.optBoolean("enabled", false)
                setPadding(px(14), px(10), px(14), px(10))
                setBackgroundColor(getColor(R.color.surface))
                setOnCheckedChangeListener { _, checked ->
                    isEnabled = false
                    Thread {
                        val result = runCatching { repository.setEnabled(folder, checked) }
                        runOnUiThread {
                            if (isFinishing || isDestroyed) return@runOnUiThread
                            result.onSuccess { refreshLibrary() }
                                .onFailure {
                                    Toast.makeText(this@ManagerActivity, it.message ?: "Could not update add-on", Toast.LENGTH_LONG).show()
                                    refreshLibrary()
                                }
                        }
                    }.start()
                }
            }
            list.addView(switch, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = px(8) })
        }
    }

    private fun px(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

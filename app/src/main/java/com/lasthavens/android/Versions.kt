package com.lasthavens.android

import android.content.Context
import org.json.JSONObject

object Versions {
    const val EXTENSION_DIR = "extensions/lh-addon-manager"

    val app: String get() = BuildConfig.VERSION_NAME

    /** Read from the bundled extension manifest so the home screen always shows the real version. */
    fun addonManager(context: Context): String = runCatching {
        context.assets.open("$EXTENSION_DIR/manifest.json").bufferedReader().use {
            JSONObject(it.readText()).getString("version")
        }
    }.getOrDefault("?")
}

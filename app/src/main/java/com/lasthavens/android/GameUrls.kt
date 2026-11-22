package com.lasthavens.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object GameUrls {
    const val GAME_URL = "https://alt.lasthavens.com/play"
    const val ADDON_MANAGER_PAGE = "manager.html"

    /** True for lasthavens.com and any of its subdomains. */
    fun isGameHost(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        return host == "lasthavens.com" || host.endsWith(".lasthavens.com")
    }

    /**
     * Navigation that is allowed to stay inside the app. Everything else (other websites)
     * is handed to the system browser so the game view cannot be navigated away by accident.
     */
    fun isInApp(uri: Uri): Boolean = when (uri.scheme?.lowercase()) {
        "about", "blob", "data", "moz-extension", "resource", "javascript" -> true
        "http", "https" -> isGameHost(uri)
        else -> false
    }

    fun openExternal(context: Context, uri: Uri) {
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
        }
    }
}

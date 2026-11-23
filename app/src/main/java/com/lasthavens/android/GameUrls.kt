package com.lasthavens.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object GameUrls {
    const val GAME_URL = "https://lasthavens.com/play"
    const val ADDON_MANAGER_PAGE = "manager.html"

    /** True for lasthavens.com and any of its subdomains. */
    fun isGameHost(uri: Uri): Boolean = NavigationPolicy.isGameHost(uri.toString())

    /** Keep only approved game and Gecko-internal navigations inside the app. */
    fun isInApp(uri: Uri): Boolean = NavigationPolicy.isInApp(uri.toString())

    fun openExternal(context: Context, uri: Uri) {
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
        }
    }
}

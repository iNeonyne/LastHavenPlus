package com.lasthavens.android

import java.net.URI
import java.util.Locale

/**
 * Pure JVM URL policy so the navigation boundary can be regression-tested without Android.
 * Only HTTPS game pages and their blob URLs stay in the app.
 */
object NavigationPolicy {
    fun isGameHost(rawUrl: String): Boolean {
        val uri = parse(rawUrl) ?: return false
        if (uri.scheme?.lowercase(Locale.ROOT) != "https") return false
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        return host == "lasthavens.com" || host.endsWith(".lasthavens.com")
    }

    fun isInApp(rawUrl: String): Boolean {
        val uri = parse(rawUrl) ?: return false
        return when (uri.scheme?.lowercase(Locale.ROOT)) {
            "https" -> isGameHost(rawUrl)
            "blob" -> isTrustedBlob(uri)
            // HTTP, javascript:, data:, file:, intent:, and unknown schemes fail closed.
            else -> false
        }
    }

    private fun isTrustedBlob(uri: URI): Boolean {
        val source = parse(uri.rawSchemeSpecificPart) ?: return false
        return when (source.scheme?.lowercase(Locale.ROOT)) {
            "https" -> isGameHost(source.toString())
            else -> false
        }
    }

    private fun parse(rawUrl: String): URI? = runCatching { URI(rawUrl) }.getOrNull()
}

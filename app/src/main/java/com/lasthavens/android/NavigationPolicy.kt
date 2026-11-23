package com.lasthavens.android

import java.net.URI
import java.util.Locale

/**
 * Pure JVM URL policy so the navigation boundary can be regression-tested without Android.
 * Only HTTPS game pages and Gecko's required internal schemes stay in the app.
 */
object NavigationPolicy {
    private val internalSchemes = setOf("about", "moz-extension", "resource")

    fun isGameHost(rawUrl: String): Boolean {
        val uri = parse(rawUrl) ?: return false
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        return host == "lasthavens.com" || host.endsWith(".lasthavens.com")
    }

    fun isInApp(rawUrl: String): Boolean {
        val uri = parse(rawUrl) ?: return false
        return when (uri.scheme?.lowercase(Locale.ROOT)) {
            in internalSchemes -> true
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
            "moz-extension" -> !source.host.isNullOrBlank()
            else -> false
        }
    }

    private fun parse(rawUrl: String): URI? = runCatching { URI(rawUrl) }.getOrNull()
}

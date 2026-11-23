package com.lasthavens.android

/**
 * Keeps untrusted download names inside the app's chosen download directory.
 * Pure Kotlin so path and control-character cases can be regression-tested on the JVM.
 */
internal object DownloadFileNamePolicy {
    fun sanitize(candidate: String, fallbackName: String): String {
        val safe = candidate
            .replace(Regex("[\\\\/:*?\\\"<>|\\p{Cntrl}]"), "_")
            .trim()
            .trimStart('.')
            .take(120)
        return safe.ifBlank { fallbackName }
    }
}

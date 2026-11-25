package com.neonyne.lasthavenlauncher


internal object UpdateVersionPolicy {
    private val versionPattern = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)")
    private val sha256Pattern = Regex("[a-fA-F0-9]{64}")

    fun isValid(version: String): Boolean = versionPattern.matches(version)

    fun versionCode(version: String): Long {
        val parts = versionPattern.matchEntire(version)?.groupValues?.drop(1)
            ?: error("Invalid release version: $version")
        val values = parts.map { it.toLong() }
        check(values.all { it <= 999L }) { "Release version components must be 999 or lower" }
        return values[0] * 1_000_000L + values[1] * 1_000L + values[2]
    }

    fun compare(first: String, second: String): Int {
        val a = versionPattern.matchEntire(first)?.groupValues?.drop(1)
            ?: error("Invalid version: $first")
        val b = versionPattern.matchEntire(second)?.groupValues?.drop(1)
            ?: error("Invalid version: $second")
        for (index in 0..2) {
            val comparison = a[index].toLong().compareTo(b[index].toLong())
            if (comparison != 0) return comparison
        }
        return 0
    }

    fun requireSha256(value: String): String {
        require(sha256Pattern.matches(value)) { "Invalid SHA-256 value in update manifest" }
        return value.lowercase()
    }

    fun requireGithubAssetUrl(value: String): String =
        UpdateNetworkPolicy.requireGithubAssetUrl(value)
}

package com.lasthavens.android

internal data class UpdateAsset(
    val name: String,
    val state: String,
    val sizeBytes: Long,
    val downloadUrl: String,
)

internal data class SelectedReleaseAssets(
    val apk: UpdateAsset,
    val checksum: UpdateAsset,
)

internal object UpdatePolicy {
    const val REPOSITORY = "iNeonyne/Lasthavens-Android"

    fun selectReleaseAssets(versionName: String, assets: List<UpdateAsset>): SelectedReleaseAssets? {
        if (!isStableVersionName(versionName)) return null
        val apkName = "LastHavenPlus-$versionName-arm64-v8a.apk"
        val apks = assets.filter { it.name == apkName && it.state == "uploaded" }
        val checksums = assets.filter { it.name == "SHA256SUMS.txt" && it.state == "uploaded" }
        if (apks.size != 1 || checksums.size != 1) return null
        return SelectedReleaseAssets(apks.single(), checksums.single())
    }

    fun isTrustedReleaseAssetUrl(value: String): Boolean {
        val uri = runCatching { java.net.URI(value) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("github.com", ignoreCase = true) &&
            uri.userInfo == null && (uri.port == -1 || uri.port == 443) &&
            uri.path?.startsWith("/$REPOSITORY/releases/download/") == true
    }

    fun isTrustedReleaseAssetRedirect(value: String): Boolean {
        val uri = runCatching { java.net.URI(value) }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false
        val trustedHost = host == "github.com" || host.endsWith(".githubusercontent.com")
        return uri.scheme.equals("https", ignoreCase = true) && trustedHost &&
            uri.userInfo == null && (uri.port == -1 || uri.port == 443)
    }

    fun supportsDevice(deviceAbis: List<String>): Boolean = "arm64-v8a" in deviceAbis

    fun isCompleteAsset(expectedSize: Long, actualSize: Long, maxSize: Long): Boolean =
        expectedSize in 1..maxSize && actualSize == expectedSize

    fun sameSigningCertificates(installed: Set<String>, candidate: Set<String>): Boolean =
        installed.isNotEmpty() && candidate.isNotEmpty() && installed == candidate

    fun isNewerVersion(currentCode: Long, offeredCode: Long): Boolean =
        currentCode > 0 && offeredCode > currentCode

    fun isStableVersionName(value: String): Boolean =
        value.matches(Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)"))

    fun isPublishedStableRelease(tag: String, draft: Boolean, prerelease: Boolean): Boolean =
        !draft && !prerelease && tag.startsWith("v") && isStableVersionName(tag.removePrefix("v"))

    fun isExpectedReleaseApkName(versionName: String, fileName: String): Boolean =
        isStableVersionName(versionName) && fileName == "LastHavenPlus-$versionName-arm64-v8a.apk"

    fun isValidSha256(value: String): Boolean =
        value.matches(Regex("[0-9a-fA-F]{64}"))

    fun checksumFileMatches(contents: String, fileName: String, sha256: String): Boolean {
        if (!isValidSha256(sha256)) return false
        return contents.lineSequence().any { line ->
            val parts = line.trim().split(Regex("\\s+"), limit = 2)
            parts.size == 2 && parts[0].equals(sha256, ignoreCase = true) &&
                parts[1].removePrefix("*") == fileName
        }
    }
}

package com.neonyne.lasthavenlauncher

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

data class AppUpdatePatch(
    val downloadUrl: String,
    val sha256: String,
    val size: Long,
    val baseVersion: String,
    val baseVersionCode: Long,
    val baseSha256: String,
)

data class AppUpdateInfo(
    val version: String,
    val versionCode: Long,
    val releaseNotes: String,
    val downloadUrl: String,
    val sha256: String,
    val size: Long,
    val patch: AppUpdatePatch?,
)

sealed class UpdateCheckResult {
    data object NoUpdate : UpdateCheckResult()
    data class Available(val update: AppUpdateInfo) : UpdateCheckResult()
    data class Failed(val message: String) : UpdateCheckResult()
}

/**
 * Opt-in GitHub Releases updater.
 *
 * A signed full APK is always published as a fallback. If a matching patch is
 * available, the user may choose it; BSDIFF reconstructs the exact signed APK
 * locally, then package metadata and the pinned signing certificate are checked
 * before Android's package installer is opened.
 */
object AppUpdater {
    private const val RELEASE_API =
        "https://api.github.com/repos/iNeonyne/Lasthavens-Android/releases/latest"
    private const val APK_ASSET_NAME = "lasthavenlauncher-arm64.apk"
    private const val MANIFEST_ASSET_NAME = "lasthavenlauncher-update.json"
    private const val MAX_APK_BYTES = 500L * 1024L * 1024L
    private const val MAX_PATCH_BYTES = 500L * 1024L * 1024L
    private const val RELEASE_CERT_SHA256 =
        "B3BBF7520E77DFC384A60B1EEE6C5F205B978F9B453849E2BE523F642018D263"

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun checkForUpdate(context: Context, callback: (UpdateCheckResult) -> Unit) {
        val appContext = context.applicationContext
        executor.execute {
            val result = try {
                val releaseConnection = openConnection(RELEASE_API)
                val release = try {
                    if (releaseConnection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        null
                    } else {
                        check(releaseConnection.responseCode == HttpURLConnection.HTTP_OK) {
                            "GitHub returned HTTP ${releaseConnection.responseCode}"
                        }
                        JSONObject(releaseConnection.inputStream.bufferedReader().use { it.readText() })
                    }
                } finally {
                    releaseConnection.disconnect()
                }

                if (release == null) {
                    UpdateCheckResult.NoUpdate
                } else {
                    val tag = release.getString("tag_name")
                    val latestVersion = tag.removePrefix("v")
                    check(isValidVersion(latestVersion)) {
                        "Release tag must use vMAJOR.MINOR.PATCH"
                    }

                    val packageInfo = installedPackageInfo(appContext)
                    val currentVersion = packageInfo.versionName ?: "0.0.0"
                    val versionComparison = compareVersions(latestVersion, currentVersion)
                    val latestVersionCode = versionCodeForVersion(latestVersion)
                    if (versionComparison < 0 ||
                        (versionComparison == 0 && latestVersionCode <= packageInfo.longVersionCode)
                    ) {
                        UpdateCheckResult.NoUpdate
                    } else {
                        val assets = release.getJSONArray("assets")
                        val assetByName = (0 until assets.length())
                            .map { assets.getJSONObject(it) }
                            .associateBy { it.optString("name") }

                        val manifestAsset = assetByName[MANIFEST_ASSET_NAME]
                            ?: error("Release is missing $MANIFEST_ASSET_NAME")
                        val manifestUrl = requireGithubUrl(manifestAsset.getString("browser_download_url"))
                        val manifestConnection = openConnection(manifestUrl)
                        val manifest = try {
                            check(manifestConnection.responseCode == HttpURLConnection.HTTP_OK) {
                                "Update manifest download failed (HTTP ${manifestConnection.responseCode})"
                            }
                            JSONObject(manifestConnection.inputStream.bufferedReader().use { it.readText() })
                        } finally {
                            manifestConnection.disconnect()
                        }

                        check(manifest.optInt("schemaVersion") == 1) {
                            "Unsupported update manifest version"
                        }
                        check(manifest.getString("version") == latestVersion) {
                            "Update manifest version does not match the release tag"
                        }

                        val target = manifest.getJSONObject("apk")
                        check(target.getString("name") == APK_ASSET_NAME) {
                            "Update manifest references an unexpected APK"
                        }
                        val fullAsset = assetByName[APK_ASSET_NAME]
                            ?: error("Release is missing $APK_ASSET_NAME")
                        val fullUrl = requireGithubUrl(fullAsset.getString("browser_download_url"))
                        val fullSha = requireSha256(target.getString("sha256"))
                        val apiDigest = fullAsset.optString("digest")
                        if (apiDigest.startsWith("sha256:")) {
                            check(apiDigest.removePrefix("sha256:").equals(fullSha, true)) {
                                "GitHub APK digest does not match the update manifest"
                            }
                        }

                        val versionCode = manifest.getLong("versionCode")
                        check(versionCode > packageInfo.longVersionCode) {
                            "Update versionCode is not newer than the installed app"
                        }
                        val targetSize = target.getLong("size")
                        check(targetSize in 1..MAX_APK_BYTES) { "Update APK size is invalid" }

                        val patch = manifest.optJSONObject("patch")?.let { patchJson ->
                            val patchName = patchJson.getString("name")
                            val patchAsset = assetByName[patchName]
                                ?: error("Release is missing differential patch $patchName")
                            val patchUrl = requireGithubUrl(patchAsset.getString("browser_download_url"))
                            val patchSha = requireSha256(patchJson.getString("sha256"))
                            val patchApiDigest = patchAsset.optString("digest")
                            if (patchApiDigest.startsWith("sha256:")) {
                                check(patchApiDigest.removePrefix("sha256:").equals(patchSha, true)) {
                                    "GitHub patch digest does not match the update manifest"
                                }
                            }
                            val patchSize = patchJson.getLong("size")
                            check(patchSize in 1..MAX_PATCH_BYTES) { "Differential patch size is invalid" }
                            AppUpdatePatch(
                                downloadUrl = patchUrl,
                                sha256 = patchSha,
                                size = patchSize,
                                baseVersion = patchJson.getString("fromVersion"),
                                baseVersionCode = patchJson.getLong("fromVersionCode"),
                                baseSha256 = requireSha256(patchJson.getString("fromSha256")),
                            ).takeIf {
                                it.baseVersion == currentVersion &&
                                    it.baseVersionCode == packageInfo.longVersionCode
                            }
                        }

                        UpdateCheckResult.Available(
                            AppUpdateInfo(
                                version = latestVersion,
                                versionCode = versionCode,
                                releaseNotes = release.optString("body").ifBlank {
                                    "A newer version of LastHavenLauncher is available."
                                },
                                downloadUrl = fullUrl,
                                sha256 = fullSha,
                                size = targetSize,
                                patch = patch,
                            ),
                        )
                    }
                }
            } catch (error: Exception) {
                UpdateCheckResult.Failed(error.message ?: "Unable to check for updates")
            }

            mainHandler.post { callback(result) }
        }
    }

    fun downloadUpdate(
        context: Context,
        update: AppUpdateInfo,
        preferDelta: Boolean,
        callback: (Result<Uri>) -> Unit,
    ) {
        val appContext = context.applicationContext
        executor.execute {
            val result = runCatching {
                val updateDirectory = File(appContext.filesDir, "updates").apply { mkdirs() }
                val finalApk = File(updateDirectory, APK_ASSET_NAME)
                val temporaryApk = File(updateDirectory, "update.apk.part")
                temporaryApk.delete()

                val patch = update.patch
                val installedInfo = installedPackageInfo(appContext)
                val canUsePatch = preferDelta &&
                    patch != null &&
                    installedInfo.versionName == patch.baseVersion &&
                    installedInfo.longVersionCode == patch.baseVersionCode

                if (canUsePatch) {
                    val delta = patch ?: error("Differential patch metadata is missing")
                    val baseApk = File(appContext.applicationInfo.sourceDir)
                    check(baseApk.isFile && baseApk.canRead()) {
                        "Installed APK is not accessible; choose the full APK instead"
                    }
                    check(sha256(baseApk).equals(delta.baseSha256, true)) {
                        "Installed APK does not match this patch; choose the full APK instead"
                    }

                    val patchFile = File(updateDirectory, "update.bsdiff.part")
                    patchFile.delete()
                    try {
                        downloadToFile(
                            delta.downloadUrl,
                            delta.sha256,
                            delta.size,
                            MAX_PATCH_BYTES,
                            patchFile,
                        )
                        BsdiffPatch.apply(baseApk, patchFile, temporaryApk, MAX_APK_BYTES)
                    } finally {
                        patchFile.delete()
                    }
                } else {
                    downloadToFile(
                        update.downloadUrl,
                        update.sha256,
                        update.size,
                        MAX_APK_BYTES,
                        temporaryApk,
                    )
                }

                check(temporaryApk.length() == update.size) {
                    "Reconstructed APK size does not match the release manifest"
                }
                check(sha256(temporaryApk).equals(update.sha256, true)) {
                    "APK checksum verification failed"
                }
                verifySignedUpdate(appContext, temporaryApk, update)

                finalApk.delete()
                check(temporaryApk.renameTo(finalApk)) { "Could not finalize downloaded APK" }
                FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.updates",
                    finalApk,
                )
            }

            mainHandler.post { callback(result) }
        }
    }

    private fun verifySignedUpdate(context: Context, apkFile: File, update: AppUpdateInfo) {
        val packageManager = context.packageManager
        @Suppress("DEPRECATION")
        val archiveInfo = packageManager.getPackageArchiveInfo(
            apkFile.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES,
        ) ?: error("Downloaded file is not a valid Android APK")

        check(archiveInfo.packageName == context.packageName) {
            "Update package ID does not match this launcher"
        }
        check(archiveInfo.longVersionCode == update.versionCode) {
            "Update APK versionCode does not match the release manifest"
        }
        check(archiveInfo.versionName == update.version) {
            "Update APK version does not match the release tag"
        }

        val installed = installedPackageInfo(context)
        val installedSigners = installed.signingInfo?.apkContentsSigners
            ?: error("Installed app signing certificate could not be read")
        val archiveSigners = archiveInfo.signingInfo?.apkContentsSigners
            ?: error("Update APK signing certificate could not be read")
        check(installedSigners.isNotEmpty() && archiveSigners.isNotEmpty()) {
            "Update APK has no signing certificate"
        }

        val installedFingerprints = installedSigners.map { certificateSha256(it.toByteArray()) }.toSet()
        val updateFingerprints = archiveSigners.map { certificateSha256(it.toByteArray()) }.toSet()
        check(RELEASE_CERT_SHA256 in installedFingerprints) {
            "Installed app does not use the configured LastHaven release key"
        }
        check(updateFingerprints == installedFingerprints && RELEASE_CERT_SHA256 in updateFingerprints) {
            "Update APK was not signed with the installed LastHaven release key"
        }
    }

    @Suppress("DEPRECATION")
    private fun installedPackageInfo(context: Context): PackageInfo =
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES,
        )

    private fun downloadToFile(
        url: String,
        expectedSha256: String,
        expectedSize: Long,
        maximumBytes: Long,
        destination: File,
    ) {
        val connection = openConnection(url)
        try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Update download failed (HTTP ${connection.responseCode})"
            }
            val contentLength = connection.contentLengthLong
            check(contentLength <= maximumBytes) { "Update file is unexpectedly large" }
            if (expectedSize > 0 && contentLength >= 0) {
                check(contentLength == expectedSize) { "Update file size does not match its manifest" }
            }

            val digest = MessageDigest.getInstance("SHA-256")
            var totalBytes = 0L
            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        totalBytes += count
                        check(totalBytes <= maximumBytes) { "Update file is unexpectedly large" }
                        digest.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }
            check(totalBytes == expectedSize) { "Downloaded file size does not match its manifest" }
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            check(actual.equals(expectedSha256, true)) { "Downloaded file checksum verification failed" }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun certificateSha256(certificate: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(certificate)
            .joinToString("") { "%02X".format(it) }

    private fun requireSha256(value: String): String {
        check(SHA256_PATTERN.matches(value)) { "Invalid SHA-256 value in update manifest" }
        return value.lowercase()
    }

    private fun requireGithubUrl(value: String): String {
        check(value.startsWith("https://github.com/")) {
            "Release assets must be hosted on GitHub"
        }
        return value
    }

    private fun openConnection(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "LastHavenLauncher-Android")
        }

    private fun isValidVersion(version: String): Boolean = VERSION_PATTERN.matches(version)

    private fun versionCodeForVersion(version: String): Long {
        val parts = VERSION_PATTERN.matchEntire(version)?.groupValues?.drop(1)
            ?: error("Invalid release version: $version")
        val values = parts.map { it.toLong() }
        check(values.all { it <= 999L }) { "Release version components must be 999 or lower" }
        return values[0] * 1_000_000L + values[1] * 1_000L + values[2]
    }

    private fun compareVersions(first: String, second: String): Int {
        val a = VERSION_PATTERN.matchEntire(first)?.groupValues?.drop(1)
            ?: error("Invalid installed version: $second")
        val b = VERSION_PATTERN.matchEntire(second)?.groupValues?.drop(1)
            ?: error("Invalid release version: $first")

        for (index in 0..2) {
            val comparison = a[index].toLong().compareTo(b[index].toLong())
            if (comparison != 0) return comparison
        }
        return 0
    }

    private val VERSION_PATTERN = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)")
    private val SHA256_PATTERN = Regex("[a-fA-F0-9]{64}")
}

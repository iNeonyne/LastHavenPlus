package com.lasthavens.android

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

internal enum class UpdateStage { CHECKING, DOWNLOADING, VERIFYING }

internal data class UpdateProgress(
    val stage: UpdateStage,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
)

internal data class AvailableUpdate(
    val versionName: String, val versionCode: Long, val sizeBytes: Long,
    val releaseTag: String, val apkFile: File,
)

internal class UpdateManager(context: Context) {
    private val appContext = context.applicationContext

    fun checkAndDownload(
        onProgress: (UpdateProgress) -> Unit,
        callback: (Result<AvailableUpdate?>) -> Unit,
    ) {
        fun report(progress: UpdateProgress) { mainHandler.post { onProgress(progress) } }
        executor.execute {
            report(UpdateProgress(UpdateStage.CHECKING))
            val result = runCatching { checkAndDownloadBlocking(::report) }
            mainHandler.post { callback(result) }
        }
    }

    /** Prepares a verified APK for Android's installer without blocking the UI thread. */
    fun install(activity: Activity, update: AvailableUpdate, callback: (Result<Unit>) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            activity.startActivity(Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${appContext.packageName}"),
            ))
            callback(Result.failure(
                IllegalStateException("Allow LastHavenPlus to install updates, then check again."),
            ))
            return
        }
        executor.execute {
            val result = runCatching { installBlocking(update) }
            mainHandler.post { callback(result) }
        }
    }

    private fun installBlocking(update: AvailableUpdate) {
        val apk = update.apkFile
        check(apk.isFile && apk.length() == update.sizeBytes) {
            "The verified update file is missing or incomplete. Check for updates again."
        }
        verifyInstalledCompatibility(apk, update)
        val installer = appContext.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(appContext.packageName)
            setSize(apk.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
            }
        }
        val sessionId = installer.createSession(params)
        try {
            installer.openSession(sessionId).use { session ->
                FileInputStream(apk).use { input ->
                    session.openWrite("base.apk", 0, apk.length()).use { output ->
                        input.copyTo(output, DEFAULT_BUFFER_SIZE)
                        session.fsync(output)
                    }
                }
                val statusIntent = Intent(appContext, UpdateInstallStatusActivity::class.java)
                    .putExtra(UpdateInstallStatusActivity.EXTRA_VERSION, update.versionName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val status = PendingIntent.getActivity(
                    appContext, sessionId, statusIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0),
                )
                session.commit(status.intentSender)
            }
            // PackageInstaller now owns the staged session data; free the cached source APK.
            runCatching { apk.delete() }
        } catch (error: Exception) {
            runCatching { installer.abandonSession(sessionId) }
            throw error
        }
    }

    private fun checkAndDownloadBlocking(report: (UpdateProgress) -> Unit): AvailableUpdate? {
        check(UpdatePolicy.supportsDevice(Build.SUPPORTED_ABIS.toList())) {
            "LastHavenPlus updates are available for ARM64 (arm64-v8a) devices only."
        }
        val installed = installedPackageInfo()
        val release = latestPublishedRelease()
        val releaseTag = release.getString("tag_name")
        val versionName = releaseTag.removePrefix("v")
        check(UpdatePolicy.isPublishedStableRelease(releaseTag, release.optBoolean("draft", true), release.optBoolean("prerelease", true))) {
            "Only published stable GitHub Releases can provide updates."
        }
        check(UpdatePolicy.isStableVersionName(versionName)) { "The latest published release does not use stable SemVer." }

        val assets = release.optJSONArray("assets") ?: throw IOException("The GitHub Release has no assets.")
        val apkName = "LastHavenPlus-$versionName-arm64-v8a.apk"
        check(UpdatePolicy.isExpectedReleaseApkName(versionName, apkName)) { "The release APK filename is invalid." }
        val apkAssets = (0 until assets.length()).mapNotNull { assets.optJSONObject(it) }
            .filter { it.optString("name") == apkName && it.optString("state") == "uploaded" }
        val checksumAssets = (0 until assets.length()).mapNotNull { assets.optJSONObject(it) }
            .filter { it.optString("name") == "SHA256SUMS.txt" && it.optString("state") == "uploaded" }
        check(apkAssets.size == 1 && checksumAssets.size == 1) {
            "The latest GitHub Release must contain exactly one ARM64 APK and one SHA256SUMS.txt asset."
        }
        val apkAsset = apkAssets.single()
        val checksumAsset = checksumAssets.single()
        val apkSize = apkAsset.optLong("size")
        check(apkSize in 1..MAX_APK_BYTES) { "The release APK has an invalid size." }
        check(checksumAsset.optLong("size") in 1..MAX_CHECKSUM_BYTES.toLong()) {
            "The release checksum file has an invalid size."
        }

        val checksumFile = File(appContext.cacheDir, "release-checksums.txt")
        val apkPart = File(appContext.cacheDir, "release-update.apk.part")
        val readyApk = File(appContext.cacheDir, "release-update.apk")
        try {
            downloadReleaseAsset(checksumAsset, checksumFile, MAX_CHECKSUM_BYTES.toLong())
            val checksums = checksumFile.readText(Charsets.UTF_8)
            val checksumLines = checksums.lineSequence().filter { it.isNotBlank() }.toList()
            check(checksumLines.size == 1) { "The release checksum file must contain exactly one APK checksum." }
            val checksum = checksumLines.single().trim().split(Regex("\\s+"), limit = 2)
            check(checksum.size == 2 && checksum[1].removePrefix("*") == apkName &&
                UpdatePolicy.isValidSha256(checksum[0])) {
                "The release checksum file does not describe the expected ARM64 APK."
            }
            check(appContext.cacheDir.usableSpace >= apkSize + MIN_FREE_SPACE_BYTES) {
                "Not enough free storage to download and verify this update. Free up space and try again."
            }
            report(UpdateProgress(UpdateStage.DOWNLOADING, 0, apkSize))
            downloadReleaseAsset(apkAsset, apkPart, MAX_APK_BYTES) { downloaded, total ->
                report(UpdateProgress(UpdateStage.DOWNLOADING, downloaded, total))
            }
            report(UpdateProgress(UpdateStage.VERIFYING))
            check(UpdatePolicy.isCompleteAsset(apkSize, apkPart.length(), MAX_APK_BYTES) && sha256(apkPart).equals(checksum[0], ignoreCase = true)) {
                "The release APK is incomplete or failed SHA-256 verification. Installation was blocked."
            }

            val releaseMetadata = JSONObject()
                .put("versionName", versionName)
            val downloaded = archivePackageInfo(apkPart)
                ?: throw IOException("Android could not read the downloaded release APK.")
            releaseMetadata.put("versionCode", PackageInfoCompat.getLongVersionCode(downloaded))
            check(UpdatePolicy.isNewerVersion(
                PackageInfoCompat.getLongVersionCode(installed), releaseMetadata.getLong("versionCode"),
            )) { "The latest published GitHub Release is not newer than this installation." }
            verifyArchivePackage(apkPart, releaseMetadata, installed)

            if (readyApk.exists()) check(readyApk.delete()) { "Could not replace the previous verified update." }
            check(apkPart.renameTo(readyApk)) { "Could not finalize the verified release APK." }
            return AvailableUpdate(
                versionName, releaseMetadata.getLong("versionCode"), apkSize,
                release.getString("tag_name"), readyApk,
            )
        } finally {
            checksumFile.delete()
            apkPart.delete()
        }
    }

    private fun latestPublishedRelease(): JSONObject {
        val release = getJson("https://api.github.com/repos/${UpdatePolicy.REPOSITORY}/releases/latest")
        check(UpdatePolicy.isPublishedStableRelease(release.optString("tag_name"), release.optBoolean("draft", true), release.optBoolean("prerelease", true))) {
            "The latest GitHub Release is not a published stable release."
        }
        check(release.optString("html_url").startsWith("https://github.com/${UpdatePolicy.REPOSITORY}/releases/tag/")) {
            "The release does not belong to the official LastHavenPlus repository."
        }
        return release
    }

    private fun downloadReleaseAsset(
        asset: JSONObject,
        destination: File,
        maxBytes: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ) {
        val expectedSize = asset.optLong("size")
        val url = asset.optString("browser_download_url")
        check(asset.optString("state") == "uploaded" && expectedSize in 1..maxBytes) {
            "The GitHub Release asset is missing, incomplete, or exceeds its download limit."
        }
        val uri = Uri.parse(url)
        check(uri.scheme == "https" && uri.host == "github.com" &&
            uri.path?.startsWith("/${UpdatePolicy.REPOSITORY}/releases/download/") == true) {
            "The release asset URL is not a trusted GitHub Releases download."
        }
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = DOWNLOAD_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/octet-stream")
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("GitHub Release asset download failed (HTTP $status).")
            FileOutputStream(destination).use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        check(total <= expectedSize && total <= maxBytes) {
                            "Release asset exceeded its declared size or download limit."
                        }
                        output.write(buffer, 0, count)
                        onProgress(total, expectedSize)
                    }
                    output.fd.sync()
                }
            }
            check(UpdatePolicy.isCompleteAsset(expectedSize, destination.length(), maxBytes)) {
                "The GitHub Release asset download was incomplete."
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun verifyArchivePackage(apk: File, meta: JSONObject, installed: PackageInfo) {
        val downloaded = archivePackageInfo(apk) ?: throw IOException("Android could not read the downloaded APK.")
        check(downloaded.packageName == appContext.packageName) { "APK package ID does not match this app." }
        check(downloaded.versionName == meta.getString("versionName")) { "APK versionName does not match metadata." }
        check(PackageInfoCompat.getLongVersionCode(downloaded) == meta.getLong("versionCode")) {
            "APK versionCode does not match metadata."
        }
        check(UpdatePolicy.isNewerVersion(
            PackageInfoCompat.getLongVersionCode(installed), PackageInfoCompat.getLongVersionCode(downloaded),
        )) { "Downloaded APK is not newer than the installed app." }
        check(UpdatePolicy.sameSigningCertificates(signerDigests(installed), signerDigests(downloaded))) {
            "APK uses a different signing certificate. It was blocked to protect app data."
        }
    }

    private fun verifyInstalledCompatibility(apk: File, update: AvailableUpdate) {
        val current = installedPackageInfo()
        val downloaded = archivePackageInfo(apk) ?: throw IOException("Android could not read the verified update APK.")
        check(downloaded.packageName == appContext.packageName &&
            PackageInfoCompat.getLongVersionCode(downloaded) == update.versionCode) { "The staged APK metadata changed." }
        check(UpdatePolicy.isNewerVersion(
            PackageInfoCompat.getLongVersionCode(current), PackageInfoCompat.getLongVersionCode(downloaded),
        )) { "The staged APK is no longer newer than the installed version." }
        check(UpdatePolicy.sameSigningCertificates(signerDigests(current), signerDigests(downloaded))) {
            "The signing certificate no longer matches."
        }
    }

    private fun installedPackageInfo(): PackageInfo {
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else {
            @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
        }
        return packageInfo(appContext.packageName, flags)
    }

    @Suppress("DEPRECATION")
    private fun packageInfo(name: String, flags: Int): PackageInfo =
        if (Build.VERSION.SDK_INT >= 33) appContext.packageManager.getPackageInfo(
            name, PackageManager.PackageInfoFlags.of(flags.toLong()),
        ) else appContext.packageManager.getPackageInfo(name, flags)

    @Suppress("DEPRECATION")
    private fun archivePackageInfo(apk: File): PackageInfo? {
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        return if (Build.VERSION.SDK_INT >= 33) appContext.packageManager.getPackageArchiveInfo(
            apk.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong()),
        ) else appContext.packageManager.getPackageArchiveInfo(apk.absolutePath, flags)
    }

    private fun signerDigests(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else {
            @Suppress("DEPRECATION") info.signatures
        } ?: throw IOException("Could not read APK signing certificate.")
        check(signatures.isNotEmpty()) { "APK has no signing certificate." }
        return signatures.map { MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).toHex() }.toSet()
    }

    private fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"; connectTimeout = CONNECT_TIMEOUT_MS; readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            val status = connection.responseCode
            if (status == 404 && url.endsWith("/releases/latest")) {
                throw IOException("No published stable GitHub Release is available yet.")
            }
            if (status !in 200..299) throw IOException("GitHub update service returned HTTP $status.")
            return JSONObject(connection.inputStream.use { it.readBounded(MAX_JSON_BYTES).toString(Charsets.UTF_8) })
        } finally { connection.disconnect() }
    }

    private fun java.io.InputStream.readBounded(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            total += count
            check(total <= limit) { "Update service returned an oversized response." }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
        }
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    companion object {
        private const val USER_AGENT = "LastHavenPlus-Android-Updater"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 30_000
        private const val DOWNLOAD_TIMEOUT_MS = 120_000
        private const val MAX_JSON_BYTES = 2 * 1024 * 1024
        private const val MAX_CHECKSUM_BYTES = 64 * 1024
        private const val MAX_APK_BYTES = 400L * 1024 * 1024
        private const val MIN_FREE_SPACE_BYTES = 16L * 1024 * 1024
        private val executor = Executors.newSingleThreadExecutor()
        private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    }
}

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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

internal enum class UpdateStage { CHECKING, FETCHING_CHECKSUM, DOWNLOADING, VERIFYING }

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

    fun checkAvailableVersions(callback: (Result<List<ReleaseVersion>>) -> Unit) {
        executor.execute {
            val result = runCatching { checkAvailableVersionsBlocking() }
            mainHandler.post { callback(result) }
        }
    }

    /** Downloads and verifies only the release explicitly selected by the user. */
    fun downloadUpdate(
        release: ReleaseVersion,
        onProgress: (UpdateProgress) -> Unit,
        callback: (Result<AvailableUpdate>) -> Unit,
    ) {
        fun report(progress: UpdateProgress) { mainHandler.post { onProgress(progress) } }
        executor.execute {
            val result = runCatching { downloadUpdateBlocking(release, ::report) }
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

    private fun checkAvailableVersionsBlocking(): List<ReleaseVersion> {
        check(UpdatePolicy.supportsDevice(Build.SUPPORTED_ABIS.toList())) {
            "LastHavenPlus updates are available for ARM64 (arm64-v8a) devices only."
        }
        val installed = installedPackageInfo()
        val installedVersion = installed.versionName ?: BuildConfig.VERSION_NAME
        check(UpdatePolicy.isStableVersionName(installedVersion)) {
            "The installed app version cannot be compared with stable releases."
        }

        val releases = getJsonArray(
            "https://api.github.com/repos/${UpdatePolicy.REPOSITORY}/releases?per_page=100",
        )
        val candidates = mutableListOf<ReleaseVersion>()
        for (index in 0 until releases.length()) {
            val release = releases.optJSONObject(index) ?: continue
            val tag = release.optString("tag_name")
            if (!UpdatePolicy.isPublishedStableRelease(
                    tag, release.optBoolean("draft", true), release.optBoolean("prerelease", true),
                )) continue
            if (!UpdatePolicy.isTrustedReleasePage(release.optString("html_url"), tag)) continue
            val version = tag.removePrefix("v")
            val assetsJson = release.optJSONArray("assets") ?: continue
            val assets = (0 until assetsJson.length()).mapNotNull { assetsJson.optJSONObject(it) }.map {
                UpdateAsset(
                    name = it.optString("name"),
                    state = it.optString("state"),
                    sizeBytes = it.optLong("size"),
                    downloadUrl = it.optString("browser_download_url"),
                )
            }
            val selected = UpdatePolicy.selectReleaseAssets(version, assets) ?: continue
            if (selected.apk.sizeBytes !in 1..MAX_APK_BYTES ||
                selected.checksum.sizeBytes !in 1..MAX_CHECKSUM_BYTES.toLong() ||
                !UpdatePolicy.isTrustedReleaseAssetUrl(selected.apk.downloadUrl) ||
                !UpdatePolicy.isTrustedReleaseAssetUrl(selected.checksum.downloadUrl)) continue
            candidates += ReleaseVersion(
                versionName = version,
                releaseTag = tag,
                sizeBytes = selected.apk.sizeBytes,
                apkAsset = selected.apk,
                checksumAsset = selected.checksum,
            )
        }

        val current = ReleaseVersion(
            versionName = installedVersion,
            releaseTag = null,
            sizeBytes = 0,
            apkAsset = null,
            checksumAsset = null,
            isInstalled = true,
        )
        return listOf(current) + UpdatePolicy.newerStableReleases(installedVersion, candidates)
    }

    private fun downloadUpdateBlocking(
        release: ReleaseVersion,
        report: (UpdateProgress) -> Unit,
    ): AvailableUpdate {
        check(!release.isInstalled && release.releaseTag != null &&
            release.apkAsset != null && release.checksumAsset != null) {
            "Select a newer stable release before downloading."
        }
        check(UpdatePolicy.isStableVersionName(release.versionName) &&
            release.releaseTag == "v${release.versionName}") {
            "The selected release metadata is invalid."
        }
        check(UpdatePolicy.isTrustedReleaseAssetUrl(release.apkAsset.downloadUrl) &&
            UpdatePolicy.isTrustedReleaseAssetUrl(release.checksumAsset.downloadUrl)) {
            "The selected release contains an untrusted download URL."
        }
        check(UpdatePolicy.supportsDevice(Build.SUPPORTED_ABIS.toList())) {
            "LastHavenPlus updates are available for ARM64 (arm64-v8a) devices only."
        }

        val installed = installedPackageInfo()
        val installedVersion = installed.versionName ?: BuildConfig.VERSION_NAME
        check(UpdatePolicy.compareStableVersions(release.versionName, installedVersion)?.let { it > 0 } == true) {
            "The selected release is no longer newer than this installation."
        }
        val apkAsset = release.apkAsset
        val checksumAsset = release.checksumAsset
        val apkSize = apkAsset.sizeBytes
        check(apkSize in 1..MAX_APK_BYTES) { "The selected release APK has an invalid size." }
        check(checksumAsset.sizeBytes in 1..MAX_CHECKSUM_BYTES.toLong()) {
            "The selected release checksum file has an invalid size."
        }

        val checksumFile = File(appContext.cacheDir, "release-checksums.txt")
        val apkPart = File(appContext.cacheDir, "release-update.apk.part")
        val readyApk = File(appContext.cacheDir, "release-update.apk")
        try {
            report(UpdateProgress(UpdateStage.FETCHING_CHECKSUM))
            downloadReleaseAsset(checksumAsset, checksumFile, MAX_CHECKSUM_BYTES.toLong(), timeoutMs = CHECKSUM_TIMEOUT_MS)
            val checksums = checksumFile.readText(Charsets.UTF_8)
            val checksumLines = checksums.lineSequence().filter { it.isNotBlank() }.toList()
            check(checksumLines.size == 1) { "The release checksum file must contain exactly one APK checksum." }
            val checksum = checksumLines.single().trim().split(Regex("\\s+"), limit = 2)
            check(checksum.size == 2 && checksum[1].removePrefix("*") == apkAsset.name &&
                UpdatePolicy.isValidSha256(checksum[0])) {
                "The release checksum file does not describe the selected ARM64 APK."
            }
            check(appContext.cacheDir.usableSpace >= apkSize + MIN_FREE_SPACE_BYTES) {
                "Not enough free storage to download and verify this update. Free up space and try again."
            }
            report(UpdateProgress(UpdateStage.DOWNLOADING, 0, apkSize))
            downloadReleaseAsset(apkAsset, apkPart, MAX_APK_BYTES) { downloaded, total ->
                report(UpdateProgress(UpdateStage.DOWNLOADING, downloaded, total))
            }
            report(UpdateProgress(UpdateStage.VERIFYING))
            check(UpdatePolicy.isCompleteAsset(apkSize, apkPart.length(), MAX_APK_BYTES) &&
                sha256(apkPart).equals(checksum[0], ignoreCase = true)) {
                "The release APK is incomplete or failed SHA-256 verification. Installation was blocked."
            }

            val releaseMetadata = JSONObject().put("versionName", release.versionName)
            val downloaded = archivePackageInfo(apkPart)
                ?: throw IOException("Android could not read the downloaded release APK.")
            releaseMetadata.put("versionCode", PackageInfoCompat.getLongVersionCode(downloaded))
            check(UpdatePolicy.isNewerVersion(
                PackageInfoCompat.getLongVersionCode(installed), releaseMetadata.getLong("versionCode"),
            )) { "The selected release APK is not newer than this installation." }
            verifyArchivePackage(apkPart, releaseMetadata, installed)

            if (readyApk.exists()) check(readyApk.delete()) { "Could not replace the previous verified update." }
            check(apkPart.renameTo(readyApk)) { "Could not finalize the verified release APK." }
            return AvailableUpdate(
                release.versionName, releaseMetadata.getLong("versionCode"), apkSize,
                release.releaseTag, readyApk,
            )
        } finally {
            checksumFile.delete()
            apkPart.delete()
        }
    }

    private fun downloadReleaseAsset(
        asset: UpdateAsset,
        destination: File,
        maxBytes: Long,
        timeoutMs: Int = DOWNLOAD_TIMEOUT_MS,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ) {
        val expectedSize = asset.sizeBytes
        val url = asset.downloadUrl
        check(asset.state == "uploaded" && expectedSize in 1..maxBytes) {
            "The GitHub Release asset is missing, incomplete, or exceeds its download limit."
        }
        check(UpdatePolicy.isTrustedReleaseAssetUrl(url)) {
            "The release asset URL is not a trusted GitHub Releases download."
        }
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = timeoutMs
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/octet-stream")
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("GitHub Release asset download failed (HTTP $status).")
            check(UpdatePolicy.isTrustedReleaseAssetRedirect(connection.url.toString())) {
                "GitHub redirected the update download to an untrusted destination."
            }
            FileOutputStream(destination).use { output ->
                var lastProgressPercent = -1
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
                        val progressPercent = ((total * 100) / expectedSize).toInt()
                        if (progressPercent != lastProgressPercent) {
                            lastProgressPercent = progressPercent
                            onProgress(total, expectedSize)
                        }
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

    private fun getJsonArray(url: String): JSONArray {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("GitHub update service returned HTTP $status.")
            return JSONArray(connection.inputStream.use {
                it.readBounded(MAX_JSON_BYTES).toString(Charsets.UTF_8)
            })
        } finally {
            connection.disconnect()
        }
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
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
        private const val CHECKSUM_TIMEOUT_MS = 15_000
        private const val DOWNLOAD_TIMEOUT_MS = 120_000
        private const val MAX_JSON_BYTES = 2 * 1024 * 1024
        private const val MAX_CHECKSUM_BYTES = 64 * 1024
        private const val MAX_APK_BYTES = 400L * 1024 * 1024
        private const val MIN_FREE_SPACE_BYTES = 16L * 1024 * 1024
        private val executor = Executors.newSingleThreadExecutor()
        private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    }
}

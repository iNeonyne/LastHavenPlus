package com.neonyne.lasthavenlauncher

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class AppUpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String,
)

sealed class UpdateCheckResult {
    data object NoUpdate : UpdateCheckResult()
    data class Available(val update: AppUpdateInfo) : UpdateCheckResult()
    data class Failed(val message: String) : UpdateCheckResult()
}

/**
 * Checks the latest stable GitHub Release and downloads its ARM64 APK.
 *
 * Release asset contract: lasthavenlauncher-arm64.apk
 * Release tag contract: vMAJOR.MINOR.PATCH
 */
object AppUpdater {
    private const val RELEASE_API =
        "https://api.github.com/repos/iNeonyne/Lasthavens-Android/releases/latest"
    private const val APK_ASSET_NAME = "lasthavenlauncher-arm64.apk"
    private const val MIME_APK = "application/vnd.android.package-archive"
    private const val MAX_APK_BYTES = 500L * 1024L * 1024L

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun checkForUpdate(context: Context, callback: (UpdateCheckResult) -> Unit) {
        val appContext = context.applicationContext
        executor.execute {
            val result = try {
                val connection = (URL(RELEASE_API).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                    setRequestProperty("User-Agent", "LastHavenLauncher-Android")
                }

                try {
                    if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        UpdateCheckResult.NoUpdate
                    } else {
                        check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                            "GitHub returned HTTP ${connection.responseCode}"
                        }

                        val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                        val tag = release.getString("tag_name")
                        val latestVersion = tag.removePrefix("v")
                        val currentVersion = appContext.packageManager
                            .getPackageInfo(appContext.packageName, 0).versionName ?: "0.0.0"

                        check(isValidVersion(latestVersion)) {
                            "Release tag must use vMAJOR.MINOR.PATCH"
                        }

                        if (compareVersions(latestVersion, currentVersion) <= 0) {
                            UpdateCheckResult.NoUpdate
                        } else {
                            val assets = release.getJSONArray("assets")
                            val asset = (0 until assets.length())
                                .map { assets.getJSONObject(it) }
                                .firstOrNull { it.optString("name") == APK_ASSET_NAME }
                                ?: error("Release is missing $APK_ASSET_NAME")

                            val downloadUrl = asset.getString("browser_download_url")
                            check(downloadUrl.startsWith("https://github.com/")) {
                                "Release APK must be hosted on GitHub"
                            }

                            UpdateCheckResult.Available(
                                AppUpdateInfo(
                                    version = latestVersion,
                                    releaseNotes = release.optString("body").ifBlank {
                                        "A newer version of LastHavenLauncher is available."
                                    },
                                    downloadUrl = downloadUrl,
                                ),
                            )
                        }
                    }
                } finally {
                    connection.disconnect()
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
        callback: (Result<Uri>) -> Unit,
    ) {
        val appContext = context.applicationContext
        executor.execute {
            val result = runCatching {
                val connection = (URL(update.downloadUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "LastHavenLauncher-Android")
                }

                try {
                    check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                        "APK download failed (HTTP ${connection.responseCode})"
                    }
                    val expectedSize = connection.contentLengthLong
                    check(expectedSize <= MAX_APK_BYTES) { "APK is unexpectedly large" }

                    val updateDirectory = File(appContext.filesDir, "updates").apply { mkdirs() }
                    val temporaryFile = File(updateDirectory, "update.apk.part")
                    val apkFile = File(updateDirectory, "lasthavenlauncher-arm64.apk")

                    connection.inputStream.use { input ->
                        FileOutputStream(temporaryFile).use { output ->
                            input.copyTo(output, bufferSize = 64 * 1024)
                            output.fd.sync()
                        }
                    }

                    check(temporaryFile.length() > 1_000_000L) { "Downloaded APK is incomplete" }
                    check(temporaryFile.inputStream().use {
                        it.read() == 0x50 && it.read() == 0x4B
                    }) { "Downloaded file is not an APK archive" }

                    check(temporaryFile.renameTo(apkFile)) { "Could not finalize downloaded APK" }

                    FileProvider.getUriForFile(
                        appContext,
                        "${appContext.packageName}.updates",
                        apkFile,
                    )
                } finally {
                    connection.disconnect()
                }
            }

            mainHandler.post { callback(result) }
        }
    }

    private fun isValidVersion(version: String): Boolean =
        VERSION_PATTERN.matches(version)

    private fun compareVersions(first: String, second: String): Int {
        val a = VERSION_PATTERN.matchEntire(first)?.groupValues?.drop(1)
            ?: error("Invalid installed version: $first")
        val b = VERSION_PATTERN.matchEntire(second)?.groupValues?.drop(1)
            ?: error("Invalid release version: $second")

        for (index in 0..2) {
            val comparison = a[index].toLong().compareTo(b[index].toLong())
            if (comparison != 0) return comparison
        }
        return 0
    }

    private val VERSION_PATTERN = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)")
}

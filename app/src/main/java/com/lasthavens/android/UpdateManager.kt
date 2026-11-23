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
import java.util.zip.ZipFile

internal data class AvailableUpdate(
    val versionName: String, val versionCode: Long, val sizeBytes: Long,
    val commitSha: String, val runNumber: Long, val apkFile: File,
)

internal class UpdateManager(context: Context) {
    private val appContext = context.applicationContext

    fun checkAndDownload(callback: (Result<AvailableUpdate?>) -> Unit) {
        executor.execute {
            val result = runCatching { checkAndDownloadBlocking() }
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

    private fun checkAndDownloadBlocking(): AvailableUpdate? {
        val installed = installedPackageInfo()
        val run = latestSuccessfulMainRun()
        if (!UpdatePolicy.isNewerVersion(PackageInfoCompat.getLongVersionCode(installed), run.runNumber)) {
            return null
        }

        val metadataArtifact = runArtifact(run, UpdatePolicy.METADATA_ARTIFACT_NAME, MAX_METADATA_ARTIFACT_BYTES)
        val metadataZip = File(appContext.cacheDir, "main-update-metadata-${run.id}.zip")
        val metadataZipPart = File(appContext.cacheDir, "main-update-metadata-${run.id}.zip.part")
        val apkZip = File(appContext.cacheDir, "main-update-apk-${run.id}.zip")
        val apkZipPart = File(appContext.cacheDir, "main-update-apk-${run.id}.zip.part")
        val apkPart = File(appContext.cacheDir, "main-update.apk.part")
        val readyApk = File(appContext.cacheDir, "main-update.apk")

        try {
            downloadArtifact(metadataArtifact, metadataZipPart, MAX_METADATA_ARCHIVE_BYTES)
            verifyArtifactArchiveDigest(metadataArtifact, metadataZipPart)
            if (metadataZip.exists()) check(metadataZip.delete()) { "Could not replace the previous metadata archive." }
            check(metadataZipPart.renameTo(metadataZip)) { "Could not stage the update metadata archive." }

            val metadataAndChecksums = ZipFile(metadataZip).use { zip ->
                val entries = zip.entries().asSequence().filterNot { it.isDirectory }.toList()
                check(entries.size == 2 && entries.map { it.name }.toSet() == setOf("update.json", "SHA256SUMS.txt")) {
                    "The main metadata artifact must contain only update.json and SHA256SUMS.txt."
                }
                entries.forEach { entry ->
                    check(!entry.name.contains('/') && !entry.name.contains(92.toChar())) {
                        "The metadata artifact contains an unsafe path."
                    }
                }
                JSONObject(readZipText(zip, "update.json", MAX_METADATA_BYTES)) to
                    readZipText(zip, "SHA256SUMS.txt", MAX_CHECKSUM_BYTES)
            }

            val metadata = metadataAndChecksums.first
            val checksums = metadataAndChecksums.second
            validateMetadata(metadata, run)
            val variants = parseApkVariants(metadata)
            val selected = UpdatePolicy.selectApk(Build.SUPPORTED_ABIS.toList(), variants)
                ?: throw IOException("This release supports ARM64 (`arm64-v8a`) devices only.")
            check(UpdatePolicy.checksumFileMatches(checksums, selected.fileName, selected.sha256)) {
                "The APK checksum does not match the checksum list from this main build."
            }
            check(UpdatePolicy.isNewerVersion(
                PackageInfoCompat.getLongVersionCode(installed), metadata.getLong("versionCode"),
            )) { "The latest successful main build is not newer than this installation." }

            val apkArtifactName = "${UpdatePolicy.APK_ARTIFACT_PREFIX}${selected.abi}"
            val apkArtifact = runArtifact(run, apkArtifactName, MAX_APK_ARTIFACT_BYTES)
            // The compressed archive and extracted APK coexist briefly in the cache.
            val requiredFreeSpace = apkArtifact.optLong("size_in_bytes") + selected.sizeBytes + MIN_FREE_SPACE_BYTES
            check(requiredFreeSpace > 0 && appContext.cacheDir.usableSpace >= requiredFreeSpace) {
                "Not enough free storage to download and verify this update. Free up space and try again."
            }
            downloadArtifact(apkArtifact, apkZipPart, MAX_APK_ARCHIVE_BYTES)
            verifyArtifactArchiveDigest(apkArtifact, apkZipPart)
            if (apkZip.exists()) check(apkZip.delete()) { "Could not replace the previous APK archive." }
            check(apkZipPart.renameTo(apkZip)) { "Could not stage the selected APK archive." }

            ZipFile(apkZip).use { zip ->
                val entries = zip.entries().asSequence().filterNot { it.isDirectory }.toList()
                check(entries.size == 1 && entries.single().name == selected.fileName) {
                    "The selected ABI artifact does not contain exactly the expected APK."
                }
                val entry = entries.single()
                check(!entry.name.contains('/') && !entry.name.contains(92.toChar())) {
                    "The APK artifact contains an unsafe path."
                }
                check(entry.size == selected.sizeBytes) { "The APK size does not match its published metadata." }
                FileOutputStream(apkPart).use { output ->
                    zip.getInputStream(entry).use { input ->
                        val digest = MessageDigest.getInstance("SHA-256")
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var total = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            check(total <= selected.sizeBytes && total <= MAX_APK_BYTES) {
                                "APK exceeded its declared size."
                            }
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                        }
                        val actualDigest = digest.digest().toHex()
                        check(UpdatePolicy.isValidDownload(
                            selected.sizeBytes, total, MAX_APK_BYTES, selected.sha256,
                            actualDigest, completed = total == selected.sizeBytes,
                        )) {
                            "The APK is incomplete or failed SHA-256 verification. Installation was blocked."
                        }
                        output.fd.sync()
                    }
                }
            }

            verifyArchivePackage(apkPart, metadata, installed)
            if (readyApk.exists()) check(readyApk.delete()) { "Could not replace the previous verified update." }
            check(apkPart.renameTo(readyApk)) { "Could not finalize the verified APK." }
            return AvailableUpdate(
                metadata.getString("versionName"), metadata.getLong("versionCode"),
                selected.sizeBytes, run.commitSha, run.runNumber, readyApk,
            )
        } finally {
            metadataZipPart.delete()
            metadataZip.delete()
            apkZipPart.delete()
            apkZip.delete()
            apkPart.delete()
        }
    }

    private fun latestSuccessfulMainRun(): UpdateRun {
        val url = "https://api.github.com/repos/${UpdatePolicy.REPOSITORY}/actions/workflows/build.yml/runs" +
            "?branch=main&status=completed&event=push&per_page=100"
        val runs = getJson(url).optJSONArray("workflow_runs") ?: JSONArray()
        val candidates = (0 until runs.length()).mapNotNull { i ->
            val item = runs.optJSONObject(i) ?: return@mapNotNull null
            val repo = item.optJSONObject("head_repository")?.optString("full_name")?.takeIf { it.isNotBlank() }
                ?: item.optJSONObject("repository")?.optString("full_name").orEmpty()
            UpdateRun(
                item.optLong("id"), item.optLong("run_number"), item.optLong("workflow_id"),
                item.optString("head_branch"), item.optString("event"), item.optString("status"),
                item.optString("conclusion"), item.optString("path"), repo, item.optString("head_sha"),
            ).takeIf(UpdatePolicy::isEligibleRun)
        }
        return candidates.maxByOrNull { it.runNumber }
            ?: throw IOException("No successful completed main-branch update build was found.")
    }

    private fun runArtifact(
        run: UpdateRun,
        artifactName: String,
        maxSizeBytes: Long,
    ): JSONObject {
        val response = getJson(
            "https://api.github.com/repos/${UpdatePolicy.REPOSITORY}/actions/runs/${run.id}/artifacts?per_page=100",
        )
        val artifacts = response.optJSONArray("artifacts") ?: JSONArray()
        val matches = (0 until artifacts.length()).mapNotNull { artifacts.optJSONObject(it) }
            .filter { it.optString("name") == artifactName }
        check(matches.size == 1) { "The successful main build does not contain exactly one $artifactName artifact." }
        val artifact = matches.single()
        check(!artifact.optBoolean("expired", true)) { "The main update artifact $artifactName has expired." }
        check(artifact.optLong("id") > 0) { "The update artifact ID is invalid." }
        val runInfo = artifact.optJSONObject("workflow_run")
            ?: throw IOException("Artifact has no workflow-run provenance.")
        check(UpdatePolicy.isEligibleArtifact(
            artifact.optString("name"), artifactName, artifact.optBoolean("expired", true),
            runInfo.optLong("id"), runInfo.optString("head_branch"), runInfo.optString("head_sha"), run,
        )) { "Artifact $artifactName does not belong to the latest successful main commit." }
        check(artifact.optLong("size_in_bytes") in 1..maxSizeBytes) {
            "The main update artifact $artifactName is invalid or exceeds its download limit."
        }
        return artifact
    }

    private fun verifyArtifactArchiveDigest(artifact: JSONObject, archive: File) {
        val digest = artifact.optString("digest")
        if (digest.startsWith("sha256:", ignoreCase = true)) {
            val expected = digest.substringAfter(':')
            check(UpdatePolicy.isValidSha256(expected) && sha256(archive) == expected.lowercase()) {
                "The GitHub Actions artifact archive failed its SHA-256 integrity check."
            }
        }
    }

    private fun validateMetadata(meta: JSONObject, run: UpdateRun) {
        check(meta.optInt("schemaVersion") == 1) { "Update metadata format is unsupported." }
        check(meta.optString("packageName") == appContext.packageName) { "Update is for a different Android package." }
        check(UpdatePolicy.isStableVersionName(meta.optString("versionName"))) { "Main update metadata must use stable SemVer." }
        check(meta.optLong("versionCode") == run.runNumber) { "versionCode does not match the workflow run number." }
        check(meta.optString("branch") == "main" && meta.optString("ref") == "refs/heads/main") { "Update metadata is not from main." }
        check(meta.optString("commitSha") == run.commitSha && meta.optLong("workflowRunId") == run.id &&
            meta.optLong("workflowRunNumber") == run.runNumber && meta.optLong("workflowId") == run.workflowId) {
            "Update metadata does not match the successful main workflow run."
        }
    }

    private fun parseApkVariants(meta: JSONObject): List<UpdateApk> {
        val version = meta.getString("versionName")
        val apks = meta.optJSONArray("apks") ?: throw IOException("Update metadata has no APK list.")
        check(apks.length() == 1) { "Main build must publish exactly one ARM64 APK." }
        val variants = (0 until apks.length()).map { i ->
            val item = apks.getJSONObject(i)
            val abi = item.getString("abi")
            val name = item.getString("file")
            val hash = item.getString("sha256")
            val size = item.getLong("sizeBytes")
            check(UpdatePolicy.isExpectedApkName(version, abi, name)) { "Unexpected APK filename or ABI in metadata." }
            check(UpdatePolicy.isValidSha256(hash)) { "Invalid SHA-256 digest in metadata." }
            check(size in 1..MAX_APK_BYTES) { "Invalid APK size in metadata." }
            UpdateApk(abi, name, hash, size)
        }
        check(variants.map { it.abi }.toSet() == UpdatePolicy.supportedAbis) { "The ARM64 APK variant is missing or unsupported variants were published." }
        return variants
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
            if (status !in 200..299) throw IOException("GitHub update service returned HTTP $status.")
            return JSONObject(connection.inputStream.use { it.readBounded(MAX_JSON_BYTES).toString(Charsets.UTF_8) })
        } finally { connection.disconnect() }
    }

    private fun downloadArtifact(artifact: JSONObject, destination: File, maxBytes: Long) {
        val artifactId = artifact.optLong("id")
        check(artifactId > 0) { "The update artifact ID is invalid." }
        val expectedSize = artifact.optLong("size_in_bytes")
        check(expectedSize in 1..maxBytes) { "The update artifact size is invalid." }
        val url = "https://api.github.com/repos/${UpdatePolicy.REPOSITORY}/actions/artifacts/$artifactId/zip"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = DOWNLOAD_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("Main update artifact download failed (HTTP $status).")
            FileOutputStream(destination).use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        check(total <= expectedSize && total <= maxBytes) {
                            "Update artifact exceeded its declared size or download limit."
                        }
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }
            check(UpdatePolicy.isCompleteArtifact(expectedSize, destination.length(), maxBytes)) {
                "The update artifact download was incomplete."
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun readZipText(zip: ZipFile, name: String, limit: Int): String {
        val entry = zip.getEntry(name) ?: throw IOException("The artifact is missing $name.")
        check(!entry.isDirectory && entry.size >= 0 && entry.size <= limit.toLong()) { "$name has an invalid size." }
        return zip.getInputStream(entry).use { it.readBounded(limit).toString(Charsets.UTF_8) }
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
        private const val MAX_METADATA_BYTES = 64 * 1024
        private const val MAX_CHECKSUM_BYTES = 64 * 1024
        private const val MAX_APK_BYTES = 400L * 1024 * 1024
        private const val MAX_METADATA_ARTIFACT_BYTES = 2L * 1024 * 1024
        private const val MAX_APK_ARTIFACT_BYTES = MAX_APK_BYTES + 5L * 1024 * 1024
        private const val MAX_METADATA_ARCHIVE_BYTES = MAX_METADATA_ARTIFACT_BYTES + 1024L * 1024
        private const val MAX_APK_ARCHIVE_BYTES = MAX_APK_ARTIFACT_BYTES + 1024L * 1024
        private const val MIN_FREE_SPACE_BYTES = 16L * 1024 * 1024
        private val executor = Executors.newSingleThreadExecutor()
        private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    }
}

package com.neonyne.lasthavenlauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

data class AddonLibraryItem(
    val id: String,
    val archiveSha256: String,
    val archiveName: String,
    val archiveBytes: Long,
    val expandedBytes: Long,
    val rootPath: String,
    val tocPath: String,
    val folderName: String,
    val title: String,
    val notes: String?,
    val version: String?,
    val interfaceVersions: List<Int>,
    val dependencies: List<String>,
    val optionalDependencies: List<String>,
    val enabled: Boolean,
    val importedAt: Long,
)

sealed interface AddonImportResult {
    data class Imported(val added: Int, val replaced: Int) : AddonImportResult
    data class RequiresConfirmation(val token: String, val conflictingFolders: List<String>) : AddonImportResult
}

/**
 * Stores opaque addon ZIP packages and parsed metadata outside Cefrium's browser profile.
 * No addon files are extracted and no game runtime integration is performed here.
 */
class AddonLibraryRepository(context: Context) {
    private val root = File(context.filesDir, "addon-library")
    private val packages = File(root, "packages")
    private val staging = File(root, "staging")
    private val index = File(root, "library.json")

    init {
        packages.mkdirs()
        staging.mkdirs()
    }

    @Synchronized
    fun list(): List<AddonLibraryItem> = readIndex()

    @Synchronized
    fun importArchive(input: InputStream, displayName: String): AddonImportResult {
        val token = UUID.randomUUID().toString()
        val stagedFile = File(staging, "$token.zip")
        try {
            copyBounded(input, stagedFile, AddonArchiveInspector.MAX_ARCHIVE_BYTES)
            val inspection = AddonArchiveInspector.inspect(stagedFile, safeDisplayName(displayName))
            val existing = readIndex()
            val folderNames = inspection.addons.map { it.folderName.lowercase() }.toSet()
            val conflicts = existing
                .filter { it.folderName.lowercase() in folderNames }
                .map { it.folderName }
                .distinct()
            if (conflicts.isNotEmpty()) {
                return AddonImportResult.RequiresConfirmation(token, conflicts)
            }
            return commit(stagedFile, inspection, existing, replaceExisting = false)
        } catch (error: Exception) {
            stagedFile.delete()
            throw error
        } finally {
            input.close()
        }
    }

    @Synchronized
    fun confirmReplacement(token: String, archiveName: String): AddonImportResult.Imported {
        val stagedFile = stagedFile(token)
        if (!stagedFile.isFile) throw IllegalStateException("The pending addon import has expired.")
        try {
            val inspection = AddonArchiveInspector.inspect(stagedFile, safeDisplayName(archiveName))
            return commit(stagedFile, inspection, readIndex(), replaceExisting = true)
        } finally {
            stagedFile.delete()
        }
    }

    @Synchronized
    fun cancelPendingImport(token: String) {
        stagedFile(token).delete()
    }

    @Synchronized
    fun setEnabled(itemId: String, enabled: Boolean) {
        val items = readIndex()
        val updated = items.map { if (it.id == itemId) it.copy(enabled = enabled) else it }
        if (updated.none { it.id == itemId }) return
        writeIndex(updated)
    }

    @Synchronized
    fun remove(itemId: String) {
        val items = readIndex()
        val updated = items.filterNot { it.id == itemId }
        if (updated.size == items.size) return
        writeIndex(updated)
        deleteUnreferencedPackages(updated)
    }

    @Synchronized
    fun export(itemId: String, output: OutputStream) {
        val item = readIndex().firstOrNull { it.id == itemId }
            ?: throw IllegalArgumentException("Addon is no longer in the library.")
        val archive = packageFile(item.archiveSha256)
        if (!archive.isFile) throw IllegalStateException("The stored addon package is missing.")
        archive.inputStream().buffered().use { input ->
            output.buffered().use { destination -> input.copyTo(destination) }
        }
    }

    private fun commit(
        stagedFile: File,
        inspection: AddonArchiveInspection,
        existing: List<AddonLibraryItem>,
        replaceExisting: Boolean,
    ): AddonImportResult.Imported {
        val incomingFolders = inspection.addons.map { it.folderName.lowercase() }.toSet()
        val conflicts = existing.filter { it.folderName.lowercase() in incomingFolders }
        if (conflicts.isNotEmpty() && !replaceExisting) {
            return AddonImportResult.Imported(0, 0)
        }

        val finalArchive = packageFile(inspection.sha256)
        val archiveAlreadyExists = finalArchive.isFile
        if (!archiveAlreadyExists && !stagedFile.renameTo(finalArchive)) {
            stagedFile.copyTo(finalArchive, overwrite = false)
            if (!stagedFile.delete()) finalArchive.delete()
        }

        val retained = if (replaceExisting) {
            existing.filterNot { it.folderName.lowercase() in incomingFolders }
        } else {
            existing
        }
        val importedAt = System.currentTimeMillis()
        val newItems = inspection.addons.map { addon ->
            val id = "${inspection.sha256}:${addon.rootPath}"
            AddonLibraryItem(
                id = id,
                archiveSha256 = inspection.sha256,
                archiveName = inspection.archiveName,
                archiveBytes = inspection.archiveBytes,
                expandedBytes = inspection.expandedBytes,
                rootPath = addon.rootPath,
                tocPath = addon.tocPath,
                folderName = addon.folderName,
                title = addon.title,
                notes = addon.notes,
                version = addon.version,
                interfaceVersions = addon.interfaceVersions,
                dependencies = addon.dependencies,
                optionalDependencies = addon.optionalDependencies,
                enabled = false,
                importedAt = importedAt,
            )
        }
        val updated = retained + newItems
        val uniquePackages = updated.distinctBy { it.archiveSha256 }
        val libraryBytes = uniquePackages.sumOf { it.archiveBytes }
        if (libraryBytes > MAX_LIBRARY_BYTES) {
            if (!archiveAlreadyExists && !existing.any { it.archiveSha256 == inspection.sha256 }) finalArchive.delete()
            throw AddonArchiveException("The local addon library is limited to 1 GiB. Remove older packages first.")
        }
        try {
            writeIndex(updated)
        } catch (error: Exception) {
            if (!archiveAlreadyExists && !existing.any { it.archiveSha256 == inspection.sha256 }) {
                finalArchive.delete()
            }
            throw error
        }
        deleteUnreferencedPackages(updated)
        stagedFile.delete()
        return AddonImportResult.Imported(newItems.size, conflicts.size)
    }

    private fun stagedFile(token: String): File {
        require(token.matches(Regex("^[0-9a-fA-F-]{36}$"))) { "Invalid pending import token." }
        return File(staging, "$token.zip")
    }

    private fun packageFile(sha256: String): File {
        require(sha256.matches(Regex("^[0-9a-f]{64}$"))) { "Invalid package fingerprint." }
        return File(packages, "$sha256.zip")
    }

    private fun copyBounded(input: InputStream, destination: File, maximumBytes: Long) {
        var copied = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        FileOutputStream(destination).use { output ->
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                copied += count
                if (copied > maximumBytes) {
                    throw AddonArchiveException("ZIP is larger than the 128 MiB import limit.")
                }
                output.write(buffer, 0, count)
            }
            output.fd.sync()
        }
        if (copied == 0L) throw AddonArchiveException("The selected file is empty.")
    }

    private fun safeDisplayName(value: String): String =
        value.substringAfterLast('/').substringAfterLast('\\').take(128).ifBlank { "addon.zip" }

    private fun readIndex(): List<AddonLibraryItem> {
        val backup = File(root, "library.json.bak")
        if (!index.exists() && backup.exists()) backup.renameTo(index)
        if (index.exists()) backup.delete()
        if (!index.isFile) return emptyList()
        val array = JSONArray(index.readText(Charsets.UTF_8))
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    AddonLibraryItem(
                        id = item.getString("id"),
                        archiveSha256 = item.getString("archiveSha256"),
                        archiveName = item.getString("archiveName"),
                        archiveBytes = item.getLong("archiveBytes"),
                        expandedBytes = item.getLong("expandedBytes"),
                        rootPath = item.getString("rootPath"),
                        tocPath = item.getString("tocPath"),
                        folderName = item.getString("folderName"),
                        title = item.getString("title"),
                        notes = item.optString("notes").takeIf(String::isNotBlank),
                        version = item.optString("version").takeIf(String::isNotBlank),
                        interfaceVersions = item.getJSONArray("interfaceVersions").toIntList(),
                        dependencies = item.getJSONArray("dependencies").toStringList(),
                        optionalDependencies = item.getJSONArray("optionalDependencies").toStringList(),
                        enabled = item.optBoolean("enabled", false),
                        importedAt = item.getLong("importedAt"),
                    ),
                )
            }
        }
    }

    private fun writeIndex(items: List<AddonLibraryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("archiveSha256", item.archiveSha256)
                    .put("archiveName", item.archiveName)
                    .put("archiveBytes", item.archiveBytes)
                    .put("expandedBytes", item.expandedBytes)
                    .put("rootPath", item.rootPath)
                    .put("tocPath", item.tocPath)
                    .put("folderName", item.folderName)
                    .put("title", item.title)
                    .put("notes", item.notes ?: "")
                    .put("version", item.version ?: "")
                    .put("interfaceVersions", JSONArray(item.interfaceVersions))
                    .put("dependencies", JSONArray(item.dependencies))
                    .put("optionalDependencies", JSONArray(item.optionalDependencies))
                    .put("enabled", item.enabled)
                    .put("importedAt", item.importedAt),
            )
        }

        val temporary = File(root, "library.json.tmp")
        val backup = File(root, "library.json.bak")
        FileOutputStream(temporary).use { output ->
            output.write(array.toString().toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        if (backup.exists()) backup.delete()
        if (index.exists() && !index.renameTo(backup)) {
            temporary.delete()
            throw IllegalStateException("Could not prepare the addon library index replacement.")
        }
        if (!temporary.renameTo(index)) {
            if (backup.exists()) backup.renameTo(index)
            temporary.delete()
            throw IllegalStateException("Could not save the addon library index; the previous index was restored.")
        }
        backup.delete()
    }

    companion object {
        const val MAX_LIBRARY_BYTES = 1024L * 1024L * 1024L
    }

    private fun deleteUnreferencedPackages(items: List<AddonLibraryItem>) {
        val retained = items.map { it.archiveSha256 }.toSet()
        packages.listFiles()?.forEach { file ->
            if (file.name.endsWith(".zip") && file.nameWithoutExtension !in retained) {
                file.delete()
            }
        }
    }

    private fun JSONArray.toIntList(): List<Int> =
        (0 until length()).map { getInt(it) }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }
}

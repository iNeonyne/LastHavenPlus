package com.lasthavens.android

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipInputStream

/**
 * Validates and stages a manager migration archive without touching the active add-on store.
 *
 * The caller owns activation/rollback. A returned stage is only a verified candidate; it must
 * not be treated as the active library until a later repository commit succeeds.
 */
object MigrationArchiveReader {
    const val FORMAT = "lasthavens-addon-manager-migration"
    const val SCHEMA = 1

    const val MAX_ARCHIVE_BYTES = 128L * 1024 * 1024
    const val MAX_EXPANDED_BYTES = 384L * 1024 * 1024
    const val MAX_FILE_BYTES = 64L * 1024 * 1024
    const val MAX_MANIFEST_BYTES = 8L * 1024 * 1024
    const val MAX_FILES = 32_000

    data class Stage(
        val directory: File,
        val manifest: JSONObject,
        val fileCount: Int,
        val expandedBytes: Long,
    )

    private data class FileRecord(
        val addonFolder: String,
        val storageFolder: String,
        val path: String,
        val archivePath: String,
        val size: Long,
        val sha256: String,
    )

    /**
     * Reads a ZIP from [input] and writes only verified files under [stagingRoot].
     * [archiveBytes] must be the original compressed file size, not an estimate.
     */
    fun stage(input: InputStream, archiveBytes: Long, stagingRoot: File): Stage {
        require(archiveBytes in 1..MAX_ARCHIVE_BYTES) { "Migration archive size is invalid or exceeds the limit" }
        require(stagingRoot.exists() || stagingRoot.mkdirs()) { "Could not create migration staging directory" }

        val root = stagingRoot.canonicalFile
        val stageDir = File(root, "migration-${UUID.randomUUID()}").canonicalFile
        require(stageDir.path.startsWith(root.path + File.separator)) { "Invalid staging destination" }
        require(stageDir.mkdir()) { "Could not create migration staging generation" }

        try {
            ZipInputStream(input).use { zip ->
                val first = zip.nextEntry ?: error("Migration archive is empty")
                require(!first.isDirectory && first.name == "manifest.json") {
                    "Migration manifest must be the first archive entry"
                }
                val manifestBytes = readEntryBytes(zip, MAX_MANIFEST_BYTES)
                zip.closeEntry()
                val manifest = JSONObject(String(manifestBytes, Charsets.UTF_8))
                val records = validateManifest(manifest)
                val expected = records.associateBy { it.archivePath }
                val seen = HashSet<String>()
                val seenFolded = HashSet<String>()
                val perAddonBytes = HashMap<String, Long>()
                val perAddonFiles = HashMap<String, Int>()
                val tocFolders = HashSet<String>()
                var expanded = 0L
                var count = 0

                var entry = zip.nextEntry
                while (entry != null) {
                    require(!entry.isDirectory) { "Directory entries are not allowed in migration archives" }
                    val name = entry.name
                    require(isSafeArchivePath(name)) { "Unsafe migration archive path" }
                    require(name != "manifest.json") { "Duplicate migration manifest" }
                    require(seen.add(name) && seenFolded.add(name.lowercase())) {
                        "Duplicate migration archive entry"
                    }
                    val record = expected[name] ?: error("Unexpected file in migration archive")
                    require(entry.size < 0 || entry.size == record.size) { "Declared file size does not match manifest" }

                    val target = File(stageDir, name).canonicalFile
                    require(target.path.startsWith(stageDir.path + File.separator)) { "Migration file escaped staging directory" }
                    require(target.parentFile?.let { it.exists() || it.mkdirs() } == true) {
                        "Could not create migration file directory"
                    }

                    val digest = MessageDigest.getInstance("SHA-256")
                    var fileBytes = 0L
                    FileOutputStream(target).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = zip.read(buffer)
                            if (read < 0) break
                            fileBytes += read
                            expanded += read
                            require(fileBytes <= MAX_FILE_BYTES) { "Migration file exceeds the per-file limit" }
                            require(expanded <= MAX_EXPANDED_BYTES) { "Migration exceeds the expanded-size limit" }
                            output.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                        }
                        output.flush()
                        output.fd.sync()
                    }
                    require(fileBytes == record.size) { "Migration file size does not match manifest" }
                    require(digest.digest().toHex() == record.sha256) { "Migration file checksum mismatch" }

                    count++
                    perAddonBytes[record.storageFolder] = (perAddonBytes[record.storageFolder] ?: 0L) + fileBytes
                    perAddonFiles[record.storageFolder] = (perAddonFiles[record.storageFolder] ?: 0) + 1
                    if (record.path.equals("${record.addonFolder}.toc", ignoreCase = true)) {
                        tocFolders.add(record.storageFolder.lowercase())
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }

                require(count == records.size && seen.size == expected.size) {
                    "Migration archive is missing one or more add-on files"
                }
                validateAddonTotals(manifest.getJSONObject("library").getJSONArray("addons"), perAddonBytes, perAddonFiles, tocFolders)
                val totals = manifest.getJSONObject("totals")
                require(totals.getInt("addons") == manifest.getJSONObject("library").getJSONArray("addons").length()) {
                    "Migration add-on total does not match library metadata"
                }
                require(totals.getInt("files") == count && totals.getLong("expandedBytes") == expanded) {
                    "Migration file totals do not match extracted data"
                }

                FileOutputStream(File(stageDir, "manifest.json")).use { output ->
                    output.write(manifestBytes)
                    output.flush()
                    output.fd.sync()
                }
                return Stage(stageDir, manifest, count, expanded)
            }
        } catch (error: Throwable) {
            stageDir.deleteRecursively()
            throw error
        }
    }

    private fun validateManifest(manifest: JSONObject): List<FileRecord> {
        require(manifest.getString("format") == FORMAT) { "Unsupported migration archive format" }
        require(manifest.getInt("schema") == SCHEMA) { "Unsupported migration schema version" }
        require(manifest.getJSONObject("library").has("revision")) { "Library revision is missing" }
        require(manifest.has("preferences") && manifest.get("preferences") is JSONObject) {
            "Manager preferences are missing"
        }

        val addons = manifest.getJSONObject("library").getJSONArray("addons")
        val addonByStorage = HashMap<String, String>()
        val folders = HashSet<String>()
        for (index in 0 until addons.length()) {
            val addon = addons.getJSONObject(index)
            val folder = addon.getString("folder")
            val storage = addon.optString("storageFolder", folder)
            require(isSafeFolder(folder) && isSafeFolder(storage)) { "Invalid add-on storage identity" }
            require(folders.add(folder.lowercase())) { "Duplicate add-on folder in library metadata" }
            require(addonByStorage.put(storage.lowercase(), folder) == null) {
                "Duplicate storage folder in library metadata"
            }
        }

        val fileArray = manifest.getJSONArray("files")
        require(fileArray.length() <= MAX_FILES) { "Migration file count exceeds the limit" }
        val records = ArrayList<FileRecord>(fileArray.length())
        val paths = HashSet<String>()
        var declaredTotal = 0L
        for (index in 0 until fileArray.length()) {
            val item = fileArray.getJSONObject(index)
            val addonFolder = item.getString("addonFolder")
            val storageFolder = item.getString("storageFolder")
            val path = item.getString("path")
            val archivePath = item.getString("archivePath")
            val size = item.getLong("size")
            val checksum = item.getString("sha256").lowercase()
            require(addonByStorage[storageFolder.lowercase()]?.equals(addonFolder, ignoreCase = true) == true) {
                "Migration file refers to an unknown add-on"
            }
            require(isSafeFolder(storageFolder) && isSafeRelativePath(path)) { "Unsafe add-on file path" }
            require(archivePath == "files/$storageFolder/$path" && isSafeArchivePath(archivePath)) {
                "Migration archive path does not match its metadata"
            }
            require(size in 0..MAX_FILE_BYTES) { "Migration file size is invalid" }
            require(checksum.matches(Regex("[a-f0-9]{64}"))) { "Migration file checksum is invalid" }
            require(paths.add(archivePath.lowercase())) { "Duplicate migration file metadata" }
            declaredTotal += size
            require(declaredTotal <= MAX_EXPANDED_BYTES) { "Migration declared size exceeds the limit" }
            records += FileRecord(addonFolder, storageFolder, path, archivePath, size, checksum)
        }
        require(manifest.getJSONObject("totals").getLong("expandedBytes") == declaredTotal) {
            "Declared expanded size does not match file records"
        }
        require(manifest.getJSONObject("totals").getInt("files") == records.size) {
            "Declared file count does not match file records"
        }
        return records
    }

    private fun validateAddonTotals(
        addons: JSONArray,
        byteTotals: Map<String, Long>,
        fileTotals: Map<String, Int>,
        tocFolders: Set<String>,
    ) {
        for (index in 0 until addons.length()) {
            val addon = addons.getJSONObject(index)
            val folder = addon.getString("folder")
            val storage = addon.optString("storageFolder", folder)
            val key = storage.lowercase()
            val bytes = byteTotals[key] ?: 0L
            val files = fileTotals[key] ?: 0
            require(files > 0 && key in tocFolders) { "Add-on files or TOC are missing for $folder" }
            if (addon.has("size")) require(addon.getLong("size") == bytes) {
                "Add-on byte total does not match library metadata for $folder"
            }
            if (addon.has("fileCount")) require(addon.getInt("fileCount") == files) {
                "Add-on file count does not match library metadata for $folder"
            }
        }
    }

    private fun readEntryBytes(zip: ZipInputStream, limit: Long): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = zip.read(buffer)
            if (read < 0) break
            total += read
            require(total <= limit) { "Migration manifest exceeds the size limit" }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun isSafeFolder(value: String): Boolean =
        value.length in 1..120 && value.matches(Regex("[A-Za-z0-9_-]+"))

    private fun isSafeRelativePath(value: String): Boolean =
        value.isNotEmpty() && value.length <= 1024 && !value.startsWith("/") &&
            '\\' !in value && !Regex("[\\x00-\\x1f:]").containsMatchIn(value) &&
            value.split('/').all { it.isNotEmpty() && it != "." && it != ".." }

    private fun isSafeArchivePath(value: String): Boolean =
        isSafeRelativePath(value) && value.startsWith("files/")

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

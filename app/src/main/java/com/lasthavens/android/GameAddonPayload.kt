package com.lasthavens.android

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile

/**
 * Read-only projection of an activated migration generation for the remote game.
 * File bytes are served in small ArrayBuffer chunks; no JavaScript interface can mutate storage.
 */
object GameAddonPayload {
    const val CHUNK_BYTES = 512 * 1024
    const val MAX_ENABLED_BYTES = 64L * 1024 * 1024
    const val MAX_ENABLED_FILES = 12_000

    data class FileRef(
        val storageFolder: String,
        val path: String,
        val archivePath: String,
        val size: Long,
    )

    data class Result(
        val payload: JSONObject,
        val files: Map<String, FileRef>,
        val totalBytes: Long,
    )

    fun build(manifest: JSONObject): Result {
        val library = manifest.getJSONObject("library")
        val addons = library.getJSONArray("addons")
        val records = manifest.getJSONArray("files")
        val recordsByStorage = HashMap<String, MutableList<FileRef>>()
        for (index in 0 until records.length()) {
            val item = records.getJSONObject(index)
            val storage = item.getString("storageFolder")
            val path = item.getString("path")
            val archivePath = item.getString("archivePath")
            val size = item.getLong("size")
            require(isSafeFolder(storage) && isSafePath(path)) { "Active add-on file path is invalid" }
            require(archivePath == "files/$storage/$path") { "Active add-on archive path is invalid" }
            require(size in 0..MigrationArchiveReader.MAX_FILE_BYTES) { "Active add-on file size is invalid" }
            recordsByStorage.getOrPut(storage.lowercase()) { mutableListOf() }
                .add(FileRef(storage, path, archivePath, size))
        }

        val enabled = ArrayList<JSONObject>()
        val refs = LinkedHashMap<String, FileRef>()
        var totalBytes = 0L
        var totalFiles = 0
        for (index in 0 until addons.length()) {
            val addon = addons.getJSONObject(index)
            if (!addon.optBoolean("enabled", false)) continue
            val folder = addon.getString("folder")
            val storage = addon.optString("storageFolder", folder)
            require(isSafeFolder(folder) && isSafeFolder(storage)) { "Active add-on identity is invalid" }
            val addonFiles = recordsByStorage[storage.lowercase()].orEmpty()
                .sortedBy { it.path.lowercase() }
            require(addonFiles.isNotEmpty()) { "No files found for enabled add-on $folder" }
            var addonBytes = 0L
            val fileList = JSONArray()
            for (file in addonFiles) {
                val key = "${storage.lowercase()}/${file.path.lowercase()}"
                require(refs.put(key, file) == null) { "Duplicate active add-on file path" }
                addonBytes += file.size
                totalBytes += file.size
                totalFiles++
                fileList.put(JSONObject().put("path", file.path).put("size", file.size))
            }
            require(addonBytes == addon.optLong("size", addonBytes)) {
                "Stored file size does not match enabled add-on $folder"
            }
            require(addonFiles.size == addon.optInt("fileCount", addonFiles.size)) {
                "Stored file count does not match enabled add-on $folder"
            }
            enabled += JSONObject()
                .put("folder", folder)
                .put("storageFolder", storage)
                .put("files", fileList)
        }
        require(totalFiles <= MAX_ENABLED_FILES) { "Enabled add-on file count exceeds the game bridge limit" }
        require(totalBytes <= MAX_ENABLED_BYTES) { "Enabled add-ons exceed the 64 MB game bridge limit" }

        val settings = manifest.optJSONObject("preferences")?.optJSONObject("settings")
        val payload = JSONObject()
            .put("revision", library.optString("revision", "unknown"))
            .put("badge", settings?.optBoolean("badge", true) ?: true)
            .put("addons", JSONArray(enabled))
        return Result(payload, refs, totalBytes)
    }

    fun readChunk(generation: File, file: FileRef, offset: Long, requestedLength: Int): ByteArray {
        require(offset >= 0 && offset < file.size) { "Requested add-on file offset is invalid" }
        require(requestedLength in 1..CHUNK_BYTES) { "Requested add-on chunk size is invalid" }
        val length = minOf(requestedLength.toLong(), file.size - offset).toInt()
        val root = generation.canonicalFile
        val target = File(root, file.archivePath).canonicalFile
        require(target.path.startsWith(root.path + File.separator) && target.isFile) {
            "Active add-on file is missing or outside its generation"
        }
        require(target.length() == file.size) { "Active add-on file size changed" }
        val bytes = ByteArray(length)
        RandomAccessFile(target, "r").use { input ->
            input.seek(offset)
            input.readFully(bytes)
        }
        return bytes
    }

    private fun isSafeFolder(value: String): Boolean =
        value.matches(Regex("[A-Za-z0-9_-]{1,120}"))

    private fun isSafePath(value: String): Boolean =
        value.isNotBlank() && value.length <= 1024 && !value.contains('\\') &&
            !value.startsWith("/") && !Regex("[\\x00-\\x1f:]").containsMatchIn(value) &&
            value.split('/').all { it.isNotEmpty() && it != "." && it != ".." }
}

package com.neonyne.lasthavenlauncher

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Locale
import java.util.zip.CRC32
import java.security.MessageDigest

data class AddonTocMetadata(
    val rootPath: String,
    val tocPath: String,
    val folderName: String,
    val title: String,
    val notes: String?,
    val version: String?,
    val interfaceVersions: List<Int>,
    val dependencies: List<String>,
    val optionalDependencies: List<String>,
)

data class AddonArchiveInspection(
    val archiveName: String,
    val archiveBytes: Long,
    val sha256: String,
    val expandedBytes: Long,
    val entryCount: Int,
    val addons: List<AddonTocMetadata>,
)

class AddonArchiveException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

/**
 * Inspects a ZIP without extracting it. The original archive is retained as an opaque package;
 * no archive-controlled path is ever used to create a filesystem path.
 */
object AddonArchiveInspector {
    const val MAX_ARCHIVE_BYTES = 128L * 1024L * 1024L
    const val MAX_ENTRY_BYTES = 64L * 1024L * 1024L
    const val MAX_EXPANDED_BYTES = 512L * 1024L * 1024L
    const val MAX_ENTRIES = 10_000
    const val MAX_COMPRESSION_RATIO = 200.0
    const val MAX_TOC_BYTES = 256 * 1024
    private const val ZIP64_EXTRA_FIELD_ID = 0x0001

    fun inspect(file: File, archiveName: String = file.name): AddonArchiveInspection {
        if (!file.isFile || file.length() <= 0L) {
            throw AddonArchiveException("The selected file is empty or unavailable.")
        }
        if (file.length() > MAX_ARCHIVE_BYTES) {
            throw AddonArchiveException("ZIP is larger than the 128 MiB import limit.")
        }

        if (hasZip64EndLocator(file)) {
            throw AddonArchiveException("ZIP64 archives are not supported.")
        }
        val digest = sha256(file)
        try {
            ZipFile(file).use { zip ->
                val seenPaths = HashSet<String>()
                val tocEntries = ArrayList<ZipArchiveEntry>()
                var expandedBytes = 0L
                var entryCount = 0

                val entries = zip.entries
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    entryCount++
                    if (entryCount > MAX_ENTRIES) {
                        throw AddonArchiveException("ZIP contains more than $MAX_ENTRIES entries.")
                    }

                    val safePath = validatePath(entry.name)
                    val pathKey = safePath.lowercase(Locale.ROOT)
                    if (!seenPaths.add(pathKey)) {
                        throw AddonArchiveException("ZIP contains duplicate paths that differ only by case: ${entry.name}")
                    }

                    if (entry.isUnixSymlink) {
                        throw AddonArchiveException("ZIP contains a symbolic link: ${entry.name}")
                    }
                    if (entry.generalPurposeBit?.usesEncryption() == true) {
                        throw AddonArchiveException("Encrypted ZIP entries are not supported.")
                    }
                    if (entry.method != java.util.zip.ZipEntry.STORED &&
                        entry.method != java.util.zip.ZipEntry.DEFLATED
                    ) {
                        throw AddonArchiveException("Unsupported ZIP compression method in ${entry.name}.")
                    }
                    if (entry.versionRequired >= 45 ||
                        entry.getExtraFields(true).any { it.headerId.value == ZIP64_EXTRA_FIELD_ID }
                    ) {
                        throw AddonArchiveException("ZIP64 archives are not supported.")
                    }
                    if (entry.size < 0L || entry.compressedSize < 0L || entry.crc < 0L) {
                        throw AddonArchiveException("ZIP entry has missing size or CRC metadata: ${entry.name}")
                    }
                    if (entry.size > MAX_ENTRY_BYTES) {
                        throw AddonArchiveException("ZIP entry exceeds the 64 MiB per-file limit: ${entry.name}")
                    }
                    expandedBytes = Math.addExact(expandedBytes, entry.size)
                    if (expandedBytes > MAX_EXPANDED_BYTES) {
                        throw AddonArchiveException("ZIP expands beyond the 512 MiB total limit.")
                    }
                    if (entry.size > 0L) {
                        if (entry.compressedSize == 0L ||
                            entry.size.toDouble() / entry.compressedSize.toDouble() > MAX_COMPRESSION_RATIO
                        ) {
                            throw AddonArchiveException("Suspicious compression ratio in ${entry.name}.")
                        }
                    }

                    if (entry.isDirectory && entry.size != 0L) {
                        throw AddonArchiveException("Directory entry unexpectedly contains data: ${entry.name}")
                    }
                    verifyEntry(zip, entry)
                    if (!entry.isDirectory && safePath.endsWith(".toc", ignoreCase = true)) {
                        tocEntries += entry
                    }
                }

                if (tocEntries.isEmpty()) {
                    throw AddonArchiveException("No World of Warcraft .toc file was found in this ZIP.")
                }

                val addons = tocEntries.map { entry -> parseToc(zip, entry) }
                    .distinctBy { it.rootPath.lowercase(Locale.ROOT) }
                if (addons.isEmpty()) {
                    throw AddonArchiveException("No valid addon folders were found.")
                }
                if (addons.map { it.folderName.lowercase(Locale.ROOT) }.distinct().size != addons.size) {
                    throw AddonArchiveException("ZIP contains multiple addon roots with the same folder name.")
                }

                return AddonArchiveInspection(
                    archiveName = archiveName,
                    archiveBytes = file.length(),
                    sha256 = digest,
                    expandedBytes = expandedBytes,
                    entryCount = entryCount,
                    addons = addons,
                )
            }
        } catch (error: AddonArchiveException) {
            throw error
        } catch (error: Exception) {
            throw AddonArchiveException("The ZIP is malformed or could not be read.", error)
        }
    }

    private fun validatePath(rawPath: String): String {
        if (rawPath.isBlank() || rawPath.indexOf('\u0000') >= 0 || rawPath.contains('\\')) {
            throw AddonArchiveException("ZIP contains an invalid path.")
        }
        if (rawPath.startsWith('/') || Regex("^[A-Za-z]:").containsMatchIn(rawPath)) {
            throw AddonArchiveException("ZIP contains an absolute path: $rawPath")
        }
        if (rawPath.length > 1_024) {
            throw AddonArchiveException("ZIP entry path is too long.")
        }

        val path = rawPath.removeSuffix("/")
        val segments = path.split('/')
        if (segments.any { it.isEmpty() || it == "." || it == ".." }) {
            throw AddonArchiveException("ZIP contains a traversal or malformed path: $rawPath")
        }
        return path
    }

    private fun verifyEntry(zip: ZipFile, entry: ZipArchiveEntry) {
        val crc = CRC32()
        var bytesRead = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        zip.getInputStream(entry).use { input ->
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                bytesRead += count
                if (bytesRead > entry.size || bytesRead > MAX_ENTRY_BYTES) {
                    throw AddonArchiveException("ZIP entry expands beyond its declared size: ${entry.name}")
                }
                crc.update(buffer, 0, count)
            }
        }
        if (bytesRead != entry.size || crc.value != entry.crc) {
            throw AddonArchiveException("ZIP entry size or CRC check failed: ${entry.name}")
        }
    }

    private fun parseToc(zip: ZipFile, entry: ZipArchiveEntry): AddonTocMetadata {
        val tocBytes = zip.getInputStream(entry).use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size() + count > MAX_TOC_BYTES) {
                    throw AddonArchiveException("TOC file is larger than 256 KiB: ${entry.name}")
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }

        val text = String(tocBytes, Charsets.UTF_8).removePrefix("\uFEFF")
        val directives = HashMap<String, String>()
        text.lineSequence().forEach { line ->
            val match = Regex("^\\s*##\\s*([^:]+):\\s*(.*?)\\s*$").matchEntire(line) ?: return@forEach
            directives[match.groupValues[1].trim().lowercase(Locale.ROOT)] = match.groupValues[2].trim()
        }

        val tocPath = entry.name
        val rootPath = tocPath.substringBeforeLast('/', missingDelimiterValue = "")
        val folderName = rootPath.substringAfterLast('/').ifBlank {
            tocPath.substringAfterLast('/').substringBeforeLast('.')
        }
        val title = directives["title"]?.takeIf { it.isNotBlank() } ?: folderName
        val interfaceVersions = directives["interface"]
            ?.split(',', ' ', '\t')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.distinct()
            .orEmpty()
        val dependencies = (splitDependencies(directives["dependencies"]) + splitDependencies(directives["requiredeps"])).distinct()
        val optionalDependencies = splitDependencies(directives["optionaldeps"])

        return AddonTocMetadata(
            rootPath = rootPath,
            tocPath = tocPath,
            folderName = folderName,
            title = title,
            notes = directives["notes"],
            version = directives["version"],
            interfaceVersions = interfaceVersions,
            dependencies = dependencies,
            optionalDependencies = optionalDependencies,
        )
    }

    private fun splitDependencies(value: String?): List<String> =
        value?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.distinct().orEmpty()

    private fun hasZip64EndLocator(file: File): Boolean {
        val tailLength = minOf(file.length(), 22L + 65_535L + 20L).toInt()
        if (tailLength < 42) return false
        val tail = ByteArray(tailLength)
        RandomAccessFile(file, "r").use { input ->
            input.seek(file.length() - tailLength)
            input.readFully(tail)
        }
        for (eocd in tail.size - 22 downTo 20) {
            if (tail[eocd] != 0x50.toByte() || tail[eocd + 1] != 0x4b.toByte() ||
                tail[eocd + 2] != 0x05.toByte() || tail[eocd + 3] != 0x06.toByte()
            ) continue
            val commentLength = (tail[eocd + 20].toInt() and 0xff) or
                ((tail[eocd + 21].toInt() and 0xff) shl 8)
            if (eocd + 22 + commentLength != tail.size) continue
            val locator = eocd - 20
            if (tail[locator] == 0x50.toByte() && tail[locator + 1] == 0x4b.toByte() &&
                tail[locator + 2] == 0x06.toByte() && tail[locator + 3] == 0x07.toByte()
            ) return true
            return false
        }
        return false
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

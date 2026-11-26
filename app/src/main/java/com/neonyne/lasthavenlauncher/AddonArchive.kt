package com.neonyne.lasthavenlauncher

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.CRC32
import org.apache.commons.compress.archivers.zip.ZipFile

data class ImportedAddon(
  val title: String,
  val folder: String,
  val version: String,
  val interfaceVersion: String,
  val notes: String,
  val dependencies: List<String>,
  val files: Map<String, File>,
  val sizeBytes: Long,
  val warnings: List<String>,
  val optionalDependencies: List<String> = emptyList(),
)

object AddonArchive {
  const val MAX_ARCHIVE_BYTES = 128L * 1024 * 1024
  const val MAX_EXPANDED_BYTES = 256L * 1024 * 1024
  const val MAX_FILE_BYTES = 64L * 1024 * 1024
  const val MAX_TOC_BYTES = 1024L * 1024
  const val MAX_ENTRIES = 32_000
  const val MAX_ADDONS_PER_ARCHIVE = 256
  const val MAX_LIBRARY_BYTES = 384L * 1024 * 1024

  private data class TocMetadata(
    val title: String,
    val version: String,
    val interfaceVersion: String,
    val notes: String,
    val dependencies: List<String>,
    val optionalDependencies: List<String>,
  )

  private data class AddonRoot(val path: String, val folder: String)

  /**
   * Copies the selected archive to private staging storage, validates its central directory, then
   * streams each accepted file to disk. Expanded addon payloads are never retained in RAM.
   *
   * The caller may supply a verified document size when the provider exposes one. Unknown sizes are
   * accepted, but the actual streamed byte count is always capped at the compressed archive limit.
   */
  fun importZip(
    input: InputStream,
    compressedSize: Long?,
    extractionDirectory: File,
    beforeExtraction: (Long) -> Unit = {},
  ): List<ImportedAddon> {
    require(compressedSize == null || compressedSize in 1..MAX_ARCHIVE_BYTES) {
      "Archive must be between 1 byte and 128 MiB."
    }
    require(extractionDirectory.mkdirs() || extractionDirectory.isDirectory) {
      "Could not prepare temporary addon storage."
    }

    var archiveFile: File? = null
    try {
      val archiveParent =
        extractionDirectory.parentFile
          ?: throw IllegalArgumentException("Temporary addon storage has no parent directory.")
      val sourceArchive = File.createTempFile("addon-source-", ".zip", archiveParent)
      archiveFile = sourceArchive
      var copiedBytes = 0L
      FileOutputStream(sourceArchive).use { output ->
        input.use { source ->
          val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
          while (true) {
            val count = source.read(buffer)
            if (count < 0) break
            if (count == 0) continue
            copiedBytes += count
            require(copiedBytes <= MAX_ARCHIVE_BYTES) {
              "Archive exceeds the 128 MiB compressed-size limit."
            }
            output.write(buffer, 0, count)
          }
        }
        output.fd.sync()
      }
      require(compressedSize == null || copiedBytes == compressedSize) {
        "Selected archive size changed or could not be verified."
      }
      require(copiedBytes > 0) { "Selected archive is empty." }

      val entries = linkedMapOf<String, File>()
      val seen = mutableSetOf<String>()
      val acceptedEntries = mutableListOf<org.apache.commons.compress.archivers.zip.ZipArchiveEntry>()
      var declaredExpandedBytes = 0L
      var entryCount = 0
      val canonicalRoot = extractionDirectory.canonicalFile

      ZipFile(sourceArchive).use { zip ->
        val archiveEntries = zip.entries
        while (archiveEntries.hasMoreElements()) {
          val entry = archiveEntries.nextElement()
          entryCount += 1
          require(entryCount <= MAX_ENTRIES) { "Archive contains too many entries." }

          val path = validateEntryPath(entry.name)
          require(seen.add(path.lowercase(Locale.ROOT))) { "Duplicate archive path: $path" }
          require(!entry.isUnixSymlink) { "Symbolic links are not supported: $path" }
          require(!entry.generalPurposeBit.usesEncryption()) {
            "Encrypted ZIP entries are not supported: $path"
          }
          require(entry.method == 0 || entry.method == 8) {
            "Unsupported ZIP compression method: $path"
          }
          require(zip.canReadEntryData(entry)) { "Unsupported ZIP entry: $path" }

          if (entry.isDirectory) continue
          require(entry.size in 0..MAX_FILE_BYTES) {
            "File exceeds the 64 MiB limit or has an invalid size: $path"
          }
          require(entry.compressedSize >= 0 && entry.crc >= 0) {
            "ZIP entry metadata is incomplete: $path"
          }
          declaredExpandedBytes += entry.size
          require(declaredExpandedBytes <= MAX_EXPANDED_BYTES) {
            "Expanded archive exceeds the 256 MiB limit."
          }
          acceptedEntries += entry
        }

        // Check free space before writing any expanded archive payload to disk.
        beforeExtraction(declaredExpandedBytes)

        var expandedBytes = 0L
        for (entry in acceptedEntries) {
          val path = validateEntryPath(entry.name)
          val destination = File(extractionDirectory, path)
          require(destination.canonicalFile.path.startsWith(canonicalRoot.path + File.separator)) {
            "Unsafe archive path: $path"
          }
          destination.parentFile?.let { parent ->
            require(parent.mkdirs() || parent.isDirectory) {
              "Could not create temporary addon directory."
            }
          }

          val crc = CRC32()
          var fileBytes = 0L
          zip.getInputStream(entry).use { source ->
            FileOutputStream(destination).use { output ->
              val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
              while (true) {
                val count = source.read(buffer)
                if (count < 0) break
                if (count == 0) continue
                fileBytes += count
                expandedBytes += count
                require(fileBytes <= MAX_FILE_BYTES) {
                  "File exceeds the 64 MiB limit: $path"
                }
                require(expandedBytes <= MAX_EXPANDED_BYTES) {
                  "Expanded archive exceeds the 256 MiB limit."
                }
                crc.update(buffer, 0, count)
                output.write(buffer, 0, count)
              }
            }
          }
          require(fileBytes == entry.size && crc.value == entry.crc) {
            "File integrity check failed: $path"
          }
          entries[path] = destination
        }
      }

      check(sourceArchive.delete()) { "Could not remove temporary addon archive." }
      archiveFile = null
      return parseAddons(entries)
    } catch (error: Exception) {
      archiveFile?.delete()
      extractionDirectory.deleteRecursively()
      throw error
    }
  }

  internal fun validateEntryPath(name: String): String {
    val path = name.replace('\\', '/')
    val segments = path.split('/')
    require(
      path.isNotEmpty() &&
        !path.startsWith('/') &&
        !Regex("^[A-Za-z]:").containsMatchIn(path) &&
        !path.contains(':') &&
        !path.any { it.code in 0..31 } &&
        segments.dropLast(if (path.endsWith('/')) 1 else 0).none {
          it.isEmpty() || it == "." || it == ".."
        }
    ) {
      "Unsafe archive path: $name"
    }
    return path
  }

  private fun parseAddons(entries: Map<String, File>): List<ImportedAddon> {
    val roots = mutableListOf<AddonRoot>()
    for (path in entries.keys) {
      val parts = path.split('/')
      val fileName = parts.last()
      if (parts.size >= 2) {
        val folder = parts[parts.lastIndex - 1]
        if (fileName.equals("$folder.toc", ignoreCase = true)) {
          roots += AddonRoot(parts.dropLast(1).joinToString("/"), folder)
        }
      } else if (fileName.endsWith(".toc", ignoreCase = true)) {
        roots += AddonRoot("", fileName.dropLast(4))
      }
    }

    require(roots.isNotEmpty()) { "Main Folder/Folder.toc is missing." }
    require(roots.size <= MAX_ADDONS_PER_ARCHIVE) {
      "Archive contains too many addons (maximum $MAX_ADDONS_PER_ARCHIVE)."
    }
    require(roots.size == 1 || roots.none { it.path.isEmpty() }) {
      "Multiple addons must each be packaged in a separate folder."
    }
    val topRoots = roots.filter { root ->
      roots.none { parent ->
        parent != root && (parent.path.isEmpty() || root.path.startsWith(parent.path + "/"))
      }
    }

    val seenFolders = mutableSetOf<String>()
    return topRoots.map { root ->
      require(
        root.folder.matches(Regex("[A-Za-z0-9_-]+")) &&
          !root.folder.startsWith("blizzard_", ignoreCase = true)
      ) {
        "Invalid or reserved addon folder: ${root.folder}"
      }
      require(seenFolders.add(root.folder.lowercase(Locale.ROOT))) {
        "Duplicate addon folder: ${root.folder}"
      }

      val ownFiles =
        entries
          .filter { (path, _) ->
            val inRoot = if (root.path.isEmpty()) true else path.startsWith(root.path + "/")
            inRoot &&
              path.split('/').none { it.equals("__MACOSX", ignoreCase = true) } &&
              !Regex("(^|/)\\.(git|svn)(/|$)").containsMatchIn(path)
          }
          .mapKeys { (path, _) ->
            if (root.path.isEmpty()) path else path.removePrefix(root.path + "/")
          }

      val tocName = "${root.folder}.toc"
      val tocFile =
        ownFiles.entries.firstOrNull { it.key.equals(tocName, ignoreCase = true) }?.value
          ?: throw IllegalArgumentException("Main Folder/Folder.toc is missing for ${root.folder}.")
      require(tocFile.length() <= MAX_TOC_BYTES) { "Addon TOC exceeds the 1 MiB limit." }
      val metadata = parseToc(String(tocFile.readBytes(), StandardCharsets.UTF_8))
      val total = ownFiles.values.sumOf { it.length() }
      val warnings =
        if (
          metadata.interfaceVersion.isNotBlank() &&
            "30300" !in metadata.interfaceVersion.split(Regex("[,\\s]+"))
        ) {
          listOf("The TOC does not declare Interface 30300. Compatibility is unverified.")
        } else {
          emptyList()
        }

      ImportedAddon(
        title = metadata.title.ifBlank { root.folder },
        folder = root.folder,
        version = metadata.version,
        interfaceVersion = metadata.interfaceVersion,
        notes = metadata.notes,
        dependencies = metadata.dependencies,
        files = ownFiles,
        sizeBytes = total,
        warnings = warnings,
        optionalDependencies = metadata.optionalDependencies,
      )
    }
  }

  private fun parseToc(text: String): TocMetadata {
    val values = mutableMapOf<String, String>()
    for (line in text.lineSequence()) {
      val match = Regex("^##\\s*([^:]+):\\s*(.*)$").find(line) ?: continue
      values[match.groupValues[1].trim().lowercase(Locale.ROOT)] = match.groupValues[2].trim()
    }
    fun clean(value: String?): String =
      value.orEmpty().replace(Regex("\\|c[0-9a-f]{8}|\\|r", RegexOption.IGNORE_CASE), "")

    val dependencies =
      (values["dependencies"] ?: values["requireddeps"] ?: values["deps"].orEmpty())
        .split(Regex("[,\\s]+"))
        .filter(String::isNotBlank)
    val optionalDependencies =
      (values["optionaldeps"] ?: values["optional-dependencies"].orEmpty())
        .split(Regex("[,\\s]+"))
        .filter(String::isNotBlank)

    return TocMetadata(
      title = clean(values["title"]),
      version = clean(values["version"]),
      interfaceVersion = values["interface"].orEmpty(),
      notes = clean(values["notes"]),
      dependencies = dependencies,
      optionalDependencies = optionalDependencies,
    )
  }
}

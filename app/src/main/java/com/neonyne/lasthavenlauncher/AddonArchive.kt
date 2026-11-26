package com.neonyne.lasthavenlauncher

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.CRC32
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream

data class ImportedAddon(
  val title: String,
  val folder: String,
  val version: String,
  val interfaceVersion: String,
  val notes: String,
  val dependencies: List<String>,
  val files: Map<String, ByteArray>,
  val sizeBytes: Long,
  val warnings: List<String>,
)

object AddonArchive {
  const val MAX_ARCHIVE_BYTES = 128L * 1024 * 1024
  const val MAX_EXPANDED_BYTES = 256L * 1024 * 1024
  const val MAX_FILE_BYTES = 64L * 1024 * 1024
  const val MAX_ENTRIES = 32_000
  const val MAX_LIBRARY_BYTES = 384L * 1024 * 1024

  private data class TocMetadata(
    val title: String,
    val version: String,
    val interfaceVersion: String,
    val notes: String,
    val dependencies: List<String>,
  )

  private data class AddonRoot(val path: String, val folder: String)

  /**
   * Reads a user-selected ZIP without trusting its declared entry sizes.
   *
   * The caller must obtain and validate the selected document's compressed size before
   * opening its stream. Expanded bytes and per-entry bytes are independently bounded here.
   */
  fun importZip(input: InputStream, compressedSize: Long): List<ImportedAddon> {
    require(compressedSize in 1..MAX_ARCHIVE_BYTES) {
      "Archive must be between 1 byte and 128 MiB."
    }

    val entries = linkedMapOf<String, ByteArray>()
    val seen = mutableSetOf<String>()
    var expandedBytes = 0L
    var entryCount = 0

    ZipArchiveInputStream(input).use { zip ->
      while (true) {
        val entry = zip.nextZipEntry ?: break
        entryCount += 1
        require(entryCount <= MAX_ENTRIES) { "Archive contains too many entries." }

        val path = validateEntryPath(entry.name)
        val normalizedKey = path.lowercase(Locale.ROOT)
        require(seen.add(normalizedKey)) { "Duplicate archive path: $path" }
        require(!entry.isUnixSymlink) { "Symbolic links are not supported: $path" }
        require(zip.canReadEntryData(entry)) { "Unsupported ZIP entry: $path" }

        if (entry.isDirectory) continue
        require(entry.size < 0 || entry.size <= MAX_FILE_BYTES) {
          "File exceeds the 64 MiB limit: $path"
        }

        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var fileBytes = 0L
        while (true) {
          val count = zip.read(buffer)
          if (count < 0) break
          if (count == 0) continue
          fileBytes += count
          expandedBytes += count
          require(fileBytes <= MAX_FILE_BYTES) { "File exceeds the 64 MiB limit: $path" }
          require(expandedBytes <= MAX_EXPANDED_BYTES) {
            "Expanded archive exceeds the 256 MiB limit."
          }
          bytes.write(buffer, 0, count)
        }

        val data = bytes.toByteArray()
        if (entry.crc >= 0) {
          val crc = CRC32().apply { update(data) }.value
          require(crc == entry.crc) { "File integrity check failed: $path" }
        }
        entries[path] = data
      }
    }

    return parseAddons(entries)
  }

  internal fun validateEntryPath(name: String): String {
    val path = name.replace('\\\\', '/')
    require(
      path.isNotEmpty() &&
        !path.startsWith('/') &&
        !path.startsWith("//") &&
        !Regex("^[A-Za-z]:").containsMatchIn(path) &&
        !path.contains(':') &&
        !path.any { it.code in 0..31 } &&
        path.split('/').none { it == "." || it == ".." },
    ) {
      "Unsafe archive path: $name"
    }
    return path
  }

  private fun parseAddons(entries: Map<String, ByteArray>): List<ImportedAddon> {
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
    val topRoots =
      roots.filter { root ->
        roots.none { parent ->
          parent != root &&
            (parent.path.isEmpty() || root.path.startsWith(parent.path + "/"))
        }
      }

    val seenFolders = mutableSetOf<String>()
    return topRoots.map { root ->
      require(root.folder.matches(Regex("[A-Za-z0-9_-]+")) &&
        !root.folder.startsWith("blizzard_", ignoreCase = true)) {
        "Invalid or reserved addon folder: ${root.folder}"
      }
      require(seenFolders.add(root.folder.lowercase(Locale.ROOT))) {
        "Duplicate addon folder: ${root.folder}"
      }

      val ownFiles =
        entries.filter { (path, _) ->
          val inRoot = if (root.path.isEmpty()) !path.contains('/') else path.startsWith(root.path + "/")
          val nestedRoot =
            topRoots.any { other ->
              other != root &&
                other.path.startsWith(if (root.path.isEmpty()) "" else root.path + "/") &&
                (root.path.isEmpty() || other.path != root.path)
            }
          inRoot &&
            !(nestedRoot && topRoots.any { other ->
              other != root && other.path.isNotEmpty() &&
                path.startsWith(other.path + "/")
            }) &&
            !path.startsWith("__MACOSX/") &&
            !Regex("(^|/)\\.(git|svn)(/|$)").containsMatchIn(path)
        }.mapKeys { (path, _) ->
          if (root.path.isEmpty()) path else path.removePrefix(root.path + "/")
        }

      val tocName = "${root.folder}.toc"
      val tocBytes =
        ownFiles.entries.firstOrNull { it.key.equals(tocName, ignoreCase = true) }?.value
          ?: throw IllegalArgumentException("Main Folder/Folder.toc is missing for ${root.folder}.")
      val metadata = parseToc(String(tocBytes, StandardCharsets.UTF_8))
      val total = ownFiles.values.sumOf { it.size.toLong() }
      val warnings =
        if (metadata.interfaceVersion.isNotBlank() &&
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

    return TocMetadata(
      title = clean(values["title"]),
      version = clean(values["version"]),
      interfaceVersion = values["interface"].orEmpty(),
      notes = clean(values["notes"]),
      dependencies = dependencies,
    )
  }
}

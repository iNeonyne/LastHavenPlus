package com.neonyne.lasthavenlauncher

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64
import java.util.Locale
import java.util.Properties
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class AddonLibraryItem(
  val title: String,
  val folder: String,
  val version: String,
  val interfaceVersion: String,
  val notes: String,
  val dependencies: List<String>,
  val sizeBytes: Long,
  val filesCount: Int,
  val enabled: Boolean,
  val importedAt: Long,
  val warnings: List<String>,
)

data class AddonLibrarySnapshot(
  val revision: String,
  val addons: List<AddonLibraryItem>,
) {
  val totalBytes: Long
    get() = addons.sumOf { it.sizeBytes }
}

/** App-private, file-backed addon library. Imported packages are never enabled automatically. */
class AddonLibraryRepository(
  private val root: File,
  private val availableStorageBytes: () -> Long = { root.usableSpace },
  private val beforeIndexWrite: (() -> Unit)? = null,
) {
  private companion object {
    const val MIN_FREE_SPACE_RESERVE_BYTES = 1024L * 1024L
  }
  private val indexFile = File(root, "library.properties")

  fun readLibrary(): AddonLibrarySnapshot {
    if (!indexFile.exists()) return AddonLibrarySnapshot("empty", emptyList())
    val properties = Properties().apply { FileInputStream(indexFile).use(::load) }
    val count =
      properties.getProperty("count")?.toIntOrNull()
        ?: throw IllegalStateException("Addon library index is invalid.")
    require(count in 0..10_000) { "Addon library index contains too many entries." }
    val addons =
      (0 until count).map { index ->
        fun value(key: String): String =
          decode(properties.getProperty("addon.$index.$key").orEmpty())
        val folder = value("folder")
        require(isSafeFolder(folder)) { "Addon library contains an invalid folder name." }
        AddonLibraryItem(
          title = value("title"),
          folder = folder,
          version = value("version"),
          interfaceVersion = value("interface"),
          notes = value("notes"),
          dependencies = decodeList(properties.getProperty("addon.$index.dependencies").orEmpty()),
          sizeBytes =
            properties.getProperty("addon.$index.size")?.toLongOrNull()?.also {
              require(it in 0..AddonArchive.MAX_LIBRARY_BYTES) {
                "Addon library size is invalid."
              }
            } ?: throw IllegalStateException("Addon library size is invalid."),
          filesCount =
            properties.getProperty("addon.$index.files")?.toIntOrNull()?.also {
              require(it in 1..AddonArchive.MAX_ENTRIES) {
                "Addon library file count is invalid."
              }
            } ?: throw IllegalStateException("Addon library file count is invalid."),
          enabled =
            properties.getProperty("addon.$index.enabled")?.toBooleanStrictOrNull() ?: false,
          importedAt = properties.getProperty("addon.$index.importedAt")?.toLongOrNull() ?: 0L,
          warnings = decodeList(properties.getProperty("addon.$index.warnings").orEmpty()),
        )
      }
    require(addons.map { it.folder.lowercase(Locale.ROOT) }.distinct().size == addons.size) {
      "Addon library contains duplicate folder names."
    }
    require(addons.sumOf { it.sizeBytes } <= AddonArchive.MAX_LIBRARY_BYTES) {
      "Addon library exceeds 384 MiB."
    }
    require(addons.all { addonDirectory(it.folder).isDirectory }) {
      "Addon library references missing addon files."
    }
    return AddonLibrarySnapshot(properties.getProperty("revision") ?: "unknown", addons)
  }

  fun importZip(input: InputStream, compressedSize: Long?): AddonLibrarySnapshot {
    if (!root.exists() && !root.mkdirs()) {
      throw IllegalStateException("Could not create addon storage.")
    }
    val extraction = File(root, ".extract-${UUID.randomUUID()}")
    val imported = AddonArchive.importZip(input, compressedSize, extraction)
    if (imported.isEmpty()) {
      extraction.deleteRecursively()
      throw IllegalArgumentException("The archive contains no addons.")
    }
    val current =
      try {
        readLibrary()
      } catch (error: Exception) {
        extraction.deleteRecursively()
        throw error
      }
    val replacing = imported.map { it.folder.lowercase(Locale.ROOT) }.toSet()
    val total =
      current.addons
        .filterNot { it.folder.lowercase(Locale.ROOT) in replacing }
        .sumOf { it.sizeBytes } + imported.sumOf { it.sizeBytes }
    if (total > AddonArchive.MAX_LIBRARY_BYTES) {
      extraction.deleteRecursively()
      throw IllegalArgumentException("Addon library exceeds 384 MiB.")
    }
    val stagedBytes = imported.sumOf { it.sizeBytes }
    require(availableStorageBytes() >= stagedBytes + MIN_FREE_SPACE_RESERVE_BYTES) {
      "Not enough free storage to safely commit imported addons."
    }

    val existing = current.addons.associateBy { it.folder.lowercase(Locale.ROOT) }
    val nextItems =
      current.addons.filterNot { it.folder.lowercase(Locale.ROOT) in replacing }.toMutableList()
    val staged = mutableListOf<Pair<File, File>>()
    val backups = mutableListOf<Pair<File, File>>()
    val newTargets = mutableListOf<File>()
    try {
      for (addon in imported) {
        require(isSafeFolder(addon.folder)) { "Invalid addon folder: ${addon.folder}" }
        val stage = File(root, ".stage-${UUID.randomUUID()}")
        require(stage.mkdirs()) { "Could not prepare addon storage." }
        staged += stage to File(root, addon.folder)
        for ((relativePath, sourceFile) in addon.files) {
          val destination = File(stage, relativePath)
          val canonicalRoot = stage.canonicalFile
          val canonicalDestination = destination.canonicalFile
          require(canonicalDestination.path.startsWith(canonicalRoot.path + File.separator)) {
            "Unsafe addon file path: $relativePath"
          }
          destination.parentFile?.let {
            if (!it.exists() && !it.mkdirs()) {
              throw IllegalStateException("Could not create addon file directory.")
            }
          }
          FileInputStream(sourceFile).use { source ->
            FileOutputStream(destination).use { target -> source.copyTo(target) }
          }
        }
        val previous = existing[addon.folder.lowercase(Locale.ROOT)]
        nextItems +=
          AddonLibraryItem(
            title = addon.title,
            folder = addon.folder,
            version = addon.version,
            interfaceVersion = addon.interfaceVersion,
            notes = addon.notes,
            dependencies = addon.dependencies,
            sizeBytes = addon.sizeBytes,
            filesCount = addon.files.size,
            enabled = previous?.enabled ?: false,
            importedAt = System.currentTimeMillis(),
            warnings = addon.warnings,
          )
      }

      for ((stage, target) in staged) {
        val backup =
          if (target.exists()) {
            File(root, ".backup-${UUID.randomUUID()}").also {
              check(target.renameTo(it)) { "Could not stage the existing addon for replacement." }
              backups += target to it
            }
          } else {
            null
          }
        check(stage.renameTo(target)) { "Could not commit imported addon files." }
        if (backup == null) newTargets += target
      }

      val snapshot = AddonLibrarySnapshot(UUID.randomUUID().toString(), nextItems)
      writeIndex(snapshot)
      backups.forEach { (_, backup) -> backup.deleteRecursively() }
      return snapshot
    } catch (error: Exception) {
      newTargets.forEach { it.deleteRecursively() }
      for ((target, backup) in backups.asReversed()) {
        target.deleteRecursively()
        if (backup.exists()) backup.renameTo(target)
      }
      throw error
    } finally {
      staged.forEach { (stage, _) -> stage.deleteRecursively() }
      extraction.deleteRecursively()
    }
  }

  fun setEnabled(folder: String, enabled: Boolean): AddonLibrarySnapshot {
    require(isSafeFolder(folder)) { "Invalid addon folder." }
    val current = readLibrary()
    require(current.addons.any { it.folder == folder }) { "Addon not found." }
    val next =
      current.copy(
        revision = UUID.randomUUID().toString(),
        addons = current.addons.map { if (it.folder == folder) it.copy(enabled = enabled) else it },
      )
    writeIndex(next)
    return next
  }

  fun removeAddon(folder: String): AddonLibrarySnapshot {
    require(isSafeFolder(folder)) { "Invalid addon folder." }
    val current = readLibrary()
    require(current.addons.any { it.folder == folder }) { "Addon not found." }
    val next =
      current.copy(
        revision = UUID.randomUUID().toString(),
        addons = current.addons.filterNot { it.folder == folder },
      )
    val target = addonDirectory(folder)
    val backup = File(root, ".remove-" + UUID.randomUUID())
    if (target.exists()) {
      check(target.renameTo(backup)) { "Could not stage addon files for removal." }
    }
    try {
      writeIndex(next)
    } catch (error: Exception) {
      if (backup.exists()) {
        target.deleteRecursively()
        check(backup.renameTo(target)) { "Could not restore addon files after removal failed." }
      }
      throw error
    }
    // A failed cleanup leaves only an unreferenced hidden backup; it cannot make the addon appear
    // installed again or corrupt the committed library index.
    backup.deleteRecursively()
    return next
  }

  fun exportZip(folder: String, output: OutputStream) {
    require(isSafeFolder(folder)) { "Invalid addon folder." }
    val addonDirectory = File(root, folder)
    require(addonDirectory.isDirectory) { "Addon not found." }
    ZipOutputStream(BufferedOutputStream(output)).use { zip ->
      val files =
        addonDirectory.walkTopDown().filter(File::isFile).sortedBy {
          it.relativeTo(addonDirectory).invariantSeparatorsPath
        }
      for (file in files) {
        val relative = file.relativeTo(addonDirectory).invariantSeparatorsPath
        AddonArchive.validateEntryPath(relative)
        zip.putNextEntry(ZipEntry("$folder/$relative"))
        FileInputStream(file).use { input -> input.copyTo(zip) }
        zip.closeEntry()
      }
    }
  }

  fun addonDirectory(folder: String): File {
    require(isSafeFolder(folder)) { "Invalid addon folder." }
    return File(root, folder)
  }

  private fun writeIndex(snapshot: AddonLibrarySnapshot) {
    val properties = Properties()
    properties["revision"] = snapshot.revision
    properties["count"] = snapshot.addons.size.toString()
    snapshot.addons.forEachIndexed { index, addon ->
      fun put(key: String, value: String) {
        properties["addon.$index.$key"] = encode(value)
      }
      put("title", addon.title)
      put("folder", addon.folder)
      put("version", addon.version)
      put("interface", addon.interfaceVersion)
      put("notes", addon.notes)
      properties["addon.$index.dependencies"] = encodeList(addon.dependencies)
      properties["addon.$index.size"] = addon.sizeBytes.toString()
      properties["addon.$index.files"] = addon.filesCount.toString()
      properties["addon.$index.enabled"] = addon.enabled.toString()
      properties["addon.$index.importedAt"] = addon.importedAt.toString()
      properties["addon.$index.warnings"] = encodeList(addon.warnings)
    }

    beforeIndexWrite?.invoke()
    val temporary = File(root, ".library-${UUID.randomUUID()}.tmp")
    FileOutputStream(temporary).use { output ->
      properties.store(output, "LastHavenPlus addon library")
      output.fd.sync()
    }
    try {
      Files.move(
        temporary.toPath(),
        indexFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE,
      )
    } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
      Files.move(temporary.toPath(), indexFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
  }

  private fun isSafeFolder(folder: String): Boolean =
    folder.matches(Regex("[A-Za-z0-9_-]+")) && !folder.startsWith("blizzard_", ignoreCase = true)

  private fun encode(value: String): String =
    Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8))

  private fun decode(value: String): String =
    String(Base64.getDecoder().decode(value), Charsets.UTF_8)

  private fun encodeList(values: List<String>): String = values.joinToString(".") { encode(it) }

  private fun decodeList(value: String): List<String> =
    if (value.isBlank()) emptyList() else value.split('.').map(::decode)
}

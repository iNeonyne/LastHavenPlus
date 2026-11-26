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
  val optionalDependencies: List<String> = emptyList(),
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
    const val MAX_LIBRARY_ITEMS = 10_000
    const val TXN_PREFIX = ".txn-"
    const val BACKUP_PREFIX = ".backup-"
    const val REMOVE_PREFIX = ".remove-"
    const val STAGE_PREFIX = ".stage-"
    const val EXTRACT_PREFIX = ".extract-"
    const val TEMP_INDEX_PREFIX = ".library-"
    const val TEMP_INDEX_SUFFIX = ".tmp"
    const val SOURCE_ARCHIVE_PREFIX = "addon-source-"
  }

  private val indexFile = File(root, "library.properties")

  private data class TransactionPlan(
    val newRevision: String?,
    val backups: List<Pair<File, File>>,
    val freshTargets: List<File>,
  )

  private data class CommitPlan(
    val stage: File,
    val target: File,
    val backup: File?,
  )

  fun readLibrary(performStaleSweep: Boolean = true): AddonLibrarySnapshot {
    recoverPendingTransactions(performStaleSweep)
    if (!indexFile.exists()) return AddonLibrarySnapshot("empty", emptyList())
    val properties = Properties().apply { FileInputStream(indexFile).use(::load) }
    val count =
      properties.getProperty("count")?.toIntOrNull()
        ?: throw IllegalStateException("Addon library index is invalid.")
    require(count in 0..MAX_LIBRARY_ITEMS) { "Addon library index contains too many entries." }
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
          optionalDependencies =
            decodeList(properties.getProperty("addon.$index.optionalDependencies").orEmpty()),
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

  fun importZip(input: InputStream, compressedSize: Long?): AddonLibrarySnapshot =
    input.use { source ->
      if (!root.exists() && !root.mkdirs()) {
        throw IllegalStateException("Could not create addon storage.")
      }
      val requiredStagingBytes = AddonArchive.MAX_ARCHIVE_BYTES + MIN_FREE_SPACE_RESERVE_BYTES
      require(availableStorageBytes() >= requiredStagingBytes) {
        "Not enough free storage to stage the selected archive."
      }
      val extraction = File(root, EXTRACT_PREFIX + UUID.randomUUID())
      try {
        val imported =
          AddonArchive.importZip(source, compressedSize, extraction) { expandedBytes ->
            val requiredBytes = expandedBytes * 2 + MIN_FREE_SPACE_RESERVE_BYTES
            require(availableStorageBytes() >= requiredBytes) {
              "Not enough free storage to safely extract and stage imported addons."
            }
          }
        require(imported.isNotEmpty()) { "The archive contains no addons." }

        val current = readLibrary(performStaleSweep = false)
        val replacing = imported.map { it.folder.lowercase(Locale.ROOT) }.toSet()
        val total =
          current.addons
            .filterNot { it.folder.lowercase(Locale.ROOT) in replacing }
            .sumOf { it.sizeBytes } + imported.sumOf { it.sizeBytes }
        require(total <= AddonArchive.MAX_LIBRARY_BYTES) {
          "Addon library exceeds 384 MiB."
        }
        val stagedBytes = imported.sumOf { it.sizeBytes }
        require(availableStorageBytes() >= stagedBytes + MIN_FREE_SPACE_RESERVE_BYTES) {
          "Not enough free storage to safely commit imported addons."
        }
        val projectedCount =
          current.addons.count { it.folder.lowercase(Locale.ROOT) !in replacing } + imported.size
        require(projectedCount <= MAX_LIBRARY_ITEMS) {
          "Addon library contains too many entries."
        }

        val existing = current.addons.associateBy { it.folder.lowercase(Locale.ROOT) }
        val nextItems =
          current.addons.filterNot { it.folder.lowercase(Locale.ROOT) in replacing }.toMutableList()

        val plans = mutableListOf<CommitPlan>()
        for (addon in imported) {
          require(isSafeFolder(addon.folder)) { "Invalid addon folder: ${addon.folder}" }
          val stage = File(root, STAGE_PREFIX + UUID.randomUUID())
          require(stage.mkdirs()) { "Could not prepare addon storage." }
          val target = File(root, addon.folder)
          val backup =
            if (existing.containsKey(addon.folder.lowercase(Locale.ROOT))) {
              File(root, BACKUP_PREFIX + UUID.randomUUID())
            } else {
              null
            }
          plans += CommitPlan(stage, target, backup)
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
              optionalDependencies = addon.optionalDependencies,
              sizeBytes = addon.sizeBytes,
              filesCount = addon.files.size,
              enabled = previous?.enabled ?: false,
              importedAt = System.currentTimeMillis(),
              warnings = addon.warnings,
            )
        }

        require(nextItems.size <= MAX_LIBRARY_ITEMS) {
          "Addon library contains too many entries."
        }

        val newRevision = UUID.randomUUID().toString()
        val transaction = File(root, TXN_PREFIX + UUID.randomUUID())
        writeTransactionPlan(
          transaction,
          newRevision,
          backups = plans.mapNotNull { plan -> plan.backup?.let { plan.target.name to it.name } },
          freshTargets = plans.filter { it.backup == null }.map { it.target.name },
        )
        try {
          for (plan in plans) {
            plan.backup?.let { backup ->
              check(plan.target.renameTo(backup)) {
                "Could not stage the existing addon for replacement."
              }
            }
            check(plan.stage.renameTo(plan.target)) { "Could not commit imported addon files." }
          }

          val snapshot = AddonLibrarySnapshot(newRevision, nextItems)
          writeIndex(snapshot)
          plans.forEach { plan -> plan.backup?.deleteRecursively() }
          check(transaction.delete()) { "Could not finalize the addon transaction record." }
          snapshot
        } catch (error: Exception) {
          val committed = readIndexRevision() == newRevision
          var rollbackComplete = true
          if (!committed) {
            for (plan in plans.asReversed()) {
              val backup = plan.backup
              if (backup != null && backup.exists()) {
                plan.target.deleteRecursively()
                if (!backup.renameTo(plan.target)) {
                  rollbackComplete = false
                }
              } else if (backup == null && plan.target.exists()) {
                plan.target.deleteRecursively()
              }
            }
          }
          if (rollbackComplete && !transaction.delete()) {
            rollbackComplete = false
          }
          if (!rollbackComplete) {
            throw IllegalStateException("Addon transaction rollback is incomplete.", error)
          }
          throw error
        } finally {
          plans.forEach { plan -> plan.stage.deleteRecursively() }
        }
      } finally {
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
    val newRevision = UUID.randomUUID().toString()
    val next =
      current.copy(
        revision = newRevision,
        addons = current.addons.filterNot { it.folder == folder },
      )
    val target = addonDirectory(folder)
    val backup = File(root, REMOVE_PREFIX + UUID.randomUUID())
    val transaction = File(root, TXN_PREFIX + UUID.randomUUID())
    writeTransactionPlan(
      transaction,
      newRevision,
      backups = listOf(folder to backup.name),
      freshTargets = emptyList(),
    )
    try {
      if (target.exists()) {
        check(target.renameTo(backup)) { "Could not stage addon files for removal." }
      }
      writeIndex(next)
    } catch (error: Exception) {
      if (backup.exists()) {
        target.deleteRecursively()
        check(backup.renameTo(target)) { "Could not restore addon files after removal failed." }
      }
      check(transaction.delete()) { "Could not finalize the addon transaction record." }
      throw error
    }
    // A failed cleanup leaves only an unreferenced hidden backup; the transaction record ensures
    // the next library read finishes the cleanup instead of leaving mixed state.
    backup.deleteRecursively()
    check(transaction.delete()) { "Could not finalize the addon transaction record." }
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

  /**
   * Resolves crash-interrupted import/removal transactions before the index is read. Each
   * transaction record names its pre-assigned backup directories, affected folders and the index
   * revision it would commit; a committed transaction only finishes backup cleanup, an interrupted
   * one rolls back to the recorded previous state. Staging, extraction, source-archive, backup and
   * temporary-index leftovers are deleted only after every recorded transaction has been resolved;
   * an unresolvable record stops cleanup so no referenced data is discarded. The sweep is skipped
   * while an import is in flight so the active extraction directory is never treated as stale.
   */
  private fun recoverPendingTransactions(performStaleSweep: Boolean) {
    if (!root.isDirectory) return
    val pending =
      root.listFiles { file -> file.name.startsWith(TXN_PREFIX) }?.sortedBy { it.name } ?: return
    for (transaction in pending) {
      val plan = loadTransactionPlan(transaction)
      if (plan == null || plan.newRevision == null) {
        return
      }
      if (plan.newRevision == readIndexRevision()) {
        plan.backups.forEach { (_, backup) -> backup.deleteRecursively() }
      } else {
        for ((target, backup) in plan.backups.asReversed()) {
          target.deleteRecursively()
          if (backup.exists() && !backup.renameTo(target)) {
            return
          }
        }
        for (fresh in plan.freshTargets) {
          fresh.deleteRecursively()
        }
      }
      if (!transaction.delete()) {
        return
      }
    }
    if (!performStaleSweep) return
    root
      .listFiles { file ->
        file.name.startsWith(EXTRACT_PREFIX) ||
          file.name.startsWith(STAGE_PREFIX) ||
          file.name.startsWith(BACKUP_PREFIX) ||
          file.name.startsWith(REMOVE_PREFIX) ||
          file.name.startsWith(SOURCE_ARCHIVE_PREFIX) ||
          (file.name.startsWith(TEMP_INDEX_PREFIX) && file.name.endsWith(TEMP_INDEX_SUFFIX))
      }
      ?.forEach { it.deleteRecursively() }
  }

  private fun readIndexRevision(): String? = runCatching {
    if (!indexFile.exists()) return null
    Properties().apply { FileInputStream(indexFile).use(::load) }.getProperty("revision")
  }
    .getOrNull()

  private fun writeTransactionPlan(
    transaction: File,
    newRevision: String,
    backups: List<Pair<String, String>>,
    freshTargets: List<String>,
  ) {
    val properties = Properties()
    properties["newRevision"] = newRevision
    backups.forEachIndexed { index, (folder, backupName) ->
      properties["backup.$index"] = backupName
      properties["target.$index"] = folder
    }
    freshTargets.forEachIndexed { index, folder -> properties["fresh.$index"] = folder }
    FileOutputStream(transaction).use { output ->
      properties.store(output, "LastHavenPlus addon transaction")
      output.fd.sync()
    }
  }

  private fun loadTransactionPlan(transaction: File): TransactionPlan? = runCatching {
    val properties = Properties()
    FileInputStream(transaction).use { properties.load(it) }
    val newRevision = properties.getProperty("newRevision") ?: return@runCatching null
    val backups = mutableListOf<Pair<File, File>>()
    val freshTargets = mutableListOf<File>()
    for (key in properties.stringPropertyNames()) {
      val value = properties.getProperty(key) ?: continue
      when {
        key.startsWith("backup.") -> {
          val folder =
            properties.getProperty("target." + key.removePrefix("backup."))
              ?: return@runCatching null
          backups += File(root, folder) to File(root, value)
        }
        key.startsWith("fresh.") -> freshTargets += File(root, value)
      }
    }
    TransactionPlan(newRevision, backups, freshTargets)
  }
    .getOrNull()

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
      properties["addon.$index.optionalDependencies"] = encodeList(addon.optionalDependencies)
      properties["addon.$index.size"] = addon.sizeBytes.toString()
      properties["addon.$index.files"] = addon.filesCount.toString()
      properties["addon.$index.enabled"] = addon.enabled.toString()
      properties["addon.$index.importedAt"] = addon.importedAt.toString()
      properties["addon.$index.warnings"] = encodeList(addon.warnings)
    }

    beforeIndexWrite?.invoke()
    val temporary = File(root, TEMP_INDEX_PREFIX + UUID.randomUUID() + TEMP_INDEX_SUFFIX)
    try {
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
    } finally {
      temporary.delete()
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

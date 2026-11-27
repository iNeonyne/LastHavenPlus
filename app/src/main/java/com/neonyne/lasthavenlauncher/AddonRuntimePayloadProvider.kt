package com.neonyne.lasthavenlauncher

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.LinkOption
import java.security.MessageDigest
import java.util.ArrayDeque
import java.util.Locale

/**
 * Bounded, read-only view of enabled addon resources for the ZIP 0.5.1 runtime payload.
 *
 * The provider never copies files into extension storage. It snapshots validated file descriptors
 * from the app-private library and reads only the requested bounded chunk from the same file.
 */
internal class AddonRuntimePayloadProvider(private val repository: AddonLibraryRepository) {
  data class FileDescriptor
  internal constructor(
    val folder: String,
    val path: String,
    val sizeBytes: Long,
    internal val file: File,
    internal val canonicalPath: String,
    internal val lastModified: Long,
  )

  data class Manifest
  internal constructor(
    val revision: String,
    val sourceRevision: String,
    val addonFolders: List<String>,
    val files: List<FileDescriptor>,
    internal val filesByKey: Map<String, FileDescriptor>,
    val totalBytes: Long,
  ) {
    val pageCount: Int
      get() =
        (files.size + AddonRuntimePayloadProvider.MANIFEST_PAGE_SIZE - 1) /
          AddonRuntimePayloadProvider.MANIFEST_PAGE_SIZE
  }

  fun buildManifest(snapshot: AddonLibrarySnapshot): Manifest {
    val plan = AddonRuntimePlanner.plan(snapshot)
    if (plan.orderedAddons.size > MAX_RUNTIME_ADDONS) {
      throw AddonRuntimePayloadException("payloadTooLarge")
    }

    val descriptors = mutableListOf<FileDescriptor>()
    val addonFolders = mutableListOf<String>()
    var totalBytes = 0L

    for (addon in plan.orderedAddons) {
      val directory = repository.addonDirectory(addon.folder)
      if (!directory.isDirectory) {
        throw AddonRuntimePayloadException("unavailable")
      }
      val canonicalDirectory = directory.canonicalFile
      val expectedParent = directory.absoluteFile.parentFile?.canonicalFile
      if (expectedParent == null || canonicalDirectory.parentFile != expectedParent) {
        throw AddonRuntimePayloadException("invalidPath")
      }
      val canonicalRootPath = canonicalDirectory.toPath()
      val pendingDirectories = ArrayDeque<File>()
      pendingDirectories.add(directory)
      var addonBytes = 0L
      var addonFiles = 0
      var addonEntries = 0

      while (pendingDirectories.isNotEmpty()) {
        val current = pendingDirectories.removeLast()
        val children = mutableListOf<File>()
        try {
          Files.newDirectoryStream(current.toPath()).use { entries ->
            for (entry in entries) {
              addonEntries += 1
              if (addonEntries > AddonArchive.MAX_ENTRIES) {
                throw AddonRuntimePayloadException("payloadTooLarge")
              }
              children += entry.toFile()
            }
          }
        } catch (error: AddonRuntimePayloadException) {
          throw error
        } catch (_: Exception) {
          throw AddonRuntimePayloadException("readFailed")
        }
        children.sortBy { it.name }
        for (child in children) {
          if (Files.isSymbolicLink(child.toPath())) {
            throw AddonRuntimePayloadException("invalidPath")
          }
          val canonicalChild = child.canonicalFile
          if (
            !canonicalChild.toPath().startsWith(canonicalRootPath) ||
              canonicalChild == canonicalDirectory
          ) {
            throw AddonRuntimePayloadException("invalidPath")
          }
          if (child.isDirectory) {
            if (!Files.isDirectory(child.toPath(), LinkOption.NOFOLLOW_LINKS)) {
              throw AddonRuntimePayloadException("invalidPath")
            }
            pendingDirectories.add(child)
            continue
          }
          if (!child.isFile || !Files.isRegularFile(child.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            throw AddonRuntimePayloadException("invalidPath")
          }

          val relativePath = child.relativeTo(directory).invariantSeparatorsPath
          val validatedPath = runCatching {
            AddonArchive.validateEntryPath(relativePath)
          }
            .getOrElse {
              throw AddonRuntimePayloadException("invalidPath")
            }
          if (validatedPath != relativePath || relativePath.length > MAX_RUNTIME_PATH_CHARS) {
            throw AddonRuntimePayloadException("invalidPath")
          }

          val size = child.length()
          if (size !in 0..MAX_RUNTIME_FILE_BYTES) {
            throw AddonRuntimePayloadException("payloadTooLarge")
          }
          addonBytes += size
          totalBytes += size
          addonFiles += 1
          if (
            addonFiles > AddonArchive.MAX_ENTRIES ||
              descriptors.size + 1 > MAX_RUNTIME_FILES ||
              totalBytes > MAX_RUNTIME_BUNDLE_BYTES
          ) {
            throw AddonRuntimePayloadException("payloadTooLarge")
          }

          descriptors +=
            FileDescriptor(
              folder = addon.folder,
              path = relativePath,
              sizeBytes = size,
              file = child,
              canonicalPath = canonicalChild.path,
              lastModified = child.lastModified(),
            )
        }
      }

      if (addonFiles != addon.filesCount || addonBytes != addon.sizeBytes) {
        throw AddonRuntimePayloadException("unavailable")
      }
      addonFolders += addon.folder
    }

    val sortedFiles =
      descriptors.sortedWith(compareBy<FileDescriptor> { it.folder }.thenBy { it.path })
    val normalizedKeys = sortedFiles.map { key(it.folder, it.path).lowercase(Locale.ROOT) }
    if (normalizedKeys.distinct().size != normalizedKeys.size) {
      throw AddonRuntimePayloadException("invalidPath")
    }
    val filesByKey = sortedFiles.associateBy { key(it.folder, it.path) }
    val revisionSignature = buildString {
      append(plan.bundleRevision)
      sortedFiles.forEach {
        append('\n')
          .append(it.folder)
          .append('\t')
          .append(it.path)
          .append('\t')
          .append(it.sizeBytes)
          .append('\t')
          .append(it.lastModified)
      }
    }
    val revision =
      MessageDigest.getInstance("SHA-256")
        .digest(revisionSignature.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    return Manifest(
      revision = revision,
      sourceRevision = snapshot.revision,
      addonFolders = addonFolders,
      files = sortedFiles,
      filesByKey = filesByKey,
      totalBytes = totalBytes,
    )
  }

  fun page(manifest: Manifest, page: Int): List<FileDescriptor> {
    if (page !in 0 until manifest.pageCount) {
      throw AddonRuntimePayloadException("invalidRequest")
    }
    val start = page * MANIFEST_PAGE_SIZE
    return manifest.files.subList(start, minOf(start + MANIFEST_PAGE_SIZE, manifest.files.size))
  }

  fun readChunk(manifest: Manifest, folder: String, path: String, offset: Long): ByteArray {
    val descriptor =
      manifest.filesByKey[key(folder, path)] ?: throw AddonRuntimePayloadException("notFound")
    if (offset < 0 || offset >= descriptor.sizeBytes) {
      throw AddonRuntimePayloadException("invalidOffset")
    }
    val currentCanonical = descriptor.file.canonicalFile
    if (
      Files.isSymbolicLink(descriptor.file.toPath()) ||
        currentCanonical.path != descriptor.canonicalPath ||
        !Files.isRegularFile(descriptor.file.toPath(), LinkOption.NOFOLLOW_LINKS) ||
        descriptor.file.length() != descriptor.sizeBytes ||
        descriptor.file.lastModified() != descriptor.lastModified
    ) {
      throw AddonRuntimePayloadException("unavailable")
    }

    val length = minOf(MAX_FILE_CHUNK_BYTES.toLong(), descriptor.sizeBytes - offset).toInt()
    val bytes = ByteArray(length)
    try {
      RandomAccessFile(descriptor.file, "r").use { input ->
        input.seek(offset)
        input.readFully(bytes)
      }
    } catch (_: Exception) {
      throw AddonRuntimePayloadException("readFailed")
    }
    return bytes
  }

  private fun key(folder: String, path: String): String = "$folder/$path"

  companion object {
    const val MAX_RUNTIME_BUNDLE_BYTES = 16L * 1024 * 1024
    const val MAX_RUNTIME_FILE_BYTES = 8L * 1024 * 1024
    const val MAX_RUNTIME_FILES = 1024
    const val MAX_RUNTIME_ADDONS = 128
    const val MAX_RUNTIME_PATH_CHARS = 512
    const val MAX_FILE_CHUNK_BYTES = 32 * 1024
    const val MANIFEST_PAGE_SIZE = 32
  }
}

internal class AddonRuntimePayloadException(val code: String) : IllegalStateException(code)

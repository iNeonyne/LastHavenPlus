package com.neonyne.lasthavenlauncher

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonRuntimePayloadProviderTest {
  private fun item(folder: String, directory: File, enabled: Boolean = true): AddonLibraryItem {
    val files = directory.walkTopDown().filter(File::isFile).toList()
    return AddonLibraryItem(
      title = folder,
      folder = folder,
      version = "",
      interfaceVersion = "30300",
      notes = "",
      dependencies = emptyList(),
      sizeBytes = files.sumOf { it.length() },
      filesCount = files.size,
      enabled = enabled,
      importedAt = 1L,
      warnings = emptyList(),
    )
  }

  private fun withRoot(block: (File) -> Unit) {
    val root = Files.createTempDirectory("addon-runtime-payload").toFile()
    try {
      block(root)
    } finally {
      root.deleteRecursively()
    }
  }

  private fun writeAddon(root: File, folder: String, files: Map<String, ByteArray>): File {
    val directory = File(root, folder)
    check(directory.mkdirs())
    for ((path, bytes) in files) {
      val file = File(directory, path)
      val parent = file.parentFile
      check(parent == null || parent.mkdirs() || parent.isDirectory)
      file.writeBytes(bytes)
    }
    return directory
  }

  @Test
  fun manifestIncludesEveryResourceAndReadsBoundedChunks() = withRoot { root ->
    val imageBytes = ByteArray(AddonRuntimePayloadProvider.MAX_FILE_CHUNK_BYTES + 17) { (it % 251).toByte() }
    val directory =
      writeAddon(
        root,
        "UnitFrame",
        mapOf(
          "UnitFrame.toc" to "## Interface: 30300\ninit.lua\n".toByteArray(),
          "init.lua" to "print('ready')".toByteArray(),
          "Textures/portrait.png" to imageBytes,
        ),
      )
    val snapshot = AddonLibrarySnapshot("library-revision", listOf(item("UnitFrame", directory)))
    val provider = AddonRuntimePayloadProvider(AddonLibraryRepository(root))
    val manifest = provider.buildManifest(snapshot)

    assertEquals(listOf("UnitFrame"), manifest.addonFolders)
    assertEquals(
      listOf("Textures/portrait.png", "UnitFrame.toc", "init.lua"),
      manifest.files.map { it.path },
    )
    assertEquals(imageBytes.size.toLong(), manifest.totalBytes - "## Interface: 30300\ninit.lua\n".length - "print('ready')".length)
    assertEquals(1, manifest.pageCount)
    assertEquals(3, provider.page(manifest, 0).size)

    val first = provider.readChunk(manifest, "UnitFrame", "Textures/portrait.png", 0)
    val second =
      provider.readChunk(
        manifest,
        "UnitFrame",
        "Textures/portrait.png",
        AddonRuntimePayloadProvider.MAX_FILE_CHUNK_BYTES.toLong(),
      )
    assertEquals(AddonRuntimePayloadProvider.MAX_FILE_CHUNK_BYTES, first.size)
    assertArrayEquals(imageBytes.copyOfRange(0, first.size), first)
    assertArrayEquals(imageBytes.copyOfRange(first.size, imageBytes.size), second)
  }

  @Test
  fun disabledAndDependencyBlockedAddonsAreNotIncluded() = withRoot { root ->
    val disabledDirectory = writeAddon(root, "Disabled", mapOf("Disabled.toc" to byteArrayOf(1)))
    val missingDirectory = writeAddon(root, "MissingDep", mapOf("MissingDep.toc" to byteArrayOf(2)))
    val snapshot =
      AddonLibrarySnapshot(
        "revision",
        listOf(
          item("Disabled", disabledDirectory, enabled = false),
          item("MissingDep", missingDirectory).copy(dependencies = listOf("NotInstalled")),
        ),
      )
    val manifest = AddonRuntimePayloadProvider(AddonLibraryRepository(root)).buildManifest(snapshot)
    assertTrue(manifest.addonFolders.isEmpty())
    assertTrue(manifest.files.isEmpty())
    assertEquals(0L, manifest.totalBytes)
    assertEquals(0, manifest.pageCount)
  }

  @Test
  fun rejectsFilesLargerThanRuntimeFileLimit() = withRoot { root ->
    val directory = writeAddon(root, "LargeFile", mapOf("LargeFile.toc" to byteArrayOf(1)))
    val large = File(directory, "large.bin")
    RandomAccessFile(large, "rw").use {
      it.setLength(AddonRuntimePayloadProvider.MAX_RUNTIME_FILE_BYTES + 1)
    }
    val snapshot = AddonLibrarySnapshot("revision", listOf(item("LargeFile", directory)))
    val error =
      assertThrows(AddonRuntimePayloadException::class.java) {
        AddonRuntimePayloadProvider(AddonLibraryRepository(root)).buildManifest(snapshot)
      }
    assertEquals("payloadTooLarge", error.code)
  }

  @Test
  fun rejectsLibraryIndexMismatchAndUnsafePaths() = withRoot { root ->
    val directory = writeAddon(root, "UnitFrame", mapOf("UnitFrame.toc" to byteArrayOf(1)))
    val wrongCount = item("UnitFrame", directory).copy(filesCount = 2)
    assertThrows(AddonRuntimePayloadException::class.java) {
      AddonRuntimePayloadProvider(AddonLibraryRepository(root))
        .buildManifest(AddonLibrarySnapshot("revision", listOf(wrongCount)))
    }

    val unsafeName = File(directory, "bad\\\\escape.lua")
    unsafeName.writeBytes(byteArrayOf(2))
    val withUnsafePath = item("UnitFrame", directory)
    val unsafeError =
      assertThrows(AddonRuntimePayloadException::class.java) {
        AddonRuntimePayloadProvider(AddonLibraryRepository(root))
          .buildManifest(AddonLibrarySnapshot("revision", listOf(withUnsafePath)))
      }
    assertEquals("invalidPath", unsafeError.code)
    unsafeName.delete()

    val outside = File(root, "outside.lua")
    outside.writeBytes(byteArrayOf(3))
    val symlink = File(directory, "escape.lua")
    Files.createSymbolicLink(symlink.toPath(), outside.toPath())
    val withSymlink = item("UnitFrame", directory)
    val symlinkError =
      assertThrows(AddonRuntimePayloadException::class.java) {
        AddonRuntimePayloadProvider(AddonLibraryRepository(root))
          .buildManifest(AddonLibrarySnapshot("revision", listOf(withSymlink)))
      }
    assertEquals("invalidPath", symlinkError.code)
  }

  @Test
  fun rejectsUnsafeFoldersAndFileCountOverflow() = withRoot { root ->
    val unsafeFolder = File(root, "unit.frame")
    assertTrue(unsafeFolder.mkdirs())
    assertThrows(IllegalArgumentException::class.java) {
      AddonRuntimePayloadProvider(AddonLibraryRepository(root))
        .buildManifest(
          AddonLibrarySnapshot(
            "revision",
            listOf(
              AddonLibraryItem(
                title = "unsafe",
                folder = "unit.frame",
                version = "",
                interfaceVersion = "",
                notes = "",
                dependencies = emptyList(),
                sizeBytes = 0,
                filesCount = 0,
                enabled = true,
                importedAt = 0,
                warnings = emptyList(),
              )
            ),
          )
        )
    }
  }
}

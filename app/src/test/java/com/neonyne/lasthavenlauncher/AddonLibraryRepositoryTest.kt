package com.neonyne.lasthavenlauncher

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AddonLibraryRepositoryTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  @Test
  fun importDefaultsDisabledAndSupportsEnableExportAndRemove() {
    val repository = AddonLibraryRepository(temporaryFolder.newFolder("addons"))
    val archive =
      zipOf(
        "Sample/Sample.toc" to "## Title: Sample Addon\n## Interface: 30300\n",
        "Sample/main.lua" to "print('sample')",
      )

    val imported = repository.importZip(ByteArrayInputStream(archive), archive.size.toLong())

    assertEquals(1, imported.addons.size)
    assertFalse(imported.addons.single().enabled)
    assertEquals(2, imported.addons.single().filesCount)

    val enabled = repository.setEnabled("Sample", true)
    assertTrue(enabled.addons.single().enabled)
    assertTrue(repository.readLibrary().addons.single().enabled)

    val exported = ByteArrayOutputStream()
    repository.exportZip("Sample", exported)
    val exportedAddons =
      AddonArchive.importZip(
        ByteArrayInputStream(exported.toByteArray()),
        exported.size().toLong(),
        temporaryFolder.newFolder("export-check"),
      )
    assertEquals("Sample", exportedAddons.single().folder)

    val removed = repository.removeAddon("Sample")
    assertTrue(removed.addons.isEmpty())
    assertFalse(repository.addonDirectory("Sample").exists())
  }

  @Test
  fun replacingAddonPreservesItsEnabledState() {
    val repository = AddonLibraryRepository(temporaryFolder.newFolder("addons"))
    val first = zipOf("Sample/Sample.toc" to "## Version: 1\n")
    repository.importZip(ByteArrayInputStream(first), first.size.toLong())
    repository.setEnabled("Sample", true)

    val replacement = zipOf("Sample/Sample.toc" to "## Version: 2\n")
    val result = repository.importZip(ByteArrayInputStream(replacement), replacement.size.toLong())

    assertTrue(result.addons.single().enabled)
    assertEquals("2", result.addons.single().version)
  }

  @Test
  fun libraryRejectsIndexEntriesWithoutBackingFiles() {
    val repository = AddonLibraryRepository(temporaryFolder.newFolder("missing-files"))
    val archive = zipOf("Sample/Sample.toc" to "## Interface: 30300\\n")
    repository.importZip(ByteArrayInputStream(archive), archive.size.toLong())
    assertTrue(repository.addonDirectory("Sample").deleteRecursively())

    assertThrows(IllegalArgumentException::class.java) { repository.readLibrary() }
  }

  @Test
  fun removeRejectsUnknownAddonWithoutChangingLibrary() {
    val repository = AddonLibraryRepository(temporaryFolder.newFolder("unknown-remove"))
    val archive = zipOf("Sample/Sample.toc" to "## Interface: 30300\\n")
    repository.importZip(ByteArrayInputStream(archive), archive.size.toLong())

    assertThrows(IllegalArgumentException::class.java) { repository.removeAddon("Missing") }
    assertEquals(listOf("Sample"), repository.readLibrary().addons.map { it.folder })
  }

  @Test
  fun rejectsImportWhenThereIsNotEnoughSpaceForCommitStaging() {
    val root = temporaryFolder.newFolder("low-storage")
    val repository = AddonLibraryRepository(root, availableStorageBytes = { 0L })
    val archive = zipOf("Sample/Sample.toc" to "## Interface: 30300\\n")

    assertThrows(IllegalArgumentException::class.java) {
      repository.importZip(ByteArrayInputStream(archive), archive.size.toLong())
    }

    assertTrue(root.listFiles().orEmpty().isEmpty())
    assertTrue(repository.readLibrary().addons.isEmpty())
  }

  @Test
  fun restoresExistingAddonWhenIndexCommitFails() {
    val root = temporaryFolder.newFolder("rollback")
    val originalRepository = AddonLibraryRepository(root)
    val original =
      zipOf(
        "Sample/Sample.toc" to "## Version: 1\\n",
        "Sample/main.lua" to "print('original')",
      )
    originalRepository.importZip(ByteArrayInputStream(original), original.size.toLong())

    val replacement =
      zipOf(
        "Sample/Sample.toc" to "## Version: 2\\n",
        "Sample/main.lua" to "print('replacement')",
      )
    val failingRepository =
      AddonLibraryRepository(
        root,
        beforeIndexWrite = { throw IOException("forced index failure") },
      )

    assertThrows(IOException::class.java) {
      failingRepository.importZip(ByteArrayInputStream(replacement), replacement.size.toLong())
    }

    assertEquals("1", originalRepository.readLibrary().addons.single().version)
    assertEquals("print('original')", File(root, "Sample/main.lua").readText())
    assertTrue(root.listFiles().orEmpty().none { it.name.startsWith(".backup-") })
  }

  private fun zipOf(vararg files: Pair<String, String>): ByteArray {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zip ->
      for ((name, content) in files) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
      }
    }
    return output.toByteArray()
  }
}

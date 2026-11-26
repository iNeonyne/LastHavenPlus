package com.neonyne.lasthavenlauncher

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
      AddonArchive.importZip(ByteArrayInputStream(exported.toByteArray()), exported.size().toLong())
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

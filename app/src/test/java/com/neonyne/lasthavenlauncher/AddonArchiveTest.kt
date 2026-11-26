package com.neonyne.lasthavenlauncher

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AddonArchiveTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  @Test
  fun importsAddonFolderAndReadsTocMetadata() {
    val archive =
      zipOf(
        "Example/Example.toc" to
          "## Title: Example Addon\n## Version: 1.2.3\n## Interface: 30300\n## Notes: Test notes\n## Dependencies: LibOne, LibTwo\n",
        "Example/main.lua" to "print('example')",
        "Example/Media/icon.blp" to "image",
      )

    val imported =
      AddonArchive.importZip(
        ByteArrayInputStream(archive),
        archive.size.toLong(),
        temporaryFolder.newFolder(),
      )

    assertEquals(1, imported.size)
    assertEquals("Example Addon", imported.single().title)
    assertEquals("Example", imported.single().folder)
    assertEquals("1.2.3", imported.single().version)
    assertEquals("30300", imported.single().interfaceVersion)
    assertEquals(listOf("LibOne", "LibTwo"), imported.single().dependencies)
    assertEquals(setOf("Example.toc", "main.lua", "Media/icon.blp"), imported.single().files.keys)
    assertTrue(imported.single().warnings.isEmpty())
  }

  @Test
  fun importsFlatAddonContents() {
    val archive =
      zipOf(
        "Flat.toc" to "## Title: Flat addon\n## Interface: 30300\n",
        "Scripts/init.lua" to "print('flat')",
      )

    val imported =
      AddonArchive.importZip(
        ByteArrayInputStream(archive),
        archive.size.toLong(),
        temporaryFolder.newFolder(),
      )

    assertEquals("Flat", imported.single().folder)
    assertTrue(imported.single().files.containsKey("Scripts/init.lua"))
  }

  @Test
  fun reportsUnsupportedInterfaceAsWarning() {
    val archive = zipOf("Old/Old.toc" to "## Interface: 30400\n")

    val imported =
      AddonArchive.importZip(
        ByteArrayInputStream(archive),
        archive.size.toLong(),
        temporaryFolder.newFolder(),
      )

    assertEquals(1, imported.single().warnings.size)
  }

  @Test
  fun rejectsPathTraversalAndWindowsAbsolutePaths() {
    assertThrows(IllegalArgumentException::class.java) {
      AddonArchive.validateEntryPath("../outside.lua")
    }
    assertThrows(IllegalArgumentException::class.java) {
      AddonArchive.validateEntryPath("C:\\outside.lua")
    }
    assertThrows(IllegalArgumentException::class.java) {
      AddonArchive.validateEntryPath("folder/../../outside.lua")
    }
  }

  @Test
  fun rejectsReservedAndInvalidAddonFolderNames() {
    val archive = zipOf("Blizzard_Test/Blizzard_Test.toc" to "## Title: Blizzard\n")
    assertThrows(IllegalArgumentException::class.java) {
      AddonArchive.importZip(
        ByteArrayInputStream(archive),
        archive.size.toLong(),
        temporaryFolder.newFolder(),
      )
    }
  }

  @Test
  fun rejectsArchiveAboveCompressedSizeLimitBeforeReading() {
    val input =
      object : ByteArrayInputStream(byteArrayOf()) {
        override fun read(): Int = error("Input must not be read")
      }

    assertThrows(IllegalArgumentException::class.java) {
      AddonArchive.importZip(input, AddonArchive.MAX_ARCHIVE_BYTES + 1, temporaryFolder.newFolder())
    }
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

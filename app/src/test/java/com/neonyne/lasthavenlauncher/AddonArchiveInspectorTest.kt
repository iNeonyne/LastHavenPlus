package com.neonyne.lasthavenlauncher

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AddonArchiveInspectorTest {
    @Test
    fun parsesWowTocMetadataAndArchiveFingerprint() {
        val archive = createZip(
            "Interface/AddOns/Questie/Questie.toc" to
                "## Title: Questie\n## Version: 1.2.3\n## Interface: 30300\n## Dependencies: Ace3, LibDBIcon\n## OptionalDeps: TomTom\n",
            "Interface/AddOns/Questie/Questie.lua" to "print('questie')",
        )

        val result = AddonArchiveInspector.inspect(archive, "Questie.zip")

        assertEquals("Questie.zip", result.archiveName)
        assertEquals(2, result.entryCount)
        assertEquals(1, result.addons.size)
        assertEquals("Questie", result.addons.single().folderName)
        assertEquals("Questie", result.addons.single().title)
        assertEquals("1.2.3", result.addons.single().version)
        assertEquals(listOf(30300), result.addons.single().interfaceVersions)
        assertEquals(listOf("Ace3", "LibDBIcon"), result.addons.single().dependencies)
        assertEquals(listOf("TomTom"), result.addons.single().optionalDependencies)
        assertEquals(64, result.sha256.length)
    }

    @Test
    fun rejectsPathTraversal() {
        val archive = createZip("../outside.txt" to "not safe")

        assertRejected(archive, "traversal")
    }

    @Test
    fun rejectsCaseInsensitiveDuplicatePaths() {
        val archive = createZip(
            "Questie/Questie.toc" to "## Title: Questie\n",
            "questie/questie.TOC" to "## Title: Duplicate\n",
        )

        assertRejected(archive, "duplicate")
    }

    @Test
    fun rejectsArchivesWithoutTocFiles() {
        val archive = createZip("readme.txt" to "not an addon")

        assertRejected(archive, ".toc")
    }

    private fun assertRejected(file: File, expectedMessage: String) {
        try {
            AddonArchiveInspector.inspect(file)
            fail("Expected AddonArchiveException")
        } catch (error: AddonArchiveException) {
            assertTrue(error.message.orEmpty().contains(expectedMessage, ignoreCase = true))
        }
    }

    private fun createZip(vararg entries: Pair<String, String>): File {
        val directory = Files.createTempDirectory("addon-zip-test").toFile()
        val file = File(directory, "addon.zip")
        ZipArchiveOutputStream(file).use { zip ->
            entries.forEach { (name, content) ->
                zip.putArchiveEntry(ZipArchiveEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeArchiveEntry()
            }
            zip.finish()
        }
        file.deleteOnExit()
        directory.deleteOnExit()
        return file
    }
}

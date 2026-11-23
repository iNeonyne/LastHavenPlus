package com.lasthavens.android

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MigrationArchiveReaderTest {
    private data class Fixture(
        val archive: ByteArray,
        val toc: ByteArray,
        val lua: ByteArray,
    )

    private fun fixture(
        checksumOverride: String? = null,
        extraEntry: Pair<String, ByteArray>? = null,
    ): Fixture {
        val toc = "## Interface: 30300\n".toByteArray()
        val lua = "print('native migration')".toByteArray()
        val files = listOf("Example.toc" to toc, "Example.lua" to lua)
        val addonFiles = JSONArray()
        files.forEach { (path, bytes) ->
            addonFiles.put(
                JSONObject()
                    .put("addonFolder", "Example")
                    .put("storageFolder", "lh_generation")
                    .put("path", path)
                    .put("archivePath", "files/lh_generation/$path")
                    .put("size", bytes.size)
                    .put("sha256", if (path == "Example.lua" && checksumOverride != null) checksumOverride else bytes.sha256()),
            )
        }
        val addon = JSONObject()
            .put("folder", "Example")
            .put("storageFolder", "lh_generation")
            .put("version", "1.2.3")
            .put("enabled", true)
            .put("dependencies", JSONArray().put("Core"))
            .put("optionalDeps", JSONArray().put("Extras"))
            .put("size", toc.size + lua.size)
            .put("fileCount", 2)
        val manifest = JSONObject()
            .put("format", MigrationArchiveReader.FORMAT)
            .put("schema", MigrationArchiveReader.SCHEMA)
            .put("exportedAt", 1234)
            .put("sourceManagerVersion", "1.0.6")
            .put(
                "library",
                JSONObject()
                    .put("revision", "revision-1")
                    .put("addons", JSONArray().put(addon)),
            )
            .put(
                "preferences",
                JSONObject()
                    .put("settings", JSONObject().put("badge", false))
                    .put("language", "fr")
                    .put("browseInstalled", JSONObject()),
            )
            .put("files", addonFiles)
            .put(
                "totals",
                JSONObject()
                    .put("addons", 1)
                    .put("files", files.size)
                    .put("expandedBytes", toc.size + lua.size),
            )

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString().toByteArray())
            zip.closeEntry()
            files.forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry("files/lh_generation/$path"))
                zip.write(bytes)
                zip.closeEntry()
            }
            extraEntry?.let { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return Fixture(output.toByteArray(), toc, lua)
    }

    @Test
    fun stagesFilesAndPreservesLibraryMetadataAndPreferences() {
        val fixture = fixture()
        val root = Files.createTempDirectory("lh-migration-test").toFile()
        try {
            val stage = MigrationArchiveReader.stage(
                ByteArrayInputStream(fixture.archive),
                fixture.archive.size.toLong(),
                root,
            )
            assertEquals(2, stage.fileCount)
            assertEquals((fixture.toc.size + fixture.lua.size).toLong(), stage.expandedBytes)
            assertEquals("revision-1", stage.manifest.getJSONObject("library").getString("revision"))
            assertTrue(stage.manifest.getJSONObject("library").getJSONArray("addons").getJSONObject(0).getBoolean("enabled"))
            assertFalse(stage.manifest.getJSONObject("preferences").getJSONObject("settings").getBoolean("badge"))
            assertArrayEquals(fixture.toc, File(stage.directory, "files/lh_generation/Example.toc").readBytes())
            assertArrayEquals(fixture.lua, File(stage.directory, "files/lh_generation/Example.lua").readBytes())
            assertTrue(File(stage.directory, "manifest.json").isFile)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun rejectsChecksumMismatchAndRemovesPartialStaging() {
        val fixture = fixture(checksumOverride = "0".repeat(64))
        val root = Files.createTempDirectory("lh-migration-invalid").toFile()
        try {
            try {
                MigrationArchiveReader.stage(
                    ByteArrayInputStream(fixture.archive),
                    fixture.archive.size.toLong(),
                    root,
                )
                throw AssertionError("Expected checksum validation to fail")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message.orEmpty().contains("checksum"))
            }
            assertEquals(0, root.listFiles()?.size ?: 0)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun rejectsUnexpectedEntriesAndArchiveSizeOverLimit() {
        val fixture = fixture(extraEntry = "files/unexpected.txt" to "bad".toByteArray())
        val root = Files.createTempDirectory("lh-migration-extra").toFile()
        try {
            try {
                MigrationArchiveReader.stage(
                    ByteArrayInputStream(fixture.archive),
                    fixture.archive.size.toLong(),
                    root,
                )
                throw AssertionError("Expected unexpected entry validation to fail")
            } catch (expected: IllegalStateException) {
                assertTrue(expected.message.orEmpty().contains("Unexpected file"))
            }
            assertEquals(0, root.listFiles()?.size ?: 0)
            try {
                MigrationArchiveReader.stage(
                    ByteArrayInputStream(fixture.archive),
                    MigrationArchiveReader.MAX_ARCHIVE_BYTES + 1,
                    root,
                )
                throw AssertionError("Expected archive size validation to fail")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message.orEmpty().contains("archive size"))
            }
        } finally {
            root.deleteRecursively()
        }
    }

    private fun ByteArray.sha256(): String =
        MessageDigest.getInstance("SHA-256").digest(this)
            .joinToString("") { "%02x".format(it) }
}

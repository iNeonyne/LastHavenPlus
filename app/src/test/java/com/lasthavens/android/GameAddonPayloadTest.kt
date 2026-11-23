package com.lasthavens.android

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GameAddonPayloadTest {
    @Test
    fun includesOnlyEnabledAddonsAndKeepsBadgePreference() {
        val manifest = manifest(
            addons = JSONArray()
                .put(addon("Alpha", true, 3, 1))
                .put(addon("Beta", false, 4, 1)),
            files = JSONArray()
                .put(file("Alpha", "Alpha.toc", 3))
                .put(file("Beta", "Beta.toc", 4)),
            badge = false,
        )
        val result = GameAddonPayload.build(manifest)
        val addons = result.payload.getJSONArray("addons")
        assertEquals(1, addons.length())
        assertEquals("Alpha", addons.getJSONObject(0).getString("folder"))
        assertFalse(result.payload.getBoolean("badge"))
        assertEquals(3L, result.totalBytes)
    }

    @Test
    fun streamsFileInBoundedChunksAndRejectsTraversal() {
        val root = Files.createTempDirectory("game-addon-payload").toFile()
        try {
            val file = File(root, "files/Alpha/Alpha.toc").apply {
                parentFile.mkdirs()
                writeBytes(ByteArray(GameAddonPayload.CHUNK_BYTES + 3) { (it % 251).toByte() })
            }
            val ref = GameAddonPayload.FileRef("Alpha", "Alpha.toc", "files/Alpha/Alpha.toc", file.length())
            val first = GameAddonPayload.readChunk(root, ref, 0, GameAddonPayload.CHUNK_BYTES)
            val last = GameAddonPayload.readChunk(root, ref, GameAddonPayload.CHUNK_BYTES.toLong(), 10)
            assertEquals(GameAddonPayload.CHUNK_BYTES, first.size)
            assertEquals(3, last.size)
            assertTrue(first.contentEquals(file.readBytes().copyOfRange(0, GameAddonPayload.CHUNK_BYTES)))
            assertFalse(last.isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun placesEnabledDependenciesBeforeTheirDependents() {
        val dependent = addon("Dependent", true, 2, 1).put("dependencies", JSONArray().put("Base"))
        val manifest = manifest(
            addons = JSONArray()
                .put(dependent)
                .put(addon("Base", true, 1, 1)),
            files = JSONArray()
                .put(file("Dependent", "Dependent.toc", 2))
                .put(file("Base", "Base.toc", 1)),
        )
        val ordered = GameAddonPayload.build(manifest).payload.getJSONArray("addons")
        assertEquals("Base", ordered.getJSONObject(0).getString("folder"))
        assertEquals("Dependent", ordered.getJSONObject(1).getString("folder"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEnabledLibraryLargerThanBridgeLimit() {
        val manifest = manifest(
            addons = JSONArray()
                .put(addon("Alpha", true, 40L * 1024 * 1024, 1))
                .put(addon("Beta", true, 40L * 1024 * 1024, 1)),
            files = JSONArray()
                .put(file("Alpha", "Alpha.toc", 40L * 1024 * 1024))
                .put(file("Beta", "Beta.toc", 40L * 1024 * 1024)),
        )
        GameAddonPayload.build(manifest)
    }

    private fun manifest(addons: JSONArray, files: JSONArray, badge: Boolean = true) =
        JSONObject()
            .put("library", JSONObject().put("revision", "test-rev").put("addons", addons))
            .put("files", files)
            .put("preferences", JSONObject().put("settings", JSONObject().put("badge", badge)))

    private fun addon(folder: String, enabled: Boolean, size: Long, fileCount: Int) =
        JSONObject()
            .put("folder", folder)
            .put("storageFolder", folder)
            .put("enabled", enabled)
            .put("size", size)
            .put("fileCount", fileCount)

    private fun file(folder: String, path: String, size: Long) =
        JSONObject()
            .put("addonFolder", folder)
            .put("storageFolder", folder)
            .put("path", path)
            .put("archivePath", "files/$folder/$path")
            .put("size", size)
            .put("sha256", "0".repeat(64))
}

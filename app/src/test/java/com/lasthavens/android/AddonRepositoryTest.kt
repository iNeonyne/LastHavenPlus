package com.lasthavens.android

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AddonRepositoryTest {
    @Test
    fun activationPublishesCompleteGenerationAndKeepsSourceStage() {
        val root = Files.createTempDirectory("addon-repository").toFile()
        try {
            val stage = stage(root, "rev-1", "first")
            val repository = AddonRepository(root)
            val active = repository.activate(stage)
            assertEquals("rev-1", active.manifest.getJSONObject("library").getString("revision"))
            assertEquals("first", File(active.directory, "files/Test/Test.toc").readText())
            assertEquals("first", File(stage.directory, "files/Test/Test.toc").readText())
            assertEquals(active.generation, File(root, "addon-library/CURRENT").readText())
        } finally { root.deleteRecursively() }
    }

    @Test
    fun failedActivationLeavesPreviousGenerationActive() {
        val root = Files.createTempDirectory("addon-repository-rollback").toFile()
        try {
            val repository = AddonRepository(root)
            val first = repository.activate(stage(root, "rev-1", "first"))
            val brokenStage = stage(root, "rev-2", "second")
            File(brokenStage.directory, "manifest.json").writeText("{}")
            val broken = brokenStage.copy(manifest = JSONObject("""{"bad":true}"""))
            runCatching { repository.activate(broken) }
            val stillActive = repository.active()
            assertNotNull(stillActive)
            assertEquals(first.generation, stillActive!!.generation)
            assertEquals("first", File(stillActive.directory, "files/Test/Test.toc").readText())
        } finally { root.deleteRecursively() }
    }

    @Test
    fun identicalManifestActivationIsIdempotent() {
        val root = Files.createTempDirectory("addon-repository-idempotent").toFile()
        try {
            val repository = AddonRepository(root)
            val stage = stage(root, "rev-1", "same")
            val first = repository.activate(stage)
            val second = repository.activate(stage)
            assertEquals(first.generation, second.generation)
        } finally { root.deleteRecursively() }
    }

    private fun stage(root: File, revision: String, bytes: String): MigrationArchiveReader.Stage {
        val directory = File(root, "stage-$revision").apply { mkdirs() }
        val addonDir = File(directory, "files/Test").apply { mkdirs() }
        File(addonDir, "Test.toc").writeText(bytes)
        val manifest = JSONObject()
            .put("format", MigrationArchiveReader.FORMAT)
            .put("schema", MigrationArchiveReader.SCHEMA)
            .put("library", JSONObject().put("revision", revision).put("addons", org.json.JSONArray()))
            .put("files", org.json.JSONArray())
            .put("preferences", JSONObject())
            .put("totals", JSONObject().put("addons", 0).put("files", 0).put("expandedBytes", 0))
        File(directory, "manifest.json").writeText(manifest.toString())
        return MigrationArchiveReader.Stage(directory, manifest, 1, bytes.length.toLong())
    }
}

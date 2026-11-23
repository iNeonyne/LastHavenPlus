package com.lasthavens.android

import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.channels.FileChannel\nimport java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.UUID

/** Atomic generation-based native add-on storage. Previous generations are never deleted here. */
class AddonRepository(private val filesDir: File) {
    data class ActiveLibrary(val generation: String, val directory: File, val manifest: JSONObject)

    private val root = File(filesDir, "addon-library")
    private val generations = File(root, "generations")
    private val currentPointer = File(root, "CURRENT")

    init {
        require(root.exists() || root.mkdirs()) { "Could not create native add-on storage" }
        require(generations.exists() || generations.mkdirs()) { "Could not create add-on generations directory" }
    }

    @Synchronized
    fun active(): ActiveLibrary? {
        if (!currentPointer.isFile) return null
        val generation = currentPointer.readText(Charsets.UTF_8).trim()
        require(generation.matches(Regex("[A-Za-z0-9-]{1,80}"))) { "Native add-on pointer is invalid" }
        val directory = File(generations, generation).canonicalFile
        require(directory.parentFile == generations.canonicalFile && directory.isDirectory) {
            "Active native add-on generation is missing"
        }
        val manifestFile = File(directory, "manifest.json")
        require(manifestFile.isFile) { "Active native add-on manifest is missing" }
        return ActiveLibrary(generation, directory, JSONObject(manifestFile.readText(Charsets.UTF_8)))
    }

    /** Activates a verified stage only after its complete copy has been synced. */
    @Synchronized
    fun activate(stage: MigrationArchiveReader.Stage): ActiveLibrary {
        require(stage.directory.isDirectory) { "Verified migration stage is missing" }
        val source = stage.directory.canonicalFile
        val manifestFile = File(source, "manifest.json")
        require(manifestFile.isFile) { "Verified migration manifest is missing" }
        val sourceManifest = JSONObject(manifestFile.readText(Charsets.UTF_8))
        require(sourceManifest.toString() == stage.manifest.toString()) { "Migration stage metadata changed after verification" }

        val existing = active()
        val sourceDigest = sha256(manifestFile)
        if (existing != null && sha256(File(existing.directory, "manifest.json")) == sourceDigest) return existing

        val generation = "gen-" + UUID.randomUUID().toString()
        val temporary = File(generations, ".$generation.tmp")
        val destination = File(generations, generation)
        require(temporary.mkdir()) { "Could not create new add-on generation" }

        try {
            copyTree(source, temporary)
            syncDirectories(temporary)
            Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)

            val pointerTemp = File(root, ".CURRENT-$generation.tmp")
            FileOutputStream(pointerTemp).use { output ->
                output.write(generation.toByteArray(Charsets.UTF_8))
                output.flush()
                output.fd.sync()
            }
            try {
                Files.move(pointerTemp.toPath(), currentPointer.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                pointerTemp.delete()
            }
            return ActiveLibrary(generation, destination,
                JSONObject(File(destination, "manifest.json").readText(Charsets.UTF_8)))
        } catch (error: Exception) {
            temporary.deleteRecursively()
            val nowActive = runCatching { currentPointer.takeIf { it.isFile }?.readText()?.trim() }.getOrNull()
            if (nowActive != generation) destination.deleteRecursively()
            throw error
        }
    }

    private fun copyTree(source: File, destination: File) {
        val base = source.canonicalFile
        source.walkTopDown().forEach { file ->
            val canonical = file.canonicalFile
            require(canonical.path == base.path || canonical.path.startsWith(base.path + File.separator)) {
                "Migration stage contains a path outside its root"
            }
            require(!Files.isSymbolicLink(file.toPath())) { "Symbolic links are not allowed in add-on storage" }
            val relative = base.toPath().relativize(canonical.toPath()).toString()
            val target = if (relative.isEmpty()) destination else File(destination, relative)
            if (file.isDirectory) {
                require(target.exists() || target.mkdirs()) { "Could not create add-on storage directory" }
            } else {
                require(file.isFile) { "Unsupported entry in migration stage" }
                target.parentFile?.let { require(it.exists() || it.mkdirs()) }
                FileInputStream(file).use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                        output.flush()
                        output.fd.sync()
                    }
                }
            }
        }
    }

    private fun syncDirectories(directory: File) {
        directory.walkBottomUp().filter { it.isDirectory }.forEach { dir ->
            runCatching { FileChannel.open(dir.toPath(), StandardOpenOption.READ).use { it.force(true) } }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

package com.neonyne.lasthavenlauncher

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateFileCleanupTest {
    @Test
    fun removesAbandonedPartialFilesButPreservesVerifiedApks() {
        val directory = Files.createTempDirectory("lasthaven-updates").toFile()
        try {
            val apkPart = File(directory, "update-1000000.apk.part").apply { writeText("partial") }
            val patchPart = File(directory, "update-1000000.bsdiff.part").apply { writeText("partial") }
            val verifiedApk = File(directory, "LastHavenPlus-update-1000000.apk").apply {
                writeText("verified")
            }

            UpdateFileCleanup.removeAbandonedPartialFiles(directory)

            assertFalse(apkPart.exists())
            assertFalse(patchPart.exists())
            assertTrue(verifiedApk.exists())
        } finally {
            directory.deleteRecursively()
        }
    }
}

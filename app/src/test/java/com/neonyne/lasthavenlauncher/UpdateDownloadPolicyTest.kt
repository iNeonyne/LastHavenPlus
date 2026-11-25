package com.neonyne.lasthavenlauncher

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDownloadPolicyTest {
    @Test
    fun acceptsExactSizeAndDigest() {
        withTemporaryFile { file ->
            val payload = "verified update".toByteArray()
            UpdateDownloadPolicy.copyAndVerify(
                input = ByteArrayInputStream(payload),
                destination = file,
                expectedSize = payload.size.toLong(),
                maximumBytes = payload.size.toLong(),
                expectedSha256 = sha256(payload),
            )

            assertTrue(file.isFile)
            assertEquals(payload.size.toLong(), file.length())
            assertEquals("verified update", file.readText())
        }
    }

    @Test
    fun deletesTruncatedDownload() {
        withTemporaryFile { file ->
            try {
                UpdateDownloadPolicy.copyAndVerify(
                    input = ByteArrayInputStream("short".toByteArray()),
                    destination = file,
                    expectedSize = 10,
                    maximumBytes = 20,
                    expectedSha256 = sha256("short".toByteArray()),
                )
                throw AssertionError("Expected truncated download rejection")
            } catch (_: IllegalStateException) {
                assertFalse(file.exists())
            }
        }
    }

    @Test
    fun deletesPartialFileWhenNetworkReadIsInterrupted() {
        withTemporaryFile { file ->
            val interrupted = object : InputStream() {
                private var delivered = false

                override fun read(): Int = throw IOException("connection reset")

                override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                    if (delivered) throw IOException("connection reset")
                    delivered = true
                    val bytes = "partial".toByteArray()
                    System.arraycopy(bytes, 0, buffer, offset, bytes.size)
                    return bytes.size
                }
            }

            try {
                UpdateDownloadPolicy.copyAndVerify(
                    input = interrupted,
                    destination = file,
                    expectedSize = 100,
                    maximumBytes = 200,
                    expectedSha256 = "0".repeat(64),
                )
                throw AssertionError("Expected interrupted download rejection")
            } catch (_: IOException) {
                assertFalse(file.exists())
            }
        }
    }

    @Test
    fun deletesDownloadWithDigestMismatch() {
        withTemporaryFile { file ->
            val payload = "payload".toByteArray()
            try {
                UpdateDownloadPolicy.copyAndVerify(
                    input = ByteArrayInputStream(payload),
                    destination = file,
                    expectedSize = payload.size.toLong(),
                    maximumBytes = 100,
                    expectedSha256 = "0".repeat(64),
                )
                throw AssertionError("Expected digest mismatch rejection")
            } catch (_: IllegalStateException) {
                assertFalse(file.exists())
            }
        }
    }

    @Test
    fun deletesDownloadThatExceedsMaximumSize() {
        withTemporaryFile { file ->
            val payload = "larger than limit".toByteArray()
            try {
                UpdateDownloadPolicy.copyAndVerify(
                    input = ByteArrayInputStream(payload),
                    destination = file,
                    expectedSize = payload.size.toLong(),
                    maximumBytes = 4,
                    expectedSha256 = sha256(payload),
                )
                throw AssertionError("Expected size limit rejection")
            } catch (_: IllegalStateException) {
                assertFalse(file.exists())
            }
        }
    }

    private fun withTemporaryFile(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("lasthaven-download-test").toFile()
        try {
            block(File(directory, "update.apk.part"))
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
}

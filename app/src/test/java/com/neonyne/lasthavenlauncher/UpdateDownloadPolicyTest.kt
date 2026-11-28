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
  fun reportsMeasuredBytesAfterEachSuccessfulWrite() {
    withTemporaryFile { file ->
      val payload = "progress callback payload".toByteArray()
      val reported = mutableListOf<Long>()
      var verificationStarted = false
      UpdateDownloadPolicy.copyAndVerify(
        input = ByteArrayInputStream(payload),
        destination = file,
        expectedSize = payload.size.toLong(),
        maximumBytes = payload.size.toLong(),
        expectedSha256 = sha256(payload),
        onBytesCopied = { reported += it },
        onVerificationStarted = { verificationStarted = true },
      )

      assertTrue(verificationStarted)
      assertEquals(payload.size.toLong(), reported.sum())
      assertTrue(reported.isNotEmpty())
      assertTrue(reported.all { it > 0L })
    }
  }

  @Test
  fun handlesZeroLengthReadsWithoutDroppingBytes() {
    withTemporaryFile { file ->
      val payload = "zero-read-safe".toByteArray()
      val source =
        object : InputStream() {
          private var firstRead = true
          private var offset = 0

          override fun read(): Int =
            if (offset >= payload.size) -1 else payload[offset++].toInt() and 0xff

          override fun read(buffer: ByteArray, start: Int, length: Int): Int {
            if (firstRead) {
              firstRead = false
              return 0
            }
            if (offset >= payload.size) return -1
            val count = minOf(length, payload.size - offset)
            System.arraycopy(payload, offset, buffer, start, count)
            offset += count
            return count
          }
        }

      UpdateDownloadPolicy.copyAndVerify(
        input = source,
        destination = file,
        expectedSize = payload.size.toLong(),
        maximumBytes = payload.size.toLong(),
        expectedSha256 = sha256(payload),
      )

      assertEquals(payload.toList(), file.readBytes().toList())
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
      val interrupted = interruptedAfter("partial".toByteArray())
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
  fun interruptedStreamIsRemovedBeforeRetryAndVerifiedOnSuccess() {
    withTemporaryFile { file ->
      val payload = "complete verified payload".toByteArray()
      var attempts = 0
      UpdateNetworkPolicy.withDownloadRetry(
        destination = file,
        maxAttempts = 2,
        initialDelayMillis = 0,
        pause = {},
      ) {
        attempts++
        if (attempts == 1) {
          UpdateDownloadPolicy.copyAndVerify(
            input = interruptedAfter("partial".toByteArray()),
            destination = file,
            expectedSize = payload.size.toLong(),
            maximumBytes = 100,
            expectedSha256 = sha256(payload),
          )
        } else {
          assertFalse("partial file must be gone before retry", file.exists())
          UpdateDownloadPolicy.copyAndVerify(
            input = ByteArrayInputStream(payload),
            destination = file,
            expectedSize = payload.size.toLong(),
            maximumBytes = 100,
            expectedSha256 = sha256(payload),
          )
        }
      }

      assertEquals(2, attempts)
      assertEquals(payload.toList(), file.readBytes().toList())
      assertEquals(sha256(payload), sha256(file.readBytes()))
    }
  }

  @Test
  fun exhaustedInterruptedRetriesLeaveNoInstallableFile() {
    withTemporaryFile { file ->
      var attempts = 0
      try {
        UpdateNetworkPolicy.withDownloadRetry(
          destination = file,
          maxAttempts = 2,
          initialDelayMillis = 0,
          pause = {},
        ) {
          attempts++
          UpdateDownloadPolicy.copyAndVerify(
            input = interruptedAfter("partial".toByteArray()),
            destination = file,
            expectedSize = 100,
            maximumBytes = 200,
            expectedSha256 = "0".repeat(64),
          )
        }
        throw AssertionError("Expected exhausted interrupted downloads to fail")
      } catch (_: IOException) {
        assertEquals(2, attempts)
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

  private fun interruptedAfter(prefix: ByteArray): InputStream =
    object : InputStream() {
      private var delivered = false

      override fun read(): Int = throw IOException("connection reset")

      override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (delivered) throw IOException("connection reset")
        delivered = true
        System.arraycopy(prefix, 0, buffer, offset, prefix.size)
        return prefix.size
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
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

package com.neonyne.lasthavenlauncher

import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateNetworkPolicyTest {
  @Test
  fun retriesTransientIoFailuresWithBoundedBackoff() {
    var attempts = 0
    val delays = mutableListOf<Long>()
    val result =
      UpdateNetworkPolicy.withIoRetry(
        maxAttempts = 3,
        initialDelayMillis = 500,
        pause = { delays += it },
      ) {
        attempts++
        if (attempts < 3) throw java.io.IOException("temporary connection failure")
        "downloaded"
      }
    assertEquals("downloaded", result)
    assertEquals(3, attempts)
    assertEquals(listOf(500L, 1_000L), delays)
  }

  @Test
  fun downloadRetryDeletesPartialFileBeforeRetrying() {
    val directory = Files.createTempDirectory("updater-download-retry").toFile()
    try {
      val destination = File(directory, "update.apk")
      var attempts = 0
      val result =
        UpdateNetworkPolicy.withDownloadRetry(
          destination = destination,
          maxAttempts = 3,
          initialDelayMillis = 0,
          pause = {},
        ) {
          attempts++
          if (attempts == 1) {
            destination.writeBytes(byteArrayOf(1, 2, 3))
            throw java.io.IOException("connection aborted")
          }
          assertFalse("partial download must be removed before retry", destination.exists())
          destination.writeBytes(byteArrayOf(4, 5, 6))
          "complete"
        }

      assertEquals("complete", result)
      assertEquals(2, attempts)
      assertEquals(listOf<Byte>(4, 5, 6), destination.readBytes().toList())
    } finally {
      directory.deleteRecursively()
    }
  }

  @Test
  fun exhaustedDownloadRetriesRemovePartialFile() {
    val directory = Files.createTempDirectory("updater-download-exhausted").toFile()
    try {
      val destination = File(directory, "update.apk")
      var attempts = 0
      try {
        UpdateNetworkPolicy.withDownloadRetry(
          destination = destination,
          maxAttempts = 2,
          initialDelayMillis = 0,
          pause = {},
        ) {
          attempts++
          destination.writeBytes(byteArrayOf(1, 2, 3))
          throw java.io.IOException("connection aborted")
        }
        throw AssertionError("Expected exhausted download retry to fail")
      } catch (_: java.io.IOException) {
        assertEquals(2, attempts)
        assertFalse(destination.exists())
      }
    } finally {
      directory.deleteRecursively()
    }
  }

  @Test
  fun doesNotRetryAfterAttemptLimit() {
    var attempts = 0
    try {
      UpdateNetworkPolicy.withIoRetry(maxAttempts = 2, pause = {}) {
        attempts++
        throw java.io.IOException("connection aborted")
      }
      throw AssertionError("Expected final I/O failure")
    } catch (_: java.io.IOException) {
      assertEquals(2, attempts)
    }
  }

  @Test
  fun acceptsOnlyExactTrustedHttpsHostsForConnections() {
    assertEquals(
      "https://api.github.com/repos/iNeonyne/LastHavenPlus/releases/latest",
      UpdateNetworkPolicy.requireTrustedConnectionUrl(
        "https://api.github.com/repos/iNeonyne/LastHavenPlus/releases/latest"
      ),
    )
    assertEquals(
      "https://release-assets.githubusercontent.com/path/file.apk",
      UpdateNetworkPolicy.requireTrustedConnectionUrl(
        "https://release-assets.githubusercontent.com/path/file.apk"
      ),
    )
    listOf(
        "http://github.com/file.apk",
        "https://github.com.evil.example/file.apk",
        "https://evil.example/github.com/file.apk",
        "https://github.com@evil.example/file.apk",
        "https://github.com:444/file.apk",
        "https://github.com/file.apk#fragment",
        "https://api.github.com.evil.example/releases/latest",
      )
      .forEach { value ->
        try {
          UpdateNetworkPolicy.requireTrustedConnectionUrl(value)
          throw AssertionError("Expected untrusted URL rejection: $value")
        } catch (_: IllegalArgumentException) {
          // Expected.
        }
      }
  }

  @Test
  fun followsRelativeRedirectsOnlyToTrustedHttpsHosts() {
    assertEquals(
      "https://release-assets.githubusercontent.com/download/file.apk",
      UpdateNetworkPolicy.resolveTrustedRedirect(
        "https://github.com/repo/releases/latest",
        "https://release-assets.githubusercontent.com/download/file.apk",
      ),
    )
    assertEquals(
      "https://github.com/repo/releases/download/file.apk",
      UpdateNetworkPolicy.resolveTrustedRedirect(
        "https://github.com/repo/releases/latest",
        "download/file.apk",
      ),
    )
    listOf(
        "http://github.com/file.apk",
        "https://evil.example/file.apk",
        "//evil.example/file.apk",
        "https://github.com:444/file.apk",
        "",
      )
      .forEach { location ->
        try {
          UpdateNetworkPolicy.resolveTrustedRedirect(
            "https://github.com/repo/releases/latest",
            location,
          )
          throw AssertionError("Expected redirect rejection: $location")
        } catch (_: IllegalArgumentException) {
          // Expected.
        }
      }
  }

  @Test
  fun recognizesOnlyHttpRedirectStatusCodes() {
    listOf(301, 302, 303, 307, 308).forEach {
      assertTrue(UpdateNetworkPolicy.isRedirect(it))
    }
    listOf(200, 206, 400, 404, 500).forEach {
      assertFalse(UpdateNetworkPolicy.isRedirect(it))
    }
  }

  @Test
  fun boundsManifestAndReleaseResponseBodies() {
    assertEquals(
      "small manifest",
      UpdateNetworkPolicy.readLimitedText(ByteArrayInputStream("small manifest".toByteArray()), 32),
    )
    try {
      UpdateNetworkPolicy.readLimitedText(ByteArrayInputStream(ByteArray(33)), 32)
      throw AssertionError("Expected oversized response rejection")
    } catch (_: IllegalStateException) {
      // Expected.
    }
  }

  @Test
  fun rejectsInstalledBaseApkDigestMismatch() {
    UpdateNetworkPolicy.verifyBaseApkDigest("a".repeat(64), "A".repeat(64))
    try {
      UpdateNetworkPolicy.verifyBaseApkDigest("a".repeat(64), "b".repeat(64))
      throw AssertionError("Expected base APK digest mismatch rejection")
    } catch (_: IllegalStateException) {
      // Expected.
    }
  }

  @Test
  fun verifiesDownloadedSizeAndDigest() {
    UpdateNetworkPolicy.verifyDownload(10, 10, 100, "a".repeat(64), "A".repeat(64))
    listOf(
        { UpdateNetworkPolicy.verifyDownload(9, 10, 100, "a".repeat(64), "a".repeat(64)) },
        { UpdateNetworkPolicy.verifyDownload(10, 10, 9, "a".repeat(64), "a".repeat(64)) },
        { UpdateNetworkPolicy.verifyDownload(10, 10, 100, "a".repeat(64), "b".repeat(64)) },
      )
      .forEach { action ->
        try {
          action()
          throw AssertionError("Expected downloaded payload rejection")
        } catch (_: IllegalStateException) {
          // Expected.
        }
      }
  }
}

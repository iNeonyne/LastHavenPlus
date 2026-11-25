package com.neonyne.lasthavenlauncher

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URI

/**
 * Pure policy helpers for the release updater. Redirects are followed manually so every hop is
 * checked before any request is made to the destination host.
 */
internal object UpdateNetworkPolicy {
  private val trustedConnectionHosts =
    setOf(
      "api.github.com",
      "github.com",
      "release-assets.githubusercontent.com",
      "objects.githubusercontent.com",
    )

  fun requireGithubAssetUrl(value: String): String = requireHttpsUrl(value, setOf("github.com"))

  fun requireTrustedConnectionUrl(value: String): String =
    requireHttpsUrl(value, trustedConnectionHosts)

  private fun requireHttpsUrl(value: String, allowedHosts: Set<String>): String {
    val uri =
      try {
        URI(value)
      } catch (error: Exception) {
        throw IllegalArgumentException("Update URL is invalid", error)
      }
    val host = uri.host?.lowercase()
    require(uri.isAbsolute && uri.scheme.equals("https", ignoreCase = true)) {
      "Update URLs must use HTTPS"
    }
    require(!host.isNullOrBlank() && host in allowedHosts) {
      "Update URL host is not trusted"
    }
    require(uri.rawUserInfo == null && (uri.port == -1 || uri.port == 443)) {
      "Update URL contains unsupported authority information"
    }
    require(uri.rawFragment == null) { "Update URL must not contain a fragment" }
    return uri.toASCIIString()
  }

  fun resolveTrustedRedirect(currentUrl: String, location: String): String {
    require(location.isNotBlank()) { "Update server returned an empty redirect location" }
    val resolved =
      try {
        URI(currentUrl).resolve(URI(location)).normalize()
      } catch (error: Exception) {
        throw IllegalArgumentException("Update redirect URL is invalid", error)
      }
    return requireTrustedConnectionUrl(resolved.toASCIIString())
  }

  fun isRedirect(statusCode: Int): Boolean =
    statusCode == 301 ||
      statusCode == 302 ||
      statusCode == 303 ||
      statusCode == 307 ||
      statusCode == 308

  fun readLimitedText(input: InputStream, maximumBytes: Long): String {
    require(maximumBytes in 1..Int.MAX_VALUE.toLong()) { "Invalid response size limit" }
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0L
    while (true) {
      val count = input.read(buffer)
      if (count < 0) break
      if (count == 0) {
        val singleByte = input.read()
        if (singleByte < 0) break
        total++
        check(total <= maximumBytes) { "Update server response is too large" }
        output.write(singleByte)
        continue
      }
      total += count
      check(total <= maximumBytes) { "Update server response is too large" }
      output.write(buffer, 0, count)
    }
    return output.toString(Charsets.UTF_8.name())
  }

  fun <T> withDownloadRetry(
    destination: File,
    maxAttempts: Int = 3,
    initialDelayMillis: Long = 750L,
    pause: (Long) -> Unit = { Thread.sleep(it) },
    onRetry: (java.io.IOException, Int) -> Unit = { _, _ -> },
    action: () -> T,
  ): T =
    withIoRetry(
      maxAttempts = maxAttempts,
      initialDelayMillis = initialDelayMillis,
      pause = pause,
      onRetry = onRetry,
    ) {
      try {
        action()
      } catch (error: java.io.IOException) {
        // A retry must never append to or mistake a partial prior attempt for a complete file.
        destination.delete()
        throw error
      }
    }

  fun <T> withIoRetry(
    maxAttempts: Int = 3,
    initialDelayMillis: Long = 750L,
    pause: (Long) -> Unit = { Thread.sleep(it) },
    onRetry: (java.io.IOException, Int) -> Unit = { _, _ -> },
    action: () -> T,
  ): T {
    require(maxAttempts in 1..5) { "Invalid network retry count" }
    require(initialDelayMillis in 0L..5_000L) { "Invalid network retry delay" }
    var attempt = 1
    while (true) {
      try {
        return action()
      } catch (error: java.io.IOException) {
        if (attempt >= maxAttempts) throw error
        val nextAttempt = attempt + 1
        onRetry(error, nextAttempt)
        pause(initialDelayMillis * attempt)
        attempt = nextAttempt
      }
    }
  }

  fun verifyBaseApkDigest(actualSha256: String, expectedSha256: String) {
    check(actualSha256.equals(expectedSha256, ignoreCase = true)) {
      "Installed APK does not match the patch base"
    }
  }

  fun verifyDownload(
    actualSize: Long,
    expectedSize: Long,
    maximumBytes: Long,
    actualSha256: String,
    expectedSha256: String,
  ) {
    check(actualSize in 1..maximumBytes) { "Downloaded file is unexpectedly large or empty" }
    check(actualSize == expectedSize) { "Downloaded file size does not match its manifest" }
    check(actualSha256.equals(expectedSha256, ignoreCase = true)) {
      "Downloaded file checksum verification failed"
    }
  }
}

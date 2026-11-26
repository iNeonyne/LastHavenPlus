package com.neonyne.lasthavenlauncher

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * Streams an updater payload to a staging file and validates its exact size and
 * digest before the caller can use it. Any read, write or validation failure
 * removes the incomplete or untrusted destination.
 */
internal object UpdateDownloadPolicy {
  fun copyAndVerify(
    input: InputStream,
    destination: File,
    expectedSize: Long,
    maximumBytes: Long,
    expectedSha256: String,
  ) {
    try {
      val digest = MessageDigest.getInstance("SHA-256")
      var totalBytes = 0L
      input.use { source ->
        FileOutputStream(destination).use { output ->
          val buffer = ByteArray(64 * 1024)
          while (true) {
            val count = source.read(buffer)
            if (count < 0) break
            if (count == 0) {
              val singleByte = source.read()
              if (singleByte < 0) break
              totalBytes++
              check(totalBytes <= maximumBytes) {
                "Update file is unexpectedly large"
              }
              digest.update(singleByte.toByte())
              output.write(singleByte)
              continue
            }
            totalBytes += count
            check(totalBytes <= maximumBytes) {
              "Update file is unexpectedly large"
            }
            digest.update(buffer, 0, count)
            output.write(buffer, 0, count)
          }
          output.fd.sync()
        }
      }

      val actualSha256 = digest.digest().joinToString("") { "%02x".format(it) }
      UpdateNetworkPolicy.verifyDownload(
        actualSize = totalBytes,
        expectedSize = expectedSize,
        maximumBytes = maximumBytes,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
      )
    } catch (error: Exception) {
      destination.delete()
      throw error
    }
  }
}

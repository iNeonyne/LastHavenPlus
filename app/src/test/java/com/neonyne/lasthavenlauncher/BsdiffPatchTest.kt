package com.neonyne.lasthavenlauncher

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class BsdiffPatchTest {
    @Test
    fun reconstructsExactOutputFromBsdiff40Patch() {
        val directory = createTempDir(prefix = "bsdiff-test")
        try {
            val oldBytes = "abcXYZ".toByteArray(Charsets.UTF_8)
            val expected = "abc123XYZ!".toByteArray(Charsets.UTF_8)
            val oldFile = File(directory, "old.apk").apply { writeBytes(oldBytes) }
            val patchFile = File(directory, "update.bsdiff")
            val outputFile = File(directory, "new.apk")

            val control = compressed {
                writeOffset(3)
                writeOffset(3)
                writeOffset(0)
                writeOffset(3)
                writeOffset(1)
                writeOffset(0)
            }
            val differences = compressed {
                write(ByteArray(6))
            }
            val extra = compressed {
                write("123!".toByteArray(Charsets.UTF_8))
            }

            val header = ByteArrayOutputStream().apply {
                write("BSDIFF40".toByteArray(Charsets.US_ASCII))
                writeOffset(control.size.toLong())
                writeOffset(differences.size.toLong())
                writeOffset(expected.size.toLong())
            }.toByteArray()

            patchFile.writeBytes(header + control + differences + extra)
            val outputSize = BsdiffPatch.apply(oldFile, patchFile, outputFile, 1024)

            assertEquals(expected.size.toLong(), outputSize)
            assertArrayEquals(expected, outputFile.readBytes())
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun compressed(writeContent: ByteArrayOutputStream.() -> Unit): ByteArray {
        val bytes = ByteArrayOutputStream()
        BZip2CompressorOutputStream(bytes).use { compressed ->
            val payload = ByteArrayOutputStream().apply(writeContent).toByteArray()
            compressed.write(payload)
        }
        return bytes.toByteArray()
    }

    private fun ByteArrayOutputStream.writeOffset(value: Long) {
        var magnitude = kotlin.math.abs(value)
        val bytes = ByteArray(8)
        for (index in 0..7) {
            bytes[index] = (magnitude and 0xff).toByte()
            magnitude = magnitude ushr 8
        }
        if (value < 0) bytes[7] = (bytes[7].toInt() or 0x80).toByte()
        write(bytes)
    }
}

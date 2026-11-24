package com.neonyne.lasthavenlauncher

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * Streaming BSDIFF40 patch application. Reconstructs exact signed APK bytes;
 * the large old/new APK files are never loaded into a single byte array.
 */
internal object BsdiffPatch {
    fun apply(oldFile: File, patchFile: File, outputFile: File, maxOutputBytes: Long): Long {
        require(oldFile.isFile && oldFile.canRead()) { "Installed APK is not readable" }
        require(patchFile.isFile && patchFile.length() >= HEADER_SIZE) { "Invalid patch file" }
        require(maxOutputBytes > 0) { "Invalid output size limit" }

        outputFile.delete()
        try {
            FileInputStream(patchFile).use { patchInput ->
                val header = ByteArray(HEADER_SIZE)
                readFully(patchInput, header)
                require(header.copyOfRange(0, 8).contentEquals(MAGIC)) {
                    "Unsupported differential patch format"
                }

                val controlLength = readOffset(header, 8)
                val diffLength = readOffset(header, 16)
                val newSize = readOffset(header, 24)
                require(controlLength >= 0 && diffLength >= 0 && newSize > 0) {
                    "Invalid differential patch header"
                }
                require(newSize <= maxOutputBytes) { "Updated APK exceeds the size limit" }
                require(controlLength <= patchFile.length() - HEADER_SIZE) {
                    "Truncated differential patch"
                }
                require(diffLength <= patchFile.length() - HEADER_SIZE - controlLength) {
                    "Truncated differential patch"
                }

                val controlBytes = BoundedInputStream(patchInput, controlLength)
                val diffBytes = BoundedInputStream(patchInput, diffLength)
                val extraBytes = BoundedInputStream(
                    patchInput,
                    patchFile.length() - HEADER_SIZE - controlLength - diffLength,
                )
                val control = BZip2CompressorInputStream(controlBytes, true)
                val diff = BZip2CompressorInputStream(diffBytes, true)
                val extra = BZip2CompressorInputStream(extraBytes, true)

                try {
                    RandomAccessFile(oldFile, "r").use { oldApk ->
                        FileOutputStream(outputFile).use { output ->
                            val oldSize = oldApk.length()
                            var oldPosition = 0L
                            var newPosition = 0L
                            val diffBuffer = ByteArray(BUFFER_SIZE)
                            val oldBuffer = ByteArray(BUFFER_SIZE)
                            val outputBuffer = ByteArray(BUFFER_SIZE)

                            while (newPosition < newSize) {
                                val addLength = readOffset(control)
                                val copyLength = readOffset(control)
                                val seekAdjustment = readOffset(control)
                                require(addLength >= 0 && copyLength >= 0) {
                                    "Invalid differential patch control data"
                                }
                                require(addLength <= newSize - newPosition) {
                                    "Differential patch exceeds output size"
                                }

                                var remaining = addLength
                                while (remaining > 0) {
                                    val count = minOf(remaining, BUFFER_SIZE.toLong()).toInt()
                                    readFully(diff, diffBuffer, count)
                                    oldBuffer.fill(0, 0, count)

                                    val availableStart = maxOf(0L, oldPosition)
                                    val availableEnd = minOf(oldSize, oldPosition + count)
                                    if (availableEnd > availableStart) {
                                        val sourceOffset = (availableStart - oldPosition).toInt()
                                        val availableCount = (availableEnd - availableStart).toInt()
                                        oldApk.seek(availableStart)
                                        oldApk.readFully(oldBuffer, sourceOffset, availableCount)
                                    }

                                    for (index in 0 until count) {
                                        outputBuffer[index] =
                                            ((oldBuffer[index].toInt() and 0xff) +
                                                (diffBuffer[index].toInt() and 0xff)).toByte()
                                    }
                                    output.write(outputBuffer, 0, count)
                                    oldPosition += count
                                    newPosition += count
                                    remaining -= count
                                }

                                require(copyLength <= newSize - newPosition) {
                                    "Differential patch exceeds output size"
                                }
                                remaining = copyLength
                                while (remaining > 0) {
                                    val count = minOf(remaining, BUFFER_SIZE.toLong()).toInt()
                                    readFully(extra, outputBuffer, count)
                                    output.write(outputBuffer, 0, count)
                                    newPosition += count
                                    remaining -= count
                                }
                                oldPosition += seekAdjustment
                            }

                            require(newPosition == newSize) { "Reconstructed APK has an invalid size" }
                            require(control.read() == -1) { "Unexpected trailing control data" }
                            controlBytes.skipRemaining()
                            require(diff.read() == -1) { "Unexpected trailing difference data" }
                            diffBytes.skipRemaining()
                            require(extra.read() == -1) { "Unexpected trailing extra data" }
                            extraBytes.skipRemaining()
                            output.fd.sync()
                        }
                    }
                } finally {
                    control.close()
                    diff.close()
                    extra.close()
                }
            }
            require(outputFile.length() > 0) { "Reconstructed APK is empty" }
            return outputFile.length()
        } catch (error: Exception) {
            outputFile.delete()
            throw error
        }
    }

    private fun readOffset(input: InputStream): Long {
        val bytes = ByteArray(8)
        readFully(input, bytes)
        return readOffset(bytes, 0)
    }

    private fun readOffset(bytes: ByteArray, offset: Int): Long {
        var value = bytes[offset + 7].toLong() and 0x7f
        for (index in 6 downTo 0) {
            value = (value shl 8) or (bytes[offset + index].toLong() and 0xff)
        }
        return if ((bytes[offset + 7].toInt() and 0x80) != 0) -value else value
    }

    private fun readFully(input: InputStream, buffer: ByteArray) =
        readFully(input, buffer, 0, buffer.size)

    private fun readFully(input: InputStream, buffer: ByteArray, length: Int) =
        readFully(input, buffer, 0, length)

    private fun readFully(input: InputStream, buffer: ByteArray, offset: Int, length: Int) {
        var position = offset
        val end = offset + length
        while (position < end) {
            val count = input.read(buffer, position, end - position)
            if (count < 0) throw IllegalArgumentException("Truncated differential patch")
            if (count > 0) position += count
        }
    }

    private class BoundedInputStream(
        private val source: InputStream,
        private var remaining: Long,
    ) : InputStream() {
        override fun read(): Int {
            if (remaining <= 0) return -1
            val value = source.read()
            if (value >= 0) remaining--
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (remaining <= 0) return -1
            val count = source.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
            if (count > 0) remaining -= count
            return count
        }

        fun skipRemaining() {
            while (remaining > 0) {
                val skipped = source.skip(remaining)
                if (skipped > 0) {
                    remaining -= skipped
                } else {
                    if (source.read() < 0) throw IllegalArgumentException("Truncated differential patch")
                    remaining--
                }
            }
        }

        override fun close() = Unit
    }

    private const val HEADER_SIZE = 32
    private const val BUFFER_SIZE = 64 * 1024
    private val MAGIC = "BSDIFF40".toByteArray(Charsets.US_ASCII)
}

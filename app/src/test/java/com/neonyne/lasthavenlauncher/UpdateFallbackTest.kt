package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateFallbackTest {
    @Test
    fun failedDeltaFallsBackToFullApk() {
        var fullAttempted = false
        var reportedFailure = false

        val result = UpdateFallback.withFullFallback(
            preferDelta = true,
            deltaAttempt = { error("corrupt patch") },
            fullAttempt = {
                fullAttempted = true
                "verified-full-apk"
            },
            onDeltaFailure = { reportedFailure = it.message == "corrupt patch" },
        )

        assertEquals("verified-full-apk", result)
        assertTrue(fullAttempted)
        assertTrue(reportedFailure)
    }

    @Test
    fun successfulDeltaDoesNotDownloadFullApk() {
        var fullAttempted = false
        val result = UpdateFallback.withFullFallback(
            preferDelta = true,
            deltaAttempt = { "verified-delta-apk" },
            fullAttempt = {
                fullAttempted = true
                "full-apk"
            },
        )

        assertEquals("verified-delta-apk", result)
        assertFalse(fullAttempted)
    }

    @Test
    fun disabledDeltaUsesFullApkDirectly() {
        var deltaAttempted = false
        val result = UpdateFallback.withFullFallback(
            preferDelta = false,
            deltaAttempt = {
                deltaAttempted = true
                "delta"
            },
            fullAttempt = { "full" },
        )

        assertEquals("full", result)
        assertFalse(deltaAttempted)
    }

    @Test(expected = IllegalStateException::class)
    fun fullApkFailureIsNotSuppressed() {
        UpdateFallback.withFullFallback(
            preferDelta = true,
            deltaAttempt = { error("patch failed") },
            fullAttempt = { error("full APK failed") },
        )
    }
}

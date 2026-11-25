package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVersionPolicyTest {
    @Test
    fun convertsReleaseVersionToWorkflowVersionCode() {
        assertEquals(1_000_000L, UpdateVersionPolicy.versionCode("1.0.0"))
        assertEquals(2_003_014L, UpdateVersionPolicy.versionCode("v2.3.14"))
    }

    @Test
    fun comparesNumericVersionComponents() {
        assertTrue(UpdateVersionPolicy.compare("1.10.0", "1.9.99") > 0)
        assertTrue(UpdateVersionPolicy.compare("2.0.0", "2.0.0") == 0)
        assertTrue(UpdateVersionPolicy.compare("1.2.3", "1.2.4") < 0)
    }

    @Test
    fun rejectsMalformedAndOutOfRangeVersions() {
        assertFalse(UpdateVersionPolicy.isValid("v1.0"))
        assertFalse(UpdateVersionPolicy.isValid("1.0.0-beta"))
        try {
            UpdateVersionPolicy.versionCode("1000.0.0")
            throw AssertionError("Expected version component limit")
        } catch (_: IllegalStateException) {
            // Expected.
        }
    }

    @Test
    fun validatesAndNormalizesSha256() {
        val digest = "A".repeat(64)
        assertEquals("a".repeat(64), UpdateVersionPolicy.requireSha256(digest))
        try {
            UpdateVersionPolicy.requireSha256("not-a-digest")
            throw AssertionError("Expected invalid digest rejection")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun onlyAcceptsHttpsGithubReleaseAssetUrls() {
        assertEquals(
            "https://github.com/iNeonyne/LastHavenPlus/releases/download/v1.0.0/file.apk",
            UpdateVersionPolicy.requireGithubAssetUrl(
                "https://github.com/iNeonyne/LastHavenPlus/releases/download/v1.0.0/file.apk",
            ),
        )
        listOf(
            "http://github.com/file.apk",
            "https://evil.example/file.apk",
            "https://github.com.evil.example/file.apk",
        ).forEach { url ->
            try {
                UpdateVersionPolicy.requireGithubAssetUrl(url)
                throw AssertionError("Expected untrusted URL rejection: $url")
            } catch (_: IllegalArgumentException) {
                // Expected.
            }
        }
    }
}

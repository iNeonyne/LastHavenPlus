package com.neonyne.lasthavenlauncher

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateNetworkPolicyTest {
    @Test
    fun acceptsOnlyExactTrustedHttpsHostsForConnections() {
        assertEquals(
            "https://api.github.com/repos/iNeonyne/LastHavenPlus/releases/latest",
            UpdateNetworkPolicy.requireTrustedConnectionUrl(
                "https://api.github.com/repos/iNeonyne/LastHavenPlus/releases/latest",
            ),
        )
        assertEquals(
            "https://release-assets.githubusercontent.com/path/file.apk",
            UpdateNetworkPolicy.requireTrustedConnectionUrl(
                "https://release-assets.githubusercontent.com/path/file.apk",
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
        ).forEach { value ->
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
        ).forEach { location ->
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
        ).forEach { action ->
            try {
                action()
                throw AssertionError("Expected downloaded payload rejection")
            } catch (_: IllegalStateException) {
                // Expected.
            }
        }
    }
}

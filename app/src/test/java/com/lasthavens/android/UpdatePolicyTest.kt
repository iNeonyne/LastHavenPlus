package com.lasthavens.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {
    @Test fun acceptsOnlyPublishedStableReleaseTags() {
        assertTrue(UpdatePolicy.isPublishedStableRelease("v1.0.0", draft = false, prerelease = false))
        assertFalse(UpdatePolicy.isPublishedStableRelease("v1.0.0", draft = true, prerelease = false))
        assertFalse(UpdatePolicy.isPublishedStableRelease("v1.0.0", draft = false, prerelease = true))
        assertFalse(UpdatePolicy.isPublishedStableRelease("1.0.0", draft = false, prerelease = false))
        assertFalse(UpdatePolicy.isPublishedStableRelease("v1.0.0-rc.1", draft = false, prerelease = false))
        assertFalse(UpdatePolicy.isPublishedStableRelease("v1.0.0-beta", draft = false, prerelease = false))
        assertFalse(UpdatePolicy.isPublishedStableRelease("v01.0.0", draft = false, prerelease = false))
    }

    @Test fun permitsOnlyArm64DevicesAndReleaseAssetNames() {
        assertTrue(UpdatePolicy.supportsDevice(listOf("arm64-v8a", "armeabi-v7a")))
        assertFalse(UpdatePolicy.supportsDevice(listOf("armeabi-v7a")))
        assertFalse(UpdatePolicy.supportsDevice(listOf("x86_64")))
        assertTrue(UpdatePolicy.isExpectedReleaseApkName("1.0.0", "LastHavenPlus-1.0.0-arm64-v8a.apk"))
        assertFalse(UpdatePolicy.isExpectedReleaseApkName("1.0.0", "LastHavenPlus-1.0.0-x86_64.apk"))
        assertFalse(UpdatePolicy.isExpectedReleaseApkName("1.0.0", "../LastHavenPlus-1.0.0-arm64-v8a.apk"))
        assertFalse(UpdatePolicy.isExpectedReleaseApkName("1.0.0-rc.1", "LastHavenPlus-1.0.0-rc.1-arm64-v8a.apk"))
    }

    @Test fun enforcesAssetSizeAndNewerNumericVersionCode() {
        assertTrue(UpdatePolicy.isCompleteAsset(1024, 1024, 4096))
        assertFalse(UpdatePolicy.isCompleteAsset(1024, 1023, 4096))
        assertFalse(UpdatePolicy.isCompleteAsset(8192, 8192, 4096))
        assertFalse(UpdatePolicy.isCompleteAsset(0, 0, 4096))
        assertTrue(UpdatePolicy.isNewerVersion(231, 243))
        assertFalse(UpdatePolicy.isNewerVersion(243, 242))
        assertFalse(UpdatePolicy.isNewerVersion(243, 243))
        assertFalse(UpdatePolicy.isNewerVersion(0, 1))
    }

    @Test fun requiresExactNonemptySigningCertificateMatch() {
        val installed = setOf("a".repeat(64))
        assertTrue(UpdatePolicy.sameSigningCertificates(installed, installed))
        assertFalse(UpdatePolicy.sameSigningCertificates(installed, setOf("b".repeat(64))))
        assertFalse(UpdatePolicy.sameSigningCertificates(emptySet(), installed))
        assertFalse(UpdatePolicy.sameSigningCertificates(installed, emptySet()))
        assertFalse(UpdatePolicy.sameSigningCertificates(installed, installed + "c".repeat(64)))
    }

    @Test fun validatesChecksumDigestAndExactAssetFilename() {
        val digest = "ab".repeat(32)
        val name = "LastHavenPlus-1.0.0-arm64-v8a.apk"
        assertTrue(UpdatePolicy.isValidSha256(digest))
        assertFalse(UpdatePolicy.isValidSha256("abc"))
        assertTrue(UpdatePolicy.checksumFileMatches("$digest  $name", name, digest))
        assertTrue(UpdatePolicy.checksumFileMatches("$digest *$name", name, digest))
        assertFalse(UpdatePolicy.checksumFileMatches("$digest  other.apk", name, digest))
        assertFalse(UpdatePolicy.checksumFileMatches("$digest  $name", name, "bad"))
    }
}

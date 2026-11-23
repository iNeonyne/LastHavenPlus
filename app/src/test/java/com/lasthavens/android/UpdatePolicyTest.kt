package com.lasthavens.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {
    private fun run(
        branch: String = "main", event: String = "push", status: String = "completed",
        conclusion: String = "success", workflowId: Long = UpdatePolicy.WORKFLOW_ID,
        workflowPath: String = UpdatePolicy.WORKFLOW_PATH,
        repository: String = UpdatePolicy.REPOSITORY,
        sha: String = "0123456789abcdef0123456789abcdef01234567",
    ) = UpdateRun(12, 12, workflowId, branch, event, status, conclusion, workflowPath, repository, sha)

    @Test fun acceptsOnlySuccessfulCompletedMainPushesFromExpectedWorkflow() {
        assertTrue(UpdatePolicy.isEligibleRun(run()))
        assertFalse(UpdatePolicy.isEligibleRun(run(branch = "beta")))
        assertFalse(UpdatePolicy.isEligibleRun(run(event = "pull_request")))
        assertFalse(UpdatePolicy.isEligibleRun(run(status = "in_progress", conclusion = "")))
        assertFalse(UpdatePolicy.isEligibleRun(run(conclusion = "failure")))
        assertFalse(UpdatePolicy.isEligibleRun(run(workflowId = 1)))
        assertFalse(UpdatePolicy.isEligibleRun(run(workflowPath = ".github/workflows/other.yml")))
        assertFalse(UpdatePolicy.isEligibleRun(run(repository = "attacker/Lasthavens-Android")))
        assertFalse(UpdatePolicy.isEligibleRun(run(sha = "not-a-commit")))
    }

    @Test fun choosesDeviceAbiAndUniversalFallback() {
        val arm = UpdateApk("arm64-v8a", "arm.apk", "a".repeat(64), 100)
        val x64 = UpdateApk("x86_64", "x64.apk", "b".repeat(64), 100)
        val universal = UpdateApk("universal", "all.apk", "c".repeat(64), 100)
        assertEquals(arm, UpdatePolicy.selectApk(listOf("arm64-v8a", "x86_64"), listOf(arm, x64, universal)))
        assertEquals(x64, UpdatePolicy.selectApk(listOf("x86_64"), listOf(arm, x64, universal)))
        assertEquals(universal, UpdatePolicy.selectApk(listOf("riscv64"), listOf(arm, universal)))
        assertNull(UpdatePolicy.selectApk(listOf("riscv64"), listOf(arm, x64)))
    }

    @Test fun comparesNumericVersionCodesOnly() {
        assertTrue(UpdatePolicy.isNewerVersion(2, 167))
        assertFalse(UpdatePolicy.isNewerVersion(167, 166))
        assertFalse(UpdatePolicy.isNewerVersion(167, 167))
        assertFalse(UpdatePolicy.isNewerVersion(0, 1))
    }

    @Test fun requiresStableSemverAndSafeApkNames() {
        assertTrue(UpdatePolicy.isStableVersionName("2.0.0"))
        assertFalse(UpdatePolicy.isStableVersionName("2.1.0-beta.1"))
        assertFalse(UpdatePolicy.isStableVersionName("../2.0.0"))
        assertTrue(UpdatePolicy.isExpectedApkName("2.0.0", "arm64-v8a", "LastHavenPlus-2.0.0-arm64-v8a.apk"))
        assertFalse(UpdatePolicy.isExpectedApkName("2.0.0", "arm64-v8a", "../LastHavenPlus-2.0.0-arm64-v8a.apk"))
        assertFalse(UpdatePolicy.isExpectedApkName("2.0.0", "arm64-v8a", "LastHavenPlus-2.0.0-universal.apk"))
    }

    @Test fun validatesChecksumFormatAndFilename() {
        val digest = "ab".repeat(32)
        assertTrue(UpdatePolicy.isValidSha256(digest))
        assertFalse(UpdatePolicy.isValidSha256("abc"))
        assertTrue(UpdatePolicy.checksumFileMatches("$digest  LastHavenPlus-2.0.0-arm64-v8a.apk\n", "LastHavenPlus-2.0.0-arm64-v8a.apk", digest))
        assertFalse(UpdatePolicy.checksumFileMatches("$digest  wrong.apk\n", "LastHavenPlus-2.0.0-arm64-v8a.apk", digest))
        assertFalse(UpdatePolicy.checksumFileMatches("$digest  LastHavenPlus-2.0.0-arm64-v8a.apk\n", "LastHavenPlus-2.0.0-arm64-v8a.apk", "bad"))
    }
}

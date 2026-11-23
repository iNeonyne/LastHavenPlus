package com.lasthavens.android

internal data class UpdateRun(
    val id: Long, val runNumber: Long, val workflowId: Long, val branch: String,
    val event: String, val status: String, val conclusion: String,
    val workflowPath: String, val repository: String, val commitSha: String,
)
internal data class UpdateApk(val abi: String, val fileName: String, val sha256: String, val sizeBytes: Long)

internal object UpdatePolicy {
    const val WORKFLOW_ID = 427170801L
    const val WORKFLOW_PATH = ".github/workflows/build.yml"
    const val REPOSITORY = "iNeonyne/Lasthavens-Android"
    const val METADATA_ARTIFACT_NAME = "LastHavenPlus-Main-Update-Metadata"
    const val APK_ARTIFACT_PREFIX = "LastHavenPlus-Main-Update-"
    val supportedAbis = setOf("arm64-v8a")

    fun isEligibleRun(run: UpdateRun): Boolean =
        run.id > 0 && run.runNumber > 0 && run.workflowId == WORKFLOW_ID &&
            run.branch == "main" && run.event == "push" &&
            run.status == "completed" && run.conclusion == "success" &&
            run.workflowPath == WORKFLOW_PATH &&
            run.repository.equals(REPOSITORY, ignoreCase = true) &&
            run.commitSha.matches(Regex("[0-9a-fA-F]{40}"))

    fun selectApk(deviceAbis: List<String>, apks: List<UpdateApk>): UpdateApk? {
        if ("arm64-v8a" !in deviceAbis) return null
        return apks.singleOrNull { it.abi == "arm64-v8a" }
    }

    fun isEligibleArtifact(
        name: String,
        expectedName: String,
        expired: Boolean,
        artifactRunId: Long,
        artifactBranch: String,
        artifactCommitSha: String,
        expectedRun: UpdateRun,
    ): Boolean {
        val validExpectedName = expectedName == METADATA_ARTIFACT_NAME ||
            supportedAbis.any { expectedName == "$APK_ARTIFACT_PREFIX$it" }
        return validExpectedName && name == expectedName && !expired &&
            artifactRunId == expectedRun.id && artifactBranch == "main" &&
            artifactCommitSha == expectedRun.commitSha
    }

    fun isValidDownload(
        expectedSize: Long,
        actualSize: Long,
        maxSize: Long,
        expectedSha256: String,
        actualSha256: String,
        completed: Boolean,
    ): Boolean =
        completed && expectedSize in 1..maxSize && actualSize == expectedSize &&
            isValidSha256(expectedSha256) &&
            actualSha256.equals(expectedSha256, ignoreCase = true)

    fun isCompleteArtifact(expectedSize: Long, actualSize: Long, maxSize: Long): Boolean =
        expectedSize in 1..maxSize && actualSize == expectedSize

    fun sameSigningCertificates(installed: Set<String>, candidate: Set<String>): Boolean =
        installed.isNotEmpty() && candidate.isNotEmpty() && installed == candidate

    fun isNewerVersion(currentCode: Long, offeredCode: Long): Boolean =
        currentCode > 0 && offeredCode > currentCode

    fun isStableVersionName(value: String): Boolean =
        value.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))

    fun isValidSha256(value: String): Boolean =
        value.matches(Regex("[0-9a-fA-F]{64}"))

    fun isExpectedApkName(versionName: String, abi: String, fileName: String): Boolean =
        isStableVersionName(versionName) && abi in supportedAbis &&
            fileName == "LastHavenPlus-$versionName-$abi.apk"

    fun checksumFileMatches(contents: String, fileName: String, sha256: String): Boolean {
        if (!isValidSha256(sha256)) return false
        return contents.lineSequence().any { line ->
            val parts = line.trim().split(Regex("\\s+"), limit = 2)
            parts.size == 2 && parts[0].equals(sha256, ignoreCase = true) &&
                parts[1].removePrefix("*") == fileName
        }
    }
}

package com.neonyne.lasthavenlauncher

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateFileCleanupTest {
  private fun apkName(versionCode: Long, suffix: String = "12345678-1234-1234-1234-123456789abc") =
    "LastHavenPlus-update-$versionCode-$suffix.apk"

  @Test
  fun removesObsoleteVerifiedApksAndAbandonedPartialsButPreservesProtectedApks() {
    val directory = Files.createTempDirectory("lasthaven-updates").toFile()
    try {
      val obsoleteApk = File(directory, apkName(1)).apply { writeText("obsolete") }
      val handoffApk = File(directory, apkName(2)).apply { writeText("installer") }
      val pendingApk = File(directory, apkName(3)).apply { writeText("pending") }
      val apkPart = File(directory, "update-4.apk.part").apply { writeText("partial") }
      val patchPart = File(directory, "update-4.bsdiff.part").apply { writeText("partial") }
      val unrelatedApk = File(directory, "user-export.apk").apply { writeText("user") }

      UpdateFileCleanup.removeObsoleteFiles(
        directory,
        setOf(handoffApk.name, pendingApk.name),
      )

      assertFalse(obsoleteApk.exists())
      assertTrue(handoffApk.exists())
      assertTrue(pendingApk.exists())
      assertFalse(apkPart.exists())
      assertFalse(patchPart.exists())
      assertTrue(unrelatedApk.exists())
    } finally {
      directory.deleteRecursively()
    }
  }

  @Test
  fun onlyExactUpdaterApkNamesAreEligibleForCleanup() {
    assertTrue(UpdateFileCleanup.isManagedApkName(apkName(123456)))
    assertFalse(UpdateFileCleanup.isManagedApkName("LastHavenPlus-update-123456.apk"))
    assertFalse(UpdateFileCleanup.isManagedApkName("LastHavenPlus-update-123456-not-a-uuid.apk"))
    assertFalse(
      UpdateFileCleanup.isManagedApkName(
        "../LastHavenPlus-update-123456-12345678-1234-1234-1234-123456789abc.apk"
      )
    )
  }

  @Test
  fun validatesPendingInstallerUriAuthorityRootAndFilename() {
    val name = apkName(123456)
    assertEquals(
      name,
      UpdateFileCleanup.managedApkName(
        "content",
        "com.neonyne.lasthavenlauncher.updates",
        listOf("update_apks", name),
        "com.neonyne.lasthavenlauncher",
      ),
    )
    assertNull(
      UpdateFileCleanup.managedApkName(
        "content",
        "another.package.updates",
        listOf("update_apks", name),
        "com.neonyne.lasthavenlauncher",
      )
    )
    assertNull(
      UpdateFileCleanup.managedApkName(
        "content",
        "com.neonyne.lasthavenlauncher.updates",
        listOf("other_root", name),
        "com.neonyne.lasthavenlauncher",
      )
    )
    assertNull(
      UpdateFileCleanup.managedApkName(
        "file",
        "com.neonyne.lasthavenlauncher.updates",
        listOf("update_apks", name),
        "com.neonyne.lasthavenlauncher",
      )
    )
  }

  @Test
  fun protectedNamesIncludePendingHandoffAndCrashRecoveryCandidateOnlyWhenManaged() {
    val pending = apkName(1)
    val handoff = apkName(2)
    val candidate = apkName(3)

    assertEquals(
      setOf(pending, handoff, candidate),
      UpdateFileCleanup.protectedApkNames(
        pendingUriName = pending,
        handoffApkName = handoff,
        candidateApkName = candidate,
      ),
    )
    assertEquals(
      setOf(handoff),
      UpdateFileCleanup.protectedApkNames(
        pendingUriName = "untrusted.apk",
        handoffApkName = handoff,
        candidateApkName = "../escape.apk",
      ),
    )
  }

  @Test
  fun cleanupDoesNotTraverseDirectoriesOrFollowSymlinks() {
    val directory = Files.createTempDirectory("lasthaven-updates").toFile()
    try {
      val nested = File(directory, "nested").apply { mkdir() }
      val nestedApk = File(nested, apkName(10)).apply { writeText("nested") }
      val targetApk = File(directory, apkName(11)).apply { writeText("target") }
      val link = File(directory, apkName(12))
      Files.createSymbolicLink(link.toPath(), targetApk.toPath())

      UpdateFileCleanup.removeObsoleteFiles(directory, setOf(targetApk.name))

      assertTrue(nestedApk.exists())
      assertTrue(targetApk.exists())
      assertTrue(Files.isSymbolicLink(link.toPath()))
    } finally {
      directory.deleteRecursively()
    }
  }
}

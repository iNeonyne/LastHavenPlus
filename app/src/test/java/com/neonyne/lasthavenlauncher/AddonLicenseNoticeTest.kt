package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonLicenseNoticeTest {
  @Test
  fun mitNoticeIsPackagedAsAnAppAsset() {
    val notice = File("src/main/assets/licenses/ADDON_MANAGER_0.5.1_MIT.txt")

    assertTrue(notice.isFile)
    val content = notice.readText()
    assertTrue(content.contains("MIT License"))
    assertTrue(content.contains("Lasthavens Addon Manager contributors"))
  }
}

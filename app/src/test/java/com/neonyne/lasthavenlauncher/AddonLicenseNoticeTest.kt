package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonLicenseNoticeTest {
  @Test
  fun mitNoticeIsPackagedAsAnAppAsset() {
    val notice = File("src/main/assets", ThirdPartyNotices.ADDON_MANAGER_NOTICE_ASSET)

    assertTrue(notice.isFile)
    val content = notice.readText()
    assertTrue(content.contains("MIT License"))
    assertTrue(content.contains("Lasthavens Addon Manager contributors"))
    assertEquals(notice.length(), content.length.toLong())
  }
}

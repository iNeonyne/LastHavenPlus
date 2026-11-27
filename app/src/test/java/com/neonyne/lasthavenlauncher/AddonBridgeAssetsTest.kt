package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the deterministic APK packaging of the bundled addon-bridge extension assets. */
class AddonBridgeAssetsTest {
  private val assetsDir =
    File("src/main/assets").takeIf { it.isDirectory } ?: File("app/src/main/assets")

  @Test
  fun `bridge assets exist at the documented location`() {
    val dir = File(assetsDir, AddonBridgePolicy.BUILTIN_LOCATION.removePrefix("/"))
    assertTrue(dir.isDirectory)
    assertTrue(File(dir, "manifest.json").isFile)
    assertTrue(File(dir, "background.js").isFile)
  }

  @Test
  fun `manifest declares exactly the launcher-owned extension identity`() {
    val manifest = File(assetsDir, "extensions/addon-bridge/manifest.json").readText()
    assertTrue(manifest.contains("\"id\": \"${AddonBridgePolicy.BRIDGE_EXTENSION_ID}\""))
    assertTrue(manifest.contains("\"manifest_version\": 2"))
    assertTrue(manifest.contains("\"version\": \"0.5.1\""))
  }

  @Test
  fun `manifest requests required GeckoView messaging permissions only`() {
    val manifest = File(assetsDir, "extensions/addon-bridge/manifest.json").readText()
    assertTrue(manifest.contains("\"geckoViewAddons\""))
    assertTrue(manifest.contains("\"nativeMessaging\""))
    assertFalse("content-script native messaging is not needed", manifest.contains("nativeMessagingFromContent"))
    listOf("tabs", "cookies", "webRequest", "downloads", "history", "bookmarks", "<all_urls>")
      .forEach {
        assertFalse("manifest must not request $it", manifest.contains("\"$it\""))
      }
    assertFalse("manifest must not declare content scripts", manifest.contains("content_scripts"))
  }

  @Test
  fun `assets stay within the deterministic size bounds`() {
    val dir = File(assetsDir, AddonBridgePolicy.BUILTIN_LOCATION.removePrefix("/"))
    val files = dir.walkTopDown().filter { it.isFile }.toList()
    assertTrue(files.size in 2..8)
    files.forEach { assertTrue("${it.name} too large", it.length() <= 64 * 1024) }
    assertEquals(2, files.size)
  }

  @Test
  fun `background script uses only the launcher native namespace`() {
    val background = File(assetsDir, "extensions/addon-bridge/background.js").readText()
    assertTrue(background.contains(AddonBridgePolicy.NATIVE_MESSAGING_NAME))
    assertFalse(background.contains("eval("))
    assertFalse(background.contains("XMLHttpRequest"))
    assertNotNull(background)
  }
}

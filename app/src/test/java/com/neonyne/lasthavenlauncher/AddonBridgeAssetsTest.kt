package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    assertTrue(File(dir, "runtime-core.js").isFile)
    assertTrue(File(dir, "bridge.js").isFile)
  }

  @Test
  fun `manifest declares exactly the launcher-owned extension identity`() {
    val manifest = File(assetsDir, "extensions/addon-bridge/manifest.json").readText()
    assertTrue(manifest.contains("\"id\": \"${AddonBridgePolicy.BRIDGE_EXTENSION_ID}\""))
    assertTrue(manifest.contains("\"manifest_version\": 2"))
    assertTrue(manifest.contains("\"version\": \"0.5.1\""))
    assertTrue(manifest.contains("\"run_at\": \"document_start\""))
    assertTrue(manifest.contains("\"https://lasthavens.com/play\""))
    assertTrue(manifest.contains("\"https://lasthavens.com/play/\""))
    assertTrue(manifest.contains("\"runtime-core.js\""))
    assertTrue(manifest.contains("\"bridge.js\""))
    assertFalse(manifest.contains("\"<all_urls>\""))
    assertFalse(manifest.contains("\"all_frames\": true"))
    val parsed = org.json.JSONObject(manifest)
    val scripts = parsed.getJSONArray("content_scripts")
    assertEquals(1, scripts.length())
    val script = scripts.getJSONObject(0)
    assertEquals("document_start", script.getString("run_at"))
    assertFalse(script.getBoolean("all_frames"))
    val matches = script.getJSONArray("matches")
    assertEquals(2, matches.length())
    assertEquals("https://lasthavens.com/play", matches.getString(0))
    assertEquals("https://lasthavens.com/play/", matches.getString(1))
  }

  @Test
  fun `manifest requests required GeckoView messaging permissions only`() {
    val manifest = File(assetsDir, "extensions/addon-bridge/manifest.json").readText()
    assertTrue(manifest.contains("\"geckoViewAddons\""))
    assertTrue(manifest.contains("\"nativeMessaging\""))
    assertFalse(
      "content-script native messaging is not needed",
      manifest.contains("nativeMessagingFromContent"),
    )
    listOf("tabs", "cookies", "webRequest", "downloads", "history", "bookmarks", "<all_urls>")
      .forEach {
        assertFalse("manifest must not request $it", manifest.contains("\"$it\""))
      }
    assertTrue("manifest must declare the exact-route content script", manifest.contains("content_scripts"))
  }

  @Test
  fun `assets stay within the deterministic size bounds`() {
    val dir = File(assetsDir, AddonBridgePolicy.BUILTIN_LOCATION.removePrefix("/"))
    val files = dir.walkTopDown().filter { it.isFile }.toList()
    assertTrue(files.size in 2..8)
    files.forEach { assertTrue("${it.name} too large", it.length() <= 64 * 1024) }
    assertEquals(4, files.size)
  }

  @Test
  fun `background script uses only the launcher native namespace`() {
    val background = File(assetsDir, "extensions/addon-bridge/background.js").readText()
    assertTrue(background.contains(AddonBridgePolicy.NATIVE_MESSAGING_NAME))
    assertTrue(background.contains("function scheduleReconnect()"))
    assertTrue(background.contains("MAX_RECONNECT_DELAY_MS = 30000"))
    assertTrue(background.contains("nextPort.onDisconnect"))
    assertTrue(background.contains("stalePort.disconnect()"))
    assertTrue(background.contains('requestNative("ping")'))
    assertTrue(background.contains("MAX_PENDING_REQUESTS = 8"))
    assertTrue(background.contains("MAX_RUNTIME_BUNDLE_BYTES = 16 * 1024 * 1024"))
    assertTrue(background.contains("getFileChunk"))
    assertFalse(background.contains("eval("))
    assertFalse(background.contains("XMLHttpRequest"))
  }
}

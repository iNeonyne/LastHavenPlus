package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Compile-time API guard for the exact GeckoView surface the addon bridge depends on (issue #167).
 * This test does not install an extension or claim runtime behavior.
 */
class AddonBridgeRuntimeApiTest {
  @Test
  fun `runtime exposes the web extension controller used by the bridge`() {
    val runtime = Class.forName("org.mozilla.geckoview.GeckoRuntime")
    val getter =
      runtime.methods.firstOrNull {
        it.name == "getWebExtensionController" &&
          it.parameterTypes.isEmpty() &&
          it.returnType == Class.forName("org.mozilla.geckoview.WebExtensionController")
      }
    assertNotNull("GeckoRuntime must expose getWebExtensionController()", getter)
  }

  @Test
  fun `controller supports deterministic built-in installation`() {
    val controller = Class.forName("org.mozilla.geckoview.WebExtensionController")
    val ensureBuiltIn =
      controller.methods.firstOrNull {
        it.name == "ensureBuiltIn" &&
          it.parameterTypes.contentEquals(arrayOf(String::class.java, String::class.java))
      }
    assertNotNull("WebExtensionController must expose ensureBuiltIn(String, String)", ensureBuiltIn)
    assertTrue(
      ensureBuiltIn!!.genericReturnType.typeName.startsWith("org.mozilla.geckoview.GeckoResult")
    )
  }

  @Test
  fun `message sender exposes the identity fields the bridge validates`() {
    val sender = Class.forName("org.mozilla.geckoview.WebExtension\$MessageSender")
    assertNotNull(sender.getField("webExtension"))
    assertNotNull(sender.getField("session"))
    assertNotNull(sender.getField("url"))
    assertNotNull(sender.getField("environmentType"))
    val extension = Class.forName("org.mozilla.geckoview.WebExtension")
    assertNotNull(extension.getField("id"))
    assertNotNull(extension.getField("isBuiltIn"))
  }

  @Test
  fun `message delegate supports namespace registration and port connect`() {
    val delegate = Class.forName("org.mozilla.geckoview.WebExtension\$MessageDelegate")
    assertNotNull(delegate.methods.firstOrNull { it.name == "onConnect" })
    val onMessage = delegate.methods.firstOrNull { it.name == "onMessage" }
    assertNotNull(onMessage)
    assertEquals(String::class.java, onMessage!!.parameterTypes[0])
  }
}

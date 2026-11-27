package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertTrue
import org.junit.Test

/** API surface guard for the GeckoView native port used by the addon bridge. */
class GeckoViewWebExtensionPortApiTest {
  @Test
  fun pinnedArtifactSupportsNativePortLifecycle() {
    val port = Class.forName("org.mozilla.geckoview.WebExtension\$Port")
    val delegate = Class.forName("org.mozilla.geckoview.WebExtension\$PortDelegate")

    assertTrue(hasMethod(port, "setDelegate", delegate))
    assertTrue(hasMethod(port, "postMessage", org.json.JSONObject::class.java))
    assertTrue(hasMethod(delegate, "onPortMessage", Any::class.java, port))
    assertTrue(hasMethod(delegate, "onDisconnect", port))
  }

  private fun hasMethod(clazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Boolean =
    clazz.methods.any {
      it.name == name && it.parameterTypes.contentEquals(parameterTypes)
    }
}

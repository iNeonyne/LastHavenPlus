package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertNotNull
import org.junit.Test

/** API surface guard for the GeckoView native port used by the addon bridge. */
class GeckoViewWebExtensionPortApiTest {
  @Test
  fun pinnedArtifactSupportsNativePortLifecycle() {
    val port = Class.forName("org.mozilla.geckoview.WebExtension\$Port")
    val delegate = Class.forName("org.mozilla.geckoview.WebExtension\$PortDelegate")

    assertNotNull(
      port.methods.firstOrNull {
        it.name == "setDelegate" && it.parameterTypes.contentEquals(arrayOf(delegate))
      },
    )
    assertNotNull(
      port.methods.firstOrNull { it.name == "postMessage" && it.parameterTypes.size == 1 },
    )
    assertNotNull(
      delegate.methods.firstOrNull { it.name == "onPortMessage" && it.parameterTypes.size == 2 },
    )
    assertNotNull(
      delegate.methods.firstOrNull { it.name == "onDisconnect" && it.parameterTypes.size == 1 },
    )
  }
}

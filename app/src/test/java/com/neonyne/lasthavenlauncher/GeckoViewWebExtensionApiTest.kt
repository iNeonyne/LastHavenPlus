package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Compile/runtime reflection guard for the exact GeckoView dependency pinned by this project.
 *
 * This test checks only the API surface required by issue #23. It does not install an extension
 * or claim that an XPI is integrated; runtime behavior remains a separate test gate.
 */
class GeckoViewWebExtensionApiTest {
    @Test
    fun pinnedArtifactSupportsBundledExtensionLifecycleAndNavigation() {
        val controller = Class.forName("org.mozilla.geckoview.WebExtensionController")
        val installBuiltIn = controller.methods.firstOrNull {
            it.name == "installBuiltIn" &&
                it.parameterTypes.contentEquals(arrayOf(String::class.java))
        }
        assertNotNull("GeckoView must expose installBuiltIn(String)", installBuiltIn)

        val ensureBuiltIn = controller.methods.firstOrNull {
            it.name == "ensureBuiltIn" &&
                it.parameterTypes.contentEquals(arrayOf(String::class.java, String::class.java))
        }
        assertNotNull("GeckoView must expose ensureBuiltIn(String, String)", ensureBuiltIn)
        assertEquals("org.mozilla.geckoview.GeckoResult", ensureBuiltIn!!.returnType.name)

        val extension = Class.forName("org.mozilla.geckoview.WebExtension")
        val tabDelegate = Class.forName("org.mozilla.geckoview.WebExtension\$TabDelegate")
        val setTabDelegate = extension.methods.firstOrNull {
            it.name == "setTabDelegate" &&
                it.parameterTypes.contentEquals(arrayOf(tabDelegate))
        }
        assertNotNull("WebExtension must allow an app-owned TabDelegate", setTabDelegate)

        val onNewTab = tabDelegate.methods.firstOrNull { it.name == "onNewTab" }
        assertNotNull("TabDelegate must expose onNewTab", onNewTab)
        assertEquals(2, onNewTab!!.parameterTypes.size)
        assertEquals("org.mozilla.geckoview.GeckoResult", onNewTab.returnType.name)

        val onOpenOptionsPage = tabDelegate.methods.firstOrNull { it.name == "onOpenOptionsPage" }
        assertNotNull("TabDelegate must expose onOpenOptionsPage", onOpenOptionsPage)

        val metadata = Class.forName("org.mozilla.geckoview.WebExtension\$MetaData")
        listOf("baseUrl", "optionsPageUrl", "openOptionsPageInTab").forEach { field ->
            assertNotNull("WebExtension.MetaData.$field must exist", metadata.getField(field))
        }
    }

    @Test
    fun pinnedArtifactSupportsPrivilegedNativeMessaging() {
        val extension = Class.forName("org.mozilla.geckoview.WebExtension")
        val messageDelegate = Class.forName("org.mozilla.geckoview.WebExtension\$MessageDelegate")
        val setMessageDelegate = extension.methods.firstOrNull {
            it.name == "setMessageDelegate" &&
                it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == messageDelegate &&
                it.parameterTypes[1] == String::class.java
        }
        assertNotNull("WebExtension must expose native message delegate registration", setMessageDelegate)

        val onMessage = messageDelegate.methods.firstOrNull { it.name == "onMessage" }
        assertNotNull("MessageDelegate must expose onMessage", onMessage)

    val port = Class.forName("org.mozilla.geckoview.WebExtension\$Port")
    val portDelegate = Class.forName("org.mozilla.geckoview.WebExtension\$PortDelegate")
    assertNotNull(
        "WebExtension.Port must support registering a native-message delegate",
        port.methods.firstOrNull {
            it.name == "setDelegate" &&
                it.parameterTypes.contentEquals(arrayOf(portDelegate))
        },
    )
    assertNotNull(
        "WebExtension.Port must support sending native messages",
        port.methods.firstOrNull {
            it.name == "postMessage" && it.parameterTypes.size == 1
        },
    )
    assertNotNull(
        "PortDelegate must receive messages from the extension",
        portDelegate.methods.firstOrNull {
            it.name == "onPortMessage" && it.parameterTypes.size == 2
        },
    )
    assertNotNull(
        "PortDelegate must receive native-port disconnects",
        portDelegate.methods.firstOrNull {
            it.name == "onDisconnect" && it.parameterTypes.size == 1
        },
    )
    }
}

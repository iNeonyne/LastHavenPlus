package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.geckoview.WebExtension

class AddonBridgeSenderPolicyTest {
  private val gameSessionToken = Any()
  private val otherSessionToken = Any()

  private fun allowed(
    extensionId: String? = AddonBridgePolicy.BRIDGE_EXTENSION_ID,
    isBuiltIn: Boolean? = true,
    environmentType: Int = WebExtension.MessageSender.ENV_TYPE_EXTENSION,
    senderUrl: String? = null,
    senderSession: Any? = null,
    expectedSession: Any? = gameSessionToken,
  ): Boolean =
    AddonBridgePolicy.allowsSender(
      extensionId = extensionId,
      isBuiltIn = isBuiltIn,
      environmentType = environmentType,
      senderUrl = senderUrl,
      senderSession = senderSession,
      expectedSession = expectedSession,
    )

  @Test
  fun `bundled built-in extension environment is allowed`() {
    assertTrue(allowed())
    assertTrue(allowed(senderUrl = "moz-extension://00000000-0000-0000-0000-000000000000/"))
  }

  @Test
  fun `wrong extension id is rejected`() {
    assertFalse(allowed(extensionId = "other@neonyne"))
    assertFalse(allowed(extensionId = null))
  }

  @Test
  fun `non built-in or unknown provenance is rejected`() {
    assertFalse(allowed(isBuiltIn = false))
    assertFalse(allowed(isBuiltIn = null))
  }

  @Test
  fun `content script and unknown environments are rejected`() {
    assertFalse(allowed(environmentType = WebExtension.MessageSender.ENV_TYPE_CONTENT_SCRIPT))
    assertFalse(allowed(environmentType = 0))
  }

  @Test
  fun `session-bound sender must match the expected session exactly`() {
    val gameUrl = "https://lasthavens.com/play"
    assertTrue(allowed(senderSession = gameSessionToken, senderUrl = gameUrl))
    assertFalse(allowed(senderSession = otherSessionToken, senderUrl = gameUrl))
    assertFalse(allowed(senderSession = gameSessionToken, expectedSession = null))
  }

  @Test
  fun `session-bound sender must present the exact game origin and path`() {
    assertFalse(allowed(senderSession = gameSessionToken, senderUrl = null))
    assertFalse(
      allowed(senderSession = gameSessionToken, senderUrl = "https://lasthavens.com/play/x")
    )
    assertFalse(allowed(senderSession = gameSessionToken, senderUrl = "http://lasthavens.com/play"))
    assertFalse(allowed(senderSession = gameSessionToken, senderUrl = "https://evil.com/play"))
    assertFalse(
      allowed(senderSession = gameSessionToken, senderUrl = "https://sub.lasthavens.com/play")
    )
  }

  @Test
  fun `credentials ports and encoded path tricks are rejected`() {
    assertFalse(
      allowed(senderSession = gameSessionToken, senderUrl = "https://user:pass@lasthavens.com/play")
    )
    assertFalse(
      allowed(senderSession = gameSessionToken, senderUrl = "https://lasthavens.com:8443/play")
    )
    assertFalse(
      allowed(senderSession = gameSessionToken, senderUrl = "https://lasthavens.com/play/../admin")
    )
    assertFalse(
      allowed(senderSession = gameSessionToken, senderUrl = "https://lasthavens.com/%2fplay")
    )
  }
}

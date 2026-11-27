package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AddonBridgeProtocolTest {
  @Test
  fun `parses known message types`() {
    assertEquals(
      AddonBridgeProtocol.AddonBridgeRequest("ping", "r-1"),
      AddonBridgeProtocol.parse(mapOf("type" to "ping", "requestId" to "r-1")),
    )
    assertEquals(
      AddonBridgeProtocol.AddonBridgeRequest("listAddons", "r-2"),
      AddonBridgeProtocol.parse(mapOf("type" to "listAddons", "requestId" to "r-2")),
    )
    assertEquals(
      AddonBridgeProtocol.AddonBridgeRequest("getLoadOrder", "r-3"),
      AddonBridgeProtocol.parse(mapOf("type" to "getLoadOrder", "requestId" to "r-3")),
    )
  }

  @Test
  fun `rejects unknown or malformed fields`() {
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "ping", "requestId" to "r", "extra" to 1)))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "ping")))
    assertNull(AddonBridgeProtocol.parse(mapOf("requestId" to "r")))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "evalJs", "requestId" to "r")))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to 5, "requestId" to "r")))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "ping", "requestId" to 42)))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "ping", "requestId" to "bad id!")))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "ping", "requestId" to "x".repeat(65))))
    assertNull(AddonBridgeProtocol.parse(mapOf("type" to "ping", "requestId" to "")))
  }

  @Test
  fun `oversized message field count is rejected`() {
    val fields =
      (1..AddonBridgeProtocol.MAX_MESSAGE_FIELDS).associate { "k$it" to 1 } + ("type" to "ping")
    assertNull(AddonBridgeProtocol.parse(fields))
  }

  @Test
  fun `ack and error builders validate input`() {
    assertEquals("ack", AddonBridgeProtocol.ack("r-1")!!.toFields()["type"])
    assertNull(AddonBridgeProtocol.ack("bad id!"))
    assertNull(AddonBridgeProtocol.error("r-1", "notACode"))
    assertEquals("invalidRequest", AddonBridgeProtocol.error("r-1", "invalidRequest")!!.code)
  }

  @Test
  fun `addon list enforces bounds and item text`() {
    val summary = AddonBridgeProtocol.AddonSummary("unit-frame", "0.5.1", true)
    assertNotNull(AddonBridgeProtocol.addonList("r-1", "rev1", listOf(summary)))
    assertNull(
      AddonBridgeProtocol.addonList(
        "r-1",
        "rev1",
        List(AddonBridgeProtocol.MAX_RESPONSE_ITEMS + 1) { summary },
      )
    )
    assertNull(
      AddonBridgeProtocol.addonList(
        "r-1",
        "rev1",
        listOf(AddonBridgeProtocol.AddonSummary("../escape", "0.5.1", true)),
      )
    )
    assertNull(
      AddonBridgeProtocol.addonList(
        "r-1",
        "rev1",
        listOf(
          AddonBridgeProtocol.AddonSummary("ok", "0.5.1", true),
          AddonBridgeProtocol.AddonSummary("a".repeat(129), "0.5.1", false),
        ),
      )
    )
  }

  @Test
  fun `load order enforces bounds and item text`() {
    assertNotNull(AddonBridgeProtocol.loadOrder("r-1", "rev1", listOf("a"), listOf("b")))
    assertNull(AddonBridgeProtocol.loadOrder("r-1", "rev1", List(257) { "addon$it" }, emptyList()))
    assertNull(AddonBridgeProtocol.loadOrder("r-1", "rev1", List(129) { "addon$it" }, List(128) { "blocked$it" }))
    assertNull(AddonBridgeProtocol.loadOrder("r-1", "rev1", listOf(".."), emptyList()))
    assertNull(AddonBridgeProtocol.loadOrder("r-1", "rev1", listOf("."), emptyList()))
    assertNull(AddonBridgeProtocol.loadOrder("r-1", "rev1", listOf("../escape"), emptyList()))
    assertNull(
      AddonBridgeProtocol.loadOrder(
        "r-1",
        "rev1",
        listOf("a".repeat(AddonBridgeProtocol.MAX_ITEM_CHARS + 1)),
        emptyList(),
      )
    )
  }

  @Test
  fun `response byte count is bounded`() {
    assertTrue(AddonBridgeProtocol.isResponseWithinByteLimit(0))
    assertTrue(AddonBridgeProtocol.isResponseWithinByteLimit(AddonBridgeProtocol.MAX_RESPONSE_BYTES))
    assertFalse(AddonBridgeProtocol.isResponseWithinByteLimit(AddonBridgeProtocol.MAX_RESPONSE_BYTES + 1))
    assertFalse(AddonBridgeProtocol.isResponseWithinByteLimit(-1))
  }

  @Test
  fun `responses serialize to flat bounded field maps`() {
    val ack = AddonBridgeProtocol.ack("r-1")!!
    assertFalse(ack.toFields().values.any { it is Map<*, *> || it is List<*> })

    val list =
      AddonBridgeProtocol.addonList(
        "r-2",
        "rev1",
        listOf(AddonBridgeProtocol.AddonSummary("a", "1", false)),
      )!!
    assertEquals("listAddons", list.toFields()["type"])
    assertEquals("rev1", list.toFields()["revision"])

    val order = AddonBridgeProtocol.loadOrder("r-3", "rev1", listOf("a", "b"), listOf("c"))!!
    assertEquals(listOf("a", "b"), order.toFields()["loadOrder"])
    assertEquals(listOf("c"), order.toFields()["blocked"])
  }
}

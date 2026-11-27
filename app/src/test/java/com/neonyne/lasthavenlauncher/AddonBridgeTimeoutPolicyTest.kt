package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonBridgeTimeoutPolicyTest {
  @Test
  fun `requests expire only after the timeout window`() {
    val createdAt = 1_000L
    assertFalse(AddonBridgeTimeoutPolicy.isExpired(createdAt, createdAt))
    assertFalse(
      AddonBridgeTimeoutPolicy.isExpired(
        createdAt,
        createdAt + AddonBridgeTimeoutPolicy.REQUEST_TIMEOUT_MS,
      )
    )
    assertTrue(
      AddonBridgeTimeoutPolicy.isExpired(
        createdAt,
        createdAt + AddonBridgeTimeoutPolicy.REQUEST_TIMEOUT_MS + 1,
      )
    )
  }

  @Test
  fun `pending request set is bounded`() {
    assertTrue(AddonBridgeTimeoutPolicy.canAcceptPendingRequest(0))
    assertTrue(
      AddonBridgeTimeoutPolicy.canAcceptPendingRequest(
        AddonBridgeTimeoutPolicy.MAX_PENDING_REQUESTS - 1
      )
    )
    assertFalse(
      AddonBridgeTimeoutPolicy.canAcceptPendingRequest(
        AddonBridgeTimeoutPolicy.MAX_PENDING_REQUESTS
      )
    )
  }
}

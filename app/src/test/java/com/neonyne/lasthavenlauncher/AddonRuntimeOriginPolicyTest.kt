package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonRuntimeOriginPolicyTest {
  @Test
  fun allowsOnlyExactHttpsGameRoute() {
    assertTrue(AddonRuntimeOriginPolicy.allows("https://lasthavens.com/play"))
    assertTrue(AddonRuntimeOriginPolicy.allows("https://lasthavens.com/play/"))
    assertTrue(AddonRuntimeOriginPolicy.allows("https://LASTHAVENS.COM/play?realm=1#game"))
    assertTrue(AddonRuntimeOriginPolicy.allows("https://lasthavens.com:443/play"))
    assertEquals(
      AddonRuntimeOriginPolicy.GAME_ORIGIN,
      AddonRuntimeOriginPolicy.canonicalOrigin("https://lasthavens.com/play"),
    )
  }

  @Test
  fun rejectsOtherHostsSchemesPortsAndRoutes() {
    listOf(
        "http://lasthavens.com/play",
        "https://www.lasthavens.com/play",
        "https://lasthavens.com.evil.example/play",
        "https://user@lasthavens.com/play",
        "https://lasthavens.com:444/play",
        "https://lasthavens.com/",
        "https://lasthavens.com/player",
        "https://lasthavens.com/play/extra",
        "https://lasthavens.com/%70lay",
        "https://lasthavens.com/%2fplay",
        "not a url",
        "",
      )
      .forEach { assertFalse("Must reject $it", AddonRuntimeOriginPolicy.allows(it)) }
  }

  @Test
  fun rejectsOversizedUrlInput() {
    assertFalse(AddonRuntimeOriginPolicy.allows("https://lasthavens.com/play?" + "x".repeat(4096)))
  }
}

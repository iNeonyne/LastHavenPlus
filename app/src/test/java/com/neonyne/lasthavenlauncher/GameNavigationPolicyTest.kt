package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameNavigationPolicyTest {
  @Test
  fun allowsOnlyTheTwoConfiguredGameOrigins() {
    assertTrue(GameNavigationPolicy.allows("https://lasthavens.com/play"))
    assertTrue(GameNavigationPolicy.allows("https://alt.lasthavens.com/play"))
    assertTrue(GameNavigationPolicy.allows("HTTPS://LASTHAVENS.COM/auth/callback?state=ok"))
    assertTrue(GameNavigationPolicy.allows("https://alt.lasthavens.com/anything/path?code=abc"))
    assertTrue(GameNavigationPolicy.allows("https://lasthavens.com:443/login"))
  }

  @Test
  fun rejectsUnverifiedExternalHostsAndLookalikes() {
    assertFalse(GameNavigationPolicy.allows("https://login.example.com/auth/callback"))
    assertFalse(GameNavigationPolicy.allows("https://cdn.third-party.net/assets/game.js"))
    assertFalse(GameNavigationPolicy.allows("https://discord.gg/lasthavens"))
    assertFalse(GameNavigationPolicy.allows("https://stripe.com/checkout"))
    assertFalse(GameNavigationPolicy.allows("https://auth.lasthavens.com/login"))
    assertFalse(GameNavigationPolicy.allows("https://evil-lasthavens.com/play"))
    assertFalse(GameNavigationPolicy.allows("https://lasthavens.com.example.net/play"))
    assertFalse(GameNavigationPolicy.allows("https://lasthavens.com./play"))
    assertFalse(GameNavigationPolicy.allows("https://127.0.0.1/play"))
  }

  @Test
  fun allowsPathsAndQueryStringsOnApprovedOrigins() {
    assertTrue(GameNavigationPolicy.allows("https://lasthavens.com/auth/callback?state=ok"))
    assertTrue(GameNavigationPolicy.allows("https://alt.lasthavens.com/play?realm=main&lang=en"))
  }

  @Test
  fun rejectsUnsafeSchemes() {
    assertFalse(GameNavigationPolicy.allows("http://lasthavens.com/play"))
    assertFalse(GameNavigationPolicy.allows("javascript:alert(1)"))
    assertFalse(GameNavigationPolicy.allows("data:text/html,hello"))
    assertFalse(GameNavigationPolicy.allows("file:///data/local/tmp/page.html"))
    assertFalse(GameNavigationPolicy.allows("intent://lasthavens.com/play"))
  }

  @Test
  fun rejectsEmbeddedCredentials() {
    assertFalse(GameNavigationPolicy.allows("https://user:password@lasthavens.com/play"))
    assertFalse(GameNavigationPolicy.allows("https://user@alt.lasthavens.com/login"))
  }

  @Test
  fun rejectsUnexpectedPorts() {
    assertFalse(GameNavigationPolicy.allows("https://lasthavens.com:80/play"))
    assertFalse(GameNavigationPolicy.allows("https://alt.lasthavens.com:444/play"))
  }

  @Test
  fun rejectsMalformedEmptyAndOversizedUrls() {
    assertFalse(GameNavigationPolicy.allows(""))
    assertFalse(GameNavigationPolicy.allows("   "))
    assertFalse(GameNavigationPolicy.allows("https:///play"))
    assertFalse(GameNavigationPolicy.allows("https://lasthavens.com:invalid/play"))
    assertFalse(GameNavigationPolicy.allows("https://lasthavens.com/" + "a".repeat(4096)))
  }
}

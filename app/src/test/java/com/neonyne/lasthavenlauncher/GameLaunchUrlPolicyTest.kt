package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class GameLaunchUrlPolicyTest {
  @Test
  fun normalizesSupportedHostsToPlayPath() {
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://lasthavens.com"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://lasthavens.com/"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://lasthavens.com/old-route"))
    assertEquals(ALTERNATE_GAME_URL, GameLaunchUrlPolicy.normalize("https://alt.lasthavens.com"))
    assertEquals(ALTERNATE_GAME_URL, GameLaunchUrlPolicy.normalize("https://alt.lasthavens.com/"))
    assertEquals(ALTERNATE_GAME_URL, GameLaunchUrlPolicy.normalize("https://alt.lasthavens.com/old-route"))
  }

  @Test
  fun rejectsUnsupportedOrUnsafePersistedUrlsToDefault() {
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize(null))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize(""))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("http://lasthavens.com/play"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://evil-lasthavens.com/play"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://lasthavens.com.evil.net/play"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://user@lasthavens.com/play"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://lasthavens.com:444/play"))
    assertEquals(DEFAULT_GAME_URL, GameLaunchUrlPolicy.normalize("https://lasthavens.com/play?redirect=evil"))
  }
}

package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherNavigationPolicyTest {
  @Test
  fun backFromGameOpensOverlayWithoutLeavingGame() {
    val target =
      LauncherNavigationPolicy.onBack(
        currentScreen = Screen.Game,
        menuVisible = false,
        settingsReturnScreen = Screen.Game,
        diagnosticsReturnScreen = Screen.Settings,
        updateReturnScreen = Screen.Game,
      )

    assertEquals(Screen.Game, target.screen)
    assertTrue(target.menuVisible)
  }

  @Test
  fun backFromOverlayDismissesIt() {
    val target =
      LauncherNavigationPolicy.onBack(
        currentScreen = Screen.Game,
        menuVisible = true,
        settingsReturnScreen = Screen.Game,
        diagnosticsReturnScreen = Screen.Settings,
        updateReturnScreen = Screen.Game,
      )

    assertEquals(Screen.Game, target.screen)
    assertFalse(target.menuVisible)
  }

  @Test
  fun backFromSettingsReturnsToGame() {
    val target =
      LauncherNavigationPolicy.onBack(
        currentScreen = Screen.Settings,
        menuVisible = false,
        settingsReturnScreen = Screen.Game,
        diagnosticsReturnScreen = Screen.Settings,
        updateReturnScreen = Screen.Game,
      )

    assertEquals(Screen.Game, target.screen)
    assertFalse(target.menuVisible)
  }

  @Test
  fun menuActionsReturnToGameAndCloseOverlay() {
    assertEquals(
      LauncherNavigationTarget(Screen.Game, false),
      LauncherNavigationPolicy.afterReload(),
    )
    assertEquals(
      LauncherNavigationTarget(Screen.Game, false),
      LauncherNavigationPolicy.returnToGame(),
    )
  }

  @Test
  fun settingsOpensOutsideImmersiveGameplay() {
    assertEquals(
      LauncherNavigationTarget(Screen.Settings, false),
      LauncherNavigationPolicy.openSettings(),
    )
    assertTrue(LauncherNavigationPolicy.isGameplay(Screen.Game))
    assertFalse(LauncherNavigationPolicy.isImmersiveGameplay(Screen.Game, true))
    assertFalse(LauncherNavigationPolicy.isGameplay(Screen.Settings))
  }
}

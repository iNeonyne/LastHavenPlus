package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherNavigationPolicyTest {
  private val defaultState = LauncherNavigationState()

  @Test
  fun backFromGameOpensOverlayWithoutLeavingGame() {
    val next = LauncherNavigationPolicy.onBack(defaultState)

    assertEquals(Screen.Game, next.screen)
    assertTrue(next.menuVisible)
  }

  @Test
  fun backFromOverlayDismissesIt() {
    val next = LauncherNavigationPolicy.onBack(defaultState.copy(menuVisible = true))

    assertEquals(Screen.Game, next.screen)
    assertFalse(next.menuVisible)
  }

  @Test
  fun backFromSettingsReturnsToGame() {
    val next =
      LauncherNavigationPolicy.onBack(
        defaultState.copy(screen = Screen.Settings, menuVisible = true),
      )

    assertEquals(Screen.Game, next.screen)
    assertFalse(next.menuVisible)
  }

  @Test
  fun backReturnsToEachRecordedReturnDestination() {
    val fromSettings =
      LauncherNavigationPolicy.onBack(
        defaultState.copy(screen = Screen.Settings, settingsReturnScreen = Screen.Home),
      )
    val fromDiagnostics =
      LauncherNavigationPolicy.onBack(
        defaultState.copy(screen = Screen.Diagnostics, diagnosticsReturnScreen = Screen.Settings),
      )
    val fromUpdate =
      LauncherNavigationPolicy.onBack(
        defaultState.copy(screen = Screen.Update, updateReturnScreen = Screen.Home),
      )

    assertEquals(Screen.Home, fromSettings.screen)
    assertEquals(Screen.Settings, fromDiagnostics.screen)
    assertEquals(Screen.Home, fromUpdate.screen)
  }

  @Test
  fun backFromAddonManagerAndHomeReturnsToGame() {
    val fromAddonManager =
      LauncherNavigationPolicy.onBack(defaultState.copy(screen = Screen.AddonManager))
    val fromHome = LauncherNavigationPolicy.onBack(defaultState.copy(screen = Screen.Home))

    assertEquals(Screen.Game, fromAddonManager.screen)
    assertEquals(Screen.Game, fromHome.screen)
  }

  @Test
  fun explicitBackButtonsPreserveOverlayState() {
    val state = defaultState.copy(screen = Screen.Update, menuVisible = true)

    val next = LauncherNavigationPolicy.popToReturn(state)

    assertEquals(Screen.Game, next.screen)
    assertTrue(next.menuVisible)
  }

  @Test
  fun popToReturnIsANoOpOutsideSettingsFamily() {
    val onGame = LauncherNavigationPolicy.popToReturn(defaultState)
    val onAddonManager =
      LauncherNavigationPolicy.popToReturn(defaultState.copy(screen = Screen.AddonManager))

    assertEquals(defaultState, onGame)
    assertEquals(defaultState.copy(screen = Screen.AddonManager), onAddonManager)
  }

  @Test
  fun opensRecordReturnDestinationsAndCloseOverlay() {
    val openedSettings =
      LauncherNavigationPolicy.openSettings(defaultState.copy(menuVisible = true), Screen.Home)
    val openedUpdate =
      LauncherNavigationPolicy.openUpdate(defaultState.copy(menuVisible = true), Screen.Game)
    val openedDiagnostics =
      LauncherNavigationPolicy.openDiagnostics(defaultState.copy(menuVisible = true))

    assertEquals(Screen.Settings, openedSettings.screen)
    assertEquals(false, openedSettings.menuVisible)
    assertEquals(Screen.Home, openedSettings.settingsReturnScreen)

    assertEquals(Screen.Update, openedUpdate.screen)
    assertEquals(Screen.Game, openedUpdate.updateReturnScreen)

    assertEquals(Screen.Diagnostics, openedDiagnostics.screen)
    assertEquals(Screen.Settings, openedDiagnostics.diagnosticsReturnScreen)
  }

  @Test
  fun menuActionsReturnToGameAndCloseOverlay() {
    val state = defaultState.copy(menuVisible = true)

    val afterReload = LauncherNavigationPolicy.afterReload(state)
    val returnToGame = LauncherNavigationPolicy.returnToGame(state)

    assertEquals(Screen.Game, afterReload.screen)
    assertFalse(afterReload.menuVisible)
    assertEquals(Screen.Game, returnToGame.screen)
    assertFalse(returnToGame.menuVisible)
  }

  @Test
  fun startGameResetsToGameWithoutOverlay() {
    val next =
      LauncherNavigationPolicy.startGame(
        defaultState.copy(screen = Screen.Update, menuVisible = true),
      )

    assertEquals(Screen.Game, next.screen)
    assertFalse(next.menuVisible)
  }

  @Test
  fun duplicateOpenOfTheCurrentScreenIsANoOp() {
    val onSettings = defaultState.copy(screen = Screen.Settings)
    val onUpdate = defaultState.copy(screen = Screen.Update)
    val onDiagnostics = defaultState.copy(screen = Screen.Diagnostics)

    assertEquals(onSettings, LauncherNavigationPolicy.openSettings(onSettings, Screen.Game))
    assertEquals(onUpdate, LauncherNavigationPolicy.openUpdate(onUpdate, Screen.Game))
    assertEquals(onDiagnostics, LauncherNavigationPolicy.openDiagnostics(onDiagnostics))
  }

  @Test
  fun invalidReturnDestinationIsRejectedWithoutStateChange() {
    assertEquals(
      defaultState,
      LauncherNavigationPolicy.openSettings(defaultState, Screen.Diagnostics),
    )
    assertEquals(
      defaultState,
      LauncherNavigationPolicy.openUpdate(defaultState, Screen.AddonManager),
    )
  }

  @Test
  fun addonManagerOpensFromOverlayAndBackReturnsToSameGameScreen() {
    val opened = LauncherNavigationPolicy.openAddonManager(defaultState.copy(menuVisible = true))

    assertEquals(Screen.AddonManager, opened.screen)
    assertFalse(opened.menuVisible)

    val back = LauncherNavigationPolicy.onBack(opened)

    assertEquals(Screen.Game, back.screen)
  }

  @Test
  fun addonManagerOnlyOpensFromTheGameScreen() {
    val fromSettings = defaultState.copy(screen = Screen.Settings)
    val fromUpdate = defaultState.copy(screen = Screen.Update)

    assertEquals(fromSettings, LauncherNavigationPolicy.openAddonManager(fromSettings))
    assertEquals(fromUpdate, LauncherNavigationPolicy.openAddonManager(fromUpdate))
  }

  @Test
  fun addonManagerKeepsGameplayOrientationWithoutImmersiveBars() {
    assertTrue(LauncherNavigationPolicy.isGameplay(Screen.AddonManager))
    assertFalse(LauncherNavigationPolicy.isImmersiveGameplay(Screen.AddonManager, false))
  }

  @Test
  fun settingsOpensOutsideImmersiveGameplay() {
    val opened = LauncherNavigationPolicy.openSettings(defaultState, Screen.Game)

    assertTrue(LauncherNavigationPolicy.isImmersiveGameplay(Screen.Game, false))
    assertFalse(LauncherNavigationPolicy.isImmersiveGameplay(opened.screen, opened.menuVisible))
  }
}

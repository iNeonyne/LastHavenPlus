package com.neonyne.lasthavenlauncher

internal data class LauncherNavigationTarget(
  val screen: Screen,
  val menuVisible: Boolean,
)

internal object LauncherNavigationPolicy {
  fun onBack(
    currentScreen: Screen,
    menuVisible: Boolean,
    settingsReturnScreen: Screen,
    diagnosticsReturnScreen: Screen,
    updateReturnScreen: Screen,
  ): LauncherNavigationTarget {
    if (currentScreen == Screen.Game) {
      return LauncherNavigationTarget(Screen.Game, !menuVisible)
    }
    val destination =
      when (currentScreen) {
        Screen.Settings -> settingsReturnScreen
        Screen.Diagnostics -> diagnosticsReturnScreen
        Screen.Update -> updateReturnScreen
        Screen.AddonManager -> Screen.Game
        Screen.Home,
        Screen.Game -> Screen.Game
      }
    return LauncherNavigationTarget(destination, false)
  }

  fun openSettings(): LauncherNavigationTarget = LauncherNavigationTarget(Screen.Settings, false)

  fun openAddonManager(): LauncherNavigationTarget =
    LauncherNavigationTarget(Screen.AddonManager, false)

  fun afterReload(): LauncherNavigationTarget = LauncherNavigationTarget(Screen.Game, false)

  fun returnToGame(): LauncherNavigationTarget = LauncherNavigationTarget(Screen.Game, false)

  fun isGameplay(screen: Screen): Boolean = screen == Screen.Game || screen == Screen.AddonManager

  fun isImmersiveGameplay(screen: Screen, menuVisible: Boolean): Boolean =
    screen == Screen.Game && !menuVisible
}

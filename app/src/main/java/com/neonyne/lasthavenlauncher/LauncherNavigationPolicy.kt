package com.neonyne.lasthavenlauncher

/**
 * Single immutable navigation record for the launcher (issue #147). It replaces the scattered
 * current-screen/return-screen bookkeeping: every transition is a pure function from this state to
 * the next state, so Back handling, return destinations and duplicate/invalid transitions are
 * defined and tested in exactly one place.
 */
internal data class LauncherNavigationState(
  val screen: Screen = Screen.Game,
  val menuVisible: Boolean = false,
  val settingsReturnScreen: Screen = Screen.Game,
  val diagnosticsReturnScreen: Screen = Screen.Settings,
  val updateReturnScreen: Screen = Screen.Game,
)

internal object LauncherNavigationPolicy {
  private val validReturnTargets = setOf(Screen.Game, Screen.Home, Screen.Settings)

  /**
   * Android Back behavior, identical to prior beta: Back on the game screen toggles the overlay,
   * Back on a settings-family screen returns to its recorded return destination, Back on the Addon
   * Manager returns to the game, and Back on Home returns to the game.
   */
  fun onBack(state: LauncherNavigationState): LauncherNavigationState {
    if (state.screen == Screen.Game) {
      return state.copy(menuVisible = !state.menuVisible)
    }
    val destination =
      when (state.screen) {
        Screen.Settings -> state.settingsReturnScreen
        Screen.Diagnostics -> state.diagnosticsReturnScreen
        Screen.Update -> state.updateReturnScreen
        Screen.AddonManager -> Screen.Game
        Screen.Home,
        Screen.Game -> Screen.Game
      }
    return state.copy(screen = destination, menuVisible = false)
  }

  /** Explicit back buttons on Settings/Diagnostics/Update keep the menu state untouched. */
  fun popToReturn(state: LauncherNavigationState): LauncherNavigationState {
    val destination =
      when (state.screen) {
        Screen.Settings -> state.settingsReturnScreen
        Screen.Diagnostics -> state.diagnosticsReturnScreen
        Screen.Update -> state.updateReturnScreen
        else -> return state
      }
    return state.copy(screen = destination)
  }

  fun openSettings(
    state: LauncherNavigationState,
    returnTo: Screen,
  ): LauncherNavigationState {
    if (isDuplicateOrInvalidOpen(state, Screen.Settings, returnTo)) return state
    return state.copy(
      screen = Screen.Settings,
      menuVisible = false,
      settingsReturnScreen = returnTo,
    )
  }

  fun openDiagnostics(state: LauncherNavigationState): LauncherNavigationState {
    if (state.screen == Screen.Diagnostics) return state
    return state.copy(screen = Screen.Diagnostics, menuVisible = false)
  }

  fun openUpdate(
    state: LauncherNavigationState,
    returnTo: Screen,
  ): LauncherNavigationState {
    if (isDuplicateOrInvalidOpen(state, Screen.Update, returnTo)) return state
    return state.copy(screen = Screen.Update, updateReturnScreen = returnTo)
  }

  fun openAddonManager(state: LauncherNavigationState): LauncherNavigationState {
    if (state.screen != Screen.Game) return state
    return state.copy(screen = Screen.AddonManager, menuVisible = false)
  }

  fun afterReload(state: LauncherNavigationState): LauncherNavigationState =
    state.copy(screen = Screen.Game, menuVisible = false)

  fun returnToGame(state: LauncherNavigationState): LauncherNavigationState =
    state.copy(screen = Screen.Game, menuVisible = false)

  fun startGame(state: LauncherNavigationState): LauncherNavigationState =
    state.copy(screen = Screen.Game, menuVisible = false)

  fun isGameplay(screen: Screen): Boolean = screen == Screen.Game || screen == Screen.AddonManager

  fun isImmersiveGameplay(screen: Screen, menuVisible: Boolean): Boolean =
    screen == Screen.Game && !menuVisible

  /**
   * Rejects duplicate opens (already on the target screen) and return destinations that no current
   * caller legitimately supplies; both leave the navigation state unchanged.
   */
  private fun isDuplicateOrInvalidOpen(
    state: LauncherNavigationState,
    target: Screen,
    returnTo: Screen,
  ): Boolean = state.screen == target || returnTo !in validReturnTargets
}

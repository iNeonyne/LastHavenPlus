package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Source-contract regressions for the Addon Manager's same-session integration (issue #170). */
class AddonManagerIntegrationTest {
  private val sourceRoot =
    File("src/main/java").takeIf { it.isDirectory } ?: File("app/src/main/java")

  private fun source(name: String): String =
    File(sourceRoot, "com/neonyne/lasthavenlauncher/$name").readText()

  @Test
  fun managerReturnRefreshesBridgeSnapshotWithoutStartingOrReplacingGameSession() {
    val main = source("MainActivity.kt")
    val managerBranch =
      main.substringAfter("Screen.AddonManager ->").substringBefore("Screen.Diagnostics ->")
    assertTrue(managerBranch.contains("addonBridgeController.refreshSnapshot()"))
    assertTrue(managerBranch.contains("LauncherNavigationPolicy.returnToGame(navigation)"))
    assertTrue(!managerBranch.contains("startGame("))
    assertTrue(!managerBranch.contains("restoreGame("))
  }

  @Test
  fun managerChangesAreMarkedPendingAndOnlyClearedAfterSuccessfulExplicitReload() {
    val main = source("MainActivity.kt")
    assertTrue(main.contains("var addonReloadRequired by remember { mutableStateOf(false) }"))
    assertTrue(main.contains("addonReloadRequired = true"))
    val reload =
      main.substringAfter("val reloadGame: () -> Unit =").substringBefore("val loadGameUrl:")
    assertTrue(reload.contains("if (refreshed)"))
    assertTrue(reload.contains("gameSessionController.reloadGame(context, gameUrl)"))
    assertTrue(reload.contains("addonReloadRequired = false"))
  }

  @Test
  fun managerHostReportsLibraryRevisionChangesAndKeepsOperationsSerialized() {
    val host = source("AddonManagerHost.kt")
    assertTrue(host.contains("initialRevision"))
    assertTrue(host.contains("libraryChanged = true"))
    assertTrue(host.contains("BackHandler(enabled = busy) {}"))
    assertTrue(host.contains("executor.execute"))
  }

  @Test
  fun existingGameSessionControllerRemainsCompositionScopedAcrossManagerNavigation() {
    val main = source("MainActivity.kt")
    assertTrue(main.contains("val gameSessionController = remember { GameSessionController() }"))
    assertTrue(main.contains("session = gameSessionController.session"))
    assertTrue(main.contains("AddonManagerHost("))
  }
}

package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the composition-root wiring that makes the bundled bridge part of the live game session. */
class AddonBridgeWiringTest {
  private val sourceRoot =
    File("src/main/java").takeIf { it.isDirectory } ?: File("app/src/main/java")

  @Test
  fun `bridge is attached to the existing game session and installed lazily`() {
    val source = File(sourceRoot, "com/neonyne/lasthavenlauncher/MainActivity.kt").readText()

    assertTrue(source.contains("AddonBridgeController("))
    assertTrue(source.contains("LaunchedEffect(gameSessionController.session)"))
    assertTrue(source.contains("addonBridgeController.setGameSession(gameSession)"))
    assertTrue(source.contains("addonBridgeController.ensureInstalled(context)"))
    assertTrue(source.contains("AddonRuntimePayloadProvider(addonLibraryRepository)"))
  }

  @Test
  fun `bridge refreshes after returning from addon manager and tears down with composition`() {
    val source = File(sourceRoot, "com/neonyne/lasthavenlauncher/MainActivity.kt").readText()

    assertTrue(source.contains("LaunchedEffect(currentScreen, gameSessionController.session)"))
    assertTrue(source.contains("currentScreen == Screen.Game"))
    assertTrue(source.contains("addonBridgeController.refreshSnapshot()"))
    assertTrue(source.contains("addonBridgeController.teardown()"))
    assertTrue(source.contains("addonBridgeExecutor.shutdown()"))
  }

  @Test
  fun `bridge wiring reuses app private library and does not create a second game session`() {
    val source = File(sourceRoot, "com/neonyne/lasthavenlauncher/MainActivity.kt").readText()

    assertTrue(source.contains("File(context.filesDir, \"addons\")"))
    assertTrue(source.contains("snapshotProvider = { addonLibraryRepository.readLibrary() }"))
    assertTrue(source.contains("val gameSession = gameSessionController.session"))
    assertTrue(source.contains("addonBridgeController.setGameSession(gameSession)"))
    assertTrue(source.contains("addonBridgeController.ensureInstalled(context)"))
    assertTrue(source.contains("gameSessionController.startGame(context, gameUrl)"))
  }
}

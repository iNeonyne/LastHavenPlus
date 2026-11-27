package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the composition-root wiring that makes the bundled bridge part of the live game session.
 */
class AddonBridgeWiringTest {
  private val sourceRoot =
    File("src/main/java").takeIf { it.isDirectory } ?: File("app/src/main/java")

  @Test
  fun `extension installation and sender binding precede the first game navigation`() {
    val mainSource = File(sourceRoot, "com/neonyne/lasthavenlauncher/MainActivity.kt").readText()
    val sessionSource =
      File(sourceRoot, "com/neonyne/lasthavenlauncher/GameSessionController.kt").readText()

    assertTrue(mainSource.contains("addonBridgeController.ensureInstalled(context) {"))
    assertTrue(
      mainSource.contains("gameSessionController.startGame(context, gameUrl) { session ->")
    )
    assertTrue(mainSource.contains("addonBridgeController.setGameSession(session)"))

    val bindPosition = sessionSource.indexOf("onSessionReady(gameSession)")
    val loadPosition = sessionSource.indexOf("gameSession.loadUri(gameUrl)")
    assertTrue(
      "session must be bound before the first loadUri",
      bindPosition >= 0 && loadPosition > bindPosition,
    )
  }

  @Test
  fun `bridge refreshes payload before explicit reload and tears down with composition`() {
    val source = File(sourceRoot, "com/neonyne/lasthavenlauncher/MainActivity.kt").readText()
    val reloadBlock =
      source.substringAfter("val reloadGame: () -> Unit = {").substringBefore("val startGame")

    assertTrue(reloadBlock.contains("addonBridgeController.ensureInstalled(context)"))
    assertTrue(reloadBlock.contains("addonBridgeController.refreshSnapshot {"))
    assertTrue(reloadBlock.indexOf("refreshSnapshot") < reloadBlock.indexOf("reloadGame"))
    assertTrue(source.contains("addonBridgeController.teardown()"))
    assertTrue(source.contains("addonBridgeExecutor.shutdown()"))
  }

  @Test
  fun `bridge reuses the app private library and existing session for restore`() {
    val source = File(sourceRoot, "com/neonyne/lasthavenlauncher/MainActivity.kt").readText()

    assertTrue(source.contains("File(context.filesDir, \"addons\")"))
    assertTrue(source.contains("snapshotProvider = { addonLibraryRepository.readLibrary() }"))
    assertTrue(source.contains("gameSessionController.restoreGame(context, gameUrl) { session ->"))
    assertTrue(source.contains("addonBridgeController.refreshSnapshot {"))
    assertTrue(source.contains("gameSessionController.startGame(context, gameUrl) { session ->"))
    assertTrue(source.contains("addonBridgeController.setGameSession(session)"))
    assertTrue(source.contains("gameSessionController.reloadGame(context, gameUrl)"))
  }
}

package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class GameLoadPolicyTest {
  @Test
  fun noSessionShowsPreparing() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = false,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadFailureKind.None,
      )

    assertEquals(GameLoadPresentation.Preparing, presentation)
  }

  @Test
  fun initialLoadShowsLoadingGame() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadFailureKind.None,
      )

    assertEquals(GameLoadPresentation.LoadingGame, presentation)
  }

  @Test
  fun loadedGameIsNeverObscuredByLaterLoadProgress() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = true,
        failure = GameLoadFailureKind.None,
      )

    assertEquals(GameLoadPresentation.Game, presentation)
  }

  @Test
  fun navigationFailureObscuresLoading() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadFailureKind.Navigation,
      )

    assertEquals(GameLoadPresentation.RecoverableFailure, presentation)
  }

  @Test
  fun navigationFailureObscuresLoadedGame() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = true,
        failure = GameLoadFailureKind.Navigation,
      )

    assertEquals(GameLoadPresentation.RecoverableFailure, presentation)
  }

  @Test
  fun processFailureTakesPriorityOverNavigationFailure() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = true,
        failure = GameLoadFailureKind.Process,
      )

    assertEquals(GameLoadPresentation.RendererFailure, presentation)
  }

  @Test
  fun processFailureTakesPriorityOverLoading() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadFailureKind.Process,
      )

    assertEquals(GameLoadPresentation.RendererFailure, presentation)
  }

  @Test
  fun completedHealthyLoadShowsGame() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = true,
        failure = GameLoadFailureKind.None,
      )

    assertEquals(GameLoadPresentation.Game, presentation)
  }

  @Test
  fun crashSignalClassifiesAsProcessFailure() {
    assertEquals(
      GameLoadFailureKind.Process,
      GameLoadPolicy.failure(sessionCrashed = true, pageLoadFailed = false),
    )
  }

  @Test
  fun pageStopFailureClassifiesAsNavigationFailure() {
    assertEquals(
      GameLoadFailureKind.Navigation,
      GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = true),
    )
  }

  @Test
  fun crashSignalWinsWhenPageStopAlsoReportsFailure() {
    assertEquals(
      GameLoadFailureKind.Process,
      GameLoadPolicy.failure(sessionCrashed = true, pageLoadFailed = true),
    )
  }

  @Test
  fun healthySignalsClassifyAsNoFailure() {
    assertEquals(
      GameLoadFailureKind.None,
      GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
    )
  }

  // The tests below replay the exact flag sequences wired in MainActivity: retry clears
  // pageLoadFailed/sessionCrashed and reloads in place; restore recreates the session and
  // clears every load flag while keeping the session present.

  @Test
  fun retryAfterRecoverableFailureResetsToLoadingThenGame() {
    val presentation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = true),
      )
    assertEquals(GameLoadPresentation.RecoverableFailure, presentation)

    val retrying =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.LoadingGame, retrying)

    val recovered =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = true,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.Game, recovered)
  }

  @Test
  fun failedRetryShowsRecoverableFailureAgain() {
    val stillFailing =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = true),
      )
    assertEquals(GameLoadPresentation.RecoverableFailure, stillFailing)
  }

  @Test
  fun crashDuringInitialLoadShowsRendererFailure() {
    val crashedWhileLoading =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = true, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.RendererFailure, crashedWhileLoading)
  }

  @Test
  fun restoreResetsFlagsAndReloadsBrandedLoadingState() {
    val afterRestore =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.LoadingGame, afterRestore)

    val restoredReload =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.LoadingGame, restoredReload)

    val recovered =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = true,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.Game, recovered)
  }

  @Test
  fun restoredGameIsNeverObscuredByLaterNavigation() {
    val laterNavigation =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = true,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.Game, laterNavigation)
  }

  @Test
  fun crashAfterRestoreTakesPriorityOverLoading() {
    val crashedAgain =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = true, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.RendererFailure, crashedAgain)
  }

  @Test
  fun coldStartBeforeFirstOnPageStartKeepsBrandedLoadingState() {
    val preSignalWindow =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = false),
      )
    assertEquals(GameLoadPresentation.LoadingGame, preSignalWindow)
  }

  @Test
  fun failedPageStopRecordsRecoverableFailureWhileFirstLoadFails() {
    val failedDuringLoad =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = false,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = true),
      )
    assertEquals(GameLoadPresentation.RecoverableFailure, failedDuringLoad)

    val failedWhileStillLoading =
      GameLoadPolicy.resolve(
        hasSession = true,
        isLoading = true,
        hasCompletedLoad = false,
        failure = GameLoadPolicy.failure(sessionCrashed = false, pageLoadFailed = true),
      )
    assertEquals(GameLoadPresentation.RecoverableFailure, failedWhileStillLoading)
  }
}

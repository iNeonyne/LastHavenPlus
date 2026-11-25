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
}

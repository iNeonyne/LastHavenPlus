package com.neonyne.lasthavenlauncher

/** Why the game session needs attention, derived from GeckoView load/process signals. */
enum class GameLoadFailureKind {
  None,

  /** A recoverable page-load failure (for example, the game failed to load over the network). */
  Navigation,

  /** The GeckoView content process crashed or was killed; the session must be recreated. */
  Process,
}

/** The single launcher-owned presentation shown on the game screen. */
enum class GameLoadPresentation {
  Preparing,
  LoadingGame,
  RecoverableFailure,
  RendererFailure,
  Game,
}

/**
 * Classifies GeckoView load signals into one user-visible game-screen state. Rules:
 * - A process failure always wins over a navigation failure and over loading.
 * - A navigation failure obscures loading.
 * - The branded loading state stays until the first successful page load completes, so the
 *   pre-onPageStart window never briefly exposes a blank game view; later in-game navigations never
 *   obscure healthy, loaded gameplay because hasCompletedLoad stays true.
 */
object GameLoadPolicy {
  fun resolve(
    hasSession: Boolean,
    isLoading: Boolean,
    hasCompletedLoad: Boolean,
    failure: GameLoadFailureKind,
  ): GameLoadPresentation =
    when {
      failure == GameLoadFailureKind.Process -> GameLoadPresentation.RendererFailure
      failure == GameLoadFailureKind.Navigation -> GameLoadPresentation.RecoverableFailure
      !hasSession -> GameLoadPresentation.Preparing
      !hasCompletedLoad -> GameLoadPresentation.LoadingGame
      else -> GameLoadPresentation.Game
    }

  fun failure(sessionCrashed: Boolean, pageLoadFailed: Boolean): GameLoadFailureKind =
    when {
      sessionCrashed -> GameLoadFailureKind.Process
      pageLoadFailed -> GameLoadFailureKind.Navigation
      else -> GameLoadFailureKind.None
    }
}

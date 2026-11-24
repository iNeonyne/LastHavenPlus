# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator; Ploy is a scoped implementation agent.

## Roles

- **Chaty:** owns intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone pushes accepted commits directly to `beta`.
- **Ploy:** implements only the task assigned below. Ploy does not edit central trackers, create or manage GitHub issues, integrate into `beta`, or push to `main`.

## Shared workflow

- `beta` is the only active development, integration and testing branch. `main` is stable.
- Pull requests are allowed and are the standard handoff/integration path. Ploy opens a PR targeting `beta` for each completed assignment.
- Parallel work is allowed only for different issues with non-overlapping file scopes. Never work on the same issue concurrently.
- Ploy works from a dedicated task branch based on current `beta`, then opens a PR targeting `beta` and hands Chaty the PR link, exact head commit SHA, changed files, test results and limitations.
- Chaty alone reviews and merges accepted PRs into `beta`; contributors must not merge their own PRs or push directly to `beta`.
- Promote to `main` or publish a release only after release gates pass and the owner explicitly approves.
- GitHub Issues and `ISSUES.md` are the source of truth. TickTick may be used as a supplementary task tracker.
- Physical-device behavior remains unverified until tested on real ARM64 hardware.

## Project constraints

- Keep the application ID `com.neonyne.lasthavenlauncher`, established release signing identity and ARM64-only target unchanged.
- Do not modify the game/client.
- Do not add automatic update downloads or installation.
- Do not ship debug APKs or partially implemented features.
- Do not change Gradle, AGP, JDK, Android SDK or GeckoView versions without evidence and compatibility validation.
- Do not restore or work on the removed addon manager. Issue #23 remains on hold pending a new owner specification.

## Ploy — ACTIVE: issue #22 home screen and navigation

**Goal:** Add the app's first-launch home screen and complete the transition between home, settings and the existing game.

**Required behavior**
- Fresh app launch opens the LastHavenPlus home screen, not the game.
- Home screen presents one prominent Play action and a Settings entry.
- Play opens the existing game.
- The in-game menu exposes Reload, Home and Return to game while preserving existing supported actions.
- Home returns to the launcher home screen. Return to game restores the existing game session rather than unnecessarily recreating or reloading it.
- Settings can be opened from home and the game menu.
- Apply immersive mode, landscape/game orientation and keep-screen-on behavior only while gameplay is active; restore normal system UI on home/settings.
- Do not initialize a game session merely by opening Settings.

**File ownership**
- Ploy owns the home/navigation UI and directly related UI tests, including `MainActivity.kt`, `HomeScreen.kt`, `AppNavigation.kt` if present, and narrowly related resources/tests.
- Do not edit GeckoRuntime/session provider internals, Gradle/dependency configuration, updater implementation, diagnostics internals or addon-related code.
- Chaty must not edit Ploy's assigned files while this task is active. Any scope expansion requires a handoff and an updated assignment first.

**Acceptance and evidence**
- Home appears on fresh launch.
- Play opens the game.
- Home and Settings navigation work from the appropriate entry points.
- Return to game preserves the existing session.
- Reload reloads the current game page.
- System UI/orientation/keep-awake behavior is scoped to gameplay.
- Relevant automated tests/build checks pass.
- Physical ARM64 interaction and TalkBack behavior remain unverified until tested on hardware.

## Chaty — integration and release coordination

- Maintain exclusive ownership of central documentation, `STATUS.md`, `ISSUES.md`, `README.md`, `AGENTS.md`, this file, `CHANGELOG-AGENTS.md` and `WORK_LOG.md`.
- Verify the CI optimization run associated with commit `54b947279a783c6c382210e65045edd02bd5ee73`; do not claim performance improvement until elapsed time is compared with a suitable baseline.
- Keep the successful run #308 evidence tied to its actual tested commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; do not describe it as a build of later documentation/workflow commits.
- Coordinate the remaining GeckoView physical-device checks and review Ploy's issue #22 handoff.
- Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Paused and deferred work

- Issue #9 accessibility follow-up: paused until the home/navigation implementation is integrated and can be checked against the final UI.
- Issue #11 diagnostics test expansion: paused; resume after the home/navigation handoff or when Chaty explicitly reorders it.
- Issue #23 addon manager: blocked by the owner's hold. Do not begin until a new written specification is supplied.

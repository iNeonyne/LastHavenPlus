# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator and currently owns all active implementation work. Ploy has no active assignment until Chaty explicitly assigns a separate issue.

## Roles

- **Chaty:** owns intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone reviews and merges accepted PRs into `beta`.
- **Ploy:** may implement only a separately assigned issue. There is currently no active Ploy assignment. Ploy must not continue PR #25 or work on issue #22 unless Chaty explicitly reassigns it.

## Shared workflow

- `beta` is the only active development, integration and testing branch. `main` is stable.
- Pull requests are allowed and are the standard handoff/integration path. Contributors open PRs targeting `beta` for completed assignments.
- Parallel work is allowed only for different issues with non-overlapping file scopes. Never work on the same issue concurrently.
- Any contributor works from a dedicated task branch based on current `beta`, then opens a PR targeting `beta` and hands Chaty the PR link, exact head commit SHA, changed files, test results and limitations.
- Chaty alone reviews and merges accepted PRs into `beta`; contributors must not merge their own PRs or push directly to `beta`.
- PR CI for `beta` builds the unsigned release variant and runs unit tests; it does not use signing secrets or publish APKs. Chaty must still verify the full signed build and release gates on the resulting beta commit.
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

## Chaty — ACTIVE: issue #22 home/navigation and issue #4 updater hardening

**Current PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26), branch `chaty/home-updater-demo` → `beta`.

### Issue #22 — home and navigation
- Fresh launch opens Home; it must not load the game.
- Play lazily creates the GeckoRuntime/GeckoSession and loads the game once.
- Settings from Home must not initialize GeckoView.
- Game menu: Reload, Home, Return to game, Settings, diagnostics, manual update check and Exit.
- Home navigation detaches the GeckoView but retains the open GeckoSession; returning must not call `loadUri` again.
- Home Back confirms exit.
- Immersive system bars, landscape orientation and keep-awake apply only while gameplay is active. Settings/diagnostics restore normal UI.
- Keep existing application ID, GeckoView version, signing identity, Gradle toolchain and ARM64-only configuration.

### Issue #4 — updater
- A differential patch is an optimization only. Any base mismatch, patch download/reconstruction failure, APK size/digest mismatch or package/signature validation failure must retry using the full APK.
- Delete temporary/partial files on failure; never pass an unverified file to Android's installer.
- Restrict release asset URLs and post-redirect hosts to HTTPS GitHub hosts.
- Keep user-confirmed download and installer handoff. No automatic downloads or installs.
- Cover version/versionCode, SHA-256, trusted URL and delta-to-full fallback policy with unit tests.
- Do not change the updater architecture or signing identity.
- Beta branch demo artifacts must reuse the latest published stable versionName/versionCode so an existing stable install is not treated as a version downgrade. Keep `is_release=false`: no tag, manifest upload or GitHub release may be created by a beta build.

### Verification and handoff
- PR run #317 passed `assembleRelease` and `testDebugUnitTest` for code SHA `283d649cd31ae009889d76affb25f655e28d3949`. Any later source change requires a fresh exact-head run; merge only with the current PR checks green.
- After merge, inspect beta branch CI for signed release build, tests, signer and ARM64-only packaging.
- Physical ARM64 gameplay and update installation remain unverified until tested on hardware.
- Chaty alone reviews and merges to beta. No release tag, main promotion or publication without owner approval.

## Chaty — integration and release coordination

- Maintain exclusive ownership of central documentation, `STATUS.md`, `ISSUES.md`, `README.md`, `AGENTS.md`, this file, `CHANGELOG-AGENTS.md` and `WORK_LOG.md`.
- Verify the CI optimization run associated with commit `54b947279a783c6c382210e65045edd02bd5ee73`; do not claim performance improvement until elapsed time is compared with a suitable baseline.
- Keep the successful run #308 evidence tied to its actual tested commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; do not describe it as a build of later documentation/workflow commits.
- Own the #22/#4 implementation and review in PR #26; coordinate remaining GeckoView physical-device checks.
- Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Paused and deferred work

- Issue #9 accessibility follow-up: paused until the home/navigation implementation is integrated and can be checked against the final UI.
- Issue #11 diagnostics test expansion: paused; resume after PR #26 is integrated or when Chaty explicitly reorders it.
- Ploy: no active assignment. Do not work on #22 or PR #25.
- Issue #23 addon manager: blocked by the owner's hold. Do not begin until a new written specification is supplied.

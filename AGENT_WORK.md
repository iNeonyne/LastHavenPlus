# Agent work assignments — LastHavenPlus

This file is the execution order and file-ownership contract for the two project agents. The repository owner remains the decision-maker.

## Agent identities
- **Ploy** — repository-only coding/research agent. She works only on the assignment below, cannot inspect GitHub Issues/Actions/releases/secrets, and must not edit central trackers.
- **Chaty** — main orchestrator. Owns issue intake, priorities, central docs/status, review, CI/release coordination and direct pushes to `beta`. `main` is stable.

## Rules
- Read `AGENTS.md`, `README.md`, `STATUS.md`, `ISSUES.md` and this file before starting a task.
- `beta` is the only development/integration branch. `main` is stable.
- Parallel work is allowed only on separate issues and non-overlapping files. Never work on the same issue concurrently.
- Ploy creates a task branch from latest `beta`, pushes only to that task branch and hands Chaty its branch name, exact commit SHA, changed files, tests/results and limitations. Ploy never pushes to `beta` or `main`.
- Chaty alone reviews the complete diff and integrates accepted commits directly into `beta`. No pull requests are used.
- Chaty owns GitHub issue creation/updates, labels, assignment, priority, backlog, STATUS, README, AGENTS and CHANGELOG-AGENTS. Ploy must not change central trackers.
- No TickTick. GitHub Issues and `ISSUES.md` are the source of truth.
- Do not modify the game/client, application ID, established release signing identity or arm64-only target. Do not add automatic update downloading/installation. Do not create tags, releases or promote beta to main without explicit approval.
- Current beta still contains Cefrium. The approved target is GeckoView; follow issues #20/#21 and do not independently change engine dependencies or begin engine work before the assigned task is ready.
- Keep the existing WoW addon ZIP library distinct from Gecko WebExtensions. Local enabled flags do not mean game injection or in-game loading.
- Physical-device testing remains unverified until performed on real ARM64 hardware.

## Ploy — exclusive assignment sequence

Work one issue at a time. Stop after each task-branch handoff and wait for Chaty's review and integration before starting the next item.

### ACTIVE — #13 accessibility semantics
**Goal:** Resolve the Compose compile errors and improve accessible names for launcher actions.

Scope:
- Fix `AddonManagerDialog.kt` semantics using supported Compose APIs; do not pass a `contentDescription` argument to Material3 `Switch`.
- Add concise semantics only when visible text does not already provide a suitable accessible name.
- Check the menu, Retry, Reload, update/download, Settings, diagnostics, addon manager and Exit controls.
- Keep the visual design and behavior unchanged except where required for accessibility.
- Add/update automated tests where the current test setup supports them.

File boundary: Compose UI files needed for #13 and directly related tests. Do not modify Gradle/engine configuration, repository logic, central trackers or unrelated UI.

Acceptance: current compiler errors are fixed, relevant checks pass, no duplicate screen-reader labels are introduced, and TalkBack/focus-order verification remains pending until a physical ARM64 device is tested.

### QUEUED — #11 diagnostics tests
Start only after #13 is integrated into `beta`.

Scope: tests for redaction formats, bounded log size/rotation and safe failure-path logging. Do not change the logging architecture or collect additional sensitive data unless a reproducible test exposes a specific defect.

File boundary: diagnostics logger and its unit tests only. Do not touch engine migration or screen navigation.

### QUEUED — #8 addon repository tests
Start only after #11 is integrated into `beta`.

Scope: duplicate imports, replacement confirmation/rollback, storage limits, export round-trip, index recovery and failed writes using temporary directories and deterministic ZIP fixtures.

File boundary: `AddonLibraryRepository.kt` and repository-focused tests only. Preserve local enabled flags as library metadata. No game injection, engine hooks, Gecko WebExtensions, online catalog or UI redesign.

### QUEUED — #20 GeckoView technical decision record
Start only after #8 is integrated into `beta`.

Scope:
- Create `docs/GECKOVIEW_MIGRATION.md` only; no production source, Gradle or manifest changes.
- Verify current stable GeckoView artifact/version and official Mozilla API documentation.
- Document runtime/session/view lifecycle, profile persistence and Cefrium data portability risks, navigation/crash/permission/prompt/media/fullscreen/file delegates, target game API requirements, ARM64/AAR packaging, APK-size measurement and CI/device test plan.
- Clearly distinguish WoW addon ZIP/`.toc` packages from Gecko WebExtensions.

Acceptance: official references and date/version evidence are recorded; unresolved questions and an ordered migration plan are clear. Chaty reviews the decision record before #21 starts.

### QUEUED — #22 home screen and navigation
Start only after Chaty has integrated #21 into `beta` and handed off the engine screen contract.

Scope: home screen with LastHavenPlus branding, one prominent Play action and a Settings entry; no automatic game navigation on app startup; game-to-home/settings/back/exit navigation; preserve the existing Gecko session when navigating away; apply immersive/keep-awake only during gameplay.

File boundary: screen/navigation UI (`MainActivity.kt`, `HomeScreen.kt`, `AppNavigation.kt` and directly related tests). Do not change Gecko runtime/session implementation, Gradle dependencies, updater internals or addon repository logic.

Acceptance: home appears on fresh launch; Play opens game; Settings works without initializing a game session; home/game transitions do not reload the game; relevant navigation tests pass.

### QUEUED — #23 move existing addon manager into Settings
Start only after #22 is integrated into `beta`.

Scope: place the current `AddonManagerDialog` behind the Settings destination; preserve existing repository data and reuse `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt` and tests. Do not reset or rename `filesDir/addon-library`.

File boundary: Settings UI and addon manager presentation/navigation only. Avoid editing the archive inspector/repository unless a separately approved defect is found. No game injection or Gecko WebExtension conversion.

Acceptance: packages already stored by the current beta remain visible; import/export/replace/remove remain explicit; the UI clearly labels local library state and never claims addons were loaded by the game.

## Chaty — active and queued

### ACTIVE — #4 build reliability and updater tests
- Restore a clean current beta CI build and complete updater edge-case tests.
- Keep the committed Gradle Wrapper; do not change Gradle/JDK/AGP/SDK/signing identity speculatively.
- Current compile blocker is tracked under Ploy's #13 assignment; do not edit the same UI files while she owns #13.

### QUEUED — #21 replace Cefrium with GeckoView
Start after #4 is at a clean, verified baseline and #20 is reviewed.

Scope: replace Cefrium plugin/dependencies/repository/runtime with one verified stable GeckoView version; implement process-scoped GeckoRuntime, managed GeckoSession and GeckoView host; safe delegates; persistent profile; remove all Cefrium production references.

File boundary: `app/build.gradle.kts`, `settings.gradle.kts`, `LastHavenApplication.kt`, browser-engine package and the minimal `MainActivity.kt` host conversion needed to maintain the current one-screen Play/game flow. Do not implement the new home navigation or alter addon library logic.

Acceptance: no Cefrium code/dependency remains; builds/tests/signature/ABI checks pass; runtime/session/profile behavior is tested; physical ARM64 gameplay verification remains explicitly pending until performed.

### DEFERRED — #19 beta updater channel
Do not start until the GeckoView engine migration and home/navigation changes are integrated and the updater is retested against the new app architecture. Stable remains the default; no automatic download/install.

## Handoff format

Ploy provides Chaty:
- GitHub issue number and task-branch name.
- Exact commit SHA and changed file list.
- Commands run and actual test/build results.
- Known limitations, unverified behavior and any follow-up decision.

Ploy stops after handing off. Chaty reviews the full diff, reruns or verifies required checks, integrates accepted work directly into `beta`, updates central trackers and only then advances the assignment queue.
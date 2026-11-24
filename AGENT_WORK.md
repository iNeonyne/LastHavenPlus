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
- The previous addon manager implementation has been removed. Do not recreate or design a replacement until the owner provides a new written specification. Keep WoW addon packages distinct from Gecko WebExtensions.
- Physical-device testing remains unverified until performed on real ARM64 hardware.

## Ploy — exclusive assignment sequence

Work one issue at a time. Stop after each task-branch handoff and wait for Chaty's review and integration before starting the next item.

### ACTIVE — #9 navigation menu accessibility (easy task)
**Goal:** Finish the small accessibility follow-up for the existing exit/navigation menu.

Scope:
- Add meaningful Compose semantics only where visible text does not already provide an accessible name.
- Check Return to game, Reload, Settings, diagnostics, update and Exit controls.
- Check focus order and avoid duplicate TalkBack announcements.
- Do not add an Addons control; the legacy addon manager has been removed.

File boundary: `MainActivity.kt` menu semantics and directly related UI tests only. Do not modify Gradle/engine configuration, updater logic, diagnostics internals or central trackers.

Acceptance: menu actions have clear, non-duplicated accessible names; relevant automated checks pass. Physical TalkBack/focus-order verification remains pending until a real ARM64 device is tested.

### QUEUED — #11 diagnostics tests
Start only after #9 is integrated into `beta`.

Scope: tests for redaction formats, bounded log size/rotation and safe failure-path logging. Do not change the logging architecture or collect additional sensitive data unless a reproducible test exposes a specific defect.

File boundary: diagnostics logger and its unit tests only. Do not touch engine migration or screen navigation.

### QUEUED — #11 diagnostics tests
Start after #9 is integrated into `beta`.

Scope: tests for redaction formats, bounded log size/rotation and safe failure-path logging. Do not change logging architecture or collect additional sensitive data.

File boundary: diagnostics logger and unit tests only.

### QUEUED — #20 GeckoView technical decision record
Start after #11 and the small, isolated #6 settings preparation task are integrated, and after Chaty confirms the beta build baseline.

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

### BLOCKED — #23 addon manager rebuild
Do not start this task until the owner provides a new written specification. The previous ZIP addon manager implementation, tests and app entry point have been deleted. Do not recreate its old features or storage format based on historical documentation.

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

## Easy work queued for Ploy

After #9 and #11, Ploy may take #6's straightforward, non-engine settings work only after Chaty has integrated #21 and #22. The initial isolated part is editable HTTPS game URL preference validation/persistence with unit tests; do not implement Gecko profile clearing, cache deletion, version reporting or other engine-specific controls until the pinned GeckoView API is reviewed. No addon-manager work until the owner specification arrives.


## Ploy task queue after current easy work

1. #9 — ACTIVE: small menu accessibility follow-up.
2. #11 — diagnostics redaction/rotation/failure-path unit tests; next after #9 integration.
3. #6 — small, isolated editable HTTPS game URL validation/persistence task after the GeckoView/home navigation contract is available. Do not implement profile/cache clearing or Gecko-specific controls before the exact pinned API is verified.
4. #20 — GeckoView documentation/research update after Chaty confirms clean beta baseline; documentation-only.
5. #22 — home screen after #21 integration.
6. #23 — blocked until owner supplies addon-manager requirements.

Every task must append progress and interruption checkpoints to `WORK_LOG.md`. Ploy never integrates or pushes to beta.

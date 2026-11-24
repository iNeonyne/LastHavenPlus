# Agent work assignments — LastHavenPlus

This file is the execution order and file-ownership contract for the two project agents. The repository owner remains the decision-maker.

## Agent identities
- **Ploy** — repository-only coding/research agent. She works only on the assignment below, cannot inspect GitHub Issues/Actions/releases/secrets, and must not edit central trackers.
- **Chaty** — main orchestrator. Owns issue intake, priorities, central docs/status, review, CI/release coordination and direct pushes to `beta`. `main` is stable.

## Rules
- Read `AGENTS.md`, `README.md`, `STATUS.md`, `ISSUES.md`, `WORK_LOG.md` and this file before starting a task; read `docs/GECKOVIEW_MIGRATION.md` before GeckoView work.
- `beta` is the only development/integration branch. `main` is stable.
- Parallel work is allowed only on separate issues and non-overlapping files. Never work on the same issue concurrently.
- Ploy creates a task branch from latest `beta`, pushes only to that task branch and hands Chaty its branch name, exact commit SHA, changed files, tests/results and limitations. Ploy never pushes to `beta` or `main`.
- Chaty alone reviews the complete diff and integrates accepted commits directly into `beta`. No pull requests are used.
- Chaty owns GitHub issue creation/updates, labels, assignment, priority, backlog, STATUS, README, AGENTS and CHANGELOG-AGENTS. Ploy must not change central trackers.
- No TickTick. GitHub Issues and `ISSUES.md` are the source of truth.
- Do not modify the game/client, application ID, established release signing identity or arm64-only target. Do not add automatic update downloading/installation. Do not create tags, releases or promote beta to main without explicit approval.
- Current beta still contains Cefrium. The approved target is GeckoView; follow issues #20/#21 and do not independently change engine dependencies or begin engine work before the assigned task is ready.
- The previous addon manager implementation has been removed. Issue #23 (Firefox WebExtension addon manager) is explicitly on hold by the owner. Do not research, design, implement, test, assign, or otherwise spend project effort on addon-manager work until the owner explicitly resumes it. Keep WoW addon packages distinct from Gecko WebExtensions.
- Physical-device testing remains unverified until performed on real ARM64 hardware.

## Ploy — paused while GeckoView is the top priority

Ploy's smaller tasks #9 (menu accessibility) and #11 (diagnostics tests) are paused. Do not edit their files or begin another task until Chaty reactivates the queue. The current top-priority work is owned by Chaty; no concurrent agent is assigned to the same GeckoView issues.

### PAUSED — #9 navigation menu accessibility (easy task)
**Goal:** Finish the small accessibility follow-up for the existing exit/navigation menu.

Scope:
- Add meaningful Compose semantics only where visible text does not already provide an accessible name.
- Check Return to game, Reload, Settings, diagnostics, update and Exit controls.
- Check focus order and avoid duplicate TalkBack announcements.
- Do not add an Addons control; the legacy addon manager has been removed.

File boundary: `MainActivity.kt` menu semantics and directly related UI tests only. Do not modify Gradle/engine configuration, updater logic, diagnostics internals or central trackers.

Acceptance: menu actions have clear, non-duplicated accessible names; relevant automated checks pass. Physical TalkBack/focus-order verification remains pending until a real ARM64 device is tested.

### PAUSED — #11 diagnostics tests
Start only after #9 is integrated into `beta`.

Scope: tests for redaction formats, bounded log size/rotation and safe failure-path logging. Do not change the logging architecture or collect additional sensitive data unless a reproducible test exposes a specific defect.

File boundary: diagnostics logger and its unit tests only. Do not touch engine migration or screen navigation.

### NOT ASSIGNED — #20 GeckoView technical decision record
Chaty owns this P0 research decision as part of the engine migration workstream. Ploy must not research or edit the same document concurrently.

Scope:
- Update `docs/GECKOVIEW_MIGRATION.md` only; no production source, Gradle or manifest changes.
- Verify current stable GeckoView artifact/version and official Mozilla API documentation.
- Document runtime/session/view lifecycle, profile persistence and Cefrium data portability risks, navigation/crash/permission/prompt/media/fullscreen/file delegates, target game API requirements, ARM64/AAR packaging, APK-size measurement and CI/device test plan.
- Keep the future addon manager out of scope until the owner provides its new specification.

Acceptance: official references and date/version evidence are recorded; unresolved questions and an ordered migration plan are clear. Chaty reviews the decision record before #21 starts.

### QUEUED — #22 home screen and navigation
Start only after Chaty has integrated #21 into `beta` and handed off the engine screen contract.

Scope: home screen with LastHavenPlus branding, one prominent Play action and a Settings entry; no automatic game navigation on app startup; game-to-home/settings/back/exit navigation; preserve the existing Gecko session when navigating away; apply immersive/keep-awake only during gameplay.

File boundary: screen/navigation UI (`MainActivity.kt`, `HomeScreen.kt`, `AppNavigation.kt` and directly related tests). Do not change Gecko runtime/session implementation, Gradle dependencies, updater internals or addon repository logic.

Acceptance: home appears on fresh launch; Play opens game; Settings works without initializing a game session; home/game transitions do not reload the game; relevant navigation tests pass.

### BLOCKED — #23 addon manager rebuild
Do not start this task until the owner provides a new written specification. The previous ZIP addon manager implementation, tests and app entry point have been deleted. Do not recreate its old features or storage format based on historical documentation.

## Chaty — active and queued

### ACTIVE — #4 build baseline gate (limited)
- Monitor the current post-removal beta CI run and inspect its build/test result.
- Resolve only confirmed baseline blockers needed to establish a reproducible starting point for GeckoView.
- Keep the committed Gradle Wrapper; do not change Gradle/JDK/AGP/SDK/signing identity speculatively.
- Do not let unrelated updater enhancements delay the engine migration; defer remaining updater test expansion until after the GeckoView replacement is stable.

### ACTIVE — #20 GeckoView technical decision record
- Chaty owns immediate verification of the stable GeckoView artifact, exact coordinates, API surface, Java/toolchain compatibility, ARM64 payload, licenses and size impact.
- Do not add the dependency until the official artifact and current build compatibility are verified.

### NEXT — #21 replace Cefrium with GeckoView
Begin production integration immediately after #20's exact stable-artifact/API decision and the current beta baseline build are verified.

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


## Ploy task queue while GeckoView is top priority

1. #9 — PAUSED: small menu accessibility follow-up.
2. #11 — PAUSED: diagnostics redaction/rotation/failure-path unit tests.
3. #20 — Chaty owns the technical decision record; no parallel Ploy work.
4. #22 — queued after Chaty integrates #21.
5. #6 — queued after #21/#22 define the navigation contract.
6. #23 — on hold at the owner's explicit request.

Every task must append progress and interruption checkpoints to `WORK_LOG.md`. Ploy never integrates or pushes to beta. Chaty owns the GeckoView migration as the top priority; Ploy remains paused until a non-overlapping task is explicitly reactivated.


## DEFERRED — #23 Firefox WebExtension addon manager

Owner placed this entire workstream on hold on 2026-11-25. Do not assign it to Ploy or Chaty, create extension files, vendor dependencies, conduct further implementation research, or run tests for it until the owner explicitly resumes the task. Preserve the owner's specification in conversation context; it is not an authorization to proceed while on hold. Existing removal of the legacy Android addon manager remains in effect.

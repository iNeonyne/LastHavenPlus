# Work log — LastHavenPlus beta

**Purpose:** Durable restart point for the owner, Chaty and agents. This is an append-only operational journal, not a replacement for GitHub Issues or STATUS.md.

## Rules for every contributor

- Before changing files, record the issue, task branch, starting commit, intended file scope and acceptance criteria below.
- After each meaningful step (inspection, edit, test, commit, handoff), append a dated checkpoint with exact paths, commit SHA, commands and actual results.
- Record failures and skipped checks explicitly. Never write “passed” unless the command/run output proves it.
- At task end, record the exact handoff commit, changed files, tests, known limitations and next action.
- If interrupted, leave a final checkpoint stating the last completed action and the first safe next action. Do not amend or rewrite earlier checkpoints; append a correction if needed.
- Chaty alone integrates task commits directly into `beta`. No PRs. Never push to `main` or publish without explicit owner approval.
- Do not use TickTick. GitHub Issues and `ISSUES.md` are the source of truth.

## Current restart point — 2026-11-25

- Active branch: `beta`.
- Stable branch: `main`; no promotion or release is authorized.
- Starting commit for this preparation: `8d624ccf9dc85be1428e352c2fe9c22dfdc69310`.
- Approved target: replace Cefrium with Mozilla GeckoView; launch into a simple home screen with Play and Settings; addon manager is removed pending a new owner specification.
- Current build issue #13 previously reported Kotlin compile errors inside the legacy addon manager. The legacy manager code is being removed, so CI must be rerun before treating those errors as resolved.
- Ploy's first task is the small accessibility follow-up in #13 after the addon UI is removed; next #11 diagnostics tests; then easy settings task #6 after engine/home dependencies are resolved. Ploy must not start GeckoView production integration.
- Chaty owns #4 build baseline/updater tests, #21 GeckoView engine replacement after #20 research is reviewed, and all beta integration.
- GeckoView technical notes are in `docs/GECKOVIEW_MIGRATION.md`.
- Addon manager rebuild is blocked pending the owner's detailed instruction. Do not recreate the deleted implementation.

## Checkpoints

### 2026-11-25 — Preparation started

- **Starting beta commit:** `8d624ccf9dc85be1428e352c2fe9c22dfdc69310`.
- **Approved work:** add repo-local GeckoView agent references and durable work log; remove legacy addon manager code/UI; revise assignments so Ploy gets small, non-overlapping tasks; preserve the user's future addon-manager requirements as a hard gate.
- **No main/release action:** none.
- **Next safe action:** verify post-removal source references and inspect the latest beta CI result; update status after the final documentation commit.

### Checkpoint template

```markdown
### YYYY-MM-DD HH:MM UTC — short step
- **Issue/task:** #N / description
- **Branch and starting commit:** ...
- **Files inspected:** ...
- **Files changed:** ...
- **Action:** ...
- **Commands/checks:** exact command or CI URL
- **Result:** exact pass/fail/skipped evidence
- **Commit:** ...
- **Unverified / risks:** ...
- **Next safe action:** ...
```
### 2026-11-25 — Remove legacy addon manager and prepare GeckoView agent references

- **Issue/task:** owner instruction; #9, #11, #20, #21, #22, #23.
- **Branch and starting commit:** `beta`, `8d624ccf9dc85be1428e352c2fe9c22dfdc69310`.
- **Files inspected:** `MainActivity.kt`, all four legacy addon manager source/test files, README, AGENTS, AGENT_WORK, STATUS, ISSUES, CHANGELOG-AGENTS and workflow definition.
- **Files changed:** removed Addon manager state/menu/dialog from `MainActivity.kt`; deleted `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt`, `AddonArchiveInspectorTest.kt`; added `docs/GECKOVIEW_MIGRATION.md`; added this `WORK_LOG.md`; updated README, AGENTS, AGENT_WORK, STATUS, ISSUES and relevant GitHub issue bodies.
- **Action:** removed the legacy addon manager implementation at the owner's request. The new addon manager is blocked until the owner provides requirements. No user app-private package data was targeted by these GitHub repository file deletions.
- **Commits:** MainActivity removal `255e2a784ad5f03338ee183d181954ab8b242bb3`; deleted archive inspector `2a49d2a4ee53d01f46ed803a3264fc32c2611b9f`; deleted repository `7b96a1d62396211a87cddb0a4130f4cb3f605305`; deleted dialog `6d2f94d8ccf77333f9b55fa5c73922fd868720f7`; deleted inspector tests `8ac4b79ba5713cd4f313b6d4898b654cfc5d42a5`; migration guide `077aca85fbf350950a595afa193d16d86474f2fc`; work log `3a29c59fd7e80d1a0d53bc67eb7a09c466e6cef6`.
- **Commands/checks:** inspected recursive beta tree; confirmed no legacy addon implementation/test paths remain. Inspected official Mozilla GeckoView quick start, architecture, API/Javadocs, permissions and WebExtension docs. Latest post-removal beta CI run #268 (ID `45496604607`) was pending at last inspection.
- **Result:** source removal committed; build/test result not yet known. No app build is claimed successful. No GeckoView production code/dependency has been added.
- **Unverified / risks:** whether app compiles after removing manager; whether any unsearched dangling references remain; exact stable GeckoView version and build compatibility; all physical-device/browser-profile behavior.
- **Next safe action:** check run #268 and inspect all remaining source references; fix only actual compile issues. Ploy's active easy task is #9 menu accessibility, followed by #11 diagnostics tests. #23 remains blocked pending owner requirements.

### 2026-11-25 — CI observation after manager removal

- **Workflow:** beta CI run #268, ID `45496604607`, on `810cecdec2c2d795acc81331272f0751efa3b579`.
- **Result:** run was cancelled by newer pushes before useful build/test evidence was produced. It does not establish that the app compiles after the manager removal.
- **Follow-up:** wait for the latest beta push's CI run; record the exact run/commit and inspect job logs before updating implementation status.


### 2026-11-25 — Final preparation verification checkpoint

- **Branch:** `beta`.
- **Commit at checkpoint:** `f8c57eefe73b220340334ae217dc309b2828dc72`.
- **Verified repository tree:** `docs/GECKOVIEW_MIGRATION.md` and `WORK_LOG.md` are present; `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt` and `AddonArchiveInspectorTest.kt` are absent.
- **Verified assignments:** Ploy #9 active (small menu accessibility), #11 queued, #20 queued documentation research, #22 queued after #21; #23 blocked awaiting owner requirements. Chaty owns #4 and #21.
- **CI:** run #281, ID `45497088290`, on commit `f8c57eefe73b220340334ae217dc309b2828dc72`, pending at inspection. Runs #268, #278 and #279 were cancelled by subsequent pushes and are not test evidence.
- **Next safe action:** inspect the final current beta CI run and its logs. If build/tests fail, fix only confirmed failures, respecting Ploy's #9 file ownership. Do not begin GeckoView production integration before the clean baseline and reviewed #20 decision record.


### 2026-11-25 — Mandatory logging rule propagated to agent instructions

- **Action:** Updated `AGENTS.md` and `AGENT_WORK.md` to require an append-only `WORK_LOG.md` checkpoint after every meaningful inspection, change, test, commit, handoff or interruption. Each entry must include exact commit, paths, commands/results, limitations and next safe action.
- **Reason:** Ensure another agent can resume safely after an unexpected stop without relying on chat history.
- **Next safe action:** Check the current beta head and CI. No tests are claimed from cancelled runs.


### 2026-11-25 — Finalize agent continuity rules

- **Files:** `AGENTS.md`, `AGENT_WORK.md`, `WORK_LOG.md`.
- **Changes:** Added explicit pre-task reading of the restart journal and GeckoView reference; made per-step logging mandatory after inspections, edits, tests, commits, handoffs and interruptions; removed the last obsolete instruction to reuse the deleted addon manager; finalized Ploy's sequence as #9 → #11 → #20, then #22 after #21, followed by the small #6 URL-settings task. #23 remains blocked pending owner requirements.
- **Commits:** `32e6a33921f5d26d91589ea49592baf180537ecc` (AGENTS), `6119d686e25ce883abc41148b428cc13e54b578c` (AGENT_WORK).
- **Next safe action:** inspect the latest beta CI run for the post-removal source. Current docs-only commits do not prove app compilation. No GeckoView implementation has begun.


### 2026-11-25 — Canonical game URL correction

- **Branch:** `beta`.
- **Starting commit:** `9af9743be1b82e0995583181ff1de970ccdcb270`.
- **Change:** Replaced the `alt.lasthavens.com` game URL with `https://lasthavens.com/play` in `MainActivity.kt`, `README.md`, `AGENTS.md` and `ISSUES.md`; updated `STATUS.md` and this journal.
- **Commit:** `bf5dec75e2933b1181917e73dbba4489485965e4` (URL source/docs); follow-up status/journal commit recorded separately by this checkpoint.
- **CI:** run #295 (`45498271033`) on the URL source/docs commit was pending when inspected. No successful build is claimed.
- **Limitations:** Firefox WebExtension implementation has not been committed. The fflate vendor distribution remains an external prerequisite, and injection must be validated against the live game.
- **Next safe action:** check CI #295, then continue #23 against the exact supplied specification and test desktop/Android Firefox behavior independently.


### 2026-11-25 — Owner placed addon manager work on hold

- **Branch:** `beta`.
- **Starting commit:** `5f3fbe619aed796b80979919bbe9a710d6ef1370`.
- **Action:** Stopped the Firefox WebExtension addon-manager workstream per explicit owner instruction. Updated issue #23, `ISSUES.md`, `AGENT_WORK.md`, `STATUS.md`, and this journal to mark it deferred and prohibit further assignment or implementation until the owner resumes it.
- **Files changed:** `STATUS.md`, `ISSUES.md`, `AGENT_WORK.md`, `WORK_LOG.md`, `CHANGELOG-AGENTS.md`.
- **Tests:** Documentation-only change; no application or extension tests run.
- **Limitations:** The old Android addon manager remains removed. The supplied Firefox extension specification is retained as context only; no extension source was added.
- **Next safe action:** Continue the non-addon project priorities (#4, #9, #11, #20, #21, #22). Do not touch #23 until explicit resume instruction.


### 2026-11-25 — Reprioritize project around Cefrium replacement

- **Issue/task:** #20 technical decision, #21 engine migration, #4 minimum build baseline.
- **Branch and starting commit:** `beta`, `a9d9b272a004a9a4dcc49fed0067f414ba3601cf`.
- **Files inspected:** `README.md`, `AGENTS.md`, `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md`, `AGENT_WORK.md`, `docs/GECKOVIEW_MIGRATION.md`, `app/build.gradle.kts`, GitHub issues #4/#20/#21, current beta Actions run #297.
- **Action:** Promoted #20 and #21 to P0 and updated their GitHub titles/scopes. Chaty owns #20 and #21. Ploy's #9/#11 work is paused. #4 is limited to verifying a clean post-removal baseline; unrelated updater enhancements are deferred until GeckoView is underway.
- **CI evidence:** Run #297, ID `45498642232`, commit `a9d9b272a004a9a4dcc49fed0067f414ba3601cf`; JDK 25, Android SDK 37, Wrapper validation, version metadata and signing-key restore completed. The signed release build/unit-test step was still in progress at the last inspection. No pass is claimed.
- **Research started:** Mozilla's official GeckoView quick-start confirms stable/beta/nightly channels, Mozilla Maven repository and Java 17 source/target compatibility flags. Third-party Maven indexing surfaced a candidate 160.x version, but the exact stable artifact has not yet been verified directly against Mozilla Maven; do not pin it yet.
- **Constraints:** No production dependency/source changes yet. Preserve Gradle 9.7.1, AGP 9.4.0, JDK 25, SDK 37, package ID, signing identity and arm64-v8a-only target until compatibility evidence supports a change. Keep the old Cefrium profile untouched.
- **Next safe action:** Monitor run #297 to completion and inspect logs; finish official stable artifact/API verification for #20; then begin #21.

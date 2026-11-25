# Work log — LastHavenPlus beta

**Purpose:** Durable restart point for the owner, Chaty and agents. This is an append-only operational journal, not a replacement for GitHub Issues or STATUS.md.

## Rules for every contributor

- Before changing files, record the issue, task branch, starting commit, intended file scope and acceptance criteria below.
- After each meaningful step (inspection, edit, test, commit, handoff), append a dated checkpoint with exact paths, commit SHA, commands and actual results.
- Record failures and skipped checks explicitly. Never write “passed” unless the command/run output proves it.
- At task end, record the exact handoff commit, changed files, tests, known limitations and next action.
- If interrupted, leave a final checkpoint stating the last completed action and the first safe next action. Do not amend or rewrite earlier checkpoints; append a correction if needed.
- Task work is submitted through PRs targeting `beta`. Chaty alone reviews and merges accepted PRs; contributors must not push directly to `beta`. Never push to `main` or publish without explicit owner approval.
- TickTick may be used as a supplementary personal task tracker. GitHub Issues and `ISSUES.md` remain the project sources of truth.

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


### 2026-11-25 — Start #20 source inventory and GeckoView artifact investigation

- **Issue/task:** #20 P0 technical decision for #21.
- **Branch and starting commit:** `beta`, `3b5787752940b42bbf31a8cab361446b06918a2b`.
- **Files inspected:** recursive beta Git tree; `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `LastHavenApplication.kt`, `MainActivity.kt`, Android manifest, CI workflow and Mozilla GeckoView quick start.
- **Files changed:** `docs/GECKOVIEW_MIGRATION.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`, `WORK_LOG.md`.
- **Action:** Recorded the exact Cefrium integration surface and the migration boundary. Identified a candidate GeckoView 160.0.20261116130455 generic artifact and ARM64-specific artifact from a Maven index; did not pin or add either because direct POM/AAR metadata/native contents remain unverified.
- **Checks:** Recursive tree inspection confirms only `MainActivity.kt` and `LastHavenApplication.kt` have direct Cefrium source usage; Gradle plugin/dependencies are in `app/build.gradle.kts`; Cefrium repository scopes are in `settings.gradle.kts`. Mozilla's official quick start confirms Java 17 source/target flags and Maven repository configuration.
- **CI:** Run #297 (ID `45498642232`) remains in progress at the last check. Its release build step emitted many R8 warnings from Chromium resource classes; no completion status or successful test result is available yet. Run #298 is pending behind it.
- **Limitations:** No GeckoView production code/dependency added. Stable artifact coordinates, POM, native AAR contents, exact API compatibility and final APK size remain unverified. Physical ARM64 game behavior remains unverified.
- **Next safe action:** Complete #20 artifact/API verification and obtain the #297 baseline result; then start #21 with only the current game-view behavior, leaving home-screen redesign for #22.


### 2026-11-25 — GeckoView 160 implementation started

- **Issue/task:** #21 P0.
- **Branch and starting commit:** `beta`, `f16238851ca8decd8400048b8734d5ccdb954d95`.
- **Code changes:** Removed Cefrium Gradle plugin, libraries and Codeberg repository. Added Mozilla Maven and `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`. Added lazy process-scoped runtime provider. Replaced Cefrium Compose host with GeckoView/GeckoSession and version-160 API delegates. Settings now measures `filesDir` as persistent app data including GeckoView, not as an exact profile-only value.
- **API evidence:** Mozilla Firefox `FIREFOX_160_0_RELEASE` public API/source confirms runtime/session/view and delegate method signatures. Maven index lists the ARM64 artifact, Mozilla repository, MPL 2.0 and 14 dependencies.
- **Safety:** Runtime is created lazily from the foreground app process, not Application.onCreate(), because Gecko child processes also instantiate the Application class. Existing Cefrium data is untouched. Persistent storage/autoplay permissions are scoped to lasthavens.com; other content permissions and media capture are denied by default.
- **Known follow-up:** Download responses are currently logged, not handled. Verify whether the game requires file downloads before considering that path complete. Session/profile persistence and real gameplay remain unverified.
- **CI:** Run #301 (ID `45500026644`) was pending at the time of this checkpoint; no build result is claimed.
- **Next:** Fix any compile/dependency errors from CI, then add tests and physical ARM64 verification.


### 2026-11-25 — GeckoView 160 API 37.2 build-gate correction and home-navigation intake

- **Issue/task:** #21 build compatibility prerequisite; owner request for #22 home screen and in-game Home menu action.
- **Branch and starting commit:** `beta`, `78be2ce796ceace711f4984f0353e7c5cfe4221a`.
- **Files inspected:** `README.md`, `AGENTS.md`, `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md`, `AGENT_WORK.md`, `WORK_LOG.md`, `docs/GECKOVIEW_MIGRATION.md`, `MainActivity.kt`, `app/build.gradle.kts`, `settings.gradle.kts`, `.github/workflows/android.yml`; GitHub issues #21 and #22; workflow run #302.
- **Finding:** beta source and Gradle configuration already contain GeckoView 160, despite stale documents claiming Cefrium. CI run #302 failed at AAR metadata validation: GeckoView requires compile SDK 37.2, but the project compiled against 37.0. Tests and signing/ABI/artifact checks did not run.
- **Files changed:** `app/build.gradle.kts` (`compileSdkMinor = 2`); `.github/workflows/android.yml` (install `platforms;android-37.2`); README and STATUS updated to describe the actual GeckoView source and verification boundary; issue #22 acceptance criteria updated to include Reload / Home / Return to game while preserving existing controls.
- **Commits:** `5853695b50dfd5d19bb256f08e25ddc0ec8c11db` (Gradle API 37.2); `b0d1b154e5eb49c8ef72909e2ba2f4b8fdbddf3d` (CI SDK package); `bd85506758cc497eff729a9e4bff73753bb1faee` (README); `eed0efece19409ae78c5df4884975f9dbf18066d` (STATUS).
- **Verification:** official Android Gradle Plugin API docs confirm `compileSdkMinor` / `minorApiLevel` configuration; run #302 logs confirm GeckoView's explicit 37.2 minimum. New run #304 (ID `45502893050`) was pending at the last check on commit `b0d1b154e5eb49c8ef72909e2ba2f4b8fdbddf3d`.
- **Not verified:** whether `platforms;android-37.2` resolves on GitHub runners; Kotlin compile, unit tests, signed APK, signer, ARM64-only payload, GeckoView runtime/profile persistence and physical ARM64 gameplay.
- **Next safe action:** inspect run #304. If green through compilation/tests/signing/ABI, proceed with #22 home screen and navigation as a separate UI task, preserving the same GeckoSession while moving between screens. Do not promote `beta` to `main` or publish a release.

## 2026-11-25 — Documentation reconciliation

- **Branch:** `beta`
- **Starting verified repository snapshot:** `cf1629ef1312daf3ed4cd7a07d41aff4123f462c` (backlog refresh commit; documentation updates only since the README cleanup).
- **Files updated:** README, agent instructions and assignments, issue index, release changelog and GeckoView migration notes. STATUS is being refreshed as the final audit snapshot.
- **Audit evidence:** Successful Android CI run #308 tested commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`. It ran `assembleRelease` and `testDebugUnitTest`; its release APK was ARM64-only and matched the established release signing certificate. Do not treat it as verification of subsequent workflow changes or physical hardware behavior.
- **Current boundaries:** GeckoView 160 is present in beta source. Home-screen/navigation work is assigned to Ploy under #22. Addon-manager work remains on owner hold. TickTick is supplementary; GitHub Issues and `ISSUES.md` are authoritative.
- **Not done:** No app source changed, no issue was closed, no release was published and `main` was not modified.
- **Next safe action:** Verify the CI optimization run and review the #22 implementation when Ploy hands it off. Keep physical ARM64 testing explicitly unverified until performed.


## 2026-11-25 — Update integration workflow to allow pull requests

- **Branch:** `beta`.
- **Change:** Replaced the active no-PR policy with PR-based task integration. Contributors use dedicated branches and open PRs targeting `beta`; Chaty alone reviews and merges accepted work. `beta` remains the active integration branch and `main` remains stable.
- **Scope:** Updated the active workflow instructions in README, AGENTS, AGENT_WORK, STATUS, ISSUES and this work log. Historical entries remain intact as an append-only record and are superseded by this entry.
- **Safety:** No application source, release configuration, `main` branch or release assets changed. PRs do not authorize promotion to `main`; explicit owner approval remains required.

## 2026-11-25 — Add PR validation to Android CI

- **Branch:** `beta`.
- **Change:** Added a `pull_request` trigger for PRs targeting `beta`. The PR-only job uses read-only repository permissions, builds the unsigned release variant and runs unit tests without loading signing secrets or uploading APKs. The existing branch/tag pipeline retains signed release and artifact verification.
- **Verification:** Workflow source updated. A successful Actions run has not yet been inspected; no CI pass is claimed.
- **Safety:** No application source, app ID, release signing identity, `main` or published release changed.


### 2026-11-25 — Chaty takes over home/navigation and updater implementation

- **Issues:** #22 home screen/navigation; #4 updater fallback and tests.
- **Starting branch/commit:** `beta`, `c8eb02a265012d7be898277443a53dd1e3589cad`.
- **Task branch:** `chaty/home-updater-demo` → `beta`.
- **Files inspected:** Required project docs, `MainActivity.kt`, `AppUpdater.kt`, `BsdiffPatch.kt`, existing BSDIFF tests, GeckoRuntime provider, Android manifest, Gradle config, CI workflow, release metadata, issues #4/#22 and open PR #25.
- **Files changed:** `MainActivity.kt`, `HomeScreen.kt`, `GameScreen.kt`, `GameMenuDialog.kt`, `AndroidManifest.xml`, `AppUpdater.kt`, `UpdateFallback.kt`, `UpdateVersionPolicy.kt`, `UpdateFallbackTest.kt`, `UpdateVersionPolicyTest.kt`; project status, assignment, issue, README, changelog and work-log records.
- **Implementation:** Home is the first destination; Play lazily creates the GeckoRuntime/GeckoSession and loads the configured game URL once. Home detaches the view without closing the session; returning does not issue another `loadUri`. Home Back confirms exit. Gameplay alone enables landscape orientation, immersive system bars and keep-awake. Settings/diagnostics restore normal system UI.
- **Updater:** Delta patch is attempted only for matching installed version/versionCode; base digest is checked before use. Any delta attempt failure, including patch download, BSDIFF reconstruction, output digest/size or package/signature validation, falls back to the full APK. Temporary files are removed on failures. Release asset URLs and post-response redirect hosts are checked for HTTPS GitHub destinations; manifest asset sizes are compared with GitHub metadata. Full APK validation still checks package name, version, signing certificate and SHA-256 before installer handoff.
- **Commits:** `c8b51abafb1d806b7fb64677c9d87c68e7970de1`, `32b286fc054b488501dc6c712c1662ed0cd97b12`, `2caa87dff8b88110a709e42bf4518b60891b9932`, `6443048ca2939ae731ed12313f34320cbca9b7a0`, `de2a88536f0ee201ce3059b2cfac52cbd8e274bd`.
- **PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26), head at this checkpoint `de2a88536f0ee201ce3059b2cfac52cbd8e274bd`.
- **CI:** PR runs #313 and #314 were cancelled by later pushes. Run #315 started for the current code head; a completed exact-head result has not yet been inspected. No local Gradle build was run in this environment. Do not report tests as passing.
- **Superseded handoff:** PR #25 was closed without merging; issue #22 is now owned by Chaty. Ploy has no active assignment.
- **Limitations:** No physical ARM64 device test; GeckoView game compatibility/profile persistence and actual update install remain unverified. No APK artifact has been produced from this PR-only job.
- **Next safe action:** Finish documentation commit, inspect the newly triggered PR validation run for the exact final head, correct any real compile/test failures, then review and merge to beta only when green. After merge, inspect the signed ARM64 beta artifact; do not publish a release.


### 2026-11-25 — PR #26 build and unit-test validation passed

- **PR head tested:** `283d649cd31ae009889d76affb25f655e28d3949`.
- **CI run:** [#317](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45515320999), job `PR build and unit tests (unsigned)`.
- **Result:** Passed. Logs show `compileReleaseKotlin`, `compileDebugKotlin`, `compileDebugUnitTestKotlin`, `testDebugUnitTest`, R8/minification, `packageRelease` and `assembleRelease`; final output `BUILD SUCCESSFUL in 2m 7s`. Unit-test report artifact `pr-unit-test-reports-26` (artifact ID `14976459805`) contains reports only, not an APK.
- **Skipped by design:** `Build and test (arm64)` and `Create release tag` were skipped for the PR event. This run did not use signing secrets, verify the signing certificate, verify final APK ABI contents or test on hardware.
- **Current live release metadata:** v1.0.0 exposes `lasthavenplus-arm64.apk` (120,381,565 bytes) and `lasthavenplus-update.json` (249 bytes); no differential patch asset is published. The full APK path is available in release metadata; no real download/install was performed.
- **Next safe action:** Complete final PR diff review, ensure current head checks remain green, merge PR #26 to beta, then inspect the exact beta push run for signed release, package identity, signing certificate, ARM64-only packaging and artifact contents. Do not publish a release or promote main. Physical gameplay/profile/update tests remain unverified.


### 2026-11-25 — Preserve stable-install compatibility for beta demo APKs

- **Finding:** The beta branch pipeline previously defaulted to versionName `0.1.0` and versionCode `1`, while the published v1.0.0 APK uses versionCode `1000000`. A signed beta demo built with the old default could be rejected as a downgrade when installed over the user's existing stable app.
- **Change:** PR #26 updates the `Resolve version metadata` workflow step. On `beta`, it reads the latest published stable release tag using the read-only workflow token, validates the semver tag and computes the same versionCode formula. It explicitly keeps `is_release=false` and an empty release tag, so this creates only the normal signed Actions artifact—no Git tag, update manifest, draft or published release.
- **Purpose:** Keep the application ID and signing identity stable and avoid a lower versionCode for the demo. The beta APK uses the same version identity as the latest stable release; a later stable release with a higher versionCode can update it normally.
- **Verification:** Workflow change is on the PR branch and awaits final PR CI. The post-merge beta build must confirm the resolved versionName/versionCode, signer, ARM64-only package and artifact. Same-version replacement and game profile preservation still require physical-device testing.

### 2026-11-25 — P0 hardening added to PR #26

- **Starting branch head:** `a1882b83e414d33091996096256770899bf9959d` on `chaty/home-updater-demo`.
- **Changes:** Updated `AppUpdater.kt` to use a single queued worker with a 30-second idle timeout; updated `MainActivity.kt` to save/restore `pendingUpdateUri` and close the retained GeckoSession when the Activity composition is destroyed; bounded decompressed BSDIFF streams and added an over-limit regression test; wired `app/proguard-rules.pro` into release minification with GeckoView/SnakeYAML keep rules.
- **Commits:** `91a87f5d1dd12f1ad13d44ee277e7d1be06bcd8b`, `a2c6bf98b823281a6de236f158be21dc670a1ed3`, `f655d179970f186f5f7f4a7432824dc073128bf1`, `86cb6211a291ebb3ea7a0d1134ed147b8447dfa3`, `269777616947ff3d5f2c6b4fb02924b19d55d6d3`, `f4891ab41b36fead89030a9fccdcabb9927cee8f`.
- **CI:** Run [#325](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45517931847) is in progress. The unsigned PR build/unit-test job has started; signed ARM64 build and release-tag jobs are skipped by design for PR validation.
- **Not verified:** No exact-head CI result yet; no physical ARM64 game, process-death installer, GeckoView minified startup or in-place update test has been performed.
- **Next safe action:** Inspect #325 to completion, address failures, then review PR #26. Do not merge to beta or main, tag or publish without explicit owner approval.


### 2026-11-25 — Final P0 PR validation queued

- **PR:** #26, branch `chaty/home-updater-demo` → `beta`.
- **P0 implementation commit:** `f4891ab41b36fead89030a9fccdcabb9927cee8f`.
- **Documentation head:** `5255a0aa4670f317b4b0a9b350652da22008c514`.
- **CI:** Run #325 was cancelled after additional documentation commits. Final-head run [#327](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518147065) is pending; its job list has not yet populated. No successful build or test is claimed.
- **Next:** Inspect run #327 (or the replacement run triggered by this checkpoint), then review exact PR diff. No beta merge or main promotion has been performed.

### 2026-11-25 — PR #26 merged into beta

- **Issue/task:** #22 home/navigation and #4 updater hardening/P0 blockers.
- **Branch and merge commit:** `beta`, `e924c580699ad34f1083d1a9a0fe96901037c0ae`.
- **PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26), merged using GitHub merge commit.
- **PR head:** `3a1396f7364be1e6aa658b3eb67be41549a906fb`.
- **Files:** `.github/workflows/android.yml`, central docs/agent assignments, `app/build.gradle.kts`, `app/proguard-rules.pro`, `AndroidManifest.xml`, `AppUpdater.kt`, `BsdiffPatch.kt`, `GameMenuDialog.kt`, `GameScreen.kt`, `HomeScreen.kt`, `MainActivity.kt`, `UpdateFallback.kt`, `UpdateVersionPolicy.kt`, and corresponding updater/BSDIFF tests.
- **CI:** PR run [#329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) passed the unsigned release build and unit tests for the exact PR head. Signed ARM64 branch CI for the merge commit is not yet verified.
- **Integration verification:** GitHub reported the PR merged; merge commit SHA recorded. No release tag or publication created. No direct push to `main`.
- **Unverified:** Physical ARM64 game compatibility, Gecko profile persistence, installer URI restoration after actual process death, real updater installation/fallback, signed artifact contents for the merge commit.
- **Branch state:** `main` and `beta` remain diverged: beta ahead 166, main ahead 9; merge base `2455c541729d20f00759ac9fa258faaaa473efcf`. Nine main-only commits must be reviewed/reconciled before promotion.
- **Next safe action:** Verify exact merge commit signed ARM64 CI/artifact; inspect the nine main-only commits; do not promote or publish until all release gates pass.


### 2026-11-25 — Signed beta integration verified; main promotion PR prepared

- **Beta merge commit:** `e924c580699ad34f1083d1a9a0fe96901037c0ae`.
- **CI:** Run [#330](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518704468) passed signed `assembleRelease`, unit tests, signature verification and ARM64-only ABI check for the exact merge commit.
- **Version/artifact:** latest stable `1.0.0` / versionCode `1000000`; signed ARM64 artifact uploaded (92,919,786 bytes). No release tag or publication.
- **Certificate:** matched established SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263`.
- **Main-only history:** reviewed the nine unique main commits: seven remove internal documentation, one corrects the published v1.0.0 changelog, and one enables beta CI. The promotion branch preserves the internal-doc removals, retains the corrected historical v1.0.0 notes and keeps the updated workflow.
- **Promotion PR:** [#27](https://github.com/iNeonyne/LastHavenPlus/pull/27) is a draft. A two-parent merge checkpoint includes current main ancestry and preserves the promotion tree; GitHub API reports `mergeable=true`, `mergeable_state=clean`.
- **Not verified:** physical ARM64 gameplay/profile persistence, process-death installer restoration and actual differential/full update installation.
- **Next safe action:** keep PR #27 draft until physical device gates and promotion diff checks are complete. Do not merge main or publish a release yet.


### 2026-11-25 — Updater hardening candidate and release follow-up triage

- **Starting beta HEAD:** `b7d9f3256527de41a45ff8c935fac510be635a69`.
- **Branch:** `chaty/fix-updater-hardening`.
- **Candidate HEAD:** `4b5ecb6d4db4276983f98aff3cfb81d69b8fd882`.
- **Files changed:** `AppUpdater.kt`, `MainActivity.kt`, `UpdateNetworkPolicy.kt`, `UpdateVersionPolicy.kt`, `UpdateFallback.kt`, `UpdateNetworkPolicyTest.kt`, `UpdateFallbackTest.kt`.
- **Changes:** Added per-hop HTTPS redirect validation against exact GitHub hosts, bounded JSON response bodies, bounded/idle-retiring updater execution, shared download size/digest validation, unique temporary/final update file names, process-death-safe installer permission handoff state, and diagnostic-failure-safe full APK fallback. Added JVM tests for network policy and fallback resilience.
- **Issue triage:** Updated physical device issue #5 to the canonical `https://lasthavens.com/play` URL and included updater recovery tests. Created #28 for storage measurement accuracy/iterative traversal and #29 for stale documentation/CI evidence. Existing #4 and #15 remain open.
- **Verification:** No Gradle build or test command has been run in this environment. Candidate is not integrated; exact-head PR CI is the next verification step.
- **Next safe action:** Open a PR targeting `beta`, inspect CI for exact candidate SHA, then update #4/#15 and STATUS with evidence. Do not merge to main or publish.


### 2026-11-25 — Extend updater manifest validation coverage

- **PR:** [#30](https://github.com/iNeonyne/LastHavenPlus/pull/30).
- **Candidate HEAD:** `c68db2c8704813822c27b86dfb48cffbb84eb87f`.
- **Changes:** Extracted manifest schema/version/versionCode/APK-name/size validation and exact delta base compatibility into a pure policy; added base APK digest validation helper. Added tests for malformed/inconsistent manifest fields, base version/code mismatch and base digest mismatch.
- **CI:** Exact-head run [#340](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45521280150) is pending. No pass claimed.
- **Correction:** Two new policy files were briefly created on main by a contents-API call without an explicit branch parameter; both were immediately deleted. A compare against the pre-change main commit confirms zero file differences; main's tree is unchanged. All intended policy files are present on the PR branch.
- **Next safe action:** Wait for run #340, inspect logs/results and review the final PR head before merging into beta.


### 2026-11-25 — Finalize updater candidate documentation checkpoint

- **PR:** [#30](https://github.com/iNeonyne/LastHavenPlus/pull/30), targeting beta.
- **Latest candidate before this documentation checkpoint:** `df82f40feca9eed7af318403e93a284ec692edad`.
- **Tests in scope:** URL/redirect allowlist, bounded response parsing, download size/digest, release manifest schema and metadata, patch base compatibility/digest, and full-APK fallback resilience.
- **CI:** Unsigned PR build and unit-test validation is pending for the latest PR head. No pass is claimed.
- **Main safety:** The unintended temporary files were deleted from main; comparing main against its pre-incident HEAD shows zero changed files. Main's content tree is restored.
- **Next safe action:** Inspect exact-head CI, correct any failures, review PR #30 and only then merge to beta. Physical updater installation remains unverified under #5.


### 2026-11-25 — Add recovery cleanup for interrupted updater downloads

- **PR:** [#30](https://github.com/iNeonyne/LastHavenPlus/pull/30).
- **Code commit:** `5c51978d1499c369fbc0a4e5801635c08d9b48b1`.
- **Change:** At the start of a new explicit download, remove leftover .part files from an interrupted prior process. Verified APK files are intentionally preserved. Added a JVM test covering partial APK/patch deletion and verified APK preservation. Also made response-body reading robust to zero-length reads and tightened fallback diagnostic exception handling.
- **CI:** PR run [#344](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45521735696) is pending for the code commit. No pass is claimed.
- **Next safe action:** Inspect CI on the final PR head after this documentation checkpoint. Keep physical update installation under #5.


### 2026-11-25 — Apply right-sized PR review policy

- **Starting beta HEAD:** `c925a93fbc039a9985f4a8577c2d3a6958b18dd9`.
- **Branch:** `chaty/workflow-efficiency-policy`.
- **Files:** `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`, `WORK_LOG.md`.
- **Change:** Added lightweight, standard and strict review levels. Lightweight applies to docs/copy/isolated low-risk changes; standard to contained application changes; strict to GeckoView/session, updater, storage/profile, security/network, toolchain, manifest/signing and CI/release work. All PRs still receive Chaty review and required CI.
- **Efficiency controls:** Keep one issue per PR, avoid unrelated cleanup and overlapping scopes, require concise evidence-based handoffs, and refresh/retest branches when beta advances.
- **Verification:** Documentation-only changes; no application build or tests run. Exact-head PR CI is still required by the repository workflow. No branch merge, main promotion, tag or release publication performed.


## 2026-11-25 — PR #43 integration and v2.1.0 documentation

- PR #43 exact-head validation run #386 passed for `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3`.
- Merged PR #43 into beta; merge commit `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` is the verified beta branch HEAD.
- Post-merge signed build run #45601558602 was still in progress at the time of this entry. No signed APK, v2.1.0 tag or release is claimed.
- Started docs reconciliation issue #29 on branch `chaty/docs-reconciliation-v2.1`. Reconciles current launcher behavior, owner-approved-but-not-implemented XPI scope, CI evidence and outstanding physical-device verification.


### 2026-11-25 — Repository housekeeping inspection and reconciliation started

- **Task:** Reconcile repository records after v2.1.0 beta documentation PR #46 and main promotion PR #47.
- **Starting beta HEAD:** `854fce1b1e2989cb012ebf5a15d196d91ff7cad7` (PR #46 merge).
- **Files inspected:** README.md, AGENTS.md, STATUS.md, ISSUES.md, AGENT_WORK.md, CHANGELOG-AGENTS.md, WORK_LOG.md and docs/GECKOVIEW_MIGRATION.md; open GitHub issues, pull requests, branch names and relevant PR/issue records.
- **Findings:** no open PRs returned; issue #29 is closed after PR #46 but remained marked open in STATUS/ISSUES; STATUS still described run #45601558602 as pending despite PR #47 recording it passed; STATUS did not record the completed promotion; AGENT_WORK still assigned the completed #29 task; addon-manager docs and issue #23 still said approved to resume despite the owner's later hold instruction.
- **Branch:** `chaty/housekeeping-doc-reconciliation` from beta.
- **Scope:** repository records and #23 issue status only. No source, build, release, signing or game files.
- **Verification:** no application tests run; documentation changes require diff/link review. Physical ARM64 feature-matrix verification remains unverified.
- **Next safe action:** inspect the docs-only diff and submit a PR targeting beta. Do not merge without reviewing the exact diff and CI policy.


### 2026-11-25 — Housekeeping PR #49 merged

- **PR:** [#49](https://github.com/iNeonyne/LastHavenPlus/pull/49), squash-merged into `beta`.
- **Merge commit:** `1f661cb6234dcfdfd051d0cb963e739668896a26`.
- **Result:** reconciled STATUS, README, AGENTS, ISSUES, AGENT_WORK and GeckoView notes; issue #29 is documented as completed; issue #23 is on hold; issue #31's PR and verification history is corrected.
- **Tests:** no application tests run; documentation-only. PR validation query returned no PR-triggered workflow runs for the documentation commit. The existing signed beta evidence cited in PR #47 remains the relevant build evidence.
- **Physical-device status:** full ARM64 feature matrix remains unverified.
- **Next safe action:** inspect remaining stale branches and release-tag/draft state; do not delete branches without confirming their PR/commit dependencies.


### 2026-11-25 — Roadmap and branch housekeeping audit

- Updated the backlog's release-roadmap statement: PR #47 promoted the v2.1.0 candidate to `main`; it is not asserted to be published.
- Kept physical ARM64 feature verification and the exact v2.1.0 tag-build artifact checks as release blockers.
- Corrected issue #31's signed-beta evidence to cite run #45601558602; release-tag differential fallback behavior remains unverified, so #31 stays open.
- Reaffirmed issue #23 is on hold and removed addon-manager integration from the active roadmap.
- Audited 27 branch names and compared the retained Chaty task branches against beta. No open PRs were found. Branch deletion was not performed because the connected GitHub actions do not expose a branch-delete operation; diverged/unique commits must be preserved until safely reviewed and deleted through an authorized GitHub interface.
- Documentation-only change. No app tests or physical-device checks were run.


### 2026-11-25 — Close completed Home implementation and rescope UI follow-up

- Closed #22 as completed: Home/Play/Settings source implementation is integrated in PR #26; the older menu behavior was superseded by #42.
- Retitled #42 to focus on physical ARM64 verification of the v2.1.0 UI already integrated by PR #43.
- Replaced the stale #18 roadmap with current candidate-promotion state and the remaining physical-device/final-artifact gates. Addon manager #23 remains on hold.
- Updated #4/#31 with the signed beta CI evidence from run #45601558602 without claiming physical updater verification or release-tag differential fallback coverage.
- No app tests or device tests were run for this documentation/issue-tracker work.


### 2026-11-25 — Merge roadmap and backlog housekeeping PR #51

- PR #51 merged to beta at `b711af8e9c0dd35032edc21a1c3f7faacd4c343c`.
- The v2.1.0 roadmap now records candidate promotion to main, physical ARM64 and final versioned-artifact gates, and the addon-manager hold.
- Closed #22 as completed source implementation; #42 now tracks physical v2.1.0 UI verification.
- #4/#31 retain open updater/release-workflow work with signed beta CI evidence recorded.
- Docs-only PR; no app tests or device checks were run. Branch pruning remains deferred because the connected GitHub actions expose no delete-ref operation.


### 2026-11-25 — Verify published v2.1.0 and reconcile release records

- GitHub confirms release v2.1.0 is published (not draft/prerelease) from tag commit `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.
- Release run #45603533787 built versionName 2.1.0 / versionCode 2001000; unit tests, signature verification, established certificate match, ARM64-only ABI check, differential generation and release upload/publish steps succeeded.
- The final release asset query failed with HTTP 404. The current release metadata independently confirms one canonical APK, one update manifest and one differential patch. Issue #31 tracks this workflow verification failure.
- Updated beta-facing release records to distinguish the published artifact from remaining physical ARM64 feature testing.
- Stable-facing README/CHANGELOG on main remain candidate wording; correcting main requires a separate stable-branch documentation action under repository policy.
- No app tests or physical-device checks were run for this documentation/record reconciliation.

## 2026-11-25 — Harden updater redirects, downloads and installer recovery

- **Role:** Chaty / updater implementation
- **Branch:** `chaty/fix-updater-hardening`
- **Starting beta HEAD:** `b7d9f3256527de41a45ff8c935fac510be635a69`
- **PR:** [#30](https://github.com/iNeonyne/LastHavenPlus/pull/30), targeting `beta`.
- **Candidate HEAD at checkpoint:** `5c51978d1499c369fbc0a4e5801635c08d9b48b1`
- **Files:** `AppUpdater.kt`, `MainActivity.kt`, `UpdateNetworkPolicy.kt`, `UpdateVersionPolicy.kt`, `UpdateFallback.kt`, `UpdateNetworkPolicyTest.kt`, `UpdateManifestPolicy.kt`, `UpdateManifestPolicyTest.kt`, `UpdateFileCleanup.kt`, `UpdateFileCleanupTest.kt`, `UpdateFallbackTest.kt`
- **Change:** Replaced automatic redirects with a bounded manual redirect loop that validates each HTTPS destination against exact GitHub hosts; bounded release/manifest JSON bodies; bounded updater queue with an idle-timeout daemon worker and explicit busy callback; centralized downloaded size/SHA-256 checks; staged patch/APK data in unique temporary files and generated unique final APK paths; removes crash-abandoned .part files on the next explicit download while preserving verified APKs; persisted the pending installer URI before leaving for Android install-permission settings and restored it after process death; prevented diagnostics callback errors from blocking full APK fallback.
- **Tests added:** Redirect allowlist and rejection cases (including untrusted hosts, credentials, ports and fragments), redirect status codes, response-size limit, download size/digest validation, manifest field validation, patch base compatibility/digest validation, partial-file cleanup and fallback when diagnostics fails.
- **Verification:** Unsigned PR build/unit-test validation is pending for the latest PR head. No build/test pass is claimed. Physical installer handoff and update installation remain unverified.
- **Related issues:** #4 updater tests, #15 recovery, #5 physical ARM64 update verification; new backlog issues #28 storage accuracy and #29 documentation reconciliation.
- **Next:** Inspect exact-head unsigned build/unit-test CI and correct any failures before review/merge.

## 2026-11-25 — Verify signed ARM64 beta integration

- **Role:** Chaty / integration and release coordination
- **Commit:** `e924c580699ad34f1083d1a9a0fe96901037c0ae`
- **Workflow:** [Actions run #330](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518704468)
- **Result:** `assembleRelease` and `testDebugUnitTest` passed. Release APK signature verified; certificate SHA-256 matched the established key. Packaged native ABI was exactly `arm64-v8a`.
- **Version identity:** CI resolved stable `1.0.0` / versionCode `1000000` for the beta demo artifact. Tag creation, draft release, differential release package generation and publication were skipped.
- **Artifact:** `lasthavenplus-release-signed-arm64`, 92,919,786 bytes; artifact ZIP SHA-256 `67d9183585f451d062f217ff5106844de2f68501352fa3ad7e56db3faf6c41ca`.
- **Not verified:** physical ARM64 gameplay, profile persistence, process-death installer restoration and actual updater installation.
- **Next:** Complete physical device gates before promoting to main or publishing.
## 2026-11-25 — Merge PR #26 into beta

- **Role:** Chaty / integration owner
- **PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26)
- **Target branch:** `beta`
- **PR head:** `3a1396f7364be1e6aa658b3eb67be41549a906fb`
- **Merge commit:** `e924c580699ad34f1083d1a9a0fe96901037c0ae`
- **Scope:** Home/Play/Settings navigation, session-preserving Home flow, Home Back exit confirmation, gameplay-scoped orientation/immersive/keep-awake, updater delta-to-full fallback, exact GitHub URL/redirect checks, BSDIFF decompression limits, updater worker idle timeout, pending installer URI saved state and GeckoView R8 rules.
- **CI:** PR run [#329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) passed unsigned release build and unit tests for the exact PR head. This is not signed ARM64 post-merge evidence.
- **Not verified:** Physical ARM64 gameplay, Gecko profile persistence, installer process-death restoration, real differential/full update installation and signed artifact checks for the merge commit.
- **Main promotion:** Not performed. `main` and `beta` are diverged; beta is 166 commits ahead and 9 behind main. The nine main-only commits require review before a safe promotion.
- **Next:** Verify signed ARM64 CI/artifact for the exact beta merge commit, review main-only commits, then prepare promotion only after release gates.
## 2026-11-25 — Add P0 security and lifecycle hardening to PR #26

- **Role:** Chaty / implementation and integration
- **Branch:** `chaty/home-updater-demo` → `beta`
- **PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26)
- **P0 code head:** `f4891ab41b36fead89030a9fccdcabb9927cee8f`; **latest docs head:** `5255a0aa4670f317b4b0a9b350652da22008c514`
- **Files:** `AppUpdater.kt`, `MainActivity.kt`, `BsdiffPatch.kt`, `BsdiffPatchTest.kt`, `app/build.gradle.kts`, new `app/proguard-rules.pro`
- **Change:** Bound decompressed BSDIFF control/diff/extra output and added an expansion regression test. Replaced the permanent updater worker with a queued worker that retires after 30 seconds idle. Persisted the pending installer URI in Activity saved state. Kept GeckoSession open across Home, but close it when the Activity composition is destroyed. Added GeckoView/R8 and SnakeYAML keep rules and wired them into release minification. Exact-host URL validation and Home Back exit confirmation were already implemented in the PR candidate.
- **Verification:** Run [#325](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45517931847) was cancelled by subsequent documentation commits. Final-head run [#327](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518147065) is pending. No build/test pass is claimed yet. Signed branch CI and physical ARM64 testing have not been performed for this candidate.
- **Constraints:** No changes to application ID, signing identity, GeckoView/Gradle toolchain versions, ABI policy, game URL or game client. No automatic update download/install, addon-manager work, beta merge, main promotion or release publication.
- **Next:** Inspect run #325, correct any failures, complete PR review, and request owner approval before integration/promotion.
## 2026-11-25 — Chaty takes over home/navigation and updater hardening

- **Role:** Chaty / implementation and integration
- **Branch:** `chaty/home-updater-demo` targeting `beta`
- **PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26)
- **Files:** `MainActivity.kt`, `HomeScreen.kt`, `GameScreen.kt`, `GameMenuDialog.kt`, `AndroidManifest.xml`, `AppUpdater.kt`, new `UpdateFallback.kt`, new `UpdateVersionPolicy.kt`, updater unit tests, and central project documentation
- **Change:** Replaced the failed #25 implementation with a Chaty-owned home/navigation implementation. Game initialization is lazy, the existing GeckoSession is retained across Home, and Home Back confirms exit. Orientation, immersive system UI and keep-awake are scoped to gameplay; Settings and diagnostics restore normal system UI. Updater delta failures now fall back to the full APK after re-verifying package identity and signing certificate. Partial files are cleaned; GitHub release URLs/redirect hosts and asset sizes are checked. Added unit tests for fallback policy and version/digest/URL validation.
- **Why:** The #25 implementation failed PR CI and had compile/navigation/session defects. The updater previously did not fall back to the full APK after a delta download or reconstruction failure.
- **Verified:** Source changes committed on the task branch; PR #26 targets current beta. PR runs #313–#316 were cancelled by follow-up pushes. Run [#317](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45515320999) passed `assembleRelease` and `testDebugUnitTest` for code SHA `283d649cd31ae009889d76affb25f655e28d3949`. The later beta-version-resolution workflow change has not yet passed final-head CI. The PR job is unsigned and skips signed ARM64 checks as intended. No physical-device result is claimed.
- **Superseded:** PR #25 was closed without merging. Ploy has no active assignment on #22.
- **Additional demo packaging fix:** Beta branch builds resolve the latest published stable tag and reuse its versionName/versionCode, while leaving `is_release=false`. This avoids a lower-versionCode beta APK that Android would reject over the existing stable installation; beta builds remain signed CI artifacts only and do not create a tag, update manifest or GitHub release.
- **Constraints:** No Gradle/AGP/JDK/SDK/GeckoView changes, no app ID or signing changes, no automatic updates, no addon manager work, no main promotion or release publication.
- **Next:** Complete final diff review and merge into beta only while the current PR checks are green. Then verify signed ARM64 branch CI. Physical ARM64 gameplay and updater installation remain unverified.

## 2026-11-25 — Bound Android CI runner time and queue optimization verification

- **Role:** Chaty / CI orchestration
- **Files:** `.github/workflows/android.yml`
- **Change:** Added a 30-minute timeout to the Android build job as a guard against stalled runner use. This is in addition to Markdown-only path filtering, shallow checkout for ordinary branch builds, and allowing Gradle daemon reuse.
- **Why:** Bound worst-case runner consumption while the optimized workflow is being exercised.
- **Verified:** Workflow change committed to `beta` as `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`. The commit is present and the workflow source contains the timeout. The Actions run result has not yet been retrieved; no successful run or time saving is claimed.
- **Do not redo:** Keep all build, unit-test, signing, ABI and release ancestry checks. Do not skip CI for source, resource, Gradle, wrapper, manifest or workflow changes.
- **Follow-ups:** Inspect the run triggered by this workflow commit, confirm the workflow parsed and completed, then compare elapsed time with run #308.

## 2026-11-25 — Optimize routine Android CI

- **Role:** Chaty / CI orchestration
- **Files:** `.github/workflows/android.yml`
- **Change:** Markdown-only pushes to `main` and `beta` no longer start Android builds; branch builds use a shallow checkout while version-tag builds retain full history for stable-main ancestry validation; Gradle is no longer forced into `--no-daemon` mode.
- **Why:** Avoid spending runner time compiling unchanged Android code for documentation edits and reduce unnecessary repository checkout work without weakening build, unit-test, signing, ABI or release verification.
- **Verified:** Workflow change committed to `beta` as `54b947279a783c6c382210e65045edd02bd5ee73`. The workflow file itself still triggers CI. Awaiting that run to validate the revised workflow and measure the actual result.
- **Do not redo:** Do not skip CI for Kotlin, resources, Gradle, wrapper, Android manifest, workflow or other build-affecting changes. Do not remove unit tests, signing checks, ABI checks or release-tag ancestry validation.
- **Follow-ups:** Inspect the CI run for this commit; compare duration against run #308 and update `STATUS.md` with the exact evidence.

## 2026-11-25 — Remove legacy addon manager and add durable agent references

- **Role:** Chaty / main orchestrator
- **Files:** `MainActivity.kt`, deleted `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt`, `AddonArchiveInspectorTest.kt`, new `docs/GECKOVIEW_MIGRATION.md`, new `WORK_LOG.md`, `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, GitHub issues #8, #9, #11, #13, #20 and #23
- **Change:** Removed the old addon manager UI, repository, archive inspector and tests from beta. Removed its entry/state from MainActivity. Added a curated GeckoView/Mozilla reference guide and an append-only restart journal. Reassigned Ploy to the small menu accessibility task #9, then diagnostics tests #11; future settings work is limited to an isolated URL preference task after the engine/home contract is ready. Reframed #23 as blocked pending the owner's new addon-manager specification.
- **Why:** The owner will provide a new addon-manager design. Keeping the old implementation or asking agents to reuse it would conflict with that direction. Durable checkpoints prevent lost context after interruption.
- **Verified:** GitHub beta tree no longer contains the four deleted addon source/test files. MainActivity no longer contains the addon manager state, menu action or dialog call. Mozilla official quick start, architecture, Javadocs, permissions and WebExtension references were checked and summarized in `docs/GECKOVIEW_MIGRATION.md`. CI run #268 (`45496604607`) was pending at last inspection.
- **Do not redo:** Do not restore the old ZIP manager or invent its replacement. Do not delete unrelated user app data. Do not copy Mozilla's full documentation into the repository; use the official links and curated notes. Do not claim the app compiles until post-removal CI passes.
- **Follow-ups:** Check run #268; search for any dangling references; Ploy completes #9 then #11; Chaty owns clean beta build and #21; wait for owner addon-manager instructions before new design/assignment.
## 2026-11-25 — Plan GeckoView migration, home screen and addon manager placement

- **Role:** Chaty / main orchestrator
- **Files:** `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md`, GitHub issues #5, #6, #7, #8, #11, #12, #17, #18 and new issues #20–#23
- **Change:** Recorded the approved transition from Cefrium to GeckoView, created separate work items for Gecko research, engine replacement, the Play/Settings home screen and removing the legacy addon manager pending new owner requirements. Reworked the roadmap and queued non-overlapping ownership after the current build/accessibility tasks.
- **Why:** The app currently launches directly into Cefrium and its Activity applies gameplay UI state globally. The engine change, app shell redesign and local addon manager should be staged independently to avoid overlapping changes and regressions.
- **Verified:** Inspected beta source, Gradle configuration, manifest, existing ZIP archive inspector/repository/dialog, current issues, branch head and latest CI run #247. GeckoView official documentation describes the Runtime/Session/View architecture and separate permission/WebExtension APIs. Run #247 fails on two Compose errors in `AddonManagerDialog.kt`; tests and APK verification did not run. The existing addon manager is local package management only, not game injection.
- **Do not redo:** Do not treat GeckoView as a drop-in dependency rename. Do not assume Cefrium profile data can be imported. Do not rebuild the addon manager or equate WoW addon ZIPs with Gecko WebExtensions. Do not change application ID, signing identity, ABI, build toolchain, game client or updater policy as part of preparation.
- **Follow-ups:** Ploy completes #13 → #11 → #8 → #20 in sequence; Chaty completes #4, reviews #20 and owns #21; Ploy then owns #22 and #23 after the required integrations. No release or main promotion.

## 2026-11-25 — Restrict release tags to stable main

- **Role:** Chaty / CI orchestration
- **Files:** `.github/workflows/android.yml`, `gradle/README.md`, `ISSUES.md`, GitHub issue #4
- **Change:** Manual release-tag creation now requires the selected ref to be `main`; pushed version tags are rejected unless their commit is contained in stable `main`. Updated the Gradle wrapper reference and marked the verified wrapper setup work complete in issue #4.
- **Why:** Prevent a manual dispatch or tag push from producing a release candidate from unpromoted beta code, and keep build instructions aligned with the committed wrapper.
- **Verified:** Workflow source inspected after the change; no release dispatch, tag creation or publication was performed. Current beta compilation remains blocked by the #13 source error.
- **Do not redo:** Do not loosen the stable-main release gate or create release tags from beta.
- **Follow-ups:** Confirm a normal beta CI run accepts the workflow syntax; complete #13 compile fix and #4 updater tests.

**Workflow history note (2026-11-25):** Workflow entries below are chronological. The latest policy update supersedes earlier no-PR instructions: PRs are allowed and are the standard path for task integration into `beta`. Chaty alone reviews and merges; GitHub Issues and `ISSUES.md` remain the task sources of truth. TickTick may be used as a supplementary tracker.

## 2026-11-25 — Audit beta workflow and correct project documentation

- **Role:** Chaty / main orchestrator
- **Files:** `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md`
- **Change:** Reasserted `beta` as the active integration branch and `main` as stable; removed active PR-based handoff instructions; made Chaty the sole reviewer/integrator/pusher to `beta`; reconciled Gradle Wrapper documentation and issue #4 with the wrapper already present; replaced stale status claims with the current beta commit, CI evidence, feature verification states and blockers.
- **Why:** Existing docs contradicted the agreed no-PR workflow, claimed the wrapper was missing, and did not accurately describe the latest failing beta CI run.
- **Verified:** Inspected the required project documents, repository tree, source files, GitHub issues, branch heads, recent commits, workflow definition, CI jobs/logs/artifacts and v1.0.0 release assets. Current beta run #232 fails compilation at `AddonManagerDialog.kt:166` and `:171`; tests and APK verification were skipped. Wrapper setup/validation passed. No app/game source, application ID, signing identity, tag, release or main branch was changed.
- **Historical instruction (superseded 2026-11-25):** This entry originally prohibited pull requests. The later workflow update in this file explicitly authorizes PRs targeting `beta`; follow the latest policy. Do not add a second Gradle distribution or alter the pinned toolchain. Do not claim device testing or current-beta build success from an older run or published release.
- **Follow-ups:** Resolve the #13 compile failure under exclusive ownership; rerun beta CI; add a main-only guard to manual release tag creation; complete updater tests and physical ARM64 verification.

## 2026-11-24 — Standardize issue intake and lifecycle for future work

- **Role:** Chaty / project coordination
- **Files:** `.github/ISSUE_TEMPLATE/*`, `AGENTS.md`, `AGENT_WORK.md`, `ISSUES.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Added structured GitHub issue forms for bug reports, feature requests and engineering tasks. Documented duplicate checks across open/closed issues, required scope and testable acceptance criteria, priority and release triage, dependencies, explicit lifecycle states, one-owner enforcement, PR linking and evidence-based closure. Clarified Chaty owns issue intake/central tracking and Ploy only executes assigned work.
- **Why:** Make it safe to grow the backlog without duplicate tasks, unclear ownership, vague requests or issues being closed before they are truly complete.
- **Verified:** Existing GitHub issue inventory reviewed (#4–#18); no new issue created and no existing issue state changed. Templates added to `beta`.
- **Do not redo:** Do not introduce parallel task IDs or allow Ploy to create/assign/close issues. Do not treat open as ready or priority as a release commitment.
- **Follow-ups:** Apply the templates to future requests; reconcile existing issue metadata only when its scope/ownership is next reviewed.

## 2026-11-24 — Make roadmap beta-first and enforce exclusive task ownership

- **Role:** Chaty / project coordination
- **Files:** `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, `README.md`, `CHANGELOG-AGENTS.md`
- **Change:** Reworked the roadmap around beta integration and explicit stable-main promotion gates. Defined the next stable release as conditional on scope freeze, CI, signed ARM64 artifact verification, regression testing, applicable physical-device checks, beta soak and explicit user approval. Added exclusive issue/task ownership and clarified that Ploy's #13 → #11 → #8 sequence pauses overlapping Chaty work until handoff.
- **Why:** Make release timing evidence-based and prevent duplicate/conflicting work.
- **Verified:** Documentation consistency checked against beta-first branch model. No code, release, tag or branch promotion performed.
- **Do not redo:** Do not set a fixed release date or allow concurrent ownership of the same issue.
- **Follow-ups:** Keep STATUS ownership current and check it before every task claim/reassignment.

## 2026-11-24 — Establish beta-first integration workflow

- **Role:** Chaty / project coordination
- **Files:** `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md`
- **Change:** Preserved the previous unrelated `beta` history as `beta-legacy-2026-11-24`, then reset `beta` to the current `main` commit. Updated agent instructions so Chaty pushes directly to `beta`, Ploy opens task PRs targeting `beta`, accepted PRs are integrated into `beta`, and `beta` is promoted to stable `main` only after verification and explicit user approval.
- **Why:** Keep stable releases isolated from ongoing integration while allowing both agents to contribute to one testable beta line.
- **Verified:** The old beta commit is reachable from the backup branch; `beta` now points at the former `main` head. Documentation updates are committed to `main` and then synchronized to `beta`.
- **Do not redo:** Do not delete the legacy backup branch. Do not direct routine development to `main` or automatically promote beta.
- **Follow-ups:** Ploy's current task branch must be based on `beta` and its PR must target `beta`; continue only after Chaty reviews the current task.

## 2026-11-24 — Allow Ploy to submit pull requests

- **Role:** Chaty / project coordination
- **Files:** `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Replaced the user-push handoff with a PR workflow. Ploy may create a dedicated branch, push assignment commits and open a PR, but may not push to `main` or edit central tracking files. Chaty reviews the PR diff and CI, maintains task tracking, and merges only after the user explicitly approves.
- **Why:** Let Ploy hand off completed work directly for review while preserving a protected, orchestrated `main` branch and the user's final merge decision.
- **Verified:** Repository instructions and status documentation updated. This is a process change only; no branch protection or GitHub repository settings were changed.
- **Do not redo:** Do not restore the manual user-push workflow. Do not merge Ploy's PRs without explicit user approval.
- **Follow-ups:** Use this workflow for Ploy's current #13 assignment; review the PR and CI before requesting merge approval.

## 2026-11-24 — Expand BSDIFF failure-path tests

- **Role:** Chaty / updater QA
- **Files:** `app/src/test/java/com/neonyne/lasthavenlauncher/BsdiffPatchTest.kt`, `ISSUES.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Added tests that assert truncated BSDIFF headers, unsupported patch magic and declared output sizes above the configured limit are rejected, and that a stale/partial output file is removed on failure. Recorded the active #4 work and CI blocker in the task tracker.
- **Why:** Increase confidence that malformed differential updates cannot leave a candidate APK output behind.
- **Verified:** Commit `29dd5aa` triggered CI run #45474077737; at documentation update time the build/test step was still running. A preceding run failed three `AddonArchiveInspectorTest` cases (`parsesWowTocMetadataAndArchiveFingerprint`, `rejectsCaseInsensitiveDuplicatePaths`, `rejectsArchivesWithoutTocFiles`); these unrelated test failures block the common unit-test task and require diagnosis. No passing result is claimed.
- **Do not redo:** Do not mark the BSDIFF test requirement complete until CI passes. The tests are not a substitute for manifest validation, patch corruption/truncation coverage or interrupted-download tests.
- **Follow-ups:** Inspect the latest CI report, diagnose the three existing addon archive test failures, then add pure unit coverage for updater manifest/version validation and failed-download handling. Continue Gradle Wrapper work only with the official wrapper JAR and verified distribution checksum.

## 2026-11-24 — Clarify Ploy handoff and review workflow

- **Role:** Chaty / project coordination
- **Files:** `AGENT_WORK.md`, `AGENTS.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Updated the delegated workflow so the user pushes Ploy's completed work to the repository. Chaty then inspects the pushed diff, verifies relevant tests, reports review findings and maintains `ISSUES.md`, `STATUS.md` and this changelog. Ploy does not push directly to `main`, edit central tracking files or start the next task without acceptance.
- **Why:** The user is responsible for publishing Ploy's handoff; Chaty's responsibility is review and accurate task tracking.
- **Verified:** Documentation-only workflow update committed to `main`. No application code or tests changed.
- **Do not redo:** Do not expect Ploy to access GitHub Issues or update the tracker. Do not mark a task complete merely because Ploy reports it done; review the pushed diff first.
- **Follow-ups:** When Ploy's changes appear in the repository, review them against issue #13's acceptance criteria and update the task state.

## 2026-11-24 — Define Ploy and Chaty agent assignments

- **Role:** Chaty / main orchestrator
- **Files:** `AGENT_WORK.md`, `AGENTS.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Added named agent responsibilities and a sequential delegation plan. Ploy's first task is issue #13 accessibility semantics, followed by #11 diagnostics tests and then #8 addon repository tests after review and explicit approval. Chaty retains integration, CI/release coordination, central tracking and the remaining project work. Added isolation, handoff and no-direct-main-push rules for Ploy.
- **Why:** Enable useful parallel implementation despite Ploy's repository-only visibility and prevent duplicate or conflicting work.
- **Verified:** Assignment file created on main; central agent instructions and active-work status updated. No application source code changed and no build/tests were run.
- **Do not redo:** Ploy must not start queued tasks before Chaty accepts the prior handoff. Ploy must not modify central tracking or push to main.
- **Follow-ups:** Give Ploy the contents of `AGENT_WORK.md` and repository access; Chaty reviews each isolated handoff before assigning the next task.

## 2026-11-24 — Rewrite README for the current v1.0.0 project state

- **Role:** Main integrator / documentation
- **Files:** `README.md`
- **Change:** Reorganized the README around the app's purpose, supported Android/ARM64 target, implemented features, current v1.0.0 release, manual updater behavior, build requirements, data handling and repository workflow. Removed stale wording that called v1.0.0 an unpublished draft. Explicitly documented that the addon manager currently stores and manages ZIP packages locally and does not inject them into the game.
- **Why:** Make the README useful to users and contributors while distinguishing implemented behavior from planned or unverified work.
- **Verified:** Confirmed the published v1.0.0 GitHub Release and its two assets through the repository Releases API; checked the current app Gradle configuration and CI workflow. Documentation-only change; no app build or tests were run for this edit.
- **Do not redo:** Keep detailed implementation state and verification evidence in STATUS.md; keep task scope in ISSUES.md rather than duplicating the backlog in README.
- **Follow-ups:** Refresh README feature/build/release claims whenever the implementation, supported toolchain or release process changes.

## 2026-11-24 — Move project task tracking into the repository

- **Role:** Main integrator / documentation
- **Files:** `ISSUES.md`, `AGENTS.md`, `README.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Added `ISSUES.md` containing all 15 currently open GitHub issues, including their full task descriptions, checklists, acceptance criteria and direct links. Updated contributor instructions and project documentation to make the repository file the consolidated, version-controlled task tracker. TickTick is no longer part of the project workflow; existing TickTick tasks are left untouched and will not be updated.
- **Why:** Keep the active backlog alongside the code so it is reviewable, versioned and maintained with implementation changes.
- **Verified:** `ISSUES.md` was created on `main`; documentation updates committed individually. No app source or release artifacts changed.
- **Do not redo:** Do not resume TickTick synchronization or recreate the task list in multiple documentation files. Update `ISSUES.md` as tasks change.
- **Follow-ups:** Keep issue states/checklists current in `ISSUES.md`; retain GitHub links for discussion and traceability.

## 2026-11-24 — Harden addon ZIP validation and package commits

- **Role:** Main integrator / Android / QA
- **Files:** `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt`, `AddonArchiveInspectorTest.kt`
- **Change:** Added ZIP64 end-record locator rejection, CRC validation for directory entries, duplicate addon-folder detection, merged required-dependency directives, locale-independent addon folder comparisons, atomic package staging, and index backup recovery. Corrected export wording to make clear that exporting one item writes its original source ZIP, which may contain multiple addon folders. Added tests for symbolic-link entries, CRC corruption and ZIP64 locators.
- **Why:** Close archive-validation and interrupted-write edge cases before considering the manager complete.
- **Verified:** Changes are committed to main. CI is queued/pending; no compile or test pass is claimed yet.
- **Do not redo:** Keep package storage separate from Cefrium profile; do not extract ZIP paths; do not treat local enabled flags as game activation.
- **Follow-ups:** Wait for CI, resolve failures, add repository storage/rollback tests, then validate the file picker and export flow on physical arm64 hardware.

## 2026-11-24 — Implement first local addon manager milestone

- **Role:** Main integrator / Android
- **Files:** `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt`, `MainActivity.kt`, `AddonArchiveInspectorTest.kt`
- **Change:** Added central-directory ZIP inspection without extraction; path traversal, absolute path, symlink, encrypted entry, unsupported compression, ZIP64 entry, duplicate case-insensitive path, size, expansion-ratio, entry-count and CRC checks; WoW .toc title/version/interface/dependency parsing; SHA-256 package identity; app-private ZIP package storage outside Cefrium profile; atomic index replacement with backup recovery; explicit replacement confirmation; a 1 GiB library cap; local enabled flags, export/remove controls and a Compose manager entry in the exit menu. Added JVM tests for TOC parsing, traversal, case-insensitive duplicates and archives without TOC files.
- **Why:** Deliver a secure local ZIP library first while preserving the game profile and keeping runtime integration separate until the operator-approved technical contract is recorded.
- **Verified:** Source and test files committed directly to main. GitHub Actions run for the first inspector/test commit is pending; subsequent source commits are on main. No successful compile/test result is claimed yet. No physical-device test or release build has been verified for this feature.
- **Do not redo:** No archive-controlled path is extracted to disk. Local enabled flags are metadata only and do not claim that addons are loaded by the game. Do not change the Cefrium profile or enable runtime injection before the exact approved integration contract is reviewed.
- **Follow-ups:** Resolve CI compilation/test failures; add storage/replacement/export tests and any missing archive edge cases; validate picker/export behavior on arm64 hardware; record the operator-approved integration contract before implementing runtime integration.

## 2026-11-24 — Begin local ZIP addon manager implementation

- **Role:** Main integrator / Android
- **Files:** `STATUS.md`, GitHub issue #8
- **Change:** User confirmed they have explicit permission from the LastHaven operator for custom addon integration. Updated issue #8 and STATUS to reflect that authorization is confirmed, while requiring the exact approved scope/API contract before implementing game-runtime integration. Began the local ZIP library work as an independent first milestone.
- **Why:** Authorization has been confirmed by the project owner, but the implementation still needs to follow the operator-approved integration method rather than assume the previously proposed XHR/worker/OPFS pipeline is the correct or authorized interface.
- **Verified:** User confirmation received; issue #8 and STATUS updated. No app code has been changed yet in this entry.
- **Do not redo:** Do not treat general permission as proof that a specific injection technique is approved or compatible. Do not touch Cefrium profile data or protected runtime internals outside the authorized scope.
- **Follow-ups:** Implement secure ZIP inspection, .toc metadata parsing, local storage and management UI; request the exact operator-approved integration contract before runtime integration.

## 2026-11-24 — Research browser addon integration and gate game injection

- **Role:** Main integrator / Cefrium / architecture
- **Files:** `STATUS.md`, `CHANGELOG-AGENTS.md`, GitHub issue #8
- **Change:** Inspected the Android/Cefrium host and current addon-manager issue. Official Lasthavens documentation states that the browser game provides its own curated AddOn Library and that user-supplied addon folders are supported by the Windows desktop app, not the browser version. The published terms prohibit modified clients beyond the in-game AddOn Library and prohibit copying or reverse-engineering the web client. Cefrium 0.21.0 does expose public document-start script and resource-interception APIs, but these APIs do not establish game compatibility or permission to inject into the client. Re-scoped #8 to a secure local ZIP library and explicitly gated any game injection on operator authorization or a supported API.
- **Why:** Avoid building or shipping a loader that modifies a protected game client, violates the operator's published rules, or reports addons as enabled when the browser game cannot load them.
- **Verified:** Official Lasthavens browser guide, Windows desktop addon guide and terms reviewed; Cefrium 0.21.0 public API and release notes reviewed. No live game manifest, OPFS layout or worker implementation was independently verified.
- **Do not redo:** Do not assume a native `Interface/AddOns` directory exists in the browser client. Do not treat `addDocumentStartJavaScript` or `setResourceInterceptor` as proof that the supplied Chrome-extension injection design is supported.
- **Follow-ups:** Obtain explicit operator authorization or a documented addon integration contract. In parallel, implement and test the local ZIP archive/library features without altering Cefrium's game profile or client runtime.

## 2026-11-24 — Add usage settings and privacy-conscious diagnostics

- **Role:** Main orchestrator / Android
- **Files:** `MainActivity.kt`, `AppUsageMeter.kt`, `SettingsDialogs.kt`, `Diagnostics.kt`, `DiagnosticsTest.kt`, `LastHavenApplication.kt`, `AppUpdater.kt`, `README.md`, `CHANGELOG.md`, `STATUS.md`
- **Change:** Added an exit-menu Settings & usage dialog showing installed APK size, Cefrium profile size when present, app/code cache size and local per-UID received/sent data since boot and app-session start. Added bounded rotating diagnostic logs, common credential/session redaction, app/Cefrium/updater event logging, a log viewer and explicit share/clear actions. Back now opens the exit menu with Return to game, Reload, Settings, logs, update check and Leave. Updated the updater API endpoint to the renamed LastHavenPlus repository and drafted user-facing v1.0.0 notes.
- **Why:** Expose storage/network use and make troubleshooting possible without telemetry, automatic uploads or weakening the existing game profile/update protections.
- **Verified:** GitHub commits created on main; issue checklists and TickTick comments updated. Added a unit test for credential/query redaction. The current GitHub commit-status query returned no status records, so compilation and tests are not yet verified; physical-device behavior is also unverified.
- **Do not redo:** Network counters are app-UID totals (game + updater), not game-only; Android counters reset at reboot. Do not claim exact profile size if the Cefrium directory is unavailable. Do not clear Cefrium profile data or publish the release draft before verification/approval.
- **Follow-ups:** Check the next CI run; validate profile directory path, byte counters and menu/session behavior on arm64 hardware; complete remaining settings controls and updater/logging tests; close issues only when their acceptance criteria are met.

## 2026-11-24 — Track storage, network usage and diagnostics work

- **Role:** Main orchestrator / Android / documentation
- **Files:** GitHub issues #6, #10, #11; TickTick project; `STATUS.md`, `README.md`
- **Change:** Expanded #6 to cover installed APK and Cefrium profile/cache size reporting; created #10 for local network usage reporting and #11 for privacy-conscious app/updater logs. Synchronized all seven repository issues (#4–#11, with #5 closed) into the LastHavenPlus TickTick project and refreshed stale repository links.
- **Why:** Make storage, data consumption and troubleshooting visible while keeping the GitHub issue checklist canonical and mirrored in TickTick.
- **Verified:** GitHub issue writes succeeded; TickTick returned successful updates for the five existing open tasks and created tasks for #5, #10 and #11. Code implementation and tests are not yet verified.
- **Do not redo:** Do not create duplicate issues/tasks; do not mark physical-device verification as passed; do not upload usage data or logs automatically.
- **Follow-ups:** Implement settings/storage and local network counters, add bounded redacted diagnostics, run CI, update issue checklists and close only issues whose acceptance criteria are met.

## 2026-11-24 — Rename product branding to LastHavenPlus

- **Role:** Main orchestrator / Android / documentation
- **Files:** `settings.gradle.kts`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values/styles.xml`, `app/src/main/AndroidManifest.xml`, `README.md`, `AGENTS.md`, `STATUS.md`, `.github/workflows/android.yml`
- **Change:** Renamed the Gradle root project, launcher display name, Android theme resource and documentation/workflow branding to LastHavenPlus.
- **Why:** Align the app's visible identity and project documentation with the approved product name.
- **Verified:** Source updates committed directly to main. Application ID `com.neonyne.lasthavenlauncher`, namespace, release signing key, updater asset filenames and version identity were intentionally preserved for installed-app update compatibility. Build verification is pending.
- **Do not redo:** Do not change the application ID or signing certificate as part of branding. The GitHub repository slug remains `Lasthavens-Android` until renamed through repository settings; existing issue/release links currently remain valid.
- **Follow-ups:** Rename the GitHub repository to `LastHavenPlus` in repository settings when an admin-capable GitHub action is available; verify the redirect and update documentation links if needed. Run CI after branding changes.

## 2026-11-24 — Fix release certificate parsing and pass signed ARM64 CI

- **Role:** Main orchestrator / Android / CI
- **Files:** `.github/workflows/android.yml`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Fixed both CI certificate digest parsers. `apksigner` output contains a colon in `V2 Signer:` before the `certificate SHA-256 digest:` label, so splitting on the first colon extracted the label instead of the digest. The parser now strips everything through the literal digest label, for both current-release verification and previous-release eligibility for BSDIFF generation. Added diagnostic checkpoints to release verification.
- **Why:** The parser falsely rejected a correctly signed APK and would silently prevent future differential patches from being offered.
- **Verified:** CI run #85 passed: debug and signed release builds, BSDIFF unit test, arm64-v8a-only checks for both APKs, established release-certificate SHA-256 verification, APK size report and both artifact uploads. The existing release key was restored successfully. Run: https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45450335590
- **Do not redo:** Do not change the established key or pinned fingerprint. Keep the full APK fallback and user-confirmed download/install flow. Do not treat CI build success as proof of a real release-to-release update.
- **Follow-ups:** Complete the remaining #4 tasks (Gradle Wrapper, broader updater edge-case tests and package-identity check), publish the first controlled stable release, then test a generated differential update and Android installation on a physical arm64 device.

## 2026-11-24 — Fix BSDIFF test compilation and diagnose release verification

- **Role:** Main orchestrator / Android / CI
- **Files:** `BsdiffPatchTest.kt`, `.github/workflows/android.yml`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Replaced deprecated `createTempDir()` in the BSDIFF reconstruction test with `Files.createTempDirectory(...).toFile()`. The next CI run (#79) passed the combined debug/release build and unit-test step. Its following package verification step failed, the diagnostic run showed both APKs contain only `arm64-v8a`, `apksigner` succeeds, and the actual certificate fingerprint exactly matches the pinned value. The failure is the AWK parser: splitting on `: ` selects the text after `V2 Signer:` rather than the SHA-256 field. The same faulty parser also gates differential patch generation against the previous release.
- **Why:** Kotlin test compilation was blocking all release verification; after fixing it, the pipeline exposed a separate release-verification blocker.
- **Verified:** CI run #79 confirms the build-and-test step succeeded and the release-verification step failed. The existing keystore restore step also succeeded. No APK artifact was uploaded and no release was published.
- **Do not redo:** Keep the existing release key, pinned certificate, application ID, arm64-only configuration, manual update confirmation and full-APK fallback. Do not treat a successful compile as a verified updater.
- **Follow-ups:** Change both certificate parsers to extract the text after the literal certificate digest label, rerun CI, then test an actual signed update on an arm64 device.

## 2026-11-24 — Move project backlog to GitHub Issues and simplify contributor instructions

- **Role:** Main orchestrator / documentation
- **Files:** `README.md`, `AGENTS.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Consolidated the remaining roadmap into GitHub Issues #4–#8. Added issues for minimal settings/safe storage controls, measured performance and APK-size work, and the deferred addon manager. Updated the README to link to the issue backlog, removed the duplicated shared TODO checklist from AGENTS.md, and clarified issue claiming, checklist maintenance, direct-to-main integration and issue closure rules. STATUS now links to tracked work and records only current progress, evidence and blockers.
- **Why:** Keep one canonical task list, avoid stale duplicated checklists, and make work ownership and completion visible in GitHub.
- **Verified:** Read current main-branch documentation and open issues; confirmed Gradle Wrapper files are absent and retained that task in issue #4. Created issues #6, #7 and #8. Documentation changes are committed directly to main.
- **Do not redo:** Do not reintroduce a second TODO checklist in README, AGENTS or STATUS. Do not create duplicate issues for tasks already covered by #4 or #5.
- **Follow-ups:** Work from the open issue checklists; close issues only after their acceptance criteria are verified.

## 2026-11-24 — Harden differential update fallback and patch parsing

- **Role:** Main integrator / Android
- **Files:** `AppUpdater.kt`, `BsdiffPatch.kt`, `STATUS.md`, `README.md`
- **Change:** Made optional patch metadata fail closed to the full-APK path, validated release versionCode against the semantic tag, handled same-version-name first signed upgrades using versionCode, isolated the BSDIFF compressed streams, rejected no-progress control records and corrected bounded stream reads. Updated the documented key state and release workflow status.
- **Why:** A malformed or unavailable patch must not block a valid signed full APK update, and the patcher must handle large APKs without sharing stream offsets or allocating the whole APK in memory.
- **Verified:** Source reviewed; CI compilation and the BSDIFF unit test are still pending. The patch-generation workflow independently verifies its output with `bspatch` and SHA-256 before publication.
- **Do not redo:** Do not remove the full APK fallback, bypass digest/certificate checks, or alter the existing release key.
- **Follow-ups:** Check the next main CI run; test a patch from one published signed release to the next on a physical arm64 device.

## 2026-11-24 — Add signed differential APK updates

- **Role:** Main integrator / Android / CI
- **Files:** `AppUpdater.kt`, `BsdiffPatch.kt`, `BsdiffPatchTest.kt`, `MainActivity.kt`, `app/build.gradle.kts`, `.github/workflows/android.yml`, `README.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Wired the existing `lasthavenplus` release keystore secrets into release builds and pinned the known LastHaven certificate SHA-256 in CI and the app. Added a streaming BSDIFF40 patch applier, a reconstruction unit test, an update manifest contract, and tag-triggered CI that signs the ARM64 APK, generates a patch against the immediately previous compatible signed release, reconstructs it with `bspatch` to prove byte-for-byte equality, and publishes the patch only when it is at least 10% smaller than the full APK. The app offers separate user-initiated smaller/full downloads, verifies the base APK, patch, reconstructed APK, package identity, versionCode and signer, and then hands the APK to Android's installer.
- **Why:** Keep the 200+ MiB Cefrium payload out of downloads when it is unchanged while retaining a complete signed APK fallback. Android still receives a complete APK for installation; the patch only reduces the network transfer.
- **Verified:** Source and CI pipeline committed. BSDIFF unit test, signed build, certificate verification, release asset generation and on-device update flow are pending the next CI run and physical-device test. No release was tagged or published by this change.
- **Do not redo:** Never embed the private signing key in the APK or repository. Keep the existing signing certificate and application ID. Never auto-download or auto-install. Do not remove the full APK fallback or clear Cefrium/game profile data during updates.
- **Follow-ups:** Check CI compilation/unit test and verify the expected signing fingerprint. First signed stable release is full APK only; validate patch generation on the next release and test same-key in-place installation on an arm64 device.

## 2026-11-24 — Enable release-only R8 and resource shrinking

- **Role:** Main integrator / build optimization
- **Files:** `app/build.gradle.kts`, `.github/workflows/android.yml`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Enabled R8 code minification and resource shrinking for the release variant only. CI now builds debug plus an unsigned optimized release candidate, checks both APKs contain arm64-v8a only, and records file size, DEX total and largest entries.
- **Why:** The measured baseline APK was 229,073,631 bytes (218.4 MiB); its 22 DEX files total about 104.4 MiB, while `libcef.so` alone was 92.84 MiB compressed. Shrinking is the least invasive candidate to reduce the DEX payload.
- **Verified:** Baseline obtained by downloading and inspecting the actual APK from successful CI run #32. Cefrium's current public guide says to keep R8 enabled and that its AAR consumer rules preserve engine-required code since 0.12.0. New CI result and runtime/device validation are pending.
- **Do not redo:** Do not strip Chromium native code or Cefrium data assets, add broad keep rules, or remove engine runtime dependencies without compatibility evidence. Do not treat the unsigned release candidate as installable or distributable.
- **Follow-ups:** Compare the optimized release APK against the measured baseline; review R8 mapping/usage outputs and test the minified build on a physical arm64 device before shipping.

## 2026-11-24 — Handle Android package-install permission

- **Role:** Android / updater integration
- **Files:** `MainActivity.kt`
- **Change:** Added the Android unknown-app installation settings handoff. If the user has not allowed this launcher to request package installs, the app opens the per-app system setting and resumes the pending installer handoff after permission is granted.
- **Why:** Android blocks APK installation from apps that have not been explicitly allowed by the device owner.
- **Verified:** Source path reviewed; CI run #31 is pending.
- **Do not redo:** Do not attempt silent installation or bypass Android's package installer confirmation.
- **Follow-ups:** Verify permission handoff and same-key update on a physical device.

## 2026-11-24 — Add opt-in GitHub Releases updater

- **Role:** Main integrator / Android
- **Files:** `AppUpdater.kt`, `MainActivity.kt`, `AndroidManifest.xml`, `res/xml/update_file_paths.xml`, `STATUS.md`, `README.md`
- **Change:** Added latest-stable-release checks at startup plus a manual check action in the exit dialog. A found update is shown with release notes and explicit Download update / Later actions. Download is user-initiated only, capped at 500 MB, checked for APK ZIP signature and GitHub release SHA-256 digest, stored privately, and handed to Android's package installer via a non-exported FileProvider.
- **Why:** Support simple in-app updates without silent downloads, external storage permissions, or touching Cefrium/game profile data.
- **Release contract:** Publish tag `vMAJOR.MINOR.PATCH` with signed `lasthavenlauncher-arm64.apk`; GitHub release API must expose its SHA-256 asset digest. Increment versionCode/versionName and keep the same app ID and signing key.
- **Verified:** Source changes committed. CI build and physical installer/update tests pending. GitHub currently has no published releases, so live update discovery cannot be tested yet.
- **Do not redo:** Do not auto-download or auto-install. Do not accept unsigned digest-less release assets or change the application ID/signing key.
- **Follow-ups:** Check CI compilation; configure a backed-up release signing key; publish first signed release and verify update over an installed older build, preserving game data.

## 2026-11-24 — Record first successful arm64 CI build

- **Role:** Main integrator / QA
- **Files:** `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Recorded successful GitHub Actions run #15 for commit `3d4e1a0`, including the Android build, unit-test task, APK ABI verification and debug artifact upload. Updated the remaining P0 blocker to physical-device gameplay validation.
- **Why:** Keep project status evidence-based and distinguish a successful CI build from verified in-game compatibility.
- **Verified:** All nine workflow steps completed successfully; artifact `lasthavenlauncher-debug-arm64` is available until 2026-12-08.
- **Do not redo:** Do not mark LastHaven gameplay, GPU, audio or storage persistence as verified based on CI alone.
- **Follow-ups:** Install the artifact on a physical arm64 phone and run the P0/P1 device checklist.

# Agent changelog — LastHavenLauncher

## 2026-11-24 — Keep Android CI lean

- **Role:** Architecture / CI
- **Files:** `.github/workflows/android.yml`
- **Change:** Removed pull-request-triggered builds to match the direct-to-main workflow and added concurrency cancellation so only the newest main build continues.
- **Why:** Avoid duplicate Gradle/Cefrium downloads and unnecessary CI runs while iterating.
- **Verified:** Workflow definition committed; next push run will exercise the concurrency policy.
- **Do not redo:** Do not add PR triggers unless the project workflow changes.
- **Follow-ups:** Monitor the current main build.

Append-only. Newest entries at the top. Every meaningful code, Gradle, build, research or documentation change must be recorded.

Entry format:

```markdown
## YYYY-MM-DD — short title
- **Role:** …
- **Files:** …
- **Change:** …
- **Why:** …
- **Verified:** …
- **Do not redo:** …
- **Follow-ups:** …
```

---

## 2026-11-24 — Add retry handling, persistent-storage request and debug inspection

- **Role:** Android / Cefrium / QA
- **Files:** `app/src/main/java/com/neonyne/lasthavenlauncher/MainActivity.kt`, `LastHavenApplication.kt`, `AndroidManifest.xml`, `STATUS.md`
- **Change:** Registered the documented `CefriumNavigationClient` to surface main-frame load errors, added retry UI, request `navigator.storage.persist()` after the first completed document load, and enabled Cefrium remote debugging in debuggable builds only.
- **Why:** Cover connection/renderer recovery, request durable origin storage and allow `chrome://inspect` to inspect the debug build.
- **Verified:** API signatures confirmed against Cefrium 0.21.0 docs. CI build and device runtime behavior are pending.
- **Do not redo:** Do not use Chromium internals or enable remote debugging in release builds. Do not manually delete Cefrium profile directories.
- **Follow-ups:** Check CI, install on an arm64 device, confirm the storage request result and run the WebGL2/WASM/SAB/WebSocket/audio checklist.

## 2026-11-24 — Correct Android 37 SDK package name

- **Role:** QA / CI
- **Files:** `.github/workflows/android.yml`, `STATUS.md`
- **Change:** Install `platforms;android-37.0`; the Android SDK repository publishes API 37 under that minor-version package name.
- **Why:** CI could not find `platforms;android-37` even though AGP and Cefrium require compileSdk 37.
- **Verified:** Android SDK package list and CI failure log identify the correct package name. Build retry pending.
- **Do not redo:** Do not request `platforms;android-37`; use `android-37.0`.
- **Follow-ups:** Verify SDK install, Gradle resolution and APK build.

## 2026-11-24 — Fix Android SDK setup in CI

- **Role:** QA / CI
- **Files:** `.github/workflows/android.yml`, `STATUS.md`
- **Change:** Set up-android to install only platform-tools explicitly; the action's default `tools` package no longer exists in the current Android SDK repository.
- **Why:** The first CI run failed during SDK setup before Gradle or application code was reached.
- **Verified:** Failure confirmed in GitHub Actions logs; retry is queued.
- **Do not redo:** Do not add the obsolete `tools` SDK package.
- **Follow-ups:** Check the new CI run and resolve any actual Gradle or Kotlin compilation errors.

## 2026-11-24 — Create greenfield Android app and Cefrium build

- **Role:** Architecture / Android / Cefrium integration
- **Files:** Gradle settings and build files, app manifest, MainActivity, app resources, GitHub Actions workflow, README.md, AGENTS.md, STATUS.md
- **Change:** Added the Kotlin + Compose Android scaffold, fixed application ID, Cefrium 0.21.0 SDK/Compose/plugin dependencies, Codeberg Maven registry, arm64-only ABI filter, immersive gameplay host using `CefriumView`, minimal Back/exit handling and a CI debug-build workflow.
- **Why:** Establish the smallest app that can load the existing game before optional features.
- **Verified:** Official Cefrium 0.21.0 docs confirm the current release, Chromium 156.0.8078.96, JDK 25 / Gradle 9.7.1 / AGP 9.4 / compileSdk 37 requirements and documented `CefriumView` usage. GitHub Actions and physical-device results are pending.
- **Do not redo:** Do not re-research the documented version/toolchain unless the CI resolver or build contradicts the official release page. `CefriumView` is the Compose API; do not replace it with WebView or another engine.
- **Follow-ups:** Check CI, install on physical arm64 device, validate LastHaven and then run web capability checks. Release signing key still needs to be generated and backed up before distributing a release build.


## 2026-11-24 — Queue beta updater channel and functional settings work

- **Role:** Planning / issue triage
- **Files:** GitHub issues #6 and #19, `AGENT_WORK.md`, `ISSUES.md`, `STATUS.md`
- **Change:** Created #19 for an opt-in updater Beta channel (published prereleases only; Stable remains default) and refined #6 around functional settings and UX clarity without a visual redesign. Assigned #19 to Chaty after #4 and queued #6 for Ploy after #13, #11 and #8 are reviewed and integrated.
- **Why:** Users need an explicit way to test beta releases and settings that expose useful, working controls rather than a usage-only popup.
- **Constraints:** No overlapping ownership; no automatic download/install; no release or tag changes; no visual redesign in this pass; preserve the existing stable updater validation path.
- **Verified:** GitHub issues #6 and #19 updated/created; coordination files updated on `beta`. No app code changed and no tests run.


## 2026-11-25 — Switch the configured game URL to the canonical host

- **Role:** Chaty / main orchestrator
- **Files:** `app/src/main/java/com/neonyne/lasthavenlauncher/MainActivity.kt`, `README.md`, `AGENTS.md`, `ISSUES.md`, `STATUS.md`, `WORK_LOG.md`
- **Change:** Replaced `https://alt.lasthavens.com/play` with `https://lasthavens.com/play` in the Android app default and all matching project instructions/backlog references. The Firefox WebExtension specification also uses this canonical host.
- **Why:** The owner explicitly requested the non-`alt` game URL as the project URL.
- **Verification:** Source and documentation were searched for the old hostname in the matching files before editing. The new URL is set in the Android default constant. CI run #295 for the URL-change commit was pending at this checkpoint; no build or runtime result is claimed.
- **Do not redo:** Do not restore the `alt.` hostname. Do not alter the application ID, signing identity, GeckoView plan or game source.
- **Follow-ups:** Check run #295; implement the separately specified Firefox WebExtension when the required fflate distribution is available; verify the canonical host on physical ARM64 and Firefox desktop/Android.


## 2026-11-25 — Defer addon manager work at owner request

- **Role:** Chaty / main orchestrator
- **Files:** `STATUS.md`, `ISSUES.md`, `AGENT_WORK.md`, `WORK_LOG.md`, `CHANGELOG-AGENTS.md`, GitHub issue #23
- **Change:** Marked the Firefox WebExtension addon-manager workstream deferred/on hold. Removed it from active agent scheduling and explicitly prohibited research, implementation, testing or assignment until the owner resumes it.
- **Why:** The owner requested that addon manager work stop for now.
- **Verified:** Issue #23 remains open and is marked deferred in the repository backlog; the old Android addon manager remains removed. No extension files or dependencies were added.
- **Do not redo:** Do not continue addon-manager work based on the earlier specification without explicit owner authorization to resume.
- **Follow-ups:** Focus on the existing Android launcher build, accessibility, diagnostics and GeckoView migration tasks.


## 2026-11-25 — Make Cefrium replacement the top project priority

- **Role:** Chaty / main orchestrator
- **Files:** `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, GitHub issues #20 and #21
- **Change:** Promoted GeckoView decision #20 and Cefrium replacement #21 to P0. Chaty now owns the complete GeckoView workstream. Ploy's smaller #9 accessibility and #11 diagnostics tasks are paused; #22 follows engine integration. The current #4 CI run is treated only as the minimum clean-build prerequisite, not as a reason to delay migration with unrelated updater work.
- **Why:** The owner explicitly set replacing Cefrium as the top project priority.
- **Verified:** Current beta HEAD is `a9d9b272a004a9a4dcc49fed0067f414ba3601cf`. CI run #297 (ID `45498642232`) is in progress; environment setup, wrapper validation and signing-key restore passed, but the build/test step has not completed. No successful build or GeckoView compatibility is claimed.
- **Do not redo:** Do not add GeckoView until the exact stable artifact/API/toolchain compatibility is verified. Do not change application ID, signing identity, ABI target or update consent. Do not work on addon manager #23.
- **Follow-ups:** Finish #20 research, then begin #21 production migration immediately after the baseline build is confirmed.


## 2026-11-25 — Start GeckoView technical investigation

- **Role:** Chaty / GeckoView migration owner
- **Files:** `docs/GECKOVIEW_MIGRATION.md`, `STATUS.md`, `WORK_LOG.md`
- **Change:** Inspected the complete beta source tree and recorded every remaining Cefrium integration point across Gradle settings, app dependencies, Application initialization and MainActivity Compose hosting. Began stable artifact investigation without changing production dependencies.
- **Why:** Establish an accurate, bounded removal plan and avoid speculative engine/API changes.
- **Verified:** The current app's direct Cefrium source usage is in `LastHavenApplication.kt` and `MainActivity.kt`; the plugin/dependencies are in `app/build.gradle.kts`; Cefrium repository declarations are in `settings.gradle.kts`. Mozilla's official quick start requires Java 17 source/target compatibility. A 160.0.20261116130455 artifact is a candidate, but POM/AAR details are not yet verified.
- **Do not redo:** Do not copy Cefrium APIs or profile data into GeckoView. Do not change JDK/Gradle/AGP/SDK, application ID, signing identity or ABI filter without evidence.
- **Follow-ups:** Verify candidate artifact POM/AAR and API surface, inspect #297 build result, then begin the engine implementation.


## 2026-11-25 — Start GeckoView 160 integration

- **Role:** Chaty / main orchestrator
- **Files:** Gradle settings/module, Application, MainActivity, GeckoRuntimeProvider, usage meter/settings, GeckoView migration record
- **Change:** Removed Cefrium integration and added the pinned ARM64 GeckoView 160 artifact. Added a process-scoped runtime provider and GeckoView/GeckoSession host with load/crash/fullscreen and permission delegates. Updated storage wording to report persistent app data including GeckoView rather than a nonexistent Cefrium profile.
- **Why:** Start the owner's top-priority engine replacement while keeping the current game-first UI and updater.
- **Verified:** Mozilla Firefox 160 versioned public API source checked. Maven index identifies the artifact as Mozilla-published and MPL 2.0.
- **Not verified:** Gradle dependency resolution, compilation, tests, final APK ABI/signing/size, persistent profile behavior and physical ARM64 gameplay.
- **Follow-ups:** Resolve CI feedback; implement only required delegates; verify game functionality on physical ARM64 hardware.


## 2026-11-25 — GeckoView 160 API 37.2 build prerequisite

- **Role:** Chaty / main orchestrator
- **Files:** `app/build.gradle.kts`, `.github/workflows/android.yml`, `README.md`, `STATUS.md`, `WORK_LOG.md`, GitHub issue #22
- **Change:** Matched the Android compile SDK minor level to GeckoView 160's AAR metadata requirement (37.2), changed CI to install the matching SDK platform, corrected stale Cefrium descriptions to reflect the checked-in GeckoView source, and clarified #22's Reload / Home / Return to game menu behavior.
- **Why:** Run #302 failed before Kotlin compilation because GeckoView required API 37.2 while the project used 37.0. The home-screen task must preserve the live GeckoSession and should follow a verified engine build.
- **Verified:** Run #302 failure cause is explicit in its Gradle log; official Android Gradle Plugin docs support the minor API compile SDK declaration. Run #304 on commit `b0d1b154e5eb49c8ef72909e2ba2f4b8fdbddf3d` was pending at this checkpoint.
- **Not verified:** SDK 37.2 package installation, app compilation, tests, release signing, ABI, artifact contents, GeckoView runtime/profile behavior and physical ARM64 gameplay.
- **Follow-up:** Inspect run #304; if it clears the build gate, begin issue #22. No main promotion, tag or release.

## 2026-11-25 — Align project documentation with the verified beta state

- **Role:** Chaty / main orchestrator
- **Files:** `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, `ISSUES.md`, `CHANGELOG.md`, `docs/GECKOVIEW_MIGRATION.md`, `CHANGELOG-AGENTS.md`, `WORK_LOG.md`
- **Change:** Reorganized the project README and aligned the agent rules, work assignment, issue index, status report, release changelog and GeckoView guide with the current beta source and verified CI evidence. Clarified beta/main responsibilities, Chaty's sole integration role, the no-PR workflow, TickTick's supplementary role and the addon-manager hold. Marked physical-device behavior unverified.
- **Why:** The documentation contained conflicting Cefrium-era instructions, stale task assignments and verification claims that did not match the current GeckoView source or CI run.
- **Verified:** Inspected the beta documentation, source references, branch state and successful Actions run #308. Run #308 built and tested commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; it is not evidence for later workflow commits or physical-device behavior. The Gradle Wrapper is already present and pinned to Gradle 9.7.1.
- **Not verified:** Current workflow optimization run, physical ARM64 gameplay/profile persistence, home-screen implementation and runtime updater behavior.
- **Do not redo:** Do not bypass PR review for task integration, change `main`, close issues for documentation-only work, or claim physical-device verification from CI.
- **Follow-ups:** Inspect the CI optimization run; review Ploy's #22 handoff; complete physical ARM64 verification; keep STATUS synchronized with exact current commit and CI evidence.



## 2026-11-25 — Allow pull requests for beta integration

- **Role:** Chaty / main orchestrator
- **Branch:** `beta`
- **Change:** Replaced the active no-PR rule with a PR-based workflow across the repository's current instructions. Contributors use dedicated task branches and open PRs targeting `beta`; Chaty alone reviews and merges accepted PRs. Parallel work remains limited to separate issues with non-overlapping file scopes.
- **Why:** The repository owner explicitly authorized pull requests and requested that the policy be changed consistently.
- **Preserved:** `beta` remains the active development/integration/testing branch; `main` remains stable. Promotion to `main` and publication still require explicit owner approval. Historical log entries are retained as history and are superseded by this entry.
- **Verification:** Documentation-only changes. No application source, build configuration, release signing, `main` or release assets changed. No build or tests were run.

## 2026-11-25 — Add pull-request CI validation

- **Role:** Chaty / CI orchestration
- **Files:** `.github/workflows/android.yml`, `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`
- **Change:** Added a `pull_request` trigger for PRs targeting `beta` and a dedicated read-only validation job. It builds the unsigned release variant and runs unit tests without signing secrets or publishing APKs. The full signed build and release-artifact checks remain on branch/tag events.
- **Why:** Documentation allowing PRs is incomplete unless CI validates them. The PR path must not expose signing secrets or create release artifacts.
- **Not yet verified:** The workflow change has not completed a GitHub Actions run. Do not claim PR CI is operational until the run is inspected.


## 2026-11-25 — Introduce risk-based review depth

- **Branch:** `chaty/workflow-efficiency-policy` → `beta`.
- **Change:** Define lightweight, standard and strict review levels for documentation/copy, contained application changes, and high-risk runtime/security/release changes. Every PR remains reviewed by Chaty and subject to required CI; high-risk work retains focused regression, exact-head, signed-package and physical-device gates as applicable.
- **Efficiency:** Require focused one-issue PRs, concise handoffs and branch refresh/retest when beta advances. Review depth changes effort, not authorization or safety gates.
- **Scope:** Documentation/workflow policy only. No app source, Gradle, CI workflow, signing, `main`, or release assets changed.


## 2026-11-25 — v2.1.0 launcher integration and docs reconciliation

- Merged PR #43 into beta at `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` after exact-head unsigned build/unit-test run #386 passed.
- Integrated responsive Home redesign, adaptive icon resources, simplified Back menu, full-screen settings/diagnostics/update pages and GeckoSession view-detach retention.
- Started issue #29 documentation reconciliation on `chaty/docs-reconciliation-v2.1`; no release claims are made until exact post-merge signed ARM64 CI and artifact inspection complete.
- Recorded owner-reported ARM64 app/GeckoView smoke test without extrapolating to the full GeckoView feature matrix or the new UI.


## 2026-11-25 — Repository housekeeping: reconcile post-promotion records

- **Role:** Chaty / repository maintenance.
- **Base:** beta commit `854fce1b1e2989cb012ebf5a15d196d91ff7cad7` (PR #46 merge).
- **Evidence checked:** PR #46 merged into beta; PR #47 merged the v2.1.0 candidate into main at `c5f6518e57d614da73642b7dc4c8a21e82e6e9b3`. PR #47 records post-merge beta run #45601558602 as passing build, unit tests, signing-certificate verification and ARM64-only ABI checks.
- **Corrections:** mark issue #29 complete in the backlog; remove its stale active assignment; record completed promotion and verified CI evidence in STATUS; align README, AGENTS and GeckoView notes with the owner's current addon-manager hold instruction.
- **Scope:** documentation and issue-tracker reconciliation only. No app source, Gradle, CI workflow, signing, release publication or game changes.
- **Limitations:** physical ARM64 feature-matrix testing remains outstanding. The final v2.1.0 tag/draft/publication state must be verified independently before it is described as published.


## 2026-11-25 — Complete repository housekeeping PR #49

- **PR:** [#49](https://github.com/iNeonyne/LastHavenPlus/pull/49), squash-merged to beta.
- **Merge commit:** `1f661cb6234dcfdfd051d0cb963e739668896a26`.
- **Outcome:** post-promotion status and CI records reconciled; completed issue #29 removed from active work; addon-manager issue #23 returned to owner-directed hold; issue #31 backlog wording corrected; active assignment cleared.
- **Validation:** documentation-only diff review. No application tests or physical-device checks were run by this PR.


## 2026-11-25 — Reconcile roadmap and audit stale branches

- **Role:** Chaty / repository housekeeping.
- **Base:** beta at `1f661cb6234dcfdfd051d0cb963e739668896a26` (PR #49 merge).
- **Roadmap correction:** v2.1.0 is a candidate already promoted to `main` by PR #47, not merely a proposed beta target. Publication remains unverified; physical ARM64 feature-matrix and final versioned release-artifact checks remain release gates.
- **Issue correction:** issue #18's active plan is narrowed to reliability and GeckoView verification. Addon-manager work #23 remains explicitly on hold. Issue #31 records the passing signed beta verification run #45601558602 but stays open because release-tag differential fallback paths have not been exercised.
- **Branch audit:** 27 branches were returned, including two protected working branches (`main`, `beta`), a legacy backup with no common ancestor, superseded task branches and prior housekeeping branches. No open PRs were found. Several branches contain unique commits or diverged history and were not deleted. The available GitHub integration exposes no branch-delete action; do not simulate deletion by force-moving refs.
- **Scope:** backlog and work-history documentation plus GitHub issue metadata only. No app source, Gradle, CI workflow, signing, release publication or game changes.
- **Verification:** exact branch comparisons were inspected for the Chaty task branches. Physical-device testing remains unverified. No application tests were run for this documentation-only update.


## 2026-11-25 — Close completed Home implementation and clarify remaining UI verification

- **Issue #22:** closed as completed after verifying the Home/Play/Settings implementation in PR #26 and the later v2.1.0 navigation changes in PR #43. The obsolete Home-menu action list was superseded by the owner's exact three-action Back menu.
- **Issue #42:** retained open and retitled to track physical ARM64 verification of the already-integrated v2.1.0 UI, not a second implementation pass.
- **Issue #18:** replaced the stale proposed-scope roadmap with the actual promoted-candidate state, remaining physical-device and final-tag artifact gates, and explicit addon-manager hold.
- **Issues #4/#31:** updated with the signed beta evidence from run #45601558602. Both remain open because physical update installation and release-tag fallback failure-path verification are outstanding.
- **Validation:** checked PR #26/#43/#47 merge records, exact CI evidence and current issue states. Documentation/issue metadata only; no application tests or physical-device checks were run.


## 2026-11-25 — Merge roadmap and backlog housekeeping PR #51

- **PR:** [#51](https://github.com/iNeonyne/LastHavenPlus/pull/51), squash-merged into beta.
- **Merge commit:** `b711af8e9c0dd35032edc21a1c3f7faacd4c343c`.
- **Outcome:** corrected the v2.1.0 roadmap and issue #4/#31 evidence; closed completed source task #22; respecified #42 as physical UI verification; kept addon-manager #23 on hold.
- **Validation:** reviewed the four-file documentation-only diff. No PR workflow runs were returned for the docs-only head. No app tests or physical-device checks were run.
- **Remaining:** stale/diverged branch pruning is not available through the connected GitHub actions; no branch refs were force-moved or deleted.


## 2026-11-25 — Reconcile published v2.1.0 release records

- **Release:** [v2.1.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.1.0), published 2026-11-25 at tag commit `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.
- **Artifact evidence:** versionCode 2001000; APK 96,444,836 bytes, SHA-256 `f65672c8855298764b3345e58694b672c39f35971a39beccb7e4d1feb6310cab`; established signing certificate matched; ABI exactly `arm64-v8a`. Release metadata contains the APK, update manifest and differential patch.
- **CI caveat:** release workflow run #45603533787 passed unit tests, signature/certificate/ABI verification, differential generation and asset upload/publish steps, but its final release asset query failed with HTTP 404. Current release metadata independently confirms the expected assets. Issue #31 tracks the workflow follow-up.
- **Docs correction:** update beta README/CHANGELOG/STATUS/ISSUES to record v2.1.0 as published rather than a candidate. The stable-facing README/CHANGELOG on main still need a separately authorized documentation correction; routine task PRs remain targeted to beta.
- **Physical-device status:** full ARM64 gameplay/profile/UI/update matrix remains unverified. Do not treat release CI as device evidence.


## 2026-11-25 — Stable documentation and release-workflow housekeeping

- **Stable docs:** owner-authorized updates to `main` README and CHANGELOG now identify v2.1.0 as published. Commits: `fdae3d90cf3abb209df8a42dd73ed1667bae9808` and `2abe7cc212b67a3b91f5dcdb108aee3a55559e32`. Verified against the published release metadata and signed ARM64 artifact evidence.
- **Issue triage:** #9 and #10 titles now describe accessibility/navigation and app-wide network usage verification; #20 is narrowed to the remaining GeckoView artifact/dependency audit; #21 tracks physical GeckoView runtime/profile checks. #22 and #29 remain closed; #23 remains on hold.
- **Release workflow:** PR #54 merged at `b12da7651445d15080e7593d441459a8c8403070`. It uses the release action's ID output for post-upload legacy-asset cleanup and final draft verification, avoiding draft-by-tag lookups.
- **CI:** exact-head unsigned PR run #45610110307 passed bounded-input tests, unsigned release build and JVM unit tests. Post-merge signed beta run #45610518305 is pending. No release-tag job or publication was run.
- **Branch audit:** 31 branches remain. Three branches are strictly behind beta with no unique commits (`chaty/bound-updater-queue`, `chaty/fix-updater-hardening`, `chaty/home-updater-demo`); branch deletion is not exposed by the connected GitHub integration. Diverged branches with unique commits were preserved.


## 2026-11-25 — Confirm post-merge signed CI for PR #54

- Post-merge beta push run [#45610518305](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45610518305) completed successfully for merge commit `b12da7651445d15080e7593d441459a8c8403070`.
- The merged beta build, unit tests, established signing-certificate verification and ARM64-only ABI checks passed.
- This push run did not execute the release-tag draft asset lookup. Live release-ID verification and optional differential failure-path testing remain open under #31.


## 2026-11-25 — Add beta Android quality gate and incremental Kotlin formatting

- **Role:** Chaty / CI workflow implementation
- **Issue:** [#60](https://github.com/iNeonyne/LastHavenPlus/issues/60)
- **Branch:** `chaty/ci-quality-gate` from beta `36f079445e227583c12b19bde9161844e0e5855d`
- **Scope:** Add a read-only Android quality workflow for relevant beta pushes and extend the existing PR workflow with lint/format checks; configure pinned Spotless 8.12.0 with ktfmt 0.64; document local formatting/lint commands and split responsibilities between Android PR validation and the quality workflow.
- **Design:** Use incremental formatting ratcheted to the PR base or previous beta commit to avoid mass-reformatting legacy files. Documentation-only edits do not trigger quality/build workflows. The existing Android PR job runs lint/format, JVM and Python updater-helper tests together; the post-merge quality workflow repeats only lint/format.
- **Constraints:** No app source behavior, release signing, secrets, ABI, GeckoView, updater or main changes. No release or publication.
- **Verification:** Exact-head Actions validation is pending. Do not claim lint/format success until the PR checks complete; a separate intentionally misformatted Kotlin change must be rejected by the formatter check.


## 2026-11-25 — Verify Android formatter negative test

- PR #72's deliberately misformatted Kotlin probe was rejected by Spotless in run [#45627319452](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627319452); the expected formatting diff was present. The run is an intentional negative-test failure, not a passing build.
- Removed the temporary probe in commit `b93be719e510757036c948d23a8aadf1409b95a2`.
- Clarified that the existing Android workflow performs PR lint/format/build/tests, while `quality.yml` is a read-only post-merge beta-push lint/format gate.
- Clean-head validation is still required before merge.


## 2026-11-25 — Merge Android quality gate to beta

- PR [#72](https://github.com/iNeonyne/LastHavenPlus/pull/72) squash-merged to beta at `82e819a43e6d728a9eb8fea620eca47ac9c998df` after exact-head PR run [#45627915789](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627915789) passed lint, formatting, unsigned release build, JVM tests and updater-helper tests.
- The negative formatter test was rejected as expected in run [#45627319452](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627319452); its temporary probe was removed before merge.
- Post-merge quality run [#45631250015](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631250015) and beta build run [#45631249817](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631249817) were triggered for the merge SHA and remain pending at this entry.
- No application behavior, `main`, tag, or release publication was changed.


## 2026-11-25 — v2.2.0 roadmap and agent ownership housekeeping

- Replaced the stale v2.1.0 post-release roadmap framing with a v2.2.0 planning track; v2.1.0 remains the published stable release and no new release is authorized.
- Assigned Chaty central roadmap, issue triage, documentation, branch audit, CI and beta integration.
- Assigned Ploy issue #56 only: direct launch and responsive in-game overlay, with a dedicated beta-targeted PR and strict non-overlapping scope.
- Reconciled #42 as superseded by #56; new UI physical verification remains under #5/#56.
- Verified issue #60 post-merge quality run #45631250015 and signed beta build run #45631249817 both completed successfully after PR #72. Release-tag publication steps were skipped.
- Preserved #23 Addon Manager hold and physical ARM64 verification blockers.

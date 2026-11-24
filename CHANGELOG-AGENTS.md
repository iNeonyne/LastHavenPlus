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

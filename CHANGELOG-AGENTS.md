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

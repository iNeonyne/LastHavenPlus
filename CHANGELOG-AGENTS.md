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

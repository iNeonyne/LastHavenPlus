## 2026-11-24 — Enable release-only R8 and resource shrinking

- **Role:** Main integrator / build optimization
- **Files:** `app/build.gradle.kts`, `.github/workflows/android.yml`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Enabled R8 code minification and resource shrinking for the release variant only. CI now builds debug plus an unsigned optimized release candidate, checks both APKs contain arm64-v8a only, and records file size, DEX total and largest entries.
- **Why:** The measured baseline APK was 229,073,631 bytes (218.4 MiB); its 22 DEX files totaled about 115 MiB, while `libcef.so` alone was 92.84 MiB compressed. Shrinking is the least invasive candidate to reduce the DEX payload.
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
# LastHavenPlus — Development Overview

## App summary

LastHavenPlus is a native Kotlin app that embeds the Lasthavens game in Mozilla GeckoView and bundles the Lasthavens Addon Manager as a built-in WebExtension. The game and manager share one Gecko runtime/profile so managed add-ons can be used by the game.

Main screens:
- Home: Play and Addon Manager entry points.
- Play: fullscreen game session, session retention, native web dialogs, file picker, downloads, and external-link routing.
- Addon Manager: bundled manager page, shared profile, add-on import/export/backup/restore.

## Performance and device compatibility

Tune performance for smooth gameplay, responsive controls, stable GeckoView sessions, efficient memory use, and reasonable battery and thermal behavior across supported Android devices. Use physical-device testing across different hardware and display configurations. Keep refresh-rate behavior adaptive, do not assume a fixed FPS target, and add device-specific workarounds only for reproducible compatibility issues. The POCO X6 Pro is one of the devices used for hands-on validation, not an exclusive target.

## Version snapshot

- Android app: 1.0.0 (CI build candidate; physical-device validation pending)
- Addon Manager: 1.0.0 (new project version baseline; version is tracked in the extension manifest)
- GeckoView: 159.0.20261102144711

The app and extension have independent version numbers. The extension's `manifest.json` is the source of truth for its version. Bump it with semantic versioning whenever manager functionality changes, and update both README version snapshots and the manager release notes in the same change. The `1.0.0` value is a version baseline reset; it does not mean the manager code was rewritten or that user data should be reset.

## Completed work

- [x] GitHub Actions release APK build.
- [x] Modernized Android Gradle/Kotlin configuration.
- [x] JSON and JavaScript syntax validation for the bundled extension.
- [x] APK asset verification for the extension manifest, English locale, and GeckoView ARM64 engine.
- [x] Native JavaScript alert/confirm/prompt dialog delegate.
- [x] ABI-specific APK packaging and universal fallback (CI validation pending).

## To-do / verification plan

Tasks are ordered by risk and dependency. A checked item means the repository or CI already contains the implementation/check; it does not mean the feature has passed a physical-device test.

### 1. Source-level, automated, and CI validation (before device testing)
- [ ] Complete the Kotlin, extension, Gradle, manifest, and workflow audit; record concrete findings and risks.
- [ ] Add unit tests for URL routing/allow-listing, filename sanitization, and version parsing.
- [ ] Add CI checks for release metadata, APK signing consistency, APK contents, and SHA-256 checksums.
- [ ] Add or expand automated tests for extension data preservation and add-on import/restore edge cases.
- [ ] Use emulator smoke tests where practical for launch, navigation, lifecycle, and file picker behavior.
- [ ] Fix reproducible blocking bugs before beginning physical-device validation.

### 2. Signing, upgrades, and first release
- [ ] Create and securely back up the permanent release keystore; configure the documented GitHub Actions secrets.
- [ ] Verify a signed APK installs and a later APK upgrades it without uninstalling or losing game login/session data, extension settings, or installed add-ons.
- [ ] Verify every ABI variant uses the same signing certificate across releases.
- [ ] Publish the first device-tested GitHub Release with all APK variants and SHA-256 checksums.
- [ ] Add automated app/extension/GeckoView version and changelog consistency checks.
- [ ] Keep release notes, README history, and version metadata synchronized.

### 3. First physical-device validation (deferred)
- [ ] Start only after at least a couple of substantial feature/update cycles and after known blocking issues and data risks are addressed.
- [ ] Install the ARM64 APK on a physical Android phone.
- [ ] Verify launch, Home → Play, sign-in, and actual gameplay.
- [ ] Verify Home → Play returns to the existing game session without an unnecessary reload.
- [ ] Open Addon Manager and verify it shares the game's Gecko runtime/profile.
- [ ] Install a known-compatible add-on and verify it runs in-game.
- [ ] Test archive import, export/download, backup, and restore.
- [ ] Test native JavaScript dialogs, Android file picker, and download saving.
- [ ] Test external-link routing, popups, fullscreen, back navigation, keyboard, and insets.
- [ ] Test background/foreground, network transitions, activity recreation, and Gecko process recovery.
- [ ] Record exact phone model/RAM variant and Android/vendor OS build.

### 4. Functional reliability and regression tests
- [ ] Re-run the full game/manager smoke test after changes to GeckoView, navigation, files, or lifecycle.
- [ ] Verify `ensureBuiltIn` does not overwrite existing extension data on normal startup or app upgrade.
- [ ] Test cancelled, malformed, large, and duplicate imports; verify safe download filenames and cleanup after failed downloads.
- [ ] Add Kotlin unit tests for URL routing/allow-listing, filename sanitization, and version parsing.
- [ ] Add build checks for manifest permissions, extension assets, and ABI-specific APK contents.
- [ ] Review crash logs and fix reproducible crashes before optional features.

### 5. Performance and device compatibility — measure first
- [ ] Establish cold/warm startup, memory, frame pacing, touch response, battery, and temperature baselines on physical devices.
- [ ] Test a representative short session and a 30–60 minute gameplay session.
- [ ] Compare adaptive refresh-rate behavior and display scaling without forcing a fixed refresh rate.
- [ ] Test background/resume and battery-management behavior on the primary phone and at least one other Android device.
- [ ] Test minSdk 26 and a current Android version where devices/emulators are available.
- [ ] Add device-specific workarounds only for reproducible issues; keep general Android behavior as the fallback.
- [ ] Introduce Macrobenchmark, baseline profiles, StrictMode, or leak detection only when measurements justify them.

### 6. Ongoing maintenance and deferred work
- [ ] Review GeckoView stable security updates regularly; test gameplay and the current Addon Manager version before upgrading.
- [ ] Review Gradle, Android Gradle Plugin, Kotlin, and AndroidX updates as separate build-verified changes.
- [ ] Evaluate R8/resource shrinking only with release-build and extension/runtime regression tests; minification is currently disabled.
- [ ] Consider an in-app GitHub Releases update checker/installer handoff after stable release signing is established.
- [ ] Keep full signed APK downloads as the default; defer differential updates unless download size becomes a demonstrated issue.
- [ ] Expand OEM/cloud-device testing in response to real compatibility gaps.

### Explicitly out of scope
- General-purpose browser features unrelated to the game or Addon Manager.
- Replacing GeckoView with Android WebView or another engine without a demonstrated blocker.
- Changing extension storage formats or migration behavior without a tested data-preservation and rollback plan.
- Differential APK patching, analytics, push notifications, or background services without a concrete requirement.
- Claiming device/OS support before it has been tested.

## Release rule

Only publish a version after:
1. CI build passes.
2. APK packaging checks pass.
3. A physical-device smoke test passes.
4. README version history and release notes are updated.

Push a matching `vX.Y.Z` tag to trigger GitHub Release creation and attach all generated APKs. Do not mark a CI-only candidate as device verified.

## Device smoke-test checklist

- [ ] Install the ARM64 APK.
- [ ] Launch app and open Play.
- [ ] Complete sign-in and verify gameplay.
- [ ] Return Home and reopen Play; verify session remains.
- [ ] Open Addon Manager and install a compatible add-on.
- [ ] Return to Play and verify the add-on runs.
- [ ] Import an add-on archive using Android's file picker.
- [ ] Export an add-on and save a backup ZIP.
- [ ] Restore the backup ZIP.
- [ ] Exercise alert, confirm, and prompt dialogs.
- [ ] Open an external link and confirm system-browser routing.
- [ ] Test fullscreen, back navigation, and app background/foreground.
- [ ] Force-stop or simulate a Gecko content-process crash and verify recovery.

## Known limitations

- The latest CI run reached APK packaging but failed because the configured release keystore private key could not be read. The workflow now performs an earlier keystore/alias/key-password preflight; verify the four GitHub Actions signing secrets before expecting a successful release build.
- Addon scan observes requested files; it cannot prove Lua initialization succeeded.
- Backups contain add-on files only, not SavedVariables or game settings.

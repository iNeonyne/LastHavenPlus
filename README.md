# Lasthavens for Android

A native Android shell for **Lasthavens**. It runs the game in Mozilla GeckoView and bundles the Lasthavens Addon Manager, so managed add-ons can be shared with the game through one Gecko profile.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Latest CI: https://github.com/iNeonyne/Lasthavens-Android/actions/workflows/build.yml

## Current version

| Component | Version | Notes |
|---|---:|---|
| Android app | `1.0.0` | Current source version; not yet a device-verified release |
| Addon Manager | `0.11.0` | Bundled WebExtension |
| GeckoView | `159.0.20261102144711` | Pinned Mozilla engine build |

The app and Addon Manager versions are independent. Version numbers may change with major feature updates; they are not locked to the original 1.0.0 baseline.

## What the app does

- **Home screen:** opens Play or the Addon Manager.
- **Play:** loads the Lasthavens game in GeckoView, supports fullscreen, and keeps the game session alive when returning Home.
- **Addon Manager:** opens the bundled manager in a second session using the same Gecko runtime/profile.
- **Add-ons:** the manager installs and stores add-ons; returning to Play requests a game reload so updates can take effect.
- **Files:** Android's document picker handles add-on import and backup restore; downloads are saved to the Downloads/Lasthavens folder on supported Android versions.
- **Web behavior:** native JavaScript alert/confirm/prompt dialogs, external-link routing, popup handling, and load progress feedback.

## Primary device target: POCO X6 Pro

The POCO X6 Pro is the primary performance and compatibility target for upcoming development. Its Dimensity 8300-Ultra / Mali-G615 platform, 120Hz 2712 × 1220 display, and 8GB or 12GB LPDDR5X configurations make it suitable for high-refresh gameplay, but the app must remain efficient and compatible with other Android devices.

Optimization rules:
- Prefer smooth, consistent frame pacing over chasing an unstable maximum FPS.
- Keep display refresh behavior adaptive; do not force 120Hz globally or assume the game can render at 120 FPS.
- Profile GeckoView startup, memory, long-session stability, battery use, and thermal behavior on the actual phone before tuning.
- Test touch responsiveness, fullscreen/insets, keyboard behavior, downloads, file picking, and navigation at the device's native resolution.
- Avoid POCO-only code paths unless a measured HyperOS/device issue requires one; retain sensible Android-wide fallbacks.
- Use the ARM64 APK for the POCO X6 Pro.

## Architecture

- **Kotlin + AndroidX:** native activities, app lifecycle, dialogs, file picker, and downloads.
- **GeckoView:** embeds Mozilla's browser engine. It is deliberately used instead of Android WebView because the bundled manager relies on WebExtension APIs.
- **Built-in WebExtension:** source files live in `app/src/main/assets/extensions/lh-addon-manager/`; GeckoView installs it from the APK assets.
- **Shared runtime:** game and manager sessions use the same Gecko runtime/profile so installed extension data is available to the game.
- **CI checks:** validates bundled JSON and JavaScript, compiles the release APK, and checks that the manager, locale, and GeckoView native engine are packaged.

## Build and downloads

GitHub Actions builds on pushes to `main`, pull requests, and manual dispatches.

**Download the right APK from the latest successful run's `Lasthavens-APK` artifact:**

| File suffix | Intended device |
|---|---|
| `arm64-v8a` | Most modern Android phones; choose this first |
| `armeabi-v7a` | Older 32-bit ARM Android devices |
| `x86_64` | Android emulators and x86-64 devices |
| `universal` | Fallback containing all supported CPU architectures; largest download |

The build produces three ABI-specific APKs to avoid shipping all GeckoView native engines to every phone. The universal APK is retained as a compatibility fallback. Choose `arm64-v8a` for most modern Android phones.

For a local build, open the project in Android Studio with JDK 17 and run `assembleRelease`.

## Releases and version history

A version is published as a GitHub Release only after its build and validation pass. Each release must include the matching APKs and a concise list of user-visible changes. To publish a version, update `appVersionName` and `appVersionCode` in `app/build.gradle.kts`, update this README's version table and history, commit the changes, then push a matching tag such as `v1.1.0`. The release workflow attaches all generated APK variants to that GitHub Release.

### Released versions

| Version | Status | Summary |
|---|---|---|
| 1.0.0 | CI build candidate; device testing pending | First packaged Android app baseline with GeckoView, Home/Play/Addon Manager screens, shared runtime, and bundled Addon Manager. |

### Unreleased changes on `main`

- Migrated the build to a current Android Gradle Plugin/Kotlin toolchain and public Gradle DSLs.
- Added validation for every bundled Addon Manager JSON and JavaScript file.
- Added APK-content checks for the built-in extension, English locale, and GeckoView native engine.
- Added native JavaScript alert, confirm, and prompt dialogs.
- Added ABI-specific APK outputs plus a universal fallback (being validated by CI).

For every future release, add a new row under **Released versions** and summarize the user-visible changes. Keep this README in the same commit as the version bump.

## To-do list

### Build and release
- [x] Compile a release APK in GitHub Actions.
- [x] Validate bundled extension JSON and JavaScript.
- [x] Verify required extension and GeckoView assets are present in the APK.
- [x] Add ABI-specific APK packaging and a universal fallback.
- [x] Confirm ABI APK generation and packaging in CI.
- [ ] Install the ARM64 APK on a physical phone.
- [ ] Configure a stable signing keystore so updates install over previous releases.
- [ ] Publish the first device-tested GitHub Release.
- [ ] Add an automated version/changelog consistency check.

### POCO X6 Pro device validation and optimization
- [ ] Confirm the exact phone variant (8GB or 12GB RAM) and Android/HyperOS build used for testing.
- [ ] Install the ARM64 APK and verify first launch, permissions, and update behavior.
- [ ] Check layout, display cutout/insets, fullscreen, keyboard, and touch targets at 2712 × 1220 with the user's display scaling.
- [ ] Measure cold start and warm start times for Home, Play, and Addon Manager.
- [ ] Profile GeckoView memory use and stability during a long game session and repeated Play ↔ Home ↔ Manager navigation.
- [ ] Measure frame pacing and touch responsiveness; compare 60Hz and higher refresh settings without forcing a global refresh rate.
- [ ] Check battery drain and device temperature during a representative 30–60 minute session; tune only from measured results.
- [ ] Test HyperOS backgrounding, app resume, battery restrictions, and Game Turbo interaction.
- [ ] Check network transitions, reconnect behavior, downloads, and file picker behavior on the phone.
- [ ] Keep the app usable on other Android devices; add device-specific workarounds only when reproducible.

### App functionality
- [ ] Test game launch, sign-in, and actual gameplay on a phone.
- [ ] Confirm the game session survives Home → Play navigation.
- [ ] Install a known-compatible add-on and confirm it runs in-game.
- [ ] Test add-on ZIP import, export/download, backup, and restore.
- [ ] Test JavaScript alert, confirm, and prompt dialogs in real game flows.
- [ ] Test external links, popups, fullscreen, and back navigation.
- [ ] Test background/foreground transitions, activity recreation, and Gecko content-process crashes.
- [ ] Add automated Kotlin unit tests for URL routing, filename handling, and version parsing.
- [ ] Add a repeatable smoke-test checklist for every release.

### Size and performance
- [ ] Measure per-ABI APK sizes and compare them with the current universal APK.
- [ ] Profile cold start, memory use, and game responsiveness on the POCO X6 Pro.
- [ ] Evaluate safe R8/resource shrinking after confirming GeckoView keep rules.
- [ ] Keep GeckoView pinned to an exact stable version and review updates for extension compatibility.

## Signing

Without repository signing secrets, CI uses the Android debug key. Debug keys can differ between GitHub runners, so a later APK may not install as an update over an earlier CI APK. Uninstall the previous test build if Android reports a signature conflict.

For stable in-place updates, configure these GitHub repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Do not commit the keystore or passwords to the repository.

## Known limitations

- Live Game Scan observes add-on file requests; it cannot prove that an add-on's Lua code initialized successfully.
- Backup ZIPs contain managed add-on files only. They do not include game settings, SavedVariables, or manager enabled/disabled flags.
- The bundled extension's Gecko minimum version is 142.0; keep GeckoView at or above that version and test extension compatibility before upgrades.
- A successful CI build proves packaging and static validation, not real-device functionality.

## Project structure

```
.github/workflows/build.yml       CI build, validation, and release upload
app/src/main/java/...             Kotlin Android app
app/src/main/assets/extensions/   Bundled Addon Manager WebExtension
app/src/main/res/                 Android layouts, icons, themes, and strings
app/build.gradle.kts              Android config and app version
gradle.properties                 GeckoView version and Gradle settings
PROJECT_OVERVIEW.md               Development plan and device test checklist
README.md                         User-facing project summary, releases, and to-do list
```

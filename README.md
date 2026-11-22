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

## Performance and device compatibility

Development prioritizes smooth gameplay, responsive touch controls, stable GeckoView sessions, efficient memory use, and reasonable battery and thermal behavior across supported Android devices. Validate performance on physical devices with different hardware and display configurations. Keep refresh-rate behavior adaptive, avoid assuming a fixed FPS target, and add device-specific workarounds only for reproducible compatibility issues.

## Architecture

- **Kotlin + AndroidX:** native activities, app lifecycle, dialogs, file picker, and downloads.
- **GeckoView:** embeds Mozilla's browser engine. It is deliberately used instead of Android WebView because the bundled manager relies on WebExtension APIs.
- **Built-in WebExtension:** source files live in `app/src/main/assets/extensions/lh-addon-manager/`; GeckoView installs it from the APK assets.
- **Shared runtime:** game and manager sessions use the same Gecko runtime/profile so installed extension data is available to the game.
- **CI checks:** validates bundled JSON and JavaScript, compiles the release APK, and checks that the manager, locale, and GeckoView native engine are packaged.

## Build and downloads

GitHub Actions builds on pushes to `main`, pull requests, and manual dispatches.

**Download the right APK from the latest successful run's `Lasthavens-APKs` artifact:**

| File suffix | Intended device |
|---|---|
| `arm64-v8a` | Most modern Android phones; choose this first |
| `armeabi-v7a` | Older 32-bit ARM Android devices |
| `x86_64` | Android emulators and x86-64 devices |
| `universal` | Fallback containing all supported CPU architectures; largest download |

The build produces three ABI-specific APKs to avoid shipping all GeckoView native engines to every phone. The universal APK is retained as a compatibility fallback. Choose `arm64-v8a` for most modern Android phones.

### Verified APK sizes

The successful GitHub Actions run [#46](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45077047753) built and verified all four APKs. Sizes below are exact file sizes from that run:

| APK | Size | Bytes |
|---|---:|---:|
| `arm64-v8a` | 92.53 MiB | 97,023,158 |
| `armeabi-v7a` | 89.91 MiB | 94,275,656 |
| `x86_64` | 97.27 MiB | 102,021,015 |
| `universal` | 238.6 MiB | 250,186,943 |

These are CI artifact sizes, not download-speed estimates. Future builds may differ.

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
- [x] Confirm ABI APK generation and packaging in successful CI run #46; record exact sizes.
- [ ] Install the ARM64 APK on a physical phone.
- [ ] Configure a stable signing keystore so updates install over previous releases.
- [ ] Publish the first device-tested GitHub Release.
- [ ] Add an automated version/changelog consistency check.
- [ ] Add an in-app GitHub Releases update checker and APK download/install handoff.
- [ ] Evaluate differential APK updates; retain full signed APK downloads as the reliable fallback.

### Physical-device performance and compatibility testing
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
- [x] Measure per-ABI APK sizes and compare them with the universal APK (CI run #46).
- [ ] Investigate safe APK size reductions without removing GeckoView or manager functionality.
- [ ] Profile cold start, memory use, and game responsiveness on representative physical Android devices.
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

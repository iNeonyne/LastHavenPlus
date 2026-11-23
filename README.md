# LastHavenPlus

A native Android client for [Lasthavens](https://alt.lasthavens.com/play), built with Kotlin and Mozilla GeckoView. It includes a bundled Addon Manager WebExtension so the game and manager can share browser state and installed add-ons.

> **Project status:** beta development. Updater policy/build checks are passing, but the first main-signed in-place update and complete physical-device smoke test remain unverified. This is an unofficial, independent client; it is not endorsed by Lasthavens or Blizzard Entertainment.

## At a glance

- **Platform:** Android
- **Engine:** Mozilla GeckoView
- **Language:** Kotlin
- **Application ID:** `com.lasthavens.android`
- **Development branch:** `beta`
- **Distribution target:** GitHub Releases; no Google Play track is planned

Useful links: [Game](https://alt.lasthavens.com/play) · [Repository](https://github.com/iNeonyne/Lasthavens-Android) · [Builds](https://github.com/iNeonyne/Lasthavens-Android/actions) · [Releases](https://github.com/iNeonyne/Lasthavens-Android/releases)

## Features

- Landscape Home screen with Play and Addon Manager entry points.
- GeckoView-powered game session.
- Bundled Addon Manager WebExtension.
- Shared Gecko runtime/profile design for game and manager.
- Read-only storage dashboard for shared app/profile data, cache, and available device space.
- HTTPS-only in-app navigation for Lasthavens pages; external sites open in the system browser.
- Android integration for fullscreen, JavaScript dialogs, file selection, downloads, popups, and external links.\n- User-triggered, main-only APK updater with artifact provenance, SHA-256 and signing-certificate checks, and Android installer handoff.

## Get a demo build

1. Open [GitHub Actions](https://github.com/iNeonyne/Lasthavens-Android/actions).
2. Select the latest successful build running from the `beta` branch.
3. Download the `LastHavenPlus-Demo-APKs` artifact (or the ARM64-only artifact, if provided).
4. Choose the APK matching your device:
   - `arm64-v8a`: most modern Android phones.
   - `armeabi-v7a`: older 32-bit ARM devices.
   - `x86_64`: x86-64 devices and many emulators.
   - `universal`: fallback with all supported CPU architectures; largest file.
5. Install and test it as a development build.

**Important:** CI demo APKs are debug-signed. Do not uninstall an existing installation signed with a different certificate if its app data matters; uninstalling can erase the local app profile. A successful CI build is not proof of device compatibility.

## Build locally

Open the repository in Android Studio with JDK 17 and the Android SDK required by the project. Build with:

```bash
./gradlew assembleRelease
```

GitHub Actions runs automated checks and produces debug-signed demo APK artifacts on pushes to `beta`, pull requests, and manual dispatches. A separate permanent-key-signed main update artifact is configured but will not be published until the workflow is promoted to `main`.

## Versions

| Component | Current version | Status |
|---|---:|---|
| Android app | `2.0.0` | Development; not released or device-validated |
| Bundled Addon Manager | `1.0.2` | Crash-safe add-on replacement; preserves existing library data |
| GeckoView | `159.0.20261102144711` | Pinned dependency |

The extension version is independent of the Android app version. Check the bundled extension's `manifest.json` for its authoritative version. Existing add-on files remain in their current storage locations until that add-on is updated; new replacements use staged storage generations so an interrupted update does not replace the last committed files.


## Versioning and updates

The Android app's `versionName` follows `MAJOR.MINOR.PATCH` (Semantic Versioning). Android's numeric `versionCode` is separate and determines whether an APK can replace an installed version. The Android app, bundled Addon Manager, and GeckoView each have independent versions.

The current beta app uses `versionName = 2.0.0`. Android `versionCode` is now taken from the existing workflow's monotonically increasing `GITHUB_RUN_NUMBER`; local builds use a development-only fallback and are never update candidates. Main promotions must use a clean stable SemVer number; after promotion, `beta` restarts from that exact main version and advances as a prerelease candidate.\n\nThe beta branch contains a user-triggered updater. It queries only successful completed `push` runs from `main`, downloads a complete ABI-matched APK (universal fallback), checks artifact/run/commit provenance and metadata, verifies the APK SHA-256, and checks package ID, version code and installed signing certificate before offering Android's user-confirmed package installer. It never silently installs, uninstalls, or clears app data. Debug-signed beta APKs are not accepted as update sources. The `main` branch has not been changed, so its signed update artifact is not yet live. No public download link is published until an approved release exists. See [AGENTS.md](AGENTS.md) for the rules agents must follow.

## Project documents

| Document | Purpose |
|---|---|
| [TODO.md](TODO.md) | Current priorities, demo test plan, bugs, and deferred work |
| [CHANGELOG.md](CHANGELOG.md) | User-visible changes by version |
| [AGENTS.md](AGENTS.md) | Required instructions for AI agents and contributors |
| [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) | Technical architecture and current project state |

## Important notice

The Lasthavens Terms of Use restrict modified clients, with an exception for add-ons in the in-game AddOn Library. Whether this independent client and its bundled manager are permitted has not been confirmed. Obtain explicit guidance from the Lasthavens team before public distribution. Do not present this app as official or endorsed.

## License

No project license has been declared. Do not assume this repository grants permission to reuse or redistribute its code.

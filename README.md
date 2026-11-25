# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate or modify the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest published release:** [v1.0.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0)

## Project status

The current repository candidate uses Mozilla GeckoView 160. It is not yet published as a new stable release. The published v1.0.0 release remains unchanged until a separately approved release is created.

The GeckoView candidate was integrated into `beta` by [PR #26](https://github.com/iNeonyne/LastHavenPlus/pull/26). Its exact PR head passed the unsigned release build and unit tests. Signed optimized release build, unit tests, established signing-certificate verification and ARM64-only packaging passed Actions run [#330](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518704468) for the integrated beta commit. Physical-device gameplay, profile persistence and update installation remain unverified.

## Compatibility and identity

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

Preserve the application ID and established release signing identity to maintain update compatibility.

## Features in the current candidate

- **Game hosting:** Process-scoped GeckoRuntime; the GeckoSession is created when Play is selected.
- **Navigation:** Home screen with Play and Settings; in-game Reload, Home, Return to game, diagnostics, manual update check and Exit actions.
- **Session handling:** Home detaches the GeckoView while retaining the GeckoSession; the session is closed when the Activity composition is destroyed.
- **Settings and storage:** Separate installed APK size, app-private data size and cache size. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-app UID counters, including game and updater traffic.
- **Diagnostics:** Bounded local logs, redaction, viewing, clearing and explicit user-initiated sharing.
- **Updater:** User-initiated checks, downloads and installer handoff. Differential patch failures fall back to a separately verified full APK. No automatic download or installation.
- **Addon manager:** Not included. Replacement work remains on hold pending a new owner specification.

## GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data as part of this transition. A fresh game login may be required.

Persistent GeckoView data across force-stop and app restart—including cookies, LocalStorage, IndexedDB and service workers—has not been confirmed on physical hardware.

## Build and test

Requirements: JDK 25, Gradle 9.7.1 via the committed Gradle Wrapper, Android SDK Platform 37.2 and Android Build Tools 37.0.0.

Run from the repository root:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. Never commit keystores or signing secrets.

## Release policy

- Package ARM64 only.
- Preserve application ID and signing identity.
- Keep update downloads and installation user-initiated.
- Do not publish debug APKs or incomplete features.
- CI success does not replace physical ARM64 device testing.

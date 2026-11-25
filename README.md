# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate or modify the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest release:** [v2.0.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.0.0)

## Compatibility

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

Preserve the application ID and established release signing identity to maintain update compatibility.

## Features

- **Game hosting:** Process-scoped GeckoRuntime; the GeckoSession is created when Play is selected.
- **Navigation:** Home screen with Play and Settings; in-game Reload, Home, Return to game, diagnostics, manual update check and Exit actions.
- **Session handling:** Home detaches the GeckoView while retaining the GeckoSession; the session is closed when the Activity composition is destroyed.
- **Settings and storage:** Separate installed APK size, app-private data size and cache size. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-app UID counters, including game and updater traffic.
- **Diagnostics:** Bounded local logs, redaction, viewing, clearing and explicit user-initiated sharing.
- **Updater:** User-initiated checks, downloads and installer handoff. Redirects and response sizes are bounded; manifests, payload sizes and SHA-256 digests are validated. Differential patch failures fall back to a separately verified full APK. No automatic download or installation.
- **Addon manager:** Not included. Replacement work remains on hold pending a new owner specification.

## GeckoView profile transition and testing status

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data as part of this transition. A fresh game login may be required.

Persistent GeckoView data across force-stop and app restart—including cookies, LocalStorage, IndexedDB and service workers—has not been confirmed on physical hardware. The v2.0.0 release was approved with physical ARM64 gameplay, profile persistence and update-installation verification still outstanding. CI build and unit-test success does not establish physical-device compatibility.

## Build and test

Requirements: JDK 25, Gradle 9.7.1 via the committed Gradle Wrapper, Android SDK Platform 37.2 and Android Build Tools 37.0.0.

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
- Complete the outstanding physical-device verification in follow-up updates.

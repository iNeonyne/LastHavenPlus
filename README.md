# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate or modify the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest published release:** [v2.0.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.0.0)
- **v2.1.0:** promotion candidate; publication follows final release verification.

## Compatibility

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (arm64-v8a) |
| Browser engine | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | com.neonyne.lasthavenlauncher |
| Game URL | https://lasthavens.com/play |

Preserve the application ID and established release signing identity to maintain update compatibility.

## Features in the v2.1.0 candidate

- **Launcher Home:** Responsive compact and wide layouts with Play, Settings and update entry points. Play lazily creates the GeckoView session; Settings does not initialize the game.
- **Gameplay menu:** Android Back opens a full-screen menu with exactly Reload, Settings and Return to game. The persistent top-right Menu button is removed.
- **Navigation:** Settings, diagnostics and update details are full-screen pages. The existing GeckoSession is retained while its view is detached for launcher navigation.
- **Launcher icon:** Adaptive launcher icon resources are included.
- **Settings and storage:** Installed APK size, persistent app-private data size and cache size are reported separately. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-UID counters include both game and updater traffic; they are not game-only totals.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing.
- **Updater:** Checks, downloads and installation remain user-initiated. Metadata, payload size and digest are validated; differential failures retain a verified full-APK fallback. Android's package installer controls installation.
- **Addon manager:** Not included. The owner has approved investigation of a supplied XPI manager under issue #23, but no implementation is integrated.

## GeckoView profile transition and testing

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data. A fresh game login may be required.

The owner reports that the app and GeckoView work on an ARM64 device. The device model, Android version, exact tested commit and individual test results have not been recorded. This does not establish full WebGL/WASM, WebSockets, audio, fullscreen, profile persistence, process recovery or updater compatibility, nor does it verify every v2.1.0 UI behavior. CI success does not replace physical-device testing.

## Build and test

Requirements: JDK 25, Gradle 9.7.1 via the committed Gradle Wrapper, Android SDK Platform 37.2 and Android Build Tools 37.0.0.

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. Never commit keystores or signing secrets.

## Release policy

- Package ARM64 only.
- Preserve application ID and established signing identity.
- Keep update checks, downloads and installation user-initiated.
- Never publish debug APKs or incomplete features.
- CI build/signing/ABI checks do not replace physical ARM64 testing.
- The v2.1.0 candidate remains unpublished until release gates are complete.

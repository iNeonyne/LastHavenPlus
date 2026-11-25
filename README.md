# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate or modify the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest published release:** [v2.1.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.1.0)
- **v2.1.0:** published 2026-11-25.

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

## Features in the published v2.1.0 release

- **Launcher Home:** Responsive compact and wide layouts with Play, Settings and update entry points. Play lazily creates the GeckoView session; Settings does not initialize the game.
- **Gameplay menu:** Android Back opens a full-screen menu with exactly Reload, Settings and Return to game. The persistent top-right Menu button is removed.
- **Navigation:** Settings, diagnostics and update details are full-screen pages. The existing GeckoSession is retained while its view is detached for launcher navigation.
- **Launcher icon:** Adaptive launcher icon resources are included.
- **Settings and storage:** Installed APK size, persistent app-private data size and cache size are reported separately. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-UID counters include both game and updater traffic; they are not game-only totals.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing.
- **Updater:** Checks, downloads and installation remain user-initiated. Metadata, payload size and digest are validated; differential failures retain a verified full-APK fallback. Android's package installer controls installation.
- **Addon manager:** Not included; work is on hold under issue #23 pending renewed explicit owner approval.

## GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data. A fresh game login may be required.

The repository owner evaluates published releases on their own schedule and may report gameplay, profile, browser API, media and updater behavior. Feedback is used to prioritize follow-up fixes.

## Build and test

Requirements: JDK 25, Gradle 9.7.1 via the committed Gradle Wrapper, Android SDK Platform 37.2 and Android Build Tools 37.0.0.

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. Never commit keystores or signing secrets.


## v2.2.0 candidate changes (not yet published)

The current development candidate includes:
- Direct launch into the game with a responsive in-game overlay for Reload, Settings and Return to game.
- Session-preserving navigation between gameplay and Settings.
- Recreated LastHavens-inspired adaptive launcher artwork. This is newly recreated artwork, not the exact official game icon.

These changes are not part of the published v2.1.0 release. The v2.2.0 version number, tag and publication remain owner-controlled.

## Published v2.1.0 artifact

- Tag: `v2.1.0` at commit `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.
- APK: `lasthavenplus-arm64.apk`, 96,444,836 bytes; SHA-256 `f65672c8855298764b3345e58694b672c39f35971a39beccb7e4d1feb6310cab`.
- The release contains one ARM64 APK, one update manifest and one differential patch. The release workflow verified the established signing certificate and `arm64-v8a`-only packaging.

## Release policy

- Package ARM64 only.
- Preserve application ID and established signing identity.
- Keep update checks, downloads and installation user-initiated.
- Never publish debug APKs or incomplete features.
- The repository owner may provide feedback after installing a release; feedback is not a publication prerequisite.
- v2.1.0 is published. Do not republish or replace it without explicit owner approval.

## Project guidance

- [Contributing](CONTRIBUTING.md): development workflow, build checks and release requirements.
- [Agent instructions](AGENTS.md): repository constraints and owner-controlled integration policy.
- [Security policy](SECURITY.md): supported versions and private vulnerability reporting guidance.
- [Reproducible build status](docs/REPRODUCIBLE_BUILDS.md): current guarantees and the verification work still outstanding.


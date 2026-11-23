# LastHavenPlus

An unofficial native Android client for [Lasthavens](https://lasthavens.com/play). The project is undergoing a major rebuild to use Android System WebView (Chromium) for gameplay and a native-backed Addon Manager.

> **Status:** v1.0.0 was the first public release. The current source is the transition baseline; the Chromium game screen and native Addon Manager are not yet complete or released. This project is independent and is not endorsed by Lasthavens or Blizzard Entertainment.

## Project

- **Platform:** Android
- **Language:** Kotlin, JavaScript
- **Target engine:** Android System WebView supplied by the device
- **Application ID:** com.lasthavens.android
- **Development branch:** main
- **Distribution:** GitHub Releases; no Google Play track

Useful links: [Game](https://lasthavens.com/play) · [Repository](https://github.com/iNeonyne/Lasthavens-Android) · [Builds](https://github.com/iNeonyne/Lasthavens-Android/actions) · [Releases](https://github.com/iNeonyne/Lasthavens-Android/releases)

## Current source and target

The checked-in source still contains the existing GeckoView game screen and WebExtension-based Addon Manager, plus a debug-only Chromium WebView probe. They are transitional code, not the target architecture.

The target app uses Android System WebView without bundling a second browser engine. The Addon Manager will use native app-private storage and a restricted local WebView host. The migration is planned to preserve installed add-ons and manager settings. Gecko login cookies/session state are not assumed to transfer to WebView, so a new sign-in may be required.

See [ROADMAP.md](ROADMAP.md) for the architecture and phase gates, and [TODO.md](TODO.md) for the one active implementation queue.

## Existing app capabilities

The current source includes native Home, Play, Addon Manager and Storage screens, a stable-release updater, HTTPS navigation policy, Android file selection/download handling and the current 1.0.5 Addon Manager implementation. The target architecture and migration status are tracked separately; planned Chromium functionality must not be described as shipped.

## Build

Open the project in Android Studio with JDK 17 and the Android SDK required by the project. Build with:

```bash
./gradlew assembleRelease
```

GitHub Actions runs automated validation and builds ARM64 APKs. Main builds are validation artifacts, not updater sources. Tagged builds create draft GitHub Releases signed with the permanent release key. A draft must be reviewed and explicitly published by the owner.

## Updates and app identity

The native updater is independent of the browser engine. Preserve its metadata-only version check, explicit version selection, full ARM64 APK download, SHA-256 verification, package/version/ABI/signing-certificate checks and Android installer confirmation. It must never clear app data or silently install an update.

The application ID and permanent signing identity must remain unchanged so compatible in-place updates remain possible. Android versionCode comes from the existing globally increasing GitHub Actions run-number sequence. Keep stable SemVer versionName values; do not bump them for ordinary commits.

See [AGENTS.md](AGENTS.md) for mandatory development and release rules.

## Project documents

| Document | Purpose |
|---|---|
| [ROADMAP.md](ROADMAP.md) | Approved target architecture, delivery phases and acceptance gates |
| [TODO.md](TODO.md) | Single actionable development queue |
| [AGENTS.md](AGENTS.md) | Mandatory agent/contributor rules |
| [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) | Current source architecture and verified status |
| [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md) | Owner-only legal, key-custody and release actions |
| [CHANGELOG.md](CHANGELOG.md) | User-visible changes that have actually shipped |

## Distribution notice

The Lasthavens Terms of Use restrict modified clients except for add-ons in the in-game AddOn Library. Permission to distribute this independent client and its bundled manager has not been confirmed. Obtain written guidance from the Lasthavens team before public distribution. Do not present this app as official or endorsed.

No project license has been declared. Do not assume this repository grants permission to reuse or redistribute its code.

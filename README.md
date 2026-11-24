# LastHavenLauncher

A minimal Android launcher for the existing **LastHaven WotLK private server**. The game is already developed; this app only embeds the browser engine and opens it.

**Default game URL:** https://alt.lasthavens.com/play

## Current implementation

- Native Android app: Kotlin + Jetpack Compose
- Embedded engine: Cefrium 0.21.0 (Chromium 156.0.8078.96)
- Browser surface: Cefrium Compose API, `CefriumView`
- Product ABI: `arm64-v8a` only
- Minimum Android: 10 (API 29)
- Fixed application ID: `com.neonyne.lasthavenlauncher`

Cefrium's `CefriumView` is a Compose API, not the classic Android `View` class. It is the API named in the project requirements. The classic View integration is `CefriumBrowser.createWithSurface(activity)`.

## Build requirements

Cefrium 0.21.0 requires:

- JDK 25
- Gradle 9.7.1
- Android Gradle Plugin 9.4.0
- Android SDK / compileSdk 37
- 4 GB Gradle heap for Cefrium dexing

Cefrium is pinned at 0.21.0. The Codeberg Maven registry mirrors the current release and the previous release; it is configured in both plugin and dependency resolution. Update the pinned version and plugin together after checking the official release notes.

Official references:
- [Cefrium Quickstart](https://cefrium.com/quickstart/)
- [Cefrium Releases](https://cefrium.com/releases/)
- [Cefrium API reference](https://cefrium.com/docs/)

## Build

Install JDK 25, Android SDK 37 and Gradle 9.7.1, then run:

```bash
gradle assembleDebug
gradle testDebugUnitTest
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds the debug APK and publishes it as a workflow artifact. A release keystore is intentionally not stored in this repository. Configure and securely back up the release signing key before distributing the first release build; never commit it.

## Priorities

| Priority | Deliverable |
|---|---|
| P0 | Minimal app, Cefrium integrated, LastHaven loads on arm64 |
| P1 | Fullscreen gameplay, touch/audio, lifecycle, persistent storage, retry handling |
| P2 | Minimal settings, verified compatibility, measured performance, APK size |
| P3 | Release APK, stable signing, update/data-preservation verification |

## In-app updater

- The launcher checks the latest stable GitHub Release on startup and also offers a manual check from the exit dialog.
- It never downloads automatically. Select **Download smaller update** or **Download full APK instead**; Android handles the final install confirmation.
- Every stable release uses a `vMAJOR.MINOR.PATCH` tag and publishes a signed `lasthavenlauncher-arm64.apk` plus `lasthavenlauncher-update.json`.
- When the immediately previous stable release is available and uses the same signing certificate, CI creates a BSDIFF40 patch. It publishes the patch only when it is at least 10% smaller than the full APK.
- The app offers a patch only when the installed version, versionCode and APK SHA-256 match that patch's base. It reconstructs the exact signed APK locally, verifies its digest, package ID, version and pinned release certificate, then opens Android's installer.
- The complete signed APK is always published as a fallback. If a user skips a version or the patch cannot be used, they can choose the full APK.
- Release signing is configured through GitHub Actions secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` and `ANDROID_KEY_PASSWORD`, using alias `lasthavenplus`. The key is never committed or bundled in the app.

## Storage policy

- Cookies, IndexedDB, LocalStorage and Cache Storage are game data and must persist.
- Never clear browser data automatically.
- “Clear game cache” may clear only HTTP and code caches.
- “Clear game data” must be a separate destructive action with confirmation.
- APK updates must use the same application ID and release signing key.

## Agent workflow

Read [AGENTS.md](AGENTS.md) before editing. Keep [STATUS.md](STATUS.md) current and append every meaningful change to [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md). Claim a file or work area before editing it. Keep the workflow lean and prioritize a playable build.

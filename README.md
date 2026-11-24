# LastHavenPlus

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

## Build locally

Install JDK 25, Android SDK 37 and Gradle 9.7.1, then run:

```bash
gradle assembleDebug
gradle testDebugUnitTest
```

The Gradle Wrapper is not yet committed. Adding the wrapper and switching CI/local build instructions to `./gradlew` is tracked in [issue #4](https://github.com/iNeonyne/Lasthavens-Android/issues/4).

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds ARM64 debug and signed release variants. Release signing uses repository secrets; the private keystore is never stored in the repository. See [STATUS.md](STATUS.md) for the latest verification state.

## Work tracking

**GitHub Issues are the single source of truth for all unfinished work.** Use the issue checklists for scope and acceptance criteria. [Open issues](https://github.com/iNeonyne/Lasthavens-Android/issues).

| Priority | Tracked work |
|---|---|
| P0.1 | [Build reproducibility, updater tests and CI verification](https://github.com/iNeonyne/Lasthavens-Android/issues/4) |
| P0.2 | [Physical-device gameplay, persistence and update verification](https://github.com/iNeonyne/Lasthavens-Android/issues/5) |
| P2 | [Minimal settings and safe storage controls](https://github.com/iNeonyne/Lasthavens-Android/issues/6) |
| P3 | [Measure runtime performance and APK-size improvements](https://github.com/iNeonyne/Lasthavens-Android/issues/7) |
| Deferred | [Addon manager — only after the core launcher is stable](https://github.com/iNeonyne/Lasthavens-Android/issues/8) |

P0/P1 gameplay reliability work is the priority. Do not start optional settings, performance tuning or the addon manager ahead of a verified playable core.

## In-app updater

- The launcher checks the latest stable GitHub Release on startup and also offers a manual check from the exit dialog.
- It never downloads automatically. Select **Download smaller update** or **Download full APK instead**; Android handles the final install confirmation.
- Every stable release uses a `vMAJOR.MINOR.PATCH` tag and publishes a signed `lasthavenlauncher-arm64.apk` plus `lasthavenlauncher-update.json`.
- When the immediately previous stable release is available and uses the same signing certificate, CI creates a BSDIFF40 patch. Unchanged Cefrium/Chromium bytes are reused from the installed APK instead of being downloaded again. CI publishes the patch only when it is at least 10% smaller than the full APK; the app reconstructs a complete signed APK locally for Android to install.
- The app offers a patch only when the installed version, versionCode and APK SHA-256 match that patch's base. It reconstructs the exact signed APK locally, verifies its digest, package ID, version and pinned release certificate, then opens Android's installer.
- The complete signed APK is always published as a fallback. If a user skips a version or the patch cannot be used, they can choose the full APK.
- Release signing is configured through GitHub Actions secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` and `ANDROID_KEY_PASSWORD`, using alias `lasthavenplus`. The key is never committed or bundled in the app.

## Storage policy

- Cookies, IndexedDB, LocalStorage and Cache Storage are game data and must persist.
- Never clear browser data automatically.
- “Clear game cache” may clear only HTTP and code caches, and only after the pinned Cefrium API is verified.
- “Clear game data” must be a separate destructive action with confirmation.
- APK updates must use the same application ID and release signing key.

## Agent workflow

Read [AGENTS.md](AGENTS.md) before editing. Keep [STATUS.md](STATUS.md) limited to current progress, active ownership, evidence and blockers. Append every meaningful change to [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md).

Use GitHub Issues for task scope and checklists; do not maintain a second TODO list in repository documentation. Claim an issue and work area before editing. Keep the workflow lean, prioritize a playable game, and have the main orchestrator integrate and push directly to `main` without pull requests.

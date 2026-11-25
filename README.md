# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate, modify or develop the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest stable release:** [v2.0.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.0.0)
- **Development:** `beta`; stable releases: `main`

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

## Current beta features

These are source-level features integrated into beta. CI verifies compilation and unit tests; it does not prove all runtime behavior on physical hardware.

- **Launcher home:** Branded, responsive home screen with Play, Settings and update entry points. Play lazily creates the GeckoView session; opening Settings does not initialize the game.
- **Gameplay menu:** Opened with Android Back; contains exactly Reload, Settings and Return to game. The persistent top-right Menu button is removed.
- **Navigation:** Settings, diagnostics and update details are full-screen pages rather than stacked popup dialogs. The existing GeckoSession is retained when the game view is detached for launcher pages.
- **Responsive layout:** Home switches between compact and wide layouts; settings pages cap content width on larger windows. The Activity is resizeable.
- **Launcher icon:** Adaptive launcher icon resources are included.
- **Settings and storage:** Installed APK size, persistent app-private data size and cache size are reported separately. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-UID counters report app traffic, including both game and updater traffic; they are not game-only totals.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing. Review logs before sharing.
- **Updater:** Stable release checks and downloads remain user-initiated. Manifest, version, URL, payload and digest validation are applied; differential patch failures retain a verified full-APK fallback. Android's package installer controls installation.
- **Addon manager:** On hold by the owner. Do not resume issue #23 or integrate the supplied XPI until the owner explicitly approves resuming it. The legacy ZIP package manager is not part of this app.

## GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data as part of this transition. A fresh game login may be required.

The owner has reported that the app and GeckoView work on an ARM64 device. The device model, Android version, exact tested commit and individual feature results have not been recorded here. This does not verify the v2.1.0 UI changes, WebGL/WASM, WebSockets, audio, fullscreen, profile persistence, process recovery or updater installation. See [STATUS.md](STATUS.md) and [docs/GECKOVIEW_MIGRATION.md](docs/GECKOVIEW_MIGRATION.md).

## Build and test

Requirements: JDK 25, Gradle 9.7.1 via the committed Gradle Wrapper, Android SDK Platform 37.2 and Android Build Tools 37.0.0.

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. CI obtains these from repository secrets; never commit keystores or credentials.

## Release policy

- Package ARM64 only.
- Preserve application ID and established signing identity.
- Keep update checks, downloads and installation user-initiated.
- Never publish debug APKs or incomplete features.
- Promote beta only after release gates pass and the owner explicitly approves.
- Physical-device verification must be recorded against the exact tested build; CI does not replace it.

## Project workflow and records

- `beta` is the development, integration and testing branch; `main` is the stable release branch.
- Task PRs target `beta`. Chaty alone reviews and merges into beta.
- `ISSUES.md` and GitHub Issues are the task sources of truth.
- `STATUS.md` records verified, unverified and incomplete work plus exact CI evidence.
- `AGENTS.md` and `AGENT_WORK.md` define contributor workflow and active assignments.
- `CHANGELOG-AGENTS.md` and `WORK_LOG.md` preserve project/work-session history.

See [Mozilla GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html), [quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html) and [WebExtensions documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html).

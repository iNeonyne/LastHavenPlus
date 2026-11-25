# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate or modify the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest published release:** [v2.2.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.2.0)
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

## Features in v2.2.0

- **Direct launch:** The app opens directly into the game.
- **In-game menu:** Android Back opens a responsive overlay with Reload, Settings and Return to game.
- **Session-preserving navigation:** The existing GeckoSession is retained while navigating to Settings and back to gameplay.
- **Responsive layout:** The in-game menu adapts to available window space; Settings pages cap content width on larger windows.
- **Launcher icon:** Owner-approved recreated LastHavens-inspired adaptive artwork is included. It is newly recreated artwork, not the exact official game icon.
- **Settings and storage:** Installed APK size, persistent app-private data size and cache size are reported separately. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-UID counters include both game and updater traffic; they are not game-only totals.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing. Review logs before sharing.
- **Updater:** Stable release checks and downloads remain user-initiated. Manifest, version, URL, payload and digest validation are applied; differential patch failures retain a verified full-APK fallback. Android's package installer controls installation.
- **Addon manager:** Not included; work remains on hold under issue #23 pending renewed explicit owner approval.

## GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data as part of this transition. A fresh game login may be required.

The repository owner evaluates published releases on their own schedule and may report gameplay, profile, browser API, media and updater behavior. Feedback is recorded in GitHub Issues and used to prioritize follow-up fixes.

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
- The repository owner may provide feedback after installing a release; pending feedback is not a publication prerequisite.

## Project workflow and records

- `beta` is the development, integration and testing branch; `main` is the stable release branch.
- Task PRs target `beta`. Chaty alone reviews and merges into beta.
- `ISSUES.md` and GitHub Issues are the task sources of truth.
- `STATUS.md` records verified, unverified and incomplete work plus exact CI evidence.
- `AGENTS.md` and `AGENT_WORK.md` define contributor workflow and active assignments.
- `CHANGELOG-AGENTS.md` and `WORK_LOG.md` preserve project/work-session history.

See [Mozilla GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html), [quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html) and [WebExtensions documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html).

# LastHavenPlus

LastHavenPlus is a lightweight Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate, modify, or develop the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Stable release:** [v2.0.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.0.0)
- **Project backlog:** [ISSUES.md](ISSUES.md)
- **Verified project status:** [STATUS.md](STATUS.md)

## Project state

Development continues on `beta`; `main` is the stable release branch. The published stable release is **v2.0.0**. Its GitHub release metadata confirms the ARM64 APK asset `lasthavenplus-arm64.apk` and update manifest `lasthavenplus-update.json`.

The current beta source includes Mozilla GeckoView 160, the launcher Home screen, Play/Settings navigation and updater hardening. Updater hardening was integrated in beta at commit `1d6c1ec42becab44735edfd0323dfc60ff4b9f9d`. Exact-head unsigned PR validation passed in [Actions #345](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45521843766), and the signed ARM64 branch build, unit tests, signing-certificate and ABI checks passed in [Actions #346](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45522408962) for that exact merge commit. The current beta HEAD is `1f300b90e5aed9a5eaa86e473b746f0841d42b16`; the commits after #346 are documentation-only. PR #32 was superseded. Replacement [PR #34](https://github.com/iNeonyne/LastHavenPlus/pull/34) adds byte-bounded release inputs and regression tests; its latest exact-head CI is still running.

CI success does not establish physical-device compatibility. Real ARM64 gameplay, browser-profile persistence, navigation, media and in-place update installation remain unverified.

## Compatibility and identity

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

Preserve the application ID and established release signing identity. Do not change either as part of browser, UI or updater work.

## Current beta features

The following describes the source in beta. Runtime behavior not tested on physical ARM64 hardware remains explicitly unverified in [STATUS.md](STATUS.md).

- **Game hosting:** A process-scoped GeckoRuntime and GeckoSession host the existing game. Game initialization is deferred until the user selects Play.
- **Navigation:** The launcher opens Home with Play and Settings. The in-game menu provides Reload, Home, Return to game, Settings, diagnostics, manual update check and Exit. Home navigation retains the GeckoSession. Physical interaction and lifecycle behavior remain unverified.
- **Settings and storage:** Settings report installed APK size, persistent app-private data size and cache size separately. Persistent app-private data includes GeckoView data; it is not a profile-only measurement.
- **Network usage:** Android per-UID traffic counters report sent and received bytes since boot and, where supported, during the current app session. These totals include game and updater traffic.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing. Redaction is implemented; remaining verification is recorded in STATUS.
- **Updater:** The app checks stable GitHub releases and provides explicit update download and Android installer handoff. It validates release metadata, download size/digest and package identity. Differential patch failures fall back to a separately verified full APK. Updates are never downloaded or installed automatically. Physical installation and recovery remain unverified.
- **Addon manager:** No addon manager is currently integrated in beta. The owner has approved resuming work on the supplied LastHavens Addon Manager XPI. The planned integration is tracked in [issue #23](https://github.com/iNeonyne/LastHavenPlus/issues/23), with a dedicated Home-screen button. It is not implemented and is not part of v2.0.0.

### GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy Cefrium profile data into GeckoView or delete the old profile as part of migration. A fresh game login may be required.

Persistence of GeckoView cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication across force-stop/restart and app update has not yet been confirmed on physical hardware.

## Build and test

### Requirements

- JDK 25
- Gradle 9.7.1 via the committed Gradle Wrapper
- Android SDK Platform 37.2
- Android Build Tools 37.0.0

The Gradle Wrapper distribution is pinned with a SHA-256 checksum. GeckoView 160 requires the Android compile SDK minor level used by this project. Do not change Gradle, Android Gradle Plugin, JDK, SDK, GeckoView, signing configuration or application ID without compatibility evidence.

### Commands

Run from the repository root:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. CI obtains these from repository secrets; never commit the private keystore or credentials. Debug APKs must not be published as release artifacts.

### CI and device verification

- [Actions #345](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45521843766): exact PR-head unsigned release build and unit tests passed for the updater-hardening PR.
- [Actions #346](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45522408962): signed release build, unit tests, signing-certificate verification, ARM64-only ABI verification and artifact upload passed for beta merge commit `1d6c1ec42becab44735edfd0323dfc60ff4b9f9d`.
- These runs predate the documentation-only beta HEAD `1f300b90e5aed9a5eaa86e473b746f0841d42b16`. No app source changes are present in the intervening documentation commits.
- PR #32 was superseded after cancelled validation; PR #34 is the active release-workflow fix and must pass exact-head CI before merge.
- CI does not replace physical ARM64 device testing.

See [STATUS.md](STATUS.md) for exact commits, outstanding checks and blockers.

## Download and update

Install the stable APK from [GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases). The published v2.0.0 release includes `lasthavenplus-arm64.apk` and `lasthavenplus-update.json`.

The updater is opt-in: it checks for a newer stable release and lets the user choose whether to download and install it. A differential patch is eligible only when the installed base version and APK digest match. Otherwise, the full APK is used. The downloaded package is checked for digest, package identity, version and signing certificate before Android's installer is opened.

Do not add automatic update downloads or installation. Do not publish debug APKs as release assets.

## Privacy and data handling

- Network counters are local Android per-app totals and include all app networking.
- Diagnostic logs are stored locally, bounded in size and redacted before writing. Sharing requires an explicit user action.
- GeckoView data is stored in app-private storage. Do not clear browser data or legacy Cefrium profile files without an explicitly approved data-management action.
- Do not add analytics, tracking or background update installation.

## Project workflow and documentation

Read [AGENTS.md](AGENTS.md) and [AGENT_WORK.md](AGENT_WORK.md) before working on the repository.

- **`beta`:** active development, integration and testing branch.
- **`main`:** stable release branch.
- **Pull requests:** contributors work on dedicated branches and open PRs targeting `beta`. Parallel contributors must work on separate issues with non-overlapping file scopes.
- **Integration:** Chaty alone reviews and merges accepted PRs into `beta`.
- **PR checks:** PRs targeting `beta` run an unsigned release-variant build and unit tests without signing secrets or release artifacts. Full signed APK, certificate and ABI checks remain on branch/tag builds.
- **Promotion:** `beta` is promoted to `main` only after release gates pass and the repository owner explicitly approves. Publishing a release requires separate explicit approval.

Project records:

- [`ISSUES.md`](ISSUES.md) is the version-controlled backlog; GitHub Issues and this file are the task sources of truth.
- [`STATUS.md`](STATUS.md) records implementation state, verification evidence, active assignments and release blockers.
- [`CHANGELOG-AGENTS.md`](CHANGELOG-AGENTS.md) records meaningful agent and project changes.
- [`WORK_LOG.md`](WORK_LOG.md) records work-session history.
- [`docs/GECKOVIEW_MIGRATION.md`](docs/GECKOVIEW_MIGRATION.md) contains GeckoView migration references and notes.

TickTick may be used as a supplementary personal task tracker; it does not replace GitHub Issues or the repository backlog.

## GeckoView references

- [Mozilla GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [GeckoView quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [GeckoView architecture](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [WebExtensions in GeckoView](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [GeckoView site permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)

# LastHavenPlus

LastHavenPlus is a lightweight Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate, modify, or develop the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Stable release:** [v1.0.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0)
- **Project backlog:** [ISSUES.md](ISSUES.md)
- **Verified project status:** [STATUS.md](STATUS.md)

## Project state

Development is active on `beta`. The stable release remains on `main`.

The current beta source uses Mozilla GeckoView 160 and includes the home screen, Play/Settings navigation and updater hardening merged by [PR #26](https://github.com/iNeonyne/LastHavenPlus/pull/26) at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Exact-head PR validation passed the unsigned release build and unit tests. The post-merge signed ARM64 branch build and physical-device behavior still require verification. Beta CI is configured to reuse the latest stable versionName/versionCode for its signed demo artifact without creating a GitHub release.

The GeckoView runtime and app shell are implemented in source, but successful CI builds do not establish that the game works correctly on a physical device. Game compatibility, profile persistence and the complete navigation flow still require physical ARM64 testing.

## Compatibility and identity

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

The application ID and established release signing identity must remain unchanged to preserve update compatibility. Do not change either as part of browser or UI work.

## Current features

The following describes what is present in the beta source. Runtime behavior that has not been tested on physical ARM64 hardware is explicitly identified as unverified in [STATUS.md](STATUS.md).

- **Game hosting:** A process-scoped GeckoRuntime and GeckoSession host the LastHaven game. Game initialization is deferred until Play.
- **Navigation:** The launcher opens Home with Play and Settings. The game menu provides Reload, Home, Return to game, Settings, diagnostics, manual update check and Exit. Home navigation detaches the GeckoView while preserving its session. These flows passed the PR's unsigned build/unit-test CI; physical interaction remains unverified.
- **Settings and storage:** Settings report installed APK size, persistent app-private data size and cache size. Persistent data includes GeckoView data; it is not a profile-only measurement.
- **Network usage:** Android per-app traffic counters report received and sent bytes since boot and, where supported, during the current app session. These totals include game and updater traffic, not just game traffic.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing. Redaction is implemented; see the status report for remaining verification.
- **Updater:** The app checks stable GitHub releases and provides explicit update download and installer handoff. Differential patch failures fall back to a separately verified full APK. URL, version, digest and fallback policy tests passed in PR validation. Physical update installation remains unverified. Updates are never downloaded or installed automatically.
- **Addon manager:** No addon manager is included. The previous ZIP package manager was removed at the owner's request and replacement work is on hold pending a new specification. Storing an addon package is not the same as injecting or loading it in the game.

### GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Existing Cefrium profile data must not be copied into GeckoView or deleted as part of the migration. A fresh game login may be required.

Persistent GeckoView data across force-stop and app restart—including cookies, LocalStorage, IndexedDB and service workers—has not yet been confirmed on physical hardware.

## Build and test

### Requirements

- JDK 25
- Gradle 9.7.1 via the committed Gradle Wrapper
- Android SDK Platform 37.2
- Android Build Tools 37.0.0

The Gradle Wrapper distribution is pinned with a SHA-256 checksum. GeckoView 160 requires the Android compile SDK minor level used by this project. Do not change Gradle, Android Gradle Plugin, JDK, SDK, GeckoView, signing configuration or application ID without first validating compatibility.

### Commands

Run from the repository root:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. CI obtains these from repository secrets; never commit the private keystore or credentials. Debug APKs must not be published as release artifacts.

### CI evidence

The latest verified successful Android build and unit-test task is [Actions run #310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45508671565), for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`. The signed release build passed signing-certificate and ARM64-only ABI verification. This run predates the new PR-only validation job.

That run predates the current beta HEAD. The six later commits changed workflow/documentation files rather than application source; the subsequent CI workflow optimization has not yet been independently verified. Check [GitHub Actions](https://github.com/iNeonyne/LastHavenPlus/actions) and [STATUS.md](STATUS.md) for the latest evidence. CI does not replace physical-device testing.

## Download and update

The stable signed APK is published through [GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases). Install it through Android and approve the package installer prompt.

The updater is opt-in: it checks for a newer stable release and lets the user choose whether to download and install it. A differential patch is eligible only when the installed base version and APK digest match; otherwise, the full APK is used. The downloaded package is checked for its digest, package identity, version and signing certificate before Android's installer is opened.

Do not add automatic update downloads or installation. Do not publish debug APKs as release assets.

## Privacy and data handling

- Network counters are local Android per-app totals and include all app networking.
- Diagnostic logs are stored locally, bounded in size and redacted before writing. Sharing requires an explicit user action.
- GeckoView data is stored in the app's private storage. Do not clear browser data or legacy Cefrium profile files without an explicitly approved data-management action.
- Do not add analytics, tracking or background update installation.

## Project workflow and documentation

Read [AGENTS.md](AGENTS.md) and [AGENT_WORK.md](AGENT_WORK.md) before working on the repository.

- **`beta`:** active development, integration and testing branch.
- **`main`:** stable release branch.
- **Pull requests are allowed:** contributors work on dedicated branches and open PRs targeting `beta`. Parallel contributors must work on separate issues with non-overlapping file scopes.
- **Integration:** contributors submit task branches as PRs targeting `beta`. Chaty alone reviews and merges accepted PRs into `beta`.
- **PR checks:** PRs targeting `beta` run an unsigned release-variant build and unit tests without signing secrets or release artifacts. Full signed APK, certificate and ABI checks remain on branch/tag builds.
- **Promotion:** `beta` is promoted to `main` only after release gates pass and the repository owner explicitly approves. Publishing a release requires explicit approval.

Project records:

- [`ISSUES.md`](ISSUES.md) is the version-controlled backlog. GitHub Issues and this file are the sources of truth.
- [`STATUS.md`](STATUS.md) records implementation state, verification evidence, active assignments and release blockers.
- [`CHANGELOG-AGENTS.md`](CHANGELOG-AGENTS.md) records meaningful agent and project changes.
- [`WORK_LOG.md`](WORK_LOG.md) records the work session history.
- [`docs/GECKOVIEW_MIGRATION.md`](docs/GECKOVIEW_MIGRATION.md) contains GeckoView migration references and notes.

TickTick may be used as a supplementary personal task tracker; it does not replace GitHub Issues or the repository backlog.

## GeckoView references

- [Mozilla GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [GeckoView quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [GeckoView architecture](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [WebExtensions in GeckoView](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [GeckoView site permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)

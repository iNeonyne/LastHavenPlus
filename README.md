# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It opens the game in the embedded Cefrium browser; it does not recreate or develop the game.

- **Game:** [alt.lasthavens.com/play](https://alt.lasthavens.com/play)
- **Latest release:** [v1.0.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0)
- **Project backlog:** [ISSUES.md](ISSUES.md)
- **Current project status:** [STATUS.md](STATUS.md)

## Compatibility

| Item | Support |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 (arm64-v8a) only |
| Application ID | com.neonyne.lasthavenlauncher |
| Browser engine | Cefrium 0.21.0 |
| Chromium | 156.0.8078.96 |
| UI | Kotlin and Jetpack Compose |

The application ID and release signing identity are part of update compatibility. Do not change them for routine branding or feature work.

## Features

- Opens the LastHaven game in a persistent Cefrium browser profile.
- Provides an in-app exit menu with return, reload, settings/usage, diagnostics and update actions.
- Reports installed APK size, available Cefrium profile size and app cache size as separate measurements.
- Reports app-wide network bytes received and sent. These counters include game and updater traffic, not game traffic alone.
- Keeps bounded diagnostic logs on-device, redacts common sensitive fields and shares logs only when requested.
- Checks for stable releases and offers a manual choice between a smaller differential update (when compatible) and the full APK. Downloads and installation are user-initiated; updates are never installed automatically.
- Includes a local ZIP addon library for importing, inspecting, organizing, exporting and removing addon packages. **The current manager does not inject addons into the browser game.** Game integration is tracked separately in [issue #8](https://github.com/iNeonyne/LastHavenPlus/issues/8).

## Download and update

Get the signed ARM64 APK from [GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases).

The launcher checks for a newer stable release on startup and also provides a manual check. It does not download or install updates without user action. A full APK is published as the fallback; a differential patch is offered only when the installed base version and digest match.

Install the APK through Android and approve the package installer prompt. Keep the same application installed when updating so its browser profile and game data remain available.

## Build from source

### Requirements

- JDK 25
- Gradle 9.7.1
- Android SDK Platform 37 and Build Tools 37.0.0
- At least 4 GB of Gradle heap for Cefrium dexing

Cefrium 0.21.0 is pinned across the SDK, Compose integration and Gradle plugin. Its Maven package is resolved from the Codeberg registry.

### Build and test

The Gradle Wrapper is not committed yet. Install the required Gradle version, then run from the repository root:

```bash
gradle assembleRelease
gradle testDebugUnitTest
```

Release signing requires the established project keystore and credentials. GitHub Actions restores these from repository secrets; the private keystore must not be committed. For local unsigned development builds, use the appropriate debug build task when needed.

The CI workflow builds the signed, optimized ARM64 release APK and runs the unit test task. See [Actions](https://github.com/iNeonyne/LastHavenPlus/actions) for the latest run. Build reproducibility and wrapper setup are tracked in [issue #4](https://github.com/iNeonyne/LastHavenPlus/issues/4).

### Official Cefrium references

- [Quickstart](https://cefrium.com/quickstart/)
- [Releases](https://cefrium.com/releases/)
- [API documentation](https://cefrium.com/docs/)

## Data and privacy

- Browser cookies, LocalStorage, IndexedDB, service workers and other profile data are stored in Cefrium's persistent app-private profile.
- Game data is not cleared during startup, update or recovery.
- Network counters and diagnostic logs stay on-device. Logs are shared only through the user's explicit action.
- Do not manually delete Cefrium profile files or change the app's signing identity when updating.

## Project workflow

Read [AGENTS.md](AGENTS.md) before making changes.

- [ISSUES.md](ISSUES.md) is the consolidated, version-controlled project backlog. Update it as work progresses.
- [GitHub Issues](https://github.com/iNeonyne/LastHavenPlus/issues) remain available for discussion and issue history.
- [STATUS.md](STATUS.md) records current implementation state, verification and blockers.
- [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md) records meaningful implementation changes.
- Work is integrated directly into main by the main orchestrator; this repository does not use pull requests.
- TickTick is not used for this project.

Keep documentation aligned with verified code and CI results. Mark device-only behavior as unverified until tested on physical ARM64 hardware.

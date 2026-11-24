# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It opens the existing game; it does not recreate, modify or develop the game client.

- **Game:** [alt.lasthavens.com/play](https://alt.lasthavens.com/play)
- **Latest stable release:** [v1.0.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0)
- **Project backlog:** [ISSUES.md](ISSUES.md)
- **Current project status:** [STATUS.md](STATUS.md)

## Development and releases

- `beta` is the active development, integration and testing branch.
- `main` is the stable release branch.
- Contributors may work on separate issues with disjoint file scopes, but never on the same issue concurrently.
- Contributors hand completed task-branch commits and verification evidence to Chaty. **Chaty alone reviews, integrates and pushes directly to `beta`. No pull requests are used.**
- Promote a verified beta commit to `main` only after all release gates pass and the repository owner explicitly approves. Publishing a release requires separate explicit approval.

## Compatibility

| Item | Current beta implementation / policy |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 (`arm64-v8a`) only |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Current browser engine | Cefrium 0.21.0; this is transitional and remains in beta until issue #21 is completed |
| Current Chromium | 156.0.8078.96 |
| UI | Kotlin and Jetpack Compose |

The application ID and established release signing identity are part of update compatibility. Do not change either during the engine or UI migration.

## Current beta behavior

- The current beta implementation opens the LastHaven game directly when the app starts. The new home screen is planned under [issue #22](https://github.com/iNeonyne/LastHavenPlus/issues/22); it is not implemented yet.
- The current game view uses a persistent app-private Cefrium profile. Runtime persistence on physical ARM64 hardware remains unverified.
- The app has an exit/navigation menu, settings and usage information, diagnostics and manual update actions. The legacy addon manager has been removed pending a new owner-provided specification.
- Storage information distinguishes installed APK size, browser profile directory size and app cache size.
- Network counters report app-wide bytes received and sent, including game and updater traffic; they are not game-only counters.
- Diagnostic logs are bounded and stored locally, with redaction and explicit user-initiated sharing.
- The updater checks stable releases and offers explicit manual download/install choices. It supports a compatible differential patch and a full APK fallback. It does not automatically download or install updates.
- **Addon manager:** no addon manager implementation is currently included in beta. The previous ZIP manager has been removed at the owner's request. Do not rebuild or add addon functionality until the owner provides a new specification.

## Approved architecture change

The next major beta change replaces Cefrium with **Mozilla GeckoView** and introduces a simple home screen:

- GeckoView becomes the sole embedded browser engine; no Android System WebView or Cefrium fallback is planned. The current source remains Cefrium-based until [issue #21](https://github.com/iNeonyne/LastHavenPlus/issues/21) is implemented and verified.
- The app opens to a home screen with a prominent **Play** button and a **Settings** entry. The game is loaded only after Play is selected ([issue #22](https://github.com/iNeonyne/LastHavenPlus/issues/22)).
- The future addon manager will be designed from a new owner-provided specification ([issue #23](https://github.com/iNeonyne/LastHavenPlus/issues/23)); do not reuse or recreate the deleted implementation.
- A WoW addon ZIP / `.toc` package is not a Gecko WebExtension. Moving to GeckoView does not itself provide game addon loading. Any future game integration requires a separately documented, operator-approved contract and physical-device verification.

See [issue #18](https://github.com/iNeonyne/LastHavenPlus/issues/18) for the staged plan and [issue #20](https://github.com/iNeonyne/LastHavenPlus/issues/20) for the GeckoView research/decision record. Agent-ready GeckoView references are maintained in [`docs/GECKOVIEW_MIGRATION.md`](docs/GECKOVIEW_MIGRATION.md); the append-only interruption/restart journal is [`WORK_LOG.md`](WORK_LOG.md).

### Browser profile transition

Cefrium and GeckoView must be treated as different browser engines with separate profile implementations unless a supported, verified migration method is demonstrated. Do not copy Gecko/Cefrium profile files between engines, clear the existing Cefrium profile, or promise that cookies and game login sessions will transfer. Keep legacy profile data untouched through the migration; a fresh login may be required.

## Download and update

Get the signed ARM64 APK from [GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases).

The updater checks for a newer stable release on startup and also provides a manual check. It does not download or install updates without user action. A full APK is the fallback; a differential patch is offered only when the installed base version and digest match.

Install the APK through Android and approve the package installer prompt. Preserve the same application ID and established signing identity when updating.

## Build from source

### Current beta requirements

- JDK 25
- Gradle 9.7.1 (the committed Wrapper is pinned with a distribution SHA-256)
- Android SDK Platform 37 and Build Tools 37.0.0
- At least 4 GB of Gradle heap for the current Cefrium dexing workload

The engine migration issue must validate GeckoView's documented Java 17 compile compatibility and its impact on the existing Android Gradle Plugin/JDK setup before changing build configuration. Do not change the Gradle, AGP, JDK, Android SDK, signing configuration or application ID speculatively.

### Build and test

The repository includes the Gradle Wrapper pinned to Gradle 9.7.1. From the repository root, run:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

Release signing requires the established project keystore and credentials. GitHub Actions restores these from repository secrets; the private keystore must not be committed. Do not ship debug APKs as release assets.

See [Actions](https://github.com/iNeonyne/LastHavenPlus/actions) for current CI evidence. The latest inspected beta run is failing Kotlin compilation; see [STATUS.md](STATUS.md) and [issue #4](https://github.com/iNeonyne/LastHavenPlus/issues/4).

### Official GeckoView references

- [Mozilla GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [GeckoView quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [GeckoView architecture](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [WebExtensions in GeckoView](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [Site permissions in GeckoView](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)

The exact stable GeckoView artifact/version is intentionally not pinned in this documentation. Issue #20 must verify the available stable version from Mozilla's Maven repository and validate API/toolchain compatibility before issue #21 changes dependencies.

## Data and privacy

- The current beta stores browser data in Cefrium's persistent app-private profile. The new GeckoView profile must also be persistent and must be verified over force-stop/restart.
- Do not assume existing Cefrium cookies, LocalStorage, IndexedDB, service workers or game sessions can be converted to GeckoView format.
- Do not delete or reset legacy Cefrium profile files as part of the engine transition.
- Network counters and diagnostic logs stay on-device. Logs are shared only through the user's explicit action.
- Do not add analytics, tracking, automatic update downloads or automatic installation.

## Project workflow

Read [AGENTS.md](AGENTS.md) and [AGENT_WORK.md](AGENT_WORK.md) before making changes.

- [ISSUES.md](ISSUES.md) is the version-controlled project backlog; GitHub Issues and this index are the source of truth.
- [STATUS.md](STATUS.md) records verified implementation state, unverified behavior, CI evidence and blockers.
- [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md) records meaningful project changes.
- Chaty is the sole integration and release coordinator. All accepted work is integrated directly into `beta`.
- `main` remains stable. Promote a verified `beta` commit only after release gates pass and the user explicitly approves.

Keep documentation aligned with inspected code and CI evidence. Mark physical-device behavior unverified until actually tested on ARM64 hardware.
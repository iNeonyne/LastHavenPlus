# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Phase:** Restore the current beta build, then execute the approved GeckoView and home-screen transition.

## Snapshot

The stable v1.0.0 release is published. The checked-in beta still uses Cefrium and opens the game directly on startup. The owner has approved a major architecture change: replace Cefrium with Mozilla GeckoView, add a simple home screen with Play and Settings, and reuse the existing ZIP addon manager from Settings. These are planned tasks, not implemented features.

The latest inspected source baseline is not buildable: GitHub Actions run #247 failed Kotlin compilation in `AddonManagerDialog.kt`. Later pushes in this preparation changed documentation and issue tracking only; no application or build source was changed. Unit tests did not run, no test report was produced, and signed APK/signing/ABI checks were skipped. Restore a clean beta baseline before changing the engine.

- **`main` (stable):** `6211cf389b8d9f6ef98aa7818f47643bfdaf2053`
- **Latest source baseline audited:** `c00740a77e54ff4e26d69512959fb8feb0487a51`.
- **Documentation/roadmap preparation committed to `beta` through:** `2d9c71fc09dd18dc9debf803195c6ad5e3b064f0` (README, AGENTS, AGENT_WORK, STATUS, ISSUES and CHANGELOG-only changes; no app/build source modified).
- **Latest source-build result inspected:** [#247](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45493653394), run ID `45493653394`, failed on source baseline `c00740a77e54ff4e26d69512959fb8feb0487a51`.
- **Documentation-head CI:** [#253](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45495673227) was queued on the documentation-only head `2d9c71fc09dd18dc9debf803195c6ad5e3b064f0` at the time of this status update; it is not recorded as a successful build.
- **Latest successful beta CI run found:** [#179](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45183038304), commit `593fec6b048790cbc30a7fbadf571b749069abea`; historical and not evidence for current beta.
- **Published release:** [v1.0.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0), with `lasthavenplus-arm64.apk` and `lasthavenplus-update.json`. This does not verify the new beta architecture.

## Current implementation inspected

| Area | Current state | Verification boundary |
|---|---|---|
| Application identity | `com.neonyne.lasthavenlauncher` configured | Preserve unchanged; current beta artifact signing verification skipped in run #247 |
| Release signing | CI restores the established keystore; expected certificate SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263` | Run #247 failed before APK signing verification |
| CPU architecture | `abiFilters += "arm64-v8a"` configured | Actual APK ABI verification skipped in run #247 |
| Build wrapper | Gradle Wrapper present, pinned to 9.7.1 with SHA-256 | Wrapper setup/validation step passed in run #247; no replacement wrapper or toolchain change needed |
| Browser engine | Cefrium SDK/Compose/plugin 0.21.0 currently configured; Chromium `156.0.8078.96` | Runtime behavior on physical hardware unverified; this engine is scheduled for removal under #21 |
| Game launch | `MainActivity` directly creates the Cefrium Compose view and loads the configured LastHaven URL on app launch | Source inspected; no physical ARM64 gameplay evidence |
| App shell | Current app is game-first and applies immersive mode/keep-screen-on from Activity startup; manifest declares `sensorLandscape` | Home screen and gameplay-only window/orientation behavior are not implemented |
| Browser profile | Current Cefrium default persistent app-private profile is used; source requests `navigator.storage.persist()` | Cookies/LocalStorage/IndexedDB/service-worker persistence across force-stop/restart is unverified on device |
| Exit/navigation | Back opens the current compact exit menu; Return/Reload/Settings/Diagnostics/Addon manager/update/Exit paths exist | Source inspected; physical navigation and TalkBack checks unverified |
| Settings/usage | APK size, Cefrium profile directory size and app/code cache size displayed separately | Source inspected; physical-device values and GeckoView profile measurement not yet verified |
| Network usage | Android per-UID RX/TX counters and app-session baseline | Includes game and updater traffic; physical-device readings unverified |
| Diagnostics | Bounded local rotating log, viewer/share/clear and redaction logic exist | Unit tests have not run on current beta; physical logging/redaction behavior unverified |
| Updater | Stable startup/manual checks, explicit download choices, digest/package/version/signature validation and Android installer handoff exist in source | CI and runtime verification pending; no automatic download/install is intended |
| Differential updater | Patch eligibility checks installed base/version/digest; reconstructs/verifies APK and has full APK fallback | Current-beta tests and physical compatible-update flow unverified |
| ZIP addon manager | `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt` and `AddonManagerDialog.kt` exist; imports/inspects/stores/exports/removes WoW addon packages with local enabled flags | Local package library only; no game injection/loading; archive/repository tests not verified by current CI |

## Latest CI failure — run #247

- Wrapper setup/validation, JDK 25, Android SDK 37, version metadata and signing-key restore passed.
- Kotlin compilation failed with the same two source errors:
  - `AddonManagerDialog.kt:166:77` — unresolved reference `contentDescription`.
  - `AddonManagerDialog.kt:171:45` — Material3 `Switch` has no parameter named `contentDescription`.
- Unit-test task failed before tests could run; the report upload step found no XML results.
- Signing-certificate verification, ARM64 ABI verification, APK upload and differential package steps were skipped.
- Job ID: `138798442495`.
- Ploy retains exclusive ownership of #13 until she hands off a task-branch commit. Chaty must not edit #13's files concurrently.

## GeckoView decision and risks

- **Decision:** replace Cefrium with Mozilla GeckoView as the one embedded engine; do not add System WebView or keep a Cefrium fallback.
- Mozilla documents a process-scoped `GeckoRuntime`, per-site `GeckoSession` and Android `GeckoView`; app delegates handle navigation/content events, permissions, prompts and media. See the official [quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html), [architecture](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html), [WebExtensions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html) and [permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html) docs.
- The official quick start documents Java 17 compile compatibility and Mozilla's Maven repository. Current project CI uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and SDK 37. Do not change any toolchain version until the #20 compatibility review validates the impact.
- The exact stable GeckoView artifact/version is not yet pinned. #20 must verify current official Mozilla Maven availability at implementation time; do not pin Beta/Nightly or rely solely on a third-party listing.
- GeckoView has its own persistent profile/session lifecycle. No transparent Cefrium-to-Gecko profile migration has been established. Keep old profile files untouched and warn that the user may need to sign in again.
- WoW addon ZIPs and `.toc` packages are not Gecko WebExtensions. GeckoView's WebExtension APIs do not, by themselves, provide game addon loading.
- ARM64 packaging, native library size, signed APK size, memory use, WebGL/WebAssembly, WebSockets, touch, fullscreen, audio, browser storage, permissions and crash recovery all need validation on the new engine.

## Planned work — GitHub issues

| Issue | Internal owner | Order / status |
|---|---|---|
| [#13](https://github.com/iNeonyne/LastHavenPlus/issues/13) | Ploy | Active — fix compile/accessibility blocker |
| [#4](https://github.com/iNeonyne/LastHavenPlus/issues/4) | Chaty | Active — clean build and updater tests; #13 currently blocks CI |
| [#11](https://github.com/iNeonyne/LastHavenPlus/issues/11) | Ploy | Queued after #13 |
| [#8](https://github.com/iNeonyne/LastHavenPlus/issues/8) | Ploy | Queued after #11; repository tests only |
| [#20](https://github.com/iNeonyne/LastHavenPlus/issues/20) | Ploy | Queued after #8; docs-only GeckoView technical decision record |
| [#21](https://github.com/iNeonyne/LastHavenPlus/issues/21) | Chaty | Queued after clean #4 baseline and reviewed #20; replace Cefrium |
| [#22](https://github.com/iNeonyne/LastHavenPlus/issues/22) | Ploy | Queued after #21 integration; home screen / Play / Settings navigation |
| [#23](https://github.com/iNeonyne/LastHavenPlus/issues/23) | Ploy | Queued after #22; place existing addon manager under Settings |
| [#19](https://github.com/iNeonyne/LastHavenPlus/issues/19) | Chaty | Deferred until engine and home/navigation migration is stable |

Ploy is an internal repository agent and is not assigned through GitHub's account assignee field; this file is the authoritative internal work order. One active owner per issue/task remains mandatory.

## Release blockers

1. Fix #13 and obtain a clean current-beta CI run; unit tests must actually execute and report.
2. Complete GeckoView research #20 and implementation #21 without speculative changes to Gradle/AGP/JDK/SDK, application ID, signing identity or ABI target.
3. Verify persistent Gecko profile behavior and decide how to preserve legacy Cefrium profile data; do not delete old data as part of migration.
4. Complete the Play/Settings home flow and integrate the existing addon manager under Settings without resetting its local package store.
5. Pass unit tests, release signing-certificate verification, ARM64 ABI verification and one-canonical-APK artifact checks on the exact candidate commit.
6. Perform physical ARM64 gameplay verification: WebGL/WebAssembly, WebSockets, touch, fullscreen, audio, login, permissions, lifecycle, profile persistence and same-key update installation.
7. Keep addon injection out of the release scope unless the exact operator-approved contract is separately documented and physical-device verified.

## Branch and integration workflow

- `beta` is the active development, integration and testing branch.
- `main` is the stable release branch.
- Contributors work on separate task branches and never push to `beta` or `main`.
- Chaty alone reviews, integrates and pushes accepted work directly into `beta`. No pull requests.
- Promote beta to main only after all release gates pass and the user explicitly approves.
- Do not create tags, publish releases, or change the application ID/signing identity during routine development.
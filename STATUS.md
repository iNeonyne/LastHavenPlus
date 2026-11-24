# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Phase:** Restore the current beta build, then execute the approved GeckoView and home-screen transition.

## Snapshot

The stable v1.0.0 release is published. The checked-in beta still uses Cefrium and opens the game directly on startup. The owner has approved replacing Cefrium with Mozilla GeckoView and adding a home screen with Play and Settings. The legacy addon manager has been removed; a replacement is blocked until the owner supplies a new specification.

The previous source baseline failed compilation in the now-deleted `AddonManagerDialog.kt`. The manager and its app entry points have since been removed. A new beta CI run #268 is pending on the current source after the removal; do not treat the old compile error as resolved until that run completes. Unit tests and signed APK/ABI verification remain unverified.

- **`main` (stable):** `6211cf389b8d9f6ef98aa7818f47643bfdaf2053`
- **Latest source baseline audited:** `c00740a77e54ff4e26d69512959fb8feb0487a51`.
- **Documentation/roadmap preparation commits:** `afc84aeb60ba7e3c57cce1b6c509f730f322b8fe` (README), `acc70a3163701502ce4017d14e76cca05993934c` (AGENTS), `92352c49a97bf92f7dc970a76816be670ecdd508` (AGENT_WORK), `262e134e6547339034083765fd69f736cc0e6cdb` (STATUS), `ffafa6d8af80a36b75efe1a09e29d7e45c2f510c` and `688db4d17bfceeba42f4990faac187cf76b329a9` (ISSUES), and `2d9c71fc09dd18dc9debf803195c6ad5e3b064f0` (CHANGELOG). These are documentation/index changes only; no app/build source changed.
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
| Exit/navigation | Back opens the current compact exit menu; Return/Reload/Settings/Diagnostics/update/Exit paths exist | Addon manager entry removed; physical navigation and TalkBack checks unverified |
| Settings/usage | APK size, Cefrium profile directory size and app/code cache size displayed separately | Source inspected; physical-device values and GeckoView profile measurement not yet verified |
| Network usage | Android per-UID RX/TX counters and app-session baseline | Includes game and updater traffic; physical-device readings unverified |
| Diagnostics | Bounded local rotating log, viewer/share/clear and redaction logic exist | Unit tests have not run on current beta; physical logging/redaction behavior unverified |
| Updater | Stable startup/manual checks, explicit download choices, digest/package/version/signature validation and Android installer handoff exist in source | CI and runtime verification pending; no automatic download/install is intended |
| Differential updater | Patch eligibility checks installed base/version/digest; reconstructs/verifies APK and has full APK fallback | Current-beta tests and physical compatible-update flow unverified |
| Addon manager | Legacy ZIP addon manager source and UI removed from beta on 2026-11-25 at the owner's request | No addon manager implementation is currently present. Replacement is blocked pending the owner's new specification; do not recreate the old design. |

## Latest CI failure — run #247

- Wrapper setup/validation, JDK 25, Android SDK 37, version metadata and signing-key restore passed.
- Kotlin compilation failed with the same two source errors:
  - The reported errors were in `AddonManagerDialog.kt`, which has now been removed. The next CI run must establish whether any further compilation errors remain.
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
| [#9](https://github.com/iNeonyne/LastHavenPlus/issues/9) | Ploy | Active — small exit-menu accessibility follow-up |
| [#4](https://github.com/iNeonyne/LastHavenPlus/issues/4) | Chaty | Active — clean build and updater tests; #13 currently blocks CI |
| [#11](https://github.com/iNeonyne/LastHavenPlus/issues/11) | Ploy | Queued after #13 |
| [#8](https://github.com/iNeonyne/LastHavenPlus/issues/8) | — | Superseded; old manager removed, future spec tracked by #23 |
| [#20](https://github.com/iNeonyne/LastHavenPlus/issues/20) | Ploy | Queued after #8; docs-only GeckoView technical decision record |
| [#21](https://github.com/iNeonyne/LastHavenPlus/issues/21) | Chaty | Queued after clean #4 baseline and reviewed #20; replace Cefrium |
| [#22](https://github.com/iNeonyne/LastHavenPlus/issues/22) | Ploy | Queued after #21 integration; home screen / Play / Settings navigation |
| [#23](https://github.com/iNeonyne/LastHavenPlus/issues/23) | Unassigned | Blocked pending owner addon-manager specification |
| [#19](https://github.com/iNeonyne/LastHavenPlus/issues/19) | Chaty | Deferred until engine and home/navigation migration is stable |

Ploy is an internal repository agent and is not assigned through GitHub's account assignee field; this file is the authoritative internal work order. One active owner per issue/task remains mandatory.

## Release blockers

1. Fix #13 and obtain a clean current-beta CI run; unit tests must actually execute and report.
2. Complete GeckoView research #20 and implementation #21 without speculative changes to Gradle/AGP/JDK/SDK, application ID, signing identity or ABI target.
3. Verify persistent Gecko profile behavior and decide how to preserve legacy Cefrium profile data; do not delete old data as part of migration.
4. Complete the Play/Settings home flow. Addon manager rebuild remains blocked until the owner supplies a new specification.
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

## Addon manager removal (2026-11-25)

At the owner's request, removed `AddonArchiveInspector.kt`, `AddonLibraryRepository.kt`, `AddonManagerDialog.kt`, `AddonArchiveInspectorTest.kt`, and the Addon manager entry/state from `MainActivity.kt`. No user app-private data was targeted or deleted by these repository commits. The replacement design is intentionally unspecified and must not be recreated until the owner provides instructions. Issue #23 now tracks the future rebuild requirement.


## Work log and agent references

- `WORK_LOG.md` is the append-only restart journal. Agents must record scope, starting commit, inspected/changed files, commands, results, commit SHA, limitations and next safe action at every meaningful checkpoint.
- `docs/GECKOVIEW_MIGRATION.md` contains the official Mozilla reference links and project-specific migration notes. It is a curated guide, not a vendored copy of Mozilla documentation.
- Current post-removal CI: run #268, ID `45496604607`, on commit `810cecdec2c2d795acc81331272f0751efa3b579`, pending at last inspection.

# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Phase:** Restore the current beta build, then execute the approved GeckoView and home-screen transition.

## Snapshot

The stable v1.0.0 release is published. Beta still uses Cefrium and opens the game directly on startup. The approved target is GeckoView plus a home screen with Play and Settings. The previous addon manager has been removed. The owner supplied a Firefox WebExtension specification, then explicitly placed addon-manager work on hold. No extension implementation has started; do not schedule or assign it until the owner resumes it.

- **Latest verified `beta` HEAD before this hold update:** `5f3fbe619aed796b80979919bbe9a710d6ef1370`.

- **`main` (stable):** `6211cf389b8d9f6ef98aa7818f47643bfdaf2053`.
- **Latest CI noted for the URL change:** [run #295](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45498271033), ID `45498271033`, on commit `bf5dec75e2933b1181917e73dbba4489485965e4`; pending at last inspection. This documentation-only hold update has no CI result yet.
- **Build evidence:** no successful build or test is claimed for the current documentation-only head.
- **Previous source failure:** [run #247](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45493653394) failed Kotlin compilation in `AddonManagerDialog.kt`. That source file and its app entry point have since been removed. Do not treat the blocker as fixed until a new build completes.
- **Latest successful beta CI run found:** [#179](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45183038304), commit `593fec6b048790cbc30a7fbadf571b749069abea`; historical only.
- **Published release:** [v1.0.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0), with the signed ARM64 APK and update manifest. It does not verify current beta.
- **Current task ownership:** Ploy #9 (easy menu accessibility), then #11 diagnostics tests; Chaty #4 build/updater tests and #21 engine migration after #20 research. Addon manager #23 is deferred by explicit owner request; no implementation is authorized until resumed.

Documentation, agent references and the removal work are committed directly to `beta`. No GeckoView production dependency/source has been added. No main promotion, tag or release was performed.

## Current implementation inspected

| Area | Current state | Verification boundary |
|---|---|---|
| Application identity | `com.neonyne.lasthavenlauncher` configured | Preserve unchanged; current beta artifact signing verification skipped in run #247 |
| Release signing | CI restores the established keystore; expected certificate SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263` | Run #247 failed before APK signing verification; post-removal run #278 is pending |
| CPU architecture | `abiFilters += "arm64-v8a"` configured | Actual APK ABI verification skipped in run #247; current run #278 pending |
| Build wrapper | Gradle Wrapper present, pinned to 9.7.1 with SHA-256 | Wrapper setup/validation step passed in run #247; no replacement wrapper or toolchain change needed |
| Browser engine | Cefrium SDK/Compose/plugin 0.21.0 currently configured; Chromium `156.0.8078.96` | Runtime behavior on physical hardware unverified; this engine is scheduled for removal under #21 |
| Game launch | `MainActivity` directly creates the Cefrium Compose view and loads `https://lasthavens.com/play` on app launch | Source inspected; no physical ARM64 gameplay evidence |
| App shell | Current app is game-first and applies immersive mode/keep-screen-on from Activity startup; manifest declares `sensorLandscape` | Home screen and gameplay-only window/orientation behavior are not implemented |
| Browser profile | Current Cefrium default persistent app-private profile is used; source requests `navigator.storage.persist()` | Cookies/LocalStorage/IndexedDB/service-worker persistence across force-stop/restart is unverified on device |
| Exit/navigation | Back opens the current compact exit menu; Return/Reload/Settings/Diagnostics/update/Exit paths exist | Addon manager entry removed; physical navigation and TalkBack checks unverified |
| Settings/usage | APK size, Cefrium profile directory size and app/code cache size displayed separately | Source inspected; physical-device values and GeckoView profile measurement not yet verified |
| Network usage | Android per-UID RX/TX counters and app-session baseline | Includes game and updater traffic; physical-device readings unverified |
| Diagnostics | Bounded local rotating log, viewer/share/clear and redaction logic exist | Unit tests have not run on current beta; physical logging/redaction behavior unverified |
| Updater | Stable startup/manual checks, explicit download choices, digest/package/version/signature validation and Android installer handoff exist in source | CI and runtime verification pending; no automatic download/install is intended |
| Differential updater | Patch eligibility checks installed base/version/digest; reconstructs/verifies APK and has full APK fallback | Current-beta tests and physical compatible-update flow unverified |
| Addon manager | Legacy ZIP addon manager source and UI removed from beta on 2026-11-25 at the owner's request | No addon manager implementation is currently present. Replacement is blocked pending the owner's new specification; do not recreate the old design. |

## Previous CI failure before addon manager removal — run #247

- Wrapper setup/validation, JDK 25, Android SDK 37, version metadata and signing-key restore passed.
- Kotlin compilation failed with the same two source errors:
  - The reported errors were in `AddonManagerDialog.kt`, which has now been removed. The next CI run must establish whether any further compilation errors remain.
- Unit-test task failed before tests could run; the report upload step found no XML results.
- Signing-certificate verification, ARM64 ABI verification, APK upload and differential package steps were skipped.
- Job ID: `138798442495`.
- The previous #13 compile errors belonged to the deleted addon manager. Ploy's active task is now #9 menu accessibility; Chaty must not edit her menu files concurrently.

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
| [#4](https://github.com/iNeonyne/LastHavenPlus/issues/4) | Chaty | Active — verify post-removal build and updater tests |
| [#11](https://github.com/iNeonyne/LastHavenPlus/issues/11) | Ploy | Queued after #9 |
| [#8](https://github.com/iNeonyne/LastHavenPlus/issues/8) | — | Superseded; old manager removed, future spec tracked by #23 |
| [#20](https://github.com/iNeonyne/LastHavenPlus/issues/20) | Ploy | Queued after #8; docs-only GeckoView technical decision record |
| [#21](https://github.com/iNeonyne/LastHavenPlus/issues/21) | Chaty | Queued after clean #4 baseline and reviewed #20; replace Cefrium |
| [#22](https://github.com/iNeonyne/LastHavenPlus/issues/22) | Ploy | Queued after #21 integration; home screen / Play / Settings navigation |
| [#23](https://github.com/iNeonyne/LastHavenPlus/issues/23) | Unassigned | Blocked pending owner addon-manager specification |
| [#19](https://github.com/iNeonyne/LastHavenPlus/issues/19) | Chaty | Deferred until engine and home/navigation migration is stable |

Ploy is an internal repository agent and is not assigned through GitHub's account assignee field; this file is the authoritative internal work order. One active owner per issue/task remains mandatory.

## Release blockers

1. Obtain a clean current-beta CI run after addon manager removal; unit tests must actually execute and report. Ploy's current small task is #9.
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
- Current verification checkpoint is recorded in `WORK_LOG.md`; recent post-removal CI runs were cancelled and the exact current head has no completed build evidence.

# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Phase:** P0 — GeckoView 160 build baseline is now green in CI; physical ARM64 gameplay and profile persistence remain unverified. Ploy is working on the home-screen task #22.

## Snapshot

The stable v1.0.0 release remains on `main`. Beta uses Mozilla GeckoView 160 and still launches the game directly; the app home screen and Reload / Home / Return to game navigation are in progress under issue #22. The previous addon manager has been removed, and the replacement addon-manager work remains on hold at the owner's request.

- **Current beta HEAD:** `7f90d55ca8927bf754948ad887b3f85358102265` (CI changelog update; latest workflow code commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`).
- **Latest workflow change:** `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21` (Android CI optimization plus a 30-minute build-job timeout).
- **Stable main:** `6211cf389b8d9f6ef98aa7818f47643bfdaf2053`.
- **Latest successful CI:** [run #308](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45503025246), commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`. The Android release build and unit-test task passed; the produced APK was 92 MB, contained only `arm64-v8a`, and matched the established release signing certificate.
- **CI optimization verification:** commit `54b947279a783c6c382210e65045edd02bd5ee73` changes workflow triggers, checkout depth and Gradle daemon behavior. Its resulting Actions run has not yet been inspected, so the optimization is committed but not yet validated or benchmarked.
- **Physical-device verification:** GeckoView gameplay, session/profile persistence, app navigation, storage/network measurements, updater installation and other runtime behavior remain unverified on physical ARM64 hardware.
- **Release state:** no tag, release publication or promotion to `main` was performed as part of this work.

Ploy owns the home-screen task #22. Chaty owns CI optimization, integration and release coordination. Keep changes to separate files/tasks; do not edit Ploy's home-screen implementation concurrently.

## Current implementation inspected

| Area | Current state | Verification boundary |
|---|---|---|
| Application identity | `com.neonyne.lasthavenlauncher` configured | Preserve unchanged; current beta artifact signing verification skipped in run #247 |
| Release signing | CI restores the established keystore; expected certificate SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263` | Run #247 failed before APK signing verification; post-removal run #278 is pending |
| CPU architecture | `abiFilters += "arm64-v8a"` configured | Actual APK ABI verification skipped in run #247; current run #278 pending |
| Build wrapper | Gradle Wrapper present, pinned to 9.7.1 with SHA-256 | Wrapper setup/validation step passed in run #247; no replacement wrapper or toolchain change needed |
| Browser engine | Mozilla ARM64 GeckoView `160.0.20261116130455` is configured; Cefrium references removed from production source/build | Run #302 failed AAR metadata validation because compile SDK 37.2 was required; API 37.2 configuration is pending run #304 |
| Game launch | `MainActivity` directly creates a GeckoSession/GeckoView and loads `https://lasthavens.com/play` on app launch | Source inspected; CI build and physical ARM64 gameplay remain unverified |
| App shell | Current app is game-first and applies immersive mode/keep-screen-on from Activity startup; manifest declares `sensorLandscape` | Home screen and gameplay-only window/orientation behavior are not implemented |
| Browser profile | GeckoView runtime/session uses app-private Gecko data; no Cefrium profile copying is implemented | Cookies/LocalStorage/IndexedDB/service-worker persistence across force-stop/restart is unverified on device |
| Exit/navigation | Back opens the current compact exit menu; Return/Reload/Settings/Diagnostics/update/Exit paths exist | Addon manager entry removed; physical navigation and TalkBack checks unverified |
| Settings/usage | APK size, app-private persistent data size and app cache size displayed separately | Source inspected; physical-device values and exact Gecko profile-only measurement are unverified |
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
- The official quick start documents Java 17 compile compatibility and Mozilla's Maven repository. Current project CI uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and SDK 37.2 minor API level. Run #302 proved GeckoView 160's AAR requires compile SDK 37.2; the project now configures this via `compileSdkMinor = 2`, with run #304 pending.
- The ARM64 GeckoView artifact `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455` is pinned in `app/build.gradle.kts`; Mozilla Firefox 160 public API was inspected. CI run #302 failed before compilation due AAR metadata requiring API 37.2. CI run #304 is pending after the matching Gradle/SDK adjustment. Dependency resolution, final APK ABI/signing/size, tests and runtime behavior remain unverified.
- GeckoView has its own persistent profile/session lifecycle. No transparent Cefrium-to-Gecko profile migration has been established. Keep old profile files untouched and warn that the user may need to sign in again.
- WoW addon ZIPs and `.toc` packages are not Gecko WebExtensions. GeckoView's WebExtension APIs do not, by themselves, provide game addon loading.
- ARM64 packaging, native library size, signed APK size, memory use, WebGL/WebAssembly, WebSockets, touch, fullscreen, audio, browser storage, permissions and crash recovery all need validation on the new engine.

## Planned work — GitHub issues

| Issue | Internal owner | Order / status |
|---|---|---|
| [#9](https://github.com/iNeonyne/LastHavenPlus/issues/9) | Ploy | Paused — menu accessibility follow-up |
| [#4](https://github.com/iNeonyne/LastHavenPlus/issues/4) | Chaty | Active — verify post-removal build and updater tests |
| [#11](https://github.com/iNeonyne/LastHavenPlus/issues/11) | Ploy | Paused — diagnostics tests |
| [#8](https://github.com/iNeonyne/LastHavenPlus/issues/8) | — | Superseded; old manager removed, future spec tracked by #23 |
| [#20](https://github.com/iNeonyne/LastHavenPlus/issues/20) | Chaty | Research recorded; candidate/API decision implemented, final compatibility verification remains under #21 |
| [#21](https://github.com/iNeonyne/LastHavenPlus/issues/21) | Chaty | ACTIVE — P0 verify GeckoView 160 build; run #302 compileSdk blocker addressed in pending run #304 |
| [#22](https://github.com/iNeonyne/LastHavenPlus/issues/22) | Ploy | Queued after #21 integration; home screen / Play / Settings navigation |
| [#23](https://github.com/iNeonyne/LastHavenPlus/issues/23) | Unassigned | Blocked pending owner addon-manager specification |
| [#19](https://github.com/iNeonyne/LastHavenPlus/issues/19) | Chaty | Deferred until engine and home/navigation migration is stable |

Ploy is an internal repository agent and is not assigned through GitHub's account assignee field; this file is the authoritative internal work order. Chaty currently owns the GeckoView workstream. Ploy's unrelated tasks are paused to avoid delaying the top priority. One active owner per issue/task remains mandatory.

## Release blockers

1. Complete the GeckoView 160 build/test gate: verify the API 37.2 SDK package installs in CI, then inspect Kotlin compilation, tests, signature, ABI and artifacts.
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

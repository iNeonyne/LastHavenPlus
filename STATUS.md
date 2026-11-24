# Project status — LastHavenPlus

**Last updated:** 2026-11-24  
**Phase:** P0 — Playable core verification and release hardening

## Snapshot

v1.0.0 was published on 2026-11-24. Physical-device gameplay, persistence and same-key update verification remains outstanding as follow-up work; do not claim those checks passed until completed on real arm64 hardware.

| Area | State | Evidence / notes |
|---|---|---|
| Repository | Ready | Greenfield project files committed to main |
| Application ID | Set | `com.neonyne.lasthavenlauncher`; do not change |
| Android scaffold | Created | Kotlin + Jetpack Compose, compileSdk 37, minSdk 29 |
| Build toolchain | Configured | JDK 25, Gradle 9.7.1, AGP 9.4.0 |
| Cefrium dependency | Pinned | SDK / Compose / plugin 0.21.0 |
| Chromium revision | Documented | 156.0.8078.96, CEF branch 8078 |
| Maven registry | Configured | Codeberg mirror `https://codeberg.org/api/packages/cefrium/maven` |
| ABI | Verified in CI | Previous CI APK contains arm64-v8a and no x86, x86_64 or armeabi-v7a libraries; new debug/release checks pending |
| Immersive host | Implemented, unverified | Fullscreen flags, landscape sensor orientation, screen stays awake |
| Loading / retry | Implemented, unverified | Compose loading state, main-frame load error and renderer termination retry |
| Persistent storage request | Implemented, unverified | Calls `navigator.storage.persist()` after the first completed document load |
| Settings & usage | Implemented; CI/device verification pending | Shows installed APK bytes, Cefrium profile directory size when present, app/code cache size and refresh action |
| Network usage | Implemented; device verification pending | Android per-UID RX/TX counters since boot and app-session baseline; includes game + updater, local-only |
| Diagnostics | Implemented; CI verification pending | Bounded rotating local log, lifecycle/Cefrium/updater events, redaction, viewer/share and redaction unit test |
| Addon manager | Local ZIP manager code committed; CI verification pending | ZIP inspector, TOC parser, local package repository, management dialog and exit-menu entry are on main. No release/device verification yet. Operator permission confirmed; exact runtime integration contract pending. |
| Exit menu | Implemented; device verification pending | Back opens menu with Return to game, Reload, Settings & usage, logs, update check and Leave |
| Browser profile | Default persistent | Cefrium default profile stores cookies, LocalStorage, IndexedDB, service workers and cache in app-private `app_cefrium`; not the Android cache directory |
| Remote debugging | Debug builds only | `Cefrium.setRemoteDebuggingEnabled` enabled from Application.onCreate before first browser creation |
| Latest CI build | Pending | The LastHavenPlus 1.0.0 re-release workflow is queued; no result is claimed until its current commit completes. |
| Debug APK baseline | Measured | 229,073,631 bytes (218.4 MiB); actual APK extracted from run #32 artifact |
| R8 / resource shrinking | Configured; validation pending | Enabled only on release variant; CI builds debug and signed release APKs |
| Device test | Not run | No connected arm64 device/emulator available to this run |
| Release signing | Configured; verification pending | Existing `lasthavenplus` key restores successfully in run #79; Run #82 printed the expected certificate fingerprint; CI parser fix and a passing verification rerun are pending |

## Agent contribution workflow

Ploy submits each completed assignment from a dedicated feature branch through a pull request. Ploy must not push directly to `main` or edit central tracking files. Chaty reviews the full diff, verifies relevant CI/tests and maintains the repository trackers. **No PR is merged until the user explicitly approves it.** Chaty may continue making separate orchestrator commits directly to `main`.

## Work tracking

[`ISSUES.md`](ISSUES.md) is now the consolidated, version-controlled backlog containing the open GitHub issues and their checklists. Update it as work progresses. GitHub Issues remain linked discussion/history records. TickTick is no longer used for this repository.

- [#4 — Build reproducibility, updater tests and CI verification](https://github.com/iNeonyne/LastHavenPlus/issues/4)
- [#5 — Physical-device gameplay, persistence and update verification](https://github.com/iNeonyne/LastHavenPlus/issues/5)
- [#6 — Minimal settings and safe storage controls](https://github.com/iNeonyne/LastHavenPlus/issues/6)
- [#7 — Performance and APK-size measurements](https://github.com/iNeonyne/LastHavenPlus/issues/7)
- [#8 — Local ZIP addon manager (game integration gated)](https://github.com/iNeonyne/LastHavenPlus/issues/8)
- [#9 — Compact navigation and exit menu](https://github.com/iNeonyne/LastHavenPlus/issues/9)
- [#10 — Network data usage](https://github.com/iNeonyne/LastHavenPlus/issues/10)
- [#11 — Privacy-conscious app and updater logs](https://github.com/iNeonyne/LastHavenPlus/issues/11)

## App updater

- Checks the latest **published stable GitHub Release** on launcher start; a manual check is also available from the exit dialog.
- Never downloads automatically. The user explicitly chooses **Download smaller update** or **Download full APK instead**, then Android presents its package installer.
- Expected release tag: `vMAJOR.MINOR.PATCH`; canonical assets: signed `lasthavenplus-arm64.apk`, `lasthavenplus-update.json`, and an optional `*.bsdiff` patch. Legacy-named APK and manifest aliases are retained for older updater clients.
- The app checks manifest and GitHub asset digests, installed base APK SHA-256, reconstructed APK SHA-256, package ID, versionCode and the pinned LastHaven release certificate before installer handoff.
- Uses an app-private temporary file and a non-exported AndroidX FileProvider; no external storage permission or background service.
- Differential patches reconstruct the complete signed APK byte-for-byte; the app does not repackage or re-sign it. Full APK remains the fallback.
- CI restores the established `lasthavenplus` signing key from `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` and `ANDROID_KEY_PASSWORD`; it verifies the expected certificate SHA-256 before continuing.
- Stable tags `vMAJOR.MINOR.PATCH` build signed ARM64 APKs, generate a manifest, and publish a BSDIFF40 patch from the immediately previous compatible release only when the patch is at least 10% smaller than the full APK.
- The full signed APK is always published as fallback. Differential downloads are offered only for the exact base version/versionCode; the app checks the installed APK hash, reconstructs the signed APK in a streaming file-based patcher, verifies the target SHA-256, package ID, versionCode and pinned signing certificate, then opens Android's installer.
- No published GitHub Releases currently exist, so the first stable release will contain the full APK only. Later releases can include a patch if the previous release and generated patch meet compatibility/size checks.

## APK size audit

- Baseline from CI run #32: APK is 229,073,631 bytes (218.4 MiB).
- Largest compressed entry: `lib/arm64-v8a/libcef.so`, 92.84 MiB (201.94 MiB uncompressed).
- DEX payload: 22 DEX files totaling 104.4 MiB (109,487,264 bytes as stored in the APK); this is the main candidate for R8 code shrinking.
- Chromium data files include `icudtl.dat` (8.85 MiB) and `resources.pak` (1.23 MiB); do not strip or replace engine assets without Cefrium-specific compatibility evidence.
- Release-only R8 minification and resource shrinking are enabled. CI now builds the signed optimized release APK and reports its exact APK size and DEX total alongside the debug APK; no optimized result is recorded until that job completes.
- CI verifies the signed APK against the established LastHaven certificate fingerprint before it can be published.
- The archive downloaded from Actions is not the APK size; use the APK's own file size for comparisons.

## Cefrium API findings

- Current release: Cefrium 0.21.0, Chromium 156.0.8078.96, CEF branch 8078.
- Official quickstart requires JDK 25, Gradle 9.7.1, AGP 9.4, compileSdk 37, minSdk 29 and a 4 GB Gradle heap.
- `CefriumView` is the Compose API. The classic Android View API is `CefriumBrowser.createWithSurface(activity)`; this project uses `CefriumView`.
- `CefriumViewState.rendererTermination` reports renderer death; `CefriumNavigationClient.onLoadError` reports main-frame load errors.
- `CefriumProfile.getDefault()` is persistent. Its data lives under Cefrium's private `app_cefrium` directory, not Android's cache directory.
- `CefriumBrowser.clearCache()` / `CefriumNavigator.clearCache()` clears HTTP cache. No documented public code-cache-only clearing API has been confirmed yet.
- Cefrium's current guide recommends R8 and says the AAR includes consumer rules for engine-required code; the minified app still needs physical-device testing.
- Cefrium's API is pre-1.0 and may change between releases.

## Web capability verification

**No capabilities are marked passed until tested inside the app and compared with Chrome on the same device.**

| Requirement | Chrome reference | Cefrium app | Status |
|---|---|---|---|
| WebGL2 | Not run | Not run | Pending device |
| WebAssembly | Not run | Not run | Pending device |
| WASM threads / SharedArrayBuffer | Not run | Not run | Pending device; requires COOP/COEP |
| WebSockets | Not run | Not run | Pending device |
| Audio after user gesture | Not run | Not run | Pending device |
| LastHaven gameplay | Not run | Not run | Pending device |

The web fetch of the game URL returns only “Loading... Please enable JavaScript to play Lasthavens.” That does not reveal the game's actual runtime requirements. Use remote debugging (`chrome://inspect`) against the Cefrium process and Chrome on the same phone.

## Active work

| Owner | Issue / surface | State |
|---|---|---|
| Chaty | #4 — build reliability and updater test coverage | In progress. Added three malformed BSDIFF tests; CI run #45474077737 is pending. Previous run #45473637053 failed on three addon archive tests; diagnosis required. Gradle Wrapper and isolated updater-manifest/download tests remain outstanding. |

Delegated ownership and task order are defined in [AGENT_WORK.md](AGENT_WORK.md). Ploy's first assignment is #13 (accessibility semantics); #11 diagnostics tests and #8 addon repository tests are queued sequentially after review. Ploy opens a PR for completed work. Chaty reviews the PR diff, verifies it and updates the repository task tracker; merge requires explicit user approval. Chaty owns all other issues, CI, release coordination and central documentation.

| Owner | Surface | State |
|---|---|---|
| Main integrator | Issues #6 and #9 — settings, usage panel and exit menu | Initial implementation committed; CI/device verification pending; remaining settings/cache actions not implemented |
| Main integrator | Issue #10 — network usage reporting | Per-UID RX/TX counters implemented; explicitly app-wide (game + updater); device verification pending |
| Main integrator | Issue #11 — local diagnostics and export | Local rotating logs, redaction, viewer/share and unit test committed; CI verification pending |
| Ploy (assigned; awaiting handoff) | Issue #13 — accessibility semantics | First delegated task; repository-only changes on an isolated branch. Ploy opens a PR; Chaty reviews and updates tracking. |
| Ploy (queued) | Issue #11 — diagnostics test coverage | Starts only after #13 PR is reviewed and accepted. Ploy opens a PR; Chaty reviews and updates tracking. |
| Ploy (queued) | Issue #8 — addon repository tests | Starts only after #11 PR is reviewed and accepted and Chaty explicitly unblocks it; no runtime injection. Ploy opens a PR; Chaty reviews and updates tracking. |
| Chaty | Issues #4–#17 and integration | Main orchestrator; owns CI, issue tracking, review/integration and all non-delegated implementation. |
| Main integrator | Issue #8 — local ZIP addon manager | ZIP inspection/TOC parsing, app-private package library, local enable/disable state, export/remove and Compose UI committed. CI verification pending. Operator permission confirmed; exact authorized runtime scope/API still pending. |


## Blockers

| Blocker | Impact | Next action |
|---|---|---|
| No physical arm64 test device attached to this session | Cannot verify gameplay, GPU, audio or persistence | Install CI APK on target phone and run checklist |
| Differential update path not yet verified across two stable releases | The patch path needs a compatible previous release and physical-device validation | Test the next compatible release update on a physical arm64 device |
| Code cache clear API not confirmed | Cannot yet implement the requested HTTP + code-cache-only clear action | Inspect pinned SDK API; do not delete engine directories manually |

## Performance log

No runtime performance changes have been made. APK size baseline is measured; establish gameplay and runtime baselines after gameplay loads.

| Metric | Baseline | Optimized |
|---|---:|---:|
| Cold start to first paint | Not measured | — |
| FPS / frame pacing | Not measured | — |
| PSS / native memory | Not measured | — |
| APK download size | 229,073,631 bytes (218.4 MiB) | Pending CI release candidate |
| Rendering latency | Not measured | — |

## Next actions

1. Verify CI for the latest main commits and resolve any compilation/test failures.
2. Complete [#4 — build reproducibility, updater tests and CI verification](https://github.com/iNeonyne/LastHavenPlus/issues/4).
3. Complete [#5 — physical-device gameplay, persistence and update verification](https://github.com/iNeonyne/LastHavenPlus/issues/5).
4. Finish #6/#9/#10/#11 after CI and device validation; keep unverified behavior marked pending.
5. Defer [#7 — performance and APK-size measurements](https://github.com/iNeonyne/LastHavenPlus/issues/7) until a repeatable gameplay baseline exists.
6. Keep [#8 — addon manager](https://github.com/iNeonyne/LastHavenPlus/issues/8) deferred until the core launcher is stable.

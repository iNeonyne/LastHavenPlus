# Project status — LastHavenLauncher

**Last updated:** 2026-11-24  
**Phase:** P0 — Android scaffold and Cefrium integration

## Snapshot

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
| Browser profile | Default persistent | Cefrium default profile stores cookies, LocalStorage, IndexedDB, service workers and cache in app-private `app_cefrium`; not the Android cache directory |
| Remote debugging | Debug builds only | `Cefrium.setRemoteDebuggingEnabled` enabled from Application.onCreate before first browser creation |
| Previous CI build | **Passing** | Run #32, commit `0c4393c`; debug build, unit-test task, ABI verification and artifact upload succeeded |
| Debug APK baseline | Measured | 229,073,631 bytes (218.4 MiB); actual APK extracted from run #32 artifact |
| R8 / resource shrinking | Configured; validation pending | Enabled only on release variant; CI builds debug and signed release APKs |
| Device test | Not run | No connected arm64 device/emulator available to this run |
| Release signing | Configured; CI validation pending | Existing `lasthavenplus` key is restored from GitHub secrets and its SHA-256 fingerprint is pinned in CI and the updater |

## App updater

- Checks the latest **published stable GitHub Release** on launcher start; a manual check is also available from the exit dialog.
- Never downloads automatically. The user explicitly chooses **Download smaller update** or **Download full APK instead**, then Android presents its package installer.
- Expected release tag: `vMAJOR.MINOR.PATCH`; assets: signed `lasthavenlauncher-arm64.apk`, `lasthavenlauncher-update.json`, and an optional `*.bsdiff` patch.
- The app checks manifest and GitHub asset digests, installed base APK SHA-256, reconstructed APK SHA-256, package ID, versionCode and the pinned LastHaven release certificate before installer handoff.
- Uses an app-private temporary file and a non-exported AndroidX FileProvider; no external storage permission or background service.
- Differential patches reconstruct the complete signed APK byte-for-byte; the app does not repackage or re-sign it. Full APK remains the fallback.
- CI now restores the existing `lasthavenplus` signing key from `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` and `ANDROID_KEY_PASSWORD`; it verifies the expected certificate SHA-256 before continuing.
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

| Owner | Surface | State |
|---|---|---|
| Main integrator | Signed release pipeline and BSDIFF differential updater; own updater, Gradle signing config, CI workflow, tests and release docs | In progress; preserve existing release certificate and full-APK fallback |

## Blockers

| Blocker | Impact | Next action |
|---|---|---|
| No physical arm64 test device attached to this session | Cannot verify gameplay, GPU, audio or persistence | Install CI APK on target phone and run checklist |
| Signed updater path not yet CI-validated | No production release should be tagged until build/test passes | Check main CI, then create a test release tag only after the signed APK fingerprint and patch reconstruction are verified |
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

1. Install the arm64 CI APK on a physical phone and confirm LastHaven loads.
2. Run the web capability checklist in Cefrium and Chrome on the same phone.
3. Confirm persistent storage result and app update/data retention.
4. Verify the signed build and BSDIFF unit test in CI; publish the first signed release (full APK only), then test one subsequent release's delta generation and on-device installation.
4. Add minimal settings after the playable path is reliable; preserve the cache/data distinction.

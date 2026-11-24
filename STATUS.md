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
| R8 / resource shrinking | Configured; validation pending | Enabled only on release variant; CI now builds both debug and unsigned release APKs |
| Device test | Not run | No connected arm64 device/emulator available to this run |
| Release signing | Not configured | Release key must be generated/backed up before first distributed release |

## App updater

- Checks the latest **published stable GitHub Release** on launcher start; a manual check is also available from the exit dialog.
- Never downloads automatically. The user must select **Download update**, then Android presents its package installer.
- Expected release tag: `vMAJOR.MINOR.PATCH`; expected asset: `lasthavenlauncher-arm64.apk`.
- Requires GitHub's release asset `sha256:` digest; the downloaded APK is SHA-256 verified before the installer is opened.
- Uses an app-private temporary file and a non-exported AndroidX FileProvider; no external storage permission or background service.
- Android package installer enforces package identity/signature. A distributed release must use the same application ID and signing key as the installed app.
- No published GitHub Releases currently exist; updater cannot return a real update until the first signed release and matching asset are published.

## APK size audit

- Baseline from CI run #32: APK is 229,073,631 bytes (218.4 MiB).
- Largest compressed entry: `lib/arm64-v8a/libcef.so`, 92.84 MiB (201.94 MiB uncompressed).
- DEX payload: 22 DEX files totaling approximately 115 MiB; this is the main candidate for R8 code shrinking.
- Chromium data files include `icudtl.dat` (8.85 MiB) and `resources.pak` (1.23 MiB); do not strip or replace engine assets without Cefrium-specific compatibility evidence.
- Release-only R8 minification and resource shrinking are now enabled. A CI build produces an unsigned optimized release candidate and reports its exact APK size and DEX total alongside the debug APK; no optimized result is recorded until that job completes.
- The release candidate is unsigned because no release keystore is configured; it is for size analysis only and is not installable/distributable as a production update.
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
| Main integrator | APK size audit and safe R8 / resource shrinking trial; own `app/build.gradle.kts`, CI workflow, status and changelog | Implementation committed; CI validation pending |

## Blockers

| Blocker | Impact | Next action |
|---|---|---|
| No physical arm64 test device attached to this session | Cannot verify gameplay, GPU, audio or persistence | Install CI APK on target phone and run checklist |
| Release signing key not configured | No stable release APK can be shipped yet | Generate/backup key and configure CI secrets before release |
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
4. Add minimal settings after the playable path is reliable; preserve the cache/data distinction.

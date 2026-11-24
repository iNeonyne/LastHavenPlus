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
| ABI | Verified in CI | APK contains arm64-v8a and no x86, x86_64 or armeabi-v7a libraries |
| Immersive host | Implemented, unverified | Fullscreen flags, landscape sensor orientation, screen stays awake |
| Loading / retry | Implemented, unverified | Compose loading state, main-frame load error and renderer termination retry |
| Persistent storage request | Implemented, unverified | Calls `navigator.storage.persist()` after the first completed document load |
| Browser profile | Default persistent | Cefrium default profile stores cookies, LocalStorage, IndexedDB, service workers and cache in app-private `app_cefrium`; not the Android cache directory |
| Remote debugging | Debug builds only | `Cefrium.setRemoteDebuggingEnabled` enabled from Application.onCreate before first browser creation |
| CI build | **Passing** | Run #15, commit `3d4e1a0`; build, unit-test task, ABI verification and artifact upload all succeeded |
| Debug APK artifact | Available | `lasthavenlauncher-debug-arm64`, run #15; artifact expires 2026-12-08 |
| Device test | Not run | No connected arm64 device/emulator available to this run |
| Release signing | Not configured | Release key must be generated/backed up before first distributed release |

## Cefrium API findings

- Current release: Cefrium 0.21.0, Chromium 156.0.8078.96, CEF branch 8078.
- Official quickstart requires JDK 25, Gradle 9.7.1, AGP 9.4, compileSdk 37, minSdk 29 and a 4 GB Gradle heap.
- `CefriumView` is the Compose API. The classic Android View API is `CefriumBrowser.createWithSurface(activity)`; this project uses `CefriumView`.
- `CefriumViewState.rendererTermination` reports renderer death; `CefriumNavigationClient.onLoadError` reports main-frame load errors.
- `CefriumProfile.getDefault()` is persistent. Its data lives under Cefrium's private `app_cefrium` directory, not Android's cache directory.
- `CefriumBrowser.clearCache()` / `CefriumNavigator.clearCache()` clears HTTP cache. No documented public code-cache-only clearing API has been confirmed yet.
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
| Main integrator | CI verification and status reconciliation | Complete |

## Blockers

| Blocker | Impact | Next action |
|---|---|---|
| No physical arm64 test device attached to this session | Cannot verify gameplay, GPU, audio or persistence | Install CI APK on target phone and run checklist |
| Release signing key not configured | No stable release APK can be shipped yet | Generate/backup key and configure CI secrets before release |
| Code cache clear API not confirmed | Cannot yet implement the requested HTTP + code-cache-only clear action | Inspect pinned SDK API; do not delete engine directories manually |

## Performance log

No performance changes have been made. Establish baseline numbers after gameplay loads.

| Metric | Baseline | Optimized |
|---|---:|---:|
| Cold start to first paint | Not measured | — |
| FPS / frame pacing | Not measured | — |
| PSS / native memory | Not measured | — |
| APK download size | Not measured | — |
| Rendering latency | Not measured | — |

## Next actions

1. Install the arm64 CI APK on a physical phone and confirm LastHaven loads.
2. Run the web capability checklist in Cefrium and Chrome on the same phone.
3. Confirm persistent storage result and app update/data retention.
4. Add minimal settings after the playable path is reliable; preserve the cache/data distinction.

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
| ABI | Configured | arm64-v8a only |
| Game page | Not tested in app | Web URL responds with JS-required page; requires real browser execution |
| CI build | Running | CI setup correction: platform-tools explicit; Android API 37 is published as `platforms;android-37.0`, not `android-37`. Workflow updated; build pending |
| Device test | Not run | No connected arm64 device/emulator available to this run |
| Release signing | Not configured | Release key must be generated/backed up before first distributed release |

## Cefrium API findings

- Official quickstart documents `CefriumView`, `rememberCefriumViewState` and `rememberCefriumNavigator` in package `com.cefrium.compose`.
- Official docs call this the **Compose API**. The classic Android View API is `CefriumBrowser.createWithSurface(activity)`; the project uses the requested `CefriumView` API.
- Cefrium 0.21.0 is the current release and tracks Chromium 156.0.8078.96.
- Required toolchain: JDK 25, Gradle 9.7.1, AGP 9.4, compileSdk 37, minSdk 29 and 4 GB Gradle heap.
- SDK manifest contributes its multiprocess services and permissions via manifest merge; do not duplicate those service declarations.
- Cefrium API is pre-1.0 and may change between releases.

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
| Main integrator | Greenfield scaffold + CI | Initial implementation committed; CI pending |

## Blockers

| Blocker | Impact | Next action |
|---|---|---|
| No physical arm64 test device attached to this session | Cannot verify gameplay, GPU, audio or persistence | Install CI APK on target phone and run checklist |
| Release signing key not configured | No stable release APK can be shipped yet | Generate/backup key and configure CI secrets before release |
| Cefrium Compose error callback and profile/cache controls need API-level validation | Retry and clear-cache features not implemented yet | Inspect pinned 0.21.0 API docs before implementation |

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

1. Confirm the GitHub Actions debug build passes; fix any build errors.
2. Install the arm64 APK on a physical phone and confirm LastHaven loads.
3. Run the web capability checklist in Cefrium and Chrome on the same phone.
4. Implement retry, persistent storage request and lifecycle handling based on verified Cefrium APIs.
5. Add settings only after the playable path is reliable.

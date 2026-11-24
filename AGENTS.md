# LastHavenLauncher

Instructions for every human or AI agent working in this repository.

## Mission

Ship the simplest reliable Android app that opens the existing LastHaven WotLK private server in Cefrium and lets the user play.

Default URL: `https://alt.lasthavens.com/play`

Do not recreate, modify or develop the game. Do not add System WebView, GeckoView or any fallback engine. Do not add general-purpose browser features.

## Hard rules

1. **Read STATUS and the latest changelog entries before editing.**
2. **Claim ownership.** Add your role and file/work area under STATUS → Active work before editing. One owner per file or Gradle block at a time.
3. **Document every meaningful change.** Update STATUS and append to CHANGELOG-AGENTS.md, including research failures and unverified assumptions.
4. **Research Cefrium APIs first.** Never guess engine API names. Use official docs and samples. Record uncertain APIs as blockers.
5. **Game first.** Get LastHaven loading before optional settings, theming or performance work.
6. **arm64-v8a only.** Do not package x86, x86_64 or armeabi-v7a native libraries.
7. **Stable app identity.** Do not change `applicationId` (`com.neonyne.lasthavenlauncher`) or release signing key after the first distributed build.
8. **Storage safety.** Never clear cookies, IndexedDB, LocalStorage or Cache Storage except through a confirmed “Clear game data” action.
9. **No speculative optimization.** Every performance change needs before/after measurements in STATUS.
10. **No unnecessary handoffs.** Parallelize independent tasks only; one integrator owns merging and pushes to main.

## Current engine decision

- Cefrium SDK: `com.cefrium:cefrium-sdk:0.21.0`
- Cefrium Compose wrapper: `com.cefrium:cefrium-compose:0.21.0`
- Gradle plugin: `com.cefrium` version `0.21.0`
- Chromium: `156.0.8078.96` (CEF branch 8078)
- Maven registry: `https://codeberg.org/api/packages/cefrium/maven`
- UI API: `com.cefrium.compose.CefriumView`, `rememberCefriumViewState`, `rememberCefriumNavigator`

**Terminology:** Official Cefrium docs describe `CefriumView` as the Compose API. The classic Android View integration is `CefriumBrowser.createWithSurface(activity)`. Use the Compose `CefriumView` requested by this project unless an API-level blocker is found; do not silently switch rendering APIs.

Official build requirements:
- JDK 25, Gradle 9.7.1, AGP 9.4.0
- compileSdk 37, minSdk 29
- 4 GB Gradle heap
- AGP 9 built-in Kotlin support; do not apply `org.jetbrains.kotlin.android`
- Compose compiler Gradle plugin at a matching stable Kotlin version
- Legacy JNI library packaging

The Codeberg Maven registry mirrors the current and previous releases only. Keep Cefrium pinned; check the official releases page before updating it. Update SDK, Compose wrapper and Gradle plugin together.

References:
- https://cefrium.com/quickstart/
- https://cefrium.com/releases/
- https://cefrium.com/guides/adopting-cefrium/
- https://cefrium.com/docs/

## Parallel agent roles

| Role | Owns | Must not duplicate |
|---|---|---|
| Architecture | App structure, package, boundaries | Cefrium API research |
| Android | Activity, manifest, lifecycle, orientation, permissions | Engine integration internals |
| Cefrium | SDK version, Maven, view host, engine APIs | General app UI |
| QA | Build, arm64 device/emulator, web capability checks | Unmeasured performance changes |
| Performance | Profiling and measured improvements after compatibility checks | Speculative flag lists |

Agents may work in parallel only on disjoint files/surfaces. The main integrator reviews and commits the combined result directly to `main`.

## Shared TODO

### P0 — Playable core
- [x] Create a greenfield Android project scaffold
- [x] Pin Cefrium and configure its Codeberg Maven registry
- [x] Configure arm64-only packaging
- [ ] Build the app successfully in CI
- [ ] Install and load LastHaven on a physical arm64 device

### P1 — Gameplay reliability
- [x] Main-frame load failure and renderer termination retry UI
- [x] Request persistent web storage after initial page load
- [ ] Confirm fullscreen behavior and touch input
- [ ] Verify audio and autoplay after a user gesture
- [ ] Verify cookies, IndexedDB, LocalStorage and Cache Storage persist
- [ ] Verify lifecycle pause/resume and session restoration

### P2 — Settings and measurement
- [ ] Game URL
- [ ] Fullscreen and orientation
- [ ] Clear HTTP/code cache only (code-cache API still to verify)
- [ ] Confirmed clear-game-data action
- [ ] Reload, engine information and app version
- [ ] Run WebGL2, WebAssembly, WASM threads/SAB, WebSockets and audio checks in Cefrium; compare with Chrome on the same phone
- [ ] Measure cold start, frame pacing, memory, rendering latency and APK size before changing performance settings

### P3 — Release
- [ ] Create and securely back up the release signing key
- [ ] Build release APK/AAB for arm64 only
- [ ] Verify same-key in-place update preserves game data

## Documentation protocol

When starting work:
1. Read this file, STATUS and the last 5–10 changelog entries.
2. Claim a slice under STATUS → Active work.
3. Implement the smallest useful change.
4. Build/test where possible.
5. Update STATUS, append CHANGELOG-AGENTS, and unclaim when done.

Append changelog entries with date, role, files, change, reason, verification, do-not-redo notes and follow-ups. A failed experiment is still worth recording.

## Storage and privacy

- Use the Cefrium default persistent profile unless a documented requirement proves otherwise.
- Do not place browser data in cache directories.
- Never clear user data during startup, update or recovery.
- No analytics SDKs, tracking or background services.
- Do not log game cookies, tokens, local storage values or other session secrets.

# LastHavenLauncher

Minimal Android launcher for the **LastHaven** WotLK private server.  
The game already exists at the URL below. This app only embeds a full Chromium engine and opens it reliably.

**Default game URL:** `https://alt.lasthavens.com/play` (editable in Settings)

## Goal

Open LastHaven and play. No browser chrome, no game library, no WebView fallback.

## Stack

| Item | Choice |
|------|--------|
| Engine | **Cefrium** only (`com.cefrium:cefrium-sdk` + Compose `CefriumView`) |
| Chromium (pin) | Track current Cefrium release (Chromium 156 family as of docs) |
| ABI | **arm64-v8a only** |
| UI | Kotlin + Jetpack Compose |
| Toolchain | JDK **25**, AGP 9.4+, Gradle 9.7+, compileSdk 36–37, minSdk 29+ |
| applicationId | Fixed from first signed build (changing id or signing key wipes updates / data) |

Maven: primary `https://maven.cefrium.com` (group `com.cefrium`).  
Codeberg Maven registry mirrors the newest releases.

Official docs: [cefrium.com/quickstart](https://cefrium.com/quickstart/)

## Priorities

| Priority | Scope |
|----------|--------|
| **P0** | New project, Cefrium integrated, LastHaven loads |
| **P1** | Fullscreen, touch, audio, persistent storage, connection retry |
| **P2** | Settings, measurable performance work, arm64 APK size |
| **P3** | Release APK; verify in-place update keeps game data |

## Build (once the Gradle tree exists)

```bash
# Requires JDK 25 + Android SDK matching Cefrium requirements
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

Prefer a physical **arm64** phone (test class: modern device, ~12 GB RAM).

## Settings (planned)

- Game URL  
- Fullscreen  
- Screen orientation  
- Clear game cache (HTTP cache + code cache only)  
- Clear game data (cookies, IndexedDB, localStorage, Cache Storage — with confirmation)  
- Reload game  
- Engine information (Cefrium + Chromium versions)  
- Application version  

## Storage rules

- Must persist: cookies, IndexedDB, localStorage, Cache Storage across sessions.  
- Same `applicationId` + signing key across APK updates must keep game data.  
- Request persistent storage (`navigator.storage.persist()`) on a valid launch path.  
- Never clear browser data automatically.  
- “Clear cache” must not touch game data stores.

## Web requirements checklist

Record results in [`STATUS.md`](STATUS.md) **inside the Cefrium build** (Chrome on device as reference):

- [ ] WebGL2  
- [ ] WebAssembly  
- [ ] WASM threads / SharedArrayBuffer (needs COOP/COEP from the game server)  
- [ ] WebSockets  
- [ ] Audio (including after a user gesture)  

Use remote debugging (`chrome://inspect`) against the embedded engine.

## Performance policy

Optimize only where you can show **before/after numbers**. No background services. Prefer GPU path and long-session stability over speculative flags.

| Metric | How |
|--------|-----|
| Cold start → first paint | logcat timestamps |
| Steady FPS / jank | DevTools Performance in-game |
| Memory (PSS / native) | Android Studio Profiler |
| APK / download size | arm64-only build report |

## Agent workflow

See [`AGENTS.md`](AGENTS.md) for non-goals, architecture, parallel agent roles, and the shared TODO.

**Every change must be documented** so agents do not overlap or redo work:

| File | Purpose |
|------|---------|
| [`STATUS.md`](STATUS.md) | Current truth: phase, blockers, active ownership, metrics |
| [`CHANGELOG-AGENTS.md`](CHANGELOG-AGENTS.md) | Append-only log of each change (what / why / verified / do-not-redo) |

Claim work under STATUS → Active work before editing a surface. Unclaim when done.

## License

- App code: MIT (unless stated otherwise in `LICENSE`)  
- Cefrium / CEF / Chromium: LGPL-3.0-or-later and upstream licenses  

# Project status — LastHavenLauncher

Living truth for agents. Update on every meaningful change. Details and history go in `CHANGELOG-AGENTS.md`.

**Last updated:** 2026-11-24  
**Phase:** P0 — greenfield scaffold not started

---

## Snapshot

| Area | State | Notes |
|------|-------|--------|
| Repository | Bootstrap ready | Project instructions and tracking docs are on `main`; no application source or Gradle project exists yet |
| Android scaffold | Not started | Kotlin + Jetpack Compose; JDK 25, AGP 9.4+, Gradle 9.7+ target |
| Cefrium integrated | Not started | Target: `https://maven.cefrium.com`; verify and pin the current plugin/SDK at integration time |
| LastHaven loads | Not started | URL: `https://alt.lasthavens.com/play` |
| Fullscreen / touch / audio | Not started | |
| Persistent storage | Not started | |
| Settings | Not started | |
| Release APK | Not started | |

---

## Active work

| Owner / role | Surface | Started | Notes |
|--------------|---------|---------|--------|
| — | — | — | No implementation work claimed |

---

## Blockers

| ID | Blocker | Impact | Owner |
|----|---------|--------|-------|
| — | None currently recorded | — | — |

---

## Web requirements (record inside Cefrium build)

| Feature | Chrome on device | Cefrium build | Notes |
|---------|------------------|---------------|-------|
| WebGL2 | TBD | TBD | |
| WebAssembly | TBD | TBD | |
| WASM threads / SAB | TBD | TBD | Needs COOP/COEP from server |
| WebSockets | TBD | TBD | |
| Audio after gesture | TBD | TBD | |

---

## Performance log

| Date | Change | Metric | Before | After | Device |
|------|--------|--------|--------|-------|--------|
| — | — | — | — | — | — |

---

## Decisions (do not reverse without STATUS note)

1. Cefrium only — no WebView fallback.
2. arm64-v8a product ABI only.
3. Fixed applicationId + signing key after first release build.
4. Every agent change documented in CHANGELOG-AGENTS + STATUS.
5. Get the game loading before polish or speculative optimization.

---

## Next actions (ordered)

1. Scaffold the minimal Kotlin + Compose Android project using the target toolchain.
2. Verify the current official Cefrium plugin and SDK versions; add Maven, plugin, SDK, and arm64-only ABI filters.
3. Host `CefriumView` and load the default LastHaven URL.
4. Build and run on a physical arm64 device; record actual results and complete the web capability checklist via `chrome://inspect`.
5. Add P1 fullscreen, touch/audio, persistent storage, lifecycle handling, and connection retry.

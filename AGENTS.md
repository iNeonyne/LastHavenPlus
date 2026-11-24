# AGENTS.md — LastHavenLauncher

Instructions for any human or AI agent working in this repository.

## Mission

Ship the simplest reliable Android app that opens **LastHaven** (WotLK private server client on the web) in **Cefrium** and lets the user play.

Default URL: `https://alt.lasthavens.com/play`

Do **not** rebuild the game. Do **not** use System WebView. Do **not** add a general browser UI.

---

## Hard rules (read before editing)

1. **Document every change for other agents.**  
   After any meaningful edit (code, Gradle, docs, experiments, failed attempts), update:
   - [`STATUS.md`](STATUS.md) — current truth (what works, what doesn’t, blockers)
   - [`CHANGELOG-AGENTS.md`](CHANGELOG-AGENTS.md) — append-only log of *what* changed, *why*, *who/role*, and *what others must not redo*
   Incomplete logs cause overlap and wasted work. Treat an undocumented change as incomplete.

2. **No parallel conflicting edits.**  
   Before claiming a surface (module, file, Gradle block, settings screen), check `STATUS.md` “Active work” and the latest `CHANGELOG-AGENTS.md` entries. If another agent owns it, coordinate or wait.

3. **Research before inventing APIs.**  
   Cefrium is pre-1.0. Prefer [cefrium.com/quickstart](https://cefrium.com/quickstart/) and official samples. Flag unverified API names in STATUS instead of guessing.

4. **Game first.**  
   Get LastHaven loading in Cefrium before settings polish, theming, or speculative performance flags.

5. **arm64-v8a only.**  
   Filter ABIs in Gradle. Do not ship x86 / x86_64 / armeabi-v7a native libs in the product APK.

6. **Stable identity.**  
   Never change `applicationId` or the release signing key once established. Updates must preserve game data.

7. **Storage safety.**  
   Never auto-clear cookies, IndexedDB, localStorage, or Cache Storage. “Clear cache” ≠ “Clear data”.

---

## Non-goals

- Recreating or modifying the LastHaven game client  
- System WebView or multi-engine fallbacks  
- Bookmarks, tabs, search, multi-site browser  
- Background services, push, accounts (unless the game itself requires them in-page)  
- Over-engineered architecture before a working play path  

---

## Architecture (target)

```
app/
  MainActivity          → fullscreen host, lifecycle, back handling
  GameScreen / host     → CefriumView (or View CefriumBrowser), load URL, loading + error UI
  Settings              → small overlay or activity; does not interrupt mid-fight unless user opens it
  SettingsStore         → URL, fullscreen, orientation preferences
```

Engine: **Cefrium only**

| Piece | Notes |
|-------|--------|
| Maven | `https://maven.cefrium.com` + `includeGroup("com.cefrium")` |
| Plugin | `id("com.cefrium")` version pinned with SDK |
| SDK | `com.cefrium:cefrium-sdk:<version>` |
| UI API | Prefer Compose `CefriumView` + `rememberCefriumViewState` per official quickstart; View API (`CefriumBrowser`) is allowed if documented |
| Init | Prefer SDK auto-init; only call `Cefrium.initialize` if measured need |
| Toolchain | JDK **25**, AGP 9.4+, Gradle 9.7+, minSdk 29+, large Gradle heap (`-Xmx4g`) |

Pin exact Cefrium version in Gradle and record Chromium/Cefrium versions in Settings → Engine information and in STATUS.

---

## Parallel agent roles

Use roles to split work. Each role updates STATUS + CHANGELOG-AGENTS when finishing a slice.

| Role | Owns | Does not own |
|------|------|----------------|
| **Architecture** | Module layout, package names, navigation boundaries, shared types | Engine API details, pixel-level UI polish |
| **Android** | Manifest, lifecycle, back press, permissions, release signing notes | Cefrium-specific calls without reading docs |
| **Cefrium** | SDK version, Maven, `CefriumView` / browser host, engine info strings | App-wide theming, Play Store listing |
| **Performance** | Measured experiments (FPS, memory, start time, APK size) with before/after | Untested flag dumps |
| **QA** | Web feature checklist, device matrix notes, regression list | Shipping features without a failing test or repro |

**Anti-redundancy**

- One owner per file or Gradle block at a time → list under STATUS “Active work”.  
- Performance changes require a STATUS note with metric + numbers.  
- If research is already in STATUS, do not re-research; extend it.  
- Prefer editing existing files over adding parallel “v2” implementations.

---

## Shared TODO (keep in sync with STATUS.md)

### P0

- [ ] Scaffold empty Android app (Kotlin, Compose) matching Cefrium toolchain  
- [ ] Wire Cefrium Maven + plugin + SDK; arm64-only ABI filters  
- [ ] Load default LastHaven URL in CefriumView  
- [ ] Confirm page renders on physical arm64 device  

### P1

- [ ] Fullscreen gameplay path  
- [ ] Touch + audio (user-gesture autoplay path)  
- [ ] Persistent storage + `navigator.storage.persist()` where applicable  
- [ ] Connection failure → retry UI  
- [ ] Lifecycle: resume restores session without wiping data  

### P2

- [ ] Settings: URL, fullscreen, orientation, clear cache, clear data, reload, engine info, app version  
- [ ] Performance pass with logged metrics  
- [ ] APK size: arm64-only, no dead ABIs  

### P3

- [ ] Release-signed APK  
- [ ] Verify update (same id + key) keeps game data  

### Web capability checks (Cefrium build)

- [ ] WebGL2  
- [ ] WASM  
- [ ] WASM threads / SharedArrayBuffer (COOP/COEP)  
- [ ] WebSockets  
- [ ] Audio after gesture  

---

## Documentation protocol (mandatory)

### When you finish a task

Append to `CHANGELOG-AGENTS.md`:

```markdown
## YYYY-MM-DD — short title
- **Role:** Cefrium | Android | …
- **Files:** list paths touched
- **Change:** what landed
- **Why:** motivation / bug / metric
- **Verified:** device / command / result (or “not verified”)
- **Do not redo:** explicit note for future agents
- **Follow-ups:** open items (link STATUS)
```

Update `STATUS.md`:

- Current phase (P0–P3)  
- Working / broken / unknown  
- Active work (who owns what)  
- Blockers  
- Latest measured numbers  

### When research fails or is blocked

Still log it. “Tried X, failed because Y” prevents the next agent from repeating X.

### When you start work

1. Read `STATUS.md` and the last 5–10 changelog entries.  
2. Claim a slice under Active work.  
3. Implement.  
4. Log. Unclaim when done.

---

## Performance guidelines

- Optimize only with before/after numbers in STATUS.  
- Candidates: GPU/Surface path, WebGL, WASM memory, frame pacing, long sessions, APK size.  
- Avoid: background services, unbounded caches, duplicate engine instances.  

---

## Security & privacy (minimal)

- Only load the configured game URL by default (user may change URL in settings).  
- No analytics SDKs unless explicitly required later.  
- Clear-data is destructive; always confirm.

---

## References

- Spec: project objective doc (LastHavenLauncher)  
- Cefrium: https://cefrium.com/quickstart/  
- Releases: https://cefrium.com/releases/  
- Status living file: [`STATUS.md`](STATUS.md)  
- Agent change log: [`CHANGELOG-AGENTS.md`](CHANGELOG-AGENTS.md)  

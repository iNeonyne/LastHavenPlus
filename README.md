# LastHavenLauncher

A minimal Android application that embeds the **Cefrium** (Chromium Embedded Framework for Android) engine to run the **LastHaven WotLK private server** at `https://lasthavens.com/play`.

## Quick Start

### Prerequisites
- JDK 25 (required by Cefrium 0.8.0+ for class file version 69)
- Android SDK API 36
- NDK r27c
- Gradle 9.4+

### Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install & Run
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.lasthaven.launcher -c android.intent.category.LAUNCHER 1
```

### Logs
```bash
# Filter by package
adb logcat -s "LastHavenLauncher" "Cefrium" "chromium" "*:E"
```

---

## 🤖 Agent Instructions for AI Assistants

### Project Overview
**LastHavenLauncher** is a single-Activity Kotlin/Compose app using Cefrium 0.21.0 (Chromium 156) to render the LastHaven WoW game. No browser UI — pure game launcher.

**Architecture:**
- `MainActivity` → `GameScreen` (Compose) → `CefriumView` with `rememberCefriumViewState`
- `LastHavenApplication` → Cefrium init (data dir, GPU process)
- `SettingsStore` → DataStore preferences (URL, fullscreen, orientation)
- `SettingsScreen` → Compose UI (long-press/FAB to open)

### Key Files
| File | Purpose |
|------|---------|
| `app/src/main/java/com/lasthaven/launcher/LastHavenApplication.kt` | App class, Cefrium.init(), setDataDirectory() |
| `app/src/main/java/com/lasthaven/launcher/MainActivity.kt` | Activity, GameScreen, CefriumView, NavigationClient, ChromeClient |
| `app/src/main/java/com/lasthaven/launcher/SettingsStore.kt` | DataStore preferences (Flowable) |
| `app/src/main/java/com/lasthaven/launcher/SettingsScreen.kt` | Settings UI (Compose) |
| `app/src/main/java/com/lasthaven/launcher/ui/theme/Theme.kt` | Dark gaming theme |
| `AGENTS.md` | Complete agent handoff guide |

### Cefrium Integration Points
```kotlin
// Application.onCreate() - MUST run before any browser
Cefrium.setDataDirectory(File(filesDir, "app_cefrium"))  // Persistent storage
Cefrium.setGpuProcessEnabled(true)                        // WebGL stability
Cefrium.initialize(this)

// GameScreen - CefriumView with hoisted state
val cefriumState = rememberCefriumViewState(
    url = gameUrl,
    incognito = false,
    profile = null
)
CefriumView(
    state = cefriumState,
    navigationClient = navClient,   // Load errors, retry
    chromeClient = chromeClient,    // Fullscreen sync
    backgroundMediaPolicy = BackgroundMediaPolicy.PAUSE_ON_STOP
)
```

### Hard Constraints (DO NOT CHANGE)
- **JDK 25 required** — class file version 69, older toolchains fail
- **Gradle 9.4+, AGP 9.4+, compileSdk 36, minSdk 29**
- **NDK abiFilters**: `arm64-v8a`, `armeabi-v7a`, `x86_64`
- **Gradle heap**: `-Xmx4g` in `gradle.properties`
- **Cefrium Maven repo**: `https://maven.cefrium.com` (both pluginManagement and dependencyResolutionManagement)
- **`android:extractNativeLibs="false"`** for APK size

### Do's and Don'ts
| ✅ Do | ❌ Don't |
|------|----------|
| Use `CefriumNavigationClient` / `CefriumChromeClient` | Use deprecated `CefriumBrowser.Callback` |
| Set data directory before first browser | Clear Cefrium data dir manually — use Profile APIs |
| Use `rememberCefriumViewState` for hoisted state | Assume WebView-like behavior — Cefrium is full Chromium |
| Use `onDocumentAvailable` for JS injection | Skip `setDataDirectory()` — data goes to cache (cleared by OS) |
| Use `CefriumProfile.clearSiteData()` for cache clearing | Delete profile dir while app runs |

### Testing Checklist (Phase 1)
- [ ] App launches without crash
- [ ] Loads `https://lasthavens.com/play`
- [ ] WebGL renders (game graphics visible)
- [ ] WebAssembly works (game logic runs)
- [ ] Touch input responds (movement, interaction)
- [ ] Audio plays (game sounds/music)
- [ ] Fullscreen toggles (game's requestFullscreen)
- [ ] Orientation locks per setting
- [ ] Cookies/IndexedDB/localStorage persist across restart
- [ ] Connection error shows retry UI (auto-retry after 3s)
- [ ] Renderer crash triggers auto-reload (after 1s)
- [ ] Back button minimizes app (not exits)
- [ ] Settings opens (long-press/FAB)
- [ ] Game URL change takes effect on reload
- [ ] Clear Cache preserves login/session
- [ ] Clear Data wipes all game data
- [ ] Engine Info shows Cefrium/Chromium versions

### Phase 2 (Future) — Addon System
When implementing WoW addon support (replacing Chrome extension):
1. Kotlin port of `archive.js` (ZIP validation, TOC parsing)
2. Kotlin port of `library.js` (merge/toggle/remove)
3. Store addons in CefriumProfile (IndexedDB)
3. Inject 3 JS modules via `executeJavaScript()` on `onDocumentAvailable`:
   - `bridge.js` (isolated world → reads IndexedDB)
   - `loader.js` (MAIN world → hooks `Worker` → `WebVfsStoreIo`)
   - `runtime-core.js` (overrides `XMLHttpRequest` + manifest)
4. Addon Manager UI in Settings

Reference: `.harness/attachments/lasthavens-addon-manager/` (original Chrome extension)

### Common Issues & Fixes
| Error | Cause | Fix |
|-------|-------|-----|
| `Unsupported class file major version 69` | JDK < 25 | Install JDK 25, verify `JAVA_HOME` |
| `Could not find com.cefrium:cefrium-sdk` | Missing Maven repo | Verify `maven { url = uri("https://maven.cefrium.com") }` in both blocks |
| `Cefrium.initialize() throws IllegalStateException` | Unsupported ABI | Test on arm64-v8a device or x86_64 emulator |
| Black screen / WebGL fail | GPU process issue | Check `Cefrium.getRendererServices().gpuProcessEnabled`, try disabling |
| "Clear Data" doesn't work immediately | Deferred to next start | `CefriumProfile.delete("default")` returns `false` if browsers open |

### CI/CD
GitHub Actions workflow: `.github/workflows/android.yml`
- Builds debug APK on push/PR
- Builds release APK on manual dispatch/main push
- Uses `android-actions/setup-android@v4` with `cmdline-tools-version: latest`

---

## License
LGPL-3.0-or-later (Cefrium), project code MIT

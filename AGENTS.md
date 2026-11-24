# LastHavenLauncher - Agent Guide

## Project Overview

**LastHavenLauncher** is a minimal Android application that embeds the **Cefrium** (Chromium Embedded Framework for Android) engine to run the **LastHaven WotLK private server** at `https://lasthavens.com/play`.

- **Engine**: Cefrium 0.21.0 (Chromium 156-based)
- **Target**: Android 10+ (API 29), arm64-v8a / armeabi-v7a / x86_64
- **Language**: Kotlin + Jetpack Compose
- **Build**: Gradle 9.4, AGP 9.4, JDK 25, compileSdk 36
- **Architecture**: Single Activity, Compose UI, CefriumView for gameplay

### Key Features (Phase 1 - Core)
- Direct launch into LastHaven game
- Hardware-accelerated Surface rendering (WebGL, WebAssembly)
- Fullscreen gameplay with edge-to-edge display
- Touch input and audio support
- Persistent storage (cookies, IndexedDB, localStorage, Cache Storage)
- Connection error handling with auto-retry
- Minimal settings screen (Game URL, Fullscreen, Orientation, Clear Cache/Data, Reload, Engine Info)

### Phase 2 (Future) - Addon System
- WoW addon support via custom JS injection (replacing Chrome extension)
- Addon manager UI for import/enable/disable/remove

---

## Project Structure

```
LastHavenLauncher/
├── AGENTS.md                    # This file
├── build.gradle.kts             # Root Gradle config (plugins)
├── settings.gradle.kts          # Plugin management + Cefrium Maven repo
├── gradle.properties            # Gradle JVM args (4GB heap)
├── app/
│   ├── build.gradle.kts         # App module config (dependencies, SDK)
│   ├── proguard-rules.pro       # ProGuard rules for Cefrium
│   └── src/main/
│       ├── AndroidManifest.xml  # Permissions, Application, Activity
│       ├── java/com/lasthaven/launcher/
│       │   ├── LastHavenApplication.kt    # App class - Cefrium init
│       │   ├── MainActivity.kt            # Main Activity + GameScreen
│       │   ├── SettingsStore.kt           # DataStore preferences
│       │   ├── SettingsScreen.kt          # Settings UI (Compose)
│       │   └── ui/theme/Theme.kt          # Dark gaming theme
│       └── res/
│           ├── values/strings.xml         # String resources
│           └── xml/
│               ├── backup_rules.xml       # Backup exclusion (Cefrium data)
│               └── data_extraction_rules.xml
```

---

## Build & Run

### Prerequisites (One-time Setup)
```bash
# Install Android SDK tools (JDK 25, D8, aapt2, apksigner, android.jar API 36)
# Run via android_sdk_install tool with platform=36, gradle=true

# Verify installation
java -version           # Should show openjdk 25
d8 --version            # Should work
aapt2 version           # Should work
ls $HOME/android-sdk/platforms/android-36/android.jar
```

### Build Debug APK
```bash
cd /storage/emulated/0/Android/data/com.androidharness.app/files/workspace
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK
```bash
# Configure signing in app/build.gradle.kts first
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Install & Run
```bash
# Via ADB (requires Wireless ADB pairing in AndroidHarness)
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.lasthaven.launcher -c android.intent.category.LAUNCHER 1
```

### View Logs
```bash
# Filter by package
read_logcat --package_name com.lasthaven.launcher --level D
```

---

## Cefrium Integration Points

### Initialization (LastHavenApplication.kt)
```kotlin
// Called BEFORE any browser creation
Cefrium.setDataDirectory(File(filesDir, "app_cefrium"))  // Persistent storage
Cefrium.setGpuProcessEnabled(true)                        // WebGL stability
Cefrium.initialize(this)                                   // Early init
```

### Game Screen (MainActivity.kt)
```kotlin
val cefriumState = rememberCefriumViewState(
    url = gameUrl,
    incognito = false,
    profile = null  // Default profile
)

CefriumView(
    state = cefriumState,
    modifier = Modifier.fillMaxSize(),
    navigationClient = navigationClient,  // Load errors, retry
    chromeClient = chromeClient,          // Fullscreen sync
    backgroundMediaPolicy = BackgroundMediaPolicy.PAUSE_ON_STOP,
    onBrowserReady = { browser -> /* Phase 2: inject addon JS */ }
)
```

### Navigation Client (Error Handling)
```kotlin
object : CefriumNavigationClient {
    override fun onLoadError(browser, error) {
        // Auto-retry on network errors after 3s
        if (isNetworkError(error.errorCode)) {
            delay(3000); browser.reload()
        }
    }
    override fun onRenderProcessTerminated(browser, status, errorCode) {
        // Renderer crash - reload after 1s
        delay(1000); browser.reload()
    }
}
```

### Chrome Client (Fullscreen)
```kotlin
object : CefriumChromeClient {
    override fun onFullscreenChanged(browser, fullscreen) {
        isFullscreen = fullscreen
        updateSystemBarsVisibility(fullscreen)
    }
}
```

---

## Settings & Persistence

### DataStore (SettingsStore.kt)
- **Game URL**: String, default `https://lasthavens.com/play`
- **Fullscreen**: Boolean, default `true`
- **Orientation**: Enum (PORTRAIT, LANDSCAPE, AUTO), default `LANDSCAPE`
- **First Launch**: Boolean, default `true`

### Cefrium Persistent Storage
- **Location**: `filesDir/app_cefrium/`
- **Survives**: App updates, device restarts, storage pressure
- **Contains**: Cookies, IndexedDB, localStorage, Cache Storage, Service Workers
- **NOT cleared** by "Clear Cache" (only HTTP cache)
- **Cleared** by "Clear Data" (nuclear option)

---

## Coding Conventions

### Kotlin
- **Language version**: Kotlin 2.0.20, JVM target 17
- **Coroutines**: Use `kotlinx.coroutines` for async work
- **Flow/State**: Compose `mutableStateOf` for UI state, RxJava `Flowable` for DataStore
- **Nullability**: Explicit `@Nullable`/`@NonNull` where needed
- **Visibility**: `private` by default, `internal` for module-internal

### Compose UI
- **Theme**: `LastHavenLauncherTheme` (dark only, gaming aesthetic)
- **Colors**: Primary `#4A90D9`, Surface `#121212`, Error `#FF6B6B`
- **Typography**: Material3 defaults, no custom fonts
- **State Hoisting**: `rememberCefriumViewState` for browser, `mutableStateOf` for UI
- **Side Effects**: `LaunchedEffect` for browser operations, `DisposableEffect` for cleanup

### Cefrium Usage
- **Always** set data directory before first browser
- **Enable** GPU process for WebGL games
- **Use** `CefriumNavigationClient` for load errors (not deprecated `Callback`)
- **Use** `CefriumChromeClient` for fullscreen/dialogs (not deprecated `Callback`)
- **Inject JS** via `onDocumentAvailable` or `onBrowserReady` + `executeJavaScript`

### Gradle
- **Kotlin DSL** (`.kts`) only
- **Version catalogs** via `compose-bom` for Compose
- **Cefrium plugins** from `maven.cefrium.com`
- **NDK abiFilters**: `arm64-v8a`, `armeabi-v7a`, `x86_64`
- **Heap**: `-Xmx4g` in `gradle.properties`

---

## Key APIs Reference

| API | Purpose |
|-----|---------|
| `Cefrium.setDataDirectory()` | Persistent storage (cookies, IDB, localStorage, Cache) |
| `Cefrium.setGpuProcessEnabled(true)` | Separate GPU process for WebGL |
| `CefriumProfile.getDefault()` | Main profile for game data |
| `CefriumProfile.clearSiteData(url)` | Clear specific site's storage |
| `CefriumView(state)` | Compose gameplay view |
| `rememberCefriumViewState(url)` | Hoisted browser state |
| `CefriumNavigator` | Programmatic control (reload, back) |
| `CefriumNavigationClient` | Load events, errors, SSL, auth |
| `CefriumChromeClient` | Fullscreen, dialogs, permissions |
| `BackgroundMediaPolicy.PAUSE_ON_STOP` | Audio pauses when app backgrounds |
| `executeJavaScript()` | Inject scripts into page (Phase 2) |

---

## Testing Checklist (Phase 1)

- [ ] App launches without crash
- [ ] Loads `https://lasthavens.com/play`
- [ ] WebGL renders (game graphics visible)
- [ ] WebAssembly works (game logic runs)
- [ ] Touch input responds (movement, interaction)
- [ ] Audio plays (game sounds/music)
- [ ] Fullscreen toggles (game's requestFullscreen)
- [ ] Orientation locks per setting
- [ ] Cookies persist across app restart
- [ ] IndexedDB/localStorage persist
- [ ] Connection error shows retry UI
- [ ] Renderer crash triggers auto-reload
- [ ] Back button minimizes app (not exits)
- [ ] Settings screen opens (long-press/FAB)
- [ ] Game URL change takes effect on reload
- [ ] Clear Cache preserves login/session
- [ ] Clear Data wipes all game data
- [ ] Engine Info shows Cefrium/Chromium versions

---

## Troubleshooting

### Build Fails: "Unsupported class file major version 69"
- **Cause**: JDK < 25
- **Fix**: Ensure `openjdk-25` installed, `JAVA_HOME` points to it

### Build Fails: "Could not find com.cefrium:cefrium-sdk"
- **Cause**: Missing Maven repo
- **Fix**: Verify `maven { url = uri("https://maven.cefrium.com") }` in both `settings.gradle.kts` blocks

### Runtime: "Cefrium.initialize() throws IllegalStateException"
- **Cause**: Unsupported ABI (x86, 32-bit ARM)
- **Fix**: Test on arm64-v8a device or x86_64 emulator

### Runtime: Game shows black screen
- **Cause**: GPU process issue or WebGL context lost
- **Fix**: Check `Cefrium.getRendererServices().gpuProcessEnabled`, try disabling GPU process

### Runtime: "Clear Data" doesn't work
- **Cause**: Profile deletion deferred to next start
- **Fix**: `CefriumProfile.delete("default")` returns `false` if browsers open; data cleared on next app launch

---

## Phase 2: Addon System (Future Work)

When implementing addon support, reference the Chrome extension at `.harness/attachments/lasthavens-addon-manager/`:
- `bridge.js` → Isolated world, reads IndexedDB, sends to loader
- `loader.js` → MAIN world, overrides `Worker` → hooks `WebVfsStoreIo`
- `runtime-core.js` → `LHInstallOverlay` overrides `XMLHttpRequest` + manifest

Implementation approach:
1. Kotlin port of `archive.js` (ZIP validation, TOC parsing)
2. Kotlin port of `library.js` (addon merge/toggle/remove)
3. Store addons in CefriumProfile (IndexedDB)
4. Inject 3 JS modules via `executeJavaScript()` on `onDocumentAvailable`
5. Addon Manager UI in Settings

---

## Agent Handoff Notes

### For Next Session
1. Run `./gradlew assembleDebug` to verify build
2. Install APK on device/emulator
3. Test Phase 1 checklist
4. Fix any Cefrium initialization or rendering issues
5. Only then proceed to Phase 2 (addons)

### Common Pitfalls to Avoid
- Don't use deprecated `CefriumBrowser.Callback` - use NavigationClient/ChromeClient
- Don't clear Cefrium data directory manually - use Profile APIs
- Don't assume WebView-like behavior - Cefrium is full Chromium
- Don't skip `setDataDirectory()` - data will be in cache dir (cleared by OS)
- Don't forget `android:extractNativeLibs="false"` for APK size

---

*Generated for LastHavenLauncher v1.0.0 - Phase 1 Core Complete*

# GeckoView integration notes

**Current branch:** `beta`  
**Current engine:** Mozilla GeckoView 160, ARM64 artifact  
**Last reviewed:** 2026-11-26

This document records the GeckoView integration as it exists in beta. It supplements, but does not replace, Mozilla's documentation.

## Official references

- [GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [Quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [Architecture and process model](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [GeckoSession API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.html)
- [GeckoView API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoView.html)
- [Site permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)
- [WebExtensions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [Mozilla Maven repository](https://maven.mozilla.org/maven2/org/mozilla/geckoview/)

Use the API matching the pinned GeckoView artifact. Do not assume moving mozilla-central Javadocs exactly match the release used by this project.

## Current implementation

Beta uses `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`, a process-scoped `GeckoRuntime`, a `GeckoSession` and a `GeckoView` host. Runtime/session initialization is deferred until Play. The game URL is `https://lasthavens.com/play`.

The app opens on a launcher Home screen. Settings does not initialize the game. Android Back from gameplay opens a full-screen menu containing Reload, Settings and Return to game. Settings, diagnostics and update details are full-screen destinations. PR #43 also adds responsive Home/settings layouts and adaptive launcher icon resources.

The existing GeckoSession is retained when its view is detached for launcher navigation. Source and JVM layout-policy tests are integrated; physical session retention and UI behavior still require device verification.

## Planned UX redesign — issue #56

The Home-screen-first flow described above is the **current v2.1.0 beta/stable baseline**, not the next UX target. The owner has requested a direct-launch experience with a responsive in-game overlay; implementation is being developed by Chaty under issue #56 on `chaty/issue-56-direct-launch-overlay`; it is not yet integrated into beta. Until that work is reviewed and merged, do not describe the overlay as implemented. The new flow must preserve the existing GeckoSession and profile, support Settings in portrait and landscape, restore landscape gameplay, and keep Addon Manager out of scope while #23 remains on hold.

## Build configuration and identity

The current beta build uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and Android SDK Platform 37.2. GeckoView's AAR metadata requires compile SDK 37.2. Keep the pinned toolchain unless a reproducible compatibility issue and validated change justify otherwise.

Preserve:
- ARM64 only (`arm64-v8a`)
- Application ID `com.neonyne.lasthavenlauncher`
- Established release signing identity
- User-confirmed update download and Android installer handoff
- Full APK fallback when a differential patch is ineligible or fails validation

## Pinned artifact and dependency audit (issue #20)

### Artifact identity

- Coordinates: `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`, packaging AAR.
- Source: [Mozilla Maven](https://maven.mozilla.org/maven2/org/mozilla/geckoview/geckoview-arm64-v8a/160.0.20261116130455/), published with Gradle module metadata (the POM itself defers to it).
- Upstream revision: `mozilla-release` `b1145b0710192f716be5f59f5b2dfeed650d5294` (recorded in the artifact POM's SCM block).
- License declared in the POM: [Mozilla Public License 2.0](http://mozilla.org/MPL/2.0/).

### POM-declared dependencies (fetched 2026-11-26)

| Group:artifact | Version | Scope | Project-side note |
|---|---|---|---|
| `org.jetbrains.kotlin:kotlin-stdlib` | 2.4.21 | compile | Catalog (#99) pins Kotlin 2.4.10; Gradle resolves the higher runtime version |
| `androidx.annotation:annotation` | 1.11.0 | runtime | |
| `androidx.annotation:annotation-experimental` | 1.6.0 | runtime | |
| `androidx.collection:collection` | 1.7.0 | runtime | |
| `androidx.core:core` | 1.19.1 | runtime | Catalog (#99) pins `androidxCore` 1.16.0; Gradle resolves 1.19.1 |
| `androidx.lifecycle:lifecycle-common` | 2.11.0 | runtime | |
| `androidx.lifecycle:lifecycle-process` | 2.11.0 | runtime | |
| `com.google.android.gms:play-services-fido` | 21.3.1 | runtime | Google Play Services SDK terms apply |
| `org.yaml:snakeyaml` | 2.2 | runtime | |
| `androidx.media3:media3-common` | 1.11.1 | runtime | media3 family pulled for playback support |
| `androidx.media3:media3-datasource` | 1.11.1 | runtime | |
| `androidx.media3:media3-decoder` | 1.11.1 | runtime | |
| `androidx.media3:media3-exoplayer` | 1.11.1 | runtime | |
| `androidx.media3:media3-exoplayer-hls` | 1.11.1 | runtime | |

Resolution caveat: this is the POM's declared list, not a resolved-by-Gradle dump (the audit environment has no JDK/Android SDK). When the toolchain is available, capture `./gradlew app:dependencies --configuration releaseRuntimeClasspath` as the authoritative resolved set and reconcile it against the #99 version catalog, so consumer pins are never lowered below GeckoView's runtime requirements.

### License and notice obligations

- GeckoView is MPL 2.0. Binary distribution (our APK) must ship the MPL 2.0 notice text and identified source location with the app (MPL 2.0 section 3.4); this is not yet packaged in the APK and is an outstanding obligation, best resolved together with the #67 license work.
- Transitive dependencies are licensed independently (Apache 2.0 for androidx media3/annotation/collection/core/lifecycle and kotlin-stdlib; snakeyaml Apache 2.0; play-services-fido under Google's Play Services SDK terms). Verify each artifact's own POM before making any redistribution claim; none of this changes our apps' runtime behavior.

### Used-API cross-check against official documentation

Verification methods, in order of authority for this audit:
1. **Compile-time proof:** every symbol below is compiled against the pinned AAR in CI (PR validation and signed beta runs compile `assembleRelease`); a missing symbol would fail the build.
2. **Reflection guards:** `GeckoViewWebExtensionApiTest` (`testDebugUnitTest` in the same CI runs) reflects `WebExtensionController.installBuiltIn(String)`, `ensureBuiltIn(String, String)` returning `GeckoResult`, `WebExtension$TabDelegate` (`setTabDelegate`, `onNewTab`, `onOpenOptionsPage`), `WebExtension$MetaData` fields (`baseUrl`, `optionsPageUrl`, `openOptionsPageInTab`) and `MessageDelegate` wiring against the pinned artifact binaries.
3. **Official Javadocs:** the mozilla-central snapshot (geckoview 163.0.20261125035921, the nearest currently published; release-specific 160 docs are not served) confirms all used symbols.

Used surface and status:

| Symbol | Verified in docs | Notes |
|---|---|---|
| `GeckoRuntime.create(context, settings)` | yes | |
| `GeckoRuntimeSettings.Builder().remoteDebuggingEnabled(boolean).build()` | yes | enabled only for debuggable builds |
| `GeckoSession.open(GeckoRuntime)`, `close()`, `reload()`, `loadUri(String)` | yes | |
| `GeckoSession.ProgressDelegate.onPageStart` / `onPageStop` | yes | |
| `GeckoSession.ContentDelegate.onCrash`, `onKill`, `onFullScreen`, `onExternalResponse` + `WebResponse` | yes | crash/kill handlers keep the load-state flags; external responses are deliberately unhandled |
| `GeckoSession.NavigationDelegate.onLoadRequest` -> `GeckoResult<AllowOrDeny>` + `LoadRequest` | yes | always returns `allow()` |
| `GeckoSession.PermissionDelegate` (onContentPermissionRequest -> `GeckoResult<Integer>`; `ContentPermission.uri`/`.permission`; constants `PERMISSION_PERSISTENT_STORAGE`, `PERMISSION_AUTOPLAY_AUDIBLE`, `PERMISSION_AUTOPLAY_INAUDIBLE`, `VALUE_ALLOW`, `VALUE_DENY`; `onAndroidPermissionsRequest` + `Callback.reject()`; `onMediaPermissionRequest` + `MediaSource`, `MediaCallback.reject()`) | yes | game-origin-scoped allow, everything else denied |
| `GeckoView` (Compose host), `GeckoResult.allow()` / `fromValue()` | yes | |
| `WebExtensionController` / `WebExtension` reflection surface | reflection-guarded | used by tests only; addon manager itself stays on hold (#23) |

Javadoc caveat: the snapshot is 163, not 160. No behavioral differences were observed for the used subset, and compile evidence against the 160 AAR is authoritative for symbol existence; behavior-specific differences remain a what-to-watch item during device verification.

### Measured APK-size impact

Sizes from GitHub release asset metadata (`assets[].size`), fetched 2026-11-26:

| Release | Engine | APK bytes | MiB |
|---|---|---:|---:|
| v1.0.0 | Cefrium | 120,381,565 | 114.8 |
| v2.0.0 | GeckoView 160 | 96,334,724 | 91.9 |
| v2.1.0 | GeckoView 160 | 96,444,836 | 92.0 |
| v2.2.0 | GeckoView 160 | 96,451,277 | 92.0 |

The v1.0.0 -> v2.0.0 change of `24,046,841` bytes (-20.0%) is not a pure engine delta: that changeset also contains the launcher Home redesign and updater hardening. The GeckoView 160 releases since then are stable (within ~0.13 MiB across three builds). A dedicated engine-only size comparison between comparable verified builds is unavailable without rebuilding a Cefrium-based APK, and is not worth producing given the Cefrium code is removed.

## Runtime, session and UI lifecycle

- Keep one runtime per application process.
- Manage the game session lifecycle explicitly.
- Keep the existing GeckoSession while navigating to Home/settings and back; do not reload solely because the visible Compose destination changes.
- Scope immersive UI, gameplay orientation and keep-screen-on to gameplay; Home/settings restore normal system UI.
- Handle navigation, progress/load state, content events, crashes, fullscreen, permissions, prompts, media and external responses using the pinned API and actual game requirements.
- Do not grant broad site or Android permissions automatically. Keep permission decisions origin-scoped and user-mediated where required.
- Do not log cookies, tokens, authorization headers, full sensitive URLs or page content.

A successful build proves compilation and packaging only. It does not prove gameplay, media, permissions, profile persistence or correct lifecycle behavior.

## Profile and data safety

GeckoView uses its own app-private browser data. No supported Cefrium-to-GeckoView profile migration has been established.

- Do not copy Cefrium profile files into GeckoView.
- Do not delete or reset legacy Cefrium profile data as part of this migration.
- A fresh game login may be required.
- Verify cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication across force-stop/restart and same-key app update on physical ARM64 hardware.
- Do not report the whole app-private data directory as profile-only size.
- Never clear user data during startup, update or recovery.

## Game compatibility test plan

Record device model, Android version, GeckoView version, beta commit, APK SHA-256 and result for every physical-device test.

| Area | Required result |
|---|---|
| Launch/login | Game opens and login completes |
| WebGL/WebGL2 | Game world renders without blank/black surfaces |
| WebAssembly | Required game modules load and execute |
| WebSockets | Login and live game traffic remain connected; reconnect works |
| Touch/input | Controls, drag, keyboard/IME and focus work |
| Fullscreen/orientation | Gameplay works; Home/settings restore system UI |
| Audio | Playback and pause/resume work and respect Android audio focus |
| Storage/auth | Cookies and site data persist across restart/update |
| Permissions/prompts | Required flows work and unrelated requests remain denied |
| Process recovery | Renderer/process loss does not corrupt profile/session state |
| Navigation | Reload, Back, Home, Return to game, redirects and errors behave correctly |
| Updater | Compatible differential and full APK fallback install with same identity/signature |

The owner has reported that the app and GeckoView work on an ARM64 device. No device model, Android version, exact commit/APK digest or individual test results are recorded, so this is not evidence for the full matrix or PR #43's new UI behavior. CI/emulator results do not replace physical ARM64 verification.

## Addon boundary

Issue #23 and all addon-manager work are on hold by owner instruction (2026-11-25). The previous XPI specification and audit remain historical reference only; do not investigate, assign or implement the manager until the owner explicitly approves resuming it. A WoW 3.3.5a addon ZIP/`.toc` package is not a Firefox WebExtension; GeckoView WebExtension support does not itself load game addons. Do not claim game addon injection.

## CI evidence and remaining work

- PR #43 exact-head unsigned validation passed in [run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) for `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3`.
- PR #43 merged to beta at `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210`.
- Post-merge signed beta run [#45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks, as recorded in merged PR #47.
- Published v2.1.0 tag run [#45603533787](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45603533787) built versionName 2.1.0 / versionCode 2001000; signature, established certificate, ARM64-only ABI, differential generation and asset upload/publish steps passed. The final draft-by-tag lookup failed HTTP 404; PR #54 later changed cleanup and verification to use the release ID output from the release action. PR #54 exact-head validation passed in run #45610110307; post-merge signed beta run [#45610518305](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45610518305) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks.
- Physical ARM64 game/profile/UI/update verification remains unverified; CI does not replace the device matrix.

Remaining work:
1. Complete physical ARM64 game, profile, permission, media, navigation and updater tests.
2. Inspect the adaptive launcher icon in the built APK.
3. Reconcile the resolved `releaseRuntimeClasspath` (via `./gradlew app:dependencies`) with the POM inventory and the #99 catalog once a Gradle-capable environment is available, and package the MPL 2.0 notice text with the APK before any external distribution.
4. Exercise the release-ID draft cleanup/verification path and optional differential fallback failure cases on a future owner-authorized versioned release run; do not republish v2.1.0 for testing.

# GeckoView migration reference for LastHavenPlus

**Status:** Preparation/reference document. No GeckoView dependency or production integration has been added yet.  
**Last checked:** 2026-11-25.  
**Primary owner:** Chaty for engine implementation (#21); Ploy may update this research document only under #20.

## Purpose

This file is the project-specific handoff for agents implementing the approved replacement of Cefrium with Mozilla GeckoView. It is not a copy of Mozilla's documentation. Mozilla's official pages remain authoritative; the notes below summarize the parts that affect this app and record what must be re-verified against the exact stable artifact selected for implementation.

## Official Mozilla documentation

- [GeckoView overview](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/index.html)
- [Consumer documentation index](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [Quick start — Gradle, Java compatibility, Runtime/Session/View setup](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [Architecture — Runtime, Session, View, delegates, process management](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [API Javadocs](https://mozilla.github.io/geckoview/javadoc/mozilla-central/index.html)
- [GeckoSession API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.html)
- [GeckoView API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoView.html)
- [Site permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)
- [Interacting with web content and WebExtensions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [Mozilla Maven repository](https://maven.mozilla.org/maven2/org/mozilla/geckoview/)
- [GeckoView source/documentation repository](https://github.com/mozilla/geckoview)

**Do not vendor/copy the full Mozilla docs or Javadocs into this repository.** Keep these official links and the concise app-specific notes here so agents can work offline from the implementation contract without creating a stale third-party documentation fork.

## Verified architectural model

Mozilla's architecture documentation describes three core objects:

1. `GeckoRuntime` — one process-level Gecko runtime, normally alive for the lifetime of the application process.
2. `GeckoSession` — a site/browser-window session with navigation, content and session-specific state.
3. `GeckoView` — the Android view that renders an attached session and dispatches input/events.

Mozilla's quick start creates the runtime once, opens a session against it, attaches the session to a view and loads a URI. The final implementation must use the exact API signatures from the pinned stable artifact's Javadocs, not copy snippets blindly from `mozilla-central` if they differ.

## Build and dependency requirements — not yet approved for change

- Mozilla publishes GeckoView from `https://maven.mozilla.org/maven2/`.
- Coordinates follow `org.mozilla.geckoview:geckoview-<channel>:<version>`; select the stable channel only for production.
- Mozilla's current consumer quick start states Java 17 source/target compatibility flags are required because GeckoView uses Java 17 APIs.
- The current LastHavenPlus beta uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and Android SDK 37. Do not change any of these based on a quick-start snippet alone. First verify how the selected GeckoView AAR behaves with the existing toolchain and project compile options.
- Do not pin a version based only on `mozilla-central` Javadocs. They may describe a newer development build. Resolve the exact stable version from Mozilla's Maven metadata and record the verification date, artifact URL, version, POM dependencies and checksums where available.
- Inspect the AAR/native libraries to prove `arm64-v8a` is present and no unwanted ABI is packaged. Verify final APK contents after building.

## Required integration responsibilities

Build a small engine boundary rather than letting Compose screens call Gecko APIs directly. The exact set of delegates must be driven by LastHaven behavior and the pinned API, but review at least:

- `GeckoSession.NavigationDelegate` — navigation requests, redirects and location changes.
- `GeckoSession.ProgressDelegate` — page load progress, security/load state and session-state changes.
- `GeckoSession.ContentDelegate` — page content events, fullscreen requests, crashes and other content lifecycle events.
- `GeckoSession.PermissionDelegate` — site content/media permissions; request Android runtime permissions where the API requires them and ask the user explicitly.
- `GeckoSession.PromptDelegate` — page alerts, confirms, authentication and select prompts.
- Media session/delegate APIs — audio playback lifecycle and media controls as required.
- File selection/download APIs — inspect the pinned API and implement explicit user-mediated handling; do not silently download files.
- Runtime/process/crash callbacks — handle renderer or Gecko process loss without corrupting persistent profile data.

Do not create empty delegates merely to make the code compile if the live game requires a user-facing flow. Do not grant all permissions automatically. Do not log cookies, tokens, authorization headers, full sensitive URLs or page content.

## Profile and session preservation

- Use a persistent, non-private Gecko profile for the LastHaven origin.
- Keep runtime lifetime process-scoped and session lifetime explicit. Preserve the same `GeckoSession` when navigating from gameplay to home/settings and back; do not recreate it or reload the URL solely because a Compose destination changed.
- Investigate Gecko session-state serialization (`onSessionStateChange`, `flushSessionState`, `restoreState`) for session UI/history restoration. Session state is not a substitute for the full browser profile and does not prove cookies/site storage are persisted.
- Verify cookies, LocalStorage, IndexedDB, Cache Storage, service workers, authentication state and storage permissions over force-stop/restart and same-key app update on a physical ARM64 device.
- Do not copy Cefrium profile files into GeckoView. No supported cross-engine migration has been established. Preserve the old Cefrium profile untouched through migration and rollback; explain that users may need to sign in again.
- Do not implement a Clear Data action by deleting profile files. Only use documented, version-matched GeckoView APIs after the session/runtime lifecycle is safely handled.

## LastHaven compatibility test matrix

| Capability | Required verification |
|---|---|
| WebGL / WebGL2 | Game world renders; GPU acceleration works without black/blank surfaces |
| WebAssembly | Game client loads and runs all required WASM modules |
| WebSockets | Login and live game traffic remain connected; reconnect works |
| Touch/input | Tap, drag, long-press, keyboard/IME and game controls work |
| Fullscreen/orientation | Game fullscreen and configured landscape behavior work; home/settings restore system UI |
| Audio | Game audio starts, continues, pauses/resumes and respects Android audio focus |
| Cookies/auth | Login and session survive force-stop/restart as designed |
| LocalStorage/IndexedDB | Game data persists after restart and app update |
| Service workers/cache | Required resources install and remain available; cache invalidation works |
| Permissions/prompts | Every requested capability has an appropriate user-mediated flow |
| Process recovery | Gecko child-process death/background process pressure does not corrupt session/profile |
| Navigation | External links, redirects, errors, back/reload and the configured game origin are handled safely |

Record the physical device model, Android version, GeckoView version, build commit, APK SHA-256 and each test outcome. Emulator success is not a substitute for physical ARM64 testing.

## WebExtension and addon boundary

GeckoView supports browser WebExtensions/content-script mechanisms. These are not World of Warcraft 3.3.5a addon ZIP packages or `.toc` files. No addon manager implementation is currently retained in this repository; the owner will provide a new specification before a replacement is designed. Do not recreate the old manager or implement game injection based on this document.

## Ordered implementation gates

1. Restore a clean beta build and passing unit-test baseline.
2. Complete issue #20 with exact stable version/API/build compatibility evidence and review this document.
3. Implement GeckoView engine replacement under #21 without changing application ID, release signing identity, ARM64-only packaging or updater consent behavior.
4. Verify CI build, tests, package identity, signer, ABI and release artifact contents.
5. Run the physical ARM64 game compatibility matrix above.
6. Only after #21 is integrated, implement the Play/Settings home flow under #22, preserving the live session.
7. Do not begin addon-manager rebuild until the owner supplies its requirements.

## Research log

- 2026-11-25: Checked Mozilla's current GeckoView consumer quick start, architecture guide, API index, `GeckoSession` and `GeckoView` Javadocs, site permissions guide and official Maven repository entry point. Confirmed Runtime/Session/View architecture, stable/beta/nightly channels, Mozilla Maven host, Java 17 compile-compatibility guidance and delegate-based permission/content/navigation responsibilities. Exact stable artifact/version and compatibility with the current LastHavenPlus build remain unverified.

## Current beta source inventory — 2026-11-25

The current `beta` tree was inspected at commit `3b5787752940b42bbf31a8cab361446b06918a2b`. The Cefrium integration surface is small but crosses application initialization, Compose hosting and dependency resolution:

- `app/build.gradle.kts`: applies `com.cefrium` plugin version `0.21.0`; declares `com.cefrium:cefrium-sdk:0.21.0` and `com.cefrium:cefrium-compose:0.21.0`.
- `settings.gradle.kts`: declares the Codeberg Cefrium Maven repository in plugin management and dependency resolution, scoped to `com.cefrium`.
- `LastHavenApplication.kt`: imports `com.cefrium.Cefrium` and configures remote debugging only for debuggable builds.
- `MainActivity.kt`: imports Cefrium browser/navigation/error and Compose APIs; creates remembered Cefrium view state/navigator, hosts `CefriumView`, handles load/renderer errors, reloads and requests `navigator.storage.persist()` after the first completed load.
- The app currently opens the game immediately at `https://lasthavens.com/play`. Home screen/navigation is deliberately separate in #22.

No other app source file in the recursive beta tree imports Cefrium. The migration must remove the plugin, repository entries, dependencies, application initialization and all MainActivity engine-specific APIs. It must retain the current game URL, error/retry behavior, diagnostics, updater callbacks, settings, app-wide usage meter and user-confirmed installer flow.

## Stable artifact investigation — 2026-11-25

- Mozilla's current consumer quick start confirms three channels (Stable, Beta and Nightly), the Mozilla Maven host `https://maven.mozilla.org/maven2/`, and Java 17 source/target compatibility flags because GeckoView uses Java 17 APIs. This is official guidance; the existing CI currently uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and SDK 37. Do not change the toolchain without a compatibility build.
- A Maven index currently lists `org.mozilla.geckoview:geckoview:160.0.20261116130455` and an architecture-specific `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`, both attributed to Mozilla's repository and MPL 2.0. This is a **candidate only**, not yet approved for pinning: the direct Maven POM endpoint could not be parsed by the available retrieval path, so dependency metadata, exact native payload, artifact sizes and any transitive/native packaging relationship still require confirmation.
- Do not assume the generic `geckoview` artifact is safe for this ARM64-only app or that selecting the arm64-named artifact automatically prevents other ABIs from being packaged. Inspect the actual POM/AAR and final APK contents before accepting either.
- Candidate version is not yet written into production Gradle files. No GeckoView dependency has been added.

## Migration start boundary

The first implementation pass should introduce the GeckoView runtime/session host while preserving the current single-screen game-first behavior. Do not mix in the new home screen, navigation redesign or optional features. Keep Cefrium profile files untouched. The existing Cefrium persistence request uses page JavaScript; GeckoView profile persistence must be established through its own documented profile/session lifecycle, not by copying Cefrium data or assuming the same JS request is sufficient.

 
## Versioned API verification — GeckoView 160

The official Mozilla Firefox source tag `FIREFOX_160_0_RELEASE` was inspected directly at `mobile/android/geckoview/api.txt` and the matching Java sources. The public API for this release includes:

- `GeckoRuntime.create(Context, GeckoRuntimeSettings)` and `GeckoRuntimeSettings.Builder.remoteDebuggingEnabled(boolean)`.
- `GeckoSession.open(GeckoRuntime)`, `close()`, `loadUri(String)`, `reload()`, and `isOpen()`.
- `GeckoSession.ProgressDelegate.onPageStart/onPageStop/onSessionStateChange`.
- `GeckoSession.ContentDelegate.onCrash/onKill/onFullScreen/onExternalResponse`.
- `GeckoSession.NavigationDelegate.onLoadRequest` and permission delegates for content, Android and media permissions.
- `GeckoView.setSession(GeckoSession)` and `releaseSession()`.

Versioned source/API: https://github.com/mozilla-firefox/firefox/tree/FIREFOX_160_0_RELEASE/mobile/android/geckoview

The selected Maven artifact is `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`. The Maven index identifies it as Mozilla-published, dated 2026-11-17, MPL 2.0, with 14 listed dependencies. Runtime dependencies include AndroidX Core 1.19.1, Lifecycle 2.11.0, Media3 1.11.1, Play Services FIDO 21.3.1 and SnakeYAML 2.2; the compile dependency includes Kotlin stdlib 2.4.21. Gradle resolution and the final APK size remain subject to CI verification.

Artifact listing and dependency metadata: https://mvnrepository.com/artifact/org.mozilla.geckoview/geckoview-arm64-v8a/160.0.20261116130455

### Profile and storage boundary

The GeckoView 160 public `GeckoRuntime` API does not expose a supported profile-directory getter or a profile-name/path builder. Source inspection shows GeckoThread passes `Context.getFilesDir()` as Gecko's HOME path. The app therefore measures `filesDir` as **persistent app data including GeckoView**, not as an exact profile-only size. Do not guess an internal profile subdirectory or reuse Cefrium's `app_cefrium` path.

### First implementation slice

The first integration removes Cefrium's plugin, dependencies and Maven repository; creates one lazily initialized runtime in the foreground app process; embeds a GeckoSession in GeckoView; handles page load failure, content-process crash/kill, fullscreen notifications, persistent-storage/autoplay permissions for the official game origin, and rejects unrelated site/media permissions by default. The current game-first screen, updater, settings, diagnostics and URL remain in place. Home-screen redesign stays in #22.

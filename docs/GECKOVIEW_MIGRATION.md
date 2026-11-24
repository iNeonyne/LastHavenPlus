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
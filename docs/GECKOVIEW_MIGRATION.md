# GeckoView integration notes

**Current branch:** `beta`  
**Current engine:** Mozilla GeckoView 160, ARM64 artifact  
**Last reviewed:** 2026-11-25

This document records the GeckoView integration as it exists in the current beta source. It is an implementation and verification guide, not a replacement for Mozilla's documentation.

## Official references

- [GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [Quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [Architecture and process model](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [GeckoSession API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.html)
- [GeckoView API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoView.html)
- [Site permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)
- [WebExtensions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [Mozilla Maven repository](https://maven.mozilla.org/maven2/org/mozilla/geckoview/)

Use the API matching the pinned GeckoView artifact. Do not assume the moving `mozilla-central` Javadocs exactly match the release used by this project.

## Current implementation

The beta source has replaced Cefrium with:

`org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`

The app uses a process-scoped `GeckoRuntime`, a `GeckoSession` and a `GeckoView` host. The runtime is initialized lazily in the foreground app process. The game URL remains `https://lasthavens.com/play`.

The app currently launches the game directly. The separate home screen and Play/Settings navigation are assigned to issue #22; do not mix that UI task with engine lifecycle changes.

## Build configuration and identity

The current beta build configuration uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and Android SDK Platform 37.2. GeckoView's AAR metadata requires compile SDK 37.2. Keep the existing pinned toolchain unless a reproducible compatibility issue and a validated change justify otherwise.

The app must remain ARM64-only (`arm64-v8a`). Preserve:

- Application ID: `com.neonyne.lasthavenlauncher`
- Established release signing identity
- User-confirmed update download and Android installer handoff
- Full APK fallback when a differential patch is ineligible or fails validation

The committed Gradle Wrapper is pinned to Gradle 9.7.1 with a distribution checksum. Do not add a second wrapper or change its version without evidence.

## Runtime, session and UI lifecycle

- Keep one runtime per application process.
- Manage the game session lifecycle explicitly.
- When issue #22 adds home/settings navigation, retain the existing session while navigating away from gameplay and back. Do not reload the game solely because the visible Compose destination changes.
- Scope immersive UI, landscape/game orientation and keep-screen-on behavior to gameplay. Home and settings must restore normal system UI.
- Handle navigation, progress/load state, content events, crashes, fullscreen, permissions, prompts, media and external responses according to the pinned API and actual game requirements.
- Do not grant broad site or Android permissions automatically. Keep permission decisions origin-scoped and user-mediated where required.
- Do not log cookies, tokens, authorization headers, full sensitive URLs or page content.

A successful build proves compilation and packaging only. It does not prove gameplay, media, permissions, session persistence or correct lifecycle behavior.

## Profile and data safety

GeckoView uses its own app-private browser data. No supported Cefrium-to-GeckoView profile migration has been established.

- Do not copy Cefrium profile files into GeckoView.
- Do not delete or reset legacy Cefrium profile data as part of this migration.
- Warn that a fresh game login may be required.
- Verify cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication across force-stop/restart and same-key app update on physical ARM64 hardware.
- Do not infer an internal Gecko profile directory from implementation details or report the whole app-private data directory as profile-only size.
- Never clear user data during startup, update or recovery.

## Game compatibility test plan

Record the device model, Android version, GeckoView version, beta commit, APK SHA-256 and result for every physical-device test.

| Area | Required result |
|---|---|
| Launch/login | Game opens and login completes |
| WebGL/WebGL2 | Game world renders without blank/black surfaces |
| WebAssembly | Required game modules load and execute |
| WebSockets | Login and live game traffic remain connected; reconnect works |
| Touch/input | Game controls, drag, keyboard/IME and focus work |
| Fullscreen/orientation | Gameplay presentation works; home/settings restore system UI |
| Audio | Playback and pause/resume work and respect Android audio focus |
| Storage/auth | Cookies and site data persist as intended across restart/update |
| Permissions/prompts | Required flows work and unrelated requests remain denied |
| Process recovery | Renderer/process loss does not corrupt profile/session state |
| Navigation | Reload, Home, Return to game, Back, redirects and errors behave correctly |
| Updater | Compatible differential and full APK fallback install with same app identity/signature |

Emulator and CI results are useful but do not replace physical ARM64 verification.

## Addon boundary

No addon manager implementation is currently included. The former ZIP package manager was removed at the owner's request; issue #23 remains on hold until a new written specification is supplied.

WoW 3.3.5a addon ZIP/`.toc` packages are not Firefox WebExtensions. GeckoView's WebExtension support does not itself provide game addon loading. Do not recreate the removed manager, add package storage or claim game injection without an explicitly approved and independently verified design.

## CI evidence and remaining work

The latest verified successful Android build and unit-test task is [run #310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45508671565), for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`. Its signed release build passed signing-certificate and ARM64-only ABI verification.

Run #308 predates the latest beta HEAD. The subsequent commits through the current documentation cleanup changed documentation/workflow configuration rather than application source. The CI optimization change still requires its own workflow run verification. Check [GitHub Actions](https://github.com/iNeonyne/LastHavenPlus/actions) and `STATUS.md` before making a newer build claim.

Remaining release-readiness work includes:
1. Verify the latest workflow optimization run and retain all build, test, signing and ABI gates.
2. Complete and review issue #22 home/navigation implementation.
3. Perform the full physical ARM64 game, profile, permission, media, navigation and updater test plan.
4. Update `STATUS.md` with exact current commit and CI run evidence.

Do not promote `beta` to `main` or publish a release without explicit owner approval.

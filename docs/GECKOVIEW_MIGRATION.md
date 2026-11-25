# GeckoView integration notes

**Current branch:** `beta`  
**Current engine:** Mozilla GeckoView 160, ARM64 artifact  
**Last reviewed:** 2026-11-25

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

## Build configuration and identity

The current beta build uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and Android SDK Platform 37.2. GeckoView's AAR metadata requires compile SDK 37.2. Keep the pinned toolchain unless a reproducible compatibility issue and validated change justify otherwise.

Preserve:
- ARM64 only (`arm64-v8a`)
- Application ID `com.neonyne.lasthavenlauncher`
- Established release signing identity
- User-confirmed update download and Android installer handoff
- Full APK fallback when a differential patch is ineligible or fails validation

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

Issue #23 is owner-approved for investigation/integration of a supplied XPI addon manager. No implementation is integrated yet. Before any install path is added, verify the pinned GeckoView 160 extension APIs, unsigned XPI behavior, install confirmation, session/tab lifecycle and all archive/network security constraints. A WoW 3.3.5a addon ZIP/`.toc` package is not a Firefox WebExtension; GeckoView WebExtension support does not itself load game addons. Do not claim game addon injection.

## CI evidence and remaining work

- PR #43 exact-head unsigned validation passed in [run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) for `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3`.
- PR #43 merged to beta at `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210`.
- Post-merge signed beta run [#45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) must be checked for final conclusion, artifact, certificate and ABI verification before release readiness is claimed.
- The last previously verified signed ARM64 build was run #330 for older commit `e924c580699ad34f1083d1a9a0fe96901037c0ae`; it does not verify the v2.1.0 candidate.

Remaining work:
1. Inspect exact post-merge signed build and artifact.
2. Complete physical ARM64 game, profile, permission, media, navigation and updater tests.
3. Inspect the adaptive launcher icon in the built APK.
4. Review the beta-to-main promotion diff while preserving stable-branch history and stable documentation policy.

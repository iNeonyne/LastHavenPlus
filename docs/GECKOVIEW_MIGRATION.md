# GeckoView integration notes

**Documented baseline:** `main` / published v2.1.0  
**Current engine:** Mozilla GeckoView 160, ARM64 artifact  
**Last reviewed:** 2026-11-25

This document records the GeckoView integration on stable `main` and the published v2.1.0 baseline. It supplements, but does not replace, Mozilla's documentation. Issue #56 is the source of truth for the requested next-step direct-launch/in-game-overlay redesign; that redesign is not part of the published v2.1.0 baseline.

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

The `main` build uses `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`, a process-scoped `GeckoRuntime`, a `GeckoSession` and a `GeckoView` host. Runtime/session initialization is deferred until Play. The game URL is `https://lasthavens.com/play`.

In the published v2.1.0 baseline, the app opens on a launcher Home screen. Settings does not initialize the game. Android Back from gameplay opens a full-screen menu containing Reload, Settings and Return to game. Settings, diagnostics and update details are full-screen destinations. The release includes responsive Home/settings layouts and adaptive launcher icon resources. This is the current released behavior, not the desired next UX: issue #56 requests direct game launch and a responsive in-game overlay that keeps the game visible.

The existing GeckoSession is retained when its view is detached for launcher navigation. Source and JVM layout-policy tests are integrated; physical session retention and UI behavior still require device verification. Changes made for issue #56 must preserve this same-session guarantee while replacing the Home/full-screen-menu flow.

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
- Keep the existing GeckoSession while navigating to the current Home/settings destinations and back; do not reload solely because the visible Compose destination changes. When issue #56 is implemented, preserve the same session while showing/dismissing the overlay and visiting Settings.
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

Addon-manager work is on hold pending explicit owner approval to resume it. Do not start implementation, add install paths or treat the menu entry in issue #56 as authorization to resume the addon manager. If the owner reopens the work, first verify the pinned GeckoView 160 extension APIs, unsigned XPI behavior, install confirmation, session/tab lifecycle and archive/network security constraints. A WoW 3.3.5a addon ZIP/`.toc` package is not a Firefox WebExtension; GeckoView WebExtension support does not itself load game addons. Do not claim game addon injection.

## CI evidence and remaining work

- The v2.1.0 release is published. Its signed ARM64 APK, update manifest and differential patch are documented in the root README and CHANGELOG.
- Main commit `3726b140df0d1bf15610d034ce29106a6d037282` includes the release-ID-based draft asset cleanup and final-release verification fix.
- Main workflow run [#45621083722](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45621083722) completed successfully. It verified the bounded-input Python tests, signed optimized APK build, JVM unit tests, signing certificate and ARM64 ABI. Because this was a normal main push rather than a release-tag run, differential generation, release publishing, legacy asset cleanup and final release-asset verification were skipped; the fix's release-only path is not yet exercised by this run.
- The separate Android quality workflow has not yet had a confirmed run. Validate it independently before treating lint and its complete test set as a reliable required gate.
- Physical ARM64 verification remains outstanding for the requested issue #56 overlay behavior and the full GeckoView feature matrix. CI does not replace device testing.

Remaining work:
1. Complete issue #56's direct-launch/in-game-overlay redesign without changing the existing GeckoSession/profile or addon-manager scope.
2. Run the separate quality workflow and resolve any SDK, lint or test failures.
3. Complete physical ARM64 game, profile, permission, media, navigation and updater tests; record device, Android version, exact APK digest and results.
4. Review the beta/main divergence before any promotion or branch deletion; do not merge stale branch snapshots wholesale.

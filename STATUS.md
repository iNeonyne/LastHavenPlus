# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest integrated beta HEAD:** `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210`  
**Latest change:** PR [#43](https://github.com/iNeonyne/LastHavenPlus/pull/43) merged into beta.  
**Current phase:** v2.1.0 release candidate integration; documentation reconciliation and exact-commit build verification in progress. No v2.1.0 promotion, tag, APK or published release exists yet.

## Current source state

- Mozilla GeckoView 160 is the embedded browser engine; Cefrium is removed from the production build.
- The app opens on a responsive launcher Home screen. Play lazily starts the game; Settings does not initialize GeckoView.
- Android Back from gameplay opens a full-screen menu with exactly Reload, Settings and Return to game. The persistent top-right Menu button is removed.
- Settings, diagnostics and update details are full-screen pages. The existing GeckoSession is retained while its view is detached for launcher navigation.
- Adaptive launcher icon resources and a resizeable Activity are integrated.
- The updater remains manual: no automatic downloads or installation.
- The owner approved XPI addon-manager work under #23. No addon manager is integrated; GeckoView API compatibility, XPI installation and security review remain open.

## Integration and CI evidence

| Check | Result |
|---|---|
| PR #43 head | `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3` |
| PR #43 validation | [Run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) passed unsigned release-variant build and JVM unit tests |
| PR #43 merge | `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` |
| Post-merge beta build | [Run #45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) was in progress at the time this status was prepared; do not treat it as passed until its conclusion and artifacts are inspected |
| Updater tag validation | PR #41 merged at `1d35e520331ca9ffa3a9b41b21402a3110728001`; its PR validation passed, but exact post-merge run evidence must be checked separately |
| Last known signed ARM64 verification | Run #330 passed for older commit `e924c580699ad34f1083d1a9a0fe96901037c0ae`; it is not evidence for the v2.1.0 candidate |

PR validation is unsigned and does not access release secrets or publish APKs. A successful PR run does not verify signing, APK ABI contents or release artifacts.

## v2.1.0 UI scope

Integrated from PR #43:
- Responsive branded Home screen and adaptive launcher icon resources.
- Removed persistent gameplay Menu button.
- Gameplay menu has Reload, Settings and Return to game only.
- Settings, diagnostics and update details are full-screen pages.
- Responsive layout policy and JVM breakpoint tests.
- GeckoSession retained while the game view is detached for launcher pages.

Not verified yet:
- Adaptive icon as rendered from the built APK.
- Physical resize, split-screen and freeform behavior.
- Back/navigation behavior and session retention on a physical device.
- Gameplay orientation, immersive mode and keep-awake behavior after navigation.
- Compose UI instrumentation tests are not included.

## Device verification

The owner reports that the app and GeckoView work on an ARM64 device. The device model, Android version, exact commit/APK digest and per-feature test results have not been supplied. Record this as an owner-reported smoke test only; do not claim the full GeckoView matrix or the v2.1.0 UI has been tested.

Still unverified on physical ARM64 hardware:
- Game login and gameplay; WebGL/WebGL2, WASM and WebSockets.
- Touch/IME/focus, audio/audio focus, fullscreen and permissions.
- Cookies, LocalStorage, IndexedDB, Cache Storage and service-worker persistence across restart/update.
- Renderer/process recovery and session restoration.
- Same-key update installation, installer permission restoration, differential update and full-APK fallback.
- v2.1.0 launcher icon, responsive resizing, menu and screen navigation.

## Release constraints and blockers

1. Wait for and inspect exact post-merge run #45601558602, including signed APK artifact, signing certificate, package ID and ARM64-only contents.
2. Reconcile documentation against beta source and CI (issue #29).
3. Review the 32 main-only commits/history and preserve intentional stable-branch documentation and release policy; do not overwrite main with a beta snapshot.
4. Complete and record required physical ARM64 testing. The owner's smoke-test report is not a substitute for the feature matrix.
5. Inspect the final promotion diff and exact-main CI before creating the v2.1.0 tag/draft release.

## Open work

- #4/#15: updater regression coverage and physical update verification.
- #5/#21: physical GeckoView/game compatibility and profile verification.
- #20: GeckoView decision/reference record.
- #23: owner-approved XPI addon manager; not implemented.
- #28: storage measurement accuracy.
- #29: documentation reconciliation; this PR addresses it, keep issue open until merge and verification.
- #31: release packaging resilience; confirm remaining acceptance criteria against current workflow.
- #42: launcher redesign; implemented by PR #43, physical UI checks remain.
- #9/#11/#12/#14/#16/#17: remaining accessibility, diagnostics, load-error, connection, gameplay and backup work.

## Branch policy

`beta` is development/integration; `main` is stable. Task PRs target beta; Chaty alone merges beta PRs. Promotion and publication require explicit owner approval. The owner has approved the v2.1.0 promotion/release, but release gates still apply.

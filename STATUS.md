# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest application integration commit:** `854fce1b1e2989cb012ebf5a15d196d91ff7cad7` (PR [#46](https://github.com/iNeonyne/LastHavenPlus/pull/46)).  
**Latest repository maintenance:** PR [#49](https://github.com/iNeonyne/LastHavenPlus/pull/49) merged into beta at `1f661cb6234dcfdfd051d0cb963e739668896a26`.  
**Stable promotion:** PR [#47](https://github.com/iNeonyne/LastHavenPlus/pull/47) merged v2.1.0 candidate into `main` at `c5f6518e57d614da73642b7dc4c8a21e82e6e9b3`. Main subsequently received release-preparation commits; draft/tag/publication state is not asserted by this beta status record.  
**Current phase:** v2.1.0 candidate promoted to `main`; physical ARM64 verification and final release gates remain outstanding. Promotion is not the same as publication.

## Current source state

- Mozilla GeckoView 160 is the embedded browser engine; Cefrium is removed from the production build.
- The app opens on a responsive launcher Home screen. Play lazily starts the game; Settings does not initialize GeckoView.
- Android Back from gameplay opens a full-screen menu with exactly Reload, Settings and Return to game. The persistent top-right Menu button is removed.
- Settings, diagnostics and update details are full-screen pages. The existing GeckoSession is retained while its view is detached for launcher navigation.
- Adaptive launcher icon resources and a resizeable Activity are integrated.
- The updater remains manual: no automatic downloads or installation.
- Addon-manager work under #23 is on hold by owner instruction. No addon manager is integrated; do not resume until explicit owner approval.

## Integration and CI evidence

| Check | Result |
|---|---|
| PR #43 head | `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3` |
| PR #43 validation | [Run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) passed unsigned release-variant build and JVM unit tests |
| PR #43 merge | `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` |
| Post-merge beta build | [Run #45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks, as recorded in merged PR #47 |
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

1. Signed beta evidence for run #45601558602 is recorded in PR #47; artifact SHA-256: `0e1407a809897bd134817f7427bc5dbfea04344345c2ee0d8a4a9ffc945aec8e`; established signing certificate SHA-256: `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263`.
2. PR #47 promoted the candidate to `main` while preserving stable history and stable-facing documentation policy. Do not repeat or overwrite this promotion.
3. Confirm the current `main` release tag/draft/published state and exact-main release workflow evidence before making any release-status claim.
4. Complete and record the required physical ARM64 feature matrix. The owner's earlier smoke-test report is not a substitute for the matrix.
5. Verify the final versioned v2.1.0 APK identity, signature and ARM64-only contents from the exact release-tag build.

## Open work

- #4/#15: updater regression coverage and physical update verification.
- #5/#21: physical GeckoView/game compatibility and profile verification.
- #20: GeckoView decision/reference record.
- #23: addon-manager work on hold by owner instruction; do not resume without explicit approval.
- #28: storage measurement accuracy.
- #31: release packaging resilience; confirm remaining acceptance criteria against current workflow.
- #42: launcher redesign; implemented by PR #43, physical UI checks remain.
- #9/#11/#12/#14/#16/#17: remaining accessibility, diagnostics, load-error, connection, gameplay and backup work.

## Branch policy

`beta` is development/integration; `main` is stable. Task PRs target beta; Chaty alone merges beta PRs. The v2.1.0 candidate was promoted by PR #47. Promotion does not authorize publication; release gates and explicit owner approval still apply.

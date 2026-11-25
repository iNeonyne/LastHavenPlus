# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest application integration commit:** `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` (PR [#43](https://github.com/iNeonyne/LastHavenPlus/pull/43)); PR #46 was documentation-only.  
**Latest workflow integration:** PR [#54](https://github.com/iNeonyne/LastHavenPlus/pull/54) merged to beta at `b12da7651445d15080e7593d441459a8c8403070`; exact-head unsigned and post-merge signed CI both passed.  
**Stable documentation:** owner-authorized v2.1.0 corrections are on `main`; README commit `fdae3d90cf3abb209df8a42dd73ed1667bae9808`, CHANGELOG commit `2abe7cc212b67a3b91f5dcdb108aee3a55559e32`.  
**Stable promotion and publication:** PR [#47](https://github.com/iNeonyne/LastHavenPlus/pull/47) promoted to `main`; tag/release `v2.1.0` is published at `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.  
**Current phase:** v2.1.0 is published. Physical ARM64 feature verification remains outstanding. PR #54 fixed draft asset cleanup/verification by release ID; exact-head unsigned validation and post-merge signed beta validation both passed. The release-ID path still requires a future owner-authorized versioned release run.
**Active workflow task:** issue #60 is being implemented on `chaty/ci-quality-gate`, based on beta HEAD `36f079445e227583c12b19bde9161844e0e5855d`. The branch adds lint/format checks to existing read-only Android PR validation and a post-merge beta quality workflow with incremental Spotless/ktfmt; exact-head CI is pending.

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
| Updater tag validation | PR #41 merged at `1d35e520331ca9ffa3a9b41b21402a3110728001`; v2.1.0 tag creation succeeded in release run #45603533787 |
| v2.1.0 tag release | [Run #45603533787](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45603533787): unit tests, signer, established certificate, ARM64-only ABI, differential generation and upload/publish steps passed; final draft-by-tag lookup failed HTTP 404. Release metadata confirms published assets |

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
3. Investigate and fix the final release-asset query failure in run #45603533787; do not recreate or republish v2.1.0 merely to retry verification.
4. Complete and record the required physical ARM64 feature matrix. The owner's earlier smoke-test report is not a substitute for the matrix.
5. The versioned v2.1.0 APK is published: versionCode 2001000, signed with the established certificate and packaged arm64-v8a only. The workflow verified these checks and uploaded the release assets; its final API lookup failed, while current release metadata confirms the expected APK, manifest and patch.

## Open work

- #60: Android lint and Kotlin formatting quality gate; implementation is in progress on `chaty/ci-quality-gate` (PR #72). Exact-head baseline validation passed on `07fa36c416aee83a64845cedaf67f2d99c359034` in [run #45625221679](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45625221679). The deliberate misformat probe was rejected specifically by Spotless in [run #45627319452](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627319452); that run's overall failure is expected for the negative test, not a product-code failure. The probe was removed in commit `b93be719e510757036c948d23a8aadf1409b95a2`. The existing Android PR workflow handles PR lint/format/build/tests; `quality.yml` runs lint/format only on relevant beta pushes. Final clean-head validation is pending.
- #4/#15: updater regression coverage and physical update verification.
- #5/#21: physical GeckoView/game compatibility and profile verification.
- #20: complete remaining GeckoView 160 artifact/POM/dependency/license/API evidence.
- #23: addon-manager work on hold by owner instruction; do not resume without explicit approval.
- #28: storage measurement accuracy.
- #31: PR #54 merged; exact-head unsigned validation passed in run #45610110307; post-merge signed beta run [#45610518305](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45610518305) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks. Future owner-authorized release run must exercise draft-ID cleanup/verification and differential fallback.
- #42: v2.1.0 launcher UI verification; source implementation is integrated by PR #43, physical UI checks remain.
- #22: Home/Play/Settings source implementation closed as complete; physical navigation/session checks remain under #5/#42.
- #9/#11/#12/#14/#16/#17: remaining accessibility, diagnostics, load-error, connection, gameplay and backup work.
- Stable README/CHANGELOG are reconciled on main; no release was created or republished during housekeeping.

## Branch policy

`beta` is development/integration; `main` is stable. Task PRs target beta; Chaty alone merges beta PRs. The v2.1.0 candidate was promoted by PR #47 and published as tag/release `v2.1.0` by the owner-approved release workflow. Do not create another tag or republish without explicit owner approval.

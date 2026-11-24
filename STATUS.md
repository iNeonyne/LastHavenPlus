# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Phase:** P0 — Restore a clean beta build, then verify the playable core

## Snapshot

The stable `v1.0.0` release is published from the stable line. The current `beta` source is not buildable at its latest commit: GitHub Actions run #232 fails Kotlin compilation in `AddonManagerDialog.kt`. The unit-test task did not run and no test-report artifact was produced. Do not treat the published APK or an older successful beta run as verification of the current beta source.

- **`main` (stable):** `6211cf389b8d9f6ef98aa7818f47643bfdaf2053`
- **`beta` audit head before this status refresh:** `6db0eb9613b86d0d2a62bd78d95fa7f09fb432c4` (documentation/workflow-only commits since the failed source build)
- **Latest fully inspected source-build failure:** [run #232](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45484304840), failed on source commit `4b4a77293a14648974c78a49824a84a33e2a3e19`. Later documentation-only pushes triggered replacement CI runs; they do not change the source failure described below.
- **Latest successful beta CI run found:** [run #179](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45183038304), commit `593fec6b048790cbc30a7fbadf571b749069abea`. This used an earlier workflow/source snapshot and does **not** verify current beta.
- **Latest verified CI commit:** `593fec6b048790cbc30a7fbadf571b749069abea` (run #179; historical only).
- **Current release:** [v1.0.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v1.0.0), with one `lasthavenplus-arm64.apk` and `lasthavenplus-update.json`. Its existence does not prove current beta readiness.

## Verification status

| Area | Status | Evidence / limitations |
|---|---|---|
| Application identity | Implemented; configuration inspected | `applicationId = com.neonyne.lasthavenlauncher`; preserve it |
| Release signing identity | Configured; current beta artifact unverified | CI expects certificate SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263`; run #232 restored the keystore but compilation failed before APK signing verification |
| ARM64-only packaging | Configured; current beta artifact unverified | `abiFilters += "arm64-v8a"`; workflow checks APK native libraries after build, but run #232 skipped that check |
| Gradle Wrapper | Implemented and wrapper-validation step passed | `gradlew` is executable; wrapper pins Gradle 9.7.1 and distribution SHA-256 `acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a`; no toolchain change needed |
| Cefrium engine | Configured; runtime unverified | SDK, Compose wrapper and plugin pinned to 0.21.0; Chromium 156.0.8078.96 |
| Game launch | Implemented; device unverified | `CefriumView` loads the configured LastHaven URL; no physical ARM64 gameplay test recorded |
| Browser profile persistence | Default persistent profile path used; persistence unverified | No custom ephemeral profile is configured; source requests `navigator.storage.persist()`; actual cookies/LocalStorage/IndexedDB/service-worker retention needs device testing |
| Exit menu/navigation | Implemented; device unverified | Back opens menu with Return, Reload, Settings & usage, Diagnostics, Addon manager, update check and Exit |
| Settings/storage measurements | Implemented; device unverified | Reports installed APK bytes, Cefrium profile directory size when present, and app/code cache size; this is a usage screen, not yet a full preferences surface |
| Network usage | Implemented; device unverified | Android per-UID RX/TX since boot and app-session baseline; includes game and updater traffic, not game-only |
| Diagnostics | Implemented; test/runtime verification incomplete | Bounded local rotating log, viewer/share/clear UI and redaction patterns exist; current CI never reached tests |
| Updater | Implemented in source; CI/runtime unverified | Stable release startup/manual checks, explicit download choices, digest and package/version/signing checks, Android installer handoff; no automatic download/install |
| Differential update | Implemented in source; compatibility unverified | Patch offered only for matching base version/versionCode and installed APK digest; reconstructed APK digest and signing identity checked; full APK remains selectable fallback |
| ZIP addon manager | Local package management implemented; game integration absent | ZIP inspection, metadata, local library, import/export/remove and local enabled flags; no extraction into or injection into the game runtime |
| Physical-device gameplay | Not verified | No evidence of testing on ARM64 hardware |
| Release readiness | Blocked | Current beta build fails; current signed APK/ABI checks skipped; device and update-path checks remain outstanding |

## Current CI failure

Run #232 — commit `4b4a77293a14648974c78a49824a84a33e2a3e19`:

- Wrapper setup and wrapper validation: passed.
- JDK 25, Android SDK 37, version metadata and signing-key restore: passed.
- `compileDebugKotlin` and `compileReleaseKotlin`: failed.
- Errors:
  - `AddonManagerDialog.kt:166:77` — unresolved reference `contentDescription`.
  - `AddonManagerDialog.kt:171:45` — `Switch` has no parameter named `contentDescription`.
- Unit tests: not run.
- Unit-test reports: none found; no report artifact uploaded.
- Signing-certificate verification, ABI verification and release APK artifact upload: skipped.
- Differential package generation and release publication: skipped.

The compile error is in the active #13 accessibility-semantics scope. Keep exclusive ownership: the current owner must correct and hand off that task; Chaty reviews and integrates the resulting commit. Do not have two agents work on #13 concurrently.

## Release workflow finding

**Corrected on beta.** `.github/workflows/android.yml` now restricts manual release-tag creation to `main` and rejects pushed version tags unless their commit is already contained in stable `main`. The workflow was not dispatched for a release; no tag or release was created.

## Tests and artifacts

- The latest CI attempt did not execute unit tests because Kotlin compilation failed first.
- Existing test source includes BSDIFF patch tests, ZIP archive-inspector tests and diagnostics redaction tests. Their presence is not evidence that they pass on current beta.
- Run #232 has zero artifacts.
- Older successful run #179 uploaded demo APK artifacts under its former workflow; those artifacts are not release evidence for current beta.
- The published v1.0.0 release has exactly one APK asset plus its update manifest; no debug APK is listed as a release asset.

## Release blockers

1. Fix the current Kotlin compilation errors in the exclusively owned #13 task and rerun beta CI.
2. Obtain a clean current-beta signed release build, unit-test reports, ARM64 ABI verification and signing-certificate verification.
3. Add and pass updater tests for malformed manifests, version/versionCode handling, digest mismatch, incompatible base APK, interrupted downloads and patch failure/fallback.
4. Restrict manual release-tag creation to `main`.
5. Perform physical ARM64 testing for game launch, gameplay, profile persistence, orientation/audio/GPU behavior and same-key update installation.
6. Verify differential update behavior between two compatible signed releases on a physical ARM64 device.
7. Keep addon injection explicitly out of scope until the operator-approved runtime integration contract is established; local ZIP package management must not be described as game integration.

## Branch and integration workflow

- `beta` is the active development, integration and testing branch.
- `main` is the stable release branch.
- Contributors may work in parallel only on separate issues with disjoint file ownership. Never work on the same issue concurrently.
- Contributors hand Chaty a task-branch commit SHA and verification report. Chaty alone reviews, integrates and pushes commits directly to `beta`. No pull requests.
- Promote `beta` to `main` only after release gates pass and the user explicitly approves.
- Do not create tags, publish releases, or change application ID/signing identity during routine development.

## Active work and handoff

| Owner | Issue | Scope / boundary |
|---|---|---|
| Chaty | #4 | Build reproducibility and updater test coverage; wrapper is already present, so focus on remaining tests and current CI evidence |
| Ploy | #13 | Accessibility semantics; exclusive owner until task handoff. The current compiler errors are within this scope. No task branch or handoff commit is visible in GitHub yet |
| Ploy (queued) | #11, then #8 | Sequential only after each prior task is accepted and integrated into beta |
| Chaty (queued) | #19 | Beta updater channel; only after #4 handoff |

## Next actions

1. Correct the #13 Compose semantics compile error under its single-owner assignment; hand off the task-branch commit to Chaty.
2. Chaty reviews and integrates the fix directly into beta, then verifies a new CI run.
3. Update #4 acceptance tracking: wrapper setup is complete; updater edge-case coverage and current successful CI evidence remain open.
4. Keep the `main`-only release-tag guard in place; verify its behavior in a normal CI run without dispatching a release.
5. Keep physical-device and differential-update checks open until actual ARM64 hardware verification is recorded.

# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest inspected beta HEAD before this status update:** `8231c43bde8bac2606c785605ce818209f753f07` (documentation and workflow updates only)  
**Phase:** GeckoView 160 is integrated in source and has a successful CI build/test baseline. Home-screen/navigation work is assigned to Ploy. Physical ARM64 gameplay and profile persistence remain unverified.

## Summary

- `beta` is the active development, integration and testing branch. `main` remains the stable release branch.
- Beta source uses Mozilla GeckoView 160; Cefrium has been removed from the production build.
- The app currently opens the game directly. The dedicated home screen and Reload / Home / Return to game flow are assigned to Ploy under issue #22.
- The previous ZIP addon manager was removed. Replacement work is on hold pending a new owner specification.
- No physical-device test is claimed. CI success does not establish game compatibility or persistent browser profile behavior.

## Branch and CI evidence

| Item | Verified value |
|---|---|
| Active branch | `beta` |
| Stable branch | `main` |
| Latest inspected beta HEAD before this status update | `8231c43bde8bac2606c785605ce818209f753f07` |
| Latest successful Android CI run inspected | [#310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45508671565) |
| Commit tested by run #310 | `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21` |
| CI result | `assembleRelease`, `testDebugUnitTest`, release signature verification and ARM64-only ABI verification succeeded |
| Signed APK artifact | `lasthavenplus-release-signed-arm64` uploaded; workflow job passed |
| Earlier measured APK size | Run #308: 92,850,795 bytes |
| PR workflow change | Run [#311](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45511064839) is testing the workflow commit; no PR-triggered run has yet verified the new PR-only job |
| Release state | No promotion to `main` or new release publication performed |

Run #310 is the latest successful Android run inspected. It verifies the signed ARM64 release build and unit tests for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`, not the current documentation/workflow HEAD. Its total elapsed time was 3m44s, compared with 5m02s for run #308; this is an observed one-run comparison, not a controlled benchmark. Run #311 is validating the updated workflow on a push. The PR-only job is correctly skipped for that push and remains unverified until an actual PR triggers it.

## Implemented in beta source

| Area | Source state | Verification boundary |
|---|---|---|
| Browser engine | GeckoView 160 ARM64 artifact configured; Cefrium removed from production source/build | CI run #310 built and verified a later beta commit; the latest documentation/workflow commits still need their own checks |
| Runtime/session | Lazy process-scoped GeckoRuntime, GeckoSession and GeckoView host | Source inspected; lifecycle and persistence on hardware unverified |
| Game launch | Loads `https://lasthavens.com/play` directly at app launch | CI build evidence exists for run #310; real gameplay unverified |
| Navigation menu | Existing compact menu includes Reload, Return to game, Settings, diagnostics, update and Exit paths | Physical navigation/accessibility unverified; Home destination is part of #22 |
| Settings/storage | Shows APK size, persistent app-private data size and cache size separately | Source inspected; physical values unverified; app-private data is not an exact Gecko profile-only measurement |
| Network usage | Android per-UID received/sent byte counters and app-session baseline | Includes game and updater traffic; physical readings unverified |
| Diagnostics | Bounded local logs, viewer/share/clear and redaction logic | Source inspected; expanded tests and hardware behavior remain |
| Updater | Stable startup/manual checks, explicit download choice and Android installer handoff | Source inspected; physical install flow remains unverified |
| Differential updater | Checks base version/digest, reconstructs and verifies APK; full APK fallback exists | Source inspected; current-beta edge-case coverage and physical update remain |
| Packaging | Gradle Wrapper pinned to Gradle 9.7.1; app configured for ARM64 only | Wrapper validation and ARM64-only APK verification passed in run #310 |
| Application identity | `com.neonyne.lasthavenlauncher` | Preserve unchanged |
| Release signing | Established signing identity configured in CI | Matched in run #310 for its tested commit |
| Addon manager | No implementation present; legacy ZIP manager removed | Replacement prohibited until owner supplies a new specification |

## Incomplete and unverified

- GeckoView gameplay on real ARM64 hardware: WebGL/WebAssembly, WebSockets, touch/input, fullscreen, audio, login and reconnect.
- GeckoView cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication persistence across force-stop/restart and same-key update.
- GeckoView process recovery, permissions, prompts, media and external-response handling under real game behavior.
- Full home-screen and navigation flow under issue #22.
- Physical settings/storage values, network counters and diagnostic behavior.
- Physical differential update and full APK fallback installation.
- Accessibility/TalkBack and focus order on physical hardware.
- Additional updater edge-case tests tracked by issue #4.

## CI failures and missing evidence

- Earlier run #302 failed AAR metadata validation because GeckoView 160 requires compile SDK 37.2. The project and workflow were adjusted to use API 37.2.
- Run #308 subsequently passed `assembleRelease` and `testDebugUnitTest` for commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; APK ABI and signing checks passed.
- Run #310 completed successfully after the CI runner-time optimization. Its total elapsed time was 3m44s versus 5m02s for run #308; treat this as an observed comparison only, not a controlled performance benchmark.
- Run #311 is in progress for the PR-trigger workflow update. Its push event skips the PR-only job; an actual PR run is still required to verify that path.
- A passing unit-test task on run #308 does not prove every runtime path is covered or that physical-device behavior works.

## Active ownership

| Owner | Issue/task | Scope |
|---|---|---|
| Ploy | #22 — home screen and navigation | Home, Play, Settings, Reload, Home and Return to game UI plus directly related tests |
| Chaty | Integration and release coordination | Review/handoff, central documentation, CI verification and release gates |
| Chaty | #4 — build/updater verification | Confirm workflow optimization run and continue updater test coverage |
| Owner hold | #23 — addon manager | No work until a new written specification is provided |

Chaty must not edit Ploy's assigned #22 UI files while Ploy owns the task. Any ownership change requires an explicit handoff and an update to this table and `AGENT_WORK.md`.

## Pull request validation

PRs targeting `beta` now have a dedicated CI job that builds the unsigned release variant and runs unit tests with read-only permissions. It does not access signing secrets or publish APKs. The normal branch pipeline remains responsible for signed release, certificate, ARM64 ABI and artifact verification. The updated workflow is being checked by push run #311. That event skips the PR-only job, so PR-specific behavior remains unverified until an actual pull request triggers it.

## Release blockers

1. Complete and review the #22 home/navigation implementation and its automated checks.
2. Verify the current CI workflow and retain build, unit-test, signing, ABI and artifact checks.
3. Complete the physical ARM64 game compatibility and persistent-profile test plan.
4. Verify updater installation and differential/full APK fallback on hardware.
5. Reconcile all release notes and status against the exact candidate commit before asking for release approval.

Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Next actions

2. Review Ploy's #22 PR, inspect the exact diff and tests, and merge it into `beta` only after required checks pass.
3. Arrange real ARM64 device testing and record the device, Android version, tested commit, APK SHA-256 and individual results.
4. Update this file with the exact latest verified commit and CI run after new evidence exists.

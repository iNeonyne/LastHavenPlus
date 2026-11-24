# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Audit snapshot:** `54273f6ae5cb337d103c5a27a511057138c1c4c1` (the beta HEAD immediately before this status-report commit)  
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
| Beta audit snapshot before this report commit | `54273f6ae5cb337d103c5a27a511057138c1c4c1` |
| Latest successful Android CI run inspected | [#308](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45503025246) |
| Commit tested by run #308 | `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469` |
| CI result | `assembleRelease` and `testDebugUnitTest` succeeded |
| Release APK | 92,850,795 bytes; ARM64 (`arm64-v8a`) only |
| Signing | APK certificate matched the established release signing certificate |
| Current workflow optimization | Commits `54b947279a783c6c382210e65045edd02bd5ee73` and `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`; resulting run has not been verified in this audit |
| Release state | No promotion to `main` or new release publication performed |

Run #308 is the latest successful run inspected, not a build of the current documentation/workflow HEAD. Its test task passing is evidence for that tested commit only. The later workflow optimization needs its own run inspection and timing comparison before any improvement is claimed.

## Implemented in beta source

| Area | Source state | Verification boundary |
|---|---|---|
| Browser engine | GeckoView 160 ARM64 artifact configured; Cefrium removed from production source/build | CI run #308 built a prior beta commit; current workflow changes still need verification |
| Runtime/session | Lazy process-scoped GeckoRuntime, GeckoSession and GeckoView host | Source inspected; lifecycle and persistence on hardware unverified |
| Game launch | Loads `https://lasthavens.com/play` directly at app launch | CI build evidence exists for run #308; real gameplay unverified |
| Navigation menu | Existing compact menu includes Reload, Return to game, Settings, diagnostics, update and Exit paths | Physical navigation/accessibility unverified; Home destination is part of #22 |
| Settings/storage | Shows APK size, persistent app-private data size and cache size separately | Source inspected; physical values unverified; app-private data is not an exact Gecko profile-only measurement |
| Network usage | Android per-UID received/sent byte counters and app-session baseline | Includes game and updater traffic; physical readings unverified |
| Diagnostics | Bounded local logs, viewer/share/clear and redaction logic | Source inspected; expanded tests and hardware behavior remain |
| Updater | Stable startup/manual checks, explicit download choice and Android installer handoff | Source inspected; physical install flow remains unverified |
| Differential updater | Checks base version/digest, reconstructs and verifies APK; full APK fallback exists | Source inspected; current-beta edge-case coverage and physical update remain |
| Packaging | Gradle Wrapper pinned to Gradle 9.7.1; app configured for ARM64 only | Wrapper validation passed in earlier CI; run #308 APK confirmed ARM64-only |
| Application identity | `com.neonyne.lasthavenlauncher` | Preserve unchanged |
| Release signing | Established signing identity configured in CI | Matched in run #308 for its tested commit |
| Addon manager | No implementation present; legacy ZIP manager removed | Replacement prohibited until owner supplies a new specification |

## Incomplete and unverified

- GeckoView gameplay on real ARM64 hardware: WebGL/WebAssembly, WebSockets, touch/input, fullscreen, audio, login and reconnect.
- GeckoView cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication persistence across force-stop/restart and same-key update.
- GeckoView process recovery, permissions, prompts, media and external-response handling under real game behavior.
- Full home-screen and navigation flow under issue #22.
- Physical settings/storage values, network counters and diagnostic behavior.
- Physical differential update and full APK fallback installation.
- Accessibility/TalkBack and focus order on physical hardware.
- CI optimization run and measured before/after runner duration.
- Additional updater edge-case tests tracked by issue #4.

## CI failures and missing evidence

- Earlier run #302 failed AAR metadata validation because GeckoView 160 requires compile SDK 37.2. The project and workflow were adjusted to use API 37.2.
- Run #308 subsequently passed `assembleRelease` and `testDebugUnitTest` for commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; APK ABI and signing checks passed.
- The workflow optimization commits have not yet been validated by an inspected run. Do not report the optimization as successful or faster until the associated run is checked.
- A passing unit-test task on run #308 does not prove every runtime path is covered or that physical-device behavior works.

## Active ownership

| Owner | Issue/task | Scope |
|---|---|---|
| Ploy | #22 — home screen and navigation | Home, Play, Settings, Reload, Home and Return to game UI plus directly related tests |
| Chaty | Integration and release coordination | Review/handoff, central documentation, CI verification and release gates |
| Chaty | #4 — build/updater verification | Confirm workflow optimization run and continue updater test coverage |
| Owner hold | #23 — addon manager | No work until a new written specification is provided |

Chaty must not edit Ploy's assigned #22 UI files while Ploy owns the task. Any ownership change requires an explicit handoff and an update to this table and `AGENT_WORK.md`.

## Release blockers

1. Complete and review the #22 home/navigation implementation and its automated checks.
2. Verify the current CI workflow and retain build, unit-test, signing, ABI and artifact checks.
3. Complete the physical ARM64 game compatibility and persistent-profile test plan.
4. Verify updater installation and differential/full APK fallback on hardware.
5. Reconcile all release notes and status against the exact candidate commit before asking for release approval.

Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Next actions

1. Inspect the Actions run for the CI optimization commits; compare elapsed time with run #308 without weakening gates.
2. Wait for Ploy's #22 handoff, review the exact diff and tests, then integrate directly into `beta`.
3. Arrange real ARM64 device testing and record the device, Android version, tested commit, APK SHA-256 and individual results.
4. Update this file with the exact latest verified commit and CI run after new evidence exists.

# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest inspected beta HEAD:** `c8eb02a265012d7be898277443a53dd1e3589cad`  
**Phase:** GeckoView 160 is integrated in beta. Chaty's #22/#4 implementation in [PR #26](https://github.com/iNeonyne/LastHavenPlus/pull/26) passed PR build/unit-test CI on code SHA `283d649cd31ae009889d76affb25f655e28d3949`; review/merge and post-merge signed CI remain. Physical ARM64 gameplay, profile persistence and update installation remain unverified.

## Summary

- `beta` is the active development, integration and testing branch. `main` remains the stable release branch.
- Beta source uses Mozilla GeckoView 160; Cefrium has been removed from the production build.
- The current beta still opens the game directly. Chaty's PR #26 adds the home screen and Reload / Home / Return to game flow; it is not integrated until CI passes and the PR is reviewed.
- The previous ZIP addon manager was removed. Replacement work is on hold pending a new owner specification.
- No physical-device test is claimed. CI success does not establish game compatibility or persistent browser profile behavior.

## Branch and CI evidence

| Item | Verified value |
|---|---|
| Active branch | `beta` |
| Stable branch | `main` |
| Latest inspected beta HEAD | `c8eb02a265012d7be898277443a53dd1e3589cad` |
| Latest successful Android CI run inspected | [#310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45508671565) |
| Commit tested by run #310 | `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21` |
| CI result | `assembleRelease`, `testDebugUnitTest`, release signature verification and ARM64-only ABI verification succeeded |
| Signed APK artifact | `lasthavenplus-release-signed-arm64` uploaded; workflow job passed |
| Earlier measured APK size | Run #308: 92,850,795 bytes |
| PR workflow change | Run [#311](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45511064839) succeeded on a push and skipped PR validation; PR run [#317](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45515320999) passed for code SHA `283d649cd31ae009889d76affb25f655e28d3949` |
| Release state | No promotion to `main` or new release publication performed |

Run #310 is the latest successful Android run inspected. It verifies the signed ARM64 release build and unit tests for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`, not the current documentation/workflow HEAD. Its total elapsed time was 3m44s, compared with 5m02s for run #308; this is an observed one-run comparison, not a controlled benchmark. Run #311 is validating the updated workflow on a push. The PR-only job is correctly skipped for that push and remains unverified until an actual PR triggers it.

## Implemented in beta source

| Area | Source state | Verification boundary |
|---|---|---|
| Browser engine | GeckoView 160 ARM64 artifact configured; Cefrium removed from production source/build | CI run #310 built and verified a later beta commit; the latest documentation/workflow commits still need their own checks |
| Runtime/session | Current beta creates the process-scoped GeckoRuntime/GeckoSession at startup; PR #26 defers initialization until Play and retains the session across Home | PR source compiled; lifecycle and persistence on hardware unverified |
| Game launch | Current beta loads `https://lasthavens.com/play` directly at app launch | CI build evidence exists for run #310; real gameplay unverified; PR #26 changes startup to Home |
| Navigation menu | Current beta menu includes Reload, Return to game, Settings, diagnostics, update and Exit paths | PR #26 adds Home and Return to game navigation; final CI and physical navigation remain unverified |
| Settings/storage | Shows APK size, persistent app-private data size and cache size separately | Source inspected; physical values unverified; app-private data is not an exact Gecko profile-only measurement |
| Network usage | Android per-UID received/sent byte counters and app-session baseline | Includes game and updater traffic; physical readings unverified |
| Diagnostics | Bounded local logs, viewer/share/clear and redaction logic | Source inspected; expanded tests and hardware behavior remain |
| Updater | Stable startup/manual checks, explicit download choice and Android installer handoff | Source inspected; physical install flow remains unverified |
| Differential updater | Current beta checks base version/digest and verifies reconstructed APK; PR #26 adds full APK fallback after any delta failure, including final package validation | PR #26 tests are awaiting CI; physical differential/full update remains unverified |
| Packaging | Gradle Wrapper pinned to Gradle 9.7.1; app configured for ARM64 only | Wrapper validation and ARM64-only APK verification passed in run #310 |
| Application identity | `com.neonyne.lasthavenlauncher` | Preserve unchanged |
| Release signing | Established signing identity configured in CI | Matched in run #310 for its tested commit |
| Addon manager | No implementation present; legacy ZIP manager removed | Replacement prohibited until owner supplies a new specification |

## Incomplete and unverified

- GeckoView gameplay on real ARM64 hardware: WebGL/WebAssembly, WebSockets, touch/input, fullscreen, audio, login and reconnect.
- GeckoView cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication persistence across force-stop/restart and same-key update.
- GeckoView process recovery, permissions, prompts, media and external-response handling under real game behavior.
- PR #26 home-screen/navigation implementation passed code build/unit-test CI; integration/review remains pending.
- Physical settings/storage values, network counters and diagnostic behavior.
- Physical differential update and full APK fallback installation.
- Accessibility/TalkBack and focus order on physical hardware.
- New updater fallback/version/digest/URL tests in PR #26 passed CI; manifest/network integration and physical update behavior still need verification.

## CI failures and missing evidence

- Earlier run #302 failed AAR metadata validation because GeckoView 160 requires compile SDK 37.2. The project and workflow were adjusted to use API 37.2.
- Run #308 subsequently passed `assembleRelease` and `testDebugUnitTest` for commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; APK ABI and signing checks passed.
- Run #310 completed successfully after the CI runner-time optimization. Its total elapsed time was 3m44s versus 5m02s for run #308; treat this as an observed comparison only, not a controlled performance benchmark.
- Run #311 completed successfully on a beta push and skipped the PR-only job. PR run #317 then passed the unsigned release build and unit tests for PR #26 code SHA `283d649cd31ae009889d76affb25f655e28d3949`.
- A passing unit-test task on run #308 does not prove every runtime path is covered or that physical-device behavior works.

## Active ownership

| Owner | Issue/task | Scope |
|---|---|---|
| Chaty | #22 — home screen and navigation | Owns implementation in PR #26; code build/unit-test CI passed, review/merge pending |
| Chaty | #4 — build/updater verification | Fallback/version/digest/URL tests passed in PR #317; additional edge cases and hardware update verification remain |
| Ploy | No active assignment | Do not start work until Chaty explicitly assigns a separate issue |
| Owner hold | #23 — addon manager | No work until a new written specification is provided |

Per the owner's instruction, Ploy has no active assignment on #22. Chaty owns the implementation and review; any future Ploy work requires a separate explicit assignment.

## Pull request validation

PRs targeting `beta` now have a dedicated CI job that builds the unsigned release variant and runs unit tests with read-only permissions. It does not access signing secrets or publish APKs. The normal branch pipeline remains responsible for signed release, certificate, ARM64 ABI and artifact verification. Push run #311 succeeded but skipped the PR-only job. PR #26 runs #313–#316 were cancelled by follow-up commits. Run #317 passed the unsigned release build and unit tests for code SHA `283d649cd31ae009889d76affb25f655e28d3949`. Signed release, signer and ARM64 ABI checks were skipped as intended on the PR event and must be verified after merge. Documentation-only follow-ups are tracked in PR #26; the current PR head must have green validation checks before merge.

## Release blockers

1. Complete CI and review for Chaty's PR #26 (#22 home/navigation and #4 updater hardening).
2. After merge to beta, verify the signed ARM64 branch build, package identity, signing certificate and artifact checks.
3. Complete the physical ARM64 game compatibility and persistent-profile test plan.
4. Verify updater installation and differential/full APK fallback on hardware.
5. Reconcile all release notes and status against the exact candidate commit before asking for release approval.

Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Next actions

1. Complete final PR #26 diff review and merge to beta only while required checks are green.
2. Verify the exact beta HEAD and signed ARM64 artifact; do not publish a release.
3. Arrange real ARM64 device testing and record the device, Android version, tested commit, APK SHA-256 and individual results.

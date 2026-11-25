# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest integrated beta HEAD:** `e924c580699ad34f1083d1a9a0fe96901037c0ae`  
**Phase:** PR #26 is merged into beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. It integrates the home/navigation flow, updater fallback and P0 hardening. Exact-head PR run #329 passed the unsigned release build and unit tests for `3a1396f7364be1e6aa658b3eb67be41549a906fb`. The merge commit's signed branch CI has not yet been verified. Physical ARM64 gameplay, profile persistence and update installation remain unverified. `main` and `beta` are still diverged (beta ahead 166, behind 9); promotion is blocked pending reconciliation and release gates.

## Summary

- `beta` is the active development, integration and testing branch. `main` remains the stable release branch.
- Beta source uses Mozilla GeckoView 160; Cefrium has been removed from the production build.
- Beta now opens the launcher Home screen. Play lazily starts GeckoView; Settings does not initialize the game. The in-game menu provides Reload, Home and Return to game. The GeckoSession is retained across Home and closed when the Activity composition is destroyed.
- The previous ZIP addon manager was removed. Replacement work is on hold pending a new owner specification.
- No physical-device test is claimed. CI success does not establish game compatibility or persistent browser profile behavior.

## Branch and CI evidence

| Item | Verified value |
|---|---|
| Active branch | `beta` |
| Stable branch | `main` |
| Latest integrated beta HEAD | `e924c580699ad34f1083d1a9a0fe96901037c0ae` |
| Latest successful PR validation | [#329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) — unsigned build and unit tests for PR head `3a1396f7364be1e6aa658b3eb67be41549a906fb` |
| Commit tested by run #310 | `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21` |
| CI result | `assembleRelease`, `testDebugUnitTest`, release signature verification and ARM64-only ABI verification succeeded |
| Signed APK artifact | `lasthavenplus-release-signed-arm64` uploaded; workflow job passed |
| Earlier measured APK size | Run #308: 92,850,795 bytes |
| Post-merge signed CI | Not yet verified for merge commit `e924c580699ad34f1083d1a9a0fe96901037c0ae`; PR run #329 is unsigned and does not satisfy this gate |
| Release state | No promotion to `main` or new release publication performed |

PR run #329 completed successfully for the exact PR head `3a1396f7364be1e6aa658b3eb67be41549a906fb`: unsigned release build and unit tests passed. It does not include signing secrets, signed ARM64 packaging or release artifact checks. Run #310 remains historical evidence only for its older beta commit. The exact merge commit still needs signed branch CI verification.

## Implemented in beta source

| Area | Source state | Verification boundary |
|---|---|---|
| Browser engine | GeckoView 160 ARM64 artifact configured; Cefrium removed from production source/build | CI run #310 built and verified a later beta commit; the latest documentation/workflow commits still need their own checks |
| Runtime/session | Process-scoped GeckoRuntime; GeckoSession is created on Play, retained across Home and closed when Activity composition is destroyed | PR build passed; hardware lifecycle/persistence unverified |
| Game launch | Home appears first; Play loads `https://lasthavens.com/play` | PR build passed; real gameplay unverified |
| Navigation menu | Reload, Home, Return to game, Settings, diagnostics, manual update check and Exit | PR build passed; physical navigation/accessibility unverified |
| Settings/storage | Shows APK size, persistent app-private data size and cache size separately | Source inspected; physical values unverified; app-private data is not an exact Gecko profile-only measurement |
| Network usage | Android per-UID received/sent byte counters and app-session baseline | Includes game and updater traffic; physical readings unverified |
| Diagnostics | Bounded local logs, viewer/share/clear and redaction logic | Source inspected; expanded tests and hardware behavior remain |
| Updater | Stable startup/manual checks, explicit download choice and Android installer handoff | Source inspected; physical install flow remains unverified |
| Differential updater | Base identity/digest checks, bounded BSDIFF output, final APK verification and full APK fallback after delta failure | PR build/tests passed; physical update remains unverified |
| Packaging | Gradle Wrapper pinned to Gradle 9.7.1; app configured for ARM64 only | Wrapper validation and ARM64-only APK verification passed in run #310 |
| Application identity | `com.neonyne.lasthavenlauncher` | Preserve unchanged |
| Release signing | Established signing identity configured in CI | Matched in run #310 for its tested commit |
| Addon manager | No implementation present; legacy ZIP manager removed | Replacement prohibited until owner supplies a new specification |

## Incomplete and unverified

- GeckoView gameplay on real ARM64 hardware: WebGL/WebAssembly, WebSockets, touch/input, fullscreen, audio, login and reconnect.
- GeckoView cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication persistence across force-stop/restart and same-key update.
- GeckoView process recovery, permissions, prompts, media and external-response handling under real game behavior.
- PR #26 home-screen/navigation implementation is merged into beta. PR run #329 passed unsigned build and unit tests; physical behavior remains unverified.
- Physical settings/storage values, network counters and diagnostic behavior.
- Physical differential update and full APK fallback installation.
- Accessibility/TalkBack and focus order on physical hardware.
- Updater fallback/version/digest/URL tests passed in PR run #329. Manifest/network integration and physical update behavior still need verification.

## CI failures and missing evidence

- Earlier run #302 failed AAR metadata validation because GeckoView 160 requires compile SDK 37.2. The project and workflow were adjusted to use API 37.2.
- Run #308 subsequently passed `assembleRelease` and `testDebugUnitTest` for commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; APK ABI and signing checks passed.
- Run #310 completed successfully after the CI runner-time optimization. Its total elapsed time was 3m44s versus 5m02s for run #308; treat this as an observed comparison only, not a controlled performance benchmark.
- PR run #329 completed successfully for PR #26 head `3a1396f7364be1e6aa658b3eb67be41549a906fb`: unsigned release build and unit tests passed. The merge commit is `e924c580699ad34f1083d1a9a0fe96901037c0ae`; signed branch CI is still outstanding.
- A passing unit-test task on run #308 does not prove every runtime path is covered or that physical-device behavior works.

## Active ownership

| Owner | Issue/task | Scope |
|---|---|---|
| Chaty | #22 — home screen and navigation | PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`; physical tests pending |
| Chaty | #4 — build/updater verification | Fallback/version/digest/URL tests passed in PR #317; additional edge cases and hardware update verification remain |
| Ploy | No active assignment | Do not start work until Chaty explicitly assigns a separate issue |
| Owner hold | #23 — addon manager | No work until a new written specification is provided |

Per the owner's instruction, Ploy has no active assignment on #22. Chaty owns the implementation and review; any future Ploy work requires a separate explicit assignment.

## Pull request validation

PRs targeting `beta` use an unsigned release build and unit-test job with read-only permissions; it does not access signing secrets or publish APKs. PR #26 run [#329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) passed for head `3a1396f7364be1e6aa658b3eb67be41549a906fb`. The merge commit is `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Signed release, signer, ARM64 ABI and artifact verification must be checked on the exact post-merge beta commit; PR validation does not satisfy those gates.

## P0 implementation status in beta

| Finding | Candidate implementation | Verification state |
|---|---|---|
| BSDIFF decompression expansion | Per-section decompressed-output caps plus an over-limit regression test | PR run #329 passed; deeper adversarial coverage remains advisable |
| Updater worker lifetime | Queued worker retires after 30 seconds idle | Source reviewed; PR run #329 passed |
| GitHub URL validation | HTTPS URL requires exact `github.com` hostname; redirect hosts use an exact allowlist | URL unit tests and PR run #329 passed |
| GeckoSession lifecycle | Preserved across Home; closed when Activity composition is destroyed | Source reviewed; physical lifecycle unverified |
| Home Back | BackHandler opens exit confirmation | Source reviewed; physical interaction unverified |
| Pending installer URI | Saved/restored through Activity instance state | Source reviewed; process-death flow unverified on device |
| GeckoView R8 rules | Keep rules wired into minified release build | PR run #329 unsigned release build passed; signed/minified device startup unverified |

The updater is process-scoped; its worker thread now times out after idle rather than remaining alive indefinitely. It is not shut down after each check, because later update operations must remain usable.

## Release blockers

1. Verify signed ARM64 branch CI, package identity, signing certificate, ARM64-only packaging and artifact contents for the exact post-merge beta commit.
2. Complete the physical ARM64 game compatibility and persistent-profile test plan.
3. Verify updater installation, pending installer restoration and differential/full APK fallback on hardware.
4. Reconcile the nine main-only commits before promotion; `main` and `beta` are diverged (beta ahead 166, behind 9).
5. Reconcile README/release notes against the exact candidate commit before release approval.

Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Next actions

1. Verify exact beta HEAD and post-merge signed ARM64 CI/artifact.
2. Inspect and reconcile the nine main-only commits without discarding intended stable-branch history.
3. Arrange real ARM64 device testing and record device, Android version, tested commit, APK SHA-256 and individual results.
4. Only after all gates, prepare a beta-to-main promotion plan; owner approval for promotion/release remains required.


## Integration record — PR #26

- **PR:** [#26](https://github.com/iNeonyne/LastHavenPlus/pull/26), merged.
- **PR head tested:** `3a1396f7364be1e6aa658b3eb67be41549a906fb`.
- **PR validation:** [run #329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) passed unsigned release build and unit tests.
- **Merge commit / beta HEAD at integration:** `e924c580699ad34f1083d1a9a0fe96901037c0ae`.
- **Post-merge signed CI:** Not verified by the available run evidence; do not infer success from PR run #329.
- **Main comparison:** beta ahead 166, main ahead 9; merge base `2455c541729d20f00759ac9fa258faaaa473efcf`. Main promotion is not safe until the nine main-only commits are reviewed and reconciled.

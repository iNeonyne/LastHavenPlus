# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest beta HEAD:** `1f300b90e5aed9a5eaa86e473b746f0841d42b16`  
**Latest integrated application change:** `1d6c1ec42becab44735edfd0323dfc60ff4b9f9d`  
**Current stable release:** [v2.0.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.0.0)

## Summary

- v2.0.0 is published on GitHub Releases. Release metadata confirms the non-draft, non-prerelease release has two assets: `lasthavenplus-arm64.apk` (96,334,724 bytes) and `lasthavenplus-update.json` (248 bytes).
- `beta` is the active development/integration branch; `main` is stable. Main HEAD is `5405a88d77176be28babeafbe473ed8c1bad75f9`.
- Beta uses GeckoView 160 and is ARM64-only. The application ID is `com.neonyne.lasthavenlauncher`.
- Updater hardening from PR #30 is integrated in beta at `1d6c1ec`.
- Beta HEAD `1f300b9` is a documentation-only commit after the integrated app source commit. The intervening commits do not change application source.
- PR #32 was superseded. Replacement PR [#34](https://github.com/iNeonyne/LastHavenPlus/pull/34) adds streamed byte limits, HTTPS host validation, release-tag validation and regression tests. Run #351 passed for an earlier head; run #353 failed only in an assertion that inspected a closed test response, and run #354 was cancelled after the fix. Latest head `91de158273b3abcb7f3442aed24f25a3ac7a3439` is being validated in [run #355](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45527780630). PR #34 is not integrated.
- The owner approved resuming the supplied addon manager. Issue #23 records the audit and scope; no manager code is integrated.
- No physical ARM64 device verification is claimed.

## Branch and CI evidence

| Item | Verified state |
|---|---|
| Active branch | `beta` |
| Beta HEAD | `1f300b90e5aed9a5eaa86e473b746f0841d42b16` |
| Integrated application source commit | `1d6c1ec42becab44735edfd0323dfc60ff4b9f9d` |
| Stable main HEAD | `5405a88d77176be28babeafbe473ed8c1bad75f9` |
| Updater PR validation | [Run #345](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45521843766) passed unsigned release build and unit tests for the exact PR head |
| Signed beta validation | [Run #346](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45522408962) passed for exact integrated commit `1d6c1ec` |
| Release workflow retry | Run #347 was cancelled during optional differential packaging; the v2.0.0 GitHub Release is now independently confirmed published with the two assets listed above |
| Release workflow validation | PR #32 runs #348/#349 were cancelled and the PR was superseded. Replacement PR #34 run #351 passed for earlier head `751102ac...`; run #353 failed in a test assertion and run #354 was cancelled. Latest head `91de158273b3abcb7f3442aed24f25a3ac7a3439` is being validated in [run #355](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45527780630) |
| Physical device | Not performed / unverified |

Run #346 verified the signed optimized release build, unit tests, established signing certificate, ARM64-only package and artifact upload for the exact beta integration commit `1d6c1ec`. It does not verify later documentation-only beta HEAD `1f300b9`. Do not describe CI as physical-device verification.

## Implemented in beta source

| Area | Source state | Verification boundary |
|---|---|---|
| Browser engine | GeckoView 160 ARM64 artifact; Cefrium removed from production build | Signed CI verified build/package; game behavior on hardware unverified |
| Runtime/session | Process-scoped GeckoRuntime; GeckoSession created on Play and retained across Home | Source and CI verified; physical lifecycle/persistence unverified |
| Game launch | Home appears first; Play loads `https://lasthavens.com/play` | Source and CI verified; real gameplay unverified |
| Navigation | Reload, Home, Return to game, Settings, diagnostics, manual update check and Exit | Source and CI verified; physical navigation/accessibility unverified |
| Settings/storage | APK size, persistent app-private data and cache shown separately | Source inspected; physical values unverified; persistent data is not profile-only |
| Network usage | Android per-UID sent/received byte counters | Includes game and updater traffic; physical readings unverified |
| Diagnostics | Bounded local logs, viewer/share/clear and redaction logic | Source inspected; hardware behavior and remaining tests open |
| Updater | Stable release check, explicit download choice, digest/package/version/signature checks and installer handoff | Unit tests and signed CI passed; physical install/recovery unverified |
| Differential update | Base identity/digest checks and full APK fallback | CI passed; physical delta/full install remains unverified |
| Packaging | Gradle Wrapper pinned; ARM64-only configuration | Signed build, certificate and ABI checks passed for `1d6c1ec` |
| Addon manager | Not present in beta | Owner approved new XPI integration under #23; not implemented |

## Open blockers and unverified work

### P0 — release workflow and updater

- [ ] Review PR #34 and exact-head CI. The superseded PR #32 lacked hard byte caps; PR #34 adds bounded streaming reads, release-tag validation and download/redirect tests. Do not merge until the latest exact-head CI passes.
- [ ] Complete updater regression coverage tracked in #4: malformed manifests, digest mismatch, incompatible base APK, corrupted/truncated patches and interrupted downloads at the download boundary.
- [ ] Verify pending installer recovery, delta/full fallback and same-key in-place update on physical ARM64 hardware (#5).

### P0 — physical GeckoView/game verification (#5)

- [ ] Cold install and launch signed APK on physical ARM64 hardware, Android 10/API 29+.
- [ ] Verify Home-first behavior, Play/game loading, login and representative gameplay.
- [ ] Verify WebGL/WebGL2, WebAssembly, WebSockets, touch, audio, fullscreen and orientation.
- [ ] Verify cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication persistence across force-stop/restart.
- [ ] Verify pause/resume, Home/Return to game, Back/exit confirmation and renderer recovery.
- [ ] Verify same-key update and installer permission recovery without profile loss.
- Record device model, Android version, tested commit, APK digest, results and redacted logs.

### P1 — addon manager (#23)

- Owner-approved to resume on 2026-11-25.
- Supplied XPI v0.11.0 audit completed; issues include pre-allocation import reads, unbounded network response sizes, missing timeouts, recursive dependency traversal, incomplete Range handling, partial catalog cache replacement, memory duplication and GeckoView tab/session integration.
- Required UX: dedicated Home-screen button, not Settings. Opening the manager must not start the game. Preserve an existing game GeckoSession; manager Play resumes that same session.
- No implementation is integrated. Verify exact GeckoView 160 WebExtension APIs before implementation; full acceptance criteria are in issue #23.

### P1 — documentation reconciliation (#29)

- [ ] This documentation candidate updates README, STATUS, AGENT_WORK, CHANGELOG, WORK_LOG and the GeckoView migration notes. Keep #29 open until the PR is merged and current beta documentation is verified.
- [ ] Keep the historical changelog intact; add dated corrections rather than rewriting prior history.

### P2/P3

- #28 storage measurement accuracy and iterative traversal.
- #9 accessibility follow-up.
- #11 diagnostics tests and redaction/log-bound verification.
- #12 actionable GeckoView load-error details.
- #14 connection diagnostics.
- #16 gameplay-only orientation and keep-awake controls.
- #17 settings backup/restore.
- #7 measured runtime performance and APK size; do not optimize without measurements.

## Active ownership

| Owner | Issue/task | Scope |
|---|---|---|
| Chaty | #29 documentation reconciliation | README, STATUS, AGENT_WORK, CHANGELOG-AGENTS and WORK_LOG; PR to beta |
| Chaty | #31 / PR #34 | Review bounded release inputs and exact-head CI; supersedes PR #32 |
| Chaty | #23 planning | Confirm exact GeckoView 160 extension API feasibility, then split into non-overlapping assignments |
| Owner/device tester | #5 | Physical ARM64 gameplay, profile persistence and updater installation verification |
| Ploy | No active assignment | Wait for an explicit issue-scoped assignment |

## Release readiness

v2.0.0 is published. v2.1.0 is a proposed roadmap target, not a committed release date or promise.

Do not recommend v2.1.0 release until the selected scope is complete and the following are verified on the exact candidate commit:

- Required PR validation, unit tests and signed release workflow pass.
- APK contains only `arm64-v8a`, retains app ID and established signing identity, and is signed (never ship debug APKs).
- Updater manifest, digest, signature, package identity and full-APK fallback checks pass; downloads/install remain manual.
- Physical ARM64 gameplay, media, navigation, profile persistence and same-key update installation pass.
- If addon manager is included, all #23 security, GeckoView integration and physical-device acceptance criteria pass.
- README, release notes, STATUS, ISSUES and CHANGELOG accurately describe shipped code and evidence.
- Owner explicitly approves beta-to-main promotion and separately approves release tag/publication.

No promotion, tag creation or publication is authorized by this status report.

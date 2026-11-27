# Agent work assignments — LastHavenPlus

**Last verified:** 2026-11-27  
**Verified beta HEAD after PR #175:** `022b4e2c4880d13b23e4e8c22cbd3dd5be679623` (documentation-only follow-up; latest app-code merge remains PR #173 at `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`).  
**Stable release:** v2.3.0 (tag commit ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88).

## Roles and integration

- **Chaty:** main orchestrator, issue triage/assignment, integration fixer, verification and CI coordinator, sole integrator into beta.
- **Vibe:** implementation contributor; works only on the current active issue explicitly assigned by Chaty through GitHub. After PR handoff, Chaty owns branch fixes and integration.
- **Repository owner:** final product, priority, Phase B and release authority.
- GitHub Issues, comments, PRs, commits and Actions are the only agent-to-agent communication channel.
- All implementation PRs target beta. No direct contributor push to beta/main. Chaty alone merges. No release/tag/publication or beta-to-main promotion without explicit owner approval.

## Active work

### Chaty — #4 / #15 updater reliability and recovery
- **Owner:** Chaty (agent:chaty); one coordinated updater owner; do not parallelize #4/#15/#31.
- **Current beta:** 7451b940dc0adfa1ea14234fe6d489bba688b12c.
- **Candidate:** partial-open BSDIFF cleanup exists only on chaty/updater-patch-stream-cleanup-4-beta; PRs #157/#159/#160 closed unmerged. No candidate code is integrated.
- **Latest exact-head evidence:** PR #160 head d0cb2f5144257a2270ae0ed42f649833ad688207; Markdown #45891807077 passed. Reproducibility build A failed resolving org.jetbrains.kotlin.plugin.compose:2.4.21; build B and Android PR validation were incomplete when closed. No overall pass claimed.
- **Remaining #4 acceptance:** adversarial updater test matrix, real signed ARM64 APK-pair byte-for-byte reconstruction and signature/package/version/ABI verification, differential benchmark, current-beta signed packaging checks. No physical-device update test claimed.
- **#15:** recovery UX/error mapping/cancellation cleanup is sequenced behind #4. No second updater implementation.
- **#31:** agent:blocked; release-tag packaging/runtime verification requires explicit owner authorization. Do not trigger release workflows for testing.

### Completed — #128 repository housekeeping
- **PR:** #161, squash-merged to beta at a0e5271793c9bb7754b7ac41429065e5602df569.
- **Branch:** chaty/issue-128-housekeeping-beta, based on beta 7451b940dc0adfa1ea14234fe6d489bba688b12c.
- **Scope:** reconciled README, AGENTS, STATUS, ISSUES, CHANGELOG-AGENTS, AGENT_WORK and WORK_LOG with v2.3.0, current beta, integrated Addon Manager Phase A, current issue ownership and closed PR inventory.
- **Validation:** exact-head Markdown #45892727113 and post-merge Markdown #45892884986 passed. Local link/file/anchor scan of seven changed Markdown files found zero broken local targets. Issue #128 closed.
- **Exclusions preserved:** no application behavior, game, dependencies/toolchain, signing, ABI, release, tag, main promotion or branch deletion.

### Chaty — #81 dependency evidence (queued)
- Five approved library-only pins are already integrated by PR #104 at cff8dd2078cb240a69c11fd8c59db648bd0a9d58.
- Remaining work is evidence/documentation reconciliation only. No AGP, Kotlin, Gradle, JDK, SDK/Build Tools, GeckoView, signing or ABI changes.

## Vibe work queue

Vibe has exactly one active implementation issue at a time. After handing off a PR, Vibe immediately starts the next explicitly assigned independent issue while Chaty integrates earlier PRs. A queued issue is not an assignment; Chaty must change its label to `agent:vibe` and post a complete scope/acceptance comment.

### Chaty integration — #146 GameSessionController (PR #173)
- **GitHub ownership:** `agent:chaty`; Vibe handed off PR #173 and Chaty owns all integration fixes and merge.
- **PR:** #173, branch `vibe/game-session-controller-146`, head `eb9f64b672eced9143bc5689106223c3e7af3d16`, based on beta `d83e2df`.
- **Validation:** Markdown #45920691560 passed; Android #45920691633 and unsigned reproducibility #45920691607 were still running. PR is draft and conflicts with beta documentation changed by PR #165. Chaty must reconcile docs, resolve mergeability, rerun exact-head checks and merge only after code review and all gates pass.
- **Code scope:** controller/state extraction from MainActivity; no physical ARM64 device test. See PR #173 for full handoff and limitations.

### Active — #167 GeckoView 160 extension bridge
- **GitHub ownership:** `agent:vibe`; explicitly assigned by Chaty on 2026-11-27 after PR #146 handoff. Vibe may work while Chaty integrates #146 because the initial adapter/asset work is separate.
- **Scope:** audit pinned GeckoView 160 APIs; deterministic APK-bundled extension assets; isolated bridge/adapter classes; exact-origin/path policy, sender/session validation, bounded payload schema and focused fail-closed tests.
- **Hard exclusions:** do not edit MainActivity, GameSessionController, GameSessionState, GameScreen, navigation/session ownership or shared project docs while PR #173 is being integrated. If an integration point needs an excluded file, stop and record the blocker. No Chrome MV3 service worker/APIs, external unsigned XPI, toolchain/dependency changes, game/profile changes, auto-updates or runtime-activation claims.
- **Handoff:** PR targeting beta with exact SHA, tests, CI evidence, limitations and device status. Chaty alone fixes, verifies and merges.

### Later Vibe work
- **#147:** navigation/back-stack consolidation; remains blocked until #146 is integrated.
- **#168–#169:** Addon Manager XHR/Worker runtime tasks; remain blocked until #167 and their security/runtime prerequisites are integrated.
- **#170:** same-session navigation; remains blocked pending #146/navigation and runtime prerequisites.

## Owner-controlled work

- **#23 — Addon Manager:** Phase A is integrated by PR #152 at 103bb979c98589a8704459d28d13861eccca39d6. The repository owner has authorized Phase B investigation/implementation under Chaty's coordination. Runtime injection is not yet implemented or verified. Chaty owns #23; Vibe may work only on a separately assigned child issue (#167–#170).
- **#67 — rights and third-party notices:** original project material follows the owner's All Rights Reserved decision. Third-party notice completeness and distribution-rights review remain unresolved. Issue label: agent:owner.

## Blocked dependency/authorization queue

These are genuinely blocked by prerequisites or owner decisions. They use `agent:blocked`; neither agent starts until the stated gate is cleared. Ready work waiting for an agent turn uses `agent:queued` instead:

- #20 GeckoView artifact audit — owner-deferred; PR #122 closed unmerged; notice/distribution/device evidence incomplete.
- #31 release fallback runtime verification — requires explicit owner authorization; never run a release for testing.
- #111 updater progress UX — held until v2.4.0 release and explicit v2.5.0 integration authorization; PR #143 closed unmerged.
- #19 beta channel — waits for #4 and #6.
- #16 gameplay orientation/keep-awake — waits for #56 and #14.
- #12 load-error details — waits for #11; #14 connection diagnostics waits for #12.
- #10 network totals — waits for #16; #28 storage accuracy waits for #10; #6 essential settings waits for #28; #17 settings backup/restore waits for #6.
- #147 navigation/back stack — waits for #146.
- #148 gameplay window state — waits for #146 and coordination with #16.
- #7 performance/size optimization — requires comparable measured baseline and explicit scope.
- #86 deferred feature coordination index — not an implementation assignment until owner scope is confirmed.

## Current ownership inventory — 2026-11-27

Every open issue has exactly one `agent:*` label. The label distinguishes active ownership from queue and blocker state.

| Issue | Label | State / next gate |
|---|---|---|
| #4 | `agent:chaty` | Chaty updater reliability; coordinated with #15 |
| #15 | `agent:chaty` | Chaty updater recovery; sequenced behind #4 |
| #23 | `agent:chaty` | Chaty owns Addon Manager Phase B coordination |
| #18 | `agent:chaty` | Roadmap coordination |
| #142 | `agent:chaty` | Assignment/queue coordination record |
| #149 | `agent:chaty` | Chaty integrating PR #165 directly; no routine review/return-to-Vibe cycle |
| #67 | `agent:owner` | Rights and third-party notice decision |
| #81 | `agent:queued` | Chaty documentation/evidence task; not started |
| #12 | `agent:queued` | #11 prerequisite merged; after #146/#147 and Phase B bridge work, explicit assignment required |
| #146 | `agent:chaty` | Closed completed; PR #173 merged at `5ed6af6` |
| #167 | `agent:vibe` | Active Vibe bridge assignment; branch creation and API-audit progress requested |
| #168 | `agent:blocked` | Waits for #167 bridge integration and contract verification |
| #169 | `agent:blocked` | Waits for #167 and #168 contracts to be integrated |
| #6 | `agent:blocked` | Waits for #28 |
| #7 | `agent:blocked` | Requires comparable measured baseline and scoped authorization |
| #10 | `agent:blocked` | Waits for #16 |
| #11 | `agent:blocked` | Remaining runtime-version reporting is deferred pending reprioritization |
| #14 | `agent:blocked` | Waits for #12 |
| #16 | `agent:blocked` | Waits for #56 and #14 |
| #17 | `agent:blocked` | Waits for #6 |
| #19 | `agent:blocked` | Waits for #4 and #6 |
| #20 | `agent:blocked` | Owner-deferred GeckoView artifact/distribution audit |
| #28 | `agent:blocked` | Waits for #10 |
| #31 | `agent:blocked` | Release-tag/runtime verification requires explicit owner authorization |
| #86 | `agent:blocked` | Deferred coordination scope requires owner confirmation |
| #111 | `agent:blocked` | v2.5.0 hold: v2.4.0 release and explicit owner authorization |
| #147 | `agent:queued` | #146 integrated; waiting for Vibe's #167 handoff and explicit assignment |
| #148 | `agent:blocked` | Waits for #146 and coordination with #16 |
| #170 | `agent:blocked` | Waits for #146/navigation and Phase B runtime prerequisites |

## Safety constraints

- Kotlin + Jetpack Compose; Mozilla GeckoView 160; Android 10/API 29+; ARM64 only (arm64-v8a).
- Preserve application ID com.neonyne.lasthavenlauncher and established release signing identity.
- Game URL: https://lasthavens.com/play. Never modify the game.
- No automatic update checks/downloads/installations; user action and Android installer confirmation remain required.
- Validate compatibility before Gradle, SDK, Build Tools, GeckoView, signing or dependency/toolchain changes.
- Never claim physical-device compatibility without real ARM64 hardware evidence.

## 2026-11-27 — Post-merge integration reconciliation (#146)

- **Current verified beta HEAD:** `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`.
- **#146 / PR #173:** closed completed; squash merge commit `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`; exact PR-head Android, unsigned reproducibility and Markdown checks passed. Post-merge Markdown passed; post-merge Android/signing/ARM64 and reproducibility runs were still in progress at this checkpoint.
- **#167:** remains Vibe's sole active implementation assignment (`agent:vibe`). A repository branch inventory at 01:34 UTC did not yet show `vibe/extension-bridge-167`; Chaty requested that Vibe create/confirm the dedicated branch from the latest beta and post the GeckoView 160 API audit findings. Do not treat the comment saying work is starting as evidence of an implementation branch or code delivery.
- **#147:** its #146 dependency is now satisfied; ownership changed from `agent:blocked` to `agent:queued`. Do not start until Chaty explicitly assigns it after #167 handoff and a fresh overlap check.
- **#148:** remains blocked by #16 and scheduling/dependency coordination. #168/#169 remain blocked pending #167 and security-contract integration. #170 remains blocked pending session/navigation/runtime prerequisites.
- **No release, tag, publication, `main` promotion or physical ARM64 device test.**

## 2026-11-27 — Final CI verification for PR #173

- Post-merge Android run [#45923143717](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143717) passed signed optimized APK build, unit tests, signing-certificate verification and ARM64-only ABI checks; signed APK artifact upload succeeded. Release publication/tag and differential-package generation were skipped as expected on a beta push.
- Independent unsigned APK reproducibility [#45923143712](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143712) passed both clean builds and APK archive/hash comparison.
- Post-merge Markdown [#45923143716](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143716) and documentation follow-up Markdown [#45923558131](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923558131) passed.
- PR #173 / issue #146 integration gates are complete. Physical ARM64 gameplay/session behavior remains unverified; no device test is claimed.

# Agent work assignments — LastHavenPlus

**Last verified:** 2026-11-26  
**Verified beta base before PR #164:** 0cc21b471fce646d03dad449b91acbe67a90df7d  
**Stable release:** v2.3.0 (tag commit ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88).

## Roles and integration

- **Chaty:** main orchestrator, issue triage/assignment, reviewer, documentation and CI coordinator, sole integrator into beta.
- **Vibe:** implementation contributor; works only on the current active issue explicitly assigned by Chaty through GitHub.
- **Repository owner:** final product, priority, Phase B and release authority.
- GitHub Issues, comments, PRs, commits and Actions are the only agent-to-agent communication channel.
- All implementation PRs target beta. No direct contributor push to beta/main. Chaty alone merges. No release/tag/publication or beta-to-main promotion without explicit owner approval.

## Active work

### Chaty — #4 / #15 updater reliability and recovery
- **Owner:** Chaty (agent:chaty); one coordinated updater owner; do not parallelize #4/#15/#31.
- **Current beta:** 7451b940dc0adfa1ea14234fe6d489bba688b12c.
- **Candidate:** partial-open BSDIFF cleanup exists only on chaty/updater-patch-stream-cleanup-4-beta; PRs #157/#159/#160 closed unmerged. No candidate code is integrated.
- **Latest exact-head evidence:** old PR #160 code/test head d0cb2f5144257a2270ae0ed42f649833ad688207 passed Android #45891807118, Markdown #45891807077 and reproducibility #45891807104 on retry attempt 2. Refreshed PR #160 head faab1e0b4f0f6d913ce685d34afc6a8ad8f07772 was closed while Android #45893613690 and reproducibility #45893613642 were running; not integrated.
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

Vibe has one active implementation task at a time. Queued tasks must not start until Chaty confirms the prior handoff and checks file overlap.

### Completed test coverage — #11 (issue remains blocked for runtime-version reporting)
- PR #163 merged to beta at 0cc21b471fce646d03dad449b91acbe67a90df7d after stale PR #154 was closed without merge.
- Cover redaction variants, log rotation/size bounds and failure-path logging.
- Do not add GeckoView runtime-version startup logging; explicitly deferred.
- Preserve local-only diagnostics and avoid credentials, cookies, tokens, authorization headers, session IDs and sensitive URL parameters.
- Exact-head Android #45893571423, Markdown #45893571482 and independent unsigned APK reproducibility #45893571443 all passed. The separate GeckoView runtime-version startup-reporting item remains deferred and issue #11 is agent:blocked.
- No updater, Addon Manager or unrelated MainActivity work.
- **Status:** recreated from current beta on branch `vibe/diagnostics-tests-11-refresh`; `Diagnostics.kt` limited to the internal directory-scoped testability seam (public `Context` API unchanged), 9 focused tests added to `DiagnosticsTest.kt`. Local validation passed (Spotless, lint, JVM suite); fresh exact-head CI run IDs are recorded in the PR description and the issue #11 handoff comment. GeckoView startup-version logging remains excluded.

### 1. #149 — launcher-owned string resources (active, agent:vibe)
- Move launcher-owned user-visible text to Android resources without changing wording/punctuation.
- Exclude game content, diagnostic payloads, developer logs and protocol values. No translations or copy redesign.
- Require resource-resolution review, exact-head lint/format/unsigned build/JVM tests and docs updates.

### 2. #146 — GameSessionController (queued, agent:vibe)
- Extract GeckoSession lifecycle and load/crash UI state into a focused controller/ViewModel.
- Preserve lazy process-scoped GeckoRuntimeProvider behavior and live-session retention while detaching for Settings/Diagnostics/Update.
- Do not overlap navigation (#147), gameplay window state (#148), updater or profile migration. Require focused state tests and exact-head Android validation.

## Owner-controlled work

- **#23 — Addon Manager:** Phase A is integrated by PR #152 at 103bb979c98589a8704459d28d13861eccca39d6. It provides native local ZIP/library management; enablement is metadata only and does not activate addons in-game. Phase B/runtime injection is not implemented and awaits separate owner authorization. Issue label: agent:owner.
- **#67 — rights and third-party notices:** original project material follows the owner's All Rights Reserved decision. Third-party notice completeness and distribution-rights review remain unresolved. Issue label: agent:owner.

## Blocked dependency/authorization queue

These issues are assigned, not unassigned. Neither agent starts until the stated gate is cleared:

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

## Current ownership inventory

Every open issue has exactly one agent:* label. No open issue is agent:unassigned. There are 26 open issues; #128 is closed and #11 remains open but blocked.

| Issue | Owner | State |
|---|---|---|
| #4 | agent:chaty | Active |
| #7 | agent:blocked | Measurement prerequisite |
| #86 | agent:blocked | Deferred coordination |
| #17 | agent:blocked | Waits for #6 |
| #6 | agent:blocked | Waits for #28 |
| #28 | agent:blocked | Waits for #10 |
| #10 | agent:blocked | Waits for #16 |
| #14 | agent:blocked | Waits for #12 |
| #12 | agent:blocked | Waits for #11 |
| #111 | agent:blocked | v2.5.0 release/owner hold |
| #31 | agent:blocked | Owner authorization |
| #20 | agent:blocked | Owner-deferred |
| #16 | agent:blocked | Waits for #56 and #14 |
| #148 | agent:blocked | Waits for #146/#16 |
| #147 | agent:blocked | Waits for #146 |
| #146 | agent:vibe | Queued second |
| #149 | agent:vibe | Active |
| #11 | agent:blocked | Runtime-version reporting deferred |
| #142 | agent:chaty | Queue coordination |
| #67 | agent:owner | Owner decision |
| #15 | agent:chaty | Active/queued behind #4 |
| #23 | agent:owner | Phase B decision |
| #18 | agent:chaty | Roadmap coordination |
| #81 | agent:chaty | Evidence/docs queued |
| #19 | agent:blocked | Waits for #4/#6 |

## Safety constraints

- Kotlin + Jetpack Compose; Mozilla GeckoView 160; Android 10/API 29+; ARM64 only (arm64-v8a).
- Preserve application ID com.neonyne.lasthavenlauncher and established release signing identity.
- Game URL: https://lasthavens.com/play. Never modify the game.
- No automatic update checks/downloads/installations; user action and Android installer confirmation remain required.
- Validate compatibility before Gradle, SDK, Build Tools, GeckoView, signing or dependency/toolchain changes.
- Never claim physical-device compatibility without real ARM64 hardware evidence.

# Agent work assignments — LastHavenPlus

**Last verified:** 2026-11-27  
**Verified beta HEAD after PR #172:** `a1a6eebedc205f059eac98db474c503643d35588`  
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

Vibe has exactly one active implementation issue at a time. After handing off a PR, Vibe immediately proceeds to the next explicitly assigned, independent issue while Chaty integrates prior PRs in parallel. A queued issue is not an assignment; it becomes startable only when Chaty changes its label to `agent:vibe` and posts a scope/acceptance comment.

### Chaty integration — #149 launcher-owned string resources
- **GitHub ownership:** `agent:chaty`; PR #165 has been handed off and Chaty owns integration.
- **Current PR:** #165, latest Chaty fix head `22bdc622cdc68f92b5b0e1f0c56a8ad2fbca58a9`.
- **Chaty actions:** direct corrections are committed; Android #45919019357 passed; Markdown #45919019564 passed; independent unsigned reproducibility #45919019411 is still running. Recheck exact-head/base and merge only after reproducibility passes.
- **Corrections made:** UpdateScreen labels/actions are now resource-backed in default `values/strings.xml`; untranslated English duplicates were removed from locale-specific resource files. `SettingsDialogs.kt` Spotless violations found by exact-head CI were corrected. Required project documentation updates are included in this PR branch.
- Preserve wording and punctuation. Exclude game content, diagnostics payloads, developer logs and protocol values. No updater behavior changes, GeckoView/session lifecycle, Addon Manager or game/client changes.
- **Vibe:** #146 is now active. Vibe starts this independent task immediately while Chaty integrates #149/#165; do not wait for PR #165 to merge. Chaty checked current open PR scopes: #165 changes launcher UI/resources/docs and #166 changes Addon Manager planner/repository/tests; neither overlaps MainActivity or GeckoSession ownership. PR #145 is closed unmerged.

### Active — #146 GameSessionController
- **Label:** `agent:vibe`; explicitly assigned by Chaty on 2026-11-27. Vibe may implement now while Chaty integrates prior PRs. Dedicated branch from current beta; no MainActivity/GeckoSession overlap with current open PRs #165/#166.
- Extract GeckoSession lifecycle and load/crash state into a focused controller/ViewModel. Preserve lazy process-scoped GeckoRuntimeProvider and the same live GeckoSession while detaching for Settings/Diagnostics/Update.
- No navigation refactor (#147), gameplay window state (#148), updater, profile migration or unrelated MainActivity cleanup. Add focused state tests and exact-head Android validation.

### Next after #146 integration — #147 navigation/back-stack consolidation
- **Label:** `agent:blocked` until #146 is integrated. After #146 merges, Chaty rechecks file overlap and explicitly assigns #147 to Vibe.
- Centralize Game, Settings, Diagnostics and Update navigation/back-stack state while preserving the current Back overlay and same-session behavior. Do not take over GeckoSession ownership from #146 or add Compose Navigation without evidence.

### Later — Addon Manager Phase B child work (queued)
- **#167:** GeckoView 160 extension bridge port; wait until PR #166 planner groundwork is reviewed and merged, then require a separate explicit Chaty assignment.
- **#168:** bounded XHR resource overlay; wait for #167 and Chaty's explicit assignment.
- **#169:** Worker interception and blocks.idx read masking; wait for #167 and Chaty's explicit assignment.
- **#170:** same-session navigation integration; blocked until #146 and the required navigation/runtime prerequisites are complete.
- **#12 — Actionable GeckoView load-error details:** now ready and queued because #11's test work merged. Schedule after #146/#147 and the higher-priority Phase B bridge work; do not start until Chaty explicitly assigns it.
- Owner authorization for Phase B does not grant blanket permission to Vibe. Chaty assigns each child issue separately, checks overlapping files, and supplies issue-specific security boundaries/tests. Never start from queue status alone.

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
| #146 | `agent:vibe` | Active Vibe assignment; proceed independently while Chaty integrates prior PRs |
| #167 | `agent:queued` | Phase B bridge port; blocked by PR #166 integration gate until merged, then explicit Chaty assignment |
| #168 | `agent:queued` | XHR overlay; wait for #167 and explicit assignment |
| #169 | `agent:queued` | Worker/read masking; wait for #167 and explicit assignment |
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
| #147 | `agent:blocked` | Waits for #146 |
| #148 | `agent:blocked` | Waits for #146 and coordination with #16 |
| #170 | `agent:blocked` | Waits for #146/navigation and Phase B runtime prerequisites |

## Safety constraints

- Kotlin + Jetpack Compose; Mozilla GeckoView 160; Android 10/API 29+; ARM64 only (arm64-v8a).
- Preserve application ID com.neonyne.lasthavenlauncher and established release signing identity.
- Game URL: https://lasthavens.com/play. Never modify the game.
- No automatic update checks/downloads/installations; user action and Android installer confirmation remain required.
- Validate compatibility before Gradle, SDK, Build Tools, GeckoView, signing or dependency/toolchain changes.
- Never claim physical-device compatibility without real ARM64 hardware evidence.

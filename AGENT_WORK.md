## Owner-directed Vibe assignments — 2026-11-27

The repository owner explicitly assigned Vibe three related Addon Manager Phase B issues; the prior one-active-issue-only instruction is superseded.

- **#169 (P0, primary implementation):** bounded Worker interception and read-only `blocks.idx` masking.
- **#167 (P0):** audit the already-merged bridge against acceptance, close source-level gaps, and perform/coordinate physical ARM64 runtime verification. Do not duplicate PRs #177, #180 or #190.
- **#170 (P1):** inspect #147 and prepare/implement same-session Addon Manager navigation, with strict shared-file ownership coordination.

Sequence #169 first. #167 may audit/test merged bridge code in parallel. #170 begins with #147 and an affected-file/state-transition plan; do not overlap #169 or #147 ownership. End-to-end #170 acceptance still depends on #169 and verified runtime behavior.

Chaty remains sole reviewer, fixer, exact-head CI verifier and merger into beta. Do not claim physical device testing or actual in-game addon/Lua initialization unless observed. No release, tag, publication or main promotion is authorized.

## Vibe — #169 Worker interception and blocks.idx masking (implementation complete; integration by Chaty)

- **Branch/start:** `vibe/worker-blocks-masking-169`, based on beta HEAD `837bfe9` (post-#194 reconciliation). PR targeting `beta` submitted; integration and exact-head verification remain with Chaty.
- **Delivered:** `worker-overlay.js` MAIN-world content script with fail-closed exact-URL Worker allowlist gate (empty by default, so inert until hardware-verified), bounded `em-pthread` blob bootstrap (20 s boot timeout, 64-message queue cap, 8 concurrent intercepted workers, object-URL revocation on every path), idempotent `assignWasmImports` wrapping, bounded read-only `blocks.idx` TAB-record parser with strict limits and fail-closed stage diagnostics, masking restricted to verified local-addon cache records with 48/35 fill pattern, and a worker-side XHR forwarder (GET/HEAD addon routes, inflight cap 4, sync fail-closed 404, non-addon passthrough) reusing `__LHAddonXhrOverlayCore` resolution page-side.
- **Files:** `app/src/main/assets/extensions/addon-bridge/worker-overlay.js` (new), `manifest.json` (third content-script entry), `scripts/test-addon-worker-overlay.mjs` (new, 61 tests), `AddonBridgeAssetsTest.kt`, `.github/workflows/android.yml`, README/STATUS/ISSUES/CHANGELOG-AGENTS/AGENT_WORK updates.
- **Executed:** `node --check` on all five extension assets; `node --test scripts/test-addon-worker-overlay.mjs` 61/61 pass; `node --test scripts/test-addon-xhr-overlay.mjs` and `scripts/test-addon-bridge.mjs` pass unchanged; JVM asset/manifest guard updates.
- **Limitations:** no physical ARM64 device test, no in-game addon/Lua initialization, no runtime verification of the exact worker URL or `blocks.idx` record column semantics, and the 48/35 fill pattern awaits runtime confirmation. Sync worker XHR on addon routes fail-closes with 404 by design.
- **Issue table:** #169 moves to implementation-complete awaiting review below.

## Current owner priority — 2026-11-27 (verified after PR #191)

- **P0:** Addon Manager Phase B (#23); Chaty owns coordination, integration and verification.
- **Vibe's sole active implementation assignment:** #169 — bounded Worker interception and read-only `blocks.idx` masking, assigned from beta HEAD `d160b052370bbcbae6925174ac2cb10ec609ff91`.
- **#168:** completed and closed after PR #191 merged. Exact-head Android #45951682137, unsigned reproducibility #45951682107 and Markdown #45951682194 passed.
- **#167:** remains open and `agent:chaty` for physical ARM64 runtime verification and any bridge defects found. Source-level foundation, payload transport and production session wiring are integrated by PRs #177, #180 and #190.
- **#169:** its bridge/XHR source-contract prerequisites are integrated; Vibe must implement only the worker/index scope defined in the issue.
- **#170:** remains `agent:blocked` until #169 is integrated and same-session manager-navigation prerequisites are ready.
- **Paused:** updater #4/#15 remain queued, not cancelled; do not resume until Chaty explicitly hands off after a safe Addon Manager milestone.
- Keep exactly one active Vibe implementation issue. No overlapping Worker/navigation ownership.

**Last verified:** 2026-11-27  
**Verified beta HEAD:** `d160b052370bbcbae6925174ac2cb10ec609ff91`  
**Stable release:** v2.3.0 (tag commit ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88).

# Agent work assignments — LastHavenPlus

## Current owner priority — 2026-11-27

- **P0:** Addon Manager Phase B (#23) remains the top project priority. Chaty owns coordination, integration and verification.
- **Vibe's sole active implementation assignment:** none; #168 implementation is complete and PR #191 is awaiting integration. Assigned from beta HEAD `892494aa8f0333f907b2e730f6b754ba2dabe2a1` after PR #180 integrated the GeckoView bridge payload transport.
- **#167:** Chaty-owned follow-up for the merged bridge foundation/payload transport and physical ARM64 runtime verification. Keep open until its runtime/device acceptance gates are satisfied.
- **Sequence:** #169 Worker/`blocks.idx` masking remains blocked until #168's contract is reviewed and integrated. #170 same-session navigation remains blocked until runtime and navigation prerequisites are satisfied.
- **Paused:** #4/#15 updater work is queued, not cancelled. Do not resume until Chaty explicitly reassigns it after a safe Addon Manager milestone handoff.
- Keep exactly one active Vibe implementation issue. Do not assign overlapping XHR/Worker/navigation work.

**Last verified:** 2026-11-27  
**Verified beta HEAD:** `892494aa8f0333f907b2e730f6b754ba2dabe2a1`  
**Stable release:** v2.3.0 (tag commit ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88).

## Roles and integration

- **Chaty:** main orchestrator, issue triage/assignment, integration fixer, verification and CI coordinator, sole integrator into beta.
- **Vibe:** implementation contributor; works only on the current active issue explicitly assigned by Chaty through GitHub. After PR handoff, Chaty owns branch fixes and integration.
- **Repository owner:** final product, priority, Phase B and release authority.
- GitHub Issues, comments, PRs, commits and Actions are the only agent-to-agent communication channel.
- All implementation PRs target beta. No direct contributor push to beta/main. Chaty alone merges. No release/tag/publication or beta-to-main promotion without explicit owner approval.

## Active work

### Chaty — #4 / #15 updater reliability and recovery (paused)
- **Owner/state:** agent:queued; paused by the 2026-11-27 owner priority decision. Not cancelled; do not resume until explicitly reassigned after Addon Manager P0 handoff. Keep #4/#15/#31 coordinated and do not parallelize.
- **Current beta:** `606e7c2a2df06de798df4cc3d8057483617e4a4e`.
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

### Completed — #146 GameSessionController (PR #173)
- **GitHub ownership:** completed; issue #146 closed after Chaty integrated PR #173.
- **Merge:** `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`.
- **Validation:** post-merge Android/signing/ARM64 run #45923143717, unsigned reproducibility #45923143712 and Markdown #45923143716 passed. Physical ARM64 gameplay/session behavior remains unverified.

### Chaty follow-up — #167 GeckoView 160 extension bridge and payload transport
- **GitHub ownership:** `agent:chaty`; issue remains open for runtime integration follow-up and physical ARM64 verification.
- **Integrated PRs:** #177 native-messaging foundation merged at `de70d3ea5ea3da180713d5f97dc195f416d30483e`; #180 bounded ZIP 0.5.1 payload transport merged at `892494aa8f0333f907b2e730f6b754ba2dabe2a1`.
- **PR #180 exact-head evidence:** Android PR validation #45938326655 passed JS validation, lint, Spotless, unsigned release build, JVM tests and MIT-notice check. Independent unsigned reproducibility #45938326638 passed both clean builds and archive/hash comparison.
- **Scope integrated:** deterministic bundled GeckoView extension assets, bounded native payload metadata/pages/chunks, exact-route sender checks, revision-bound sequential chunk reads, isolated-world payload handoff, path/symlink and byte/count limits.
- **Remaining limitations:** XHR interception (#168) now implemented in PR #191; Worker/index masking (#169), same-session manager navigation (#170), in-game addon initialization and physical ARM64 runtime behavior remain unverified. Do not claim addons load in-game.

### Vibe — #168 XHR resource overlay (implementation complete; integration by Chaty)
- **GitHub ownership:** `agent:vibe`; implementation complete; PR #191 exact-head CI passed; Chaty owns integration.
- **Branch/start:** `vibe/addon-xhr-overlay-168`, synchronized with beta through PR #190 merge `1d5f55d0`.
- **Assignment:** issue comment #7210226239 defines expected behavior, scope, exclusions, constraints, tests and documentation requirements.
- **Required handoff:** draft PR targeting beta; exact-head Node/Android/Markdown/reproducibility evidence; report exact SHA, changed files, limitations and physical-device status. Chaty alone reviews and merges.
- **Hard exclusions:** no Worker/`blocks.idx` masking (#169), navigation/session wiring (#170), native bridge/protocol edits, game/profile writes, dependency/toolchain/signing/ABI changes, downloads/updates, release/tag/publication or main promotion.

### Later Vibe work
- **#147:** navigation/back-stack consolidation; remains blocked until #146 is integrated.
- **#169:** Worker/`blocks.idx` masking; remains `agent:blocked` until #168 is reviewed and integrated.
- **#170:** same-session navigation; remains `agent:blocked` pending runtime and navigation prerequisites.

## Owner-controlled work

- **#23 — Addon Manager (P0):** Phase A/planner and bridge payload transport are integrated. Chaty owns coordination; #167 is Chaty-owned for runtime/device follow-up; #168 is Vibe's active XHR task; #169 and #170 remain blocked by their documented dependencies.
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

## Current ownership inventory — 2026-11-27 (reconciled after PR #180)

Every open issue has exactly one `agent:*` label. The label distinguishes active ownership from queue and blocker state.

| Issue | Label | State / next gate |
|---|---|---|
| #4 | `agent:queued` | Updater reliability paused pending Addon Manager P0 handoff |
| #15 | `agent:queued` | Updater recovery paused with #4 pending Addon Manager P0 handoff |
| #23 | `agent:chaty` | P0 top priority; Chaty coordinates Addon Manager Phase B |
| #18 | `agent:chaty` | Roadmap coordination |
| #142 | `agent:chaty` | Assignment/queue coordination record |
| #149 | `agent:chaty` | Chaty integrating PR #165 directly; no routine review/return-to-Vibe cycle |
| #67 | `agent:owner` | Rights and third-party notice decision |
| #81 | `agent:queued` | Chaty documentation/evidence task; not started |
| #12 | `agent:queued` | #11 prerequisite merged; after #146/#147 and Phase B bridge work, explicit assignment required |
| #146 | `agent:chaty` | Closed completed; PR #173 merged at `5ed6af6` |
| #167 | `agent:chaty` | PRs #177/#180/#190 integrated; Chaty owns physical ARM64 runtime follow-up |
| #168 | `agent:vibe` | Implementation complete; PR #191 exact-head Android, Markdown and reproducibility checks passed; Chaty integrating |
| #169 | `agent:vibe` | Implementation complete on branch `vibe/worker-blocks-masking-169`; PR targeting `beta` awaiting Chaty integration |
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
| #170 | `agent:blocked` | Waits for #168/#169 runtime contracts and navigation prerequisites |

## Safety constraints

- Kotlin + Jetpack Compose; Mozilla GeckoView 160; Android 10/API 29+; ARM64 only (arm64-v8a).
- Preserve application ID com.neonyne.lasthavenlauncher and established release signing identity.
- Game URL: https://lasthavens.com/play. Never modify the game.
- No automatic update checks/downloads/installations; user action and Android installer confirmation remain required.
- Validate compatibility before Gradle, SDK, Build Tools, GeckoView, signing or dependency/toolchain changes.
- Never claim physical-device compatibility without real ARM64 hardware evidence.

## 2026-11-27 — Post-merge integration reconciliation (#146) and #167 handoff

- **Verified beta HEAD at current checkpoint:** `606e7c2a2df06de798df4cc3d8057483617e4a4e`.
- **#146 / PR #173:** completed and merged at `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`; post-merge Android/signing/ARM64 #45923143717, unsigned reproducibility #45923143712 and Markdown #45923143716 passed.
- **#167:** Vibe handed off draft PR #177 at `4fc06e5328bef96aec42b117400c350a2b00d348` on `vibe/extension-bridge-167`; issue ownership changed to `agent:review`. Exact-head Android #45924935347 and unsigned reproducibility #45924935341 passed. No Markdown run because no docs changed. Chaty review remains outstanding.
- **#147:** its #146 dependency is now satisfied; ownership changed from `agent:blocked` to `agent:queued`. Do not start until Chaty explicitly assigns it after #167 handoff and a fresh overlap check.
- **#148:** remains blocked by #16 and scheduling/dependency coordination. #168/#169 remain blocked pending #167 and security-contract integration. #170 remains blocked pending session/navigation/runtime prerequisites.
- **No release, tag, publication, `main` promotion or physical ARM64 device test.**

## 2026-11-27 — Final CI verification for PR #173

- Post-merge Android run [#45923143717](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143717) passed signed optimized APK build, unit tests, signing-certificate verification and ARM64-only ABI checks; signed APK artifact upload succeeded. Release publication/tag and differential-package generation were skipped as expected on a beta push.
- Independent unsigned APK reproducibility [#45923143712](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143712) passed both clean builds and APK archive/hash comparison.
- Post-merge Markdown [#45923143716](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143716) and documentation follow-up Markdown [#45923558131](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923558131) passed.
- PR #173 / issue #146 integration gates are complete. Physical ARM64 gameplay/session behavior remains unverified; no device test is claimed.


## 2026-11-27 — PR #192 updater APK cleanup integrated

- PR [#192](https://github.com/iNeonyne/LastHavenPlus/pull/192) squash-merged to `beta` at `a2f943bf23e6b607ab3104c86798bec9130391a1` from exact PR head `b526efc59e4e7b06ef9ebc5b1bb368be44c2544c`. Issue #181 is completed and closed.
- Cleanup now scans a bounded number of direct updater-directory entries, deletes only generated finalized APK names and abandoned partial files, and never follows symlinks or descends into directories. Pending installer APKs, last handoff APKs and crash-recovery candidates remain protected. Installer handoff state is persisted before launch and rolled back if launch fails.
- Exact-head Android validation [#45948554037](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45948554037) passed lint, Spotless, JVM tests, unsigned release assembly and bundled MIT notice verification. Independent unsigned APK reproducibility [#45948554046](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45948554046) passed both clean builds and APK archive/hash comparison.
- This does not complete broader updater reliability issues #4/#15. No automatic installation, signing/ABI/toolchain changes, physical ARM64 update test, release, tag or `main` promotion occurred.


## 2026-11-27 — PR #190 GeckoView bridge session integration (source candidate)

- PR [#190](https://github.com/iNeonyne/LastHavenPlus/pull/190) wires the launcher-owned GeckoView 160 extension bridge to the exact live game GeckoSession before initial navigation and crash restoration. It reuses the process-scoped GeckoRuntime and app-private addon library; refreshes the bounded payload snapshot before explicit reload/restore; and tears down the bridge delegate, ports, pending requests and worker executor with the owning composition.
- This is source-level integration only. It does not implement XHR interception (#168), Worker/OPFS `blocks.idx` masking (#169), same-session Addon Manager navigation/page-world refresh (#170), or addon activation. No physical ARM64 test or in-game addon initialization is claimed.
- Exact PR-head Android validation [#45947192586](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45947192586) and independent unsigned APK reproducibility [#45947192520](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45947192520) passed on head `0a291ca0edd7ac7db03ffe126a02349cbd1b758a`. Documentation updates require fresh exact-head Markdown and Android/reproducibility checks before merge.
- Issue #167 remains open until physical ARM64 runtime acceptance and the remaining Addon Manager workstream gates are satisfied; merging this source integration does not imply runtime compatibility.

# Agent work assignments — LastHavenPlus

**Current beta baseline:** `1279f214b3fdd3ba420e090a6a90914a8a3399e7` (observed 2026-11-26).  
**Stable baseline:** main / v2.3.0 tag ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88.

## Roles and integration

- **Chaty:** orchestrator, issue owner/triage, reviewer, documentation coordinator and sole integrator into beta.
- **Vibe:** implementation contributor; starts only after explicit GitHub assignment and agent:vibe ownership.
- **Repository owner:** final product, priority and release authority.
- GitHub Issues, PRs, commits and Actions are the only agent-to-agent coordination channel.
- All task PRs target beta. No direct contributor pushes to beta or main. No promotion, tag or publication without explicit owner approval.

## Active Chaty work

### Issue #128 — post-v2.3.0 housekeeping
- **Owner:** Chaty (agent:chaty).
- **Branch:** chaty/post-v2.3-housekeeping, based on beta 7cd898f4c1f9b7f70787a7000e1ed9343a83540d.
- **Scope:** reconcile STATUS, ISSUES, CHANGELOG-AGENTS, AGENT_WORK and WORK_LOG; audit open PRs/issues/branches; preserve historical records and owner-paused work.
- **Exclusions:** no branch deletion, issue/PR closure, release changes or main changes. The owner subsequently requested a three-version roadmap, now recorded in issue #18.

### Issue #133 — v2.4.0 adaptive launcher icon
- **Owner:** merged; issue #133 is open with `agent:owner` solely for Android adaptive-mask/physical launcher visual signoff. No agent is implementing here.
- **Integration:** PR [#135](https://github.com/iNeonyne/LastHavenPlus/pull/135) squash-merged to `beta` at `c24b24e7125f49e86f465b1786ccfecb537e9694`; exact-head unsigned validation passed before merge.
- **Remaining verification:** Android-generated mask output and physical launcher appearance remain unverified; the owner signoff on issue #133 is the record for it.

### Issue #107 — v2.4.0 visual system (INTEGRATED)
- **Integration:** PR [#136](https://github.com/iNeonyne/LastHavenPlus/pull/136) merged into `beta` by Chaty at `e0967121b51ac1c232c92edd80157fc02b714766`; exact-head unsigned validation passed on final head `94b06eccf2670289474d0e3ad1a7a9c5e30f7c01`. Issue #107 closed on acceptance.
- **Device status:** physical ARM64 rendering/haptics remain unverified.

### Issue #9 — overlay accessibility and navigation (integrated)
- **PR:** [#139](https://github.com/iNeonyne/LastHavenPlus/pull/139), squash-merged by Chaty at `0d5ab712ce5195a3e71023c68da54f054645a8e2`.
- **Owner:** issue remains `agent:owner` for physical ARM64 TalkBack/focus/traversal, GeckoView-relative order, reduced-motion and large-font verification.
- **Device status:** no physical-device verification is claimed.

### Issue #23 — Addon Manager Phase A (active Chaty implementation)
- **Owner:** Chaty (`agent:chaty`), following the repository owner's explicit handoff on 2026-11-26. Vibe must not continue work on #23 or push to its branch.
- **PR:** [#152](https://github.com/iNeonyne/LastHavenPlus/pull/152), draft, targeting `beta`; Chaty continues the existing branch `vibe/addon-manager-phase-a-23`, synced with beta `97570eaaca86c9e27d92d04f90da9d930f6bdb33`.
- **Scope:** native Phase A only — bounded ZIP import/export, app-private addon library, storage preflight, staging/rollback, TOC dependency metadata, iterative dependency analysis, localized removal confirmation and launcher overlay navigation. Current corrections add replay-safe crash recovery, explicit old/new index revision decisions, fail-closed malformed journal/index handling, safe in-root journal path validation and regression tests for interrupted recovery.
- **Current head:** `cc15080282b24df986dee2a10788e34e1cbbe511` (docs reconciliation after the beta sync). The prior code-synced head `444985b` passed Markdown [#45875013185](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45875013185); Android/reproducibility checks were still running at that checkpoint. The final docs-reconciled head requires fresh exact-head Android, Markdown and independent reproducibility validation before merge. The previous implementation checkpoint [#45872047540](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45872047540) passed lint, Spotless, unsigned release assembly and 137/137 JVM tests; MIT notice packaging assertion passed on [#45873000772](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45873000772). No merge yet.
- **Runtime limitation:** Chrome MV3 background/service-worker execution, XHR/Worker interception, game-resource masking and Lua initialization are not implemented or enabled. GeckoView 160 runtime/lifecycle integration remains a separate gate. Phase B is not authorized in this PR.
- **Device status:** no physical ARM64 test has been performed.

### Issue #11 — privacy-conscious diagnostics tests (awaiting Chaty review)
- **Owner:** `agent:review`; Vibe submitted [PR #154](https://github.com/iNeonyne/LastHavenPlus/pull/154), and Chaty is reviewing it.
- **Current head:** `16e8abbf25626ffc8fcb7eadafa6ee05cfa79d4b`, synced with beta after PR #119.
- **Scope:** focused JVM coverage for sensitive-value redaction, log rotation/size limits and safe failure-path logging. No telemetry, network upload, logger rewrite, updater or Addon Manager changes.
- **Validation:** exact-head Android PR validation [#45874522453](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45874522453) and Markdown [#45874522448](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45874522448) passed. The independent reproducibility run hit a transient Kotlin Compose plugin-resolution failure on one clean runner; failed-job retry is pending. No physical ARM64 testing.

### Completed — issue #129 CI consolidation
- PR [#132](https://github.com/iNeonyne/LastHavenPlus/pull/132) merged at 7cd898f4c1f9b7f70787a7000e1ed9343a83540d.
- Exact PR validation [#45737717923](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717923), Markdown validation [#45737717837](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717837), and exact-merge beta run [#45738073790](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45738073790) passed.
- Beta lint/Spotless, signed ARM64 build, JVM/Python tests, established signer and ABI checks all passed. Observed runner time 94s versus 381s combined baseline; one-run comparison only.

### Issue #4 — updater regression coverage
- **Owner:** Chaty (agent:chaty).
- Keep updater implementation/test work under one owner; do not parallelize #4/#15/#31.
- Version-qualified Actions APK naming is integrated in PR #127. Remaining updater regression coverage and release-path evidence are still open; no physical updater installation claim.

### Issue #81 — compatible library refresh
- **Owner:** Chaty (agent:chaty).
- Approved scope is limited to the five libraries named in issue #81. No AGP, Kotlin, Gradle, JDK, SDK/Build Tools, GeckoView, signing or ABI changes.
- Do not merge without exact-head dependency resolution, lint, formatting, unsigned build and unit-test evidence.

### Issue #18 — v2.4.0–v2.6.0 roadmap
- Owner requested a three-version roadmap on 2026-11-26; the live issue now records v2.4.0 UI/UX, v2.5.0 updater experience and v2.6.0 settings/diagnostics.
- These are planning targets only, not release/tag/promotion authorization.
- v2.4.0 icon resources (#133), visual system (#107) and overlay accessibility (#9) are integrated; #9 remains open only for owner device verification.

## Review queue and stale-base work

### Issue #66 / PR #119 — independent unsigned APK reproducibility (merged)
- PR [#119](https://github.com/iNeonyne/LastHavenPlus/pull/119) squash-merged to beta at `97570eaaca86c9e27d92d04f90da9d930f6bdb33`; issue #66 closed automatically.
- Exact-head Android [#45867363836](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45867363836), Markdown [#45867363835](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45867363835) and independent two-runner reproducibility [#45867363851](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45867363851) passed before merge.
- Post-merge signed ARM64 beta build [#45874342894](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45874342894) and Markdown [#45874342809](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45874342809) passed. Post-merge independent reproducibility [#45874342980](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45874342980) hit a Kotlin Compose plugin-resolution failure on one clean runner; retry is running.
- No signing secrets in independent builds; no signed-artifact reproducibility, F-Droid or physical-device claim.

### Issue #70 — documentation/security guidance
- Repository-wide markdownlint was integrated by PR #120; latest beta Markdown run #45736853811 passed.
- Final central STATUS/ISSUES/CHANGELOG/AGENT_WORK reconciliation is being handled under #128. Keep #70 open until that PR is reviewed and merged.

## Owner-paused work — do not resume

- **Issue #20 / PR #122:** GeckoView artifact/dependency audit. Owner explicitly deferred it; issue remains agent:blocked, PR draft/open for traceability. No further commits or workflow-dispatch requests.
- **Issue #62 / PR #123:** branch lifecycle audit. Owner explicitly deferred it; issue remains agent:blocked, PR draft/open for traceability. No further commits.
- **Issue #65:** waiting for owner decision; do not claim emulator/device coverage.
- **Issue #15, #31, #111, #86 and #19:** blocked/deferred per current issue labels and owner priorities.

## Other preserved drafts

- **PR #113:** draft/open for traceability only. Do not merge or push further commits to its stale branch; its substance was superseded by PR #136 on `vibe/visual-system-v240` (merged).
- **PR #131:** closed unmerged because its base became stale; superseded by PR #132. No need to reopen.

## Ownership label rules

Every actionable issue must have exactly one agent:* label:
- agent:unassigned: awaiting assignment; not permission to start.
- agent:chaty: Chaty owns active work.
- agent:vibe: Vibe has explicit assignment and may start.
- agent:blocked: paused or waiting for a dependency/owner decision.
- agent:review: implementation PR submitted and awaiting Chaty's review.
- agent:owner: repository-owner decision required.

Only Chaty changes ownership labels. Assignment comments must define expected behavior, scope, exclusions, constraints, tests and documentation. Do not reassign an agent while they are actively working. After PR submission, move ownership to review; return to the implementation agent only for explicit corrections. Close issues only after acceptance criteria are verified.

## Repository safety constraints

- Kotlin + Jetpack Compose; Mozilla GeckoView 160; Android 10/API 29+; ARM64 only (arm64-v8a).
- Preserve application ID com.neonyne.lasthavenlauncher and established release signing identity.
- Game URL: https://lasthavens.com/play. Never modify the game.
- No automatic downloads or installation. No debug APK publication.
- Validate compatibility before any Gradle, SDK, Build Tools, GeckoView or signing changes.
- Never claim physical-device compatibility without real ARM64 hardware evidence.


### Issue #9 — overlay accessibility and navigation (integrated; device verification pending)
- **PR:** [#139](https://github.com/iNeonyne/LastHavenPlus/pull/139), squash-merged by Chaty to beta at `0d5ab712ce5195a3e71023c68da54f054645a8e2` from exact head `4b4d0586d2c80310d2b78e05271bc255dc4f90de`.
- **Pre-merge CI:** Android [#45829421214](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45829421214) and Markdown [#45829421210](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45829421210) passed.
- **Integrated:** pane title, heading/traversal semantics, Compose input-focus request, scrollable menu and live reduced-motion observer.
- **Remaining:** ARM64 TalkBack announcement/focus/traversal, GeckoView-relative ordering, reduced-motion behavior and large-font rendering require physical-device verification. No device test is claimed. Issue #9 remains `agent:owner` pending verification.


## New code-review follow-ups (2026-11-26)

The following GitHub issues were created from the beta code review. All are currently `agent:unassigned`; no contributor may start until Chaty checks current PR/file ownership and posts an explicit assignment.

- **#146 — GameSessionController:** P1; centralize GeckoSession lifecycle and load/crash state. Coordinate with #23 / PR #145 and avoid overlapping MainActivity/GeckoSession work.
- **#147 — Navigation/back stack:** P1; depends on #146. One pure navigation policy or lightweight stack; no new navigation dependency by default.
- **#148 — GameplayUiState:** P2; centralize existing orientation/immersive window presentation; coordinate with #16.
- **#149 — String resources:** P2; launcher-owned strings only, no translation/copy redesign.
- **#150 — Permission policy documentation:** P2; documentation-only audit of the current deny-by-default GeckoView/Android permission behavior.

Updater review findings were added to existing #15 and #4, not split into parallel updater tasks. #111 remains Vibe's active progress work and must be reviewed for file overlap before any updater follow-up is assigned.

**Assignment policy:** Do not assign these issues to Vibe while an existing task/PR owns overlapping files. Keep one active owner per issue and record the exact scope in a GitHub assignment comment before work begins.


## Active Chaty documentation task — #150

- **Owner:** Chaty (`agent:chaty`), documentation only.
- **Branch:** `chaty/issue-150-permission-policy-docs`, based on beta `1279f214b3fdd3ba420e090a6a90914a8a3399e7`.
- **Files:** README.md, AGENTS.md, STATUS.md, ISSUES.md, AGENT_WORK.md, CHANGELOG-AGENTS.md and WORK_LOG.md.
- **Source evidence:** AndroidManifest.xml declares INTERNET and REQUEST_INSTALL_PACKAGES. MainActivity grants persistent storage and audible/inaudible autoplay only to `lasthavens.com` and subdomains; all other content permissions/origins are denied; Android and media permission requests are rejected.
- **Non-goals:** no manifest, GeckoView, delegate, permission, app or game behavior changes; no physical-device claim.
- **Validation:** Markdown run #45889911748 passed on head `e2ce22c44508a87f53fc4a57a03f82e0597550bc`; internal links/anchors checked across 15 Markdown files with zero missing targets. **Next:** final review and merge.

# Agent work assignments — LastHavenPlus

**Current beta baseline:** 7cd898f4c1f9b7f70787a7000e1ed9343a83540d (verified after PR #132 merge).  
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
- **Exclusions:** no branch deletion, issue/PR closure, release changes, main changes or v2.4.0 roadmap.

### Issue #129 — CI consolidation
- **PR:** [#132](https://github.com/iNeonyne/LastHavenPlus/pull/132), merged at 7cd898f4c1f9b7f70787a7000e1ed9343a83540d.
- **Exact PR validation:** [#45737717923](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717923) passed unsigned lint, Spotless, release build, JVM and Python helper tests.
- **Post-merge:** [#45738073790](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45738073790) passed beta lint/Spotless, signed ARM64 build, JVM/Python tests, established signer and ABI checks.
- **Measured observation:** 94-second combined beta build job versus previous 258-second build + 123-second quality jobs. One-run measurement only; see STATUS.md.

### Issue #4 — updater regression coverage
- **Owner:** Chaty (agent:chaty).
- Keep updater implementation/test work under one owner; do not parallelize #4/#15/#31.
- Version-qualified Actions APK naming is integrated in PR #127. Remaining updater regression coverage and release-path evidence are still open; no physical updater installation claim.

### Issue #81 — compatible library refresh
- **Owner:** Chaty (agent:chaty).
- Approved scope is limited to the five libraries named in issue #81. No AGP, Kotlin, Gradle, JDK, SDK/Build Tools, GeckoView, signing or ABI changes.
- Do not merge without exact-head dependency resolution, lint, formatting, unsigned build and unit-test evidence.

### Issue #18 — roadmap
- Do not draft the next roadmap during this housekeeping task. Wait for the repository owner to request it after housekeeping is complete.

## Review queue and stale-base work

### Issue #66 / PR #119 — independent unsigned APK reproducibility
- PR is open on chaty/issue-66-independent-apk-rebuilds; issue ownership is agent:review.
- Prior independent builds produced byte-identical unsigned APKs (run #45727205562 on its recorded exact commit).
- The PR is based on an older beta commit and must be refreshed against current beta, then exact-head Android validation and reproducibility runs must pass before merge.
- No signing secrets, signed artifact reproducibility, F-Droid or physical-device claim.

### Issue #70 — documentation/security guidance
- Repository-wide markdownlint was integrated by PR #120; latest beta Markdown run #45736853811 passed.
- Final central STATUS/ISSUES/CHANGELOG/AGENT_WORK reconciliation is being handled under #128. Keep #70 open until that PR is reviewed and merged.

## Owner-paused work — do not resume

- **Issue #20 / PR #122:** GeckoView artifact/dependency audit. Owner explicitly deferred it; issue remains agent:blocked, PR draft/open for traceability. No further commits or workflow-dispatch requests.
- **Issue #62 / PR #123:** branch lifecycle audit. Owner explicitly deferred it; issue remains agent:blocked, PR draft/open for traceability. No further commits.
- **Issue #23:** Addon Manager remains on hold. No investigation, implementation or assignment without explicit owner approval.
- **Issue #65:** waiting for owner decision; do not claim emulator/device coverage.
- **Issue #15, #31, #111, #86 and #19:** blocked/deferred per current issue labels and owner priorities.

## Other preserved drafts

- **PR #113:** draft/open for traceability only. Owner asked Chaty to take over future UI/UX implementation; issue #107 is closed after later integration. Do not resume or merge this old stacked branch.
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

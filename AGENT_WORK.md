# Agent work assignments — LastHavenPlus

**Baseline:** `beta` at `073f839eb3cccd9a13fae09dd091667bb86a1f0c` after v2.2.0 scope integration.

## Roles

- **Chaty — roadmap and housekeeping:** owns issue #18 and central tracker/documentation reconciliation, branch audit, CI evidence, PR review and integration. Chaty alone may merge accepted PRs into `beta`.
- **Chaty — issue #56 implementation:** completed via PR [#83](https://github.com/iNeonyne/LastHavenPlus/pull/83), merged to beta. Ploy is released from this issue; no overlapping work remains.

## Active assignments

### Chaty — v2.2.0 roadmap and immediate housekeeping
- **Completed:** roadmap and central tracker reconciliation merged by PR [#73](https://github.com/iNeonyne/LastHavenPlus/pull/73) at `c16439bfcedc9949d8ec683039fb5df85afea122`.
- **Closed:** #42 as superseded by #56; #60 as completed after exact-head and post-merge CI evidence.
- **Still owned by Chaty:** finish #70 Markdown syntax/lint validation; keep #4/#15/#31 and supporting feature work tracked in the v2.3.0 backlog #86; maintain CI/release records. Branch inventory/diff audit and #56/#84 integration are complete.
- **Exclusions:** no new feature work outside the owner-approved v2.2.0 scope, no version bump/tag/publication without explicit approval, no Addon Manager work.
- **Evidence:** post-merge quality run #45631250015 and signed beta build #45631249817 passed; the build skipped release-tag publication steps.

### Chaty — application implementation status

**#56 — complete.** Direct launch and responsive overlay merged in PR #83 at `2e64f56d9dbf82fa0396d8e23b5e52a62506d9ae`; approved icon merged in PR #85 at `073f839eb3cccd9a13fae09dd091667bb86a1f0c`. Selective promotion to stable main merged in PR #87 at `f22b5c2b92b6f93b201b1a9dc50a8c8095543d76`. Exact beta PR validation and post-merge quality/signed ARM64 CI passed. Main post-merge CI is tracked in STATUS.md.

### Ploy — application issue queue (one active issue only)

Ploy has no active application issue. The remaining supporting features are deferred to v2.3.0 under #86. **Do not start queued work until the owner prioritizes and assigns it.**

**Queued work (strict order; do not start early):**

| Order | Issue | Status | Start condition / narrow scope |
|---:|---|---|---|
| 1 | [#56](https://github.com/iNeonyne/LastHavenPlus/issues/56) | COMPLETE | Direct launch, responsive overlay and session-preserving navigation integrated |
| 2 | [#9](https://github.com/iNeonyne/LastHavenPlus/issues/9) | QUEUED | Deferred to v2.3.0; start only after owner prioritization: overlay accessibility semantics, TalkBack order/labels and automated navigation coverage |
| 3 | [#11](https://github.com/iNeonyne/LastHavenPlus/issues/11) | QUEUED | Deferred to v2.3.0; diagnostics redaction, bounded rotation/size and failure-path tests only |
| 4 | [#12](https://github.com/iNeonyne/LastHavenPlus/issues/12) | QUEUED | After #11 merge: safe GeckoView 160 load-error category mapping and tests |
| 5 | [#14](https://github.com/iNeonyne/LastHavenPlus/issues/14) | QUEUED | Deferred to v2.3.0; explicit user-triggered DNS/HTTPS reachability diagnostics |
| 6 | [#16](https://github.com/iNeonyne/LastHavenPlus/issues/16) | QUEUED | After #14 and #56: persisted gameplay orientation/keep-awake preferences and lifecycle cleanup |
| 7 | [#10](https://github.com/iNeonyne/LastHavenPlus/issues/10) | QUEUED | After #16: persistent app-wide per-UID network totals; no telemetry or game-only attribution |
| 8 | [#28](https://github.com/iNeonyne/LastHavenPlus/issues/28) | QUEUED | After #10: accurate APK/data/cache reporting and iterative bounded storage traversal |
| 9 | [#6](https://github.com/iNeonyne/LastHavenPlus/issues/6) | QUEUED | Deferred to v2.3.0; essential settings surface and editable HTTPS game-URL validation/persistence only; #16/#28/#19 remain separate |
| 10 | [#17](https://github.com/iNeonyne/LastHavenPlus/issues/17) | QUEUED | After #6: versioned import/export of an allowlist of non-sensitive launcher preferences |

**Not assigned to Ploy:** #4/#15 updater code and recovery (Chaty; overlapping high-risk updater scope); #5/#21 optional owner release feedback (not an engineering or release blocker); #20 GeckoView dependency record, #31 release workflow, #61–#66 CI/build/security tasks, #70 documentation validation (Chaty); #7 requires comparable, repeatable measurements; #19 is deferred and coordinated by Chaty; #67 requires the repository owner's license decision.

**GitHub assignees:** Ploy is an internal agent role, not a confirmed GitHub account login. Mark assignments in issue bodies and `AGENT_WORK.md`; do not invent a GitHub username or assign an unrelated account.

**Per-issue constraints:** one issue per branch/PR; task branches start from current beta; never push directly to beta/main; no central tracker edits by Ploy; provide PR URL, exact head SHA, changed files, tests/CI and known limitations. Do not represent unreported owner feedback as test results.

**Global exclusions:** no game/client modifications; no application ID/signing changes; no GeckoView/toolchain upgrades without separate compatibility review; no automatic app downloads/installations; no changes to central README/AGENTS/STATUS/ISSUES/CHANGELOG/AGENT_WORK; no release/tag/publication; no Addon Manager work while #23 is on hold. Preserve GeckoView profile/session and all game data.

## Shared workflow

- Keep one issue per PR and non-overlapping scopes.
- Contributors work on task branches and open PRs targeting beta.
- Chaty alone reviews and merges into beta. Owner-authorized stable promotion uses a separate main-based PR that preserves main-only history.
- If beta advances, refresh and rerun affected checks before merge.
- Never force-push shared branches or push directly to beta/main.
- The owner retains final product and release authority.

## Deferred

- **Issue #23 Addon Manager: ON HOLD.** No investigation, implementation, assignment or review until the owner explicitly approves resuming it.
- **Issue #19 beta release channel:** deferred; not a v2.2.0 prerequisite.

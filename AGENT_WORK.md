# Agent work assignments — LastHavenPlus

**Baseline:** `beta` at `01af3f488225eaa5321b994a3957e53dcdb70c55` for the owner-directed #56 takeover.

## Roles

- **Chaty — roadmap and housekeeping:** owns issue #18 and central tracker/documentation reconciliation, branch audit, CI evidence, PR review and integration. Chaty alone may merge accepted PRs into `beta`.
- **Chaty — issue #56 implementation:** owner-directed takeover from Ploy on 2026-11-25; Chaty implements the direct-launch and responsive in-game overlay on `chaty/issue-56-direct-launch-overlay` and opens a PR targeting `beta`. Ploy must not work on #56 in parallel.

## Active assignments

### Chaty — v2.2.0 roadmap and immediate housekeeping
- **Completed:** roadmap and central tracker reconciliation merged by PR [#73](https://github.com/iNeonyne/LastHavenPlus/pull/73) at `c16439bfcedc9949d8ec683039fb5df85afea122`.
- **Closed:** #42 as superseded by #56; #60 as completed after exact-head and post-merge CI evidence.
- **Still owned by Chaty:** finish #70 Markdown syntax/lint validation; coordinate #5/#21/#31/#4 follow-up status; review and integrate Ploy's #56 PR. Branch inventory/diff audit is complete.
- **Exclusions:** no app behavior changes under this housekeeping assignment, no release/tag/publication, no main changes, no Addon Manager work.
- **Evidence:** post-merge quality run #45631250015 and signed beta build #45631249817 passed; the build skipped release-tag publication steps.

### Chaty — active application implementation

**Active assignment: #56 — direct launch and responsive in-game overlay.**  
Branch: `chaty/issue-56-direct-launch-overlay`, created from beta `01af3f488225eaa5321b994a3957e53dcdb70c55`. Chaty owns implementation and integration. Ploy must not edit #56 concurrently.

### Ploy — application issue queue (one active issue only)

Ploy is released from #56 and has no active application issue until Chaty completes and merges #56. **Do not start queued work early.**

**Queued work (strict order; do not start early):**

| Order | Issue | Status | Start condition / narrow scope |
|---:|---|---|---|
| 1 | [#56](https://github.com/iNeonyne/LastHavenPlus/issues/56) | CHATY ACTIVE | Direct launch, responsive overlay, preserve same GeckoSession, Settings in both orientations, gameplay/system UI behavior, remove idle progress bar |
| 2 | [#9](https://github.com/iNeonyne/LastHavenPlus/issues/9) | QUEUED | After #56 merge: overlay accessibility semantics, TalkBack order/labels and physical navigation follow-up |
| 3 | [#11](https://github.com/iNeonyne/LastHavenPlus/issues/11) | QUEUED | After #9 merge: diagnostics redaction, bounded rotation/size and failure-path tests only |
| 4 | [#12](https://github.com/iNeonyne/LastHavenPlus/issues/12) | QUEUED | After #11 merge: safe GeckoView 160 load-error category mapping and tests |
| 5 | [#14](https://github.com/iNeonyne/LastHavenPlus/issues/14) | QUEUED | After #12 merge: explicit user-triggered DNS/HTTPS reachability diagnostics |
| 6 | [#16](https://github.com/iNeonyne/LastHavenPlus/issues/16) | QUEUED | After #14 and #56: persisted gameplay orientation/keep-awake preferences and lifecycle cleanup |
| 7 | [#10](https://github.com/iNeonyne/LastHavenPlus/issues/10) | QUEUED | After #16: persistent app-wide per-UID network totals; no telemetry or game-only attribution |
| 8 | [#28](https://github.com/iNeonyne/LastHavenPlus/issues/28) | QUEUED | After #10: accurate APK/data/cache reporting and iterative bounded storage traversal |
| 9 | [#6](https://github.com/iNeonyne/LastHavenPlus/issues/6) | QUEUED | After #28: essential settings surface and editable HTTPS game-URL validation/persistence only; #16/#28/#19 remain separate |
| 10 | [#17](https://github.com/iNeonyne/LastHavenPlus/issues/17) | QUEUED | After #6: versioned import/export of an allowlist of non-sensitive launcher preferences |

**Not assigned to Ploy:** #4/#15 updater code and recovery (Chaty; overlapping high-risk updater scope); #5/#21 physical ARM64 verification (Chaty coordinates with the repository owner/device tester); #20 GeckoView dependency record, #31 release workflow, #61–#66 CI/build/security tasks, #70 documentation validation (Chaty); #7 requires measured device evidence; #19 is deferred and coordinated by Chaty; #67 requires the repository owner's license decision.

**GitHub assignees:** Ploy is an internal agent role, not a confirmed GitHub account login. Mark assignments in issue bodies and `AGENT_WORK.md`; do not invent a GitHub username or assign an unrelated account.

**Per-issue constraints:** one issue per branch/PR; task branches start from current beta; never push directly to beta/main; no central tracker edits by Ploy; provide PR URL, exact head SHA, changed files, tests/CI, limitations and physical-device status. No physical-device claims from CI.

**Global exclusions:** no game/client modifications; no application ID/signing changes; no GeckoView/toolchain upgrades without separate compatibility review; no automatic app downloads/installations; no changes to central README/AGENTS/STATUS/ISSUES/CHANGELOG/AGENT_WORK; no release/tag/publication; no Addon Manager work while #23 is on hold. Preserve GeckoView profile/session and all game data.

## Shared workflow

- Keep one issue per PR and non-overlapping scopes.
- Contributors work on task branches and open PRs targeting beta.
- Chaty alone reviews and merges into beta.
- If beta advances, refresh and rerun affected checks before merge.
- Never force-push shared branches or push directly to beta/main.
- The owner retains final product and release authority.

## Deferred

- **Issue #23 Addon Manager: ON HOLD.** No investigation, implementation, assignment or review until the owner explicitly approves resuming it.
- **Issue #19 beta release channel:** deferred; not a v2.2.0 prerequisite.

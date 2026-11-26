# Agent work assignments — LastHavenPlus

**Baseline:** `beta` at `6c9dadc31682db75f706435437d77969fb3e505e` (verified before this workflow change).

## Roles

- **Chaty — orchestrator and integration owner:** owns issue intake, triage, prioritization, assignments, central documentation, CI coordination, PR review, merge into beta and release coordination. Chaty may implement work directly.
- **Vibe — implementation contributor:** works only on an existing issue explicitly assigned by Chaty. Vibe submits PRs targeting beta and communicates only through GitHub Issues and PRs. Vibe cannot merge, push to beta/main, create untracked feature work or change issue priority.
- **Repository owner:** retains final product, priority and release decisions.

## Active handoffs

### Chaty — issue #4 / versioned CI APK artifact names
- Scope: qualify the signed Actions APK filename and artifact display name with resolved versionName and channel; retain canonical `lasthavenplus-arm64.apk` for updater compatibility.
- Branch: `chaty/versioned-apk-artifact-names`, based on beta `ec721c3da9a4a6f696f644ad9ad78751b36cdd10`.
- PR: [#127](https://github.com/iNeonyne/LastHavenPlus/pull/127), targeting beta. Exact-head CI and post-merge signed verification pending.
- Non-goals: no Gradle/app source, updater protocol, signing, ABI, main, tag, or release changes.


### Vibe — issue #106 / PR #112
- **Scope:** gameplay initial-loading, recoverable load failure and renderer-failure presentation.
- **PR:** https://github.com/iNeonyne/LastHavenPlus/pull/112
- **State:** PR submitted; awaiting Chaty's review and requested corrections.
- **Review gate:** verify retry/restore state-reset tests, documentation evidence and exact-head CI before merge.
- **Device status:** physical ARM64 testing unverified.

### Vibe — issue #107 / PR #113
- **Scope:** shared visual tokens, restrained explicit-action haptics and menu-overlay fade.
- **PR:** https://github.com/iNeonyne/LastHavenPlus/pull/113
- **Dependency:** stacked on PR #112. Review and integrate only after #112 is resolved and this branch is refreshed against current beta.
- **State:** PR submitted; awaiting Chaty's review after #112.
- **Device status:** physical ARM64 rendering and haptics verification unverified.

### Chaty — central ownership workflow
- **Scope:** two-agent coordination protocol, ownership labels, issue intake automation and project documentation.
- **Branch:** `chaty/agent-ownership-labels`.
- **PR:** [#114](https://github.com/iNeonyne/LastHavenPlus/pull/114), targeting beta.
- **Constraints:** no app source, Gradle, signing, release, game or main-branch changes.
- **Activation caveat:** GitHub issue-event workflows run only when the workflow file exists on the repository default branch. This beta PR prepares the workflow and templates; automatic issue-open labeling requires a separately owner-approved promotion to the default branch. Do not promote as part of this task.

## Assignment and communication rules

1. Chaty selects an existing open GitHub issue, checks duplicates/dependencies and confirms that no agent already owns it.
2. Chaty replaces the issue's current `agent:*` label with `agent:vibe` or `agent:chaty` and posts a scoped assignment comment.
3. Vibe starts only after the issue has `agent:vibe` and the assignment comment defines expected behavior, allowed scope, exclusions, constraints, tests and documentation.
4. GitHub is the only agent-to-agent communication channel. Use issue comments for assignment/progress/blockers/handoffs and PR descriptions/reviews for implementation and corrections.
5. Vibe posts a start comment, blocker updates when needed and a complete PR handoff. The PR must target beta, link its issue and report changed files, exact head SHA, test commands/results, CI links, limitations and physical-device status.
6. On PR submission, Chaty changes the issue label to `agent:review`. Chaty reviews the actual diff and exact-head CI. For revisions, Chaty returns ownership to `agent:vibe`; Vibe must not self-approve or merge.
7. Chaty alone merges accepted PRs into beta, verifies the merge commit and post-merge checks, updates status and closes the issue only when its acceptance criteria are actually met.
8. One active implementation owner per issue. Do not assign overlapping work or allow concurrent edits to the same files without an explicit coordination plan.

## Ownership label meanings

- `agent:unassigned`: no agent owns the issue; Chaty may triage and assign it. This is not permission for Vibe to start.
- `agent:chaty`: Chaty owns the issue.
- `agent:vibe`: Vibe has an explicit assignment and may start.
- `agent:blocked`: on hold or waiting for a dependency/decision.
- `agent:review`: implementation PR submitted; awaiting Chaty's review.
- `agent:owner`: waiting for repository-owner decision or optional feedback.

Exactly one agent ownership label should be present. Only Chaty changes ownership labels.

## Queue and exclusions

- **Chaty-owned coordination:** #4/#15/#31/#111 updater reliability and release verification; #20 GeckoView dependency audit; #70 documentation validation; roadmap, CI and project-wide records.
- **Vibe candidate queue:** issues are not assigned merely because they are open or labeled `agent:unassigned`. Chaty must explicitly assign one issue at a time after prioritization and scope review.
- **Owner-only:** #5/#21 optional post-release owner feedback and #67 license decision. Do not treat missing owner feedback as an engineering or release blocker.
- **Blocked/deferred:** #23 Addon Manager remains ON HOLD; #19 beta release channel, #86 deferred backlog and #7 measured performance work must not start until the owner prioritizes them.
- **Global exclusions:** no game/client modifications; no application ID or signing changes; no unapproved GeckoView/toolchain upgrades; no automatic app downloads/installations; no release/tag/publication; no Addon Manager work while #23 is on hold.

## Shared branch and release policy

- All implementation PRs target `beta`.
- Contributors use dedicated task branches and never push directly to beta/main.
- Chaty alone reviews and merges into beta.
- Never promote beta to main, create release tags or publish without explicit repository-owner approval.
- If beta advances, refresh the task branch and rerun affected checks before merge. Never force-push shared branches or bypass required checks.

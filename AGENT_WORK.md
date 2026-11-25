# Agent work assignments — LastHavenPlus

**Baseline:** `beta` at `bc270200e86e212b057d19f06616e494aa1ff538` when this assignment was opened. Chaty must refresh against current beta before integration.

## Roles

- **Chaty — roadmap and housekeeping:** owns issue #18 and central tracker/documentation reconciliation, branch audit, CI evidence, PR review and integration. Chaty alone may merge accepted PRs into `beta`.
- **Ploy — issue #56 implementation:** implement the direct-launch and responsive in-game overlay redesign on a dedicated branch and open a PR targeting `beta`. Do not edit central trackers or merge/push to beta/main.

## Active assignments

### Chaty — housekeeping and v2.2.0 roadmap
- **Issues:** #18, #42, #60, #70; coordinate #5/#21/#31/#4 follow-up status.
- **Branch:** `chaty/housekeeping-v2.2-roadmap` (documentation and issue metadata only).
- **Scope:** update the v2.2.0 roadmap, reconcile the stale Home-screen-first issue #42 against #56, close #60 only after exact post-merge CI evidence, correct obsolete workflow language in issue #70, update this assignment file, ISSUES.md, STATUS.md and CHANGELOG-AGENTS.md; audit stale branches without deleting any branch until its diff/reference status is verified.
- **Exclusions:** no app behavior changes, no release/tag/publication, no main changes, no Addon Manager work.
- **Tests:** verify Markdown structure and relative links; verify referenced issue/PR/run/commit links and current beta HEAD; confirm CI run #45631249817 and #45631250015 success.
- **Handoff:** PR targeting beta, changed-file list, commit SHA, checks, remaining limitations.

### Ploy — issue #56 direct-launch and responsive overlay
- **Issue:** #56 only. One owner and one active issue; do not work on #5/#21/#42 concurrently.
- **Branch:** create a dedicated `ploy/issue-56-direct-launch-overlay` from current `beta`; do not push to beta or main.
- **Expected behavior:** cold launch starts/resumes the existing game directly; responsive in-game overlay remains over visible gameplay; menu open/close and Settings preserve the same GeckoSession, page state and profile without reload; Settings works in portrait and landscape; gameplay returns to landscape; immersive mode/keep-awake are scoped to gameplay and restored correctly; status bar icons remain legible over Settings; no idle persistent download/progress bar; any progress feedback appears only during an explicit user-started download.
- **Scope:** only files required for #56 UI/navigation/orientation/session integration and focused tests. Keep changes minimal and isolated.
- **Exclusions:** no game/client modifications; no GeckoView engine/dependency/toolchain/signing/app-ID changes; no updater policy changes or automatic downloads/installs; no analytics; no Addon Manager implementation or menu entry while #23 is on hold; no changes to README/AGENTS/STATUS/ISSUES/CHANGELOG/AGENT_WORK; no direct pushes or merges to beta/main; no release/tag/publication.
- **Constraints:** Kotlin + Compose, GeckoView 160, API 29+, arm64-v8a only. Preserve the existing GeckoSession/profile and game data. Do not clear, migrate or overwrite browser data.
- **Tests:** focused JVM tests for menu/layout/state/orientation policy; relevant Compose/UI tests where feasible; lint, Spotless, unsigned release build, JVM and Python tests as applicable. CI does not count as physical-device verification.
- **Documentation:** update only implementation-local docs if needed; include exact changed files and behavior mapping to every #56 acceptance criterion in the PR.
- **Required handoff:** PR URL targeting beta, exact head SHA, summary, changed files, commands/results and CI links, known limitations, and explicit physical ARM64 device-testing status. Do not claim device compatibility without hardware evidence.

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

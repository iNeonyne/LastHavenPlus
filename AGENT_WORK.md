# Agent work assignments — LastHavenPlus

This file is the working agreement for the two active coding agents. The repository owner remains the decision-maker.

## Agent identities

- **Ploy** — repository-only coding agent. She can read and edit repository files but cannot inspect GitHub Issues, Pull Requests, Actions, releases or secrets. This file and `ISSUES.md` are her task brief.
- **Chaty** — main orchestrator. Owns project coordination, issue/status accuracy, review, integration, CI/release coordination and direct pushes to `beta`. `main` is the stable release branch.

## Rules for both agents

- Read `AGENTS.md`, `README.md`, `STATUS.md`, `ISSUES.md` and this file before starting.
- Follow the relevant issue scope and acceptance criteria in `ISSUES.md`.
- Preserve the application ID, established signing key, Cefrium 0.21.0 pin, persistent profile and arm64-only target.
- Do not add speculative Cefrium APIs, game-client modifications or unfinished features.
- Do not create releases, tags, or alter signing secrets.
- Do not use TickTick.
- **Exclusive task ownership:** only one agent may actively own a GitHub issue/task at a time. Never work on the same issue in parallel, even on different branches. Record owner, scope and files in STATUS before starting. The other agent must wait for explicit handoff and updated trackers.
- Keep work on separate branches/worktrees and avoid simultaneous edits to the same files.
- **`main` is stable; `beta` is the shared integration/testing branch.** Chaty pushes orchestrator work directly to `beta`.
- Ploy creates a dedicated feature branch from the latest `beta`, pushes her task commits and opens a PR with **base branch `beta`** when her assignment is ready. She must never push directly to `beta` or `main`.
- After Ploy opens a PR targeting `beta`, Chaty inspects the complete diff, runs or verifies relevant checks, requests fixes as needed, updates `ISSUES.md`, `STATUS.md` and `CHANGELOG-AGENTS.md`, and may merge the PR into `beta` once it passes review and required checks.
- Do not merge `beta` into `main` until beta build/features have been verified and the user explicitly approves the promotion. No automatic merges, tags or releases.
- Report exact tests and results. Device-only checks must remain marked pending until performed on physical ARM64 hardware.
- Chaty alone updates the central project status and changelog after reviewing/integrating Ploy's work.


## Issue intake and task boundaries

- Chaty owns all issue intake and triage. Ploy must not create GitHub issues, choose priority, assign owners, change labels/milestones, close issues or edit the central tracker.
- Every new request must be duplicate-checked against open and closed issues before creating a new issue.
- A task is not ready merely because it exists. Chaty must define a testable outcome, bounded scope, non-goals, acceptance criteria, verification plan and dependencies before assignment.
- One issue, one active owner. Record the exact files and subtask. Do not split one issue across agents unless the split is explicit and has non-overlapping files and acceptance criteria.
- If you discover adjacent work, report it to Chaty. Do not silently broaden the issue or start a second task.
- PRs must reference their issue. Use `Closes #N` only when all acceptance criteria are satisfied; otherwise use `Refs #N`.

## Ploy — assigned work

Ploy is the exclusive owner of her active delegated issue. Work in the following order, one issue at a time; move to the next only after Chaty reviews, accepts and integrates the previous PR into `beta`. Chaty must not work on the same issue or its implementation/tests while Ploy owns it.

### P1 — Issue #13: accessibility semantics

**Goal:** Improve accessibility labels and semantics for launcher controls.

Scope:
- Inspect current Compose UI and identify actionable controls whose accessible names are missing or unclear.
- Add concise, action-oriented semantics/content descriptions only where needed. Avoid duplicating visible button text.
- Check Retry, Reload, update/download, menu, Settings, diagnostics, addon manager and Exit actions.
- Add/update automated tests where the existing UI test setup supports them.
- Keep the UI compact and do not change behavior or visual design unnecessarily.

Acceptance:
- No actionable icon-only control is left without a meaningful accessible name.
- Visible text is not redundantly announced through duplicate descriptions.
- Relevant automated checks pass.
- TalkBack and focus-order verification are explicitly left pending; Ploy cannot claim physical-device verification.

File ownership while this task is active: Compose UI files required for semantics and their directly related tests. Do not modify `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md` or `AGENTS.md`; Chaty handles central tracking. When finished, push the task branch and open a PR targeting `beta`; include the PR link in the handoff report for Chaty to review.

### P2 — Issue #11: diagnostics test coverage

Start only after Chaty accepts and integrates #13 into `beta`. Ploy then exclusively owns #11; Chaty must not modify diagnostics tests or implementation while this assignment is active.

Scope:
- Expand unit tests for redaction of passwords, cookies, authorization values, tokens, session identifiers and sensitive URL parameters.
- Test bounded log size and rotation behavior.
- Test safe failure-path logging where the current implementation exposes a testable seam.
- Do not log additional runtime data or weaken redaction to make tests pass.
- Do not change the logging architecture unless a test reveals a concrete defect.

Acceptance:
- Tests cover representative sensitive formats, rotation/size limits and failure handling.
- Tests are deterministic and do not require network access or a physical device.
- Any discovered production defect is fixed within the diagnostics files only and documented in the handoff.

File ownership while active: diagnostics logger and its unit tests only. Push the completed task branch and open a PR targeting `beta`; Chaty then reviews and updates tracking.

### P3 — Issue #8: addon repository tests

Start only after Chaty accepts and integrates #11 into `beta` and explicitly confirms this task is unblocked. Ploy then exclusively owns #8 repository tests; Chaty's #8 runtime-contract investigation stays paused until Ploy hands off.

Scope:
- Add tests for duplicate imports, replacement confirmation/rollback, package storage limits, export round-trip, index recovery and failed writes.
- Use temporary test directories and deterministic ZIP fixtures.
- Verify that failed operations do not corrupt existing packages or metadata.
- Keep local enabled/disabled flags as library metadata only.

Hard exclusions:
- No game injection.
- No Cefrium hooks, JavaScript injection, XHR interception, OPFS or worker/WASM modifications.
- No online catalog or addon downloading.
- Do not change the addon manager UI or archive-inspection policy unless a test demonstrates a specific defect and Chaty approves the scope.

Acceptance:
- Repository tests cover the listed storage and recovery cases.
- Existing archive inspector tests remain passing.
- No claim is made that addons are loaded by the game.

File ownership while active: `AddonLibraryRepository.kt` and repository-focused tests only, unless Chaty approves a narrowly scoped supporting change. Push the completed task branch and open a PR targeting `beta`; Chaty then reviews and updates tracking.

## Chaty — assigned work

Chaty owns **Issue #4 only as the current active implementation task**. Continue until #4 is handed off; do not start another implementation issue concurrently with Ploy.

1. **Issue #4 — build reliability and updater tests (ACTIVE):** Gradle Wrapper, verified distribution checksum, CI migration, updater edge-case tests, package identity checks and CI verification. Preserve the current signing identity and manual update flow.
2. **Issue #19 — opt-in beta updater channel (QUEUED NEXT):** after #4 is handed off, Chaty owns stable/beta release selection, prerelease filtering, channel persistence and updater tests. Keep the stable channel as default; do not download or install automatically.
3. **Issue #5 — device verification coordination:** after #19 is handed off, maintain the checklist and record evidence supplied from the user's physical ARM64 device. Do not fabricate device results.
4. **Issue #8 — approved runtime integration investigation:** BLOCKED while Ploy owns #8 repository tests. After Ploy's #8 task is accepted and handed off, Chaty may claim the distinct operator-contract investigation. No runtime injection or game-client modification until the exact authorized interface is established and reviewed.
5. **Issues #9, #10, #12, #14, #15, #16 and #17:** Chaty-owned, scheduled one at a time after the above sequence. Never start one while Ploy owns the same issue. Do not start overlapping work.
6. Maintain `ISSUES.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`, README and CI/release documentation. Review every Ploy handoff before integration.

## Handoff format

At the end of each assignment, Ploy provides:
- Summary of implementation.
- Exact changed file list.
- Tests/commands run and actual results.
- Known limitations and anything not verified.
- Any follow-up decision needed from Chaty.

Ploy stops after opening the PR and waits for Chaty's review and merge into `beta`. She does not start the next task automatically.


### Ploy — queued after current sequence

### P2 — Issue #6: functionality-first settings and safe storage

Start only after #13, #11 and #8 have each been reviewed, accepted and integrated into `beta`. Ploy then exclusively owns #6; Chaty must not edit settings implementation while this task is active.

Scope:
- Turn the existing usage-only AlertDialog into a coherent settings experience centered on working functionality, not visual redesign.
- Implement editable HTTPS game URL with validation, explicit Apply, persistence and safe navigation behavior.
- Separate changeable preferences from read-only storage/network/version information; clearly explain units, limitations and unavailable values.
- Make every shown control functional and persisted; do not add inert placeholders.
- Investigate the pinned Cefrium API before any cache/profile deletion controls. Implement only documented safe cache clearing; do not guess profile deletion APIs or delete profile files manually.
- Coordinate with #19: Chaty exclusively implements the beta/stable updater-channel setting under #19. Ploy must not duplicate or modify that control.
- Opening/dismissing settings must not reload the game or alter its profile/session.

Acceptance:
- Settings provide useful, working controls and clear read-only information without a visual redesign.
- URL validation, persistence and explicit apply behavior have automated tests.
- Destructive actions are absent unless supported by a verified Cefrium API and have separate explicit confirmation.
- No placeholder/inert controls; tests and limitations are documented. Physical-device-only verification remains pending until performed.

File ownership: settings Compose/state/persistence files and directly related tests only. Do not modify updater implementation/channel control, central trackers or release workflows. Create a feature branch from latest `beta`, open a PR targeting `beta`, then stop for Chaty's review.

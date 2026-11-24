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
- **Exclusive task ownership:** only one agent may actively own a GitHub issue/task at a time. Never work on the same issue in parallel, even on different branches. Record owner, scope and files in STATUS before starting. The other agent must wait for explicit handoff and updated trackers.\n- Keep work on separate branches/worktrees and avoid simultaneous edits to the same files.
- **`main` is stable; `beta` is the shared integration/testing branch.** Chaty pushes orchestrator work directly to `beta`.
- Ploy creates a dedicated feature branch from the latest `beta`, pushes her task commits and opens a PR with **base branch `beta`** when her assignment is ready. She must never push directly to `beta` or `main`.
- After Ploy opens a PR targeting `beta`, Chaty inspects the complete diff, runs or verifies relevant checks, requests fixes as needed, updates `ISSUES.md`, `STATUS.md` and `CHANGELOG-AGENTS.md`, and may merge the PR into `beta` once it passes review and required checks.
- Do not merge `beta` into `main` until beta build/features have been verified and the user explicitly approves the promotion. No automatic merges, tags or releases.
- Report exact tests and results. Device-only checks must remain marked pending until performed on physical ARM64 hardware.
- Chaty alone updates the central project status and changelog after reviewing/integrating Ploy's work.

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
2. **Issue #5 — device verification coordination:** maintain the checklist and record evidence supplied from the user's physical ARM64 device. Do not fabricate device results. Claim only after #4 is handed off and after checking that Ploy does not own #5.
3. **Issue #6 — settings and storage safety:** verify pinned Cefrium APIs before implementing URL, cache or data controls. Claim only after current work is handed off.
4. **Issue #8 — approved runtime integration investigation:** BLOCKED while Ploy owns #8 repository tests. After Ploy's #8 task is accepted and handed off, Chaty may claim the distinct operator-contract investigation. No runtime injection or game-client modification until the exact authorized interface is established and reviewed.
5. **Issue #11 — diagnostics:** Ploy owns the complete delegated test task. Chaty must not edit diagnostics tests or implementation while Ploy owns #11. Any later production-code follow-up requires explicit handoff and updated ownership.
6. **Issues #9, #10, #12, #14, #15, #16 and #17:** Chaty-owned, scheduled one at a time after #4. Never start one while Ploy owns the same issue. Do not start overlapping work.
7. Maintain `ISSUES.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`, README and CI/release documentation. Review every Ploy handoff before integration.

## Handoff format

At the end of each assignment, Ploy provides:
- Summary of implementation.
- Exact changed file list.
- Tests/commands run and actual results.
- Known limitations and anything not verified.
- Any follow-up decision needed from Chaty.

Ploy stops after opening the PR and waits for Chaty's review and merge into `beta`. She does not start the next task automatically.

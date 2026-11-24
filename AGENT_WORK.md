# Agent work assignments — LastHavenPlus

This file is the working agreement for the two active coding agents. The repository owner remains the decision-maker.

## Agent identities

- **Ploy** — repository-only coding agent. She can read and edit repository files but cannot inspect GitHub Issues, Pull Requests, Actions, releases or secrets. This file and `ISSUES.md` are her task brief.
- **Chaty** — main orchestrator. Owns project coordination, issue/status accuracy, review, integration, CI/release coordination and direct pushes to `main`.

## Rules for both agents

- Read `AGENTS.md`, `README.md`, `STATUS.md`, `ISSUES.md` and this file before starting.
- Follow the relevant issue scope and acceptance criteria in `ISSUES.md`.
- Preserve the application ID, established signing key, Cefrium 0.21.0 pin, persistent profile and arm64-only target.
- Do not add speculative Cefrium APIs, game-client modifications or unfinished features.
- Do not create releases, tags, or alter signing secrets.
- Do not use TickTick.
- Keep work on separate branches/worktrees and avoid simultaneous edits to the same files.
- Ploy prepares her completed work for handoff. The user will push Ploy's changes to the repository when she is done; Ploy does not push directly to `main`.
- After the user's push, Chaty inspects the actual commit/diff, runs or verifies the relevant checks, reports defects or accepts the work, and updates `ISSUES.md`, `STATUS.md` and `CHANGELOG-AGENTS.md`. Chaty does not assume a handoff is present until the changes are visible in the repository.
- Report exact tests and results. Device-only checks must remain marked pending until performed on physical ARM64 hardware.
- Chaty alone updates the central project status and changelog after reviewing/integrating Ploy's work.

## Ploy — assigned work

Ploy works in the following order. Start only the first task; move to the next after Chaty reviews and accepts the previous task.

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

File ownership while this task is active: Compose UI files required for semantics and their directly related tests. Do not modify `STATUS.md`, `ISSUES.md`, `CHANGELOG-AGENTS.md` or `AGENTS.md`; Chaty handles central tracking. When finished, provide the handoff report to the user; the user will push the changes for Chaty to review.

### P2 — Issue #11: diagnostics test coverage

Start only after Chaty accepts #13.

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

File ownership while active: diagnostics logger and its unit tests only. The user pushes the completed changes; Chaty then reviews and updates tracking.

### P3 — Issue #8: addon repository tests

Start only after Chaty accepts #11 and explicitly confirms this task is unblocked.

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

File ownership while active: `AddonLibraryRepository.kt` and repository-focused tests only, unless Chaty approves a narrowly scoped supporting change. The user pushes the completed changes; Chaty then reviews and updates tracking.

## Chaty — assigned work

Chaty owns the following work and all integration:

1. **Issue #4 — build reliability and updater tests:** Gradle Wrapper, verified distribution checksum, CI migration, updater edge-case tests, package identity checks and CI verification. Preserve the current signing identity and manual update flow.
2. **Issue #5 — device verification coordination:** maintain the checklist and record evidence supplied from the user's physical ARM64 device. Do not fabricate device results.
3. **Issue #6 — settings and storage safety:** verify pinned Cefrium APIs before implementing URL, cache or data controls. Keep destructive game-data clearing gated on supported APIs and explicit confirmation.
4. **Issue #8 — approved runtime integration investigation:** obtain and record the operator-provided technical contract. No runtime injection or game-client modification until the exact authorized interface is established and reviewed. Coordinate with Ploy's isolated repository tests only after they are accepted.
5. **Issues #9/#10/#11 — integration and verification:** own menu/navigation, network usage, diagnostics integration, CI verification and physical-device follow-up. Ploy may contribute only the explicitly assigned isolated tasks above.
6. **Issues #12, #14, #15, #16 and #17:** own API research, error handling, updater recovery, lifecycle settings and settings backup. Delegate isolated tests only through a later update to this file.
7. Maintain `ISSUES.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`, README and CI/release documentation. Review every Ploy handoff before integration.

## Handoff format

At the end of each assignment, Ploy provides:
- Summary of implementation.
- Exact changed file list.
- Tests/commands run and actual results.
- Known limitations and anything not verified.
- Any follow-up decision needed from Chaty.

Ploy stops after the handoff and waits for Chaty's review. She does not start the next task automatically.

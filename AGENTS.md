# Agent workflow

These rules apply to every human and AI contributor working in this repository.

## Project goal

Build **LastHavenLancher**, a focused Android launcher for the existing LastHaven WotLK private-server game. The game already exists; this project is only responsible for launching and hosting it reliably. Do not recreate the game or turn the app into a general-purpose browser.

The first delivery is a playable APK. Get the game running before implementing optional launcher features. The Addon Manager is planned, but explicitly deferred until after the first playable milestone.

## Work selection

- Maintain one clear, prioritised work queue in `TODO.md`.
- Select the highest-priority task whose dependencies are satisfied.
- Complete one coherent vertical slice at a time: implementation, focused regression tests, and required documentation.
- Before starting, identify owning files, public interfaces, callers, dependencies, and existing tests.
- Avoid speculative refactors, duplicate abstractions, unnecessary frameworks, and unrelated changes.

## Efficient investigation

- Read only the documentation, code, tests, and data formats needed for the current task.
- Do not repeatedly inspect unchanged files or unrelated modules.
- Parallelise independent read-only investigations and test analysis.
- Ask delegated agents for concise findings with exact file paths, symbols, evidence, risks, and a recommended next action.
- Treat agent summaries as leads, not proof; verify important claims against upstream documentation, source, and test results.
- Validate Cefrium APIs, supported Android versions, build requirements, ABI support, licensing, and the game's actual browser requirements before committing to an integration approach.

## Agent coordination

- Use parallel agents for independent work, with one orchestrator responsible for priorities, integration, and final review.
- Give each concurrent implementation effort exclusive ownership of its files.
- Serialize overlapping edits, integration, and commits.
- Define each delegated task with a bounded scope, expected output, and clear completion criteria.
- Share discoveries promptly so agents do not repeat the same investigation.
- Integrate completed work in dependency order and resolve conflicts centrally.
- Keep one shared task queue and status record; do not create competing plans.

## Branching and pull requests

- `main` is the stable integration branch.
- Work on short-lived task branches and merge through pull requests targeting `main`.
- Do not push implementation work directly to `main`.
- Keep each PR focused and reviewable; include purpose, scope, tests, risks, and screenshots/logs when useful.
- Do not merge until required checks pass and the diff has been reviewed.
- Avoid branches for trivial changes that belong in the same coherent task.

## Product priorities

1. **P0 — Technical validation:** establish a buildable Android foundation and validate Cefrium and game compatibility with evidence.
2. **P1 — First playable:** load the existing game in Cefrium, with working rendering, touch, audio, fullscreen, and basic failure handling.
3. **P2 — Reliability:** lifecycle handling, persistent browser profile, reconnect/reload, and protection against accidental data loss.
4. **P3 — Essential controls:** minimal settings and safe cache/data controls.
5. **P4 — Addon Manager:** plan and implement only after the playable milestone; investigate the existing add-on format and workflow first.
6. **P5 — Performance and release:** profile measured bottlenecks, then harden signing, CI, and distribution.

Do not build general browser features such as tabs, bookmarks, or a browser library unless a verified game requirement makes one necessary.

## Data safety

- Keep the game profile in a stable app-private location across app upgrades.
- Distinguish disposable HTTP/cache data from cookies, LocalStorage, IndexedDB, Cache Storage, and other game state.
- Never clear game data automatically.
- Make destructive data clearing explicit and require confirmation.
- Add automated regression coverage for restart, app update, and data-preservation behavior before release.

## Implementation and validation

- Prefer extending existing abstractions over introducing parallel implementations.
- Run the narrowest relevant test after each logical change.
- Run broader validation once the complete change is assembled.
- Prefer automated tests and emulator testing. Do not ask the user to test on a physical device.
- Do not rebuild unchanged modules or rerun successful checks without a code or configuration reason.
- Never claim a build, test, deployment, or other operation succeeded unless its actual result was checked.

## Review and delivery

- Review one final diff against the starting commit.
- Remove temporary files, duplicate checks, dead code, and stale work-queue entries.
- Batch related implementation, tests, and documentation into a complete, reviewable change.
- Avoid intermediate commits that leave the repository inconsistent.
- After pushing, verify the pushed commit and any relevant CI result.
- End each handoff with completed work, branch/PR, checks performed, remaining blockers, and the next actionable task.

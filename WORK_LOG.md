# Work log — LastHavenPlus beta

**Purpose:** Durable restart point for the owner, Chaty and agents. This is an append-only operational journal, not a replacement for GitHub Issues or STATUS.md.

## Rules for every contributor

- Before changing files, record the issue, task branch, starting commit, intended file scope and acceptance criteria below.
- After each meaningful step (inspection, edit, test, commit, handoff), append a dated checkpoint with exact paths, commit SHA, commands and actual results.
- Record failures and skipped checks explicitly. Never write “passed” unless the command/run output proves it.
- At task end, record the exact handoff commit, changed files, tests, known limitations and next action.
- If interrupted, leave a final checkpoint stating the last completed action and the first safe next action. Do not amend or rewrite earlier checkpoints; append a correction if needed.
- Chaty alone integrates task commits directly into `beta`. No PRs. Never push to `main` or publish without explicit owner approval.
- Do not use TickTick. GitHub Issues and `ISSUES.md` are the source of truth.

## Current restart point — 2026-11-25

- Active branch: `beta`.
- Stable branch: `main`; no promotion or release is authorized.
- Starting commit for this preparation: `8d624ccf9dc85be1428e352c2fe9c22dfdc69310`.
- Approved target: replace Cefrium with Mozilla GeckoView; launch into a simple home screen with Play and Settings; addon manager is removed pending a new owner specification.
- Current build issue #13 previously reported Kotlin compile errors inside the legacy addon manager. The legacy manager code is being removed, so CI must be rerun before treating those errors as resolved.
- Ploy's first task is the small accessibility follow-up in #13 after the addon UI is removed; next #11 diagnostics tests; then easy settings task #6 after engine/home dependencies are resolved. Ploy must not start GeckoView production integration.
- Chaty owns #4 build baseline/updater tests, #21 GeckoView engine replacement after #20 research is reviewed, and all beta integration.
- GeckoView technical notes are in `docs/GECKOVIEW_MIGRATION.md`.
- Addon manager rebuild is blocked pending the owner's detailed instruction. Do not recreate the deleted implementation.

## Checkpoints

### 2026-11-25 — Preparation started

- **Starting beta commit:** `8d624ccf9dc85be1428e352c2fe9c22dfdc69310`.
- **Approved work:** add repo-local GeckoView agent references and durable work log; remove legacy addon manager code/UI; revise assignments so Ploy gets small, non-overlapping tasks; preserve the user's future addon-manager requirements as a hard gate.
- **No main/release action:** none.
- **Next safe action:** finish docs/issues updates, verify no stale addon-manager code references remain, inspect latest beta CI and record actual result.

### Checkpoint template

```markdown
### YYYY-MM-DD HH:MM UTC — short step
- **Issue/task:** #N / description
- **Branch and starting commit:** ...
- **Files inspected:** ...
- **Files changed:** ...
- **Action:** ...
- **Commands/checks:** exact command or CI URL
- **Result:** exact pass/fail/skipped evidence
- **Commit:** ...
- **Unverified / risks:** ...
- **Next safe action:** ...
```
# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator and currently owns central documentation, integration and release coordination. Ploy has no active assignment unless Chaty explicitly assigns a separate issue.

## Roles

- **Chaty:** owns intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone reviews and merges accepted PRs into `beta`.
- **Ploy:** may implement only a separately assigned issue. There is currently no active Ploy assignment.

## Active assignment

- **Chaty — issue #31:** release workflow draft-asset verification and optional differential fallback follow-up. PR [#54](https://github.com/iNeonyne/LastHavenPlus/pull/54) merged to beta at `b12da7651445d15080e7593d441459a8c8403070`. Exact-head unsigned validation passed in run [#45610110307](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45610110307). Post-merge signed beta run [#45610518305](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45610518305) passed.
- No contributor implementation assignment is active. Ploy has no assignment.

The next assignment must be explicitly scoped in this file before work begins.

## Shared workflow

- Keep PRs focused on one issue and non-overlapping file scope.
- Contributors work on task branches and open PRs targeting beta.
- Chaty alone integrates into beta.
- Review depth follows risk; lightweight documentation review still checks scope, factual accuracy, links and absence of behavior/security changes.
- Handoffs include outcome, scope/non-goals, changed files, exact head SHA, test commands/results, CI links, limitations and physical-device status.
- Never force-push shared branches or push directly to beta/main.

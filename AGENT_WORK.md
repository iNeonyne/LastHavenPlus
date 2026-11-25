# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator and currently owns central documentation, integration and release coordination. Ploy has no active assignment unless Chaty explicitly assigns a separate issue.

## Roles

- **Chaty:** owns intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone reviews and merges accepted PRs into `beta`.
- **Ploy:** may implement only a separately assigned issue. There is currently no active Ploy assignment.

## Active assignment

No contributor implementation assignment is currently active. Chaty completed repository housekeeping PR [#49](https://github.com/iNeonyne/LastHavenPlus/pull/49), merged at `1f661cb6234dcfdfd051d0cb963e739668896a26`. The next assignment must be explicitly scoped in this file before work begins.

## Shared workflow

- Keep PRs focused on one issue and non-overlapping file scope.
- Contributors work on task branches and open PRs targeting beta.
- Chaty alone integrates into beta.
- Review depth follows risk; lightweight documentation review still checks scope, factual accuracy, links and absence of behavior/security changes.
- Handoffs include outcome, scope/non-goals, changed files, exact head SHA, test commands/results, CI links, limitations and physical-device status.
- Never force-push shared branches or push directly to beta/main.

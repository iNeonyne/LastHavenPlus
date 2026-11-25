# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator and currently owns central documentation, integration and release coordination. Ploy has no active assignment unless Chaty explicitly assigns a separate issue.

## Roles

- **Chaty:** owns intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone reviews and merges accepted PRs into `beta`.
- **Ploy:** may implement only a separately assigned issue. There is currently no active Ploy assignment.

## Active assignment

### Chaty — documentation reconciliation (#29)

**Branch:** `chaty/docs-reconciliation-v2.1`  
**Base:** current beta at `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210`  
**Scope:** reconcile README, STATUS, ISSUES, changelog, migration notes and work records against merged PR #43, current source, exact CI evidence and owner-provided device-test status.

**Exclusions:** no application source, Gradle, signing, release workflow, game client or addon-manager implementation changes. Do not close physical test issues without evidence. Do not promote or publish from this task.

**Verification:** docs-only diff review, validate links and ensure no claim exceeds source/CI/device evidence. PR targets beta; Chaty reviews and merges.

## Shared workflow

- Keep PRs focused on one issue and non-overlapping file scope.
- Contributors work on task branches and open PRs targeting beta.
- Chaty alone integrates into beta.
- Review depth follows risk; lightweight documentation review still checks scope, factual accuracy, links and absence of behavior/security changes.
- Handoffs include outcome, scope/non-goals, changed files, exact head SHA, test commands/results, CI links, limitations and physical-device status.
- Never force-push shared branches or push directly to beta/main.

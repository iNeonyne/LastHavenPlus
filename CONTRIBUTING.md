# Contributing

## Before starting

1. Read [AGENTS.md](AGENTS.md).
2. Check [TODO.md](TODO.md) and [PROJECT_STATUS.md](PROJECT_STATUS.md).
3. Confirm that the task is the highest-priority unblocked item.
4. Check [DECISIONS.md](DECISIONS.md) before introducing a new dependency or changing an architectural assumption.
5. Identify file ownership, dependencies, and validation requirements.

## Branches and pull requests

- Branch from the latest `main`.
- Use a short, descriptive branch name, for example `feat/cefrium-poc`, `fix/profile-persistence`, or `docs/repo-bootstrap`.
- Keep each branch focused on one coherent change.
- Open a pull request targeting `main`.
- Explain the problem, approach, scope, tests performed, and any known risks.
- Do not merge until checks pass and the final diff has been reviewed.

## Parallel work

Independent research may happen in parallel. Concurrent implementation tasks must have exclusive ownership of their files. Coordinate interfaces and dependencies before integration; do not let multiple agents edit the same files simultaneously.

## Validation

Run the narrowest relevant checks while working, followed by the required broader checks before merge. Report the actual commands and results. If a check could not be run, state that clearly.

Automated and emulator testing are the expected validation methods. Do not require the user to test on a physical device.

## Scope discipline

Prioritise a playable LastHaven experience. Do not add general-purpose browser features or optional integrations without a demonstrated requirement. The Addon Manager is planned, but it belongs after the first playable milestone.

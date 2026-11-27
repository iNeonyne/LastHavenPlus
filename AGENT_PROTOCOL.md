# Agent Work and Permissions

This document defines the standing workflow and permissions for every AI agent contributing to this repository. It intentionally does not list active tasks, priorities, assignments, queues, or project status; those change over time and belong in GitHub Issues and the project tracking documents.

## 1. Authority and sources of truth

- **Chaty is the sole orchestrator and integration authority.** Chaty coordinates agents, triages work, assigns issues, resolves ownership conflicts, verifies contributions, and decides when changes are ready to merge.
- **GitHub is the required communication and work-tracking system.** GitHub Issues define tasks and assignments; PRs, commits, review comments, and Actions provide implementation discussion and evidence.
- Follow repository-level instructions in `AGENTS.md` and any more specific instructions in the assigned issue.
- Do not rely on private agent-to-agent messages as the only record of an assignment, decision, blocker, or handoff.

## 2. How agents receive work

1. Chaty selects an existing GitHub issue and explicitly assigns it to one agent.
2. The assignment must be recorded on the issue by Chaty. It must identify the expected outcome, scope, exclusions, relevant files or components when known, dependencies, and required validation.
3. An agent may start only when the issue is explicitly assigned to that agent and its scope is clear.
4. Before editing, read the issue and its assignment comments, inspect the relevant code and documentation, and check for duplicate or conflicting work.
5. Work only on the assigned issue and within its stated scope. Do not independently pick up unassigned issues, change priorities, expand scope, or reassign work.
6. If the task is unclear, conflicts with another assignment, lacks a required dependency, or needs a decision outside the agent's authority, stop the affected work and ask Chaty on GitHub.
7. Keep progress and blockers visible on the issue. Post a handoff comment when the implementation is ready for integration.

An agent must not treat a queue label, an issue appearing in a list, or an informal suggestion as permission to begin. Chaty's explicit assignment is required.

## 3. Ownership and parallel work

- One agent owns implementation of an issue at a time.
- Agents may work in parallel only on explicitly assigned tasks whose file scopes and dependencies do not conflict.
- Do not edit files owned by another active task unless Chaty explicitly coordinates the overlap.
- Do not create competing branches or duplicate PRs for the same issue.
- After handing off a PR, an implementation agent may take another independent issue only when Chaty has explicitly assigned it.
- Only Chaty changes issue ownership assignments and agent ownership/state labels.

## 4. Branch and pull request workflow

For implementation work:

1. Create a dedicated branch from the current `beta` branch, using the repository's task-branch naming convention.
2. Keep the branch focused on one assigned issue. Do not include unrelated changes.
3. Commit work to that branch; never push implementation work directly to `beta` or `main`.
4. Open a pull request targeting `beta`, unless Chaty explicitly specifies another target.
5. Keep the PR description accurate. Link the issue, summarize the result and scope, list changed files, and disclose limitations or unverified behavior.
6. Run the relevant tests, checks, and builds. Report exact commands and results; never claim a check passed without evidence.
7. Ensure CI is run for the latest PR commit. If a new commit is pushed, prior CI results do not prove the new commit passed.
8. Hand the PR to Chaty with its URL, exact head commit SHA, changed-file list, test evidence, CI status, known risks, and remaining verification needs.

Do not merge your own PR. Do not bypass required checks, force-push shared branches, or target `main` for routine task work.

## 5. Agent roles and permissions

### Chaty — orchestrator and integrator

Chaty may:
- Triage work and create or clarify task scopes.
- Assign and reassign issues and manage agent ownership labels.
- Coordinate parallel work and resolve file-scope conflicts.
- Implement work when appropriate.
- Inspect every PR's complete diff and CI evidence.
- Review PRs, request or make corrections, and commit necessary fixes directly to a PR branch when authorized and safe.
- Run or rerun validation and verify results against the exact latest commit.
- Merge accepted PRs into `beta`.
- Coordinate release readiness and promotion to `main`, subject to repository-owner approval.

Chaty is the only agent authorized to merge PRs or integrate task work into shared branches.

### Vibe — implementation agent

Vibe may:
- Implement work explicitly assigned by Chaty.
- Create and push to its own task branch.
- Commit changes, run checks, and open PRs targeting `beta`.
- Comment on assigned issues and PRs with progress, questions, blockers, and handoff evidence.

Vibe must not:
- Start unassigned work or change task priority or ownership.
- Merge PRs or push directly to `beta` or `main`.
- Change issue ownership labels or assign work to another agent.
- Treat opening a PR as proof that the work is accepted or integrated.

### IB — implementation and PR review agent

IB has the same implementation permissions and restrictions as Vibe. In addition, IB may:
- Inspect and review open PRs, including PRs created by Vibe or other contributors.
- Submit GitHub PR reviews with actionable findings, including inline comments where useful.
- Identify correctness, security, performance, compatibility, test-coverage, and CI issues.
- Recheck a PR after changes and report whether the identified concerns appear resolved.

IB must not:
- Merge PRs, approve its own changes as a substitute for independent review, or push directly to `beta` or `main`.
- Modify another agent's branch unless Chaty explicitly authorizes that change.
- Change issue assignments, priorities, or ownership labels.
- Present a review as integration approval; Chaty retains final verification and merge authority.

### Future agents

Any additional agent receives only the permissions Chaty explicitly grants in its recorded assignment. Unless Chaty records a narrower or broader authorization, a new agent follows the implementation-contributor restrictions above and has no merge authority.

## 6. PR review and integration handoff

- Reviews must be based on the complete current diff, not only the PR description or selected snippets.
- Findings should explain the impact, affected location, and a practical correction or verification.
- Reviewers must distinguish confirmed defects from questions, risks, and behavior that still requires device or environment testing.
- Agents must not claim that a PR is safe to merge solely because CI is green.
- Chaty performs the final integration assessment, checks the exact latest head SHA and required CI, makes or coordinates any required fixes, and merges only when acceptance requirements are met.
- A PR review, approval, or successful CI run does not itself authorize an agent other than Chaty to merge.

## 7. Completion and reporting

A useful handoff includes:
- Issue and PR links.
- A concise description of what changed and what did not change.
- Exact branch and head commit SHA.
- Changed files.
- Tests, build commands, and their actual results.
- Links or identifiers for relevant CI runs.
- Known limitations, risks, and unverified behavior.
- Any remaining action needed from Chaty or the repository owner.

Only Chaty records the final integration outcome, closes issues based on the repository's completion policy, and updates central assignment/status records. Agents must not close an issue merely because their implementation or PR is complete.

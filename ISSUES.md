# Issue tracker

This Markdown backlog mirror is **retired**. Its duplicated issue descriptions, assignment tables, queue labels, dependency notes and status summaries became stale as GitHub Issues changed.

**GitHub Issues are the only task tracker and the source of truth** for issue scope, acceptance criteria, priority, ownership, dependencies, blockers, progress and completion.

Agents must discover current work from live GitHub state:

- Open work owned by an agent: search `is:open label:agent:<identity>`.
- Blocked dependencies: search `is:open label:agent:blocked`.
- Open integration work: search `is:open is:pr base:beta`.
- Read the latest comments on the issue and all linked parent/dependency issues before acting.

Do not copy live issue state into this file or use an old copy of an issue description as implementation authority. Historical decisions and completed work remain in GitHub's issue/PR history and the append-only [WORK_LOG.md](WORK_LOG.md).
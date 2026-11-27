# Agent startup protocol

This file is a bootstrap guide, not a second policy manual.

**All current roles, permissions, workflow rules, technical constraints, and review requirements live in [AGENTS.md](AGENTS.md). AGENTS.md is authoritative whenever wording differs.** Do not maintain duplicate copies of those rules here.

## Startup sequence

1. Adopt the exact agent identity supplied at startup (for example, Chaty, Vibe, or IB). Do not assume another role.
2. Read [AGENTS.md](AGENTS.md) in full.
3. Read [README.md](README.md), [AGENT_WORK.md](AGENT_WORK.md), and [STATUS.md](STATUS.md). Read relevant sections of [ISSUES.md](ISSUES.md), the latest relevant entries in [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md) and [WORK_LOG.md](WORK_LOG.md), and the technical documents for the assigned component.
4. Inspect the live GitHub issue and its assignment comments, related issues/PRs, current branch heads, CI state, dependencies, and file ownership.
5. Begin implementation only after Chaty has explicitly assigned an existing issue to your exact agent identity with scope and acceptance criteria. A queue label, mention, or suggested task is not an assignment.
6. Record progress, blockers, evidence, and handoffs on GitHub. Do not rely on chat history or private messages as the only project record.

## Bootstrap prompt

> You are **[AGENT NAME]**, an AI contributor to the LastHavenPlus repository. Read [AGENTS.md](AGENTS.md) in full before taking action. Your exact identity is [AGENT NAME]. Follow only the role and permissions assigned to that identity in AGENTS.md. Then follow this startup protocol and inspect the live GitHub assignment and project records. Do not begin implementation until Chaty explicitly assigns you an existing issue with clear scope and acceptance criteria. GitHub is the durable communication and evidence system. Never exceed your permissions, and never merge a pull request unless your assigned role is Chaty.

Replace [AGENT NAME] with the exact startup identity. This prompt does not grant permissions; AGENTS.md defines them.

# Agent startup protocol

This file is a bootstrap guide, not a second policy manual.

**[AGENTS.md](AGENTS.md) is the single authoritative source for roles, permissions, workflow, and engineering rules.** Current assignments and task state live only in GitHub.

## Startup sequence

1. Adopt the exact agent identity supplied at startup (for example, Chaty, Vibe, or IB). Do not assume another role.
2. Read [AGENTS.md](AGENTS.md) in full.
3. Fetch the live `beta` head. Search GitHub Issues for the exact identity label: `is:open label:agent:vibe`, `is:open label:agent:chaty`, or the label matching your assigned identity. Also inspect `is:open label:agent:blocked` for dependencies and `is:open is:pr base:beta` for open integration work.
4. Read the selected issue and its latest comments, linked parent/dependency issues, related PRs, and relevant technical documents. Confirm the issue is still open, its label still matches your identity, and Chaty has explicitly assigned it.
5. Check current branch heads, file ownership, dependencies, and CI for the exact latest PR head. Treat the current `agent:*` label as the live owner/state and the latest valid assignment comment as the task scope. Issue bodies and older comments may contain dated historical status blocks; ignore them when superseded by later comments or labels.
6. Record assignments, progress, blockers, decisions, handoffs, and test evidence directly on GitHub Issues/PRs. Do not maintain a second queue or assignment snapshot in Markdown.

A queue label, mention, old assignment comment, or suggested task is not authorization to start. Follow the latest valid issue assignment and ask Chaty on GitHub when anything conflicts.

## Bootstrap prompt

> You are **[AGENT NAME]**, an AI contributor to the LastHavenPlus repository. Read [AGENTS.md](AGENTS.md) in full before taking action. Your exact identity is [AGENT NAME]. Follow only the role and permissions assigned to that identity in AGENTS.md. Discover current work directly from live GitHub Issues and pull requests using the ownership labels; never use Markdown snapshots as the live queue. Do not begin implementation until Chaty explicitly assigns you an existing issue with clear scope and acceptance criteria. GitHub is the durable communication and evidence system. Never exceed your permissions, and never merge a pull request unless your assigned role is Chaty.

Replace [AGENT NAME] with the exact startup identity. This prompt does not grant permissions; AGENTS.md defines them.

# Agent startup protocol

1. Adopt the exact agent identity supplied at startup.
2. Read [AGENTS.md](AGENTS.md) in full.
3. Search live GitHub Issues for your exact `agent:<identity>` label. Chaty also checks `agent:unassigned` and `agent:queued` for triage. Check `agent:blocked` dependencies and open PRs targeting `beta`.
4. Read the issue and its latest valid assignment comment, dependencies, relevant PRs and affected source.
5. Start only after Chaty explicitly assigns the issue to your exact identity with clear acceptance criteria.
6. Record progress, blockers, test results and handoff evidence on the issue or PR.

GitHub is the live task system. Do not use stale chat history, Markdown snapshots or a queue table as assignment authority. Follow AGENTS.md for all permissions and technical constraints.

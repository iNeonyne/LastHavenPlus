# issue_agent workflow

Scheduled Mistral Workflow that turns `agent:fix` issues in this repository into
draft pull requests against `beta`, ready for review and testing.

## How it works

1. **Scan** (default every 30 min, cron-managed on the deployment): lists open
   issues labeled `agent:fix` in `iNeonyne/LastHavenPlus`.
2. **Guard**: skips issues already labeled `agent:pr-opened`, `agent:needs-human`
   or `agent:failed`, or with an open `agent/issue-<n>` PR.
3. **Implement**: clones the repo at `beta`, and an LLM agent loop
   (`mistral-medium-latest` by default, `AGENT_MODEL` overrides) explores,
   edits and validates within strict guardrails:
   - protected central trackers (README, AGENTS/STATUS/ISSUES/CHANGELOG/…)
     and `.github/` cannot be written;
   - only allow-listed commands run (`./gradlew …`, `ls`, `grep`, `find`,
     `python -m py_compile`);
   - ARM64-only / app-id / signing / game-boundary rules are in the prompt.
   The result is committed and pushed to `agent/issue-<n>`.
4. **Publish**: opens a **draft PR** against `beta` with a ready-to-test summary,
   comments on the issue and labels it `agent:pr-opened`.

Failures are labeled `agent:failed` (or `agent:needs-human` when the agent
reports the issue is out of scope) and explained in an issue comment, so a
stuck issue never loops silently.

## Configuration

| Environment variable | Purpose |
| --- | --- |
| `GITHUB_PAT` | Workspace secret bound at deployment; fine-grained PAT with Contents/PRs/Issues write on this repo. Push auth goes through `GIT_ASKPASS`, never argv. |
| `AGENT_MODEL` | Mistral model for the editing loop (default `mistral-medium-latest`). |
| `AGENT_MAX_TURNS` | Tool-call budget per issue (default 40). |

## Local development

```bash
uv sync
uv run pytest
uv run ruff check .
```

Integration test against the real API (needs `MISTRAL_API_KEY` in `.env`):

```bash
uv run python - <<'EOF'
# dry-run: lists what the agent would implement without touching GitHub writes
EOF
```

Use `dry_run: true` executions for a safe preview (no edits, no PRs).

## Deployment

Deployed as managed workflow deployment `lasthaven-issue-agent` from this
directory (`build_directory=agent-workflow`, `dockerfile_path=Dockerfile`),
built from a pushed commit. A cron schedule (default `*/30 * * * *`) triggers
`issue_agent` runs.

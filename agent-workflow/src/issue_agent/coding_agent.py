"""The coding-agent loop that turns one issue into a commit on a pushed branch.

Runs entirely inside a single activity so the working tree stays on one worker.
The LLM client is constructed here, never at module import time.
"""

from __future__ import annotations

import asyncio
import json
import os
from pathlib import Path

from pydantic import BaseModel, Field

from issue_agent import git_ops

MAX_TURNS_DEFAULT = 40
MAX_TOOL_OUTPUT = 8000
MAX_FILE_BYTES = 200_000
COMMAND_TIMEOUT_SECONDS = 600

_ALLOWED_ROOT_COMMAND_PREFIXES = ("./gradlew", "ls", "grep", "find", "python -m py_compile")

_SYSTEM_PROMPT = """You are the LastHavenPlus issue-fixing agent. You work directly in a \
cloned Android (Gradle/Kotlin) launcher repository for the LastHaven game at lasthavens.com/play.

Hard repository rules (from AGENTS.md):
- PRs target the `beta` branch; the working clone is already on a fresh task branch, just commit.
- Single issue per change. Stay strictly within the file scope the issue describes.
- Never edit central trackers: README.md, AGENTS.md, AGENT_WORK.md, STATUS.md, ISSUES.md, \
CHANGELOG.md, CHANGELOG-AGENTS.md, WORK_LOG.md, or anything under .github/.
- Never touch version numbers, signing, the application id `com.neonyne.lasthavenlauncher`, or \
ABIs (ARM64-only). Do not add dependencies, engines or automatic downloads.
- Do not modify the game client. Do not add analytics or tracking. Never log session secrets.

Working method:
1. Explore first: read the relevant source under app/ before editing.
2. Make the smallest correct change that fixes the issue.
3. Validate what you can with the allowed commands (a full Android build may be unavailable in \
this environment; in that case rely on careful code reading and say so).
4. When the change is ready and committed, call the `complete` tool with a summary.

If the issue is out of scope for you, too large, on hold per the repository docs, or conflicts \
with the rules above, change nothing and call the `give_up` tool with a reason.

Summary format when you did change code: one paragraph on what changed and why it fixes the \
issue, files changed, and validation performed or not possible."""


class ToolCall(BaseModel):
    name: str
    arguments: dict = Field(default_factory=dict)


class AgentOutcome(BaseModel):
    summary: str
    tool_call_count: int = 0
    no_changes: bool = False


def _spec(name: str, description: str, props: dict, required: list[str]) -> dict:
    return {
        "type": "function",
        "function": {
            "name": name,
            "description": description,
            "parameters": {"type": "object", "properties": props, "required": required},
        },
    }


def _tool_specs() -> list[dict]:
    return [
        _spec(
            "list_dir",
            "List a directory relative to the repo root.",
            {"path": {"type": "string"}},
            ["path"],
        ),
        _spec(
            "read_file",
            "Read a text file relative to the repo root.",
            {"path": {"type": "string"}},
            ["path"],
        ),
        _spec(
            "write_file",
            "Create or overwrite a text file relative to the repo root with full content.",
            {"path": {"type": "string"}, "content": {"type": "string"}},
            ["path", "content"],
        ),
        _spec(
            "run_command",
            "Run one allowed command (./gradlew ..., ls, grep, find, "
            "python -m py_compile ...) from the repo root.",
            {"command": {"type": "string"}, "timeout_seconds": {"type": "integer"}},
            ["command"],
        ),
        _spec(
            "complete",
            "Call when the fix is committed: report what changed and why.",
            {"summary": {"type": "string"}},
            ["summary"],
        ),
        _spec(
            "give_up",
            "Call instead of editing if the issue cannot be handled.",
            {"reason": {"type": "string"}},
            ["reason"],
        ),
    ]


def _resolve(repo: Path, path: str) -> Path:
    resolved = (repo / path).resolve()
    if not str(resolved).startswith(str(repo.resolve())):
        raise ValueError("path escapes repo root")
    protected_names = {
        "readme.md",
        "agents.md",
        "agent_work.md",
        "status.md",
        "issues.md",
        "changelog.md",
        "changelog-agents.md",
        "work_log.md",
    }
    if resolved.name.lower() in protected_names or ".github" in resolved.relative_to(repo).parts:
        raise ValueError(f"{path} is out of scope for the agent")
    return resolved


def _cap_output(text: str) -> str:
    if len(text) > MAX_TOOL_OUTPUT:
        return text[-MAX_TOOL_OUTPUT:]
    return text


async def _execute_tool(repo: Path, call: ToolCall) -> str:
    name = call.name
    if name == "list_dir":
        target = _resolve(repo, call.arguments.get("path", "."))
        entries = sorted(f"{p.name}/" if p.is_dir() else p.name for p in target.iterdir())
        return _cap_output("\n".join(entries[:500]) or "(empty)")
    if name == "read_file":
        target = _resolve(repo, call.arguments["path"])
        return target.read_bytes()[:MAX_FILE_BYTES].decode("utf-8", "replace")
    if name == "write_file":
        target = _resolve(repo, call.arguments["path"])
        content = call.arguments["content"]
        if len(content.encode("utf-8")) > MAX_FILE_BYTES:
            raise ValueError("content exceeds size limit")
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")
        return f"wrote {len(content)} bytes to {call.arguments['path']}"
    if name == "run_command":
        command = call.arguments["command"].strip()
        if not command.startswith(_ALLOWED_ROOT_COMMAND_PREFIXES):
            return "command not allowed"
        timeout = min(int(call.arguments.get("timeout_seconds", 300)), COMMAND_TIMEOUT_SECONDS)
        try:
            proc = await asyncio.create_subprocess_shell(
                command,
                cwd=str(repo),
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.STDOUT,
            )
            stdout, _ = await asyncio.wait_for(proc.communicate(), timeout=timeout)
        except TimeoutError:
            return f"command timed out after {timeout}s"
        return f"exit={proc.returncode}\n{_cap_output(stdout.decode('utf-8', 'replace'))}"
    raise ValueError(f"unknown tool {name}")


async def fix_issue(
    owner: str,
    repo: str,
    base_branch: str,
    issue_number: int,
    issue_title: str,
    issue_body: str,
) -> dict:
    """Clone, edit via the agent loop, commit and push. Returns branch + summary."""
    repo_dir = await git_ops.clone_repo(owner, repo, base_branch, issue_number)
    branch = f"agent/issue-{issue_number}"

    import mistralai
    from mistralai.workflows import activity_heartbeat

    client = mistralai.client.Mistral(api_key=os.environ.get("MISTRAL_API_KEY") or None)
    model = os.environ.get("AGENT_MODEL", "mistral-medium-latest")
    max_turns = int(os.environ.get("AGENT_MAX_TURNS", str(MAX_TURNS_DEFAULT)))

    user_prompt = (
        f"Repository: {owner}/{repo}. The clone is your current directory root.\n"
        f"Issue #{issue_number}: {issue_title}\n\n"
        f"{issue_body}\n\nWork the issue now: explore, edit, validate, then call "
        "`complete` (or `give_up` with a reason)."
    )
    messages: list[dict] = [
        {"role": "system", "content": _SYSTEM_PROMPT},
        {"role": "user", "content": user_prompt},
    ]
    tools = _tool_specs()
    tool_call_count = 0
    outcome = AgentOutcome(summary="Agent produced no summary.", no_changes=True)

    for _turn in range(max_turns):
        await activity_heartbeat()
        response = await client.chat.complete_async(
            model=model, messages=messages, tools=tools, temperature=0.2
        )
        message = response.choices[0].message
        tool_calls = list(getattr(message, "tool_calls", None) or [])

        assistant_record: dict = {
            "role": "assistant",
            "content": message.content or "",
            "tool_calls": [
                {
                    "id": tool_call.id,
                    "type": "function",
                    "function": {
                        "name": tool_call.function.name,
                        "arguments": tool_call.function.arguments,
                    },
                }
                for tool_call in tool_calls
            ],
        }
        messages.append(assistant_record)

        if not tool_calls:
            text = (message.content or "").strip()
            outcome = AgentOutcome(
                summary=text[:4000] or outcome.summary,
                tool_call_count=tool_call_count,
                no_changes=(not text or text.startswith("NO_CHANGES")),
            )
            break

        finished = False
        for tool_call in tool_calls:
            try:
                arguments = json.loads(tool_call.function.arguments or "{}")
            except ValueError:
                arguments = {}
            try:
                arguments = dict(arguments)
            except (TypeError, ValueError):
                arguments = {}
            call = ToolCall(name=tool_call.function.name, arguments=arguments)
            tool_call_count += 1
            if call.name == "complete":
                outcome = AgentOutcome(
                    summary=str(call.arguments.get("summary", ""))[:4000],
                    tool_call_count=tool_call_count,
                    no_changes=False,
                )
                messages.append({"role": "tool", "tool_call_id": tool_call.id, "content": "ok"})
                finished = True
                continue
            if call.name == "give_up":
                reason = str(call.arguments.get("reason", "unspecified"))[:2000]
                outcome = AgentOutcome(
                    summary=f"NO_CHANGES: {reason}",
                    tool_call_count=tool_call_count,
                    no_changes=True,
                )
                messages.append({"role": "tool", "tool_call_id": tool_call.id, "content": "ok"})
                finished = True
                continue
            try:
                result_text = await _execute_tool(repo_dir, call)
            except (ValueError, OSError, KeyError) as exc:
                result_text = f"error: {exc}"
            messages.append(
                {
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "content": result_text,
                }
            )
        if finished:
            await activity_heartbeat()
            break

    if outcome.no_changes:
        return {
            "branch": branch,
            "no_changes": True,
            "summary": outcome.summary,
            "tool_call_count": outcome.tool_call_count,
        }

    committed = await git_ops.commit_all(
        repo_dir, f"fix(issue #{issue_number}): {issue_title}"[:200]
    )
    if not committed:
        return {
            "branch": branch,
            "no_changes": True,
            "summary": "NO_CHANGES: agent edited nothing (empty diff). " + outcome.summary,
            "tool_call_count": outcome.tool_call_count,
        }
    await activity_heartbeat()
    await git_ops.push_branch(repo_dir, branch)
    return {
        "branch": branch,
        "no_changes": False,
        "summary": outcome.summary,
        "tool_call_count": outcome.tool_call_count,
    }

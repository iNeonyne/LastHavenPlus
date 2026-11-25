"""Git operations run inside activities. Credentials reach git only via a
GIT_ASKPASS helper reading them from the environment, so tokens never appear
in argv, logs or output."""

from __future__ import annotations

import asyncio
import os
import stat
import tempfile
from pathlib import Path

_ASKPASS_SH = """#!/bin/sh
case "$1" in
  Username*) echo "x-access-token" ;;
  Password*) echo "$GITHUB_PAT" ;;
esac
"""

_GIT_IDENTITY = ["-c", "user.name=LastHaven Agent", "user.email=agent@users.noreply.github.com"]


class GitError(RuntimeError):
    pass


def _git_env(workspace: Path) -> dict:
    env = dict(os.environ)
    askpass = workspace / ".git-askpass.sh"
    askpass.write_text(_ASKPASS_SH)
    askpass.chmod(askpass.stat().st_mode | stat.S_IEXEC)
    env["GIT_ASKPASS"] = str(askpass)
    return env


async def _run(args: list[str], cwd: Path, env: dict, timeout: float) -> str:
    try:
        proc = await asyncio.create_subprocess_exec(
            *args,
            cwd=str(cwd),
            env=env,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=timeout)
    except TimeoutError as exc:
        raise GitError(f"git command timed out: {args[1]}") from exc
    except OSError as exc:
        raise GitError(f"git command failed to start: {args[1]}") from exc
    if proc.returncode != 0:
        detail = (stderr or stdout or b"")[-2000:].decode("utf-8", "replace")
        raise GitError(f"git {args[1]} failed ({proc.returncode}): {detail}")
    return stdout.decode("utf-8", "replace")


async def clone_repo(owner: str, repo: str, base_branch: str, issue_number: int) -> Path:
    root = Path(tempfile.mkdtemp(prefix=f"lhp-agent-issue-{issue_number}-"))
    destination = root / "repo"
    env = _git_env(root)
    await _run(
        [
            "git",
            "clone",
            "--depth",
            "1",
            "--branch",
            base_branch,
            f"https://github.com/{owner}/{repo}.git",
            str(destination),
        ],
        root,
        env,
        timeout=300,
    )
    branch = f"agent/issue-{issue_number}"
    await _run(["git", "checkout", "-b", branch], destination, env, timeout=30)
    return destination


async def commit_all(repo_dir: Path, message: str) -> bool:
    env = _git_env(repo_dir)
    await _run(["git", "add", "-A"], repo_dir, env, timeout=60)
    result = await _run(["git", "status", "--porcelain"], repo_dir, env, timeout=30)
    if not result.strip():
        return False
    await _run(["git", *_GIT_IDENTITY, "commit", "-m", message], repo_dir, env, timeout=60)
    return True


async def push_branch(repo_dir: Path, branch: str) -> None:
    await _run(
        ["git", "push", "origin", f"HEAD:refs/heads/{branch}"],
        repo_dir,
        _git_env(repo_dir),
        timeout=120,
    )

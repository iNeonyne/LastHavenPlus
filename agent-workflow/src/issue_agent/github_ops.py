"""GitHub REST helpers used by agent activities.

All functions raise :class:`GithubApiError` on non-2xx responses. Secrets are
read from the worker environment and never returned or logged.
"""

from __future__ import annotations

import os
from dataclasses import dataclass

import httpx

GITHUB_API = "https://api.github.com"

LABEL_QUEUED = "agent:fix"
LABEL_PR_OPEN = "agent:pr-opened"
LABEL_FAILED = "agent:failed"
LABEL_NEEDS_HUMAN = "agent:needs-human"

_SKIP_LABELS = {LABEL_FAILED, LABEL_NEEDS_HUMAN, LABEL_PR_OPEN}


class GithubApiError(RuntimeError):
    def __init__(self, status_code: int, message: str) -> None:
        super().__init__(message)
        self.status_code = status_code


def require_token() -> str:
    token = os.environ.get("GITHUB_PAT", "")
    if not token:
        raise GithubApiError(0, "GITHUB_PAT secret is not bound to the worker environment")
    return token


def _headers() -> dict[str, str]:
    return {
        "Authorization": f"Bearer {require_token()}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }


def _raise_for_status(response: httpx.Response) -> None:
    if response.status_code >= 400:
        raise GithubApiError(response.status_code, f"github_http_{response.status_code}")


@dataclass(frozen=True)
class IssueSummary:
    number: int
    title: str
    body: str
    html_url: str
    labels: list[str]

    def from_api(issue: dict) -> IssueSummary:
        return IssueSummary(
            number=int(issue["number"]),
            title=str(issue.get("title") or ""),
            body=(str(issue.get("body") or ""))[:20000],
            html_url=str(issue.get("html_url") or ""),
            labels=[
                lbl.get("name", "") for lbl in issue.get("labels", []) if isinstance(lbl, dict)
            ],
        )


async def list_open_agent_issues(owner: str, repo: str, label: str) -> list[IssueSummary]:
    """Open, unassigned-to-PR issues carrying the agent label."""
    async with httpx.AsyncClient(base_url=GITHUB_API, timeout=30, follow_redirects=False) as client:
        response = await client.get(
            f"/repos/{owner}/{repo}/issues",
            params={"labels": label, "state": "open", "per_page": 30},
            headers=_headers(),
        )
    _raise_for_status(response)
    body = response.json()
    return [IssueSummary.from_api(issue) for issue in body if "pull_request" not in issue]


async def check_submission_guard(
    owner: str,
    repo: str,
    issue: IssueSummary,
) -> tuple[bool, str]:
    """Return (skip, reason) when the issue already has a terminal agent state or PR."""
    blocking = sorted(_SKIP_LABELS & set(issue.labels))
    if blocking:
        return True, f"already carries label {blocking[0]}"
    branch = f"agent/issue-{issue.number}"
    async with httpx.AsyncClient(base_url=GITHUB_API, timeout=30, follow_redirects=False) as client:
        response = await client.get(
            f"/repos/{owner}/{repo}/pulls",
            params={"head": f"{owner}:{branch}", "state": "open", "per_page": 5},
            headers=_headers(),
        )
    _raise_for_status(response)
    pulls = response.json()
    if pulls:
        return True, f"PR already open: {pulls[0]['html_url']}"
    return False, ""


async def create_draft_pr(
    owner: str, repo: str, head: str, base: str, title: str, body: str
) -> dict:
    payload = {"title": title, "head": head, "base": base, "body": body, "draft": True}
    async with httpx.AsyncClient(base_url=GITHUB_API, timeout=30, follow_redirects=False) as client:
        response = await client.post(
            f"/repos/{owner}/{repo}/pulls", json=payload, headers=_headers()
        )
    _raise_for_status(response)
    pull = response.json()
    return {"number": pull["number"], "html_url": pull["html_url"]}


async def comment_on_issue(owner: str, repo: str, number: int, body: str) -> None:
    async with httpx.AsyncClient(base_url=GITHUB_API, timeout=30, follow_redirects=False) as client:
        response = await client.post(
            f"/repos/{owner}/{repo}/issues/{number}/comments",
            json={"body": body},
            headers=_headers(),
        )
    _raise_for_status(response)


async def add_label(owner: str, repo: str, number: int, label: str) -> None:
    async with httpx.AsyncClient(base_url=GITHUB_API, timeout=30, follow_redirects=False) as client:
        response = await client.post(
            f"/repos/{owner}/{repo}/issues/{number}/labels",
            json={"labels": [label]},
            headers=_headers(),
        )
    _raise_for_status(response)

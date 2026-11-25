"""issue_agent: scheduled GitHub issue agent for LastHavenPlus.

Scans open issues labelled ``agent:fix``, turns each into a commit on a pushed
``agent/issue-<n>`` branch and opens a draft PR against ``beta`` ready to test.
"""

from __future__ import annotations

from datetime import timedelta

import mistralai.workflows as workflows
from pydantic import BaseModel, Field

with workflows.workflow.unsafe.imports_passed_through():
    from issue_agent import coding_agent, github_ops


class IssueAgentInput(BaseModel):
    owner: str = "iNeonyne"
    repo: str = "LastHavenPlus"
    label: str = github_ops.LABEL_QUEUED
    base_branch: str = "beta"
    max_issues: int = Field(default=1, ge=1, le=5)
    dry_run: bool = False


class IssueOutcome(BaseModel):
    issue_number: int
    action: str
    pr_url: str = ""
    detail: str = ""


class IssueAgentOutput(BaseModel):
    checked: int = 0
    dry_run: bool = False
    outcomes: list[IssueOutcome] = Field(default_factory=list)


def _fail(response_status: int, detail: str) -> workflows.WorkflowError:
    retryable = response_status in {0, 429, 500, 502, 503, 504}
    return workflows.WorkflowError(detail, non_retryable=not retryable)


def _describe(exc: BaseException) -> str:
    parts: list[str] = []
    current: BaseException | None = exc
    while current is not None and len(parts) < 4:
        message = str(current) or type(current).__name__
        parts.append(f"{type(current).__name__}: {message}")
        current = getattr(current, "cause", None) or getattr(current, "__cause__", None)
    return " <- ".join(parts)[:2000]


@workflows.activity(
    name="issue_agent-list_open_issues",
    start_to_close_timeout=timedelta(seconds=45),
    retry_policy_max_attempts=3,
)
async def list_open_issues(data: IssueAgentInput) -> list:
    try:
        issues = await github_ops.list_open_agent_issues(data.owner, data.repo, data.label)
    except github_ops.GithubApiError as exc:
        raise _fail(exc.status_code, str(exc)) from None
    return [
        {
            "number": issue.number,
            "title": issue.title,
            "body": issue.body,
            "html_url": issue.html_url,
            "labels": issue.labels,
        }
        for issue in issues
    ]


@workflows.activity(
    name="issue_agent-check_submission_guard",
    start_to_close_timeout=timedelta(seconds=45),
    retry_policy_max_attempts=3,
)
async def check_submission_guard(data: IssueAgentInput, issue: dict) -> dict:
    from issue_agent.github_ops import IssueSummary

    summary = IssueSummary(
        number=int(issue["number"]),
        title=str(issue.get("title") or ""),
        body=str(issue.get("body") or ""),
        html_url=str(issue.get("html_url") or ""),
        labels=list(issue.get("labels") or []),
    )
    try:
        skip, reason = await github_ops.check_submission_guard(data.owner, data.repo, summary)
    except github_ops.GithubApiError as exc:
        raise _fail(exc.status_code, str(exc)) from None
    return {"skip": skip, "reason": reason}


@workflows.activity(
    name="issue_agent-implement_fix",
    start_to_close_timeout=timedelta(minutes=30),
    heartbeat_timeout=timedelta(minutes=5),
    retry_policy_max_attempts=1,
)
async def implement_fix(data: IssueAgentInput, issue: dict) -> dict:
    return await coding_agent.fix_issue(
        owner=data.owner,
        repo=data.repo,
        base_branch=data.base_branch,
        issue_number=int(issue["number"]),
        issue_title=str(issue.get("title") or ""),
        issue_body=str(issue.get("body") or ""),
    )


@workflows.activity(
    name="issue_agent-open_draft_pr",
    start_to_close_timeout=timedelta(seconds=45),
    retry_policy_max_attempts=2,
)
async def open_draft_pr(data: IssueAgentInput, issue: dict, implementation: dict) -> dict:
    if implementation.get("no_changes"):
        return {}
    number = int(issue["number"])
    title = f"agent: fix #{number} — {issue.get('title', '')}"[:200]
    summary = str(implementation.get("summary") or "(agent did not provide a summary)")
    body = (
        "Automated fix prepared by the LastHavenPlus issue agent.\n\n"
        f"Fixes #{number}\n\n"
        "## Summary\n"
        f"{summary}\n\n"
        "## Ready to test\n"
        "- CI on this PR must pass: unsigned `assembleRelease`, `testDebugUnitTest`, "
        "lint and Spotless/ktfmt.\n"
        "- Physical ARM64 validation is still required for device-behaviour claims.\n\n"
        "Prepared by the scheduled `issue_agent` workflow."
    )
    try:
        return await github_ops.create_draft_pr(
            owner=data.owner,
            repo=data.repo,
            head=str(implementation["branch"]),
            base=data.base_branch,
            title=title,
            body=body,
        )
    except github_ops.GithubApiError as exc:
        raise _fail(exc.status_code, str(exc)) from None


@workflows.activity(
    name="issue_agent_record_outcome",
    start_to_close_timeout=timedelta(seconds=45),
    retry_policy_max_attempts=3,
)
async def record_outcome(
    data: IssueAgentInput, issue: dict, action: str, pr: dict, detail: str
) -> None:
    number = int(issue["number"])
    try:
        if action == "pr_opened":
            comment = (
                f"Agent opened draft PR: {pr.get('html_url', '')}\n\n"
                f"{detail}\n\nReady to test: CI on the PR must pass before review."
            )
            await github_ops.comment_on_issue(data.owner, data.repo, number, comment)
            await github_ops.add_label(data.owner, data.repo, number, github_ops.LABEL_PR_OPEN)
        elif action in {"no_changes", "failed"}:
            await github_ops.comment_on_issue(
                data.owner,
                data.repo,
                number,
                f"Agent outcome `{action}`: {detail}",
            )
            await github_ops.add_label(
                data.owner,
                data.repo,
                number,
                github_ops.LABEL_NEEDS_HUMAN if action == "no_changes" else github_ops.LABEL_FAILED,
            )
    except github_ops.GithubApiError as exc:
        raise _fail(exc.status_code, str(exc)) from None


@workflows.workflow.define(name="issue_agent")
class IssueAgentWorkflow:
    @workflows.workflow.entrypoint
    async def run(self, data: IssueAgentInput) -> IssueAgentOutput:
        result = IssueAgentOutput(dry_run=data.dry_run)

        raw_issues = await list_open_issues(data)
        result.checked = len(raw_issues)
        if not raw_issues:
            return result

        for issue in raw_issues[: data.max_issues]:
            guard = await check_submission_guard(data, issue)
            if guard["skip"]:
                result.outcomes.append(
                    IssueOutcome(
                        issue_number=int(issue["number"]),
                        action="skipped",
                        detail=str(guard.get("reason") or ""),
                    )
                )
                continue

            if data.dry_run:
                result.outcomes.append(
                    IssueOutcome(
                        issue_number=int(issue["number"]),
                        action="planned_implement",
                        detail=str(issue.get("title") or ""),
                    )
                )
                continue

            try:
                implementation = await implement_fix(data, issue)
                if implementation.get("no_changes"):
                    outcome = IssueOutcome(
                        issue_number=int(issue["number"]),
                        action="no_changes",
                        detail=str(implementation.get("summary") or ""),
                    )
                    await record_outcome(data, issue, "no_changes", {}, outcome.detail)
                else:
                    pr = await open_draft_pr(data, issue, implementation)
                    outcome = IssueOutcome(
                        issue_number=int(issue["number"]),
                        action="pr_opened",
                        pr_url=str(pr.get("html_url") or ""),
                        detail=str(implementation.get("summary") or ""),
                    )
                    await record_outcome(data, issue, "pr_opened", pr, outcome.detail)
            except Exception as exc:  # noqa: BLE001 - route any failure to label + comment
                fallback = IssueOutcome(
                    issue_number=int(issue["number"]),
                    action="failed",
                    detail=_describe(exc),
                )
                await record_outcome(data, issue, "failed", {}, fallback.detail)
                result.outcomes.append(fallback)
                continue
            result.outcomes.append(outcome)

        return result

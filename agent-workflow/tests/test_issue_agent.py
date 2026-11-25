"""Workflow tests with mocked external effects (GitHub API, git, LLM)."""

from __future__ import annotations

import asyncio
import uuid
from datetime import timedelta

import pytest
from mistralai.workflows.testing import create_test_worker

from issue_agent import github_ops
from issue_agent.coding_agent import _resolve
from issue_agent.workflows.issue_agent import (
    IssueAgentWorkflow,
    check_submission_guard,
    implement_fix,
    list_open_issues,
    open_draft_pr,
    record_outcome,
)

ALL_ACTIVITIES = [
    list_open_issues,
    check_submission_guard,
    implement_fix,
    open_draft_pr,
    record_outcome,
]

WORKFLOW_EXECUTION_TIMEOUT = timedelta(seconds=15)

ISSUE = {
    "number": 9,
    "title": "Overlay accessibility",
    "body": "TalkBack order and labels for the launch overlay.",
    "html_url": "https://github.com/iNeonyne/LastHavenPlus/issues/9",
    "labels": [{"name": github_ops.LABEL_QUEUED}],
}


def _issue_summaries():
    return [
        github_ops.IssueSummary(
            number=ISSUE["number"],
            title=ISSUE["title"],
            body=ISSUE["body"],
            html_url=ISSUE["html_url"],
            labels=[github_ops.LABEL_QUEUED],
        )
    ]


async def _start(temporal_env, input_dict: dict) -> dict:
    handle = await temporal_env.client.start_workflow(
        "issue_agent",
        input_dict,
        id=f"test-issue-agent-{uuid.uuid4()}",
        task_queue="test-task-queue",
        execution_timeout=WORKFLOW_EXECUTION_TIMEOUT,
    )
    return await asyncio.wait_for(handle.result(), timeout=20)


async def test_dry_run_plans_implementations(temporal_env, monkeypatch) -> None:
    async def fake_list(owner, repo, label):
        return _issue_summaries()

    async def fake_guard(owner, repo, issue):
        return False, ""

    monkeypatch.setattr(github_ops, "list_open_agent_issues", fake_list)
    monkeypatch.setattr(github_ops, "check_submission_guard", fake_guard)
    async with create_test_worker(
        temporal_env, workflows=[IssueAgentWorkflow], activities=ALL_ACTIVITIES
    ):
        result = await _start(temporal_env, {"dry_run": True})

    assert result["checked"] == 1
    assert result["outcomes"][0]["action"] == "planned_implement"


async def test_guard_skip_reports_reason(temporal_env, monkeypatch) -> None:
    async def fake_list(owner, repo, label):
        return _issue_summaries()

    async def fake_guard(owner, repo, issue):
        return True, "PR already open: url"

    monkeypatch.setattr(github_ops, "list_open_agent_issues", fake_list)
    monkeypatch.setattr(github_ops, "check_submission_guard", fake_guard)
    async with create_test_worker(
        temporal_env, workflows=[IssueAgentWorkflow], activities=ALL_ACTIVITIES
    ):
        result = await _start(temporal_env, {"dry_run": True})

    assert result["outcomes"][0]["action"] == "skipped"
    assert "PR already open" in result["outcomes"][0]["detail"]


async def test_full_run_opens_draft_pr_and_records_outcome(temporal_env, monkeypatch) -> None:
    observed: dict = {}

    async def fake_list(owner, repo, label):
        return _issue_summaries()

    async def fake_guard(owner, repo, issue):
        return False, ""

    async def fake_fix(**kwargs):
        return {
            "branch": "agent/issue-9",
            "no_changes": False,
            "summary": "Adjusted overlay semantics and added tests.",
            "tool_call_count": 7,
        }

    async def fake_publish(owner, repo, head, base, title, body):
        observed["head"] = head
        observed["base"] = base
        observed["title"] = title
        return {"number": 99, "html_url": "https://github.com/iNeonyne/LastHavenPlus/pull/99"}

    async def fake_comment(owner, repo, number, body_text):
        observed["comment_number"] = number
        observed["comment"] = body_text

    async def fake_add_label(owner, repo, number, label):
        observed["label"] = label

    monkeypatch.setattr(github_ops, "list_open_agent_issues", fake_list)
    monkeypatch.setattr(github_ops, "check_submission_guard", fake_guard)
    monkeypatch.setattr("issue_agent.coding_agent.fix_issue", fake_fix)
    monkeypatch.setattr(github_ops, "create_draft_pr", fake_publish)
    monkeypatch.setattr(github_ops, "comment_on_issue", fake_comment)
    monkeypatch.setattr(github_ops, "add_label", fake_add_label)

    async with create_test_worker(
        temporal_env, workflows=[IssueAgentWorkflow], activities=ALL_ACTIVITIES
    ):
        result = await _start(temporal_env, {"dry_run": False})

    assert result["outcomes"][0]["action"] == "pr_opened"
    assert result["outcomes"][0]["pr_url"].endswith("/pull/99")
    assert observed["head"] == "agent/issue-9"
    assert observed["base"] == "beta"
    assert observed["title"].startswith("agent: fix #9")
    assert "pull/99" in observed["comment"]
    assert observed["label"] == github_ops.LABEL_PR_OPEN


async def test_implement_failure_routes_to_failed_label(temporal_env, monkeypatch) -> None:
    observed: dict = {}

    async def fake_list(owner, repo, label):
        return _issue_summaries()

    async def fake_guard(owner, repo, issue):
        return False, ""

    async def failing(**kwargs):
        raise RuntimeError("clone failed")

    async def fake_comment(owner, repo, number, body_text):
        observed["comment"] = body_text

    async def fake_add_label(owner, repo, number, label):
        observed["label"] = label

    monkeypatch.setattr(github_ops, "list_open_agent_issues", fake_list)
    monkeypatch.setattr(github_ops, "check_submission_guard", fake_guard)
    monkeypatch.setattr("issue_agent.coding_agent.fix_issue", failing)
    monkeypatch.setattr(github_ops, "comment_on_issue", fake_comment)
    monkeypatch.setattr(github_ops, "add_label", fake_add_label)

    async with create_test_worker(
        temporal_env, workflows=[IssueAgentWorkflow], activities=ALL_ACTIVITIES
    ):
        result = await _start(temporal_env, {"dry_run": False})

    assert result["outcomes"][0]["action"] == "failed"
    detail = result["outcomes"][0]["detail"]
    assert "clone failed" in detail or "RuntimeError" in detail
    assert observed["label"] == github_ops.LABEL_FAILED


def test_resolve_blocks_protected_files(tmp_path) -> None:
    (tmp_path / "AGENTS.md").write_text("protected")
    (tmp_path / ".github").mkdir()
    (tmp_path / "app").mkdir()
    with pytest.raises(ValueError):
        _resolve(tmp_path, "AGENTS.md")
    with pytest.raises(ValueError):
        _resolve(tmp_path, ".github/workflows/android.yml")
    with pytest.raises(ValueError):
        _resolve(tmp_path, "../outside.txt")
    assert _resolve(tmp_path, "app/src/main/kotlin/Example.kt").name == "Example.kt"

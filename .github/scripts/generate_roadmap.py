#!/usr/bin/env python3
"""Generate the live LastHavenPlus roadmap from GitHub Issues and pull requests."""
import datetime as dt
import json
import os
import urllib.parse
import urllib.request

REPOSITORY = os.environ["GITHUB_REPOSITORY"]
TOKEN = os.environ["GH_TOKEN"]
API = "https://api.github.com"

def get_pages(path):
    results = []
    page = 1
    while True:
        url = f"{API}/repos/{REPOSITORY}/{path}"
        separator = "&" if "?" in url else "?"
        request = urllib.request.Request(
            f"{url}{separator}per_page=100&page={page}",
            headers={
                "Accept": "application/vnd.github+json",
                "Authorization": f"Bearer {TOKEN}",
                "X-GitHub-Api-Version": "2022-11-28",
                "User-Agent": "LastHavenPlus-roadmap-sync",
            },
        )
        with urllib.request.urlopen(request, timeout=30) as response:
            batch = json.load(response)
        if not batch:
            break
        results.extend(batch)
        if len(batch) < 100:
            break
        page += 1
    return results

def priority(issue):
    names = {label["name"] for label in issue.get("labels", [])}
    for key in ("P0", "P0.1", "P1", "P2", "P3"):
        if key in names:
            return key
    return "Unprioritized"

def issue_line(issue):
    labels = [label["name"] for label in issue.get("labels", []) if not label["name"].startswith("agent:")]
    suffix = " — " + ", ".join(labels) if labels else ""
    return f"- [#{issue['number']} — {issue['title']}]({issue['html_url']}){suffix}"

open_items = [item for item in get_pages("issues?state=open") if "pull_request" not in item]
open_prs = get_pages("pulls?state=open")
since = (dt.datetime.now(dt.timezone.utc) - dt.timedelta(days=30)).isoformat()
recent_closed = [item for item in get_pages("issues?state=closed&since=" + urllib.parse.quote(since)) if "pull_request" not in item]
groups = ["P0", "P0.1", "P1", "P2", "P3", "Unprioritized"]
now = dt.datetime.now(dt.timezone.utc).replace(microsecond=0).isoformat()
lines = [
    "# LastHavenPlus roadmap", "",
    f"Generated: {now}",
    "Source of truth: live GitHub Issues, pull requests and labels. This file is generated automatically; do not edit it manually.",
    "",
    "Every open issue is listed below, grouped by its current priority. Opening a newer issue or PR does not supersede an older one. Duplicates and supersession must be explicitly reviewed and linked in GitHub.",
    "", "## Active work", "",
]
for group in groups:
    selected = sorted((i for i in open_items if priority(i) == group), key=lambda x: x["number"])
    if selected:
        lines.extend([f"### {group}", ""])
        lines.extend(issue_line(issue) for issue in selected)
        lines.append("")
if not open_items:
    lines.extend(["No open issues.", ""])
lines.extend(["## Open pull requests", ""])
if open_prs:
    for pr in sorted(open_prs, key=lambda x: x["number"]):
        lines.append(f"- [#{pr['number']} — {pr['title']}]({pr['html_url']}) ({pr['base']['ref']} <- {pr['head']['ref']})")
else:
    lines.append("No open pull requests.")
lines.extend(["", "## Recently completed (last 30 days)", ""])
if recent_closed:
    for issue in sorted(recent_closed, key=lambda x: x["number"], reverse=True):
        lines.append(f"- [#{issue['number']} — {issue['title']}]({issue['html_url']})")
else:
    lines.append("No issues closed in the last 30 days.")
lines.extend([
    "", "## Operating principles", "",
    "- Chaty triages new issues and records a clear next step within 1–2 days.",
    "- Resolve work through coding, debugging and automated tests; physical-device testing is not a project requirement.",
    "- New issues and PRs never automatically make older ones obsolete.",
    "- GitHub Issues hold scope and acceptance criteria; PRs and Actions hold implementation and verification evidence.",
    "- Release and promotion decisions remain controlled by the agent orchestration workflow.", "",
])
with open("ROADMAP.md", "w", encoding="utf-8", newline="\n") as output:
    output.write("\n".join(lines))
print(f"Generated roadmap for {len(open_items)} open issues, {len(open_prs)} open PRs, and {len(recent_closed)} recently closed issues.")

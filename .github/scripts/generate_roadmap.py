#!/usr/bin/env python3
"""Update only the generated live inventory section of the LastHavenPlus roadmap."""
import datetime as dt
import json
import os
import urllib.parse
import urllib.request

API = "https://api.github.com"
LIVE_START = "<!-- ROADMAP-LIVE-START -->"
LIVE_END = "<!-- ROADMAP-LIVE-END -->"


def get_pages(path):
    results = []
    page = 1
    while True:
        repository = os.environ["GITHUB_REPOSITORY"]
        token = os.environ["GH_TOKEN"]
        url = f"{API}/repos/{repository}/{path}"
        separator = "&" if "?" in url else "?"
        request = urllib.request.Request(
            f"{url}{separator}per_page=100&page={page}",
            headers={
                "Accept": "application/vnd.github+json",
                "Authorization": f"Bearer {token}",
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
    labels = [
        label["name"]
        for label in issue.get("labels", [])
        if not label["name"].startswith("agent:")
    ]
    suffix = " — " + ", ".join(labels) if labels else ""
    return f"- [#{issue['number']} — {issue['title']}]({issue['html_url']}){suffix}"


def build_live_section(open_items, open_prs, recent_closed, generated_at):
    groups = ("P0", "P0.1", "P1", "P2", "P3", "Unprioritized")
    lines = [
        LIVE_START,
        "## Live issue and PR inventory",
        "",
        f"Generated: {generated_at}",
        "Source of truth: live GitHub Issues, pull requests and labels. This section is generated automatically; do not edit it manually.",
        "",
        "Every open issue is listed below, grouped by its current priority. Opening a newer issue or PR does not supersede an older one. Duplicates and supersession must be explicitly reviewed and linked in GitHub.",
        "",
        "### Active issues",
        "",
    ]
    for group in groups:
        selected = sorted(
            (item for item in open_items if priority(item) == group),
            key=lambda item: item["number"],
        )
        if selected:
            lines.extend([f"#### {group}", ""])
            lines.extend(issue_line(item) for item in selected)
            lines.append("")
    if not open_items:
        lines.extend(["No open issues.", ""])

    lines.extend(["### Open pull requests", ""])
    if open_prs:
        for pr in sorted(open_prs, key=lambda item: item["number"]):
            lines.append(
                f"- [#{pr['number']} — {pr['title']}]({pr['html_url']}) "
                f"({pr['base']['ref']} <- {pr['head']['ref']})"
            )
    else:
        lines.append("No open pull requests.")

    lines.extend(["", "### Recently completed (last 30 days)", ""])
    if recent_closed:
        for issue in sorted(recent_closed, key=lambda item: item["number"], reverse=True):
            lines.append(f"- [#{issue['number']} — {issue['title']}]({issue['html_url']})")
    else:
        lines.append("No issues closed in the last 30 days.")
    lines.extend(["", LIVE_END, ""])
    return "\n".join(lines)


def replace_live_section(existing, generated_section):
    has_start = LIVE_START in existing
    has_end = LIVE_END in existing
    if has_start != has_end:
        raise ValueError("ROADMAP.md has an incomplete generated-section marker pair")
    if not has_start:
        return existing.rstrip() + "\n\n" + generated_section.rstrip() + "\n"

    start = existing.index(LIVE_START)
    end = existing.index(LIVE_END)
    if end < start:
        raise ValueError("ROADMAP.md generated-section markers are out of order")
    end += len(LIVE_END)
    prefix = existing[:start].rstrip()
    suffix = existing[end:].lstrip("\n")
    result = prefix + "\n\n" + generated_section.rstrip()
    if suffix:
        result += "\n\n" + suffix
    return result + "\n"


def main():
    now = dt.datetime.now(dt.timezone.utc).replace(microsecond=0)
    since = now - dt.timedelta(days=30)
    open_items = [
        item for item in get_pages("issues?state=open")
        if "pull_request" not in item
    ]
    open_prs = get_pages("pulls?state=open")
    closed_candidates = get_pages(
        "issues?state=closed&since=" + urllib.parse.quote(since.isoformat())
    )
    recent_closed = []
    for item in closed_candidates:
        closed_at = item.get("closed_at")
        if "pull_request" in item or not closed_at:
            continue
        parsed = dt.datetime.fromisoformat(closed_at.replace("Z", "+00:00"))
        if parsed >= since:
            recent_closed.append(item)

    generated = build_live_section(
        open_items,
        open_prs,
        recent_closed,
        now.isoformat(),
    )
    with open("ROADMAP.md", "r", encoding="utf-8") as source:
        existing = source.read()
    updated = replace_live_section(existing, generated)
    with open("ROADMAP.md", "w", encoding="utf-8", newline="\n") as output:
        output.write(updated)
    print(
        f"Generated roadmap inventory for {len(open_items)} open issues, "
        f"{len(open_prs)} open PRs, and {len(recent_closed)} recently closed issues."
    )


if __name__ == "__main__":
    main()

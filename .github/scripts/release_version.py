#!/usr/bin/env python3
"""Select a SemVer release for a commit merged into stable main.

Only Conventional Commits with a release-bearing type create a version:
feat -> minor; fix/perf/refactor/revert/security -> patch; a breaking marker
or ! suffix -> major. Other commit types are build-only. An explicit
[release:X.Y.Z] marker remains supported for controlled promotions.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path
from typing import Iterable

VERSION_RE = re.compile(r"^(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$")
EXPLICIT_RE = re.compile(r"\\[release:((?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*))\\]", re.IGNORECASE)
CONVENTIONAL_RE = re.compile(
    r"^(?P<type>[a-zA-Z][a-zA-Z0-9_-]*)(?:\\([^()\\r\\n]+\\))?(?P<breaking>!)?:\\s+.+$"
)
RELEASE_TYPES = {"feat", "feature", "fix", "perf", "refactor", "revert", "security"}
MAX_COMPONENT = 999


def parse_version(value: str) -> tuple[int, int, int] | None:
    match = VERSION_RE.fullmatch(value.removeprefix("v"))
    if not match:
        return None
    version = tuple(int(part) for part in match.groups())
    if any(part > MAX_COMPONENT for part in version):
        return None
    return version


def versions_from_tags(tags: Iterable[str]) -> list[tuple[int, int, int]]:
    versions: list[tuple[int, int, int]] = []
    for tag in tags:
        parsed = parse_version(tag.strip())
        if parsed is not None:
            versions.append(parsed)
    return versions


def format_version(version: tuple[int, int, int]) -> str:
    return ".".join(str(part) for part in version)


def resolve_release_version(
    commit_message: str,
    merged_tags: Iterable[str],
    head_tags: Iterable[str] = (),
) -> str | None:
    """Return the version to tag, or None when this commit is not a release."""
    merged_versions = versions_from_tags(merged_tags)
    head_versions = versions_from_tags(head_tags)
    latest = max(merged_versions, default=(0, 0, 0))

    explicit = EXPLICIT_RE.search(commit_message)
    if explicit:
        requested = parse_version(explicit.group(1))
        assert requested is not None
        if requested in head_versions:
            return format_version(requested)
        if requested <= latest:
            raise ValueError(
                f"Explicit release v{format_version(requested)} must be newer than "
                f"the latest main-history tag v{format_version(latest)}."
            )
        return format_version(requested)

    # Makes a rerun of an already-tagged main commit idempotent.
    if head_versions:
        return format_version(max(head_versions))

    lines = commit_message.splitlines()
    subject = lines[0].strip() if lines else ""
    match = CONVENTIONAL_RE.fullmatch(subject)
    if not match:
        return None

    is_breaking = bool(match.group("breaking")) or bool(
        re.search(r"^BREAKING[ -]CHANGE:\\s*\\S", commit_message, re.IGNORECASE | re.MULTILINE)
    )
    commit_type = match.group("type").lower()
    if is_breaking:
        bump = "major"
    elif commit_type in {"feat", "feature"}:
        bump = "minor"
    elif commit_type in RELEASE_TYPES:
        bump = "patch"
    else:
        return None

    major, minor, patch = latest
    if bump == "major":
        next_version = (major + 1, 0, 0)
    elif bump == "minor":
        next_version = (major, minor + 1, 0)
    else:
        next_version = (major, minor, patch + 1)

    if any(part > MAX_COMPONENT for part in next_version):
        raise ValueError("The next version exceeds the supported 999 component limit.")
    return format_version(next_version)


def main() -> int:
    if len(sys.argv) != 3:
        print(
            "Usage: release_version.py MERGED_TAGS_FILE HEAD_TAGS_FILE",
            file=sys.stderr,
        )
        return 2

    merged_tags = Path(sys.argv[1]).read_text(encoding="utf-8").splitlines()
    head_tags = Path(sys.argv[2]).read_text(encoding="utf-8").splitlines()
    commit_message = sys.stdin.read()
    try:
        version = resolve_release_version(commit_message, merged_tags, head_tags)
    except ValueError as error:
        print(f"release version error: {error}", file=sys.stderr)
        return 1

    if version is not None:
        print(version)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

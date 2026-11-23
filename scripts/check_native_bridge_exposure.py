#!/usr/bin/env python3
"""Fail CI if app source introduces a direct JavaScript-to-native bridge."""

from pathlib import Path
import re
import sys

PATTERNS = (
    (re.compile(r"\\.addJavascriptInterface\\s*\\("), "addJavascriptInterface registration"),
    (re.compile(r"@JavascriptInterface\\b"), "@JavascriptInterface method"),
)


def find_unsafe_bridges(source_root: Path) -> list[str]:
    findings = []
    if not source_root.exists():
        raise FileNotFoundError(f"Android source directory not found: {source_root}")
    for path in sorted((*source_root.rglob("*.kt"), *source_root.rglob("*.java"))):
        content = path.read_text(encoding="utf-8")
        for number, line in enumerate(content.splitlines(), 1):
            for pattern, description in PATTERNS:
                if pattern.search(line):
                    findings.append(f"{path}:{number}: {description}")
    return findings


def main() -> int:
    root = Path(__file__).resolve().parents[1] / "app" / "src" / "main"
    findings = find_unsafe_bridges(root)
    if findings:
        print("Unreviewed JavaScript-to-native bridge detected:", file=sys.stderr)
        print("\\n".join(findings), file=sys.stderr)
        return 1
    print("No direct JavaScript-to-native interfaces found in Android app source.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Compare two independently built unsigned APK ZIP archives."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
import zipfile
from pathlib import Path
from typing import Any


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def inspect_apk(path: Path) -> dict[str, Any]:
    if not path.is_file() or path.stat().st_size == 0:
        raise ValueError(f"APK is missing or empty: {path}")

    entries: dict[str, dict[str, Any]] = {}
    duplicates: list[str] = []
    with zipfile.ZipFile(path) as archive:
        bad_entry = archive.testzip()
        if bad_entry is not None:
            raise ValueError(f"Corrupt ZIP entry in {path}: {bad_entry}")
        for info in archive.infolist():
            if info.filename in entries:
                duplicates.append(info.filename)
                continue
            content = archive.read(info)
            entries[info.filename] = {
                "size": info.file_size,
                "compressed_size": info.compress_size,
                "crc": f"{info.CRC:08x}",
                "compression": info.compress_type,
                "timestamp": list(info.date_time),
                "sha256": hashlib.sha256(content).hexdigest(),
            }

    return {
        "path": str(path),
        "size": path.stat().st_size,
        "sha256": sha256_file(path),
        "entries": entries,
        "duplicates": sorted(duplicates),
    }


def compare_apks(first: Path, second: Path) -> dict[str, Any]:
    a = inspect_apk(first)
    b = inspect_apk(second)
    names_a = set(a["entries"])
    names_b = set(b["entries"])
    mismatched = []
    for name in sorted(names_a & names_b):
        if a["entries"][name] != b["entries"][name]:
            mismatched.append({
                "name": name,
                "build_a": a["entries"][name],
                "build_b": b["entries"][name],
            })

    identical = (
        a["sha256"] == b["sha256"]
        and not a["duplicates"]
        and not b["duplicates"]
    )
    return {
        "byte_identical": identical,
        "build_a": {k: v for k, v in a.items() if k != "entries"},
        "build_b": {k: v for k, v in b.items() if k != "entries"},
        "entry_count_a": len(names_a),
        "entry_count_b": len(names_b),
        "missing_from_b": sorted(names_a - names_b),
        "missing_from_a": sorted(names_b - names_a),
        "entry_metadata_or_content_mismatches": mismatched,
        "duplicate_entries_a": a["duplicates"],
        "duplicate_entries_b": b["duplicates"],
        "entry_sets_and_contents_match": (
            names_a == names_b and not mismatched
            and not a["duplicates"] and not b["duplicates"]
        ),
    }


def markdown_report(result: dict[str, Any]) -> str:
    status = "BYTE-IDENTICAL" if result["byte_identical"] else "NOT BYTE-IDENTICAL"
    lines = [
        "# Independent unsigned APK rebuild comparison",
        "",
        f"**Result: {status}**",
        "",
        "| Measurement | Build A | Build B |",
        "|---|---:|---:|",
        f"| APK size | {result['build_a']['size']:,} bytes | {result['build_b']['size']:,} bytes |",
        f"| SHA-256 | `{result['build_a']['sha256']}` | `{result['build_b']['sha256']}` |",
        f"| ZIP entries | {result['entry_count_a']} | {result['entry_count_b']} |",
        "",
        f"- Entry sets and contents match: **{result['entry_sets_and_contents_match']}**",
        f"- Missing from build B: {len(result['missing_from_b'])}",
        f"- Missing from build A: {len(result['missing_from_a'])}",
        f"- Entry metadata/content mismatches: {len(result['entry_metadata_or_content_mismatches'])}",
        f"- Duplicate ZIP entries in build A/B: {len(result['duplicate_entries_a'])}/{len(result['duplicate_entries_b'])}",
        "",
        "## Entry-level differences",
        "",
    ]
    for label, key in (
        ("Missing from build B", "missing_from_b"),
        ("Missing from build A", "missing_from_a"),
    ):
        lines.append(f"### {label}")
        lines.extend(f"- `{name}`" for name in result[key])
        if not result[key]:
            lines.append("- None")
        lines.append("")

    lines.append("### Metadata or content mismatches")
    if result["entry_metadata_or_content_mismatches"]:
        for item in result["entry_metadata_or_content_mismatches"]:
            lines.append(
                f"- `{item['name']}`: size {item['build_a']['size']} → "
                f"{item['build_b']['size']}; SHA-256 "
                f"`{item['build_a']['sha256']}` → `{item['build_b']['sha256']}`; "
                f"timestamp {item['build_a']['timestamp']} → {item['build_b']['timestamp']}"
            )
    else:
        lines.append("- None")
    lines.extend([
        "",
        "The comparison checks the complete APK SHA-256 and each ZIP entry's "
        "uncompressed content hash, size, compressed size, CRC, compression "
        "method and timestamp. A whole-file mismatch with matching entries "
        "indicates differences in ZIP/container-level bytes or metadata.",
        "",
        "This is an unsigned build comparison only. It does not test signing "
        "identity, release publication, physical-device behavior or F-Droid compatibility.",
        "",
    ])
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("build_a", type=Path)
    parser.add_argument("build_b", type=Path)
    parser.add_argument("--json", type=Path, default=Path("reproducibility-report.json"))
    parser.add_argument("--markdown", type=Path, default=Path("reproducibility-report.md"))
    args = parser.parse_args()

    try:
        result = compare_apks(args.build_a, args.build_b)
        args.json.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
        args.markdown.write_text(markdown_report(result), encoding="utf-8")
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"APK comparison failed: {error}", file=sys.stderr)
        return 2

    print(markdown_report(result))
    return 0 if result["byte_identical"] else 1


if __name__ == "__main__":
    raise SystemExit(main())

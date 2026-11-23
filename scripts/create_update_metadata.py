#!/usr/bin/env python3
"""Create the main-only machine-readable APK update manifest."""

from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path
from typing import Mapping

APP_PACKAGE_ID = "com.lasthavens.android"
WORKFLOW_ID = 427170801
ABI_VARIANTS = ("arm64-v8a", "armeabi-v7a", "x86_64", "universal")
MAX_APK_BYTES = 200 * 1024 * 1024
VERSION_PATTERN = re.compile(r"^[0-9]+\.[0-9]+\.[0-9]+$")
COMMIT_PATTERN = re.compile(r"^[0-9a-fA-F]{40}$")


def _positive_int(environment: Mapping[str, str], key: str) -> int:
    try:
        value = int(environment[key])
    except (KeyError, TypeError, ValueError) as error:
        raise ValueError(f"{key} must be a positive integer") from error
    if value <= 0:
        raise ValueError(f"{key} must be a positive integer")
    return value


def _sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def create_update_metadata(
    dist_dir: Path,
    app_gradle_file: Path,
    environment: Mapping[str, str],
) -> dict:
    """Validate main CI context and write dist/update.json."""
    if environment.get("GITHUB_REF") != "refs/heads/main":
        raise ValueError("Update metadata may only be generated for refs/heads/main")

    workflow_id = _positive_int(environment, "WORKFLOW_ID")
    if workflow_id != WORKFLOW_ID:
        raise ValueError("The build workflow ID changed; migrate the updater before proceeding")

    run_id = _positive_int(environment, "GITHUB_RUN_ID")
    run_number = _positive_int(environment, "GITHUB_RUN_NUMBER")
    if run_number <= 2:
        raise ValueError("The CI versionCode must exceed legacy distributed versionCode 2")

    commit_sha = environment.get("GITHUB_SHA", "")
    if not COMMIT_PATTERN.fullmatch(commit_sha):
        raise ValueError("GITHUB_SHA must be a 40-character commit SHA")

    gradle_text = app_gradle_file.read_text(encoding="utf-8")
    version_match = re.search(r'val appVersionName = "([^"]+)"', gradle_text)
    if version_match is None:
        raise ValueError("Could not read appVersionName from app/build.gradle.kts")
    version_name = version_match.group(1)
    if not VERSION_PATTERN.fullmatch(version_name):
        raise ValueError("Main update artifacts require a stable MAJOR.MINOR.PATCH versionName")

    apk_records = []
    for abi in ABI_VARIANTS:
        file_name = f"LastHavenPlus-{version_name}-{abi}.apk"
        apk_path = dist_dir / file_name
        if not apk_path.is_file():
            raise ValueError(f"Missing APK variant: {file_name}")
        size_bytes = apk_path.stat().st_size
        if not 0 < size_bytes <= MAX_APK_BYTES:
            raise ValueError(f"APK size is invalid or exceeds the update limit: {file_name}")
        apk_records.append({
            "abi": abi,
            "file": file_name,
            "sha256": _sha256_file(apk_path),
            "sizeBytes": size_bytes,
        })

    metadata = {
        "schemaVersion": 1,
        "packageName": APP_PACKAGE_ID,
        "versionName": version_name,
        "versionCode": run_number,
        "branch": "main",
        "ref": "refs/heads/main",
        "commitSha": commit_sha,
        "workflowRunId": run_id,
        "workflowRunNumber": run_number,
        "workflowId": workflow_id,
        "apks": apk_records,
    }
    output = dist_dir / "update.json"
    output.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    return metadata


def main() -> None:
    import os
    metadata = create_update_metadata(
        Path("dist"),
        Path("app/build.gradle.kts"),
        os.environ,
    )
    print(json.dumps(metadata, indent=2))


if __name__ == "__main__":
    main()

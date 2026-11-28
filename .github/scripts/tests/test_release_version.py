from __future__ import annotations

import importlib.util
import subprocess
import unittest
from pathlib import Path
import sys


SCRIPT = Path(__file__).resolve().parents[1] / "release_version.py"
SPEC = importlib.util.spec_from_file_location("release_version", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
release_version = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(release_version)


class ReleaseVersionTests(unittest.TestCase):
    def setUp(self) -> None:
        self.tags = ["v1.0.0", "v2.4.0", "v2.3.9", "not-a-version", "v2.5.0-rc.1"]

    def test_feature_bumps_minor_from_latest_main_tag(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version("feat(addons): add manager\n", self.tags),
            "2.5.0",
        )

    def test_fix_bumps_patch(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version("fix(updater): retry failed downloads", self.tags),
            "2.4.1",
        )

    def test_perf_refactor_and_revert_bump_patch(self) -> None:
        for subject in (
            "perf(webview): reduce startup cost",
            "refactor(storage): simplify library access",
            "revert: restore previous navigation behavior",
            "security: validate downloaded archive",
        ):
            with self.subTest(subject=subject):
                self.assertEqual(
                    release_version.resolve_release_version(subject, self.tags),
                    "2.4.1",
                )

    def test_breaking_bang_bumps_major(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version("feat(api)!: remove legacy bridge", self.tags),
            "3.0.0",
        )

    def test_breaking_footer_bumps_major(self) -> None:
        message = "fix(api): change public callback contract\n\nBREAKING CHANGE: callback now returns a result."
        self.assertEqual(
            release_version.resolve_release_version(message, self.tags),
            "3.0.0",
        )

    def test_documentation_and_maintenance_commits_do_not_release(self) -> None:
        for subject in (
            "docs: update the roadmap",
            "chore(ci): tune cache settings",
            "test: add another unit test",
            "style(ui): format composables",
            "ci: update workflow",
        ):
            with self.subTest(subject=subject):
                self.assertIsNone(
                    release_version.resolve_release_version(subject, self.tags)
                )

    def test_explicit_version_marker_is_supported(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version(
                "release: promote candidate [release:2.6.0]", self.tags
            ),
            "2.6.0",
        )

    def test_explicit_version_cannot_regress_or_reuse_published_version(self) -> None:
        with self.assertRaises(ValueError):
            release_version.resolve_release_version(
                "release: old candidate [release:2.3.0]", self.tags
            )

    def test_existing_tag_at_head_makes_retries_idempotent(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version(
                "feat(addons): add manager",
                self.tags,
                ["v2.5.0"],
            ),
            "2.5.0",
        )

    def test_explicit_current_head_tag_is_idempotent(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version(
                "release: rerun [release:2.4.0]",
                self.tags,
                ["v2.4.0"],
            ),
            "2.4.0",
        )

    def test_version_components_are_bounded(self) -> None:
        with self.assertRaises(ValueError):
            release_version.resolve_release_version(
                "feat: one more feature", ["v999.999.999"]
            )

    def test_cli_writes_only_the_computed_version(self) -> None:
        with __import__("tempfile").TemporaryDirectory() as temp_dir:
            merged = Path(temp_dir) / "merged.txt"
            head = Path(temp_dir) / "head.txt"
            merged.write_text("v2.4.0\\n", encoding="utf-8")
            head.write_text("", encoding="utf-8")
            result = subprocess.run(
                [sys.executable, str(SCRIPT), str(merged), str(head)],
                input="fix: correct a crash",
                check=True,
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.stdout.strip(), "2.4.1")

    def test_first_release_starts_from_zero(self) -> None:
        self.assertEqual(
            release_version.resolve_release_version("feat: first stable feature", []),
            "0.1.0",
        )


if __name__ == "__main__":
    unittest.main()

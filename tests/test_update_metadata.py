import json
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from scripts.create_update_metadata import (  # noqa: E402
    ABI_VARIANTS,
    APP_PACKAGE_ID,
    WORKFLOW_ID,
    create_update_metadata,
)


class CreateUpdateMetadataTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.dist = self.root / "dist"
        self.dist.mkdir()
        self.gradle_file = self.root / "app.build.gradle.kts"
        self.gradle_file.write_text('val appVersionName = "2.1.0"\\n', encoding="utf-8")
        self.environment = {
            "GITHUB_REF": "refs/heads/main",
            "GITHUB_RUN_ID": "123456",
            "GITHUB_RUN_NUMBER": "171",
            "GITHUB_SHA": "0123456789abcdef0123456789abcdef01234567",
            "WORKFLOW_ID": str(WORKFLOW_ID),
        }
        for abi in ABI_VARIANTS:
            (self.dist / f"LastHavenPlus-2.1.0-{abi}.apk").write_bytes(f"fake-apk-{abi}".encode())

    def tearDown(self):
        self.temp.cleanup()

    def test_writes_metadata_and_exact_hashes(self):
        metadata = create_update_metadata(self.dist, self.gradle_file, self.environment)
        self.assertEqual(metadata["packageName"], APP_PACKAGE_ID)
        self.assertEqual(metadata["versionName"], "2.1.0")
        self.assertEqual(metadata["versionCode"], 171)
        self.assertEqual(metadata["branch"], "main")
        self.assertEqual(metadata["ref"], "refs/heads/main")
        self.assertEqual(metadata["commitSha"], self.environment["GITHUB_SHA"])
        self.assertEqual(metadata["workflowRunId"], 123456)
        self.assertEqual(metadata["workflowRunNumber"], 171)
        self.assertEqual(metadata["workflowId"], WORKFLOW_ID)
        self.assertEqual({entry["abi"] for entry in metadata["apks"]}, set(ABI_VARIANTS))
        raw = (self.dist / "update.json").read_text(encoding="utf-8")
        self.assertTrue(raw.endswith("\\n"))
        self.assertEqual(json.loads(raw), metadata)
        for entry in metadata["apks"]:
            apk = self.dist / entry["file"]
            self.assertEqual(entry["sizeBytes"], apk.stat().st_size)
            self.assertEqual(len(entry["sha256"]), 64)

    def test_rejects_beta_tags_and_pull_requests(self):
        for ref in ("refs/heads/beta", "refs/tags/v2.1.0", "refs/pull/10/merge"):
            with self.subTest(ref=ref):
                with self.assertRaises(ValueError):
                    create_update_metadata(self.dist, self.gradle_file, dict(self.environment, GITHUB_REF=ref))

    def test_rejects_prerelease_version_names(self):
        self.gradle_file.write_text('val appVersionName = "2.1.0-beta.1"\\n', encoding="utf-8")
        with self.assertRaises(ValueError):
            create_update_metadata(self.dist, self.gradle_file, self.environment)

    def test_rejects_missing_abi_variant(self):
        (self.dist / "LastHavenPlus-2.1.0-x86_64.apk").unlink()
        with self.assertRaises(ValueError):
            create_update_metadata(self.dist, self.gradle_file, self.environment)

    def test_rejects_wrong_workflow_and_invalid_commit(self):
        for key, value in (("WORKFLOW_ID", "1"), ("GITHUB_SHA", "invalid")):
            with self.subTest(key=key):
                with self.assertRaises(ValueError):
                    create_update_metadata(self.dist, self.gradle_file, dict(self.environment, **{key: value}))

    def test_rejects_legacy_or_malformed_run_number(self):
        for run_number in ("2", "0", "not-a-number"):
            with self.subTest(run_number=run_number):
                with self.assertRaises(ValueError):
                    create_update_metadata(self.dist, self.gradle_file, dict(self.environment, GITHUB_RUN_NUMBER=run_number))


if __name__ == "__main__":
    unittest.main()

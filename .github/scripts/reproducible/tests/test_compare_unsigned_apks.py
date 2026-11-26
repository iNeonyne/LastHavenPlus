import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from compare_unsigned_apks import compare_apks


class UnsignedApkComparisonTests(unittest.TestCase):
    def make_apk(self, path: Path, files: dict[str, bytes]) -> None:
        with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            for name, content in files.items():
                archive.writestr(name, content)

    def test_identical_archives_are_byte_identical(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            first, second = root / "a.apk", root / "b.apk"
            files = {"AndroidManifest.xml": b"manifest", "classes.dex": b"dex"}
            self.make_apk(first, files)
            self.make_apk(second, files)
            result = compare_apks(first, second)
            self.assertTrue(result["byte_identical"])
            self.assertTrue(result["entry_sets_and_contents_match"])

    def test_reports_entry_content_difference(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            first, second = root / "a.apk", root / "b.apk"
            self.make_apk(first, {"classes.dex": b"one"})
            self.make_apk(second, {"classes.dex": b"two"})
            result = compare_apks(first, second)
            self.assertFalse(result["byte_identical"])
            self.assertEqual(
                [item["name"] for item in result["entry_metadata_or_content_mismatches"]],
                ["classes.dex"],
            )

    def test_reports_missing_entries(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            first, second = root / "a.apk", root / "b.apk"
            self.make_apk(first, {"one": b"1", "two": b"2"})
            self.make_apk(second, {"one": b"1"})
            result = compare_apks(first, second)
            self.assertEqual(result["missing_from_b"], ["two"])
            self.assertFalse(result["byte_identical"])

    def test_rejects_empty_or_missing_apk(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            first, second = root / "a.apk", root / "b.apk"
            first.write_bytes(b"")
            second.write_bytes(b"not a zip")
            with self.assertRaises(ValueError):
                compare_apks(first, second)

    def test_rejects_corrupt_archive(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            first, second = root / "a.apk", root / "b.apk"
            first.write_bytes(b"not a valid APK archive")
            self.make_apk(second, {"classes.dex": b"dex"})
            with self.assertRaises(zipfile.BadZipFile):
                compare_apks(first, second)


if __name__ == "__main__":
    unittest.main()

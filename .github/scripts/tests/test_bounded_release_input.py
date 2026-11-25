import io
import os
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from bounded_release_input import copy_bounded, validate_https_url


class BoundedCopyTests(unittest.TestCase):
    def test_accepts_input_at_exact_limit(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "input.bin")
            size = copy_bounded(io.BytesIO(b"12345678"), destination, 8)
            self.assertEqual(size, 8)
            with open(destination, "rb") as source:
                self.assertEqual(source.read(), b"12345678")

    def test_rejects_oversized_input_and_removes_partial_file(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "input.bin")
            with self.assertRaisesRegex(ValueError, "exceeds"):
                copy_bounded(io.BytesIO(b"123456789"), destination, 8)
            self.assertFalse(os.path.exists(destination))
            self.assertFalse(os.path.exists(destination + ".part"))

    def test_rejects_empty_input(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "input.bin")
            with self.assertRaisesRegex(ValueError, "empty"):
                copy_bounded(io.BytesIO(b""), destination, 8)
            self.assertFalse(os.path.exists(destination))

    def test_rejects_non_positive_limit(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "input.bin")
            with self.assertRaisesRegex(ValueError, "positive"):
                copy_bounded(io.BytesIO(b"x"), destination, 0)

    def test_accepts_only_https_github_asset_hosts(self):
        validate_https_url("https://github.com/owner/repo/releases/download/v1/file.apk")
        validate_https_url("https://release-assets.githubusercontent.com/file")

    def test_rejects_untrusted_or_unsafe_urls(self):
        invalid = (
            "http://github.com/file",
            "https://example.com/file",
            "https://user:pass@github.com/file",
            "https://github.com:444/file",
            "https://github.com/file#fragment",
        )
        for url in invalid:
            with self.subTest(url=url):
                with self.assertRaises(ValueError):
                    validate_https_url(url)


if __name__ == "__main__":
    unittest.main()

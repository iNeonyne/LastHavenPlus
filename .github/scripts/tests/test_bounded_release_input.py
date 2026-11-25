import io
import os
import sys
import tempfile
import unittest
from unittest.mock import patch
from urllib.request import Request

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
import bounded_release_input as bounded
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



class FakeResponse(io.BytesIO):
    def __init__(self, body, headers):
        super().__init__(body)
        self.headers = headers
        self.bytes_read = 0

    def read(self, size=-1):
        chunk = super().read(size)
        self.bytes_read += len(chunk)
        return chunk

    def getcode(self):
        return 200


class FakeOpener:
    def __init__(self, response):
        self.response = response

    def open(self, request, timeout):
        self.request = request
        self.timeout = timeout
        return self.response


class BoundedDownloadTests(unittest.TestCase):
    def test_download_accepts_unknown_length_at_exact_limit(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "asset.bin")
            response = FakeResponse(b"12345678", {})
            with patch.object(bounded, "build_opener", return_value=FakeOpener(response)):
                size = bounded.download_bounded(
                    "https://github.com/owner/repo/releases/download/v1/asset.bin",
                    destination,
                    8,
                )
            self.assertEqual(size, 8)
            self.assertEqual(os.path.getsize(destination), 8)

    def test_download_rejects_unknown_length_over_limit(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "asset.bin")
            response = FakeResponse(b"123456789", {})
            with patch.object(bounded, "build_opener", return_value=FakeOpener(response)):
                with self.assertRaisesRegex(ValueError, "exceeds"):
                    bounded.download_bounded(
                        "https://github.com/owner/repo/releases/download/v1/asset.bin",
                        destination,
                        8,
                    )
            self.assertFalse(os.path.exists(destination))
            self.assertFalse(os.path.exists(destination + ".part"))

    def test_download_rejects_oversized_content_length_before_reading(self):
        with tempfile.TemporaryDirectory() as directory:
            destination = os.path.join(directory, "asset.bin")
            response = FakeResponse(b"123456789", {"Content-Length": "9"})
            with patch.object(bounded, "build_opener", return_value=FakeOpener(response)):
                with self.assertRaisesRegex(ValueError, "declared response size"):
                    bounded.download_bounded(
                        "https://github.com/owner/repo/releases/download/v1/asset.bin",
                        destination,
                        8,
                    )
            self.assertEqual(response.bytes_read, 0)
            self.assertFalse(os.path.exists(destination))

    def test_redirect_rejects_unapproved_host(self):
        handler = bounded.SafeRedirectHandler()
        with self.assertRaises(ValueError):
            handler.redirect_request(
                Request("https://github.com/owner/repo/file"),
                io.BytesIO(),
                302,
                "Found",
                {},
                "https://example.com/file",
            )


if __name__ == "__main__":
    unittest.main()

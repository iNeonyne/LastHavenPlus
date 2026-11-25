#!/usr/bin/env python3
"""Download trusted GitHub release inputs with a strict streamed byte limit."""

from __future__ import annotations

import argparse
import os
import sys
from urllib.parse import urlsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener

ALLOWED_HOSTS = {
    "github.com",
    "release-assets.githubusercontent.com",
    "objects.githubusercontent.com",
    "github-releases.githubusercontent.com",
}
CHUNK_SIZE = 64 * 1024


def validate_https_url(url: str) -> None:
    parsed = urlsplit(url)
    host = (parsed.hostname or "").lower()
    if (
        parsed.scheme != "https"
        or host not in ALLOWED_HOSTS
        or parsed.username is not None
        or parsed.password is not None
        or parsed.port not in (None, 443)
        or parsed.fragment
    ):
        raise ValueError("URL is not an approved HTTPS GitHub release asset URL")


class SafeRedirectHandler(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        validate_https_url(newurl)
        return super().redirect_request(req, fp, code, msg, headers, newurl)


def copy_bounded(source, destination: str, max_bytes: int) -> int:
    if max_bytes <= 0:
        raise ValueError("maximum byte count must be positive")

    temporary = destination + ".part"
    total = 0
    try:
        with open(temporary, "wb") as output:
            while True:
                chunk = source.read(min(CHUNK_SIZE, max_bytes - total + 1))
                if not chunk:
                    break
                total += len(chunk)
                if total > max_bytes:
                    raise ValueError(f"input exceeds {max_bytes} byte limit")
                output.write(chunk)

        if total == 0:
            raise ValueError("input is empty")
        os.replace(temporary, destination)
        return total
    except Exception:
        try:
            os.remove(temporary)
        except FileNotFoundError:
            pass
        raise


def download_bounded(url: str, destination: str, max_bytes: int) -> int:
    validate_https_url(url)
    request = Request(url, headers={"User-Agent": "LastHavenPlus-release-workflow"})
    opener = build_opener(SafeRedirectHandler())
    with opener.open(request, timeout=15) as response:
        if response.getcode() != 200:
            raise ValueError(f"unexpected HTTP status {response.getcode()}")
        content_length = response.headers.get("Content-Length")
        if content_length is not None:
            try:
                declared_size = int(content_length)
            except ValueError as error:
                raise ValueError("invalid Content-Length") from error
            if declared_size < 0 or declared_size > max_bytes:
                raise ValueError("declared response size exceeds byte limit")
        return copy_bounded(response, destination, max_bytes)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--stdin", action="store_true", help="read input from stdin")
    parser.add_argument("source", help="URL, or '-' when reading stdin")
    parser.add_argument("destination")
    parser.add_argument("max_bytes", type=int)
    args = parser.parse_args()

    try:
        if args.stdin:
            if args.source != "-":
                raise ValueError("stdin mode requires source '-'")
            size = copy_bounded(sys.stdin.buffer, args.destination, args.max_bytes)
        else:
            size = download_bounded(args.source, args.destination, args.max_bytes)
        print(f"Accepted {size} bytes (limit {args.max_bytes})")
        return 0
    except Exception as error:
        print(f"bounded release input rejected: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

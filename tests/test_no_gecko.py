#!/usr/bin/env python3
"""Fail CI if the Android app source or package configuration reintroduces GeckoView."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
FORBIDDEN = (
    "org.mozilla.geckoview",
    "GeckoRuntime",
    "GeckoSession",
    "GeckoView",
    "geckoViewVersion",
    "maven.mozilla.org",
)
SCAN_ROOTS = (
    ROOT / "app/src/main",
    ROOT / "app/build.gradle.kts",
    ROOT / "settings.gradle.kts",
    ROOT / "gradle.properties",
)
failures = []
for item in SCAN_ROOTS:
    files = [item] if item.is_file() else [p for p in item.rglob("*") if p.is_file()]
    for path in files:
        if path.suffix not in {".kt", ".java", ".xml", ".kts", ".properties"}:
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for token in FORBIDDEN:
            if token.lower() in text.lower():
                failures.append(f"{path.relative_to(ROOT)}: forbidden Gecko reference {token}")

# Only the two portable loader scripts and the native bridge belong in the APK.
assets = ROOT / "app/src/main/assets"
expected = {
    "webview-game-bridge.js",
    "extensions/lh-addon-manager/overlay.js",
    "extensions/lh-addon-manager/inject.js",
}
actual = {p.relative_to(assets).as_posix() for p in assets.rglob("*") if p.is_file()}
unexpected_extension_assets = sorted(
    path for path in actual
    if path.startswith("extensions/lh-addon-manager/") and path not in expected
)
if unexpected_extension_assets:
    failures.append("obsolete extension assets remain: " + ", ".join(unexpected_extension_assets))
missing = sorted(expected - actual)
if missing:
    failures.append("required WebView loader assets are missing: " + ", ".join(missing))

if failures:
    print("\n".join(failures), file=sys.stderr)
    raise SystemExit(1)
print("Gecko-free source and native WebView asset checks passed.")

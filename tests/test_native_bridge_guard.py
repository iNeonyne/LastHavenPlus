#!/usr/bin/env python3
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from check_native_bridge_exposure import find_unsafe_bridges


class NativeBridgeGuardTest(unittest.TestCase):
    def test_accepts_source_without_direct_javascript_bridges(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "Safe.kt").write_text("class Safe { fun openPicker() {} }", encoding="utf-8")
            self.assertEqual([], find_unsafe_bridges(root))

    def test_rejects_add_javascript_interface_registration(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "Unsafe.kt").write_text('view.addJavascriptInterface(bridge, "native")', encoding="utf-8")
            findings = find_unsafe_bridges(root)
            self.assertEqual(1, len(findings))
            self.assertIn("addJavascriptInterface", findings[0])

    def test_rejects_javascript_interface_annotation(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "Unsafe.java").write_text("@JavascriptInterface public void reset() {}", encoding="utf-8")
            findings = find_unsafe_bridges(root)
            self.assertEqual(1, len(findings))
            self.assertIn("@JavascriptInterface", findings[0])


if __name__ == "__main__":
    unittest.main()

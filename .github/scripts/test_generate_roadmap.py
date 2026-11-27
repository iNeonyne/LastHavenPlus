#!/usr/bin/env python3
"""Unit tests for safe, bounded updates to the generated roadmap section."""
import unittest

from generate_roadmap import build_live_section, replace_live_section


class RoadmapGeneratorTests(unittest.TestCase):
    def test_preserves_strategic_content_outside_live_markers(self):
        existing = (
            "# Roadmap\n\nStrategic priorities stay here.\n\n"
            "<!-- ROADMAP-LIVE-START -->\nold generated content\n"
            "<!-- ROADMAP-LIVE-END -->\n\nFinal policy stays here.\n"
        )
        generated = (
            "<!-- ROADMAP-LIVE-START -->\n## Live issue and PR inventory\n"
            "new generated content\n<!-- ROADMAP-LIVE-END -->\n"
        )
        updated = replace_live_section(existing, generated)
        self.assertIn("Strategic priorities stay here.", updated)
        self.assertIn("Final policy stays here.", updated)
        self.assertIn("new generated content", updated)
        self.assertNotIn("old generated content", updated)

    def test_appends_live_section_when_markers_are_missing(self):
        existing = "# Roadmap\n\nCurated plan.\n"
        generated = "<!-- ROADMAP-LIVE-START -->\n## Live issue and PR inventory\n<!-- ROADMAP-LIVE-END -->\n"
        updated = replace_live_section(existing, generated)
        self.assertTrue(updated.startswith(existing.rstrip()))
        self.assertIn("ROADMAP-LIVE-START", updated)

    def test_rejects_incomplete_or_reversed_markers(self):
        with self.assertRaises(ValueError):
            replace_live_section("# Roadmap\n<!-- ROADMAP-LIVE-START -->", "generated")
        reversed_markers = "<!-- ROADMAP-LIVE-END -->\n<!-- ROADMAP-LIVE-START -->"
        with self.assertRaises(ValueError):
            replace_live_section(reversed_markers, "generated")

    def test_renders_prioritized_issues_prs_and_recent_completions(self):
        issues = [
            {
                "number": 7,
                "title": "Priority work",
                "html_url": "https://example.test/issues/7",
                "labels": [{"name": "P1"}, {"name": "agent:chaty"}],
            },
            {
                "number": 8,
                "title": "Unprioritized work",
                "html_url": "https://example.test/issues/8",
                "labels": [],
            },
        ]
        prs = [
            {
                "number": 9,
                "title": "Implementation",
                "html_url": "https://example.test/pull/9",
                "base": {"ref": "beta"},
                "head": {"ref": "agent/work"},
            }
        ]
        closed = [
            {
                "number": 6,
                "title": "Completed",
                "html_url": "https://example.test/issues/6",
            }
        ]
        section = build_live_section(issues, prs, closed, "2026-11-28T00:00:00+00:00")
        self.assertIn("#### P1", section)
        self.assertIn("Priority work", section)
        self.assertIn("Unprioritized work", section)
        self.assertNotIn("agent:chaty", section)
        self.assertIn("#9 — Implementation", section)
        self.assertIn("#6 — Completed", section)


if __name__ == "__main__":
    unittest.main()

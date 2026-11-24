# Agent changelog — LastHavenLauncher

Append-only. Newest entries at the top.  
Every agent **must** add an entry when they change code, Gradle, docs that affect work, or complete research (including failures).

Format:

```markdown
## YYYY-MM-DD — short title
- **Role:** …
- **Files:** …
- **Change:** …
- **Why:** …
- **Verified:** …
- **Do not redo:** …
- **Follow-ups:** …
```

---

## 2026-11-24 — Publish greenfield bootstrap documentation

- **Role:** Architecture / docs
- **Files:** `README.md`, `AGENTS.md`, `STATUS.md`, `CHANGELOG-AGENTS.md`
- **Change:** Added the project mission, Cefrium-only target stack, P0–P3 priorities, storage safety rules, parallel work roles, shared TODO, and mandatory documentation protocol.
- **Why:** Establish one source of truth before implementation and prevent overlapping agent work.
- **Verified:** All four files committed to the `main` branch; repository contents checked after upload. No app build exists yet.
- **Do not redo:** Do not re-research the high-level stack choices already documented unless current official Cefrium documentation contradicts them; record any contradiction in STATUS first.
- **Follow-ups:** Begin P0 scaffold only after claiming ownership in STATUS. Research and pin the exact current Cefrium plugin/SDK versions during integration.

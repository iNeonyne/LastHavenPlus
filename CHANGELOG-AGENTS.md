# Agent changelog — LastHavenLauncher

Append-only. Newest entries at the top. Every meaningful code, Gradle, build, research or documentation change must be recorded.

Entry format:

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

## 2026-11-24 — Create greenfield Android app and Cefrium build

- **Role:** Architecture / Android / Cefrium integration
- **Files:** Gradle settings and build files, app manifest, MainActivity, app resources, GitHub Actions workflow, README.md, AGENTS.md, STATUS.md
- **Change:** Added the Kotlin + Compose Android scaffold, fixed application ID, Cefrium 0.21.0 SDK/Compose/plugin dependencies, Codeberg Maven registry, arm64-only ABI filter, immersive gameplay host using `CefriumView`, minimal Back/exit handling and a CI debug-build workflow.
- **Why:** Establish the smallest app that can load the existing game before optional features.
- **Verified:** Official Cefrium 0.21.0 docs confirm the current release, Chromium 156.0.8078.96, JDK 25 / Gradle 9.7.1 / AGP 9.4 / compileSdk 37 requirements and documented `CefriumView` usage. GitHub Actions and physical-device results are pending.
- **Do not redo:** Do not re-research the documented version/toolchain unless the CI resolver or build contradicts the official release page. `CefriumView` is the Compose API; do not replace it with WebView or another engine.
- **Follow-ups:** Check CI, install on physical arm64 device, validate LastHaven and then run web capability checks. Release signing key still needs to be generated and backed up before distributing a release build.

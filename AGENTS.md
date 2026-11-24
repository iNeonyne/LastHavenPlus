# LastHavenPlus

Instructions for every human or AI agent working in this repository.

## Mission

Ship the simplest reliable Android app that opens the existing LastHaven WotLK private server in Cefrium and lets the user play.

Default URL: `https://alt.lasthavens.com/play`

Do not recreate, modify or develop the game. Do not add System WebView, GeckoView or any fallback engine. Do not add general-purpose browser features.

## Hard rules

1. **Read STATUS and the latest changelog entries before editing.**
2. **Work from a GitHub Issue.** Every planned task must have an issue with scope and acceptance criteria. Check existing open issues before creating another; do not recreate tasks already tracked.
3. **Claim ownership.** Before editing, claim the issue and add your role and file/work area under STATUS → Active work. One owner per file or Gradle block at a time.
4. **Document every meaningful change.** Update STATUS and append to CHANGELOG-AGENTS.md, including research failures and unverified assumptions.
5. **Research Cefrium APIs first.** Never guess engine API names. Use official docs and samples. Record uncertain APIs as blockers.
6. **Game first.** Get LastHaven loading and verify the core game flow before optional settings, theming, addon management or performance work.
7. **arm64-v8a only.** Do not package x86, x86_64 or armeabi-v7a native libraries.
8. **Stable app identity.** Do not change `applicationId` (`com.neonyne.lasthavenlauncher`) or the established release signing key.
9. **Storage safety.** Never clear cookies, IndexedDB, LocalStorage or Cache Storage except through a confirmed “Clear game data” action.
10. **No speculative optimization.** Every performance change needs before/after measurements in STATUS.
11. **Lean coordination.** Parallelize independent tasks only; agents must own disjoint files/surfaces. The main orchestrator alone integrates changes and pushes commits directly to `main`. Do not use pull requests.

## Current engine decision

- Cefrium SDK: `com.cefrium:cefrium-sdk:0.21.0`
- Cefrium Compose wrapper: `com.cefrium:cefrium-compose:0.21.0`
- Gradle plugin: `com.cefrium` version `0.21.0`
- Chromium: `156.0.8078.96` (CEF branch 8078)
- Maven registry: `https://codeberg.org/api/packages/cefrium/maven`
- UI API: `com.cefrium.compose.CefriumView`, `rememberCefriumViewState`, `rememberCefriumNavigator`

**Terminology:** Official Cefrium docs describe `CefriumView` as the Compose API. The classic Android View integration is `CefriumBrowser.createWithSurface(activity)`. Use the Compose `CefriumView` requested by this project unless an API-level blocker is found; do not silently switch rendering APIs.

Official build requirements:
- JDK 25, Gradle 9.7.1, AGP 9.4.0
- compileSdk 37, minSdk 29
- 4 GB Gradle heap
- AGP 9 built-in Kotlin support; do not apply `org.jetbrains.kotlin.android`
- Compose compiler Gradle plugin at a matching stable Kotlin version
- Legacy JNI library packaging

The Codeberg Maven registry mirrors the current and previous releases only. Keep Cefrium pinned; check the official releases page before updating it. Update SDK, Compose wrapper and Gradle plugin together.

References:
- https://cefrium.com/quickstart/
- https://cefrium.com/releases/
- https://cefrium.com/guides/adopting-cefrium/
- https://cefrium.com/docs/

## Parallel agent roles

| Role | Owns | Must not duplicate |
|---|---|---|
| Architecture | App structure, package, boundaries | Cefrium API research |
| Android | Activity, manifest, lifecycle, orientation, permissions | Engine integration internals |
| Cefrium | SDK version, Maven, view host, engine APIs | General app UI |
| QA | Build, arm64 device/emulator, web capability checks | Unmeasured performance changes |
| Performance | Profiling and measured improvements after compatibility checks | Speculative flag lists |

Agents may work in parallel only on disjoint files/surfaces. Each agent must identify the issue it is working on and claim its files in STATUS before editing. The main orchestrator reviews and integrates all work directly into `main`.

## Issue workflow

1. Find the existing issue that matches the task. Create a new issue only when no existing issue covers it.
2. Read its goal, checklist, dependencies, constraints and acceptance criteria.
3. Claim the issue and file/work area in STATUS before making changes.
4. Keep the issue checklist accurate as work is completed. Link related issues instead of copying their task lists.
5. Run the relevant build/tests and record what was actually verified. Do not mark device-only behavior as passed based on CI.
6. Update STATUS and append a changelog entry.
7. The main orchestrator integrates and pushes directly to `main`; no pull request workflow.
8. Close an issue only when its acceptance criteria are met. If blocked, leave it open and document the blocker in STATUS and the issue.

**Tracking links**
- [All issues](https://github.com/iNeonyne/Lasthavens-Android/issues)
- [P0.1 — Build reliability and updater tests](https://github.com/iNeonyne/Lasthavens-Android/issues/4)
- [P0.2 — Physical-device gameplay and update verification](https://github.com/iNeonyne/Lasthavens-Android/issues/5)
- [P2 — Settings and safe storage controls](https://github.com/iNeonyne/Lasthavens-Android/issues/6)
- [P3 — Performance and APK-size measurement](https://github.com/iNeonyne/Lasthavens-Android/issues/7)
- [Deferred — Addon manager](https://github.com/iNeonyne/Lasthavens-Android/issues/8)

Do not maintain a second prioritized TODO checklist in this file. GitHub Issues are the canonical backlog.

## Documentation protocol

When starting work:
1. Read this file, STATUS and the last 5–10 changelog entries.
2. Find and claim the corresponding GitHub Issue and add your ownership under STATUS → Active work.
3. Implement the smallest useful change.
4. Build/test where possible.
5. Update STATUS, append CHANGELOG-AGENTS.md, update the issue checklist, and unclaim when done.

Append changelog entries with date, role, files, change, reason, verification, do-not-redo notes and follow-ups. A failed experiment is still worth recording.

## Storage and privacy

- Use the Cefrium default persistent profile unless a documented requirement proves otherwise.
- Do not place browser data in cache directories.
- Never clear user data during startup, update or recovery.
- No analytics SDKs, tracking or background services.
- Do not log game cookies, tokens, local storage values or other session secrets.

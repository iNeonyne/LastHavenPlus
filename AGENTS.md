# LastHavenPlus

Instructions for every human or AI agent working in this repository.

## Mission

Ship the simplest reliable Android app that opens the existing LastHaven WotLK private server in Cefrium and lets the user play.

Default URL: `https://alt.lasthavens.com/play`

Do not recreate, modify or develop the game. Do not add System WebView, GeckoView or any fallback engine. Do not add general-purpose browser features.

## Hard rules

1. **Read STATUS and the latest changelog entries before editing.**
2. **Work from a GitHub Issue.** Every planned task must have an issue with scope and acceptance criteria. Check existing open issues before creating another; do not recreate tasks already tracked.
3. **Claim exclusive ownership.** Before editing, claim the issue, exact subtask and files under STATUS → Active work. Only one agent may own an issue/task at a time. The other agent must not implement, edit or independently investigate that same task until explicit handoff and tracker update. Separate subtasks on one broad issue require documented non-overlapping scope and files.
4. **Document every meaningful change.** Update STATUS and append to CHANGELOG-AGENTS.md, including research failures and unverified assumptions.
5. **Research Cefrium APIs first.** Never guess engine API names. Use official docs and samples. Record uncertain APIs as blockers.
6. **Game first.** Get LastHaven loading and verify the core game flow before optional settings, theming, addon management or performance work.
7. **arm64-v8a only.** Do not package x86, x86_64 or armeabi-v7a native libraries.
8. **Stable app identity.** Do not change `applicationId` (`com.neonyne.lasthavenlauncher`) or the established release signing key.
9. **Storage safety.** Never clear cookies, IndexedDB, LocalStorage or Cache Storage except through a confirmed “Clear game data” action.
10. **No speculative optimization.** Every performance change needs before/after measurements in STATUS.
11. **Beta-first coordination.** `main` is the stable release branch; `beta` is the integration and testing branch. Chaty works directly on `beta`. Ploy creates a dedicated task branch and opens a PR targeting `beta`; she must never push directly to `beta` or `main`. Chaty reviews the complete diff and CI, requests fixes when needed, and may merge an accepted Ploy PR into `beta`. Do not merge `beta` into `main` until the user explicitly approves the promotion after beta verification. No automatic merges or releases.

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

## Named agent assignments

Read [AGENT_WORK.md](AGENT_WORK.md) before starting delegated work. It defines Ploy (repository-only coding agent) and Chaty (main orchestrator), task order, file ownership, handoff rules and integration boundaries. Ploy creates a dedicated task branch and PR targeting `beta`; she must not push directly to `beta` or `main`. Chaty reviews the actual PR diff, verifies it and updates `ISSUES.md`, `STATUS.md` and `CHANGELOG-AGENTS.md`, then may merge accepted work into `beta`. `main` remains stable; promote `beta` into `main` only after beta verification and the user's explicit approval.

## Parallel agent roles

| Role | Owns | Must not duplicate |
|---|---|---|
| Architecture | App structure, package, boundaries | Cefrium API research |
| Android | Activity, manifest, lifecycle, orientation, permissions | Engine integration internals |
| Cefrium | SDK version, Maven, view host, engine APIs | General app UI |
| QA | Build, arm64 device/emulator, web capability checks | Unmeasured performance changes |
| Performance | Profiling and measured improvements after compatibility checks | Speculative flag lists |

Agents may work in parallel only on different issues with disjoint files/surfaces. Never assign the same issue/task to both agents concurrently. Each agent must claim its issue and exact subtask in STATUS before editing. Handoff requires the previous owner to stop and STATUS/AGENT_WORK to be updated. Chaty reviews Ploy's PRs and integrates accepted work into `beta`; `main` is updated only through an approved beta promotion.

## Issue workflow

1. Find the existing issue that matches the task. Create a new issue only when no existing issue covers it.
2. Read its goal, checklist, dependencies, constraints and acceptance criteria.
3. Claim the issue and file/work area in STATUS before making changes. For Ploy's delegated tasks, Chaty maintains central status; Ploy must not edit central tracking files. Ploy records the issue number and PR link in her handoff.
4. Keep `ISSUES.md` accurate as work is completed. It is the consolidated repository backlog; update task checklists and status alongside implementation changes. GitHub Issues are linked discussion records.
5. Run the relevant build/tests and record what was actually verified. Do not mark device-only behavior as passed based on CI.
6. Update STATUS and append a changelog entry.
7. Ploy submits work through a pull request from a dedicated task branch targeting `beta`. Chaty reviews and verifies it, then may merge accepted work into `beta`. Chaty makes orchestrator commits directly to `beta`; `main` changes only through an explicitly approved beta promotion.
8. Close an issue only when its acceptance criteria are met. If blocked, leave it open and document the blocker in STATUS and the issue.

**Tracking links**
- [Repository task tracker — ISSUES.md](ISSUES.md)
- [All GitHub issues](https://github.com/iNeonyne/LastHavenPlus/issues)

`ISSUES.md` is the consolidated, version-controlled backlog. Keep it current as work progresses. GitHub Issues remain linked discussion/history records. Do not create or update TickTick tasks for this repository.

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

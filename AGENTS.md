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
11. **Beta-first coordination.** `main` is the stable release branch; `beta` is the active development, integration and testing branch. Contributors work on separate task branches when parallel work is needed. They hand Chaty the completed branch/commit and verification report; they never push to `beta` or `main`. **Chaty alone reviews, integrates and pushes commits directly to `beta`. No pull requests are used.** Promote `beta` to `main` only after all release gates pass and the user explicitly approves. Never create a release or tag without the applicable approval.

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

Read [AGENT_WORK.md](AGENT_WORK.md) before starting delegated work. It defines task order, exclusive file ownership and handoff rules. Contributors may push only to their task branches and must provide Chaty the exact commit SHA and verification report. Chaty alone reviews and integrates accepted commits directly into `beta`. No pull requests. `main` remains stable; promote `beta` into `main` only after all release gates pass and the user explicitly approves.

## Parallel agent roles

| Role | Owns | Must not duplicate |
|---|---|---|
| Architecture | App structure, package, boundaries | Cefrium API research |
| Android | Activity, manifest, lifecycle, orientation, permissions | Engine integration internals |
| Cefrium | SDK version, Maven, view host, engine APIs | General app UI |
| QA | Build, arm64 device/emulator, web capability checks | Unmeasured performance changes |
| Performance | Profiling and measured improvements after compatibility checks | Speculative flag lists |

Agents may work in parallel only on different issues with disjoint files/surfaces. Never assign the same issue/task to both agents concurrently. Each agent must claim its issue and exact subtask in STATUS before editing. Handoff requires the previous owner to stop and STATUS/AGENT_WORK to be updated. Chaty alone integrates accepted task commits directly into `beta`; `main` is updated only through an explicitly approved beta promotion.

## Issue lifecycle and intake

GitHub Issues are the canonical records for individual work: discussion, ownership, dependencies, decisions and open/closed state. `ISSUES.md` is the human-maintained project index and execution checklist; it must link to the matching GitHub issue and must not invent a second task ID or conflicting state. Keep the detailed acceptance checklist in the issue and mirror only a concise summary/progress line in `ISSUES.md` where practical. If the two disagree, verify the GitHub issue and reconcile the index before work continues.

### Creating future issues

1. Search **open and closed** GitHub issues, the `ISSUES.md` index and recent PRs before creating anything. Search by user outcome and likely synonyms, not just exact title.
2. Reuse an existing issue when the request is already covered. Add missing acceptance criteria or a separately scoped subtask rather than creating a duplicate.
3. Create one issue per independently deliverable outcome. Split work only when deliverables, acceptance criteria and file ownership can be separated without overlapping agents.
4. Use the Bug report, Feature request or Engineering task issue form. Do not use a vague title such as "improvements" or bundle unrelated requests.
5. Write a concise title describing the outcome. Include context, exact scope, non-goals, measurable acceptance criteria, verification plan, dependencies and known risks.
6. Assign priority only after triage: **P0** release/core blocker or data/security risk; **P1** important reliability or planned release capability; **P2** normal improvement; **P3** optional/low urgency. Priority is not a release promise.
7. Set a milestone/target release only after the scope is accepted. Leave it unset when uncertain. Record dependencies by issue number; never silently pull blocked work into implementation.
8. Add the new issue to `ISSUES.md` and update the roadmap only after the GitHub issue exists. The GitHub issue number is the sole identifier.

### Lifecycle

Use these working states consistently in issue labels/comments and `STATUS.md`:
- **Needs triage:** request received; duplicate, value, scope and constraints not yet assessed.
- **Ready:** accepted, sufficiently specified, dependencies known and acceptance criteria testable.
- **Active:** one named owner has claimed the exact issue/subtask and file scope in `STATUS.md`.
- **Blocked:** work cannot proceed; record the specific blocker, who/what can unblock it and next action.
- **Review:** implementation is submitted; review and required CI/evidence are pending.
- **Done:** every acceptance criterion is met and evidence is recorded; close the GitHub issue with the appropriate completion reason.
- **Deferred / not planned:** explicitly record why and whether it may be reconsidered. Do not leave silently stale work looking active.
- **Duplicate:** link to the canonical issue and close as duplicate.

An open issue is not automatically approved, ready, assigned or scheduled. Closing an issue requires evidence against its acceptance criteria. If only part is complete, keep it open and split a follow-up only when it is independently useful.

### Ownership and agent safety

- One issue has one active owner. Record owner, exact subtask, allowed files, branch and start condition in `STATUS.md` before edits.
- Chaty owns intake, duplicate checks, GitHub issue creation/updates, priority, labels, roadmap, central trackers, assignment and review.
- Ploy works only on the issue explicitly assigned in `AGENT_WORK.md`; she cannot see GitHub issue metadata and must not create issues, change labels, assign work or edit central trackers.
- No agent may investigate, code, test or edit the same issue concurrently with the other agent, even in different branches. A handoff is valid only after the previous owner stops and the trackers are updated.
- Contributors use a task branch from latest `beta` when parallel isolation is needed, then hand the commit SHA and test evidence to Chaty. No pull requests. Chaty alone integrates into `beta`. No routine development on `main`.

### Changes, splitting and closure

- If scope changes materially, update the GitHub issue and `ISSUES.md` before expanding implementation.
- If a request contains independent outcomes, create linked child issues with explicit dependency/order and non-overlapping file ownership. Keep one parent/umbrella issue only when coordination itself has value; do not create empty hierarchy.
- Record decisions and blockers in the GitHub issue, not only in chat.
- Every task-branch handoff must identify its GitHub issue and exact commit SHA. Chaty records the integration commit and closes an issue only when all acceptance criteria are satisfied. Never close a parent issue when only one subtask is done.
- Before closing: verify acceptance criteria, relevant tests/builds, docs, known limitations and physical-device requirements. Record actual evidence; CI does not prove device-only behavior.
- Reopen an issue if a regression invalidates its acceptance criteria. Create a new issue for a genuinely new follow-up, linking the original.

## Documentation protocol

When starting work:
1. Read this file, STATUS and the last 5–10 changelog entries.
2. Search open and closed issues for duplicates; confirm the request is accepted and sufficiently specified.
3. Confirm exclusive ownership and record owner, subtask, files, branch and dependencies under STATUS → Active work.
4. Implement only the approved scope.
5. Build/test and record exact results.
6. Update the GitHub issue and ISSUES.md index, STATUS, and CHANGELOG-AGENTS.md; unclaim only after review/acceptance and handoff.

Append changelog entries with date, role, files, change, reason, verification, do-not-redo notes and follow-ups. A failed experiment is still worth recording.

## Storage and privacy

- Use the Cefrium default persistent profile unless a documented requirement proves otherwise.
- Do not place browser data in cache directories.
- Never clear user data during startup, update or recovery.
- No analytics SDKs, tracking or background services.
- Do not log game cookies, tokens, local storage values or other session secrets.

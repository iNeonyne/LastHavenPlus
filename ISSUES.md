# Project backlog — LastHavenPlus

## 2026-11-27 — Addon Manager Phase B execution checkpoint

- **P0 — Addon Manager Phase B (#23):** Chaty coordinates and integrates; Vibe has exactly one active implementation assignment, #168 (XHR resource overlay).
- **Merged bridge foundation:** PR #177 and payload transport PR #180 are integrated into beta. PR #180 merge commit / verified beta HEAD: `892494aa8f0333f907b2e730f6b754ba2dabe2a1`.
- **#167:** remains open, `agent:chaty`; Chaty owns integration follow-up and physical ARM64 runtime verification. Do not infer actual addon initialization from bridge transport.
- **#168:** `agent:vibe`; active assignment. Vibe must complete only the bounded XHR resource overlay and its security/test contract.
- **#169:** `agent:blocked`; waits for #168 review and integration before Worker/`blocks.idx` masking.
- **#170:** `agent:blocked`; waits for runtime tasks and navigation/session prerequisites.
- **Paused:** updater #4/#15 remain queued, not cancelled. Resume only after Chaty explicitly reassigns them following a safe Addon Manager milestone handoff.
- Existing ARM64-only, security, signing, device-verification and owner release-approval gates remain unchanged.

**Source of truth:** GitHub Issues and this version-controlled backlog. Keep issue scope, status and acceptance criteria aligned. Do not close an issue because documentation changed.

## Current state — 2026-11-27

- **Latest stable:** v2.3.0; no new release is authorized.
- **Development:** beta HEAD `892494aa8f0333f907b2e730f6b754ba2dabe2a1`.
- **Integrated:** PR #177 native-messaging foundation and PR #180 bounded ZIP 0.5.1 payload transport. Exact-head Android PR validation #45938326655 and unsigned reproducibility #45938326638 passed; no post-merge CI result is claimed.
- **Addon Manager:** Phase A/planner and bridge transport code are integrated. Production session wiring, XHR (#168), Worker/index masking (#169), same-session navigation (#170), and in-game addon activation remain incomplete.
- **Device/release:** no physical ARM64 runtime test, release, tag, publication or beta-to-main promotion is claimed or authorized.

## Current ownership

- **Chaty active:** #23 coordination, #167 bridge follow-up/device verification, PR review and integration.
- **Vibe active:** #168 XHR resource overlay only.
- **Blocked:** #169 waits for #168 integration; #170 waits for runtime/navigation prerequisites.
- **Updater:** #4/#15 remain queued/paused under Chaty.
- **Chaty queued:** #81 dependency-evidence documentation; not started. #20/#31 remain owner/authorization blocked.
- **Owner-controlled:** #67 third-party rights/distribution decision.

## Backlog index

| Issue | Priority | Summary | Current state |
|---|---:|---|---|
| [#4](#issue-4) | P0.1 | Build reproducibility and updater test coverage | agent:queued; paused until Addon Manager P0 reaches a safe handoff |
| [#5](#issue-5) | Feedback | Owner feedback — gameplay and update behavior after release | Closed; historical owner feedback thread |
| [#6](#issue-6) | P2 | Essential launcher settings and safe storage controls | Open; `agent:blocked` pending #28 |
| [#7](#issue-7) | P3 | Runtime performance and APK size | Open; measure before optimizing |
| [#8](#issue-8) | — | Legacy addon manager | Closed — superseded; no planned work |
| [#9](#issue-9) | P2 | Overlay accessibility and navigation | Closed after PR #139 integration; physical-device coverage is not claimed by CI |
| [#10](#issue-10) | P2 | Persistent app-wide network usage totals | Open; app-wide counters are implemented, automated counter tests remain |
| [#11](#issue-11) | P2 | Privacy-conscious log tests | Tests merged by PR #163; remaining runtime-version reporting is deferred (`agent:blocked`) |
| [#12](#issue-12) | P2 | GeckoView load-error details | Open |
| [#13](#issue-13) | — | Accessible names / legacy addon-manager compile work | Closed — consolidated into #9 |
| [#14](#issue-14) | P2 | Connection diagnostics | Open |
| [#15](#issue-15) | P2 | Updater recovery and download progress visibility | agent:queued; paused with #4 until Addon Manager P0 reaches a safe handoff |
| [#16](#issue-16) | P2 | Gameplay orientation and keep-awake settings | Open |
| [#17](#issue-17) | P2 | Settings backup and restore | Open |
| [#18](#issue-18) | Roadmap | v2.4.0–v2.6.0 UI/UX, updater and settings roadmap | Open; owner-requested planning targets only, no release authorization |
| [#19](#issue-19) | P1 | Opt-in beta release channel | Open |
| [#20](#issue-20) | Deferred | GeckoView 160 artifact/dependency audit | `agent:blocked`; PR #122 closed unmerged; resume only after owner reprioritization; unresolved notice/distribution/device evidence remains |
| [#21](#issue-21) | Feedback | Owner feedback — GeckoView runtime/profile behavior after release | Closed; historical owner feedback thread |
| [#22](#issue-22) | Superseded | Home screen and Play/Settings navigation | Closed; replaced by direct-launch/overlay scope #56 |
| [#23](#issue-23) | P0 | Addon Manager ZIP 0.5.1 Phase B | Top project priority; Phase A/planner integrated; Chaty coordinates #167–#170; runtime activation not yet implemented |
| [#28](https://github.com/iNeonyne/LastHavenPlus/issues/28) | P2 | Storage usage accuracy and iterative directory sizing | Open; queued after #10 |
| [#42](https://github.com/iNeonyne/LastHavenPlus/issues/42) | Superseded | v2.1.0 Home-screen UI verification | Closed as superseded by direct-launch redesign #56 |
| [#29](https://github.com/iNeonyne/LastHavenPlus/issues/29) | Done | Reconcile beta documentation and CI evidence | Closed as completed after PR #46 merged to beta |
| [#31](https://github.com/iNeonyne/LastHavenPlus/issues/31) | Deferred | Draft release asset verification and differential fallback | `agent:blocked`; release-tag packaging/runtime verification requires explicit owner authorization |
| [#56](https://github.com/iNeonyne/LastHavenPlus/issues/56) | Done | Direct launch and responsive in-game overlay | PR #83 merged to beta; PR #87 selectively promoted implementation to main; exact beta validation and signed CI passed |
| [#60](https://github.com/iNeonyne/LastHavenPlus/issues/60) | Done | Android lint and Kotlin formatting quality gate | PR #72 merged; exact-head PR and post-merge beta quality/build workflows passed |
| [#84](https://github.com/iNeonyne/LastHavenPlus/issues/84) | Done | Approved recreated LastHavens-inspired launcher icon | PR #85 merged to beta; PR #87 promoted icon to main; beta CI passed |
| [#86](https://github.com/iNeonyne/LastHavenPlus/issues/86) | Deferred | Supporting settings, diagnostics and feature backlog | Deferred coordination index; detailed work is sequenced in #18 and remains dependency-gated |
| [#106](https://github.com/iNeonyne/LastHavenPlus/issues/106) | Done | Gameplay loading, reconnect and load-failure UX | Closed after implementation integration; no open task remains under this issue |
| [#107](https://github.com/iNeonyne/LastHavenPlus/issues/107) | Done | Consistent visual design and interaction polish | Closed after PR #136 integration |
| [#62](https://github.com/iNeonyne/LastHavenPlus/issues/62) | Done | Branch lifecycle audit | Closed after PR #123 merged; no branch deletion performed |
| [#66](https://github.com/iNeonyne/LastHavenPlus/issues/66) | Engineering | Independent unsigned APK reproducibility | Completed; PR #119 merged to beta at `97570ea` |
| [#70](https://github.com/iNeonyne/LastHavenPlus/issues/70) | Done | Contributor/security guidance and Markdown validation | Closed after docs PR #158 and Markdown quality integration |
| [#81](https://github.com/iNeonyne/LastHavenPlus/issues/81) | Dependencies | Compatible AndroidX and compression library refresh | Five approved pins integrated by PR #104; Chaty queued to finish evidence/docs reconciliation only |
| [#111](https://github.com/iNeonyne/LastHavenPlus/issues/111) | Deferred | Updater download progress and retry feedback | `agent:blocked`; v2.5.0 integration hold requires v2.4.0 release and explicit owner authorization |
| [#133](https://github.com/iNeonyne/LastHavenPlus/issues/133) | Done | v2.4.0 adaptive launcher icon refresh | Closed after PR #135 integration; CI does not constitute physical-device appearance testing |
| [#128](https://github.com/iNeonyne/LastHavenPlus/issues/128) | Done | Post-v2.3.0 repository housekeeping | Closed after PR #161 merged; exact-head and post-merge Markdown passed |
| [#129](https://github.com/iNeonyne/LastHavenPlus/issues/129) | Done | Consolidate beta CI quality checks | Closed; PR #132 merged and exact-merge CI passed |

---

## How new work is added

GitHub Issues are the canonical records for each task's discussion, owner, dependencies and lifecycle. This file is the project-wide index and roadmap checklist. The GitHub issue number is the only task ID; do not invent a parallel numbering scheme.

**Chaty handles intake and triage.** For every new request, search open and closed issues and this index for duplicates before creating anything. Reuse or update an existing issue whenever it already covers the requested outcome. New issues must use one of the forms in `.github/ISSUE_TEMPLATE/` and include a clear outcome, bounded scope, non-goals, testable acceptance criteria, verification plan and dependencies. New issues may briefly receive `agent:unassigned` from automation; Chaty must resolve ownership promptly by assigning `agent:chaty`, `agent:vibe`, `agent:queued`, `agent:blocked` or `agent:owner` before implementation can begin. Do not leave actionable issues unassigned after triage. The GitHub workflow that creates/applies ownership labels is `.github/workflows/agent-ownership.yml`; it must be present on the repository default branch before issue-open automation becomes active. Only then add a concise entry here.

Priority guide: P0 = core/release blocker or data/security risk; P1 = important reliability or planned release capability; P2 = normal improvement; P3 = optional/low urgency. Priority does not guarantee scheduling or inclusion in a release. Set a target release/milestone only after triage.

Lifecycle: Needs triage → Ready → Active → Review → Done; Blocked and Deferred are explicit side states. An issue is not Ready just because it is open. `agent:queued` is not an active assignment; only `agent:vibe` plus a Chaty scope comment permits Vibe to start. One active implementation owner per issue. Every open actionable issue must have exactly one `agent:*` label; use `agent:queued` only for ready work waiting its turn; use `agent:blocked` for real dependencies/holds and `agent:owner` for owner decisions. Owner, exact subtask, permitted files, branch and dependencies must be recorded in `STATUS.md` before editing. Vibe only works on the active assignment in `AGENT_WORK.md`; Chaty owns issue creation, triage and central tracking.

Close only when every acceptance criterion is met and evidence is recorded. If an integrated task commit only partially addresses an issue, keep it open and record the remaining work. Record blockers and decisions on the GitHub issue as well as the repository status. Do not represent unreported owner feedback as a test result; owner feedback is optional and is not a release gate.

---

<a id="issue-4"></a>

## Issue 4 — P0.1 — Improve build reproducibility and updater test coverage

**Current audit (2026-11-25):** The Gradle Wrapper is pinned to Gradle 9.7.1 with a distribution SHA-256. Signed CI run [#310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45508671565) passed release build/tests, signer and ARM64-only ABI checks for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`; this does not verify current beta HEAD. PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Exact-head PR run [#329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) passed unsigned release build and unit tests for `3a1396f7364be1e6aa658b3eb67be41549a906fb`; it does not verify signed ARM64 packaging. Keep the issue open for remaining automated updater edge cases; signed CI is verified by run #330.

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/4
**State:** open

## Goal
Harden the existing build and updater without replacing the current update architecture.

## Build reliability
- [x] Add the Gradle Wrapper (wrapper scripts, wrapper JAR, and pinned Gradle 9.7.1 distribution URL) — present on beta.
- [x] Pin the Gradle 9.7.1 distribution SHA-256 in wrapper properties; wrapper validation passed in CI run #247.
- [x] Commit wrapper JAR and executable `gradlew`; CI wrapper validation passed in run #247.
- [x] Update GitHub Actions to invoke `./gradlew`; run #247 reached the Gradle build through the wrapper.
- [ ] Keep the existing JDK, AGP, SDK, GeckoView, signing key, application ID, and arm64-only configuration unless a verified compatibility issue requires a change.
- [ ] Verify current beta debug and signed release builds in CI. Run #232 failed Kotlin compilation before APK/signing/ABI verification; historical run #85 is not current-source evidence.

**Beta audit update (2026-11-25):** Run #310 verified `assembleRelease`, `testDebugUnitTest`, the established signing certificate and ARM64-only packaging for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`; it does not verify current beta HEAD. Chaty's PR #26 adds full-APK fallback after delta failure and updater policy tests. PR run #329 passed the unsigned release build and unit tests for the final PR head. Keep #4 open until post-merge signed CI and remaining automated updater tests are verified.

## Updater and differential-patch tests

**Chaty progress (2026-11-25):** PR #26 adds unit tests for delta-to-full fallback policy, numeric version/versionCode handling, SHA-256 format validation and trusted GitHub asset URLs. It also changes beta demo artifact versioning to match the latest stable release identity while keeping release publication disabled. Run #317 passed the full PR build/unit-test task for code SHA `283d649cd31ae009889d76affb25f655e28d3949`. The existing BSDIFF tests cover successful reconstruction, truncated headers, unsupported magic and output-size limits. Manifest parsing, actual file digest mismatch, base APK mismatch and interrupted network-download tests remain open.
- [ ] Add tests for malformed or unsupported update manifests.
- [x] Test invalid release version tags and version-code handling (PR #26; run #317 passed).
- [ ] Test incorrect APK and patch SHA-256 values.
- [ ] Test mismatched base APK version, version code, and base digest.
- [ ] Test corrupted, truncated, and incompatible BSDIFF patches.
- [ ] Test interrupted/failed downloads and ensure partial files cannot be installed.
- [x] Preserve the existing manual, user-confirmed download/install flow; no automatic download or installation was added.
- [x] Keep the full APK fallback when a differential patch is unavailable or fails validation (PR #26 implementation and fallback-policy test passed in run #317).

## CI verification
- [ ] Verify release APK package identity in CI.
- [ ] Name signed Actions APK artifacts with resolved versionName and build channel while preserving canonical updater release assets.
- [ ] Verify ARM64-only native libraries and established signing certificate for current beta. Run #232 restored the keystore but skipped APK checks.
- [ ] Publish APK size information and checksum results in the workflow summary.

## Constraints
- Do not create a second updater.
- Do not change the application ID or release signing key.
- Do not add background downloads or automatic installation.
- Follow AGENTS.md: claim work in STATUS, document meaningful changes in STATUS and CHANGELOG-AGENTS.md, and submit task work through PRs targeting `beta`. Chaty alone reviews and merges.

## Acceptance criteria
A clean checkout builds through the committed, pinned wrapper; updater edge cases are covered by passing automated tests; current beta CI verifies the signed ARM64 release, package identity and artifact sizes.

## Roadmap
- **Target:** v1.0.0 release gate.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required automated verification are not complete. Do not ship partial implementations.

---

<a id="issue-5"></a>

## Issue 5 — Owner feedback — LastHaven gameplay and update behavior after release

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/5
**State:** open

## Goal
The repository owner will evaluate published releases on their own schedule and report what works and what does not. This feedback is useful for follow-up fixes but does not block publication.

## Optional release feedback checklist
- [ ] Install and cold-start the release APK on a Android release environment.
- [ ] Confirm GeckoView starts and loads https://lasthavens.com/play.
- [ ] Verify login and a representative gameplay session.
- [ ] Verify WebGL/WebGL2 and WebAssembly functionality.
- [ ] Verify touch input, fullscreen behavior, and orientation behavior.
- [ ] Verify audio and autoplay behavior, including after a user gesture.
- [ ] Verify WebSockets and any required game networking.
- [ ] Verify cookies, IndexedDB, LocalStorage, and Cache Storage persist across app restarts.
- [ ] Verify pause/resume and renderer recovery do not lose the game session unexpectedly.
- [ ] Install a newer APK signed with the established release key over the previous version.
- [ ] Confirm the in-place update preserves browser profile/game data.

## Evidence to record
- Device model and Android version.
- APK version and signing certificate fingerprint.
- Pass/fail result for each check.
- Relevant logs or screenshots with session secrets and personal data removed.
- Any reproducible failures and steps to reproduce.

## Constraints
- Keep GeckoView as the sole browser engine.
- Do not clear browser storage during startup, update, or recovery.
- Do not make speculative performance changes; record measurements before optimizing.
- Follow AGENTS.md and document results in STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
The core game flow and same-key in-place update are verified on real arm64 hardware, with results recorded in the repository.

## Roadmap
- **Target:** v1.0.0 release gate.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required automated verification are not complete. Do not ship partial implementations.

---

<a id="issue-6"></a>

## Issue 6 — P2 — Add minimal launcher settings and safe storage controls

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/6
**State:** open

## Goal
Add essential launcher settings and safe storage visibility after the core game flow is verified on a Android release environment. The current settings popup is usage-focused and incomplete; the next pass prioritizes working functionality, clarity and task flow rather than visual redesign.

## Scope
- [ ] Editable game URL, defaulting to https://lasthavens.com/play.
  - Validate that the value is an absolute HTTPS URL with a non-empty host; reject unsupported schemes and malformed input with an inline explanation.
  - Keep the currently saved URL if validation fails; use the default only when no valid saved value exists.
  - Apply a valid change on explicit Save/Apply by navigating the game surface to the new URL; do not silently reload while the user is typing or merely opening settings.
  - Keep the chosen URL across app restarts.
- [ ] Fullscreen and orientation controls, only where useful and verified.
- [ ] Show app version and GeckoView/Chromium engine version.
  - Verify the pinned GeckoView SDK exposes a supported public version API before implementation; do not infer or hardcode a runtime version.
- [x] Show installed APK size and game/browser profile storage usage, separating cache from persistent game data where Android/GeckoView expose reliable figures (initial implementation; automated path validation pending).
- [x] Add a refresh action and clearly label estimates or unavailable breakdowns.
- [ ] Inspect the pinned GeckoView API for a safe HTTP-cache and code-cache-only clear operation.
- [ ] If both cache types can be cleared safely, add a clearly named Clear game cache action.
- [ ] Add a separate Clear game data destructive action with explicit confirmation; never combine it with cache clearing.
  - Verify the exact pinned GeckoView profile API and lifecycle requirements from official documentation/source before coding; do not guess method names.
  - Explain that this removes saved game login/session and site data; require a second explicit confirmation and ensure the game surface/profile is safely detached before deletion.
  - If no supported safe deletion API exists, leave the action unimplemented and document the limitation rather than deleting profile files by guesswork.
- [ ] Verify settings and storage reporting survive app restarts and do not disrupt the active game session unexpectedly.

## Dependencies
- The navigation/menu entry point from #9 is already implemented.
- Use automated validation for settings functionality; owner feedback is collected after releases and is not a release gate.
- Do not implement cache clearing or game-data clearing until the pinned GeckoView API and lifecycle requirements are verified.

## Constraints
- Keep the UI minimal and game-focused.
- Do not clear cookies, IndexedDB, LocalStorage, Cache Storage, service workers or other game profile data as part of cache clearing.
- Never clear browser data during startup, update, reload or recovery.
- Do not report inaccessible storage components as exact measurements.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Essential settings work on a Android release environment; URL validation and explicit apply behavior are tested; storage figures are clearly defined and accurate within Android/GeckoView API limits; cache clearing is limited to verified cache APIs; destructive game-data clearing is separate, confirmed and backed by a verified GeckoView API.

## Roadmap
- **Target:** v1.1.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required automated verification are not complete. Do not ship partial implementations.

---

<a id="issue-7"></a>

## Issue 7 — P3 — Measure runtime performance and evaluate APK size improvements

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/7
**State:** open

## Goal
Establish reproducible performance and APK-size measurements before making further optimizations.

## Baseline measurements
- [ ] Record device model, Android version, app build type and GeckoView version.
- [ ] Measure cold start to first paint.
- [ ] Measure frame pacing/FPS during a repeatable in-game scene.
- [ ] Record process memory/PSS during startup and gameplay.
- [ ] Record rendering/input latency where measurable.
- [ ] Record exact APK file size and DEX/native-library breakdown.
- [ ] Compare the same test conditions before and after any proposed change.

## Optimization candidates
- [ ] Review release R8 output and mapping/usage reports.
- [ ] Identify app-owned DEX/code that can be safely reduced.
- [ ] Evaluate whether any unchanged assets can be delivered outside the base APK without breaking GeckoView startup or offline/update assumptions.
- [ ] Keep differential APK updates as the preferred way to reduce repeat download size when the engine payload is unchanged.

## Dependencies
- The direct-launch core is integrated; proceed with settings work when prioritized, and collect optional owner feedback separately after releases.

## Constraints
- No speculative Chromium flags, native-library stripping, asset deletion or engine replacement.
- Do not compromise GeckoView compatibility, game storage, package identity or the established signing key.
- Keep arm64-v8a only.
- Record before/after evidence in STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
A repeatable baseline is recorded, and every accepted optimization has measured before/after results with gameplay and persistence still passing.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required automated verification are not complete. Do not ship partial implementations.

---

<a id="issue-8"></a>

## Issue 8 — Superseded — Legacy addon manager removed; future scope waits for owner spec (#23)

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/8  
**State:** closed as not planned; historical context only. Do not restore the removed implementation.

The previous ZIP addon manager was removed from beta at the owner's request on 2026-11-25. Its former implementation checklist is obsolete. Do not restore it or continue the former game-injection approach. A new design will be written only after the owner provides a replacement specification; see #23.

---
<a id="issue-9"></a>

## Issue 9 — P2 — Add a compact game navigation and exit menu

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/9
**State:** open

## Goal
Add a small, game-focused navigation menu that appears when the user attempts to leave the game/app.

## Menu actions
- [x] **Return to game** — dismiss the menu without reloading or changing browser state.
- [x] **Reload** — reload the current game page on explicit user selection.
- [x] **Settings** — open the settings and usage screen.
- [x] **Leave** — exit the game/app using normal Android navigation behavior.

## Interaction and safety
- [x] Intercept the app's back/leave action to show the menu instead of immediately closing the app.
- [x] Make **Return to game** the explicit way to dismiss the menu.
- [x] Do not reload the game, clear storage, or discard the current session when merely opening or dismissing the menu.
- [ ] Add meaningful Compose accessibility semantics/content descriptions to menu actions and verify their spoken labels with TalkBack.
- [ ] Add focused automated coverage for system-navigation/gesture behavior where feasible.
- [ ] Cover all menu actions with focused automated tests where feasible.
- [x] Keep the menu compact and usable in the existing fullscreen/game layout.

## Acceptance criteria
Attempting to leave the app opens the menu; all four actions work as described; controls expose meaningful accessible names; returning to the game preserves the current session and browser state. Automated verification remains pending.

## Roadmap
- **Target:** v1.2.0 — remaining accessibility and automated verification.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

<a id="issue-10"></a>

## Issue 10 — P2 — Track game network data usage

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/10
**State:** open

## Goal
Give users visibility into how much network data LastHavenPlus and the embedded game use.

## Scope
- [x] Measure bytes received and transmitted using Android per-UID counters.
- [x] Show current app-session usage and since-device-boot usage where supported.
- [x] Present download and upload separately, with human-readable units.
- [x] Explain measurement boundaries, counter resets and Android version limitations.
- [ ] Avoid double-counting updater traffic or clearly separate updater traffic from game traffic when feasible; current totals are explicitly labeled app-wide (game + updater).
- [x] Keep measurement lightweight and avoid collecting or transmitting usage data.
- [ ] Add automated tests for counter availability, reset handling and unsupported-state messaging.

## Constraints
- No analytics, telemetry upload or third-party tracking.
- Do not claim per-game precision if Android only exposes per-app counters.
- Preserve the existing manual, user-confirmed update/download flow.
- Document limitations in the UI and developer notes.

## Acceptance criteria
Users can inspect trustworthy sent/received app totals, understand what is included, and refresh/reset only locally stored session statistics if supported. automated CI verification remains pending.

## Roadmap
- **Target:** v1.0.0 verification gate.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required automated verification are not complete. Do not ship partial implementations.

---

<a id="issue-11"></a>

## Issue 11 — P2 — Add privacy-conscious app and updater logs

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/11
**State:** open

## Goal
Add useful, privacy-conscious diagnostics for troubleshooting LastHavenPlus and its updater.

## Scope
- [x] Capture app lifecycle, GeckoView startup/navigation failures and renderer termination events.
- [x] Capture updater checks and download/validation outcomes.
- [x] Add an in-app log viewer.
- [x] Add an explicit share action for diagnostic logs.
- [x] Bound log storage with one rotating previous log.
- [x] Redact credentials, cookies, authorization headers, tokens, session identifiers and sensitive URL parameters in common key/value formats.
- [ ] Add CI tests for redaction, rotation/size limits and failure-path logging; a redaction unit test has been added but CI has not yet been verified.
- [ ] Include the GeckoView/Chromium runtime version in startup diagnostics only if the pinned SDK exposes a supported public version API; otherwise record it as unavailable rather than guessing.
- [x] Keep logs local; do not upload diagnostics automatically.

## Constraints
- Never log passwords, cookies, tokens, private keys, full authorization headers or game session secrets.
- Avoid excessive logging in normal gameplay.
- Do not change GeckoView/game storage behavior to collect diagnostics.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Users can inspect and explicitly share useful diagnostics without exposing authentication/session secrets; logs remain bounded and local. Remaining CI and runtime-version work is tracked for a later release.

## Roadmap
- **Target:** v1.2.0 — remaining diagnostics tests and version reporting.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

<a id="issue-12"></a>

## Issue 12 — P2 — Surface actionable GeckoView load-error details

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/12
**State:** open

## Goal
Make game-load failures easier to diagnose without adding browser-style UI or exposing sensitive information.

## Scope
- [ ] Inspect the exact pinned GeckoView 0.21.0 public API/source for `GeckoViewLoadError` fields before implementation; do not guess property names.
- [ ] Map supported error codes into a small set of user-friendly categories (for example DNS/network unavailable, TLS/certificate failure, server response/load failure, and unknown).
- [ ] Keep the existing Retry action and concise failure screen.
- [ ] Show a safe, simplified category and optional numeric/error code; do not display raw URLs, headers, cookies, tokens, or unfiltered engine messages.
- [ ] Log the underlying safe diagnostic fields through the existing redacting diagnostics path.
- [ ] Provide a generic fallback when GeckoView supplies no useful structured detail.
- [ ] Add tests for category mapping and fallback behavior.

## Constraints
- Keep the game as the primary app surface; no address bar, browser controls, or fallback browser engine.
- Do not expose credentials, session identifiers, or sensitive URL parameters.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Common load failures are distinguishable to users/support using verified GeckoView API fields; unknown errors remain safely handled; Retry behavior is unchanged; tests cover mappings and fallback.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

<a id="issue-13"></a>

## Issue 13 — P2 — Add accessible names to launcher actions

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/13
**State:** closed as duplicate; accessibility work is tracked in #9.

## Goal
Ensure launcher controls are understandable to Android accessibility services, especially TalkBack.

## Scope
- [ ] Add meaningful Compose semantics/content descriptions to Retry, Reload, update/download, menu, Settings, diagnostics, and Exit actions where the visible text does not already provide an adequate accessible name.
- [ ] Avoid redundant descriptions when a button's text already provides its name.
- [ ] Ensure icon-only actions expose a concise action-oriented label.
- [ ] Verify focus order and spoken labels with TalkBack on an arm64 device.

## Constraints
- Keep the current compact game-focused UI.
- Do not add decorative accessibility text or duplicate announcements.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Every actionable control has a clear, non-duplicated accessible name and sensible focus behavior; TalkBack verification is recorded.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

<a id="issue-14"></a>

## Issue 14 — P2 — Add connection diagnostics

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/14
**State:** open

## Goal
Help users identify whether a loading or connection problem is caused by their device/network or by the game website, without collecting telemetry.

## Scope
- Add an explicit connection diagnostics action in the launcher.
- Check DNS/HTTPS reachability of the configured game host and report clear pass/fail results.
- Show measured response time where meaningful, with timeout and unavailable states.
- Distinguish website reachability from actual game-server, login, WebSocket, or gameplay availability; do not claim those are healthy unless separately and safely verified.
- Respect the configured game URL and avoid sending credentials, cookies, or authenticated requests.
- Provide a concise result that can optionally be included in the existing redacted diagnostics export.

## Acceptance criteria
- Tests cover success, DNS failure, timeout, TLS/HTTP errors, and malformed URL.
- Checks run only on user request (or are clearly rate-limited); no background polling or telemetry upload.
- No credentials, cookies, tokens, or full sensitive URLs appear in results or logs.
- UI explains exactly what was and was not tested.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

<a id="issue-15"></a>

## Issue 15 — P2 — Make updater recovery safer

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/15
**State:** open

## Goal
Ensure a failed or interrupted update never leaves the user with a corrupted download or misleading update state.

## Scope
- Stage downloads in temporary files; never replace the installed APK (Android package installation remains user-confirmed).
- Verify expected size and cryptographic checksum before offering installation; verify signing-certificate compatibility using the existing updater policy.
- Preserve the last known-good installed version when download, patching, verification, or installer handoff fails.
- On delta failure, cleanly fall back to the verified full APK where available.
- Provide actionable Retry / download full package guidance and clean up abandoned temporary files safely.
- Record redacted failure reasons through the existing diagnostics logger.

## Download progress and visibility follow-up (2026-11-26)

The owner reported that the UI did not clearly show that an update download was underway. The current Update screen exposes only an indeterminate progress bar while that screen is open; it has no byte/percentage reporting or persistent app-wide status. This work is deferred to v2.3.0 and is tracked in GitHub issue #15.

- [ ] Report received/total bytes and percentage when content length is trustworthy; otherwise show indeterminate progress.
- [ ] Show clear stages for differential download, full-APK fallback, retrying, verification and ready-to-install.
- [ ] Preserve visible progress/status when navigating away from and back to the Update screen.
- [ ] Evaluate a user-initiated Android progress notification for long downloads, respecting Android notification permissions/channels.
- [ ] Test progress, unknown lengths, stage transitions, retry/fallback, completion and cleanup.
- [ ] Keep downloads user-initiated and installation explicitly user-confirmed; no automatic downloading or installation.

## Acceptance criteria
- Automated tests cover interruption, truncated/corrupt files, checksum mismatch, signature mismatch, delta failure/full fallback, and storage errors.
- No unverified APK is offered for installation.
- No automatic installation or silent replacement; Android's user-confirmed installer flow is retained.
- Recovery behavior is documented and tested before release.

## Roadmap
- **Target:** v1.1.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Move unfinished functionality to a later release when appropriate; owner feedback is not a release prerequisite.

---

<a id="issue-16"></a>

## Issue 16 — P2 — Add gameplay orientation and keep-awake settings

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/16
**State:** open

## Goal
Let users choose portrait or landscape gameplay and control whether the display stays awake while the game is active.

## Scope
- Add an orientation setting with Portrait, Landscape, and System/Auto options.
- Apply the selected orientation consistently when entering gameplay and restore the appropriate launcher/settings behavior when leaving it.
- Add a Keep screen awake while playing setting. When enabled, prevent screen timeout only while the game is active; remove the keep-awake flag when leaving gameplay or when the app is no longer in the appropriate foreground state.
- Persist both preferences and apply them on the next launch.
- Keep settings UI usable in both portrait and landscape.

## Acceptance criteria
- No forced orientation outside the selected gameplay behavior.
- Screen-awake state is reliably cleared on exit, backgrounding, errors, and lifecycle transitions as appropriate.
- Tests cover preference persistence and lifecycle cleanup.
- Add automated tests for orientation policy, navigation and keep-awake lifecycle cleanup.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

<a id="issue-17"></a>

## Issue 17 — P2 — Back up and restore launcher settings

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/17
**State:** open

## Goal
Allow users to preserve launcher preferences across reinstallations or device changes without exporting sensitive data.

## Scope
- Add explicit Export and Import actions for launcher settings.
- Use a versioned, human-readable backup format and Android's document picker/storage access flow.
- Export only an explicit allowlist of non-sensitive launcher preferences (for example, orientation, keep-awake, and safe display/settings preferences).
- Never export credentials, cookies, WebView/GeckoView profile data, game cache, tokens, or private game data.
- Validate imported files, handle unknown/newer schema versions safely, and apply changes only after user confirmation.
- Avoid partial preference corruption: validate first, then apply atomically or retain the previous settings.

## Acceptance criteria
- Tests cover valid backup/restore, malformed files, missing fields, unknown versions, and rejected sensitive/unsupported fields.
- Import does not overwrite unrelated app data or game profile data.
- Export/import works through Android's system document picker without broad storage permissions.
- Document the backup format and its privacy boundaries.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Move to a later release if acceptance criteria or required device verification are incomplete; do not ship partial implementations.

---

<a id="issue-18"></a>

## Issue 18 — Roadmap — v2.2.0 direct-launch experience and reliability

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/18  
**State:** open; planning and sequencing only. The canonical roadmap, ownership and release gates are maintained in GitHub issue #18.

The stable v2.1.0 release is published. Beta already uses Mozilla GeckoView 160; Cefrium replacement is complete at source/integration level. The v2.2.0 implementation scope is the direct-launch and responsive in-game overlay redesign (#56) plus the approved recreated icon (#84), integrated in beta. Optional owner feedback is collected after releases; remaining updater/release reliability work is deferred.

### Current phases
- [x] Housekeeping, v2.1.0 release record and contributor workflow reconciliation.
- [x] **Phase 1 — #56:** Chaty's direct-launch and responsive overlay implementation merged via PR #83; automated navigation policy tests pass.
- [ ] **Phase 2 — #5/#21:** optional owner feedback on gameplay, GeckoView profile/session, Web APIs, media, permissions and update behavior after release; collected on the owner's schedule, not a release gate.
- [ ] **Phase 3 — #4/#15/#31:** updater regression coverage and release-ID/differential fallback verification in a future owner-authorized release workflow.
- [ ] **Phase 4 — #9–#17/#28:** accessibility, diagnostics, settings, network/storage accuracy and measured polish in the order recorded in AGENT_WORK.md.
- [ ] **Phase 5:** v2.2.0 release gates; owner approval is required for promotion, tagging and publication.

### Deferred and protected scope
- **Historical hold superseded:** owner reauthorized issue #23 on 2026-11-25/26. Current ownership is Chaty (`agent:chaty`); Phase A is in PR #152. Phase B remains unauthorized.
- **#19 beta release channel:** not a v2.2.0 prerequisite.
- Do not modify the game, application ID, release signing identity, GeckoView engine or update-consent policy as incidental roadmap work.
- No automatic downloads/installations, debug APK publication, or unapproved release activity.

See the GitHub issue #18 for the full phase checklist and release gates. `AGENT_WORK.md` is the current assignment queue; do not infer assignments from this historical index.

---

<a id="issue-19"></a>

## Issue 19 — P1 — Add an opt-in beta release channel to the updater

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/19
**Owner:** Chaty, queued immediately after #4 is handed off.
**Goal:** Let users explicitly opt into published beta/prerelease builds while keeping Stable as the default.

- [ ] Persist Stable/Beta channel selection; existing and new installs default to Stable.
- [ ] Stable checks published non-prerelease releases only; Beta checks published prereleases only.
- [ ] Never use branch builds, Actions artifacts or unpublished drafts.
- [ ] Reuse all existing manifest, APK digest, package identity, signing certificate and delta/full fallback validation.
- [ ] Clearly identify beta updates and selected channel; channel switching never downloads or installs.
- [ ] Never offer a downgrade; handle empty/paginated API results and malformed release metadata safely.
- [ ] Add deterministic tests for filtering, pagination, no-downgrade and channel persistence.

**Non-goals:** no auto-download/install, no change to stable release publishing, no UI redesign.

**Dependency:** #4. Coordinate with #6; Chaty owns the updater channel control and Ploy must not duplicate it.

<a id="issue-20"></a>

## Issue 20 — P1 — Complete the GeckoView 160 artifact and dependency evidence record

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/20  
**State:** open; remaining documentation/research only.  
**Owner:** Chaty.

**Current status (2026-11-25):** GeckoView 160 ARM64 is already integrated in beta and shipped in v2.1.0. This issue is not a migration blocker and does not authorize dependency or toolchain changes.

### Remaining scope
- Record the exact pinned artifact POM and transitive dependency inventory.
- Record applicable license and notice obligations.
- Cross-check project-used API names against the pinned artifact's official Javadocs/API documentation.
- Record measured APK-size impact using comparable verified artifacts/builds, or explicitly document why a reliable comparison is unavailable.

### Exclusions
- Do not restart or re-implement the Cefrium migration.
- Do not change GeckoView, Gradle, AGP, JDK, Android SDK, Build Tools, signing or ABI configuration.
- Do not infer runtime behavior from dependency research or CI; that remains under #5/#21.
- No game/client changes, no Addon Manager Phase B/runtime work (#23 Phase A is separately owner-approved), release, tag or main promotion.

**Completion evidence:** update `docs/GECKOVIEW_MIGRATION.md` with official references, artifact-specific findings and unresolved limitations; keep this issue open until each remaining item is evidenced.

---

<a id="issue-21"></a>

## Issue 21 — Owner feedback — GeckoView 160 runtime and profile behavior after release

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/21  
**State:** open; optional post-release owner feedback.  
**Owners:** Chaty coordinates; repository owner/device tester supplies hardware evidence.

**Current status (2026-11-25):** GeckoView 160 is integrated in beta and shipped in v2.1.0; Cefrium is removed from the production build. The older engine-migration implementation scope is complete and must not be repeated here. CI verifies automated build, test and packaging behavior; optional runtime feedback is collected from the owner after releases.

### Feedback areas
- Record device model, Android version, exact app commit/version and APK digest.
- Verify GeckoRuntime/GeckoSession lifecycle, cold start, pause/resume, renderer recovery and session restoration.
- Verify login and persistent cookies/site data across force-stop/restart and same-key update.
- Verify WebGL/WebGL2, WebAssembly, WebSockets, touch/IME, audio, fullscreen and required permission prompts.
- The direct-launch/overlay implementation is integrated; optional owner feedback is collected separately in #5/#21 after releases.
- Record pass/fail results and sanitized evidence in `STATUS.md`.

### Exclusions
- No Cefrium removal or GeckoView migration implementation work.
- No claim of runtime compatibility based on CI, emulator tests or an undetailed owner smoke-test report.
- Do not copy/delete Cefrium profile data or assume cross-engine profile portability.
- No changes to application identity, signing, ABI, updater policy or game/client; #23 Phase A is separately owner-approved and owned by Chaty, while Phase B remains unauthorized.

**Completion:** keep open for owner feedback and follow-up defects. Pending feedback does not block release publication.

---

<a id="issue-22"></a>

## Issue 22 — P1 — Add the LastHavenPlus home screen with Play and Settings

**Current assignment:** Chaty owns the home screen and navigation implementation in [PR #26](https://github.com/iNeonyne/LastHavenPlus/pull/26). PR run #317 passed the code build/unit tests; merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Ploy has no active assignment on this issue. Required destinations are app home, settings and the existing game; the game menu must provide Reload, Home and Return to game. Home returns to the launcher home screen, and Return to game must preserve the existing GeckoSession.


**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/22  
**Internal owner:** Chaty, active. PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`.

Fresh launch shows a simple branded home screen with Play and Settings. Do not load the game until Play is selected. Settings must be usable without starting a browser session. Preserve the Gecko session when navigating home and make immersive/keep-awake behavior gameplay-only. Define and test Back behavior.

---

<a id="issue-23"></a>

## Issue 23 — P0 — Integrate and harden the supplied GeckoView addon manager

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/23  
**State:** ACTIVE — the owner explicitly reauthorized work on 2026-11-25/26. Chaty (`agent:chaty`) owns native Phase A in draft PR #152, currently synced to beta at `97570ea` and awaiting exact-head CI/review.  
**Current source:** owner-supplied Addon Manager ZIP v0.5.1 (Chrome Manifest V3); it supersedes the earlier XPI 0.11.0 as the implementation target.  
**Phase A scope:** bounded local ZIP import/export, app-private library persistence, TOC metadata/dependency analysis and Compose management UI. Enabled state is metadata only; no runtime injection or in-game activation is implemented.  
**Phase B:** GeckoView 160 runtime adaptation, XHR/Worker interception and game-resource masking remain separate and are not authorized by PR #152.  

### Historical XPI 0.11.0 audit (superseded by ZIP 0.5.1)
The following checklist documents the earlier XPI proposal only. It is not the acceptance checklist for the current ZIP 0.5.1 Phase A work; see GitHub issue #23 for current source, phases and gates.

### Required fixes from the historical XPI audit
- [ ] Reject oversized import/restore files using `File.size` before `arrayBuffer()` allocation.
- [ ] Stream network responses with hard byte caps even when Content-Length is missing or false; add timeouts and cancellation.
- [ ] Bound repository/catalog/TOC response sizes and concurrency.
- [ ] Replace recursive dependency traversal with iterative cycle-safe ordering.
- [ ] Correct closed/open-ended/suffix HTTP Range parsing and HEAD content length.
- [ ] Preserve last-known-good catalog data when a refresh is partial or fails; remove fail-open permission reporting.
- [ ] Reduce full-library/worker payload copies and repeated repository tree scans; measure memory.
- [ ] Verify exact GeckoView 160 WebExtension APIs; bundle the reviewed unsigned extension through the supported built-in extension API.
- [ ] Implement app-owned tab/session delegation; do not leave manager options, source, screenshot or Play flows creating unmanaged sessions.
- [ ] Add security, archive, network, dependency, range, cache and GeckoView integration tests.

- **Constraints:** Preserve archive path/CRC/duplicate/count/expanded-size checks. Keep Android 10/API 29+, ARM64-only, app ID and signing identity. Do not modify the game client or claim Lua initialization from injection-layer requests alone. See the GitHub issue for full scope and acceptance criteria.

---

**Integration checkpoint (2026-11-25):** PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. PR run #329 passed unsigned release build and unit tests for the exact PR head. Signed post-merge ARM64 CI and owner feedback remains optional. `main` and `beta` are diverged (beta ahead 166, main ahead 9); review the nine main-only commits before any promotion.


**Post-merge CI (2026-11-25):** Run [#330](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518704468) passed signed optimized release build, unit tests, established certificate verification and ARM64-only packaging for `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Artifact upload succeeded. Optional owner feedback on gameplay and update behavior is collected after releases.


---

## Issue 28 — P2 — Make storage usage reporting accurate and robust

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/28  
**State:** open; queued. Audit the actual app-private directories represented by storage totals, clarify omissions, and replace recursive directory traversal with iterative traversal. Keep APK, persistent data and cache measurements separate.

---

## Issue 29 — P1 — Reconcile beta documentation with current source and CI evidence

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/29  
**State:** open; documentation candidate on `chaty/docs-reconciliation-v2.1`. Reconcile README, STATUS, ISSUES and release-facing claims against PR #43, exact commit CI evidence and outstanding automated gates. Keep open until the docs PR is merged and verified.


## Issue 31 — P0 — Make release packaging resilient to optional differential update failures

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/31  
**State:** open; PR #37 merged the optional differential fallback hardening. The later signed beta run #45601558602 passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks for the v2.1.0 candidate (recorded in PR #47). Keep this issue open because that run does not exercise every release-tag optional-delta failure path. Require a separately authorized versioned release-workflow test before closing; do not publish or alter release policy just to test it.


---

## Issue 60 — P2 — Make Android lint and Kotlin formatting a reliable quality gate

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/60  
**Owner:** Chaty  
**Branch:** `chaty/ci-quality-gate` → `beta`

### Goal
Add a focused, read-only quality workflow for Android lint and Kotlin formatting without duplicating the existing Android PR build/test job.

### Scope
- Add `.github/workflows/quality.yml` for relevant source/build changes pushed to beta. PR validation is handled by the existing `android.yml` workflow.
- Run `lintDebug` and a pinned Spotless/ktfmt `spotlessCheck`.
- Use the existing JDK 25, Android SDK 37.2 / Build Tools 37.0.0 and committed Gradle Wrapper.
- Apply formatting incrementally against `origin/beta` for PR validation and the previous beta commit for post-merge pushes, so legacy formatting is not rewritten in a giant unrelated diff.
- Keep workflow permissions read-only; do not expose signing secrets or upload/publish APKs.
- Upload Android Lint reports. Existing Android PR validation remains responsible for unsigned build, JVM unit tests, Python updater-helper tests and their reports.
- Ignore documentation-only edits.

### Non-goals
- No application behavior, GeckoView, ABI, signing, updater or release-policy changes.
- No changes to main. No release, tag or publication.
- No broad formatting rewrite of legacy Kotlin files.

### Acceptance criteria
- Exact-head Android PR validation passes `lintDebug`, `spotlessCheck`, the unsigned release build, JVM unit tests and Python updater-helper tests.
- A deliberately misformatted changed Kotlin file fails the formatting check; the formatter command is documented.
- Android PR validation continues to run the unsigned build, JVM tests and Python helper tests.
- Quality job uses read-only permissions and no release secrets.
- Documentation accurately describes which workflow runs which checks.

### Verification
Record exact branch head, Actions run URLs, lint results, formatter result and limitations here and in STATUS.md. The negative test is recorded separately from the final passing exact-head validation.


## v2.2.0 delivery ownership (2026-11-25)

- **Chaty:** owns roadmap #18, central documentation, issue triage, branch audit, CI evidence, PR review and beta integration.
- **Chaty:** implemented #56 in PR #83 and integrated the approved icon in PR #85; selective stable promotion merged in PR #87.
- **Ploy:** no active assignment; supporting work remains deferred to #86 pending owner prioritization.
- **Owner release feedback (#5/#21):** optional post-publication feedback; the owner will share findings on their own schedule and it is not a release gate.
- **#42:** the old v2.1.0 Home-screen-first acceptance scope is superseded by #56. Do not duplicate its old Home screen/menu implementation.
- **#60:** exact PR validation passed in run #45627915789; post-merge quality workflow #45631250015 and signed beta build workflow #45631249817 both completed successfully. Issue can be closed as completed.


<a id="issue-106"></a>

## Issue 106 — P2 — Improve gameplay loading, reconnect and load-failure UX

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/106
**State:** open; deferred candidate under #86

Improve the existing direct-launch game loading and failure experience with a restrained branded loading state, honest progress only when measurable, clear retry and safe failure presentation. Coordinate GeckoView error categories with #12. Do not add a home screen, browser chrome, background polling, fabricated progress or game-client changes. Preserve GeckoSession/profile state and ensure stale loading UI never obscures successful gameplay. Test loading/error/retry transitions and lifecycle interruption.

<a id="issue-107"></a>

## Issue 107 — P3 — Establish consistent visual design and interaction polish

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/107
**State:** open; deferred candidate under #86

Create a small shared Compose design system and apply it to existing launcher-owned screens (in-game overlay, Settings, Diagnostics and Update). Improve hierarchy, spacing, touch targets, contrast, responsive layouts and restrained transitions. Keep fullscreen gameplay unobstructed. No home screen, persistent gameplay HUD, game-client changes or functional behavior changes in this visual-only task. Coordinate with #6/#9/#11/#12/#14/#15 and add layout/screenshot checks where feasible.


## Owner-requested UX triage — 2026-11-26

The additional UX proposal was reviewed and consolidated into existing issues rather than creating duplicate tasks:

- **#15 (with #4):** measurable download bytes/percentage/speed/ETA, inline retry and full-APK retry, explicit ready-to-install / install-later state, storage preflight, differential-vs-full size savings, formatted release notes and prior-release history.
- **#6:** installed versionName/versionCode, optional Dark/Light/System theme for launcher-owned screens (Dark default), and accessible storage loading placeholders.
- **#14:** reliable offline hint and user-triggered connection check; no background polling.
- **#107:** restrained haptic feedback and shared theme-token coordination.

**Explicit exclusions:** no new launcher HomeScreen, HomeScreen pull-to-refresh or update badge (the game has its own home screen); no automatic update check on launch; no unattended/background download or automatic installation; no external-browser fallback. Update checks and downloads remain explicitly user-initiated. Android's package installer confirmation remains mandatory. All work is deferred under #86 pending owner prioritization.


## 2026-11-26 — Issue #9 accessibility implementation integrated

- PR [#139](https://github.com/iNeonyne/LastHavenPlus/pull/139) merged to beta at `0d5ab712ce5195a3e71023c68da54f054645a8e2` after owner authorization.
- Exact PR-head Android and Markdown CI passed. Physical ARM64 TalkBack/focus/traversal, reduced-motion and large-font verification remain outstanding; `FocusRequester` is Compose input focus, not TalkBack accessibility focus.
- Issue remains open with `agent:owner` pending the outstanding device verification. Do not represent those checks as complete.


## 2026-11-26 — Issue #23 implementation checkpoint

- Owner reauthorized Addon Manager work; issue #23 is open and owned by Chaty (agent:chaty).
- Native Phase A candidate is in draft [PR #145](https://github.com/iNeonyne/LastHavenPlus/pull/145): bounded ZIP import/export, app-private library persistence, storage preflight and transaction rollback, TOC required/optional dependency metadata, iterative dependency analysis, localized removal confirmation, and overlay navigation.
- Exact-head CI is pending after the latest review-driven changes. Do not mark complete or merge yet.
- The supplied Chrome MV3 runtime, XHR/Worker interception, game-resource masking and in-game Lua initialization are not implemented or enabled. GeckoView 160 runtime verification remains separate work.

## 2026-11-26 — Chaty takes over Addon Manager Phase A; Vibe reassigned (#23 / #11)

- Repository owner directed Chaty to take over the remaining #23 implementation. Issue #23 is `agent:chaty`; Vibe must not continue work on PR #152 or its branch.
- Vibe is assigned issue #11 (`agent:vibe`) for isolated diagnostics redaction, rotation/size-bound and failure-path tests; this is independent of #23.
- Chaty continues [PR #152](https://github.com/iNeonyne/LastHavenPlus/pull/152) on its existing branch. Phase A only; no Chrome MV3 runtime, XHR/Worker interception, game-resource masking or in-game Lua initialization.
- Recovery now records old/new index state, distinguishes missing from malformed index data, rejects malformed/unsafe transaction journals, and makes backup restoration replay-safe after an interrupted recovery. Regression tests cover a second recovery pass, mismatched/malformed index state, malformed journal paths and an absent original index.
- Exact-head implementation commit `c9067bd35323fff167a014fcda9904caf7d81617`: Android run [#45872047540](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45872047540) passed lint, Spotless, unsigned release assembly and 137/137 JVM tests; Markdown run [#45872047492](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45872047492) passed. A packaged-MIT-notice assertion is being added before final review.
- PR remains draft; no merge, release, tag or physical-device compatibility claim. Phase B remains out of scope.
---

## Code-review follow-ups — 2026-11-26

The beta code review found the strongest improvement opportunities are structural and lifecycle-focused. These are separate, unassigned tasks; they do not authorize release promotion.

### Issue 146 — P1 — Extract GeckoView game-session ownership from MainActivity
**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/146  
Extract session open/reload/restore/close and load/crash state into a focused controller while preserving the lazy process-scoped GeckoRuntime, current crash-vs-navigation semantics, and the same live session when Settings/Diagnostics/Update detach the view. Document the keep-alive invariant and add focused state-transition tests. Coordinate with #23 and any MainActivity/GeckoSession work.

### Issue 147 — P1 — Consolidate launcher navigation and back-stack state
**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/147  
Introduce one pure navigation policy or lightweight sealed back stack for Game, Settings, Diagnostics and Update. Preserve direct launch, Back overlay and return behavior; avoid adding Compose Navigation without need. Depends on #146.

### Issue 148 — P2 — Centralize gameplay orientation and immersive window state
**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/148  
Create a single GameplayUiState/window boundary for existing orientation and system-bar/immersive behavior. Keep persisted orientation and keep-awake preferences in #16; this issue adds no new user controls.

### Issue 149 — P2 — Move launcher-owned user-facing strings into Android resources
**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/149  
Move launcher-owned UI strings to Android resources without changing wording, game-hosted content, diagnostics payloads or protocol values. No translation work is included.

### Issue 150 — P2 — Document GeckoView content and Android permission policy
**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/150  
Verify the current manifest and GeckoView delegates, then document the strict allowlist and deny-by-default behavior. Documentation only; no permissions or runtime behavior may change.

### Existing updater/release follow-ups
- #15 remains the single coordinated updater recovery/UX scope. Its new review note covers pending installer URI dismissal/abandonment semantics, actionable user-facing error mapping with detailed redacted diagnostics, and safe explicit cancellation/partial-file cleanup. Coordinate with #111; do not create a second updater or parallel owner.
- #4 now also calls out an optimized-release/R8 smoke checklist covering FileProvider configuration, package identity, signing, ARM64 packaging, release installation and GeckoView game-load verification after GeckoView bumps.
- Theme-token centralization is already represented by integrated #107 and persisted theme preference belongs to #6; do not duplicate either.
- Download progress remains in #111; do not duplicate its in-progress implementation.


## 2026-11-26 — Issue #150 permission policy documentation

- #150 is assigned to Chaty (`agent:chaty`) for documentation only.
- Verified current beta source: `MainActivity.setupGeckoDelegates` allows only persistent storage and audible/inaudible autoplay for `lasthavens.com` and subdomains; all other content permissions/origins are denied; Android runtime and media permission requests are rejected.
- Manifest declares `INTERNET` and `REQUEST_INSTALL_PACKAGES`; no camera, microphone, geolocation or notification permissions are declared.
- No runtime policy change is authorized. Exact-head Markdown validation passed on PR #158 head `e2ce22c44508a87f53fc4a57a03f82e0597550bc` (run #45889911748); internal links/anchors passed across all 15 Markdown files. Final review/merge remains pending.

## 2026-11-27 — GameSessionController integration (#146)

- PR [#173](https://github.com/iNeonyne/LastHavenPlus/pull/173) squash-merged to `beta` at `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`; issue #146 closed as completed.
- Exact-head Android validation #45921162358, unsigned APK reproducibility #45921162325 and Markdown #45921162310 passed. Post-merge Markdown #45923143716 passed; post-merge Android #45923143717 and reproducibility #45923143712 were still running at this checkpoint.
- No physical ARM64 device testing is claimed.
- #147's #146 dependency is satisfied. Chaty moved #147 from `agent:blocked` to `agent:queued`; it is not an active assignment and must not start until Chaty explicitly assigns it after checking Vibe's current #167 work.
- #167 remains Vibe's active Phase B bridge assignment. #168/#169 remain blocked pending bridge implementation and contract review.

## 2026-11-27 — Final post-merge verification for #146

- Post-merge Android run #45923143717 passed signed optimized APK build, JVM tests, signing-certificate verification and ARM64-only ABI checks; signed APK artifact upload succeeded. Release-tag/publication and differential-package steps were skipped because this was a beta push, not an authorized versioned release.
- Independent unsigned APK reproducibility #45923143712 passed both clean builds and final archive/hash comparison.
- Post-merge Markdown #45923143716 passed on merge commit `5ed6af6`; documentation-only follow-up Markdown #45923558131 passed on `3856a4f`.
- No physical ARM64 device testing is claimed.

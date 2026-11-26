# Project backlog — LastHavenPlus

**Source of truth:** GitHub Issues and this version-controlled backlog. Keep issue scope, status and acceptance criteria aligned. Do not close an issue because documentation changed.

## Current state after v2.3.0

- **Latest stable:** v2.3.0 is published on main at tag commit ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88.
- **Development:** beta is at 7cd898f4c1f9b7f70787a7000e1ed9343a83540d; no new release or promotion is in progress.
- **CI:** PR #132 consolidated beta lint/format checks into the existing signed ARM64 build job and is merged. Exact-head PR validation and post-merge signed CI passed. Issue #129 is closed as completed after the measured result was recorded.
- **Housekeeping:** #128 is Chaty-owned; it reconciles repository status and work records. #70 remains open until this reconciliation is integrated.
- **Updater:** #4 remains open for remaining regression coverage. #15, #31 and #111 are blocked/deferred; no release-ID/differential fallback runtime verification is claimed.
- **Reproducibility:** #66 / PR #119 has a prior byte-identical unsigned APK result, but its branch is behind current beta and needs refresh plus exact-head reruns before merge.
- **Owner-paused:** #20 / PR #122 and #62 / PR #123 are explicitly deferred; keep their drafts open for traceability and do not resume. #23 Addon Manager remains on hold.
- **UI/UX:** PR #113 remains draft/open for traceability only; #107 is closed after later integration. The owner instructed Chaty to own any future UI/UX follow-up.
- **Other deferred work:** #86 and #19 are not active roadmap commitments. Do not create the next roadmap until the owner asks after housekeeping.

## Current ownership

- **Chaty:** #128 housekeeping, #129 CI closeout, #4 updater test coverage, #81 approved library-only dependency refresh, and final #70 documentation reconciliation.
- **Vibe:** no active implementation assignment at this snapshot. Owner-paused drafts #122/#123 must remain paused.
- **Owner decision:** #65 and any release/promotion/roadmap authorization remain with the repository owner.

## Backlog index

| Issue | Priority | Summary | Current state |
|---|---:|---|---|
| [#4](#issue-4) | P0.1 | Build reproducibility and updater test coverage | Open; versioned CI APK artifact naming added to active Chaty scope; updater edge-case coverage remains |
| [#5](#issue-5) | Feedback | Owner feedback — gameplay and update behavior after release | Open; owner will provide release feedback on their own schedule; not a release blocker |
| [#6](#issue-6) | P2 | Essential launcher settings and safe storage controls | Open; includes version identity, theme preference and accessible storage loading-state candidates |
| [#7](#issue-7) | P3 | Runtime performance and APK size | Open; measure before optimizing |
| [#8](#issue-8) | — | Legacy addon manager | Closed — superseded; no planned work |
| [#9](#issue-9) | P2 | Overlay accessibility and navigation | Open; TalkBack semantics and automated navigation coverage remain for the #56 overlay |
| [#10](#issue-10) | P2 | Persistent app-wide network usage totals | Open; app-wide counters are implemented, automated counter tests remain |
| [#11](#issue-11) | P2 | Privacy-conscious log tests | Open; queued after #9 |
| [#12](#issue-12) | P2 | GeckoView load-error details | Open |
| [#13](#issue-13) | — | Accessible names / legacy addon-manager compile work | Closed — consolidated into #9 |
| [#14](#issue-14) | P2 | Connection diagnostics | Open |
| [#15](#issue-15) | P2 | Updater recovery and download progress visibility | Open; adds measurable speed/ETA, inline retry, install-later confirmation, storage preflight, package-size comparison and release-note/history UX |
| [#16](#issue-16) | P2 | Gameplay orientation and keep-awake settings | Open |
| [#17](#issue-17) | P2 | Settings backup and restore | Open |
| [#18](#issue-18) | Roadmap | v2.2.0 roadmap and release gates | Open; planning and sequencing updated |
| [#19](#issue-19) | P1 | Opt-in beta release channel | Open |
| [#20](#issue-20) | P1 | GeckoView 160 artifact/dependency audit | Decision implemented; POM, transitive dependencies, notices and size evidence remain |
| [#21](#issue-21) | Feedback | Owner feedback — GeckoView runtime/profile behavior after release | Open; optional owner feedback and reported follow-up defects; not a release blocker |
| [#22](#issue-22) | Superseded | Home screen and Play/Settings navigation | Replaced by direct-launch/overlay scope #56; owner feedback tracked in #5/#21 |
| [#23](#issue-23) | Deferred | Integrate and harden supplied GeckoView addon manager | On hold by owner instruction; do not resume without explicit approval |
| [#28](https://github.com/iNeonyne/LastHavenPlus/issues/28) | P2 | Storage usage accuracy and iterative directory sizing | Open; queued after #10 |
| [#42](https://github.com/iNeonyne/LastHavenPlus/issues/42) | Superseded | v2.1.0 Home-screen UI verification | Superseded by direct-launch redesign #56; new UX verification belongs to #5/#56 |
| [#29](https://github.com/iNeonyne/LastHavenPlus/issues/29) | Done | Reconcile beta documentation and CI evidence | Closed as completed after PR #46 merged to beta |
| [#31](https://github.com/iNeonyne/LastHavenPlus/issues/31) | P0 | Draft release asset verification and differential fallback | PR #54 merged; exact-head unsigned and post-merge signed beta runs passed; live release-ID path/fallback checks remain |
| [#56](https://github.com/iNeonyne/LastHavenPlus/issues/56) | Done | Direct launch and responsive in-game overlay | PR #83 merged to beta; PR #87 selectively promoted implementation to main; exact beta validation and signed CI passed |
| [#60](https://github.com/iNeonyne/LastHavenPlus/issues/60) | Done | Android lint and Kotlin formatting quality gate | PR #72 merged; exact-head PR and post-merge beta quality/build workflows passed |
| [#84](https://github.com/iNeonyne/LastHavenPlus/issues/84) | Done | Approved recreated LastHavens-inspired launcher icon | PR #85 merged to beta; PR #87 promoted icon to main; beta CI passed |
| [#86](https://github.com/iNeonyne/LastHavenPlus/issues/86) | Deferred | v2.3.0 deferred reliability, updater and supporting-feature backlog | Open coordination index; linked issues remain the detailed sources of truth |
| [#106](https://github.com/iNeonyne/LastHavenPlus/issues/106) | P2 | Gameplay loading, reconnect and load-failure UX | Open; deferred candidate; coordinate with #12 |
| [#107](https://github.com/iNeonyne/LastHavenPlus/issues/107) | P3 | Consistent visual design and interaction polish | Open; deferred; includes restrained haptics and theme-token coordination; no home screen |

---

## How new work is added

GitHub Issues are the canonical records for each task's discussion, owner, dependencies and lifecycle. This file is the project-wide index and roadmap checklist. The GitHub issue number is the only task ID; do not invent a parallel numbering scheme.

**Chaty handles intake and triage.** For every new request, search open and closed issues and this index for duplicates before creating anything. Reuse or update an existing issue whenever it already covers the requested outcome. New issues must use one of the forms in `.github/ISSUE_TEMPLATE/` and include a clear outcome, bounded scope, non-goals, testable acceptance criteria, verification plan and dependencies. New issues receive `agent:unassigned`; Chaty then triages and explicitly assigns one owner. The GitHub workflow that creates/applies ownership labels is `.github/workflows/agent-ownership.yml`; it must be present on the repository default branch before issue-open automation becomes active. Only then add a concise entry here.

Priority guide: P0 = core/release blocker or data/security risk; P1 = important reliability or planned release capability; P2 = normal improvement; P3 = optional/low urgency. Priority does not guarantee scheduling or inclusion in a release. Set a target release/milestone only after triage.

Lifecycle: Needs triage → Ready → Active → Review → Done; Blocked and Deferred are explicit side states. An issue is not Ready just because it is open. One active owner per issue. Owner, exact subtask, permitted files, branch and dependencies must be recorded in `STATUS.md` before editing. Ploy only works on the assignment in `AGENT_WORK.md`; Chaty owns issue creation, triage and central tracking.

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
- **#23 Addon Manager: ON HOLD.** Do not investigate, assign or implement without fresh explicit owner approval.
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
- No game/client changes, Addon Manager work (#23 remains on hold), release, tag or main promotion.

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
- No changes to application identity, signing, ABI, updater policy or game/client; #23 Addon Manager remains on hold.

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

## Issue 23 — P1 — Integrate and harden the supplied GeckoView addon manager

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/23  
**State:** ON HOLD by owner instruction (2026-11-25); do not resume without explicit owner approval.  
**Release target:** proposed v2.1.0 roadmap (#18).

The owner later placed all addon-manager work on hold. The previous XPI specification and audit are retained as historical context only; do not investigate, assign or implement the manager until the owner explicitly approves resuming it.

### Required fixes from the XPI audit
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

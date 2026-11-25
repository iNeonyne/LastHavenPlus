# Project backlog — LastHavenPlus

**Source of truth:** GitHub Issues and this version-controlled backlog. Keep issue scope, status and acceptance criteria aligned. Do not close an issue because documentation changed.

## Current priorities

1. **P0 — Release workflow reliability (#31):** PR #37 differential fallback hardening and PR #54 draft-ID cleanup/verification are integrated. PR #54 exact-head unsigned validation and post-merge signed beta validation both passed (runs #45610110307 and #45610518305). Keep #31 open until a future owner-authorized release run exercises the draft-ID path and fallback failure cases.
2. **P0 — Physical ARM64 verification (#5):** Gameplay, GeckoView profile persistence, navigation, audio/GPU behavior and same-key updater installation remain unverified on hardware.
3. **P0 — Updater regression coverage (#4, #15):** Updater hardening is integrated at beta commit `1d6c1ec42becab44735edfd0323dfc60ff4b9f9d`; additional malformed-manifest, digest, patch and interrupted-download tests remain.
4. **P1 — Addon manager integration (#23):** On hold by owner instruction. Do not resume or assign work until explicit owner approval.
5. **P2 — Storage measurement accuracy (#28):** Audit all app-private data paths and replace recursive directory sizing with iterative traversal.
6. **P2 — Remaining product polish:** #9, #11, #12, #14, #16 and #17 only after core reliability gates.
7. **P3 — Performance and APK size (#7):** Measure before optimizing; do not expand v2.1.0 scope without owner approval.

**Release roadmap:** [#18 — v2.1.0 post-release verification and reliability](https://github.com/iNeonyne/LastHavenPlus/issues/18). v2.1.0 is published at [GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.1.0); stable README/CHANGELOG are reconciled on main. Physical ARM64 gameplay/profile/UI/update verification remains outstanding. The addon manager (#23) is on hold and is not part of active scope.

## Backlog index

| Issue | Priority | Summary | Current state |
|---|---:|---|---|
| [#4](#issue-4) | P0.1 | Build reproducibility and updater test coverage | Open; v2.1.0 signed ARM64 artifact verified, physical update and remaining updater tests remain |
| [#5](#issue-5) | P0.2 | Physical ARM64 gameplay and update verification | Open; owner smoke test reported, full feature matrix remains unverified |
| [#6](#issue-6) | P2 | Settings and safe storage controls | Open; implementation exists, device verification remains |
| [#7](#issue-7) | P3 | Runtime performance and APK size | Open; measure before optimizing |
| [#8](#issue-8) | — | Legacy addon manager | Closed — superseded; no planned work |
| [#9](#issue-9) | P2 | Accessibility and navigation verification | Open; TalkBack semantics and physical navigation checks remain after #42 |
| [#10](#issue-10) | P2 | App-wide network usage verification | Open; app-wide counters are implemented, physical readings remain to verify |
| [#11](#issue-11) | P2 | Privacy-conscious diagnostics | Paused; further tests remain |
| [#12](#issue-12) | P2 | GeckoView load-error details | Open |
| [#13](#issue-13) | — | Accessible names / legacy addon-manager compile work | Closed — consolidated into #9 |
| [#14](#issue-14) | P2 | Connection diagnostics | Open |
| [#15](#issue-15) | P2 | Updater recovery | Open |
| [#16](#issue-16) | P2 | Gameplay orientation and keep-awake settings | Open |
| [#17](#issue-17) | P2 | Settings backup and restore | Open |
| [#18](#issue-18) | Roadmap | v2.1.0 post-release verification and reliability | Open; release published, physical and workflow follow-up remains |
| [#19](#issue-19) | P1 | Opt-in beta release channel | Open |
| [#20](#issue-20) | P1 | GeckoView 160 artifact/dependency audit | Decision implemented; POM, transitive dependencies, notices and size evidence remain |
| [#21](#issue-21) | P0 | GeckoView runtime/profile hardware verification | Migration integrated; physical GeckoView checks remain alongside #5 |
| [#22](#issue-22) | P1 | Home screen and Play/Settings navigation | Closed — source implementation integrated; physical checks tracked in #5/#42 |
| [#23](#issue-23) | Deferred | Integrate and harden supplied GeckoView addon manager | On hold by owner instruction; do not resume without explicit approval |
| [#28](https://github.com/iNeonyne/LastHavenPlus/issues/28) | P2 | Storage usage accuracy and iterative directory sizing | Open; not started |
| [#42](https://github.com/iNeonyne/LastHavenPlus/issues/42) | P1 | v2.1.0 launcher UI verification | Source implementation integrated by PR #43; physical UI verification remains |
| [#29](https://github.com/iNeonyne/LastHavenPlus/issues/29) | Done | Reconcile beta documentation and CI evidence | Closed as completed after PR #46 merged to beta |
| [#31](https://github.com/iNeonyne/LastHavenPlus/issues/31) | P0 | Draft release asset verification and differential fallback | PR #54 merged; exact-head unsigned and post-merge signed beta runs passed; live release-ID path/fallback checks remain |
| [#60](https://github.com/iNeonyne/LastHavenPlus/issues/60) | P2 | Android lint and Kotlin formatting quality gate | In progress on beta; lint + incremental Spotless/ktfmt workflow being added |

---

## How new work is added

GitHub Issues are the canonical records for each task's discussion, owner, dependencies and lifecycle. This file is the project-wide index and roadmap checklist. The GitHub issue number is the only task ID; do not invent a parallel numbering scheme.

**Chaty handles intake and triage.** For every new request, search open and closed issues and this index for duplicates before creating anything. Reuse or update an existing issue whenever it already covers the requested outcome. New issues must use one of the forms in `.github/ISSUE_TEMPLATE/` and include a clear outcome, bounded scope, non-goals, testable acceptance criteria, verification plan and dependencies. Only then add a concise entry here.

Priority guide: P0 = core/release blocker or data/security risk; P1 = important reliability or planned release capability; P2 = normal improvement; P3 = optional/low urgency. Priority does not guarantee scheduling or inclusion in a release. Set a target release/milestone only after triage.

Lifecycle: Needs triage → Ready → Active → Review → Done; Blocked and Deferred are explicit side states. An issue is not Ready just because it is open. One active owner per issue. Owner, exact subtask, permitted files, branch and dependencies must be recorded in `STATUS.md` before editing. Ploy only works on the assignment in `AGENT_WORK.md`; Chaty owns issue creation, triage and central tracking.

Close only when every acceptance criterion is met and evidence is recorded. If an integrated task commit only partially addresses an issue, keep it open and record the remaining work. Record blockers and decisions on the GitHub issue as well as the repository status. Never claim physical-device verification from CI alone.

---

## Issue 4 — P0.1 — Improve build reproducibility and updater test coverage

**Current audit (2026-11-25):** The Gradle Wrapper is pinned to Gradle 9.7.1 with a distribution SHA-256. Signed CI run [#310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45508671565) passed release build/tests, signer and ARM64-only ABI checks for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`; this does not verify current beta HEAD. PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Exact-head PR run [#329](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518210756) passed unsigned release build and unit tests for `3a1396f7364be1e6aa658b3eb67be41549a906fb`; it does not verify signed ARM64 packaging. Keep the issue open for remaining updater edge cases and physical update installation; signed CI is verified by run #330.

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

**Beta audit update (2026-11-25):** Run #310 verified `assembleRelease`, `testDebugUnitTest`, the established signing certificate and ARM64-only packaging for commit `91e21d4bde0a26b8d7cbfd9697615f32c2f57d21`; it does not verify current beta HEAD. Chaty's PR #26 adds full-APK fallback after delta failure and updater policy tests. PR run #329 passed the unsigned release build and unit tests for the final PR head. Keep #4 open until post-merge signed CI and physical update installation are verified.

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
- Scope may move to a later release if acceptance criteria or required physical-device verification are not complete. Do not ship partial implementations.

---

## Issue 5 — P0.2 — Verify LastHaven gameplay and update behavior on a physical arm64 device

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/5
**State:** open

## Goal
Verify the actual launcher experience on physical Android hardware before treating the core as release-ready.

## Device verification checklist
- [ ] Install and cold-start the release APK on a physical arm64 device.
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
- Scope may move to a later release if acceptance criteria or required physical-device verification are not complete. Do not ship partial implementations.

---

## Issue 6 — P2 — Add minimal launcher settings and safe storage controls

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/6
**State:** open

## Goal
Add essential launcher settings and safe storage visibility after the core game flow is verified on a physical arm64 device. The current settings popup is usage-focused and incomplete; the next pass prioritizes working functionality, clarity and task flow rather than visual redesign.

## Scope
- [ ] Editable game URL, defaulting to https://lasthavens.com/play.
  - Validate that the value is an absolute HTTPS URL with a non-empty host; reject unsupported schemes and malformed input with an inline explanation.
  - Keep the currently saved URL if validation fails; use the default only when no valid saved value exists.
  - Apply a valid change on explicit Save/Apply by navigating the game surface to the new URL; do not silently reload while the user is typing or merely opening settings.
  - Keep the chosen URL across app restarts.
- [ ] Fullscreen and orientation controls, only where useful and verified.
- [ ] Show app version and GeckoView/Chromium engine version.
  - Verify the pinned GeckoView SDK exposes a supported public version API before implementation; do not infer or hardcode a runtime version.
- [x] Show installed APK size and game/browser profile storage usage, separating cache from persistent game data where Android/GeckoView expose reliable figures (initial implementation; physical-device path validation pending).
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
- Physical-device verification is a release/verification gate, not a blocker to developing and unit-testing settings functionality; keep device checks pending until performed.
- Do not implement cache clearing or game-data clearing until the pinned GeckoView API and lifecycle requirements are verified.

## Constraints
- Keep the UI minimal and game-focused.
- Do not clear cookies, IndexedDB, LocalStorage, Cache Storage, service workers or other game profile data as part of cache clearing.
- Never clear browser data during startup, update, reload or recovery.
- Do not report inaccessible storage components as exact measurements.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Essential settings work on a physical arm64 device; URL validation and explicit apply behavior are tested; storage figures are clearly defined and accurate within Android/GeckoView API limits; cache clearing is limited to verified cache APIs; destructive game-data clearing is separate, confirmed and backed by a verified GeckoView API.

## Roadmap
- **Target:** v1.1.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required physical-device verification are not complete. Do not ship partial implementations.

---

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
- Complete [#5 physical-device gameplay verification](https://github.com/iNeonyne/Lasthavens-Android/issues/5) and establish a working release build first.

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
- Scope may move to a later release if acceptance criteria or required physical-device verification are not complete. Do not ship partial implementations.

---

## Issue 8 — Superseded — Legacy addon manager removed; future scope waits for owner spec (#23)

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/8  
**State:** closed as not planned; historical context only. Do not restore the removed implementation.

The previous ZIP addon manager was removed from beta at the owner's request on 2026-11-25. Its former implementation checklist is obsolete. Do not restore it or continue the former game-injection approach. A new design will be written only after the owner provides a replacement specification; see #23.

---
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
- [ ] Verify no interference with Android system navigation/gestures on physical devices.
- [ ] Verify all menu actions on a physical arm64 device.
- [x] Keep the menu compact and usable in the existing fullscreen/game layout.

## Acceptance criteria
Attempting to leave the app opens the menu; all four actions work as described; controls expose meaningful accessible names; returning to the game preserves the current session and browser state. Physical-device verification remains pending.

## Roadmap
- **Target:** v1.2.0 — remaining accessibility and physical-device verification.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

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
- [ ] Verify readings on a physical arm64 device and confirm unsupported counters display clearly.

## Constraints
- No analytics, telemetry upload or third-party tracking.
- Do not claim per-game precision if Android only exposes per-app counters.
- Preserve the existing manual, user-confirmed update/download flow.
- Document limitations in the UI and developer notes.

## Acceptance criteria
Users can inspect trustworthy sent/received app totals, understand what is included, and refresh/reset only locally stored session statistics if supported. CI and device verification remain pending.

## Roadmap
- **Target:** v1.0.0 verification gate.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required physical-device verification are not complete. Do not ship partial implementations.

---

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

## Acceptance criteria
- Automated tests cover interruption, truncated/corrupt files, checksum mismatch, signature mismatch, delta failure/full fallback, and storage errors.
- No unverified APK is offered for installation.
- No automatic installation or silent replacement; Android's user-confirmed installer flow is retained.
- Recovery behavior is documented and tested before release.

## Roadmap
- **Target:** v1.1.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Move to a later release if acceptance criteria or required device verification are incomplete; do not ship partial implementations.

---

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
- Verify portrait, landscape, rotation, system navigation, and screen timeout behavior on a physical arm64 device.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

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

## Issue 18 — Roadmap — GeckoView migration, home screen and addon manager

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/18  
**State:** open

The next major beta change replaces Cefrium with Mozilla GeckoView and adds a home screen with Play and Settings. The previous addon manager has been removed; a replacement is blocked pending a new owner specification. Current beta source still uses Cefrium.

### Phase 0 — Restore beta baseline
- [ ] #9 Finish the exit-menu accessibility follow-up (Ploy); The previous AddonManager compile blocker was removed with the legacy manager; post-removal CI is still unverified.
- [ ] #4 Restore clean CI and complete updater edge-case tests. Keep application ID, signing identity and arm64-v8a configuration unchanged.

### Phase 1 — GeckoView engine migration
- [ ] #20 Verify stable dependency/version, official APIs, runtime/session lifecycle, persistent profile strategy, delegates, Java compatibility, ARM64 packaging, APK size and LastHaven requirements. Deliver `docs/GECKOVIEW_MIGRATION.md`.
- [ ] #21 Remove Cefrium and replace it with one pinned stable GeckoView engine. Do not add System WebView or a second/fallback engine.
- [ ] Keep the old Cefrium profile untouched until a safe cleanup/preservation policy is verified. Do not assume its format is portable to GeckoView; re-login may be necessary.
- [ ] Validate WebGL/WebGL2, WebAssembly, WebSockets, touch, fullscreen, audio, permissions, cookies, LocalStorage, IndexedDB, service workers, lifecycle/crash recovery and same-key updates on physical ARM64 hardware.

### Phase 2 — Home screen
- [ ] #22 Launch into a simple home screen with a Play button and Settings entry; do not load the game until Play is selected.
- [ ] Preserve the active Gecko session when navigating between gameplay and home.
- [ ] Apply immersive mode/keep-awake only while gameplay is active; restore normal system UI on home/settings.

### Phase 3 — Addon manager (blocked)
- [ ] #23 Wait for the owner to provide a new written addon-manager specification.
- [ ] Design and review the replacement before implementation. The previous ZIP manager code, tests and UI have been removed.
- [ ] Do not recreate the previous storage model, package manager or injection hooks without explicit direction.

### Phase 4 — Regression and release gates
- [ ] #5 Physical ARM64 gameplay and same-key update verification on GeckoView.
- [ ] Complete applicable settings (#6), diagnostics (#11/#12), navigation/accessibility (#9), orientation (#16), updater recovery (#15), backup (#17) and performance (#7) checks.
- [ ] Clean CI on the exact candidate commit: build, tests, signed APK, package identity, signing certificate, ARM64-only ABI, artifact count and updater manifest/checksum.
- [ ] Record exact build, commit, CI run, APK digest, device and test outcomes in STATUS.md.

### Stable promotion policy
- `beta` is the active development/integration/testing branch; `main` is stable.
- Task changes enter `beta` through PRs. Chaty alone reviews and merges; no contributor may push directly to `beta`.
- Promote only after every applicable gate passes and the user explicitly approves. Publishing/tagging requires separate explicit approval.
- No debug APKs, partial features, automatic update downloads/installs, app ID changes or signing-key changes.

---
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

## Issue 20 — P0 — Verify GeckoView migration decision and stable artifact

**Current audit (2026-11-25):** GeckoView 160 ARM64 artifact/API research is recorded in `docs/GECKOVIEW_MIGRATION.md`. The engine is now integrated in beta. Remaining build/runtime and physical-device checks are tracked under #21 and #5; do not describe the research decision alone as proof of game compatibility.


**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/20  
**Internal owner:** Ploy, queued after #9 and #11.

Docs-only research. Verify current stable GeckoView coordinates/version through Mozilla's official Maven repository and official Javadocs; document Java compatibility, runtime/session/view lifecycle, persistent profile behavior, required delegates, ARM64/native packaging, APK size, game Web APIs and the Cefrium profile transition risk. Deliver `docs/GECKOVIEW_MIGRATION.md`. Do not change source/build files. WoW addon ZIP packages are not Gecko WebExtensions.

---

## Issue 21 — P0 — Replace Cefrium with Mozilla GeckoView

**Current audit (2026-11-25):** The beta source now uses GeckoView 160 and no Cefrium production integration. CI run #310 passed the signed release build, unit-test task, signing-certificate and ARM64-only ABI checks for its tested commit. Physical ARM64 gameplay, Gecko profile persistence, permissions/media and update behavior remain unverified. Do not close this issue based on CI alone.


**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/21  
**Internal owner:** Chaty, queued after #4 is clean and #20 is reviewed.

Replace Cefrium plugin/dependencies/repository/runtime with one verified stable GeckoView release. Implement one process-scoped runtime, managed session/view, safe public delegates and a persistent browser profile. Remove Cefrium source/build references; no dual engine. Preserve app ID, release signing identity, ARM64-only ABI and manual updater flow. Keep existing Cefrium profile files untouched; do not claim portable profile migration. Test build/unit/release verification and physical-device requirements separately.

---

## Issue 22 — P1 — Add the LastHavenPlus home screen with Play and Settings

**Current assignment:** Chaty owns the home screen and navigation implementation in [PR #26](https://github.com/iNeonyne/LastHavenPlus/pull/26). PR run #317 passed the code build/unit tests; merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Ploy has no active assignment on this issue. Required destinations are app home, settings and the existing game; the game menu must provide Reload, Home and Return to game. Home returns to the launcher home screen, and Return to game must preserve the existing GeckoSession.


**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/22  
**Internal owner:** Chaty, active. PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`.

Fresh launch shows a simple branded home screen with Play and Settings. Do not load the game until Play is selected. Settings must be usable without starting a browser session. Preserve the Gecko session when navigating home and make immersive/keep-awake behavior gameplay-only. Define and test Back behavior.

---

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

Preserve archive path/CRC/duplicate/count/expanded-size checks. Keep Android 10/API 29+, ARM64-only, app ID and signing identity. Do not modify the game client or claim Lua initialization from injection-layer requests alone. See the GitHub issue for full scope and acceptance criteria.
---
---

**Integration checkpoint (2026-11-25):** PR #26 merged to beta at `e924c580699ad34f1083d1a9a0fe96901037c0ae`. PR run #329 passed unsigned release build and unit tests for the exact PR head. Signed post-merge ARM64 CI and physical device verification remain open. `main` and `beta` are diverged (beta ahead 166, main ahead 9); review the nine main-only commits before any promotion.


**Post-merge CI (2026-11-25):** Run [#330](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45518704468) passed signed optimized release build, unit tests, established certificate verification and ARM64-only packaging for `e924c580699ad34f1083d1a9a0fe96901037c0ae`. Artifact upload succeeded. Physical updater installation and device gameplay remain open.


---

## Issue 28 — P2 — Make storage usage reporting accurate and robust

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/28  
**State:** open; queued. Audit the actual app-private directories represented by storage totals, clarify omissions, and replace recursive directory traversal with iterative traversal. Keep APK, persistent data and cache measurements separate.

---

## Issue 29 — P1 — Reconcile beta documentation with current source and CI evidence

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/29  
**State:** open; documentation candidate on `chaty/docs-reconciliation-v2.1`. Reconcile README, STATUS, ISSUES and release-facing claims against PR #43, exact commit CI evidence and outstanding physical-device gates. Keep open until the docs PR is merged and verified.


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
- Add `.github/workflows/quality.yml` for relevant source/build changes on PRs to beta and pushes to beta.
- Run `lintDebug` and a pinned Spotless/ktfmt `spotlessCheck`.
- Use the existing JDK 25, Android SDK 37.2 / Build Tools 37.0.0 and committed Gradle Wrapper.
- Apply formatting incrementally against the PR base or previous beta commit so legacy formatting is not rewritten in a giant unrelated diff.
- Keep workflow permissions read-only; do not expose signing secrets or upload/publish APKs.
- Upload Android Lint reports. Existing Android PR validation remains responsible for unsigned build, JVM unit tests, Python updater-helper tests and their reports.
- Ignore documentation-only edits.

### Non-goals
- No application behavior, GeckoView, ABI, signing, updater or release-policy changes.
- No changes to main. No release, tag or publication.
- No broad formatting rewrite of legacy Kotlin files.

### Acceptance criteria
- Exact-head PR checks pass `lintDebug` and `spotlessCheck`.
- A deliberately misformatted changed Kotlin file fails the formatting check; the formatter command is documented.
- Android PR validation continues to run the unsigned build, JVM tests and Python helper tests.
- Quality job uses read-only permissions and no release secrets.
- Documentation accurately describes which workflow runs which checks.

### Verification
Record exact branch head, Actions run URLs, lint results, formatter result and any limitations here and in STATUS.md. Do not mark the issue complete until exact-head CI proves the checks.

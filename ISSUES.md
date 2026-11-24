# Project issues and task tracker — LastHavenPlus

**Repository backlog:** this file is the consolidated, version-controlled task tracker. Update its checklists and status in the same commit as meaningful implementation work.
**Discussion and history:** each task links to its GitHub Issue. GitHub Issues remain useful for discussion and external notifications, but TickTick is no longer part of this project's workflow.

## Backlog overview

| ID | Priority / title | State |
|---|---|---|
| [#4](#issue-4) | P0.1 — Improve build reproducibility and updater test coverage | open |
| [#5](#issue-5) | P0.2 — Verify LastHaven gameplay and update behavior on a physical arm64 device | open |
| [#6](#issue-6) | P2 — Add minimal launcher settings and safe storage controls | open |
| [#7](#issue-7) | P3 — Measure runtime performance and evaluate APK size improvements | open |
| [#8](#issue-8) | P1 — Build the local ZIP addon manager for v1.1.0 (game injection gated) | open |
| [#9](#issue-9) | P2 — Add a compact game navigation and exit menu | open |
| [#10](#issue-10) | P2 — Track game network data usage | open |
| [#11](#issue-11) | P2 — Add privacy-conscious app and updater logs | open |
| [#12](#issue-12) | P2 — Surface actionable Cefrium load-error details | open |
| [#13](#issue-13) | P2 — Add accessible names to launcher actions | open |
| [#14](#issue-14) | P2 — Add connection diagnostics | open |
| [#15](#issue-15) | P2 — Make updater recovery safer | open |
| [#16](#issue-16) | P2 — Add gameplay orientation and keep-awake settings | open |
| [#17](#issue-17) | P2 — Back up and restore launcher settings | open |
| [#18](#issue-18) | Roadmap — v1.0.0 released, v1.1.0 addon manager, v1.2.0 improvements | open |
| [#19](https://github.com/iNeonyne/LastHavenPlus/issues/19) | P1 — Add an opt-in beta release channel to the updater | open |

---

## How new work is added

GitHub Issues are the canonical records for each task's discussion, owner, dependencies and lifecycle. This file is the project-wide index and roadmap checklist. The GitHub issue number is the only task ID; do not invent a parallel numbering scheme.

**Chaty handles intake and triage.** For every new request, search open and closed issues and this index for duplicates before creating anything. Reuse or update an existing issue whenever it already covers the requested outcome. New issues must use one of the forms in `.github/ISSUE_TEMPLATE/` and include a clear outcome, bounded scope, non-goals, testable acceptance criteria, verification plan and dependencies. Only then add a concise entry here.

Priority guide: P0 = core/release blocker or data/security risk; P1 = important reliability or planned release capability; P2 = normal improvement; P3 = optional/low urgency. Priority does not guarantee scheduling or inclusion in a release. Set a target release/milestone only after triage.

Lifecycle: Needs triage → Ready → Active → Review → Done; Blocked and Deferred are explicit side states. An issue is not Ready just because it is open. One active owner per issue. Owner, exact subtask, permitted files, branch and dependencies must be recorded in `STATUS.md` before editing. Ploy only works on the assignment in `AGENT_WORK.md`; Chaty owns issue creation, triage and central tracking.

Close only when every acceptance criterion is met and evidence is recorded. If a PR only partially addresses an issue, reference it without auto-closing. Record blockers and decisions on the GitHub issue as well as the repository status. Never claim physical-device verification from CI alone.

---

## Issue 4 — P0.1 — Improve build reproducibility and updater test coverage

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/4
**State:** open

## Goal
Harden the existing build and updater without replacing the current update architecture.

## Build reliability
- [ ] Add the Gradle Wrapper (wrapper scripts, wrapper JAR, and pinned Gradle 9.7.1 distribution URL).
- [ ] Pin and verify the official Gradle 9.7.1 distribution SHA-256; do not invent or copy an unverified checksum.
- [ ] Ensure the wrapper JAR is the official Gradle wrapper JAR and commit executable permissions for `gradlew`.
- [ ] Update GitHub Actions to build and test with `./gradlew` rather than installing a separate Gradle distribution.
- [ ] Keep the existing JDK, AGP, SDK, Cefrium, signing key, application ID, and arm64-only configuration unless a verified compatibility issue requires a change.
- [x] Verify debug and signed release builds in CI (run #85).

## Updater and differential-patch tests

**Chaty progress (2026-11-24):** Added three BSDIFF failure-path tests for truncated headers, unsupported patch magic and output-size-limit enforcement. CI run for commit `29dd5aa` is still running; these tests are not marked complete until the run passes. The prior CI run exposed three pre-existing `AddonArchiveInspectorTest` failures that also need diagnosis because they block the shared unit-test task.
- [ ] Add tests for malformed or unsupported update manifests.
- [ ] Test invalid release version tags and version-code handling.
- [ ] Test incorrect APK and patch SHA-256 values.
- [ ] Test mismatched base APK version, version code, and base digest.
- [ ] Test corrupted, truncated, and incompatible BSDIFF patches.
- [ ] Test interrupted/failed downloads and ensure partial files cannot be installed.
- [ ] Preserve the existing manual, user-confirmed download/install flow.
- [ ] Keep the full APK fallback when a differential patch is unavailable or fails validation.

## CI verification
- [ ] Verify release APK package identity in CI.
- [x] Verify arm64-v8a-only native libraries and established signing certificate in CI (run #85).
- [ ] Publish APK size information and checksum results in the workflow summary.

## Constraints
- Do not create a second updater.
- Do not change the application ID or release signing key.
- Do not add background downloads or automatic installation.
- Follow AGENTS.md: claim work in STATUS, document meaningful changes in STATUS and CHANGELOG-AGENTS.md, and integrate directly into main through the main orchestrator.

## Acceptance criteria
A clean checkout can build with the committed wrapper; updater edge cases are covered by automated tests; CI verifies the signed arm64 release and reports artifact sizes.

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
- [ ] Confirm Cefrium starts and loads https://alt.lasthavens.com/play.
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
- Keep Cefrium as the sole browser engine.
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
- [ ] Editable game URL, defaulting to https://alt.lasthavens.com/play.
  - Validate that the value is an absolute HTTPS URL with a non-empty host; reject unsupported schemes and malformed input with an inline explanation.
  - Keep the currently saved URL if validation fails; use the default only when no valid saved value exists.
  - Apply a valid change on explicit Save/Apply by navigating the game surface to the new URL; do not silently reload while the user is typing or merely opening settings.
  - Keep the chosen URL across app restarts.
- [ ] Fullscreen and orientation controls, only where useful and verified.
- [ ] Show app version and Cefrium/Chromium engine version.
  - Verify the pinned Cefrium SDK exposes a supported public version API before implementation; do not infer or hardcode a runtime version.
- [x] Show installed APK size and game/browser profile storage usage, separating cache from persistent game data where Android/Cefrium expose reliable figures (initial implementation; physical-device path validation pending).
- [x] Add a refresh action and clearly label estimates or unavailable breakdowns.
- [ ] Inspect the pinned Cefrium API for a safe HTTP-cache and code-cache-only clear operation.
- [ ] If both cache types can be cleared safely, add a clearly named Clear game cache action.
- [ ] Add a separate Clear game data destructive action with explicit confirmation; never combine it with cache clearing.
  - Verify the exact pinned Cefrium profile API and lifecycle requirements from official documentation/source before coding; do not guess method names.
  - Explain that this removes saved game login/session and site data; require a second explicit confirmation and ensure the game surface/profile is safely detached before deletion.
  - If no supported safe deletion API exists, leave the action unimplemented and document the limitation rather than deleting profile files by guesswork.
- [ ] Verify settings and storage reporting survive app restarts and do not disrupt the active game session unexpectedly.

## Dependencies
- The navigation/menu entry point from #9 is already implemented.
- Physical-device verification is a release/verification gate, not a blocker to developing and unit-testing settings functionality; keep device checks pending until performed.
- Do not implement cache clearing or game-data clearing until the pinned Cefrium API and lifecycle requirements are verified.

## Constraints
- Keep the UI minimal and game-focused.
- Do not clear cookies, IndexedDB, LocalStorage, Cache Storage, service workers or other game profile data as part of cache clearing.
- Never clear browser data during startup, update, reload or recovery.
- Do not report inaccessible storage components as exact measurements.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Essential settings work on a physical arm64 device; URL validation and explicit apply behavior are tested; storage figures are clearly defined and accurate within Android/Cefrium API limits; cache clearing is limited to verified cache APIs; destructive game-data clearing is separate, confirmed and backed by a verified Cefrium API.

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
- [ ] Record device model, Android version, app build type and Cefrium version.
- [ ] Measure cold start to first paint.
- [ ] Measure frame pacing/FPS during a repeatable in-game scene.
- [ ] Record process memory/PSS during startup and gameplay.
- [ ] Record rendering/input latency where measurable.
- [ ] Record exact APK file size and DEX/native-library breakdown.
- [ ] Compare the same test conditions before and after any proposed change.

## Optimization candidates
- [ ] Review release R8 output and mapping/usage reports.
- [ ] Identify app-owned DEX/code that can be safely reduced.
- [ ] Evaluate whether any unchanged assets can be delivered outside the base APK without breaking Cefrium startup or offline/update assumptions.
- [ ] Keep differential APK updates as the preferred way to reduce repeat download size when the engine payload is unchanged.

## Dependencies
- Complete [#5 physical-device gameplay verification](https://github.com/iNeonyne/Lasthavens-Android/issues/5) and establish a working release build first.

## Constraints
- No speculative Chromium flags, native-library stripping, asset deletion or engine replacement.
- Do not compromise Cefrium compatibility, game storage, package identity or the established signing key.
- Keep arm64-v8a only.
- Record before/after evidence in STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
A repeatable baseline is recorded, and every accepted optimization has measured before/after results with gameplay and persistence still passing.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Scope may move to a later release if acceptance criteria or required physical-device verification are not complete. Do not ship partial implementations.

---

## Issue 8 — P1 — Build the local ZIP addon manager for v1.1.0 (game injection gated)

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/8
**State:** open

## Status
Scheduled for **v1.1.0**. The project owner confirms they have explicit permission from the LastHaven operator for custom addon integration. We are proceeding with the local ZIP manager first. The exact authorized integration surface/API and technical contract still need to be recorded before touching the game runtime.

**Implementation gate:** Do not assume the supplied MAIN-world XHR/Worker/OPFS injection pipeline is the authorized method. Obtain the operator's exact scope and supported integration contract, then validate against that contract. Cefrium document-start scripts and request interception are public APIs, but they do not establish game compatibility by themselves.

## v1.1.0 — local ZIP manager
- [ ] Import ZIPs through Android's system file picker.
- [ ] Securely inspect ZIP central-directory records before extraction; reject traversal, absolute paths, symlinks, encrypted/unsupported methods, ZIP64, duplicate case-insensitive paths, CRC mismatches, oversized archives/files and malformed archives.
- [ ] Detect WoW 3.3.5a addon roots and parse .toc title/version/interface/dependencies where applicable.
- [ ] Store imported addon packages and metadata locally, separate from Cefrium profile data.
- [ ] Show imported addons, metadata, file sizes, compatibility warnings and missing dependencies.
- [ ] Allow enable/disable, export and remove in the local library. Clearly label local enablement until game integration is implemented and confirmed.
- [ ] Preserve user data and implement safe replacement/rollback.
- [ ] Add tests for ZIP validation, metadata parsing, duplicate imports, storage limits, export round-trip and failed writes.

## Game integration — authorization confirmed; contract pending
- [x] User confirms they have explicit permission from the LastHaven operator.
- [ ] Record the operator's exact authorized scope and supported addon-loading API/integration contract.
- [ ] Only then evaluate the approved Cefrium integration method against the live client. Do not reverse-engineer or patch protected game assets, OPFS indexes, worker/WASM imports or runtime internals outside the operator-approved scope.
- [ ] Verify gameplay and recovery on a physical arm64 device before enabling integration.

## Explicitly out of scope for v1.1.0
- Online addon catalog/store, repository browsing or automatic addon discovery.
- Automatic addon downloads or updates.
- Any game-runtime modification outside the operator-approved scope.

## Constraints
- Do not recreate, copy, decompile, reverse-engineer or modify the game client outside the operator-approved scope.
- Preserve the Cefrium profile and all unrelated game data.
- All imports and destructive actions require explicit user confirmation.
- Do not present locally stored addons as game-enabled unless the client confirms they loaded.

## Acceptance criteria
The local ZIP manager passes its archive/security/storage tests. Any game integration is limited to the operator's confirmed scope, uses a documented/approved integration contract, and passes physical arm64 testing. If that contract cannot be established, ship the local ZIP library only with its limitations clearly disclosed.

## Roadmap
- **Target:** v1.1.0 — local ZIP manager; approved game integration after contract review.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18

## Implementation progress — 2026-11-24

Initial code is committed on `main`: ZIP inspection/CRC/path/security checks, WoW .toc parsing, local package/index storage, replacement confirmation, local enabled flags, export/remove UI and exit-menu entry. JVM tests have been added. CI has not yet completed successfully, and this feature is not release-ready. Physical-device verification and the operator-approved runtime integration contract remain outstanding.

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
- [x] Capture app lifecycle, Cefrium startup/navigation failures and renderer termination events.
- [x] Capture updater checks and download/validation outcomes.
- [x] Add an in-app log viewer.
- [x] Add an explicit share action for diagnostic logs.
- [x] Bound log storage with one rotating previous log.
- [x] Redact credentials, cookies, authorization headers, tokens, session identifiers and sensitive URL parameters in common key/value formats.
- [ ] Add CI tests for redaction, rotation/size limits and failure-path logging; a redaction unit test has been added but CI has not yet been verified.
- [ ] Include the Cefrium/Chromium runtime version in startup diagnostics only if the pinned SDK exposes a supported public version API; otherwise record it as unavailable rather than guessing.
- [x] Keep logs local; do not upload diagnostics automatically.

## Constraints
- Never log passwords, cookies, tokens, private keys, full authorization headers or game session secrets.
- Avoid excessive logging in normal gameplay.
- Do not change Cefrium/game storage behavior to collect diagnostics.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Users can inspect and explicitly share useful diagnostics without exposing authentication/session secrets; logs remain bounded and local. Remaining CI and runtime-version work is tracked for a later release.

## Roadmap
- **Target:** v1.2.0 — remaining diagnostics tests and version reporting.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

## Issue 12 — P2 — Surface actionable Cefrium load-error details

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/12
**State:** open

## Goal
Make game-load failures easier to diagnose without adding browser-style UI or exposing sensitive information.

## Scope
- [ ] Inspect the exact pinned Cefrium 0.21.0 public API/source for `CefriumLoadError` fields before implementation; do not guess property names.
- [ ] Map supported error codes into a small set of user-friendly categories (for example DNS/network unavailable, TLS/certificate failure, server response/load failure, and unknown).
- [ ] Keep the existing Retry action and concise failure screen.
- [ ] Show a safe, simplified category and optional numeric/error code; do not display raw URLs, headers, cookies, tokens, or unfiltered engine messages.
- [ ] Log the underlying safe diagnostic fields through the existing redacting diagnostics path.
- [ ] Provide a generic fallback when Cefrium supplies no useful structured detail.
- [ ] Add tests for category mapping and fallback behavior.

## Constraints
- Keep the game as the primary app surface; no address bar, browser controls, or fallback browser engine.
- Do not expose credentials, session identifiers, or sensitive URL parameters.
- Follow AGENTS.md and update STATUS.md and CHANGELOG-AGENTS.md.

## Acceptance criteria
Common load failures are distinguishable to users/support using verified Cefrium API fields; unknown errors remain safely handled; Retry behavior is unchanged; tests cover mappings and fallback.

## Roadmap
- **Target:** v1.2.0.
- **Roadmap:** https://github.com/iNeonyne/LastHavenPlus/issues/18
- Do not ship partial implementations.

---

## Issue 13 — P2 — Add accessible names to launcher actions

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/13
**State:** open

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
- Never export credentials, cookies, WebView/Cefrium profile data, game cache, tokens, or private game data.
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

## Issue 18 — Roadmap — stable v1.0.0, beta development and controlled main promotion

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/18
**State:** open

# LastHavenPlus release roadmap

This roadmap defines what belongs in shared `beta` and when work may be promoted to stable `main`. A commit or successful build alone does not make a release.

## Branch model

- **`main` — stable release branch.** Contains approved stable releases and explicitly approved hotfixes only. No routine feature development.
- **`beta` — integration and release-candidate branch.** Chaty pushes orchestrator changes here. Ploy contributes through reviewed PRs targeting `beta`.
- Ploy creates task branches from the latest `beta`; each PR targets `beta`. Chaty reviews the full diff and may merge accepted PRs into `beta`.
- Features must be complete, tested and documented before being considered release-ready.
- No automatic merges, tags, releases or beta-to-main promotion.

## v1.0.0 — Stable release already published

**Status: Published 2026-11-24**

- Canonical assets: `lasthavenplus-arm64.apk` and `lasthavenplus-update.json`.
- Do not rewrite this release. Follow-up work belongs in beta and a later stable version.

## Next stable release — v1.1.0

**Development branch:** `beta`  
**Promotion target:** `main`, only after the release gate below passes.

Planned scope:
- [ ] #4 Build reliability and updater tests: reproducible Gradle Wrapper, checksum verification, CI and updater failure/recovery coverage.
- [ ] #8 Complete local ZIP addon manager: secure archive validation, import/storage/replacement rollback/export tests and documented limitations. No unfinished game integration.
- [ ] #6 Complete essential settings/storage behavior supported by verified Cefrium APIs; preserve the persistent game profile.
- [ ] #15 Complete updater recovery: staged downloads, checksum/signature validation, delta-to-full fallback and safe failure cleanup. Installation remains user-confirmed.
- [ ] #11 Complete diagnostics privacy/redaction/rotation test coverage.
- [ ] #13 Complete accessibility semantics; physical TalkBack verification must not be claimed from CI.
- [ ] Run regression checks for game launch, persistence, updater and addon flows on physical ARM64 hardware.
- [ ] Defer any feature that cannot meet acceptance criteria; do not ship partial implementations.

## Later beta backlog — candidates for v1.2.0 or later

- [ ] #12 Safe, actionable Cefrium load-error categories using verified public API fields.
- [ ] #14 On-demand DNS/HTTPS diagnostics; do not imply this proves game-server or gameplay health.
- [ ] #16 Orientation and keep-awake preferences with lifecycle cleanup.
- [ ] #17 Versioned backup/restore of allowlisted non-sensitive launcher settings only.
- [ ] #7 Reproducible performance/APK-size baselines; optimize only from measurements.
- [ ] Finish remaining #9 navigation/accessibility and physical-device verification.
- [ ] Review user feedback and beta regressions before committing to stable scope.

## When do we release from main?

Promote a specific beta commit to `main` only when all applicable gates are satisfied:

1. **Scope freeze:** release checklist complete; unfinished features removed or deferred; no unresolved critical/high-severity issue.
2. **CI:** required build, unit tests, lint/static checks where configured, and release workflow checks pass on the exact candidate commit.
3. **Artifact integrity:** one canonical signed ARM64 APK; package ID, signing certificate, version metadata, ABI contents and updater manifest/checksums verified. Never publish a debug APK.
4. **Regression:** game launch/navigation, persistent Cefrium profile, manual updater and included addon flows tested. No user data is cleared or reset.
5. **Physical-device gate:** applicable gameplay, persistence, install/update and recovery checks pass on a real ARM64 Android device. If a device-only requirement cannot be tested, explicitly defer it from release rather than mark it passed.
6. **Beta soak:** candidate exercised long enough to catch obvious regressions; record exact commit, build, results and known limitations in STATUS.
7. **Owner approval:** Chaty presents the candidate commit, artifacts, evidence and risks. **The user explicitly approves promotion** before beta is merged/promoted into main.
8. **Publish separately:** after promotion, create stable tag/release and publish only the verified canonical artifact, with matching changelog. Publishing also requires explicit user authorization; promotion alone does not publish.

There is no fixed calendar release date. **The next stable main release happens when these gates pass and the user approves it**, not merely when a version number or feature list is reached.

## Branch and release policy

- arm64-v8a only; preserve application ID and established signing key.
- Manual, user-confirmed update installation; no background or silent installation.
- Preserve existing game and Cefrium profile data across updates.
- Addon ZIPs must be validated before installation; failed/interrupted operations must leave existing addon/game state recoverable.
- Every stable release must pass applicable CI, signed artifact/package checks, one-APK asset verification and physical-device tests.
- If a feature cannot be completed and verified, defer it; never ship a half-implemented feature.

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

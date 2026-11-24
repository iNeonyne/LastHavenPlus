# Project issues and task tracker — LastHavenPlus

**Repository backlog:** this file is the consolidated, version-controlled task tracker. Update its checklists and status in the same commit as meaningful implementation work.
**Source of truth:** GitHub Issues and this version-controlled `ISSUES.md` backlog are the project task records. Keep their scope, state and acceptance criteria aligned.

## Backlog overview

| ID | Priority / title | State |
|---|---|---|
| [#4](#issue-4) | P0.1 — Improve build reproducibility and updater test coverage | open |
| [#5](#issue-5) | P0.2 — Verify LastHaven gameplay and update behavior on a physical arm64 device | open |
| [#6](#issue-6) | P2 — Add minimal launcher settings and safe storage controls | open |
| [#7](#issue-7) | P3 — Measure runtime performance and evaluate APK size improvements | open |
| [#8](#issue-8) | Superseded — legacy addon manager removed; future scope waits for owner spec (#23) | open |
| [#9](#issue-9) | P2 — Add a compact game navigation and exit menu | open |
| [#10](#issue-10) | P2 — Track game network data usage | open |
| [#11](#issue-11) | P2 — Add privacy-conscious app and updater logs | open |
| [#12](#issue-12) | P2 — Surface actionable GeckoView load-error details | open |
| [#13](#issue-13) | Superseded by #9 — old AddonManager compile/accessibility task; pending CI confirmation | open |
| [#14](#issue-14) | P2 — Add connection diagnostics | open |
| [#15](#issue-15) | P2 — Make updater recovery safer | open |
| [#16](#issue-16) | P2 — Add gameplay orientation and keep-awake settings | open |
| [#17](#issue-17) | P2 — Back up and restore launcher settings | open |
| [#18](#issue-18) | Roadmap — GeckoView migration, home screen and addon manager | open |
| [#19](https://github.com/iNeonyne/LastHavenPlus/issues/19) | P1 — Add an opt-in beta release channel to the updater | open |
| [#20](https://github.com/iNeonyne/LastHavenPlus/issues/20) | P1 — Prepare the GeckoView migration technical decision record | open |
| [#21](https://github.com/iNeonyne/LastHavenPlus/issues/21) | P1 — Replace Cefrium with Mozilla GeckoView | open |
| [#22](https://github.com/iNeonyne/LastHavenPlus/issues/22) | P1 — Add the home screen with Play and Settings | open |
| [#23](https://github.com/iNeonyne/LastHavenPlus/issues/23) | P1 — Rebuild addon manager after owner specification | open |

---

## How new work is added

GitHub Issues are the canonical records for each task's discussion, owner, dependencies and lifecycle. This file is the project-wide index and roadmap checklist. The GitHub issue number is the only task ID; do not invent a parallel numbering scheme.

**Chaty handles intake and triage.** For every new request, search open and closed issues and this index for duplicates before creating anything. Reuse or update an existing issue whenever it already covers the requested outcome. New issues must use one of the forms in `.github/ISSUE_TEMPLATE/` and include a clear outcome, bounded scope, non-goals, testable acceptance criteria, verification plan and dependencies. Only then add a concise entry here.

Priority guide: P0 = core/release blocker or data/security risk; P1 = important reliability or planned release capability; P2 = normal improvement; P3 = optional/low urgency. Priority does not guarantee scheduling or inclusion in a release. Set a target release/milestone only after triage.

Lifecycle: Needs triage → Ready → Active → Review → Done; Blocked and Deferred are explicit side states. An issue is not Ready just because it is open. One active owner per issue. Owner, exact subtask, permitted files, branch and dependencies must be recorded in `STATUS.md` before editing. Ploy only works on the assignment in `AGENT_WORK.md`; Chaty owns issue creation, triage and central tracking.

Close only when every acceptance criterion is met and evidence is recorded. If an integrated task commit only partially addresses an issue, keep it open and record the remaining work. Record blockers and decisions on the GitHub issue as well as the repository status. Never claim physical-device verification from CI alone.

---

## Issue 4 — P0.1 — Improve build reproducibility and updater test coverage

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/4
**State:** open

## Goal
Harden the existing build and updater without replacing the current update architecture.

## Build reliability
- [x] Add the Gradle Wrapper (wrapper scripts, wrapper JAR, and pinned Gradle 9.7.1 distribution URL) — present on beta.
- [x] Pin the Gradle 9.7.1 distribution SHA-256 in wrapper properties; wrapper validation passed in CI run #247.
- [x] Commit wrapper JAR and executable `gradlew`; CI wrapper validation passed in run #247.
- [x] Update GitHub Actions to invoke `./gradlew`; run #247 reached the Gradle build through the wrapper.
- [ ] Keep the existing JDK, AGP, SDK, Cefrium, signing key, application ID, and arm64-only configuration unless a verified compatibility issue requires a change.
- [ ] Verify current beta debug and signed release builds in CI. Run #232 failed Kotlin compilation before APK/signing/ABI verification; historical run #85 is not current-source evidence.

**Beta audit update (2026-11-25):** The Gradle Wrapper is present and pinned to Gradle 9.7.1 with a distribution SHA-256; wrapper validation passed in run #247. Run #247 failed compilation in the legacy `AddonManagerDialog.kt`, which has since been removed. Post-removal run #268 was cancelled by newer pushes and provided no build/test evidence. Current beta run #278 is pending. Keep #4 open for a clean current-beta build and updater edge-case tests.

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
- [ ] Verify ARM64-only native libraries and established signing certificate for current beta. Run #232 restored the keystore but skipped APK checks.
- [ ] Publish APK size information and checksum results in the workflow summary.

## Constraints
- Do not create a second updater.
- Do not change the application ID or release signing key.
- Do not add background downloads or automatic installation.
- Follow AGENTS.md: claim work in STATUS, document meaningful changes in STATUS and CHANGELOG-AGENTS.md, and integrate directly into beta through Chaty. No pull requests.

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
- [ ] Confirm GeckoView starts and loads https://alt.lasthavens.com/play.
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
- [ ] Editable game URL, defaulting to https://alt.lasthavens.com/play.
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
**State:** open for historical traceability; no active implementation work.

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
- Chaty alone integrates and pushes to beta. No PRs.
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

## Issue 20 — P1 — Prepare the GeckoView migration technical decision record

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/20  
**Internal owner:** Ploy, queued after #9 and #11.

Docs-only research. Verify current stable GeckoView coordinates/version through Mozilla's official Maven repository and official Javadocs; document Java compatibility, runtime/session/view lifecycle, persistent profile behavior, required delegates, ARM64/native packaging, APK size, game Web APIs and the Cefrium profile transition risk. Deliver `docs/GECKOVIEW_MIGRATION.md`. Do not change source/build files. WoW addon ZIP packages are not Gecko WebExtensions.

---

## Issue 21 — P1 — Replace Cefrium with Mozilla GeckoView

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/21  
**Internal owner:** Chaty, queued after #4 is clean and #20 is reviewed.

Replace Cefrium plugin/dependencies/repository/runtime with one verified stable GeckoView release. Implement one process-scoped runtime, managed session/view, safe public delegates and a persistent browser profile. Remove Cefrium source/build references; no dual engine. Preserve app ID, release signing identity, ARM64-only ABI and manual updater flow. Keep existing Cefrium profile files untouched; do not claim portable profile migration. Test build/unit/release verification and physical-device requirements separately.

---

## Issue 22 — P1 — Add the LastHavenPlus home screen with Play and Settings

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/22  
**Internal owner:** Ploy, queued after #21 integration.

Fresh launch shows a simple branded home screen with Play and Settings. Do not load the game until Play is selected. Settings must be usable without starting a browser session. Preserve the Gecko session when navigating home and make immersive/keep-awake behavior gameplay-only. Define and test Back behavior.

---

## Issue 23 — P1 — Rebuild addon manager after owner specification

**GitHub:** https://github.com/iNeonyne/LastHavenPlus/issues/23  
**Internal owner:** Chaty for intake/planning; implementation owner not assigned yet.
**State:** BLOCKED — awaiting the project owner's written requirements.

The previous addon manager implementation has been removed at the owner's request. Do not restore, port or recreate the old ZIP inspector/repository/dialog design based on previous issue text.

### Removed from beta
- `AddonArchiveInspector.kt`
- `AddonLibraryRepository.kt`
- `AddonManagerDialog.kt`
- `AddonArchiveInspectorTest.kt`
- Addon manager entry/state in `MainActivity.kt`

### Next step
Wait for the owner to provide a new addon-manager specification. Then Chaty will translate it into a fresh design, data model, security boundary, implementation stages and acceptance tests before assigning work.

### Constraints until then
- No addon manager code, UI, storage schema, migrations, background work or injection hooks.
- Do not delete unrelated user app data or assume an old package library should be migrated.
- Do not conflate WoW addon packages with Gecko WebExtensions.
- No game injection or runtime modification without a separately documented, explicitly approved integration contract.
- No parallel implementation before the new scope is approved.

---
---
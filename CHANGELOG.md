# LastHavenPlus release changelog

## v2.4.0 — 2026-11-26

### New features
- Add the first phase of the native Addon Manager: user-initiated ZIP import/export, private local library storage, addon details, TOC metadata and required/optional dependency inspection.
- Improve game-menu accessibility with pane and heading semantics, a Compose keyboard/controller focus entry point, and reduced-motion-aware transitions.
- Introduce a shared icy-blue visual theme across launcher-owned screens.

### Bug fixes and reliability
- Remove the silent update-metadata request from app startup; update checks remain user-initiated.
- Protect addon-library changes with staging, transactional updates, rollback and replay-safe crash recovery.

### Small changes
- Replace the launcher artwork with an icy-blue pixel-art castle icon.
- Make typography, buttons, cards, spacing and touch targets more consistent.
- Add restrained menu transitions and haptic feedback for primary actions.

### Compatibility and known limitations
- Android 10/API 29+, ARM64-only (arm64-v8a) and Mozilla GeckoView 160.
- Preserve application ID `com.neonyne.lasthavenlauncher` and the established release signing identity.
- Addon Manager Phase A stores and manages addon files only; it does not activate addons in the game. Runtime injection, XHR/Worker interception, game-resource masking and Lua initialization are not included.
- GeckoView uses a separate browser profile; a fresh game login may be required.
- No physical ARM64 device testing was performed for this release. CI does not establish gameplay, TalkBack, adaptive-icon or document-picker behavior on hardware.

## v2.3.0 — 2026-11-26

### Launcher UI and gameplay recovery
- Unify launcher-owned screens with a shared dark/gold visual system, consistent shapes and spacing, and improved touch-target sizing.
- Add restrained menu transitions and haptic feedback for explicit primary actions.
- Add explicit initial game loading, recoverable page-load failure and renderer/process recovery states.
- Preserve the existing GeckoView session and keep the game itself unchanged.

### Updater reliability
- Stream downloaded APK and patch data through checksum and size verification before accepting the staged file.
- Clean up partial downloads on failure and retain the verified full-APK fallback when differential downloads fail.
- Keep update checks and downloads user-initiated; Android's package installer remains responsible for user confirmation.

### Compatibility and known limitations
- Android 10/API 29+, ARM64-only (arm64-v8a), application ID com.neonyne.lasthavenlauncher, GeckoView 160 and the established release signing identity are preserved.
- Updater progress/speed/ETA, persistent progress presentation and install-later UX are deferred to v2.4.0.
- Physical ARM64 device verification of gameplay, rendering, haptics, profile persistence and real update installation has not been performed. Automated CI is not a substitute for device testing.
- Addon Manager issue #23 remains on hold and is not included.

## v2.1.0 — 2026-11-25

### Launcher and navigation
- Redesign the launcher Home screen with responsive compact and wide layouts.
- Add adaptive launcher icon resources.
- Remove the persistent gameplay Menu button.
- Open the game menu with Android Back; retain only Reload, Settings and Return to game.
- Replace Settings, diagnostics and update dialogs with full-screen pages.
- Keep the existing GeckoSession while navigating to launcher pages.

### Published release artifacts
- Published GitHub Release: [v2.1.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.1.0), tag commit `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.
- VersionName `2.1.0`, versionCode `2001000`.
- Signed ARM64 APK: 96,444,836 bytes; SHA-256 `f65672c8855298764b3345e58694b672c39f35971a39beccb7e4d1feb6310cab`.
- Signing certificate SHA-256 matched the established identity: `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263`.
- Release metadata contains one canonical APK, one update manifest and one differential patch. The update manifest SHA-256 is `acaf5435905bcf4318388f94e94654ba582e45f1091898ecec8c5027ef54ed62`; the differential patch SHA-256 is `933420bf8115c3411afabbd5916e2799af43cf88c0ec54e2cdf55c152f5b090a`.

### CI and remaining verification
- Release workflow [run #45603533787](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45603533787) passed unit tests, APK signature verification, established certificate matching, ARM64-only ABI validation, differential generation and release upload/publish steps.
- The final release-asset lookup step failed with `gh: Not Found (HTTP 404)`. This is a workflow verification failure, not a reported APK-count mismatch; current GitHub release metadata independently confirms the expected three assets. Track the workflow fix under issue #31.
- Physical ARM64 verification of the new UI, session retention, gameplay and GeckoView feature matrix remains outstanding. CI does not replace device testing.

## v2.0.0 — 2026-11-25

### Browser engine and launcher
- Replace the embedded Cefrium engine with Mozilla GeckoView 160.
- Add a launcher Home screen; Play lazily creates the GeckoView session and Settings does not initialize the game.
- Add in-game Reload, Home and Return to game navigation while retaining the active GeckoSession when navigating Home.

### Updater and reliability
- Harden the manual GitHub Releases updater with per-hop HTTPS host validation and bounded redirects and response bodies.
- Validate release manifest metadata, installed patch base identity, downloaded file sizes and SHA-256 digests.
- Use unique staging files and clean up abandoned partial downloads without deleting verified APKs.
- Persist installer permission handoff state across Activity/process recreation.
- Preserve the verified full APK fallback when differential updates or diagnostic logging fail.
- Keep update checks, downloads and installation user-initiated; Android's package installer remains in control.

### Compatibility and limitations
- Keep Android 10/API 29+, ARM64-only packaging, application ID `com.neonyne.lasthavenlauncher` and the established release signing identity.
- Physical ARM64 gameplay, GeckoView profile persistence, media/fullscreen behavior and real update installation were not completed before this release. Follow-up device verification remains required.
- Addon manager replacement remains on hold and is not included.

## v1.0.0 — 2026-11-24

### Features
- Launch the existing LastHaven WotLK game in the pinned Cefrium/Chromium engine.
- Add a Settings & usage view with installed APK size, Cefrium profile storage (when available), and app cache size.
- Show app network bytes received and sent, including current-session and since-device-boot totals where Android supports them.
- Add local app, Cefrium lifecycle and updater diagnostic events with a log viewer and explicit share action.

### Updates and reliability
- Check for stable GitHub Releases and let the user choose whether to download an update.
- Offer a smaller differential update when the installed base APK matches, with a full signed APK fallback.
- Verify downloaded/reconstructed APK checksums, package identity, version and the established release signing certificate before handing off to Android's installer.
- Keep the product limited to arm64-v8a and preserve the existing application ID and signing identity.

### Privacy and storage
- Keep diagnostics and usage measurements on-device; no analytics or automatic log uploads.
- Redact common credentials and session-related values from diagnostic messages, including full Cookie/Set-Cookie headers and JSON credential fields; unit tests cover key/value, header, JSON and URL-query cases.
- Preserve the Cefrium game profile across normal launches and updates.

### Notes and limitations
- Network counters are per-app UID totals (game plus updater), not game-only measurements; Android resets them on reboot.
- Cefrium profile size is shown only when its profile directory is present and readable.
- Physical-device gameplay, GPU, audio, persistence and in-place update verification remain separate checks. Do not treat CI success as device verification.
- The local ZIP addon manager organizes packages but does not inject addons into the game.

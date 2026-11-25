# LastHavenPlus release changelog

## v2.1.0 — candidate (not yet published)

### Launcher and navigation
- Redesign the launcher Home screen with responsive compact and wide layouts.
- Add adaptive launcher icon resources.
- Remove the persistent gameplay Menu button.
- Open the game menu with Android Back; retain only Reload, Settings and Return to game.
- Replace Settings, diagnostics and update dialogs with full-screen pages.
- Retain the existing GeckoSession while its view is detached for launcher navigation.

### Build verification
- PR #43 exact-head unsigned release build and JVM unit tests passed (run #386).
- Post-merge beta signed build and unit tests passed (run #45601558602).
- The signed beta APK contained arm64-v8a only and matched the established release certificate SHA-256 b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263.
- Main promotion commit c5f6518e57d614da73642b7dc4c8a21e82e6e9b3 passed signed build, unit tests, certificate and ARM64-only verification in [run #45602742953](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45602742953).
- The main branch CI artifact used placeholder version 0.1.0 / versionCode 1 and is not distributable. The final 2.1.0 versioned APK must be built from the v2.1.0 tag.
- Physical ARM64 verification of the new UI, session retention and GeckoView feature matrix remains outstanding.

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
- Keep Android 10/API 29+, ARM64-only packaging, application ID com.neonyne.lasthavenlauncher and the established release signing identity.
- Physical ARM64 gameplay, GeckoView profile persistence, media/fullscreen behavior and real update installation were not completed before this release. Follow-up device verification remains required.
- Addon manager replacement was not included.

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

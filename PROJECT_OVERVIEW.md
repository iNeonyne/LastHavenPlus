# Lasthavens Android — Development Overview

## App summary

Lasthavens for Android is a native Kotlin app that embeds the Lasthavens game in Mozilla GeckoView and bundles the Lasthavens Addon Manager as a built-in WebExtension. The game and manager share one Gecko runtime/profile so managed add-ons can be used by the game.

Main screens:
- Home: Play and Addon Manager entry points.
- Play: fullscreen game session, session retention, native web dialogs, file picker, downloads, and external-link routing.
- Addon Manager: bundled manager page, shared profile, add-on import/export/backup/restore.

## Primary device target

**POCO X6 Pro** is the primary device for performance tuning and real-device QA. Target its Dimensity 8300-Ultra / Mali-G615, 120Hz 2712 × 1220 display, and 8GB/12GB RAM variants, while preserving compatibility with other Android devices. Do not force 120Hz or assume 120 FPS; measure actual frame pacing and keep refresh behavior adaptive. Optimize from real measurements rather than device-name checks.

## Version snapshot

- Android app: 1.0.0 (CI build candidate; physical-device validation pending)
- Addon Manager: 0.11.0
- GeckoView: 159.0.20261102144711

The app and extension have independent version numbers. Bump the app version for meaningful feature releases and update the README version history in the same change.

## Completed work

- [x] GitHub Actions release APK build.
- [x] Modernized Android Gradle/Kotlin configuration.
- [x] JSON and JavaScript syntax validation for the bundled extension.
- [x] APK asset verification for the extension manifest, English locale, and GeckoView ARM64 engine.
- [x] Native JavaScript alert/confirm/prompt dialog delegate.
- [x] ABI-specific APK packaging and universal fallback (CI validation pending).

## To-do / verification plan

### Release engineering
- [ ] Confirm split APK filenames and sizes from a completed CI artifact.
- [ ] Install the ARM64 APK on a physical Android phone.
- [ ] Configure a stable release signing key.
- [ ] Publish a device-tested GitHub Release with all ABI variants.
- [ ] Add a changelog/version consistency check.

### Functional testing
- [ ] Launch the game, sign in, and verify actual gameplay.
- [ ] Verify session retention when leaving and reopening Play.
- [ ] Install a known-compatible add-on and confirm in-game behavior.
- [ ] Verify add-on import/export, ZIP backup, and restore.
- [ ] Test native JavaScript dialogs.
- [ ] Test external links, popups, fullscreen, and back navigation.
- [ ] Test background/foreground, activity recreation, and Gecko process crash recovery.

### POCO X6 Pro optimization and QA
- [ ] Record the phone's RAM variant and exact HyperOS/Android build.
- [ ] Install and update the ARM64 APK on the phone.
- [ ] Verify resolution, display scaling, cutout/insets, fullscreen, keyboard, and touch targets.
- [ ] Measure cold/warm startup for each screen.
- [ ] Profile GeckoView memory, frame pacing, touch response, and long-session stability.
- [ ] Compare 60Hz and adaptive high-refresh behavior; do not force a refresh rate without evidence.
- [ ] Measure battery drain and temperature over a 30–60 minute gameplay session.
- [ ] Test HyperOS background/resume behavior, battery restrictions, Game Turbo, and network changes.
- [ ] Fix only reproducible device-specific issues and preserve Android-wide fallbacks.

### Quality
- [ ] Add Kotlin unit tests for URL routing, download filenames, and version parsing.
- [ ] Measure startup, memory, and responsiveness on physical hardware.
- [ ] Reduce package size safely without breaking GeckoView or bundled extension assets.
- [ ] Review GeckoView stable updates and extension compatibility regularly.

## Release rule

Only publish a version after:
1. CI build passes.
2. APK packaging checks pass.
3. A physical-device smoke test passes.
4. README version history and release notes are updated.

Push a matching `vX.Y.Z` tag to trigger GitHub Release creation and attach all generated APKs. Do not mark a CI-only candidate as device verified.

## Device smoke-test checklist

- [ ] Install the ARM64 APK.
- [ ] Launch app and open Play.
- [ ] Complete sign-in and verify gameplay.
- [ ] Return Home and reopen Play; verify session remains.
- [ ] Open Addon Manager and install a compatible add-on.
- [ ] Return to Play and verify the add-on runs.
- [ ] Import an add-on archive using Android's file picker.
- [ ] Export an add-on and save a backup ZIP.
- [ ] Restore the backup ZIP.
- [ ] Exercise alert, confirm, and prompt dialogs.
- [ ] Open an external link and confirm system-browser routing.
- [ ] Test fullscreen, back navigation, and app background/foreground.
- [ ] Force-stop or simulate a Gecko content-process crash and verify recovery.

## Known limitations

- Addon scan observes requested files; it cannot prove Lua initialization succeeded.
- Backups contain add-on files only, not SavedVariables or game settings.
- CI currently uses debug signing unless repository release-signing secrets are configured.

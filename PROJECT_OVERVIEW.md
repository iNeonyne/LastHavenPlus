# Lasthavens Android — Project Overview

## Purpose

Provide an Android app for Lasthavens using an embedded GeckoView browser engine and an integrated Addon Manager.

## Intended experience

- Launch the game at https://alt.lasthavens.com/play.
- Provide a home screen with Play and Addon Manager actions.
- Lock the game screen to landscape, matching Lasthavens' Android play guidance.
- Keep the game and manager on a shared GeckoView profile so installed add-ons are available to the game.
- Return to Home without unnecessarily discarding the game session.
- Route non-game websites to the device's browser.

## Current component versions

- Android app: 1.0.0
- Addon Manager: 0.11.0
- GeckoView: 160.0.20261116130455

Component versions can change independently with tested updates.

## Build status

An earlier release APK compiled and uploaded successfully. The current build uses Android API 37 because the selected GeckoView release declares that minimum compile SDK requirement. A successful CI build does not prove the app works correctly on a real phone.

## Development goals

1. Keep the release build reproducible and successful.
2. Confirm the game launches and authenticates.
3. Confirm add-ons installed in the manager are injected into the game.
4. Verify file import, backup, restore, export, and downloads.
5. Handle browser JavaScript alert, confirm, and prompt dialogs.
6. Verify external link routing and popup handling.
7. Test Android lifecycle, fullscreen, back navigation, and recovery after process crashes.
8. Validate performance and compatibility on a physical Android device.

## Device test checklist

- [ ] Install the CI APK.
- [ ] Launch the app and open the game.
- [ ] Complete sign-in and confirm gameplay.
- [ ] Return Home and reopen Play; confirm the same session is retained.
- [ ] Open Addon Manager.
- [ ] Install a compatible add-on and reload the game.
- [ ] Confirm the add-on is active and check manager diagnostics.
- [ ] Import an add-on archive with the Android file picker.
- [ ] Export an add-on and save a backup ZIP to Downloads.
- [ ] Restore the backup ZIP.
- [ ] Confirm alert, confirm, and text prompts.
- [ ] Open an external link and confirm it opens in the system browser.
- [ ] Rotate/recreate the activity, background the app, and test recovery after a content-process crash.

## Important limitations

- Live Game Scan observes add-on file requests; it cannot prove add-on Lua code initialized correctly.
- Backup ZIPs contain add-on files only, not game settings or SavedVariables.
- CI APK signing uses a debug key unless release signing secrets are configured.

# Lasthavens Android — Project Overview

## Purpose

Lasthavens Android is intended to provide an Android app experience for the Lasthavens game, using GeckoView rather than a separate external browser.

## Planned app experience

- Open the game at https://alt.lasthavens.com/play.
- Provide a home screen with **Play** and **Addon Manager** actions.
- Keep the game and add-on manager on a shared GeckoView browser profile so installed add-ons can be used by the game.
- Provide navigation back to the app home screen without unnecessarily discarding the game session.
- Open external links in the device's browser.

## Version targets

- Android app: 1.0.0
- Addon Manager: 0.11.0

The Addon Manager version should remain at 0.11.0 unless a later version is explicitly requested.

## Build and verification status

This repository has not yet been confirmed to produce a working APK. A successful Android build and device test are still required.

The first build should be run with Android Studio or GitHub Actions. Any GeckoView API or Gradle compatibility errors should be fixed based on the actual build output.

## Important implementation checks

Before calling the app complete, verify:

1. The app builds successfully.
2. The game opens at the intended URL.
3. Add-on installation through the manager is visible to the game.
4. Back navigation and returning to the game work as intended.
5. External links open outside the app.
6. Downloads, file selection, and backup/restore flows work on a real device.
7. JavaScript alert and confirmation dialogs behave correctly.

## Repository note

This document describes the intended app and outstanding verification work. It does not mean the Android source code or a working APK has already been committed.

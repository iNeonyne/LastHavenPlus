# Project Status

**Project:** LastHavenLancher  
**Status:** Planning / technical validation  
**Current milestone:** P0 — validate the foundation  
**Playable APK:** Not yet available

## Agreed product direction

- Build a new, focused Android launcher for the existing LastHaven WotLK game.
- Cefrium is the preferred embedded engine candidate, pending compatibility and build validation.
- First delivery is a playable APK, not a feature-complete launcher.
- The Addon Manager is planned for a later milestone, after the first playable delivery.
- Keep the workflow lean and prioritise a working game over optional features.

## Workflow

- Repository: [iNeonyne/Lasthavens-Android](https://github.com/iNeonyne/Lasthavens-Android)
- Stable branch: `main`
- Branching: short-lived task branches with pull requests into `main`.
- Agent model: parallel work for independent tasks, coordinated through one orchestrator and exclusive file ownership.
- Validation: automated checks and emulator testing; do not ask the user to test physical devices.

## Verified repository baseline

- The repository exists and is accessible.
- The default branch is `main`.
- The repository is currently being prepared for the new application.
- No application source or reproducible Android build has been established by this preparation step.

## Current blockers

1. Cefrium's current release, supported Android toolchain, ABI coverage, APIs, and licensing need source-backed validation.
2. The game's actual browser/runtime requirements and playable flow need to be verified against the target engine.
3. No minimal Android/Cefrium proof of concept has yet demonstrated that the game reaches a playable state.

## Next action

Complete P0 technical validation and record a clear go/no-go recommendation before committing to the app architecture or implementation.

# LastHavenLancher

**A focused Android launcher for the existing LastHaven WotLK game.**

LastHavenLancher is intended to open the existing game in an embedded Chromium-based engine, with a minimal launcher around it. The game itself is not being recreated.

> **Status: Planning / technical validation.** No playable APK is available from this repository yet. Engine compatibility and the first Android build must be verified before the project can claim a working game.

## Goals

- Launch directly into the existing LastHaven game.
- Provide reliable rendering, touch input, audio, and fullscreen gameplay.
- Preserve the game's browser profile and downloaded game assets between launches and app upgrades.
- Keep the launcher small and focused rather than building a general-purpose browser.
- Add an **Addon Manager in a later milestone**, after the first playable version is working.

## Planned technology

- **Android:** native Android application.
- **Embedded engine:** Cefrium is the current candidate. Its release, Android/ABI support, APIs, licensing, and compatibility with the game are subject to technical validation.
- **Game target:** the existing LastHaven WotLK web game. The default URL and required browser capabilities must be confirmed during validation.

No engine or game compatibility claim should be treated as verified until supported by a reproducible build and test.

## Roadmap

1. Validate the Android toolchain, Cefrium integration requirements, and the game's actual browser requirements.
2. Deliver the smallest playable APK.
3. Harden persistence, lifecycle handling, reconnect behavior, and data safety.
4. Add essential launcher settings.
5. Implement the Addon Manager.
6. Profile performance and prepare a signed release.

See [TODO.md](TODO.md) for the prioritised queue and [PROJECT_STATUS.md](PROJECT_STATUS.md) for the current state.

## Repository guide

| File | Purpose |
|---|---|
| [AGENTS.md](AGENTS.md) | Agent coordination, engineering, validation, and delivery rules |
| [TODO.md](TODO.md) | Prioritised project work |
| [PROJECT_STATUS.md](PROJECT_STATUS.md) | Current milestone, blockers, and next action |
| [DECISIONS.md](DECISIONS.md) | Recorded decisions and rationale |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Branch and pull-request workflow |

## Development

The Android project and its build instructions have not been created yet. Build prerequisites and exact commands will be documented here once the toolchain and project structure are validated. Do not infer build commands or dependency versions from an unverified proposal.

## Workflow

- `main` is the stable branch.
- Work is done on short-lived task branches and proposed through pull requests into `main`.
- Independent investigations may run in parallel; concurrent code changes must have clearly separated file ownership.
- Automated checks and emulator testing are preferred. Contributors must not rely on the user testing physical devices.

## Scope

The launcher is not intended to provide tabs, bookmarks, general web browsing, or unrelated browser features. Features should be added only when needed for the game or the agreed launcher roadmap.

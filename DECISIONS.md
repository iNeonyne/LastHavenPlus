# Architecture and Product Decisions

Record decisions that materially affect architecture, scope, dependencies, or delivery. Add evidence and revisit a decision when its assumptions change.

## D-001 — Build a new focused Android launcher

**Status:** Accepted

Build LastHavenLancher from a clean application baseline. The existing LastHaven WotLK game is the product being launched; this repository is not for recreating the game.

**Rationale:** Keep the app small, focused, and optimised around one game.

## D-002 — Cefrium is the preferred embedded engine candidate

**Status:** Proposed; validation required

Cefrium is the current preferred engine candidate. Do not treat its API, compatibility, licensing, build requirements, or performance as confirmed until checked against current upstream documentation and a reproducible proof of concept.

**Validation required:** Android API/toolchain, supported ABIs, artifact availability, rendering, WebGL/WebAssembly requirements, media, touch, lifecycle, and persistent profile behavior.

## D-003 — First delivery is playable

**Status:** Accepted

Prioritise the smallest APK that can load the actual game and reach a playable state. Do not delay this milestone for optional settings, advanced optimisation, or the Addon Manager.

## D-004 — Addon Manager is planned but deferred

**Status:** Accepted

The application will include an Addon Manager in a later milestone, after the first playable delivery. Investigate the game's actual add-on format and lifecycle before designing it. Do not introduce an unrelated or assumed add-on system.

## D-005 — Preserve game data across restarts and upgrades

**Status:** Accepted

Use a stable app-private profile location and protect cookies, LocalStorage, IndexedDB, Cache Storage, and downloaded game assets. Distinguish disposable cache from important profile data. Never clear game data automatically.

## D-006 — Lean parallel-agent workflow

**Status:** Accepted

Parallelise independent research and implementation, but assign exclusive file ownership to concurrent coding tasks. Maintain one prioritised queue and centralise integration and review to avoid duplicated work and conflicting edits.

## D-007 — Pull requests into main

**Status:** Accepted

Keep `main` as the stable integration branch. Use short-lived task branches and pull requests for changes. Do not push implementation work directly to `main`.

## D-008 — Automated and emulator-based validation

**Status:** Accepted

Use automated tests, CI, and emulator testing. Do not make physical-device testing a requirement for user handoffs.

## Open decisions

- Exact Cefrium version and distribution method.
- Minimum Android API and supported ABIs.
- Exact game URL/default URL and required runtime capabilities.
- Android architecture and profile ownership details, after the proof of concept.
- Release signing and distribution strategy.

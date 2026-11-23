# Project TODO

This is the single prioritised work queue. Keep tasks actionable and remove or update entries as work is completed.

## P0 — Technical validation

- [ ] Inspect and document the current repository baseline and available GitHub Actions configuration.
- [ ] Confirm the supported Android toolchain from current Android/Gradle documentation before pinning versions.
- [ ] Validate the current Cefrium release, Android API requirements, ABIs, artifact distribution, licensing, and integration APIs.
- [ ] Build a minimal Cefrium proof of concept before designing app architecture around unverified APIs.
- [ ] Inspect the actual LastHaven game entry point and determine required WebGL, WebAssembly, threading/isolation, media, input, and storage capabilities.
- [ ] Verify whether the game can load and reach a playable state in the proposed engine; loading only the landing/login page is not sufficient.
- [ ] Record evidence, risks, and the go/no-go recommendation in [DECISIONS.md](DECISIONS.md).

## P1 — First playable APK

- [ ] Create the minimal native Android project and reproducible build.
- [ ] Integrate the validated Cefrium version.
- [ ] Load the existing LastHaven game directly.
- [ ] Verify hardware-accelerated rendering and the required game graphics APIs.
- [ ] Verify touch input and audio.
- [ ] Implement fullscreen gameplay and appropriate orientation behavior.
- [ ] Add a minimal loading state and connection failure/retry path.
- [ ] Produce and validate an installable APK.

**Exit criteria:** an emulator can launch the APK, load the actual game, and reach a playable state with working rendering and input. Do not count a successful page load alone as completion.

## P2 — Reliability and persistence

- [ ] Define a stable app-private game-profile location.
- [ ] Verify cookies, LocalStorage, IndexedDB, Cache Storage, and downloaded game assets persist across restarts.
- [ ] Verify profile data survives app upgrades.
- [ ] Separate disposable HTTP cache from important game/profile data.
- [ ] Add lifecycle, process recreation, network loss, reload, and reconnect handling.
- [ ] Add automated data-preservation regression tests.
- [ ] Ensure no automatic cache or profile clearing occurs.

## P3 — Essential launcher controls

- [ ] Add a minimal settings surface.
- [ ] Add game URL configuration with validation.
- [ ] Add fullscreen and orientation controls.
- [ ] Add reload and engine/app version information.
- [ ] Add separate cache clearing and destructive game-data clearing with explicit confirmation.

## P4 — Addon Manager (deferred)

- [ ] Investigate LastHaven's existing add-on formats, sources, install/update/remove behavior, and storage layout.
- [ ] Define an Addon Manager design that does not interfere with the game's profile or downloaded assets.
- [ ] Implement import/install, listing, enable/disable if supported, update, and removal only after the format and behavior are verified.
- [ ] Add validation, rollback/error handling, and tests for malformed or incompatible add-ons.

## P5 — Performance and release

- [ ] Measure startup, frame pacing, rendering latency, memory use, thermal behavior, audio stability, and long-session reliability.
- [ ] Optimise only measured bottlenecks; avoid unnecessary background services.
- [ ] Establish one authoritative app version source.
- [ ] Configure secure release signing and CI secrets without committing credentials.
- [ ] Add automated build, test, APK metadata, package, ABI, and signature checks.
- [ ] Verify upgrades preserve the game profile.
- [ ] Publish a tested APK through GitHub Releases with clear release notes.

## Working rules

- Work on the highest-priority task whose dependencies are satisfied.
- Keep tasks small enough to review and validate.
- Parallelise independent research; assign exclusive file ownership for concurrent implementation.
- Update [PROJECT_STATUS.md](PROJECT_STATUS.md) when the active milestone or blockers change.

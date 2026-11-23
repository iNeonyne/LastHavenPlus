# LastHavenPlus — Development Roadmap

This is the authoritative active task list. Priorities are ordered by what gets a usable demo into the owner's hands safely. Update task status only after checking the implementation or actual test result. All work is on `beta`; see [AGENTS.md](AGENTS.md). Do not promote anything to `main` without the owner's explicit instruction.

## Current status

- Development branch: `beta`.
- Android app: `2.0.0` development version.
- Bundled Addon Manager: `1.0.2`, owner-authorized maintenance in progress with add-on data preservation required.
- CI: beta run #138 succeeded and uploaded both demo APK artifacts; physical-device validation is still unconfirmed.
- Physical-device validation: not yet confirmed complete.
- Permanent release signing: paused; required before public releases, not for debug demo testing.
- Automatic updates: not implemented. The planned source is `main` CI artifacts only; follow the version-code and stable-signing requirements in `AGENTS.md` if this is reintroduced.

## P0 — Demo making

**Goal:** get a working beta demo installed on a physical Android phone as soon as possible. Keep this list focused on the shortest path to testing the core app.

- [x] Configure normal pushes to `beta` to build and upload debug-signed demo APKs.
- [x] Set the Home screen to sensor landscape and add a landscape layout.
- [x] Add CI checks for APK packaging, extension assets, signatures, and checksums.
- [ ] Confirm the latest beta GitHub Actions run completed successfully, including APK build and artifact upload.
- [ ] Download the ARM64 demo APK from that successful run.
- [ ] Install it on a physical phone. Do not uninstall a differently signed existing app if its data must be preserved.
- [ ] Test first launch, landscape Home, Play, game loading, sign-in, and gameplay.
- [ ] Test Home → Play return and verify the game session remains usable.
- [ ] Open Addon Manager, inspect existing add-ons, then return to Play and verify integration.
- [ ] Test essential controls: touch, fullscreen, keyboard, Android Back, popups, external links, and dialogs.
- [ ] Test background/resume, network loss/recovery, and Gecko content-process recovery.
- [ ] Record device model, RAM, Android version, crashes, reproducible faults, and observed performance.
- [ ] Fix demo-blocking faults only, then repeat the relevant tests.

## P1 — Demo blockers and Android reliability

Do these after the first installable APK is available, or sooner only when a finding blocks the demo or risks user data.

- [ ] Review lifecycle handling, delegate cleanup, Back behavior, and Gecko process recovery.
- [ ] Confirm the exact Gecko profile/runtime sharing behavior between Play and Addon Manager.
- [ ] Review navigation, redirects, popup handling, external links, HTTPS behavior, and custom URL schemes.
- [ ] Add focused Kotlin tests for URL routing and host allow-listing.
- [ ] Audit native bridge exposure and prove untrusted pages cannot invoke privileged Android functionality.
- [ ] Add tests for download filename sanitization and other native file-handling boundaries.
- [ ] Test storage failure and interrupted-write recovery before changing persistence behavior.

## P1 — Data preservation and Addon Manager hardening

The owner has authorized necessary manager changes on the condition that installed add-ons carry over. Preserve add-on files, folder IDs, versions, enabled state, library metadata, settings, and the shared Gecko profile. Do not reset or wipe existing data. Use staged writes, compatibility fallbacks, and verified rollback behavior.

- [x] Make add-on replacement crash-safe by writing files to a fresh IndexedDB storage generation before switching the library pointer.
- [x] Preserve the existing storage-key format for already-installed add-ons; legacy entries without a storage pointer continue to read from their original folder key.
- [x] Clean up unreferenced storage generations after successful commits and at startup.
- [x] Correct numeric ordering of numbered TOC dependencies.
- [x] Enforce archive/download byte limits against actual streamed bytes, including imported/restored archives and remote ZIPs.
- [ ] Run the Addon Manager regression suite (including streaming-limit tests) and full CI build.
- [ ] Add a failure-injection test for metadata commit failure and confirm the prior generation remains readable.
- [ ] Verify install, update, remove, backup/restore, and game loading on a physical device with pre-existing add-ons.
- [ ] Document precisely which profile, login/session, add-on, and setting data survives app updates.
- [ ] Verify backup/restore scope. Current manager ZIP backups contain managed add-on files only; they do not include game settings, SavedVariables, or manager enabled/disabled flags.
- [ ] Decide whether to expand backup coverage or clearly disclose those exclusions in the UI and documentation. Any manager-code change requires explicit authorization.

## P2 — Requested features and manager improvements

These are planned features, not prerequisites for the first demo. Implement only after the core app and existing data have been validated.

- [ ] Add `.7z` archive support to Addon Manager import/export, with safe extraction, path validation, archive/expanded-size limits, and backward compatibility for existing ZIP backups.
- [x] Add a read-only storage dashboard with persistent app/shared GeckoView profile usage, browser/app cache, and available device storage. It labels the persistent figure as shared app/profile data, not game-only storage; lifetime network download totals are not claimed.
- [ ] Validate dashboard measurements on a physical device against Android app-storage settings.
- [ ] Investigate reliable GeckoView network-byte instrumentation if a lifetime downloaded-data counter is still wanted; do not infer it from current stored bytes.
- [ ] Add a diagnostics page showing app and GeckoView versions, device/Android details, add-on integration status, storage health, and recent app errors. Include a user-triggered copy/export diagnostics action; exclude credentials, cookies, tokens, and personal browsing data.
- [ ] Create a device test checklist for install, update, enable/disable, remove, import/export, backup/restore, and game integration.
- [ ] Verify error states and recovery for failed imports, failed downloads, unavailable permissions, and interrupted operations.
- [ ] Verify usability on phone-sized screens, touch targets, accessibility, and large add-on lists.
- [ ] Document manager capabilities and limitations based on the current bundled source.
- [ ] Review compatibility with GeckoView upgrades without modifying the manager.
- [ ] Review every future manager change for backward compatibility and data preservation before editing.

## P3 — Ongoing maintenance

- [ ] Add Dependabot or Renovate monitoring for dependencies and review security updates.
- [ ] Upgrade GeckoView and Android dependencies in isolated, build-verified changes.
- [ ] Evaluate R8/resource shrinking against release builds and extension/runtime regression checks.
- [ ] Expand device/OEM testing when evidence or user reports justify it.

## P4 — Release signing and safe upgrades

Paused during demo development. Do this before any public release; it is not a blocker for debug-signed demo testing.

- [ ] Resolve the release certificate mismatch and verify the intended permanent signing identity.
- [ ] Back up the permanent private key securely in at least two offline locations.
- [ ] Configure GitHub Actions secrets without exposing key material.
- [ ] Verify all ABI APKs use the same release certificate.
- [ ] Install a release-signed APK, then verify a subsequent same-certificate APK upgrades in place.
- [ ] Verify profile, login/session data, installed add-ons, and settings survive an upgrade.
- [ ] Ensure debug-signed demo artifacts can never be published as public release assets.

## Release gate

Do not publish until all applicable items are complete:

- [ ] CI, unit tests, packaging, and release build pass.
- [ ] Physical-device core workflow is tested and results recorded.
- [ ] Terms/permission to distribute the independent client have been clarified with the Lasthavens team.
- [ ] Permanent signing identity and same-certificate upgrade path are verified.
- [ ] Data/profile preservation is verified across upgrade.
- [ ] APK package name, version, signing certificate, ABI, and permissions are checked.
- [ ] SHA-256 checksums are generated and verified.
- [ ] README and changelog reflect the actual release.

## Out of scope unless a real need is demonstrated

- General-purpose browser features unrelated to Lasthavens.
- Replacing GeckoView without a technical case and migration plan.
- Analytics, tracking, push notifications, or background services.
- Differential APK patching.
- Resetting or wiping existing add-on data; any data-format change without a tested migration and rollback path.

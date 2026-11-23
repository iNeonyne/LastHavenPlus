# LastHavenPlus — Manual Verification and Owner Actions

This checklist contains work that an AI coding agent cannot truthfully complete on its own: physical-device testing, actions requiring the owner's accounts or private keys, legal/permission decisions, and release approval.

Development and code changes remain on `beta`. See [AGENTS.md](AGENTS.md) and [TODO.md](TODO.md). Do not promote anything to `main` without explicit owner approval.

## Physical Android device validation

- [ ] Download the ARM64 demo APK from a successful beta GitHub Actions run.
- [ ] Install it on a physical Android phone. Do not uninstall a differently signed existing app if its data must be preserved.
- [ ] Test first launch, landscape Home, Play, game loading, sign-in, and gameplay.
- [ ] Test Home → Play return and verify the game session remains usable.
- [ ] Open Addon Manager, inspect existing add-ons, then return to Play and verify integration.
- [ ] Test essential controls: touch, fullscreen, keyboard, Android Back, popups, external links, and dialogs.
- [ ] Test background/resume, network loss/recovery, and Gecko content-process recovery.
- [ ] Record device model, RAM, Android version, crashes, reproducible faults, and observed performance.
- [ ] Verify install, update, enable/disable, remove, import/export, backup/restore, and game integration with pre-existing add-ons.
- [ ] Validate storage dashboard measurements against Android app-storage settings.
- [ ] Verify error states and recovery for failed imports, failed downloads, unavailable permissions, and interrupted operations.
- [ ] Check usability on phone-sized screens, touch targets, accessibility, and large add-on lists.
- [ ] Verify profile, login/session data, installed add-ons, and settings survive an in-place app upgrade.
- [ ] Record the exact app version, versionCode, APK ABI, device, Android version, and test result for each validation.

## Owner actions and release decisions

- [ ] Decide whether to expand Addon Manager backup coverage beyond managed add-on files, or approve clear disclosure of the exclusions (game settings, SavedVariables, and manager enabled/disabled flags). Any manager-code change requires explicit authorization.
- [ ] Resolve the release certificate mismatch and confirm the intended permanent signing identity.
- [ ] Back up the permanent private key securely in at least two offline locations.
- [ ] Provide/configure GitHub Actions signing secrets without exposing key material.
- [ ] Confirm the release certificate fingerprint matches the intended identity.
- [ ] Verify all ABI APKs use the same release certificate.
- [ ] Install a release-signed APK, then verify a subsequent same-certificate APK upgrades in place.
- [ ] Clarify terms/permission to distribute the independent client with the Lasthavens team.
- [ ] Explicitly approve any release and any beta-to-main promotion.

## Release sign-off

Do not publish until all applicable checks are complete:

- [ ] CI, unit tests, packaging, and release build pass.
- [ ] Physical-device core workflow is tested and results are recorded.
- [ ] Distribution terms/permission are clarified.
- [ ] Permanent signing identity and same-certificate upgrade path are verified.
- [ ] Data/profile preservation is verified across upgrade.
- [ ] APK package name, version, signing certificate, ABI, and permissions are checked.
- [ ] SHA-256 checksums are generated and verified.
- [ ] README and changelog reflect the actual release.

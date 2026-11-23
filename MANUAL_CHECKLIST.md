# LastHavenPlus — Manual Verification and Owner Actions

This checklist contains work that an AI coding agent cannot truthfully complete on its own: physical-device testing, actions requiring the owner's accounts or private keys, legal/permission decisions, and release approval.

Development and code changes remain on `main`, the sole active branch. The former `beta` branch has been consolidated and retired. See [AGENTS.md](AGENTS.md) and [TODO.md](TODO.md).

## Physical Android device validation

- [ ] On a spare test device, download the permanent-key-signed ARM64 APK from main run #196. Do not install it over a data-bearing debug-signed installation.
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

### Published GitHub Release updater verification

- [ ] Do not use a debug-signed pull-request or test APK to test a main update; the updater must block a signer mismatch. A debug-signed installation cannot be upgraded in place to the permanent release key. Do not uninstall a data-bearing install to switch signing identities; use a separate test device.
- [x] Main run #196 verified the expected permanent-key fingerprint in the previous multi-ABI configuration. Main run #231 passed the ARM64-only packaging, signing, APK metadata, checksum, and update-metadata checks. The release-based updater is now being validated by CI.
- [ ] On a spare ARM64 test device, install a permanent-key-signed APK and create test data (login/session, installed add-ons, manager settings, and GeckoView profile state).
- [ ] After a stable GitHub Release is published, use Home → Check for updates; confirm it selects only the latest published stable release and the ARM64 asset. Drafts and prereleases must be ignored.
- [ ] Confirm displayed version, APK versionCode, APK size and release tag match the selected asset.
- [ ] Confirm Android's installer requires user approval and completes an in-place update without uninstalling.
- [ ] Verify login/session, GeckoView profile, installed add-ons, manager settings and other app data remain intact after update.
- [ ] Cancel an installation and verify the installed version remains unchanged.
- [ ] Confirm absent/malformed release assets, draft/prerelease releases, failed or incomplete downloads, insufficient free storage, checksum mismatch, wrong package/version, and different signing certificates all block installation without altering app data.
- [ ] Repeat on ARM64 devices/emulators only; confirm non-ARM64 devices are clearly reported as unsupported.

## Owner actions and release decisions

- [ ] Decide whether to expand Addon Manager backup coverage beyond managed add-on files, or approve clear disclosure of the exclusions (game settings, SavedVariables, and manager enabled/disabled flags). Any manager-code change requires explicit authorization.
- [x] Main run #196 confirmed the existing `ANDROID_KEYSTORE_BASE64` secret matches the expected fingerprint; no replacement was needed.
- [ ] Back up the permanent private key securely in at least two offline locations.
- [x] GitHub Actions signing secrets are configured and were successfully used by main run #196; keep all key material out of source control, logs, and chat.
- [x] Main run #196 logged identical configured and expected certificate fingerprints.
- [x] Main run #231 verified the single ARM64 APK signature with the permanent release certificate.
- [ ] Install a main/release-signed APK, then verify a subsequent same-certificate main APK upgrades in place.
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
- [ ] Published-release updater passes the end-to-end success and blocked/failure cases.
- [ ] README and changelog reflect the actual release.

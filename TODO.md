# LastHavenPlus — AI Development Roadmap

This is the active work queue for tasks an AI coding agent can perform in the repository: implementation, automated tests, CI, code review, technical investigation, and documentation. Human-only actions and physical-device verification are tracked in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).

All development work stays on `beta`. Never promote, merge, or push work to `main` unless the owner explicitly authorizes it. Follow [AGENTS.md](AGENTS.md), especially the data-preservation and versioning rules.

## Current status

- Development branch: `beta`.
- Android app: `2.0.0` development version.
- Bundled Addon Manager: `1.0.2`.
- Latest recorded successful CI validation: beta run #162 tested commit `cc06c96`; Addon Manager tests, Android navigation policy tests, APK build, signature checks, packaged assets, checksums, and both artifact uploads passed.
- Physical-device validation and owner-only release actions are tracked separately in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).
- Permanent release signing is paused. It is not required for debug demo development.
- Automatic updates are back on the roadmap, but are not implemented. They must use successful `main` CI artifacts only, preserve the installed app's data, and require the permanent signing certificate for in-place upgrades.
- Version flow: approved main promotions use clean stable SemVer; beta restarts from that main version using the next prerelease candidate. See `AGENTS.md`.

## P0 — Data safety and demo-blocking code

- [ ] Review lifecycle handling, delegate cleanup, Back behavior, and Gecko process recovery; fix confirmed code defects.
- [ ] Trace and document the exact Gecko profile/runtime sharing behavior between Play and Addon Manager.
- [ ] Review navigation, redirects, popup handling, external links, HTTPS behavior, and custom URL schemes; fix confirmed defects.
- [x] Add focused Kotlin/JVM tests for URL routing and host allow-listing; CI runs them before packaging.
- [ ] Audit native bridge exposure and add automated protections proving untrusted pages cannot invoke privileged Android functionality.
- [ ] Add automated tests for download filename sanitization and other native file-handling boundaries.
- [ ] Add storage-failure and interrupted-write tests without changing persistence behavior unless a safe migration and rollback path are demonstrated.
- [ ] Add a failure-injection test for Addon Manager metadata commit failure and confirm the prior storage generation remains readable.
- [ ] Review install/update/remove/backup/restore code paths for data-preservation defects; fix only defects that can be verified through code and automated tests.
- [ ] Inspect the implementation and document precisely which profile, login/session, add-on, and setting data is preserved by app updates; clearly distinguish verified code behavior from device-only verification.

## P1 — Addon Manager and requested features

The owner has authorized necessary manager maintenance only with preservation of existing add-on files, folder IDs, versions, enabled state, library metadata, settings, and the shared Gecko profile. Do not reset or wipe existing data. Any additional manager-code change outside the authorized scope requires explicit owner approval.

- [x] Make add-on replacement crash-safe by writing files to a fresh IndexedDB storage generation before switching the library pointer.
- [x] Preserve the existing storage-key format for already-installed add-ons; legacy entries without a storage pointer continue to read from their original folder key.
- [x] Clean up unreferenced storage generations after successful commits and at startup.
- [x] Correct numeric ordering of numbered TOC dependencies.
- [x] Enforce archive/download byte limits against actual streamed bytes, including imported/restored archives and remote ZIPs.
- [x] Run the Addon Manager regression suite (including streaming-limit tests) and full CI build (beta run #159).
- [ ] Add `.7z` archive support to Addon Manager import/export, with safe extraction, path validation, archive/expanded-size limits, and backward compatibility for existing ZIP backups.
- [x] Add a read-only storage dashboard showing persistent app/shared GeckoView profile usage, browser/app cache, and available device storage. Do not label shared profile usage as game-only storage or claim lifetime network download totals from stored-byte figures.
- [ ] Investigate reliable GeckoView network-byte instrumentation if a lifetime downloaded-data counter is still wanted; do not infer it from current stored bytes.
- [ ] Add a diagnostics page showing app and GeckoView versions, device/Android details, add-on integration status, storage health, and recent app errors. Include a user-triggered copy/export action; exclude credentials, cookies, tokens, and personal browsing data.
- [ ] Review the current backup implementation and prepare a precise description of its scope and exclusions. Current manager ZIP backups contain managed add-on files only; they do not include game settings, SavedVariables, or manager enabled/disabled flags.
- [ ] Review manager capabilities and limitations against the bundled source and update its documentation.
- [ ] Review compatibility with GeckoView upgrades without modifying the manager.
- [ ] Review future manager changes for backward compatibility and data preservation before implementation.

## P2 — Reliability, accessibility, and maintenance

- [ ] Add automated tests for failed imports, failed downloads, unavailable permissions, and interrupted operations wherever the behavior can be exercised without a physical device.
- [ ] Improve error reporting and recovery for import, download, storage, and GeckoView failures.
- [ ] Review phone-sized layouts, touch targets, accessibility semantics, and large add-on lists; implement code-side improvements.
- [ ] Add Dependabot or Renovate monitoring for dependencies and document the update/review workflow.
- [ ] Upgrade GeckoView and Android dependencies in isolated, build-verified changes.
- [ ] Evaluate R8/resource shrinking against release builds and extension/runtime regression checks.
- [ ] Expand automated tests and CI validation when code changes or reproducible reports justify them.

## P3 — Release engineering code and CI

- [ ] Add CI safeguards ensuring debug-signed demo artifacts cannot be published as public release assets.
- [ ] Add automated release validation for package ID, version metadata, ABI coverage, signing certificate consistency, and SHA-256 checksums.
- [ ] Keep release workflows compatible with the versioning and promotion policy in `AGENTS.md`.
- [ ] Add a dedicated successful-build APK artifact for pushes to `main`; keep beta/demo artifacts separate and never use them as update sources.
- [ ] Ensure main update APKs are signed with the permanent release key; verify the certificate fingerprint against the expected certificate and fail closed if signing is unavailable or mismatched.
- [ ] Replace the static Android `versionCode` with an automated, strictly increasing build number that remains compatible with the project's SemVer and main-to-beta version flow.
- [ ] Implement an update checker that queries GitHub Actions for the latest successful workflow run on `main` only; reject beta runs, failed/in-progress runs, unexpected artifacts, and incompatible app versions.
- [ ] Implement full-APK download for the device's supported ABI, with a universal APK fallback; do not implement differential/patch updates.
- [ ] Verify the downloaded APK against the SHA-256 checksum published by the same main workflow before making installation available; reject missing or mismatched checksums and incomplete downloads.
- [ ] Hand installation to Android's package installer with clear version, size, and source information; never attempt silent installation or uninstall/reinstall.
- [ ] Add automated tests for main-only update selection, workflow/artifact failures, ABI selection, version-code ordering, checksum mismatch, interrupted downloads, and signing/version compatibility.
- [ ] Document that an in-place update is supported only when the installed app and downloaded APK share the same signing certificate; do not offer an update that could require uninstalling the app.
- [ ] Validate through code and tests that the update path does not clear app data, cookies, login/session state, GeckoView profile, or installed add-ons; add physical-device upgrade verification to `MANUAL_CHECKLIST.md`.

## Not active work

The following are not implementation tasks unless the owner explicitly reopens them:

- General-purpose browser features unrelated to Lasthavens.
- Replacing GeckoView without a technical case and migration plan.
- Analytics, tracking, push notifications, or background services.
- Differential APK patching.
- Automatic updates beyond the defined main-only, checksum-verified, same-certificate in-place update design.
- Resetting or wiping existing add-on data, or changing its format without a tested migration and rollback path.

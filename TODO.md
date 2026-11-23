# LastHavenPlus — AI Development Roadmap

This is the active work queue for tasks an AI coding agent can perform in the repository: implementation, automated tests, CI, code review, technical investigation, and documentation. Human-only actions and physical-device verification are tracked in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).

All development work stays on `main`, the sole active branch. The former `beta` branch has been consolidated and is retired. Follow [AGENTS.md](AGENTS.md), especially the data-preservation and versioning rules.

## Current status

- Active development branch: `main`.
- Android app: `2.0.0` development version.
- Bundled Addon Manager: `1.0.2`.
- Latest main CI run #196 (commit `ed0c5bb`) passed all automated tests, permanent-key verification, four release APK builds, APK signature and package/version checks, checksum generation, metadata creation, and all five main update artifact uploads. The direct DER certificate hash matched the expected permanent release fingerprint.
- Main workflow-dispatch run #198 completed successfully and uploaded artifacts, but it is intentionally ignored by the updater; the latest eligible main push run remains #196.
- Physical-device validation and owner-only release actions are tracked separately in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).
- Main run #196 confirmed the configured GitHub Actions keystore and passwords/alias work and the certificate fingerprint matches the expected permanent release key. No secret replacement is needed.
- A manual, main-only updater is implemented on `main`. Run #196 published the metadata/checksum artifact and four ABI-specific signed APK artifacts; the updater verifies their shared run provenance, metadata, checksum, package/version and signing-certificate compatibility before invoking Android's user-confirmed installer. Physical same-certificate upgrade and data-preservation testing remains outstanding.
- Version flow: main-only SemVer releases with a monotonic workflow-run `versionCode`; see `AGENTS.md`.

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

- [x] Add CI safeguards ensuring debug-signed beta/demo artifacts are not accepted as main or tagged release assets; main/tag builds fail closed unless the permanent certificate matches.
- [x] Add CI validation for package ID, APK-embedded versionName/versionCode, ABI coverage, signer consistency, and SHA-256 checksums.
- [x] Keep release workflows compatible with the versioning and promotion policy in `AGENTS.md`.
- [x] Configure one main-only metadata/checksum artifact and four separate ABI-specific APK artifacts; beta/demo artifacts remain separate and are rejected by the updater. Main publishes them directly; verify the first post-merge run.
- [x] Require the permanent release key and exact expected certificate on main/tag builds; missing secrets or fingerprint mismatch fail closed. A main-signed build remains to be verified after consolidation.
- [x] Replace static Android `versionCode` with the existing workflow's global `GITHUB_RUN_NUMBER`; local builds use a development-only fallback and are never update candidates.
- [x] Implement the `main`-only update checker; reject beta, tag, PR, manual, failed, cancelled, in-progress, expired, mismatched and incompatible build artifacts.
- [x] Implement full-APK download for the device's first supported ABI, with universal fallback; no differential updates.
- [x] Verify APK size and SHA-256 against metadata and the checksum file from the same main build; reject missing/mismatched checksums and incomplete downloads.
- [x] Hand the verified APK to Android's package installer with build/version, size and commit information; Android requires user confirmation and the app never silently installs or uninstalls.
- [x] Add JVM policy tests for main-only runs/artifacts, provenance, expiry, ABI fallback, version ordering, interrupted/incomplete/checksum failures and signer mismatch; add Python tests for metadata generation and validation.
- [x] Document and enforce exact signing-certificate, package ID and version compatibility; incompatible candidates are blocked instead of prompting an uninstall.
- [x] Keep the updater to an Android package replacement only; it does not clear app data, cookies, login/session, GeckoView profile or add-ons. Physical verification remains in `MANUAL_CHECKLIST.md`.

## Updater verification boundary

The updater implementation and automated policy/build checks are complete. Main run #196 proved permanent-key signing, release APK validation, metadata/checksum generation, and publication of all five main update artifacts. Complete the same-certificate physical-device upgrade and data-preservation checks in `MANUAL_CHECKLIST.md`.

## Not active work

The following are not implementation tasks unless the owner explicitly reopens them:

- General-purpose browser features unrelated to Lasthavens.
- Replacing GeckoView without a technical case and migration plan.
- Analytics, tracking, push notifications, or background services.
- Differential APK patching.
- Automatic updates beyond the defined main-only, checksum-verified, same-certificate in-place update design.
- Resetting or wiping existing add-on data, or changing its format without a tested migration and rollback path.

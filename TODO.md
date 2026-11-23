# LastHavenPlus — AI Development Roadmap

This is the active work queue for tasks an AI coding agent can perform in the repository: implementation, automated tests, CI, code review, technical investigation, and documentation. Human-only actions and physical-device verification are tracked in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).

All development work stays on `main`, the sole active branch. The former `beta` branch has been consolidated and is retired. Follow [AGENTS.md](AGENTS.md), especially the data-preservation and versioning rules.

## Current status

- Active development branch: `main`.
- Android app: `1.0.1` next patch release being prepared; owner reports `1.0.0` is released.
- Bundled Addon Manager: `1.0.3`.
- Main CI run #231 (commit `58a0b6c`) passed automated tests, permanent-key verification, ARM64 APK build/signature/package/version checks, and checksum generation.
- The in-app updater now uses published stable GitHub Releases; main CI artifacts are not update sources.
- Physical-device validation and owner-only release actions are tracked separately in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).
- Main run #231 confirmed the configured GitHub Actions keystore and passwords/alias work and the certificate fingerprint matches the expected permanent release key. No secret replacement is needed.
- The updater is being migrated from expiring Actions artifacts to published stable GitHub Releases. It now fetches the latest published release, requires the exact ARM64 APK and checksum assets, and verifies APK integrity, package/version, ABI, and installed signing-certificate compatibility before Android installer handoff. CI and physical-device validation are pending.
- Version flow: main-only SemVer releases with a monotonic workflow-run `versionCode`; see `AGENTS.md`.
- A Termux-free release-request workflow is installed. Its dry-run validation is being repeated for `v1.0.1`; real requests create the annotated tag and dispatch the signed tag build, which creates a draft for manual review.

## P0 — Data safety and demo-blocking code

- [x] Review lifecycle handling, delegate cleanup, Back behavior, and Gecko process recovery; fix confirmed code defects. Fixed delegate cleanup and guarded duplicate process-recovery callbacks in Play and Addon Manager; main CI run #270 passed. Physical-device lifecycle checks remain in MANUAL_CHECKLIST.md.
- [x] Trace and document the Gecko profile/runtime sharing behavior between Play and Addon Manager in [docs/GECKOVIEW_PROFILE.md](docs/GECKOVIEW_PROFILE.md); same single App-owned runtime, separate Play/Manager sessions.
- [ ] Review navigation, redirects, popup handling, external links, HTTPS behavior, and custom URL schemes; fix confirmed defects.
- [x] Add focused Kotlin/JVM tests for URL routing and host allow-listing; CI runs them before packaging.
- [ ] Audit native bridge exposure and add automated protections proving untrusted pages cannot invoke privileged Android functionality.
- [x] Extract download filename sanitization into a pure JVM policy and add regression tests for path separators, reserved/control characters, length limits, empty names, and Unicode filenames. Other native file-handling boundaries remain to be audited.
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
- [x] Add a game-origin storage estimate to the Addon Manager, measured from the running game origin and kept distinct from shared profile usage and lifetime network downloads.
- [ ] Investigate reliable GeckoView network-byte instrumentation if a lifetime downloaded-data counter is still wanted; do not infer it from current stored bytes.
- [ ] Add a diagnostics page showing app and GeckoView versions, device/Android details, add-on integration status, storage health, and recent app errors. Include a user-triggered copy/export action; exclude credentials, cookies, tokens, and personal browsing data.
- [x] Review the current backup implementation and document its exact scope and exclusions in the manager UI and README-1.0.2.txt: ZIPs contain managed add-on files only, not manager settings, enabled state, game SavedVariables, login/session data, or the shared Gecko profile. New restored add-ons default to disabled; existing enabled state is retained.
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


## P2 — UI/UX consistency and responsive layouts

- [ ] Make Home orientation-adaptive: support portrait and landscape with layouts that rearrange cleanly for each available screen size; remove the forced-landscape restriction only when both layouts are ready.
- [ ] Make Addon Manager orientation-adaptive: provide a deliberate portrait layout as well as a landscape layout, with responsive navigation, lists, cards, dialogs, and controls.
- [ ] Keep Play/gameplay landscape-locked unless a separate product decision changes that requirement; verify transitions between Home, Manager, and Play do not cause awkward orientation jumps.
- [ ] Audit every screen (Home, Play, Addon Manager, Storage, updater and diagnostics when added) for consistent UI/UX, navigation, hierarchy, spacing, alignment, typography, and interaction feedback.
- [ ] Establish and apply one coherent visual theme across all screens: reuse the existing dark, frost-blue and gold palette; align backgrounds, surfaces, gradients/overlays, borders, elevation, and text contrast. Avoid unrelated colors or inconsistent styling.
- [ ] Standardize icons across the app: consistent visual family, stroke/fill treatment, size, alignment, tint, meaning, and accessible descriptions; replace mismatched or unclear icons.
- [ ] Review backgrounds on every screen and in dialogs/cards: use theme-consistent surfaces and image overlays, maintain legibility over imagery, and avoid visual noise or excessive contrast differences.
- [ ] Create/reuse shared styles, dimensions, colors, shapes, and drawable resources instead of duplicating screen-specific values; keep day/night behavior coherent if night resources are supported.
- [ ] Improve buttons, cards, text fields, menus, dialogs, progress/loading states, empty states, error states, and pressed/disabled/focus feedback so components behave and look consistently.
- [ ] Review portrait, landscape, small phones, large phones, tablets, cutouts, system bars, and edge-to-edge/inset handling; prevent clipped, overlapping, or unreachable content.
- [ ] Review touch target sizes, spacing, text scaling, contrast, TalkBack labels/order, keyboard/focus navigation, and other accessibility semantics.
- [ ] Test orientation changes, recreation, fullscreen, keyboard visibility, dialogs, back navigation, background/resume, and navigation between activities; preserve state and avoid layout regressions.
- [ ] Capture and review screenshots for key screens in portrait and landscape, then fix visual regressions before marking the UI/UX work complete.

- [x] Redesign the updater's checking, download-progress, verification, update-ready, and installer-handoff UI to match the dark, frost-blue and gold theme; improve hierarchy, spacing, typography, icon use, and primary/cancel actions. Error and up-to-date dialogs retain the shared dark/gold AppCompat dialog theme.
- [ ] Make updater dialogs and screens responsive in portrait and landscape, accessible, and consistent with Home and Addon Manager; clearly show version, download size/progress, checksum/verification status, and Android installer handoff without exposing technical clutter.
- [ ] Test updater UI states (checking, up to date, update available, downloading, verifying, failed/offline, incompatible build/signature, and installer handoff) with screenshots in both orientations.

## P3 — Release engineering code and CI

- [x] Add a GitHub Actions release-request workflow that validates `.github/release-request.json`, supports dry-run validation, creates annotated tags with `GITHUB_TOKEN`, and explicitly dispatches the existing signed build workflow; verify the dry-run on GitHub Actions.
- [x] Add CI safeguards ensuring debug-signed beta/demo artifacts are not accepted as main or tagged release assets; main/tag builds fail closed unless the permanent certificate matches.
- [x] Add CI validation for package ID, APK-embedded versionName/versionCode, ABI coverage, signer consistency, and SHA-256 checksums.
- [x] Keep release workflows compatible with the versioning and promotion policy in `AGENTS.md`.
- [x] Configure the tagged-release pipeline to attach one permanent-key-signed ARM64 APK and SHA256SUMS.txt to a draft GitHub Release; main CI artifacts remain temporary validation outputs.
- [x] Require the permanent release key and exact expected certificate on main/tag builds; missing secrets or fingerprint mismatch fail closed. A main-signed build remains to be verified after consolidation.
- [x] Replace static Android `versionCode` with the existing workflow's global `GITHUB_RUN_NUMBER`; local builds use a development-only fallback and are never update candidates.
- [x] Implement the published stable GitHub Release update checker; reject drafts, prereleases, malformed tags, missing/duplicate assets, and incompatible package/version/signing data.
- [x] Implement full-APK download for ARM64 (`arm64-v8a`) devices only; reject other device architectures; no universal fallback or differential updates.
- [x] Verify release APK size and SHA-256 against the published release checksum asset; reject missing/mismatched checksums and incomplete downloads.
- [x] Hand the verified release APK to Android's package installer with release version, versionCode, size and tag information; Android requires user confirmation and the app never silently installs or uninstalls.
- [x] Add JVM policy tests for published stable releases, draft/prerelease rejection, strict SemVer, ARM64-only selection, asset sizes, version ordering, checksums, and signer mismatch.
- [x] Document and enforce exact signing-certificate, package ID and version compatibility; incompatible candidates are blocked instead of prompting an uninstall.
- [x] Keep the updater to an Android package replacement only; it does not clear app data, cookies, login/session, GeckoView profile or add-ons. Physical verification remains in `MANUAL_CHECKLIST.md`.

## Updater verification boundary

The release-based updater implementation is in progress. Validate it against a published stable release, including asset discovery, checksum failure, malformed/draft/prerelease release rejection, package/version mismatch, unsupported ABI, and signer mismatch. Complete the same-certificate physical-device upgrade and data-preservation checks in `MANUAL_CHECKLIST.md`.

## Not active work

The following are not implementation tasks unless the owner explicitly reopens them:

- General-purpose browser features unrelated to Lasthavens.
- Replacing GeckoView without a technical case and migration plan.
- Analytics, tracking, push notifications, or background services.
- Differential APK patching.
- Automatic updates beyond the defined published-release-only, checksum-verified, same-certificate in-place update design.
- Resetting or wiping existing add-on data, or changing its format without a tested migration and rollback path.

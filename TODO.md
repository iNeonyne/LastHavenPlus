# LastHavenPlus — Active TODO

This is the single actionable work queue. The architecture and phase rationale live in [ROADMAP.md](ROADMAP.md). Do not create parallel checklists in other documents.

**Branch:** main only  
**Current source baseline:** GeckoView app with a debug-only Chromium WebView probe  
**Target:** Android System WebView + native-backed Addon Manager  
**Current bundled manager:** 1.0.5 (verified from its manifest)  
**App identity:** keep com.lasthavens.android, the permanent signing certificate, ARM64-only distribution and monotonic GITHUB_RUN_NUMBER versionCode.

The tasks below are ordered by dependency. Do not remove the Gecko migration source until the native manager import has been verified.

## P0 — 1. Baseline and contracts

- [ ] Record the baseline signed ARM64 APK size from a trusted main build; retain the artifact/run reference for final comparison.
- [ ] Inventory every manager persistence key, library field, IndexedDB file record, setting, catalogue mapping and diagnostic field used by manager 1.0.5.
- [ ] Inventory all current manager screens and operations; use the existing 1.0.5 behavior as the feature baseline, not the uploaded 0.5.1 archive.
- [x] Specify and version the migration-package schema: metadata, settings, folder IDs, versions, enabled state, dependency fields, relative file paths, bytes and SHA-256 checksums.
- [ ] Specify the local-manager bridge and game bridge message schemas, allowed origins, payload limits, error responses and timeout behavior.
- [ ] Define the native repository layout and staged-write/atomic-activation rules before implementing storage.
- [ ] Document the login/session boundary: Gecko cookies and WebView cookies are separate; do not claim automatic session migration.

## P0 — 2. One-time legacy data export/import

- [x] Add a Gecko-side exporter to the existing manager (now manager 1.0.6) that reads the library, settings and IndexedDB bytes without modifying source records.
- [x] Export manager state into a versioned migration package with file paths, byte sizes, per-file SHA-256 checksums and a metadata manifest.
- [ ] Implement a native importer with archive/path validation, strict byte/file/count limits, checksum verification and clear user-readable errors.
- [ ] Stage imported files separately, commit the metadata pointer only after verification, and roll back only the new generation on failure.
- [ ] Make migration idempotent: repeated starts/import attempts must not duplicate add-ons, downgrade versions, overwrite newer native data or mark incomplete work complete.
- [ ] Re-read the committed native library and compare file hashes, folder IDs, versions, enabled flags, dependencies, settings and library revision against the export.
- [ ] Add automated tests for empty libraries, many add-ons, large files, malformed metadata, traversal paths, mismatched hashes, interrupted writes, low storage, duplicate imports and rollback.
- [ ] Keep legacy Gecko profile data untouched until migration verification succeeds; do not clear app data as a migration step.

## P0 — 3. Production Chromium game screen

- [ ] Replace the debug-only ChromiumProbeActivity with the production PlayActivity backed by Android System WebView.
- [ ] Configure hardware acceleration, JavaScript, DOM storage, media, safe browsing, file access and mixed-content behavior with least privilege.
- [ ] Register document-start loader scripts before loading the game; require the supported AndroidX WebKit features and fail clearly if unavailable.
- [ ] Restrict WebMessageListener and injected scripts to the exact approved HTTPS Lasthavens origins; validate main-frame/source-origin on every message.
- [ ] Expose no general-purpose addJavascriptInterface or arbitrary native command API to the remote game page.
- [ ] Implement allow-listed navigation, external-link handling, target=_blank/popups, fullscreen, downloads, file chooser, back behavior and landscape gameplay orientation.
- [ ] Handle WebView renderer crashes, recreation, background/resume and network failures without corrupting manager storage.
- [ ] Verify WebGL 1/2, WebAssembly, SharedArrayBuffer/cross-origin isolation, workers, OffscreenCanvas, game 3D initialization, console errors and context-loss recovery through automated checks.
- [ ] Add automated tests for bridge rejection (wrong origin, subframe, malformed/oversized messages), navigation policy, popups, file chooser outcomes and renderer recovery.

## P0 — 4. Native-backed Addon Manager

- [ ] Reuse manager.html, manager.css, localization, icons and portable modules that remain useful; host the UI locally through WebViewAssetLoader.
- [ ] Restrict the manager's privileged bridge to its local app-assets origin; do not reuse that privileged bridge in the remote game WebView.
- [ ] Replace browser.storage.local/session, browser.runtime messaging, browser.permissions and browser.tabs with explicit native-backed adapters.
- [ ] Implement the native AddonRepository as the only source of truth for library metadata, settings and raw add-on files.
- [ ] Preserve ZIP import/export, archive validation, limits, TOC parsing, dependencies, load order, catalogue browse/search/filter, online installation, update-all, enable/disable, remove, backup, restore and pre-removal backup.
- [ ] Preserve current localization, language preference, badge setting, progress/status feedback, diagnostics and game-storage display where applicable.
- [ ] Implement SAF file selection and bounded streaming for imports/restores; distinguish user cancellation from permission, read, validation and storage failures.
- [ ] Route catalogue and add-on downloads through an explicit HTTPS host allow-list with redirect, size, timeout and archive validation.
- [ ] Ensure success is shown only after staged storage and metadata commit complete.
- [ ] Add automated regression tests for every manager operation, storage failure, network failure, interrupted import/update and recovery.

## P0 — 5. Port the current game add-on loader

- [ ] Port the 1.0.5 overlay/inject behavior to document-start WebView scripts; remove extension-only message transport.
- [ ] Implement the minimal read-only game bridge that returns the enabled add-on payload and accepts sanitized diagnostics only.
- [ ] Preserve dependency-first load order, file batching, built-in add-on conflict detection and revision tracking.
- [ ] Verify the current game's em-pthread worker wrapping and bootstrap timing before relying on the hook.
- [ ] Preserve XHR and Fetch interception, manifest extension, local file responses, HEAD/range handling, bundle masking and cached blocks.idx masking.
- [ ] Verify SharedArrayBuffer use where cross-origin isolation permits it and retain a bounded copy-transfer fallback.
- [ ] Report prepared, requested, loaded and failed add-ons accurately; never equate packaging with actual game loading.
- [ ] Add deterministic JavaScript tests for URL classification, range responses, manifest patching, worker bootstrap, payload transfer, conflict handling and diagnostics.
- [ ] Add integration tests with a controlled game fixture that simulates the expected worker/VFS requests; fail closed when expected hooks are missing.

## P0 — 6. Storage dashboard and state recovery

- [ ] Rework StorageUsage to report native add-on files, app-private persistent data, cache and WebView game-origin storage without Gecko labels or misleading game-only totals.
- [ ] Keep storage reporting read-only and clearly distinguish app storage from game-origin browser storage.
- [ ] Add integrity checks and recovery diagnostics for the native add-on repository.
- [ ] Ensure add-on activation, update, restore and remove operations preserve the previous committed generation until the new state is verified.
- [ ] Verify process death/restart and interrupted operations leave the native library consistent.

## P0 — 7. Preserve and verify the updater

- [ ] Keep the updater native and independent of all WebView/manager code.
- [ ] Preserve metadata-only checking: show the installed version and newer published stable versions; do not download an APK during discovery.
- [ ] Preserve explicit version selection, ARM64-only full APK download, SHA-256 verification, package/versionCode checks, signer equality, progress, and Android installer confirmation.
- [ ] Keep the updater from clearing app data or changing package ID, signing identity or versionCode sequencing.
- [ ] Add/retain regression tests for drafts, prereleases, malformed releases, missing/duplicate assets, redirects, incomplete downloads, checksum mismatch, unsupported ABI, package/version mismatch and signer mismatch.
- [ ] Verify the installer handoff and completion/error reporting do not report success before Android confirms the result.
- [ ] Keep the updater UI aligned with the new theme and responsive in portrait and landscape.

## P0 — 8. CI and build-system overhaul

- [ ] Remove GeckoView dependency and Mozilla Maven repository after the migration gate passes; remove geckoViewVersion.
- [ ] Update build metadata validation to use the native manager version/schema rather than the extension manifest and Gecko version.
- [ ] Replace extension-manifest validation with native manager asset, localization, archive-module and loader validation.
- [ ] Replace APK checks that require libxul.so or extension manifest assets with checks for the expected WebView manager assets and native components.
- [ ] Add a static guard that rejects Gecko dependencies, Gecko imports, Gecko activities/layouts and extension-runtime APIs from the final app.
- [ ] Add CI coverage for migration export/import, native storage, manager operations, WebView bridge security, loader behavior and updater policies.
- [ ] Add automated Android WebView integration/emulator coverage where supported by GitHub Actions; do not make physical-device testing a task for the owner.
- [ ] Measure final signed ARM64 APK size against the recorded baseline and confirm libxul.so is absent.
- [ ] Keep permanent-key verification, package ID, ARM64 output, monotonic versionCode, checksums and draft-only release creation unchanged.

## P1 — 9. Remove obsolete implementation and assets

- [ ] Remove GeckoRuntime/GeckoSession setup and App extension installation after migration is verified.
- [ ] Remove Gecko-only Delegates.kt, FilePicker.kt implementation, Gecko-specific ManagerActivity/PlayActivity code and activity_gecko layout after WebView replacements are complete.
- [ ] Remove Gecko WebExtension manifest, background runtime, isolated-world bridge and extension-only API adapters; retain only manager modules that the native host actually uses.
- [ ] Remove obsolete Gecko version metadata, Mozilla repository configuration and old profile-only code.
- [ ] Remove the debug Chromium probe once its production diagnostics are in place; avoid keeping two implementations of the same function.
- [ ] Remove duplicate/obsolete documentation and historical release-note assets from the APK; preserve required third-party licenses.
- [ ] Audit the final repository and packaged APK for unused resources, dependencies, permissions and duplicate code.
- [ ] Do not delete legacy app-private profile data during source cleanup.

## P1 — 10. UI/UX overhaul

- [ ] Make Home responsive in portrait and landscape, retaining the existing dark, frost-blue and gold theme.
- [ ] Make Storage responsive in portrait and landscape with accurate native/WebView storage labels.
- [ ] Make Addon Manager fully usable in portrait and landscape, including lists, dialogs, browse filters and import controls.
- [ ] Keep gameplay landscape-locked.
- [ ] Standardize icons, backgrounds, typography, cards, buttons, surfaces, spacing, progress indicators, empty states and error states across all screens.
- [ ] Make updater dialogs and progress screens responsive in both orientations.
- [ ] Audit system insets, edge-to-edge behavior, small/large screens, touch targets, TalkBack labels/order, text scaling, contrast and keyboard/focus navigation.
- [ ] Add automated screenshot/layout checks and UI-state tests for portrait and landscape.
- [ ] Preserve state and prevent clipped, overlapping or unreachable controls during orientation changes and activity recreation.

## P1 — 11. Final cleanup and release readiness

- [ ] Update README, PROJECT_OVERVIEW, ROADMAP and CHANGELOG to describe only the final WebView/native architecture and accurately identify migration limitations.
- [ ] Verify no roadmap or contributor instruction still directs development toward GeckoView, TWA or a bundled Chromium engine.
- [ ] Review the final manifest and remove permissions no longer required by the native architecture.
- [ ] Confirm the app uses the existing package ID and permanent signing certificate and remains upgrade-compatible.
- [ ] Choose the release SemVer based on the final user-visible/data-contract changes; do not change versionName merely for intermediate commits.
- [ ] Run all JavaScript, Kotlin, Python, Gradle, packaging, signing, checksum and release-metadata checks.
- [ ] Do not publish a release until all automated gates pass and the owner explicitly approves publication.

## Not in scope

- GeckoView as a final or fallback game engine.
- Bundled Chromium, TWA, Firefox TWA or general-purpose browser features.
- Analytics, tracking, push notifications or background services.
- Differential APK updates.
- Unrelated frameworks, dependencies or UI features.
- Automatic deletion of existing app data or the legacy profile.

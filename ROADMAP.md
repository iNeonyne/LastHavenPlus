# LastHavenPlus — Overhaul Roadmap

## Product decision

LastHavenPlus is being rebuilt around **Android System WebView (Chromium)** and a **native-backed Addon Manager**.

This is the final architecture, not an experiment or a dual-engine product:

- Use Android System WebView supplied by the device. Do not package a Chromium browser engine in the APK.
- Do not use Trusted Web Activity (TWA).
- Do not retain GeckoView as a gameplay fallback.
- Keep the native Android Home, Storage, updater, and application shell.
- Keep the existing package ID, permanent signing identity, ARM64 distribution, stable-release updater and monotonic versionCode sequence.

The current main branch is still the Gecko-based implementation with a debug-only Chromium probe. The roadmap describes work still to be implemented; it does not claim that the migration is complete.

## Target architecture

### Android shell

Kotlin owns navigation, lifecycle, permissions, file selection, downloads, storage, update discovery and installation, and recovery from WebView renderer failures.

### Game

A dedicated, hardware-accelerated Android WebView loads https://lasthavens.com/play. AndroidX WebKit supplies document-start JavaScript injection and an origin-restricted WebMessageListener. Navigation remains HTTPS-only and restricted to approved Lasthavens origins; external links go to Android.

The game receives only the add-on data and diagnostics needed by the loader. The remote game page must not receive a general-purpose native command interface.

### Addon Manager

Keep the existing manager's feature set and visual language, but remove its dependency on a browser extension runtime. Reuse its HTML/CSS and portable JavaScript modules where practical, and host the manager locally with WebViewAssetLoader. Its storage and privileged operations move to Kotlin-backed app-private storage and narrowly scoped messages from the local manager origin.

The native repository becomes the source of truth. It stores add-on files and metadata using staged writes and atomic activation so a failed import or update never destroys the last working generation.

### Add-on game integration

Port the current 1.0.5 loader, not the older uploaded 0.5.1 implementation. Preserve and test its enabled-add-on ordering, payload batching, document-start bootstrap, em-pthread worker wrapping, XHR and Fetch interception, game-manifest extension, byte-range handling, built-in bundle masking, cached blocks.idx handling and diagnostics. The existing game hooks are implementation assumptions until confirmed by automated integration checks against the current game.

### Data migration and session boundary

The current manager stores library metadata/settings in extension storage and add-on bytes in IndexedDB inside the Gecko profile. The new manager uses native app-private storage. A one-time export/import bridge is therefore required before deleting Gecko-specific code:

1. Add a migration exporter to the existing Gecko-backed manager while its storage APIs are still available.
2. Export a versioned package containing add-on bytes, file paths, folder IDs, versions, enabled state, dependency metadata, manager settings, language/preferences, library revision and per-file checksums.
3. Import into the native repository using size/path validation, staging, verification and rollback.
4. Confirm the imported library matches the export before the Gecko runtime and extension code are removed.

This is a **temporary migration mechanism only**. The final application must not contain or run GeckoView. Do not erase the old Gecko profile as part of code cleanup; it may contain data outside the manager export.

Gecko and WebView cookie/profile stores are separate. Do not promise login/session transfer. The new WebView game session may require the user to sign in again; state this clearly in migration/release notes.

### Updater

The updater remains native Kotlin and independent of the browser engine. Preserve metadata-only discovery, the installed version plus newer published stable releases, explicit version selection, full ARM64 APK download, SHA-256 verification, package/versionCode/ABI/signing-certificate checks, progress reporting and Android's user-confirmed installer handoff. An update must never clear app data or silently install an APK.

## Delivery phases

### Phase 0 — Freeze contracts and establish a baseline

Inventory the current 1.0.5 manager schema, settings, add-on storage, loader protocol, Android navigation, release metadata and CI assumptions. Record a trusted baseline APK size and existing test results. Define the migration package schema and WebView bridge message contracts before implementation.

**Exit gate:** every persisted manager field and every current manager feature has an identified destination; the bridge contracts are documented and testable.

### Phase 1 — Data migration bridge

Implement and test the Gecko-side exporter and native importer. Migration must be repeatable, idempotent and non-destructive. An incomplete or invalid import must leave the original Gecko data and the last committed native library untouched.

**Exit gate:** fixtures covering empty libraries, real-shaped records, multiple add-ons, settings, corrupt archives, size limits, interrupted writes and rollback pass automated tests. Export/import checksums and metadata match.

### Phase 2 — Chromium game host

Replace the debug probe with the production WebView game screen. Implement secure settings, document-start registration, trusted-origin message handling, navigation, popups, fullscreen, downloads, file selection, orientation, lifecycle handling and renderer recovery.

**Exit gate:** the actual game initializes its 3D renderer in WebView; WebGL, WebAssembly, workers, isolation requirements, input, fullscreen and recovery checks pass the available automated test suite. A capability-only probe is not sufficient.

### Phase 3 — Native-backed Addon Manager

Reuse the existing manager UI and portable logic. Replace extension APIs, permissions, storage, tabs and runtime messaging with the local manager host and Kotlin repository. Keep import, export, backup, restore, browse/catalogue, install/update, enable/disable, dependency display/order, remove, scan, status, diagnostics, localization and settings.

**Exit gate:** every existing manager operation has automated coverage and runs without a Gecko or browser-extension runtime.

### Phase 4 — Loader integration

Connect the native repository to the game WebView through the restricted read-only game bridge. Port the 1.0.5 loader and verify worker bootstrap ordering, payload transfer, local file responses, manifest behavior, ranges, cache masking and diagnostics.

**Exit gate:** automated tests prove enabled add-ons are prepared and requested by the game, and failed loader operations cannot mutate or corrupt the stored library.

### Phase 5 — Migration activation

Run the one-time exporter/importer as part of the transition path. Verify file hashes, folder identities, versions, enabled state, dependencies, settings and library revision. Keep a recoverable copy of the legacy profile data; never treat a successful app install as proof that migration succeeded.

**Exit gate:** migration is marked complete only after native records are re-read and verified. The app can operate entirely from native storage after restart.

### Phase 6 — Remove obsolete engine and extension infrastructure

After Phases 1–5 pass, remove GeckoView dependencies, Mozilla Maven configuration, GeckoRuntime/GeckoSession code, Gecko delegates, Gecko-only activities/layouts, Gecko version properties and extension-runtime-only APIs. Retain only manager assets/modules that the local manager uses. Remove obsolete manifests, background workers, extension bridges, historical packaged release notes, debug-only probe code once production diagnostics replace it, and duplicate documentation.

Do not delete the legacy profile directory or other app data during source cleanup.

**Exit gate:** static CI checks find no Gecko dependencies or runtime references in the final app; no extension-runtime-only files are packaged; the application builds and runs through WebView and native storage alone.

### Phase 7 — Updater and build-pipeline regression

Keep updater behavior unchanged while replacing its old metadata assumptions. Remove CI checks that require Gecko libraries or the extension manifest; add checks for the local manager bundle, WebView integration, bridge origin policies, data migration and absence of Gecko. Measure final APK size against the baseline and confirm Gecko native libraries are absent.

**Exit gate:** updater policy tests and installer-handoff tests pass; signed ARM64 APK metadata/checksums remain valid; measured APK size is recorded.

### Phase 8 — UI and reliability pass

Bring Home, Storage, Addon Manager and updater layouts into one responsive design system. Home, Storage and Addon Manager support portrait and landscape. Gameplay remains landscape. Standardize icons, backgrounds, cards, dialogs, progress, empty/error states, typography, touch targets, insets and accessibility.

**Exit gate:** automated screenshot/layout checks and UI-state tests cover both orientations and small/large display sizes; no screen relies on Gecko-only UI.

### Phase 9 — Release readiness

Update user documentation, migration notices, changelog and release metadata. Confirm package ID, signing identity, ARM64-only output, versionCode continuity, updater compatibility, data safeguards, distribution terms and draft-release workflow.

Do not publish a release without explicit owner approval.

## Permanent exclusions

Do not add a bundled Chromium engine, TWA wrapper, Gecko fallback, general-purpose browser features, analytics, tracking, push notifications, background services, differential APK updates or unrelated frameworks. Keep dependencies and packaged assets limited to the native app, WebView manager, game loader, updater and required licenses.

## Acceptance criteria for the overhaul

- The game runs using Android System WebView and renders its 3D scene.
- The Addon Manager works without WebExtension APIs or GeckoView.
- Existing add-ons and manager settings are migrated and verified before legacy engine code is removed.
- The loader retains the current 1.0.5 capabilities and is covered by regression tests.
- The updater remains selectable, checksum-verified, signer-safe and independent of WebView.
- Home, Storage and Addon Manager work in portrait and landscape; gameplay remains landscape.
- CI proves no Gecko runtime/dependency remains, reports APK size, and produces a signed ARM64 build with the unchanged package ID and certificate.
- No app data is cleared and no release is published without approval.

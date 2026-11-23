# LastHavenPlus — Roadmap

## Approved architecture

The app uses **Android System WebView (Chromium)** for gameplay and a **native-backed Addon Manager**. GeckoView, TWA and bundled Chromium are excluded.

- Native Kotlin shell, Home, Storage and updater.
- System WebView loads `https://lasthavens.com/play`; the device supplies Chromium.
- AndroidX WebKit document-start scripts and origin-restricted WebMessageListener.
- Native app-private add-on repository with staged writes and atomic generation activation.
- A read-only game bridge supplies enabled add-on files in bounded binary chunks.
- Keep application ID, permanent signing identity, ARM64 distribution and monotonic versionCode.

## Current implementation

- Production WebView PlayActivity is in place.
- GeckoView dependency, runtime, extension host, Mozilla Maven repository, Gecko-only files and extension-runtime assets have been removed.
- The old Gecko profile is not deleted or modified by source cleanup. It remains opaque to the new app.
- A one-time migration ZIP exporter exists in the old app's Addon Manager 1.0.6; the new app imports and verifies that package.
- Native repository activation and atomic enable/disable are implemented.
- Native manager lists imported add-ons and allows enable/disable.
- Native game bridge and existing 1.0.6 overlay/inject scripts are connected.
- Updater remains independent and unchanged in behavior.

## Migration warning

Users must export a migration package from the Gecko-based app before installing a Gecko-free build. The new app cannot read the old Gecko profile. The app update does not clear the old profile, but no in-app recovery path remains once GeckoView is removed. Cookies and login state are not migrated; sign-in may be required again.

## Delivery priorities

### P0 — Finish and validate the WebView game host
- Verify the current WebView provider supports document-start scripts, WebMessageListener and ArrayBuffer messaging.
- Verify WebGL 1/2, WebAssembly, workers, SharedArrayBuffer/isolation requirements, input, fullscreen and renderer recovery.
- Verify the live game's `em-pthread`, VFS, manifest, XHR/Fetch, range and cache-masking hooks.
- Add integration fixtures for worker bootstrap and VFS requests; prove actual add-on files are requested, not merely prepared.
- Test WebView storage persistence across restart, process death and same-signature APK update.

### P0 — Complete native manager essentials
- Re-read active generations and verify hashes/metadata after activation.
- Add interruption, low-storage, duplicate import and rollback tests.
- Complete ZIP import/export, remove, backup/restore and clear status/error reporting.
- Ensure manager bridge (if introduced) is limited to the local app-assets origin; never expose it to the remote game.
- Add integration tests for migration import, list, enable/disable and game handoff.

### P0 — Build and packaging verification
- CI must reject Gecko dependencies, Gecko runtime references and `libxul.so` in the APK.
- Verify only required loader assets are packaged.
- Measure APK size against the old 1.0.1 Gecko baseline.
- Keep signing, package ID, ABI and updater metadata checks unchanged.

### P1 — Manager feature parity
- Catalogue browsing/search/filter and online install/update.
- ZIP backup/restore and pre-removal backup.
- Remove add-ons, dependency details and load-order diagnostics.
- Localization, preferences, badge setting, progress and diagnostics.
- SAF import/export and bounded streaming for all archive operations.

### P1 — Storage and UI
- Distinguish WebView origin storage, native add-on files, app-private data and evictable cache.
- Keep Home, Manager and Storage responsive in portrait and landscape; gameplay remains landscape.
- Complete accessibility, insets, touch target, error/empty state and visual consistency checks.

### Release readiness
- Update migration instructions, changelog and release metadata.
- Confirm distribution terms and permanent signing key custody.
- Keep tagged builds as drafts. Never publish without explicit owner approval.

## Non-goals

No bundled browser engine, TWA, Gecko fallback, general-purpose browser features, analytics, tracking, push notifications, background services or unrelated frameworks.

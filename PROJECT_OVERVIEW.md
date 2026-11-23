# LastHavenPlus — Technical Overview

## Product identity

- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Application ID: `com.lasthavens.android`
- Active branch: `main`
- App version name: `1.0.1`
- Distribution: signed ARM64 APKs through GitHub Releases
- Engine: Android System WebView supplied by the device

## Current source implementation

- `HomeActivity` provides Play, native Addon Manager, Storage and updater entry points.
- `PlayActivity` hosts the game in persistent Android System WebView.
- `GameAddonBridge` uses AndroidX WebKit origin-restricted messaging and bounded binary chunks.
- `GameAddonPayload` projects only enabled add-ons and orders dependencies before dependents.
- `ManagerActivity` lists imported add-ons and commits enable/disable changes through atomic repository generations.
- `MigrationImportActivity` validates the versioned 1.0.6 migration ZIP, verifies checksums and activates it in native storage.
- `AddonRepository` is the source of truth for the active native library.
- `StorageActivity` reports app-private storage and cache separately.
- The updater remains native and independent of WebView.

**GeckoView, GeckoRuntime, GeckoSession, browser-extension runtime code, Mozilla Maven configuration and Gecko native libraries have been removed from the current source.** Old Gecko profile files on existing devices are not deleted by source cleanup or app update. The new app cannot read those files; export the migration ZIP from the old app before upgrading.

## Verified boundaries and remaining work

The native importer, repository, payload projection, navigation policy and updater have automated tests. CI verifies the APK and signing identity.

The document-start loader is connected to the native bridge, but the actual game worker/VFS hook timing, WebGL startup and end-to-end add-on loading still require integration verification. Do not claim full game/add-on compatibility based only on a successful APK build.

The native manager currently covers migration import, listing and enable/disable. Catalogue browsing, online installation/update, ZIP backup/restore, removal, advanced settings and full feature parity remain queued.

## Persistent data

WebView owns a separate Chromium profile. Routine navigation, activity recreation and same-signature app updates must not clear cookies, IndexedDB, Cache Storage or service-worker data. The legacy Gecko profile is separate and is retained as opaque old app data; no login/session transfer is assumed.

## Build and updater invariants

- Keep application ID `com.lasthavens.android` and the permanent signing certificate.
- Keep ARM64-only output and monotonic `GITHUB_RUN_NUMBER` versionCode.
- Main artifacts are validation builds, not updater sources.
- The updater shows the installed version and newer published stable releases; the user selects a version before download.
- Verify size, SHA-256, package ID, versionName/versionCode, ABI and exact signer before Android installer handoff.
- Never clear app data or silently install an APK.
- Tagged builds create draft releases only. Publication requires explicit owner approval.

## Related documents

- [ROADMAP.md](ROADMAP.md) — target architecture and phase gates.
- [TODO.md](TODO.md) — active implementation queue.
- [AGENTS.md](AGENTS.md) — contributor rules.
- [README.md](README.md) — build and migration notice.
- [CHANGELOG.md](CHANGELOG.md) — shipped changes only.

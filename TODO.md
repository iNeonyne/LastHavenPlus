# LastHavenPlus — Active TODO

**Branch:** `main` only  
**Architecture:** Android System WebView + native Android Addon Manager  
**Identity:** preserve `com.lasthavens.android`, permanent signing certificate, ARM64-only output and monotonic `GITHUB_RUN_NUMBER` versionCode.

## P0 — Game host and loader

- [x] Replace Gecko gameplay with Android System WebView.
- [x] Configure persistent WebView site storage without clearing it during routine lifecycle/update.
- [x] Register origin-restricted document-start loader scripts and WebMessageListener before loading the game.
- [x] Add a read-only native game bridge with bounded 512 KB binary chunks.
- [x] Filter disabled add-ons and preserve dependency-first order in the payload.
- [ ] Verify required AndroidX WebKit features on supported providers and fail with a clear compatibility message.
- [ ] Verify live-game WebGL, WebAssembly, workers, SharedArrayBuffer/isolation, input, fullscreen and renderer recovery.
- [ ] Verify live-game `em-pthread`, `WebVfsStoreIo`, manifest, XHR/Fetch, range and `blocks.idx` hooks.
- [ ] Add controlled worker/VFS integration tests proving the game requests enabled add-on files.
- [ ] Add bridge tests for wrong origin, malformed/oversized messages, invalid paths, stale generations and chunk bounds.
- [ ] Verify IndexedDB/Cache Storage survives process death, restart and same-signature APK update.

## P0 — Native Addon Manager and migration

- [x] Keep the old 1.0.6 migration exporter available in the previous app version.
- [x] Validate migration archive paths, limits, metadata and SHA-256 checksums.
- [x] Activate imported data as a separate native generation.
- [x] Preserve the previous active generation on failed activation.
- [x] Add native add-on listing and atomic enable/disable.
- [ ] Verify the committed native library by re-reading every file and comparing hashes, folder IDs, versions, enabled flags, dependencies, settings and revision.
- [ ] Add idempotent migration tests for repeated import, downgrade prevention and duplicate IDs.
- [ ] Add tests for interrupted writes, low storage, malformed archives and rollback.
- [ ] Add native ZIP import/export, remove, backup and restore.
- [ ] Add catalogue browse/search and online install/update.
- [ ] Add dependency display, settings/localization, progress and detailed error states.
- [ ] If a local WebView manager UI is introduced, serve it through WebViewAssetLoader and scope privileged messaging only to that exact local origin.

## P0 — Gecko removal and package verification

- [x] Remove GeckoView Gradle dependency and Mozilla Maven repository.
- [x] Remove Gecko runtime/session/extension code and Gecko-only file picker/delegates.
- [x] Remove Gecko-only activity layout and debug Chromium probe.
- [x] Remove obsolete browser-extension runtime assets; retain only overlay/inject scripts needed by the WebView loader.
- [x] Preserve old profile data by not clearing app-private data during source cleanup or app updates.
- [ ] Add CI static guard against Gecko dependencies/runtime references.
- [ ] Verify packaged APK contains no `libxul.so`, GeckoView classes or extension-runtime assets.
- [ ] Measure APK size against the old Gecko baseline and record the reduction.

## P0 — Updater and release safety

- [x] Keep the updater native and independent of browser-engine code.
- [ ] Preserve regression coverage for stable-only releases, explicit version selection, APK size/hash, package/versionCode, ABI and signer validation.
- [ ] Confirm tagged builds create drafts only; never publish a release without explicit owner approval.
- [ ] Keep package ID, permanent signing identity, ARM64-only output and monotonic versionCode unchanged.

## P1 — UI and reliability

- [ ] Finish responsive Addon Manager, Storage and Home layouts in portrait and landscape; keep gameplay landscape.
- [ ] Distinguish WebView origin data, native add-on files, app-private data and evictable cache.
- [ ] Add accessibility, insets, touch-target and small/large-screen checks.
- [ ] Complete manager diagnostics and clear recovery/error messaging.

## Migration warning

Before installing a Gecko-free build over a Gecko-based version, export the manager migration ZIP from Addon Manager 1.0.6 and save it outside app storage. The new app cannot read the old Gecko profile. The profile is not deleted, but it is no longer accessible through the app. Login cookies/session are not migrated.

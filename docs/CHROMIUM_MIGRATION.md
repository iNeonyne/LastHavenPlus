# Chromium migration

## Owner decision

Chromium is now the selected and primary engine for LastHavenPlus. Remove GeckoView from the app; do not maintain a dual-engine product. The target is Android System WebView, supplied by the device's installed WebView provider. Do not bundle Chromium and do not use TWA.

The Gecko engine/profile must not be reset or explicitly deleted as part of this migration. Removing the Gecko dependency is not permission to erase its existing app-private data. Preserve the package ID com.lasthavens.android, signing identity, app data directory, versionCode policy, and native updater.

## Expected APK impact

Removing the GeckoView dependency and its native runtime payload should materially reduce APK size. WebView itself is provided by Android and is not packaged into this APK. Actual size reduction must be measured from a signed ARM64 build; do not publish an estimate as a measured result. The app will depend on a compatible Android System WebView provider being installed and updated on the device.

## Existing implementation audit

- PlayActivity currently hosts a long-lived GeckoSession.
- ManagerActivity currently hosts the bundled Gecko WebExtension.
- App.kt creates the shared GeckoRuntime and installs the extension.
- The bundled Addon Manager is version 1.0.5 in app/src/main/assets/extensions/lh-addon-manager.
- The current manager includes ZIP validation, TOC/dependency parsing, catalogue browsing, import/export, staged IndexedDB storage, XHR/Fetch interception, worker bootstrap, range support, cached-index masking and diagnostics.
- The uploaded 0.5.1 Chrome extension is only a reference. Do not downgrade to it.
- Native Home, Storage and updater screens are independent of Gecko and should remain native.

## Implementation sequence

1. Replace the Gecko game screen with a production WebView screen using hardware acceleration, secure defaults, origin-restricted AndroidX WebKit document-start injection, and a secure message bridge.
2. Port the current 1.0.5 manager UI and behavior to the native/WebView architecture. Keep the existing ZIP, catalogue, dependency, validation, staged-write and diagnostics logic wherever possible. Replace extension-only APIs (browser.storage, runtime messaging, permissions and tabs) with explicit Android-backed adapters.
3. Move add-on file persistence to app-private storage accessible to both the manager and the game loader. Preserve folder IDs, files, versions, enabled state, dependency metadata, manager settings and library revision. Use atomic/staged writes and rollback.
4. Port the game integration: payload delivery, XHR/Fetch interception, manifest patching, worker bootstrap, range responses, cached blocks.idx masking and loader diagnostics. Do not expose a general-purpose JavaScript interface to the remote game page.
5. Replace Gecko prompt/download/fullscreen/process lifecycle handling with WebView equivalents: file chooser, download listener, popup policy, fullscreen, external navigation and renderer recovery.
6. Remove GeckoView imports, activities/delegates, layouts, Gradle dependency and Gecko-only version configuration once the Chromium game and manager paths compile and their automated checks pass. Do not delete the old profile directory or unrelated app data.
7. Run CI and measure the resulting ARM64 APK size. Keep the updater implementation and its selection, checksum, package/version/ABI and signing-certificate checks unchanged; add regression checks that the updater still builds and its policies pass.

## Game compatibility requirements

The black screen occurs when 3D rendering starts. The WebView implementation must be checked for:

- WebGL 1 and WebGL 2 context creation and actual game rendering.
- WebAssembly, SharedArrayBuffer/cross-origin isolation and worker startup.
- Console errors, rejected promises and WebGL context loss.
- The current game's em-pthread worker and WebVfsStoreIo integration.
- Fullscreen, landscape orientation, keyboard, file chooser, downloads, popups, external links and renderer recovery.

The existing debug-only Chromium probe is a diagnostic starting point, not proof that the production game or add-on loader works.

## Data and release safety

- Do not clear app data, change the package ID, change the signing identity, or change the updater's release-selection policy.
- Do not silently claim Gecko cookies or login sessions have migrated. WebView and Gecko cookie stores are separate; session continuity must be assessed and clearly documented.
- Keep existing Gecko profile files untouched even after the Gecko runtime dependency is removed.
- Preserve current manager data through an explicit, tested export/import or migration. The existing ZIP backup contains add-on files only and does not include every manager setting or enabled-state field.
- Do not publish a release until the Chromium game path, manager operations, storage migration/rollback, automated tests and updater regression checks pass.
- Do not ask the owner to perform physical-device testing. Use CI, source-level tests and available automated validation; report any remaining device-only uncertainty honestly.

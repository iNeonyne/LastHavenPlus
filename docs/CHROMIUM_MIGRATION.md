# Chromium migration investigation

## Decision

The project owner has directed that Chromium be evaluated as the replacement game engine because the game reaches a black screen when 3D rendering starts. The intended target is Android System WebView embedded in the app, not a bundled Chromium binary and not TWA. TWA remains a comparison option, but it does not provide the same control over document-start injection and game request handling.

This document records the first source audit. It is not authorization to remove GeckoView before the replacement path and data migration are verified.

## Existing app architecture

- `PlayActivity` attaches a long-lived, app-owned `GeckoSession` to `GeckoView`.
- `ManagerActivity` creates a separate manager session on the same `GeckoRuntime`.
- The bundled Addon Manager is a Gecko WebExtension. It uses `browser.storage.local` for library metadata and IndexedDB-backed storage generations for add-on file bytes.
- The game and manager therefore share the Gecko profile, including website storage and cookies/session state.
- `App.kt` installs the extension into the Gecko runtime. Replacing the game view alone would not make the manager or its storage portable.

## Uploaded 0.5.1 archive audit

The uploaded `lasthavens-addon-manager-0.5.1.zip` is a Chrome Manifest V3 extension (minimum Chrome 120), not a standalone Android app or a TWA package.

Its main pieces are:

- `archive.js`: ZIP inspection/extraction, path checks, CRC validation, TOC discovery and ZIP export.
- `library.js` / `repository.js`: add-on library operations and persistence through `chrome.storage.local`.
- `manager.html` / `manager.js`: browser extension manager UI.
- `bridge.js`: same-origin `postMessage` bridge between isolated extension context and game page, with diagnostics sent back to extension storage.
- `runtime-core.js` / `loader.js`: XHR interception, local add-on file responses, game-manifest extension and an `em-pthread` worker bootstrap that installs the loader before importing the game's worker.

The 0.5.1 archive stores file bytes as Base64 values in one extension-storage library object. It is an earlier and simpler implementation than the manager currently bundled in LastHavenPlus.

## Existing manager capabilities that should be ported

The current bundled manager is version 1.0.5 and is more advanced than the uploaded 0.5.1 archive:

- `overlay.js` builds a compact add-on memory package and patches both XHR and Fetch.
- `inject.js` bootstraps the game's `em-pthread` worker and contains diagnostics for prepared add-ons, requested files and worker activity.
- The current implementation batches add-on file reads instead of sending the entire library in one message.
- `archive.js`, `library.js`, TOC/dependency parsing, limits, catalogue support and staged IndexedDB storage are already implemented in the current extension.

Do not replace the current manager with 0.5.1. Use the archive as a reference for its simpler Chrome bridge and worker bootstrap, while porting the current 1.0.5 behavior and data model.

## Proposed Chromium architecture

1. Use Android System WebView for the game. It is supplied/updated by the device's WebView provider; do not bundle a second Chromium runtime.
2. Keep the app's native Home, Storage and updater screens.
3. Build a native Android Addon Manager backed by app-private storage. Reuse the current manager's ZIP validation, TOC/dependency logic, catalogue behavior, add-on loader and diagnostics where practical.
4. Inject the game loader at document start using an origin-restricted WebView integration. Prefer AndroidX WebKit document-start scripts and origin-scoped web message APIs. Do not expose a general-purpose `addJavascriptInterface` to the remote game page.
5. Preserve the loader's actual game integration: worker bootstrap, XHR and Fetch handling, manifest patching, range responses, cached-index masking and diagnostics. Verify every hook against the live game's current implementation before relying on it.
6. Keep all existing Gecko-backed screens and storage readable until the Chromium replacement and explicit migration path are ready.

## Migration gates

### Gate A — Chromium game compatibility

- Confirm WebView provider/version and hardware acceleration.
- Run a diagnostic page in the game origin to record WebGL 1/2 context creation, WebAssembly, SharedArrayBuffer/cross-origin isolation, worker startup, console errors and WebGL context-loss events.
- Confirm whether the black screen is fixed before migrating manager storage.
- Verify fullscreen, orientation, downloads, file picker, popups, external links, navigation and process recovery.

### Gate B — add-on loader compatibility

- Confirm the game still uses the expected `em-pthread` worker and `WebVfsStoreIo` integration.
- Confirm document-start injection occurs before game scripts and that the worker bootstrap executes in WebView.
- Port and test both XHR and Fetch interception, range handling, manifest additions and cached `blocks.idx` masking.
- Keep diagnostics visible and fail closed: a loader error must never corrupt game files or the add-on library.

### Gate C — data migration

- Preserve add-on folder IDs, files, versions, enabled state, dependency metadata, manager settings and library revision.
- Design an explicit export/import format from the current Gecko manager to the native manager. The existing ZIP backup contains add-on files only; it does not by itself prove preservation of enabled state or settings.
- Investigate whether Gecko website cookies and login/session state can be safely and completely transferred to WebView. Do not assume cookie stores are interchangeable.
- Do not delete, reset, overwrite or abandon the Gecko profile during migration. Keep the old manager/data path available for rollback until the new library is verified.
- If login/session portability cannot be proven, document the exact limitation before changing the default engine; do not silently claim it is preserved.

### Gate D — release readiness

- Preserve package ID `com.lasthavens.android`, the permanent signing identity, monotonic versionCode and selectable stable-release updater.
- Confirm the APK does not accidentally include a second bundled browser engine.
- Add automated tests for loader behavior, storage migration, rollback and existing updater/signature invariants.
- Do not publish a release until automated checks and the required migration verification are complete.

## First audit conclusion

The project already contains a newer and more capable version of the uploaded add-on loader. The major work is not writing a ZIP manager from scratch; it is moving the current extension's privileged browser APIs and storage model into a safe native/WebView architecture, then proving WebGL and worker compatibility without losing Gecko profile data.

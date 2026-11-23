# LastHavenPlus — Technical Overview

## Product identity

LastHavenPlus is an unofficial, focused Android client for https://lasthavens.com/play. It is not a general-purpose browser.

- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Application ID: com.lasthavens.android
- Active branch: main
- Distribution: ARM64 APKs through GitHub Releases; no Google Play track
- Source app version: 1.0.1; v1.0.0 is the first public release
- Bundled Addon Manager source version: 1.0.5 (from its current manifest)

## Current checked-in implementation

The main branch is still a transition baseline, not the final architecture:

- HomeActivity provides Play, Manager, Storage and update entry points.
- PlayActivity currently uses GeckoView and an app-owned GeckoSession.
- ManagerActivity hosts the bundled Gecko WebExtension from app/src/main/assets/extensions/lh-addon-manager.
- The manager stores library metadata/settings in extension storage and add-on file bytes in IndexedDB under the Gecko profile.
- StorageUsage currently measures app/profile files and cache; some labels and accounting must be adapted to the native/WebView storage design.
- Home, Storage, Manager, navigation, downloads and updater are native Android/Kotlin components where applicable.
- ChromiumProbeActivity is debug-only and uses Android System WebView. It is diagnostic scaffolding, not the production game screen.
- The native updater reads published stable GitHub Releases and verifies the selected ARM64 APK before Android installer handoff.

The checked-in code and build workflow still include GeckoView and extension-manifest assumptions. Do not describe the final migration as complete until the dependency, source references, packaged libraries and CI checks have been removed and verified.

## Approved target architecture

The final design is Android System WebView plus a native-backed Addon Manager:

1. **Android shell:** Kotlin owns app navigation, lifecycle, permissions, native file selection, downloads, storage and updates.
2. **Game WebView:** hardware-accelerated System WebView loads the Lasthavens game. AndroidX WebKit injects loader scripts at document start and exposes a narrowly scoped, read-only add-on payload bridge restricted to approved game origins.
3. **Manager WebView:** local manager assets are hosted with WebViewAssetLoader. The existing manager UI and portable modules are reused where practical; extension-only APIs are replaced by a local-origin bridge to Kotlin.
4. **Native repository:** Kotlin app-private storage owns add-on files, library metadata and preferences. Writes use staging, verification and atomic activation.
5. **Loader:** the current manager 1.0.5 loader behavior is ported and tested: dependency order, batching, em-pthread bootstrap, XHR/Fetch interception, manifest extension, range handling, built-in bundle masking, cached blocks.idx handling and diagnostics.
6. **Updater:** remains native and independent of WebView and manager storage.

Do not use TWA, bundle Chromium, or keep GeckoView as a gameplay fallback.

## Migration boundaries

The old manager data is inside the existing Gecko profile. Before Gecko code is removed, a temporary one-time exporter must read the real manager library, preferences and IndexedDB files. A native importer must validate and commit that data before the legacy manager is retired.

Preserve add-on bytes/paths, folder IDs, versions, enabled state, dependency metadata, library revision and relevant manager preferences. Keep the old profile data untouched during source cleanup; removing Gecko code is not permission to erase app-private data.

Gecko and WebView cookie/profile stores are separate. Login/session transfer is not assumed; users may need to sign in again. Existing file-only ZIP backups are not complete manager-state backups.

## Build and updater invariants

- Keep application ID com.lasthavens.android and the existing permanent signing certificate.
- Keep ARM64-only release output and monotonic versionCode from GITHUB_RUN_NUMBER.
- Main APK artifacts are validation builds, not update candidates.
- The updater lists the installed version and newer published stable versions without downloading during discovery. The user selects a version before download.
- Verify asset size, SHA-256, package ID, versionName/versionCode, ABI and exact signer equality before handing the APK to Android's user-confirmed installer.
- Never clear app data or silently install/uninstall an APK.
- Tagged builds create draft releases only; publication requires explicit owner approval.

## Current verification boundary

Automated CI can validate code contracts, unit tests, JavaScript modules, migration fixtures, WebView bridge policy, APK contents, signing, metadata and checksums. It cannot by itself prove behavior on every device's installed WebView provider or physical GPU. Prefer repeatable CI/emulator coverage and report unsupported or unverified capabilities accurately.

## Related documents

- [ROADMAP.md](ROADMAP.md) — final architecture, delivery phases and acceptance gates.
- [TODO.md](TODO.md) — the only actionable implementation queue.
- [AGENTS.md](AGENTS.md) — mandatory contributor/agent rules.
- [README.md](README.md) — concise project information and build instructions.
- [CHANGELOG.md](CHANGELOG.md) — shipped user-visible changes.

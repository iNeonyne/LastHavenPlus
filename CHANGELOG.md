# Changelog

This file records notable user-visible changes. It is not the development task list; see [TODO.md](TODO.md) for current work. Unreleased entries describe changes present on the development branch and are not release claims.

## [Unreleased] — development

### Added
- Read-only storage dashboard for shared GeckoView/app persistent data, browser/app cache, and available device storage; distinguishes current storage from lifetime downloads.
- Native JavaScript alert, confirm, and prompt dialogs.
- Landscape-specific Home screen layout.
- ARM64-only release APK output and updater support.
- A user-triggered updater on main that considers successful main-branch builds only, verifies artifact provenance, metadata, SHA-256, package/version and signing-certificate compatibility, then hands installation to Android.

### Changed
- Home screen now uses sensor landscape orientation.
- Removed the thin top-edge game loading-progress line while retaining the LH diagnostics badge and diagnostic logging.
- GitHub Actions produces the permanent-key-signed ARM64 update artifact and metadata on `main`; pull-request and manual test builds are not updater candidates.

### Security
- Restricted in-app navigation to HTTPS Lasthavens hosts and required Gecko internal schemes; reject insecure HTTP, executable/data/file/intent schemes, untrusted blob origins, and lookalike hosts. Added JVM regression tests to CI.

### Improved
- Addon Manager installs and updates now write add-on files to a new storage generation before switching library metadata. Interrupted updates leave the previous installed files intact; orphaned generations are cleaned up later.
- Numbered TOC dependencies are now ordered numerically (Dep1, Dep2, Dep10).
- Added automated Addon Manager regression tests for metadata parsing, dependency handling, library merge behavior, archive round-trips, and malicious/duplicate ZIP paths.
- Added checks for bundled extension JSON/JavaScript, locale assets, and GeckoView native engine packaging.
- Added SHA-256 checksum generation and verification for CI artifacts and GitHub Releases.
- Addon Manager permission checks fail closed when the browser permissions API reports an error.
- Addon Manager archive imports, restores, and downloads now enforce byte limits while streaming actual data.

### Build and maintenance
- Updated the Android Gradle Plugin/Kotlin toolchain and Gradle configuration.
- Fixed APK signature verification to use the Android SDK's explicit `apksigner` path.
- Added early validation of release keystore, alias, and private-key password configuration.

## [1.0.0] — First release candidate (not yet published)

Requires successful CI, physical-device validation, and release sign-off before publication.

- Removes the in-game top loading-progress line while retaining the configurable LH diagnostics badge and diagnostic logging.

- Includes the first packaged Android app baseline with GeckoView, Home, Play, Addon Manager screens, shared runtime design, and bundled Addon Manager.

# Changelog

This file records notable user-visible changes. It is not the development task list; see [TODO.md](TODO.md) for current work. Unreleased entries describe changes present on the development branch and are not release claims.

## [Unreleased] — beta

### Added
- Native JavaScript alert, confirm, and prompt dialogs.
- Landscape-specific Home screen layout.
- ABI-specific APK outputs and a universal fallback.

### Changed
- Home screen now uses sensor landscape orientation.
- Removed the thin top-edge game loading-progress line while retaining the LH diagnostics badge and diagnostic logging.
- GitHub Actions produces debug-signed demo APK artifacts for pushes to `beta`.

### Improved
- Added automated Addon Manager regression tests for metadata parsing, dependency handling, library merge behavior, archive round-trips, and malicious/duplicate ZIP paths.
- Added checks for bundled extension JSON/JavaScript, locale assets, and GeckoView native engine packaging.
- Added SHA-256 checksum generation and verification for CI artifacts and GitHub Releases.
- Addon Manager permission checks fail closed when the browser permissions API reports an error.

### Build and maintenance
- Updated the Android Gradle Plugin/Kotlin toolchain and Gradle configuration.
- Fixed APK signature verification to use the Android SDK's explicit `apksigner` path.
- Added early validation of release keystore, alias, and private-key password configuration.

## [2.0.0] — In development

Not published. Requires physical-device validation before release readiness can be claimed.

- Removes the in-game top loading-progress line while retaining the configurable LH diagnostics badge and diagnostic logging.

## [1.0.0] — Baseline

- First packaged Android app baseline with GeckoView, Home, Play, Addon Manager screens, shared runtime design, and bundled Addon Manager.

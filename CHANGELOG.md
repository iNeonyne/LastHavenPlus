# Changelog

User-visible changes are recorded here. This project follows a Keep a Changelog-style structure; unreleased development work is not a claim of release readiness.

## [Unreleased]

### Changed
- Home screen is locked to sensor landscape and has a dedicated landscape layout.
- Removed the thin top-edge game loading-progress line while retaining the LH diagnostics badge and diagnostic logging.
- Added native JavaScript alert, confirm, and prompt dialogs.

### Improved
- Added ABI-specific APK outputs plus a universal fallback.
- Added Addon Manager regression tests for metadata parsing, dependency handling, library merge behavior, archive round-trips, and malicious/duplicate ZIP paths.
- Addon Manager permission checks now fail closed when the browser permissions API errors.
- Added APK-content checks for the bundled extension, English locale, and GeckoView native engine.
- Added SHA-256 checksum generation and verification for CI artifacts and GitHub Releases.

### Build and maintenance
- Migrated to a current Android Gradle Plugin/Kotlin toolchain and public Gradle DSLs.
- Fixed CI APK signature verification to use the SDK's explicit `apksigner` path.
- Added early release-keystore, alias, and private-key password validation.
- Normal pushes to `beta` produce debug-signed demo APK artifacts; tagged releases still require the permanent release key.

## [2.0.0] — In development

Major development version. Not yet published or device-verified.

- Removes the in-game top loading-progress line while retaining the configurable LH diagnostics badge and diagnostic logging.

## [1.0.0] — Baseline

- First packaged Android app baseline with GeckoView, Home/Play/Addon Manager screens, shared runtime, and bundled Addon Manager.

# Changelog

Notable changes in each release. Development work that is not yet part of a release belongs in [TODO.md](TODO.md).

## [Unreleased]

_No release notes yet._

## [1.0.1] — Release candidate

### What's new
- **Safer, clearer updates:** The updater now shows download progress and verification status, then hands the verified update to Android's installer.
- **More reliable Addon Manager updates:** Add-on files are staged before an update is activated. If an update is interrupted, the previous installation remains available.
- **Storage overview:** See app and browser storage, cache usage, and available device space, with current usage kept separate from lifetime download totals.
- **Improved Home screen:** Updated themed icons and translucent controls, with a dedicated landscape layout.
- **Better web compatibility:** JavaScript alert, confirmation, and prompt dialogs now use native Android dialogs.
- **Direct game access:** The Play action now opens the primary game address, `https://lasthavens.com/play`.

### Security and reliability
- The updater accepts published stable releases only and checks the APK's size, SHA-256 checksum, ABI, package/version information, and signing-certificate continuity before installation.
- In-app navigation is restricted to approved HTTPS Lasthavens hosts and required Gecko internal schemes. Insecure and untrusted URL schemes and lookalike hosts are rejected.
- Addon Manager permission checks fail closed if the browser permission API errors.
- Addon Manager archive imports, restores, and downloads enforce byte limits while streaming data.
- Addon Manager backups are limited to managed add-on files; they do not include manager settings, enabled state, game saves, login/session data, or the shared browser profile.

### Compatibility and maintenance
- Release APKs target ARM64 devices.
- Automated checks cover Addon Manager metadata, dependencies, archive handling, bundled assets, and release checksums.

## [1.0.0] — Released

- First packaged Android app release, including GeckoView, Home, Play, and Addon Manager.
- Removed the thin in-game loading-progress line while retaining the LH diagnostics badge and diagnostic logging.

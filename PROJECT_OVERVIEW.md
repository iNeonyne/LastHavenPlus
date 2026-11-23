# LastHavenPlus — Technical Overview

## Product

LastHavenPlus is a focused native Android client for the Lasthavens game. It uses Kotlin and Mozilla GeckoView to run the game and bundles the Lasthavens Addon Manager as a WebExtension. It is not intended to become a general-purpose browser.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Distribution target: GitHub Releases only; no Google Play track.
- Automatic updater: not implemented; intended update source is successful CI artifacts from `main` only. The current workflow does not yet build on `main`.
- Android application ID: `com.lasthavens.android`

## Architecture

- **Home:** entry point for Play and Addon Manager. The Home screen uses sensor landscape orientation and has a dedicated landscape resource layout.
- **Play:** GeckoView game session, fullscreen, lifecycle/session retention, native JavaScript dialogs, file picker, downloads, external-link routing, and popup handling.
- **Addon Manager:** bundled WebExtension at `app/src/main/assets/extensions/lh-addon-manager/`.
- **Shared browser state:** Play and Addon Manager are intended to use the same Gecko runtime/profile so installed add-ons can be used by the game.
- **Build:** GitHub Actions validates the bundled extension and packages ABI-specific APKs plus a universal fallback. Normal branch builds create debug-signed demo artifacts; tagged public releases require permanent release signing.

## Version snapshot

- Android app: 2.0.0 — development version, not released or device-validated.
- Android versionCode: 2 — current static Gradle value; replace with the stable workflow run-number sequence before enabling main-based automatic updates.
- Version promotion policy: main receives clean stable SemVer versions; after each approved promotion, beta restarts from that main version and uses the next SemVer prerelease candidate. The numeric CI versionCode sequence never resets.
- Addon Manager: 1.0.2 — staged storage generations protect existing add-on files during replacement; read the bundled `manifest.json` for the authoritative version.
- GeckoView: 159.0.20261102144711 — pinned dependency.

The app and extension have independent versions. Necessary Addon Manager maintenance is owner-authorized only with strict preservation of existing add-on files, enabled state, metadata, settings, and shared Gecko profile state.

## Current build status

- Landscape Home layout and orientation are committed on `beta`.
- Earlier GitHub Actions run [#107](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45089891036) passed the Addon Manager regression suite, APK compilation, signature verification, packaged asset checks, ABI naming, and SHA-256 checksum generation; it uploaded demo APK artifacts.
- Subsequent beta work added staged add-on storage and bounded streaming reads for archive imports, restores, and remote downloads. Regression tests cover the streaming byte limits.
- The streaming-limit changes still require a successful current CI run. CI success alone does not establish physical-device compatibility; the APK has not yet been installed and smoke-tested on a physical phone.

## Immediate validation sequence

1. Download the ARM64 demo artifact from run #107 and install it without uninstalling an existing differently signed app if its data must be preserved.
2. Test first launch, landscape Home, Play, sign-in/gameplay, Home → Play session return, Addon Manager launch, and return to the game.
3. Test touch, fullscreen, keyboard, network loss/recovery, back navigation, background/resume, and Gecko content-process recovery.
4. Record device model, RAM, Android/vendor OS version, crashes, memory behavior, touch responsiveness, battery use, and thermals.
5. Add tests for URL allow-listing and native-bridge exposure before broadening functionality.
6. Review backup/restore: current backup ZIPs contain managed add-on files only, not game settings, SavedVariables, or manager enabled/disabled flags.
7. Resolve permanent release signing and test same-certificate in-place upgrades before the first public GitHub Release.

## Data and security constraints

- Treat all remote game content as untrusted.
- Keep URL/navigation boundaries strict; never expose privileged Android bridges to untrusted pages.
- Keep permission checks fail-closed.
- Preserve Gecko profile, login/session data, add-ons, and manager settings across app updates.
- Before changing profile/storage/backup formats, document current behavior and implement a tested backup, migration, and rollback path.
- Do not publish debug-signed builds as public releases. A different signing certificate prevents normal in-place upgrades and may require uninstalling, risking app data.

## Related project documents

- [README.md](README.md) — overview, build instructions, current development priorities, and release process.
- [AGENTS.md](AGENTS.md) — mandatory contributor and AI-agent working rules.
- [CHANGELOG.md](CHANGELOG.md) — user-visible changes by version.

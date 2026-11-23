# LastHavenPlus — Technical Overview

## Product

LastHavenPlus is a focused native Android client for the Lasthavens game. It uses Kotlin and Mozilla GeckoView to run the game and bundles the Lasthavens Addon Manager as a WebExtension. It is not intended to become a general-purpose browser.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Distribution target: GitHub Releases only; no Google Play track.
- Automatic updater: implemented on `main`; it accepts only successful main push artifacts and verifies provenance, ABI, checksum, package/version, and signing compatibility before Android installer handoff.
- Android application ID: `com.lasthavens.android`

## Architecture

- **Home:** entry point for Play and Addon Manager. The Home screen uses sensor landscape orientation and has a dedicated landscape resource layout.
- **Play:** GeckoView game session, fullscreen, lifecycle/session retention, native JavaScript dialogs, file picker, downloads, external-link routing, and popup handling.
- **Addon Manager:** bundled WebExtension at `app/src/main/assets/extensions/lh-addon-manager/`.
- **Shared browser state:** Play and Addon Manager are intended to use the same Gecko runtime/profile so installed add-ons can be used by the game.
- **Storage dashboard:** read-only measurement of persistent app/shared profile data, cache, and available device space; persistent usage is not isolated to the game.
- **Build:** GitHub Actions validates the bundled extension and packages ABI-specific APKs plus a universal fallback. Normal branch builds create debug-signed demo artifacts; tagged public releases require permanent release signing.

## Version snapshot

- Android app: 2.0.0 — development version, not released or device-validated.
- Android versionCode: `GITHUB_RUN_NUMBER` on CI builds; local builds use a development-only fallback. The number is global to the existing workflow and never resets between branches/tags.
- Version policy: `main` is the sole active branch and uses clean stable SemVer versions. There is no beta prerelease stream. The numeric CI versionCode sequence never resets.
- Addon Manager: 1.0.2 — staged storage generations protect existing add-on files during replacement; read the bundled `manifest.json` for the authoritative version.
- GeckoView: 159.0.20261102144711 — pinned dependency.

The app and extension have independent versions. Necessary Addon Manager maintenance is owner-authorized only with strict preservation of existing add-on files, enabled state, metadata, settings, and shared Gecko profile state.

## Current build status

- Landscape Home layout and orientation are committed on `main`.
- The read-only storage dashboard is implemented and included in the current main build. It reports shared app/profile persistent storage, cache, and available device space; it does not claim game-only storage or lifetime downloaded bytes.
- Main run [#196](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45187442263), for commit `ed0c5bb`, passed all automated tests, direct DER release-certificate verification, four release APK builds, signature/package/version checks, checksum generation, metadata creation, and all five main update artifact uploads.
- In-app navigation now permits HTTPS Lasthavens hosts and the required Gecko internal schemes; insecure HTTP, executable/data/file/intent schemes, untrusted blob origins, and lookalike hosts are rejected by the tested policy.
- Main run #196 verified the permanent signing certificate and published one metadata/checksum artifact plus four per-ABI APK artifacts. The metadata artifact and ARM64 APK artifact were downloaded; the ARM64 APK's actual size and SHA-256 matched the metadata/checksum. Manual-dispatch run #198 also succeeded, but is intentionally ignored by the updater; #196 remains the latest eligible main push run. Physical-device upgrade testing remains outstanding.
- Physical-device installation and smoke testing remain outstanding. CI success does not establish device compatibility, performance, or gameplay stability.

## Automatic updater

The landscape Home screen has a user-triggered **Check for updates** action. The updater filters the existing GitHub Actions workflow for successful completed `push` runs from `main`, checks artifact provenance and expiry, downloads the metadata/checksum artifact and only the full APK artifact for the device ABI (universal fallback), verifies metadata and SHA-256, then checks Android package ID, version code and exact signing-certificate equality. Installation is handed to Android's package installer and requires user confirmation. The updater does not clear app data, GeckoView profile, cookies, login/session state or add-ons.

Main run #196 has published the metadata/checksum and per-ABI artifacts. The first same-certificate in-place update and data-preservation test remains to be completed on a physical device. End-to-end same-certificate upgrade and data-preservation checks remain pending on a physical device.

## Immediate validation sequence

1. Install the permanent-key-signed ARM64 APK from main run #196 on a spare test device. Do not uninstall an existing differently signed app if its data must be preserved.
2. Test first launch, landscape Home, Play, sign-in/gameplay, Home → Play session return, Addon Manager launch, and return to the game.
3. Test touch, fullscreen, keyboard, network loss/recovery, back navigation, background/resume, and Gecko content-process recovery.
4. Record device model, RAM, Android/vendor OS version, crashes, memory behavior, touch responsiveness, battery use, and thermals.
5. Review native-bridge exposure and confirm untrusted pages cannot invoke privileged Android functionality.
6. Review backup/restore: current backup ZIPs contain managed add-on files only, not game settings, SavedVariables, or manager enabled/disabled flags.
7. Use a later successful main build to test the same-certificate in-place update from run #196 before the first public GitHub Release.

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

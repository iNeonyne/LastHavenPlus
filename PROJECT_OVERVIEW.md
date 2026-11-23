# LastHavenPlus — Technical Overview

## Product

LastHavenPlus is a focused native Android client for the Lasthavens game. It uses Kotlin and Mozilla GeckoView to run the game and bundles the Lasthavens Addon Manager as a WebExtension. It is not intended to become a general-purpose browser.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Distribution target: GitHub Releases only; no Google Play track.
- Automatic updater: implemented on `beta`; only successful completed main-branch push artifacts are eligible. The main-only metadata/checksum artifact and four separate signed ABI APK artifacts are configured but will not be available until owner-approved promotion.
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
- Version promotion policy: main receives clean stable SemVer versions; after each approved promotion, beta restarts from that main version and uses the next SemVer prerelease candidate. The numeric CI versionCode sequence never resets.
- Addon Manager: 1.0.2 — staged storage generations protect existing add-on files during replacement; read the bundled `manifest.json` for the authoritative version.
- GeckoView: 159.0.20261102144711 — pinned dependency.

The app and extension have independent versions. Necessary Addon Manager maintenance is owner-authorized only with strict preservation of existing add-on files, enabled state, metadata, settings, and shared Gecko profile state.

## Current build status

- Landscape Home layout and orientation are committed on `beta`.
- The read-only storage dashboard is implemented and included in the current beta build. It reports shared app/profile persistent storage, cache, and available device space; it does not claim game-only storage or lifetime downloaded bytes.
- GitHub Actions beta run [#178](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45181907663), for commit `7ac5e4e`, completed successfully. The Addon Manager regression suite, metadata-generator tests, updater-policy tests, APK compilation, signer-consistency check, packaged asset checks, APK package/version validation, ABI naming, SHA-256 checksum generation, and beta artifact uploads passed. Main-only signing and artifact uploads remain untested until promotion.
- In-app navigation now permits HTTPS Lasthavens hosts and the required Gecko internal schemes; insecure HTTP, executable/data/file/intent schemes, untrusted blob origins, and lookalike hosts are rejected by the tested policy.
- The beta build produces debug-signed demo APKs. The workflow is configured to require the permanent signing certificate for main/tag builds and to create a metadata/checksum artifact plus one signed APK artifact per ABI. This path has not been exercised on `main`, which remains unchanged.
- Physical-device installation and smoke testing remain outstanding. CI success does not establish device compatibility, performance, or gameplay stability.

## Automatic updater

The landscape Home screen has a user-triggered **Check for updates** action. The updater filters the existing GitHub Actions workflow for successful completed `push` runs from `main`, checks artifact provenance and expiry, downloads the metadata/checksum artifact and only the full APK artifact for the device ABI (universal fallback), verifies metadata and SHA-256, then checks Android package ID, version code and exact signing-certificate equality. Installation is handed to Android's package installer and requires user confirmation. The updater does not clear app data, GeckoView profile, cookies, login/session state or add-ons.

The main-only artifact creation path is configured on `beta`, but `main` has not been promoted and does not yet publish that artifact. End-to-end same-certificate upgrade and data-preservation checks remain pending on a physical device.

## Immediate validation sequence

1. Download the ARM64 demo artifact from run #178 and install it without uninstalling an existing differently signed app if its data must be preserved.
2. Test first launch, landscape Home, Play, sign-in/gameplay, Home → Play session return, Addon Manager launch, and return to the game.
3. Test touch, fullscreen, keyboard, network loss/recovery, back navigation, background/resume, and Gecko content-process recovery.
4. Record device model, RAM, Android/vendor OS version, crashes, memory behavior, touch responsiveness, battery use, and thermals.
5. Review native-bridge exposure and confirm untrusted pages cannot invoke privileged Android functionality.
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

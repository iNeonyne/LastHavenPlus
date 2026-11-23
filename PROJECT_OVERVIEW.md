# LastHavenPlus — Technical Overview

## Product

LastHavenPlus is a focused native Android client for the Lasthavens game. It uses Kotlin and Mozilla GeckoView to run the game and bundles the Lasthavens Addon Manager as a WebExtension. It is not intended to become a general-purpose browser.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Distribution target: GitHub Releases only; no Google Play track.
- Automatic updater: being switched to published stable GitHub Releases; it verifies the release APK checksum, ABI, package/version, and signing compatibility before Android installer handoff.
- Android application ID: `com.lasthavens.android`

## Architecture

- **Home:** entry point for Play and Addon Manager. The Home screen uses sensor landscape orientation and has a dedicated landscape resource layout.
- **Play:** GeckoView game session, fullscreen, lifecycle/session retention, native JavaScript dialogs, file picker, downloads, external-link routing, and popup handling.
- **Addon Manager:** bundled WebExtension at `app/src/main/assets/extensions/lh-addon-manager/`.
- **Shared browser state:** Play and Addon Manager are intended to use the same Gecko runtime/profile so installed add-ons can be used by the game.
- **Storage dashboard:** read-only measurement of persistent app/shared profile data, cache, and available device space; persistent usage is not isolated to the game.
- **Build:** GitHub Actions validates the bundled extension and packages one ARM64-only APK. Normal branch builds create debug-signed demo artifacts; tagged public releases require permanent release signing.

## Version snapshot

- Android app: 1.0.0 — first release candidate, not published or device-validated.
- Android versionCode: `GITHUB_RUN_NUMBER` on CI builds; local builds use a development-only fallback. The number is global to the existing workflow and never resets between branches/tags.
- Version policy: `main` is the sole active branch and uses clean stable SemVer versions. There is no beta prerelease stream. The numeric CI versionCode sequence never resets.
- Addon Manager: 1.0.2 — staged storage generations protect existing add-on files during replacement; read the bundled `manifest.json` for the authoritative version.
- GeckoView: 159.0.20261102144711 — pinned dependency.

The app and extension have independent versions. Necessary Addon Manager maintenance is owner-authorized only with strict preservation of existing add-on files, enabled state, metadata, settings, and shared Gecko profile state.

## Current build status

- Landscape Home layout and orientation are committed on `main`.
- The read-only storage dashboard is implemented and included in the current main build. It reports shared app/profile persistent storage, cache, and available device space; it does not claim game-only storage or lifetime downloaded bytes.
- Main run [#256](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45203875251), for commit `6074628`, passed the release-updater policy tests, permanent-key verification, ARM64 packaging, APK signature and package/version checks, and checksum generation.
- In-app navigation now permits HTTPS Lasthavens hosts and the required Gecko internal schemes; insecure HTTP, executable/data/file/intent schemes, untrusted blob origins, and lookalike hosts are rejected by the tested policy.
- Main run #256 verified the permanent signing certificate and uploaded the ARM64 validation APK artifact. The release-based updater targets published stable GitHub Releases rather than expiring Actions artifacts. Physical same-certificate upgrade testing remains outstanding.
- Physical-device installation and smoke testing remain outstanding. CI success does not establish device compatibility, performance, or gameplay stability.

## Automatic updater

The landscape Home screen has a user-triggered **Check for updates** action. The updater checks the latest published stable GitHub Release, ignores drafts and prereleases, requires one ARM64 APK and one checksum asset, validates HTTPS release asset URLs and declared sizes, verifies SHA-256, then checks Android package ID, versionName/versionCode, device ABI and exact signing-certificate equality. Installation is handed to Android's package installer and requires user confirmation. The updater does not clear app data, GeckoView profile, cookies, login/session state or add-ons.

The updater now targets permanent assets attached to published GitHub Releases rather than expiring Actions artifacts. The first same-certificate in-place update and data-preservation test remains to be completed on a physical device.


## UI/UX improvement scope

The following work is tracked in [TODO.md](TODO.md) under **P2 — UI/UX consistency and responsive layouts**; it is planned, not yet complete:

- Make Home and Addon Manager responsive in both portrait and landscape. Home is currently configured for sensor landscape; Manager currently has no forced orientation. Do not remove the Home restriction until both orientation layouts are implemented and tested.
- Keep Play landscape-locked for gameplay.
- Apply one coherent dark, frost-blue and gold theme to backgrounds, surfaces, cards, dialogs, buttons, and status/feedback states.
- Standardize icon style, dimensions, alignment, tint, and accessibility descriptions.
- Audit responsive layout, navigation, accessibility, touch targets, keyboard/focus behavior, system insets, and lifecycle/state preservation across all screens.
- Verify the result with portrait and landscape screenshots and device testing.

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

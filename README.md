# LastHavenPlus

A native Android client for [Lasthavens](https://alt.lasthavens.com/play), built with Kotlin and Mozilla GeckoView. It includes a bundled Addon Manager WebExtension so the game and manager can share browser state and installed add-ons.

> **Project status:** main-branch development. Main CI run #231 passed the ARM64-only build, signing, APK metadata, and checksum checks. The updater is being changed to use published stable GitHub Releases; same-certificate in-place update and data-preservation testing on a physical device remain outstanding. No permanent public download link is published yet. This is an unofficial, independent client; it is not endorsed by Lasthavens or Blizzard Entertainment.

## At a glance

- **Platform:** Android
- **Engine:** Mozilla GeckoView
- **Language:** Kotlin
- **Application ID:** `com.lasthavens.android`
- **Development branch:** `main`
- **Distribution target:** GitHub Releases; no Google Play track is planned

Useful links: [Game](https://alt.lasthavens.com/play) · [Repository](https://github.com/iNeonyne/Lasthavens-Android) · [Builds](https://github.com/iNeonyne/Lasthavens-Android/actions) · [Releases](https://github.com/iNeonyne/Lasthavens-Android/releases)

## Features

- Landscape Home screen with Play and Addon Manager entry points.
- GeckoView-powered game session.
- Bundled Addon Manager WebExtension.
- Shared Gecko runtime/profile design for game and manager.
- Read-only storage dashboard for shared app/profile data, cache, and available device space.
- HTTPS-only in-app navigation for Lasthavens pages; external sites open in the system browser.
- Android integration for fullscreen, JavaScript dialogs, file selection, downloads, popups, and external links.
- User-triggered updater that checks published stable GitHub Releases, verifies SHA-256 and signing-certificate compatibility, and hands installation to Android.

## Get a build

Stable downloads are distributed through [GitHub Releases](https://github.com/iNeonyne/Lasthavens-Android/releases). The in-app updater checks only the latest published stable release, downloads its ARM64 APK and checksum asset, and verifies the APK before handing it to Android. Draft releases and prereleases are ignored. Main CI artifacts are for validation and are not update sources.

Use a spare test device for same-certificate update testing. Do not uninstall a data-bearing installation signed with a different key.

**Important:** Pull-request and manual test APKs may be debug-signed. Do not uninstall an existing installation signed with a different certificate if its app data matters; uninstalling can erase the local app profile. A successful CI build is not proof of device compatibility.

## Build locally

Open the repository in Android Studio with JDK 17 and the Android SDK required by the project. Build with:

```bash
./gradlew assembleRelease
```

GitHub Actions runs automated checks on `main`, pull requests, and manual dispatches. Main builds are validation builds and publish temporary artifacts; the in-app updater does not use them. Tagged builds create draft GitHub Releases with the permanent-key-signed ARM64 APK and checksums. The updater reads only published stable releases.

## Versions

| Component | Current version | Status |
|---|---:|---|
| Android app | `1.0.0` | First release candidate; not yet published or device-validated |
| Bundled Addon Manager | `1.0.2` | Crash-safe add-on replacement; preserves existing library data |
| GeckoView | `159.0.20261102144711` | Pinned dependency |

The extension version is independent of the Android app version. Check the bundled extension's `manifest.json` for its authoritative version. Existing add-on files remain in their current storage locations until that add-on is updated; new replacements use staged storage generations so an interrupted update does not replace the last committed files.


## Versioning and updates

The Android app's `versionName` follows `MAJOR.MINOR.PATCH` (Semantic Versioning). Android's numeric `versionCode` is separate and determines whether an APK can replace an installed version. The Android app, bundled Addon Manager, and GeckoView each have independent versions.

The first public Android app release is planned as `1.0.0` (tag `v1.0.0`). Keep this stable across ordinary commits. After `1.0.0`, use `1.0.1` for the next patch release, `1.1.0` for the next backward-compatible feature release, or `2.0.0` only for a breaking release. Android `versionCode` is taken from the existing workflow's monotonically increasing `GITHUB_RUN_NUMBER`; local builds use a development-only fallback and are never update candidates. `main` is the sole development and release branch; do not create a permanent beta branch or prerelease version stream.

The user-triggered updater checks the latest published stable GitHub Release, ignores drafts and prereleases, downloads the exact ARM64 APK and `SHA256SUMS.txt` assets, and verifies declared size, SHA-256, package ID, versionName/versionCode, device ABI, and exact installed signing-certificate equality before handing the APK to Android's user-confirmed installer. It never silently installs, uninstalls, or clears app data. Main CI, pull-request, manual-dispatch, and draft-release builds are not update sources. A debug-signed test installation cannot be upgraded in place to the permanent release key; do not uninstall a data-bearing installation to switch signing identities. Use a separate test device for release-signing validation. The new release-based updater is undergoing CI validation; same-certificate physical-device upgrade and data-preservation tests remain outstanding. See [AGENTS.md](AGENTS.md) for the rules agents must follow.

## Create a GitHub Release

Official releases are created from stable version tags on `main`:

1. Complete the required physical-device, same-certificate upgrade, data-preservation, and distribution-permission checks in [MANUAL_CHECKLIST.md](MANUAL_CHECKLIST.md).
2. Update `versionName` in `app/build.gradle.kts` and add the user-visible changes to `CHANGELOG.md`. Keep the tag exactly aligned with the app version.
3. Merge the release-ready commit into `main`, then create and push the matching tag, for example `v1.0.0`.
4. GitHub Actions validates that the tag is stable SemVer, matches `versionName`, and points to a commit reachable from `main`. It builds and verifies the permanent-key-signed ARM64 APK and creates a **draft GitHub Release** with generated notes, the APK, and checksums.
5. Review the draft, its assets, and release notes. Publish it manually only after release sign-off.

Do not create a tag for an unreleased version just to test CI. A tag build is a release candidate, not proof of physical-device validation or distribution approval.

## Project documents

| Document | Purpose |
|---|---|
| [TODO.md](TODO.md) | Current priorities, demo test plan, bugs, and deferred work |
| [CHANGELOG.md](CHANGELOG.md) | User-visible changes by version |
| [AGENTS.md](AGENTS.md) | Required instructions for AI agents and contributors |
| [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) | Technical architecture and current project state |

## Important notice

The Lasthavens Terms of Use restrict modified clients, with an exception for add-ons in the in-game AddOn Library. Whether this independent client and its bundled manager are permitted has not been confirmed. Obtain explicit guidance from the Lasthavens team before public distribution. Do not present this app as official or endorsed.

## License

No project license has been declared. Do not assume this repository grants permission to reuse or redistribute its code.

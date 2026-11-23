# LastHavenPlus

A native Android client for [Lasthavens](https://alt.lasthavens.com/play), built with Kotlin and Mozilla GeckoView. It includes a bundled Addon Manager WebExtension so the game and manager can share browser state and installed add-ons.

> **Project status:** main-branch development. Main CI run #196 verified the permanent release certificate, built and signed all four APK variants, and published the metadata/checksum plus per-ABI update artifacts. Automated checks pass; same-certificate in-place update and data-preservation testing on a physical device remain outstanding. No permanent public download link is published yet. This is an unofficial, independent client; it is not endorsed by Lasthavens or Blizzard Entertainment.

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
- User-triggered, main-only APK updater with artifact provenance, SHA-256 and signing-certificate checks, and Android installer handoff.

## Get a build

For testing, main run #196 publishes one metadata/checksum artifact and four signed APK artifacts. These are temporary CI artifacts, not permanent release downloads:
1. Open [GitHub Actions](https://github.com/iNeonyne/Lasthavens-Android/actions).
2. Select the latest successful `push` run from `main`. Manual-dispatch, pull-request, and tag runs are not updater candidates.
3. Download `LastHavenPlus-Main-Update-<ABI>` for the device ABI, or `LastHavenPlus-Main-Update-universal` as fallback.
4. Use a spare test device for same-certificate update testing; do not uninstall a data-bearing debug-signed installation.

The CI artifacts expire after 90 days. A permanent public download link will be added only after physical-device update and data-preservation checks and release approval.

**Important:** Pull-request and manual test APKs may be debug-signed. Do not uninstall an existing installation signed with a different certificate if its app data matters; uninstalling can erase the local app profile. A successful CI build is not proof of device compatibility.

## Build locally

Open the repository in Android Studio with JDK 17 and the Android SDK required by the project. Build with:

```bash
./gradlew assembleRelease
```

GitHub Actions runs automated checks on `main`, pull requests, and manual dispatches. Successful `main` builds publish a metadata/checksum artifact and four permanent-key-signed, ABI-specific update APK artifacts; pull-request and manual builds are not update candidates. Main run #196 confirmed the direct DER certificate hash matches the expected release certificate and verified all four ABI APK signatures.

## Versions

| Component | Current version | Status |
|---|---:|---|
| Android app | `2.0.0` | Development; not released or device-validated |
| Bundled Addon Manager | `1.0.2` | Crash-safe add-on replacement; preserves existing library data |
| GeckoView | `159.0.20261102144711` | Pinned dependency |

The extension version is independent of the Android app version. Check the bundled extension's `manifest.json` for its authoritative version. Existing add-on files remain in their current storage locations until that add-on is updated; new replacements use staged storage generations so an interrupted update does not replace the last committed files.


## Versioning and updates

The Android app's `versionName` follows `MAJOR.MINOR.PATCH` (Semantic Versioning). Android's numeric `versionCode` is separate and determines whether an APK can replace an installed version. The Android app, bundled Addon Manager, and GeckoView each have independent versions.

The current app line uses `versionName = 2.0.0`. Keep this stable across ordinary commits. Use `2.0.1` for the next patch release, `2.1.0` for the next backward-compatible feature release, or `3.0.0` only for a breaking release. Android `versionCode` is taken from the existing workflow's monotonically increasing `GITHUB_RUN_NUMBER`; local builds use a development-only fallback and are never update candidates. `main` is the sole development and release branch; do not create a permanent beta branch or prerelease version stream.

The main branch contains a user-triggered updater. It queries only successful completed `push` runs from `main`, downloads the metadata/checksum artifact and only the complete APK artifact matching the device ABI (universal fallback), checks artifact/run/commit provenance and metadata, verifies the APK SHA-256, and checks package ID, version code and installed signing certificate before offering Android's user-confirmed package installer. It never silently installs, uninstalls, or clears app data. Debug-signed pull-request/test APKs are not accepted as update sources. A debug-signed test installation cannot be upgraded in place to a permanent-key-signed main build; do not uninstall a data-bearing installation to switch signing identities. Use a separate test device for release-signing validation. Main run #196 passed permanent-key signing and artifact publication; the live update feed still requires same-certificate physical-device validation. No public download link is published until an approved release exists. See [AGENTS.md](AGENTS.md) for the rules agents must follow.

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

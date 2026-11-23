# LastHavenPlus

An unofficial native Android client for [Lasthavens](https://lasthavens.com/play).

> **Status:** The project is transitioning to Android System WebView (Chromium) and native add-on storage. GeckoView and its browser-extension runtime have been removed from the current source. The game loader and native manager still need integration verification; this source must not be described as release-ready until the P0 checks pass.

## Architecture

- **Engine:** Android System WebView supplied by the device; Chromium is not bundled.
- **Manager:** Native Android UI backed by app-private storage.
- **Game integration:** AndroidX WebKit document-start scripts and an origin-restricted, read-only add-on data bridge.
- **Updater:** Native Kotlin updater for published stable GitHub Releases.
- **Application ID:** `com.lasthavens.android`
- **Branch:** `main`
- **Distribution:** ARM64 APKs through GitHub Releases.

## Important migration notice

If upgrading from a GeckoView-based LastHavenPlus build, export your add-ons first:

1. Open the old app's Addon Manager (version 1.0.6).
2. Choose **Export migration package**.
3. Save the ZIP somewhere outside the app, such as Downloads or cloud storage.
4. Install the new WebView-only build, open Addon Manager, and import that ZIP.

The new app no longer contains GeckoView, so it cannot open the old Gecko profile or create an export after upgrading. The update does not clear app data or deliberately delete the old profile files, but those files are not accessible through the new app. Gecko cookies/login state are not transferred; signing in again may be required.

## Current native features

- Responsive native Home, Storage and updater screens.
- WebView gameplay with persistent Chromium site storage.
- Native migration ZIP validation, checksum verification and atomic activation.
- Native add-on listing and enable/disable controls.
- A read-only game bridge with bounded binary file chunks.
- Stable-release updater with explicit version selection and APK verification.

The add-on loader hooks still require integration validation against the current live game. Add-on storage being imported does not by itself prove that the game loaded those add-ons.

## Build

Use Android Studio, JDK 17 and the Android SDK required by the project:

```bash
./gradlew assembleRelease
```

GitHub Actions builds signed ARM64 validation artifacts on `main`. Main artifacts are not update sources. Tagged builds create draft releases only; publishing requires explicit owner approval.

## Identity and safety

Keep the application ID, permanent signing certificate, ARM64-only output and monotonic CI versionCode unchanged. The updater must never clear app data or silently install an update.

See [ROADMAP.md](ROADMAP.md), [TODO.md](TODO.md) and [AGENTS.md](AGENTS.md) for engineering rules. This is an independent, unofficial client and is not endorsed by Lasthavens or Blizzard Entertainment. Confirm distribution permissions before public release.

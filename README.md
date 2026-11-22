# Lasthavens Android

Android app for playing Lasthavens (`https://alt.lasthavens.com/play`) in Mozilla GeckoView, with the Lasthavens Addon Manager bundled into the app.

| Component | Version |
|---|---:|
| Android app | 1.0.0 |
| Bundled Addon Manager | 0.11.0 |
| GeckoView | 160.0.20261116130455 |

## Main screens

- **Home:** Lasthavens branding, Play and Addon Manager actions, and component versions.
- **Play:** fullscreen landscape game session. The game session is kept when returning Home; external websites are routed to the system browser.
- **Addon Manager:** the bundled extension's manager page, sharing the same GeckoRuntime profile as the game.

## Architecture

1. **GeckoView:** embeds Firefox rather than using Android WebView, which is necessary for the manager's WebExtension APIs.
2. **Built-in extension:** the Addon Manager files are packaged under `app/src/main/assets/extensions/lh-addon-manager/` and installed with `ensureBuiltIn`.
3. **Shared profile:** the game and manager use one GeckoRuntime so extension storage and installed add-ons are shared.
4. **Native bridges:** Android handles file selection, downloads, external links, extension tab creation, and JavaScript alert/confirm/prompt dialogs.
5. **Translations:** the Android resource ignore pattern is customized so the extension's `_locales` directories are included in the APK.

## Build

GitHub Actions builds the release APK on every push to `main`, uploads it as an artifact named `Lasthavens-APK`, and attaches it to a GitHub Release when a `v*` tag is pushed.

The workflow uses JDK 17, the Android 17 preview SDK (API 37), Android Gradle Plugin 9.1.1, built-in Kotlin support, and Gradle 9.3.1. The app currently targets Android 15 (API 35) while compiling against API 37.

For a local build, open this folder in Android Studio with JDK 17 and run the `assembleRelease` task.

## Signing

Without signing secrets, CI signs with the Android debug key. Different GitHub-hosted runners may generate different keys, so a later APK may not update an earlier test APK. Uninstall the previous test APK before installing a differently signed one.

For repeatable updates, configure `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` repository secrets.

## Verification status

A release APK has compiled successfully in GitHub Actions. Device behavior is not yet verified. Test game launch and sign-in, shared add-ons, ZIP import and backup/restore, downloads, JavaScript dialogs, external links, fullscreen, back navigation, and recovery after process crashes on a physical phone.

## Engine updates

The GeckoView dependency is pinned to an exact version for reproducible builds. Update it deliberately after checking compatibility with the bundled extension's `strict_min_version`.

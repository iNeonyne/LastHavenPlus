# Lasthavens Android

Android app for playing Lasthavens (`https://alt.lasthavens.com/play`) in a built-in
Firefox engine (Mozilla GeckoView) with an integrated **Addon Manager**.

| | Version |
|---|---|
| App | **1.0.0** |
| Addon Manager | **0.11.0** (kept fixed until the project owner approves a change) |

## Screens
- **Home** – dark gaming style, Lasthavens branding, **Play** and **Addon Manager** buttons, both versions shown.
- **Play** – full-screen game. Back asks before leaving (Keep playing / Reload / Home). Non-Lasthavens links open in the system browser. The session stays loaded when you return to Home.
- **Addon Manager** – the bundled extension's own `manager.html`, shown in its own GeckoView session.

## Architecture (decisions)
1. **GeckoView, not WebView/TWA.** The Addon Manager is a Firefox MV3 WebExtension (content scripts in the `MAIN` world, `browser.storage`, IndexedDB). GeckoView is the only embeddable engine that can run it. (The previous `lasthavens.apk` was a Trusted Web Activity with no add-on support.)
2. **Add-on Manager is bundled unchanged** in `app/src/main/assets/extensions/lh-addon-manager/` and installed as a *built-in* extension (`ensureBuiltIn`), so Mozilla signing is not needed. The game is only loaded after the extension is ready so its `document_start` scripts apply.
3. **One GeckoRuntime = one profile**, so the manager page and the game share extension storage; installed add-ons reach the game. After using the manager, the next Play reloads the game.
4. **Host-side glue GeckoView does not provide:** file picker (`<input type=file>` for ZIP restore), downloads (add-on export / backup ZIP → `Downloads/Lasthavens`), approval of the manager's optional GitHub/GitLab host permissions (only its declared origins), and `tabs.create` routing.
5. `_locales` caveat: AAPT drops asset folders starting with `_`; `build.gradle.kts` overrides the ignore pattern so the extension's translations are packaged.
6. **Version guard:** the build fails if the bundled manager is not 0.11.0.

## Build
GitHub Actions (`.github/workflows/build.yml`) builds `Lasthavens-1.0.0.apk` on every push, uploads it as a workflow artifact, and attaches it to a GitHub Release when you push a tag like `v1.0.0`.

Locally: Android Studio (JDK 17) → open this folder → Run. Or `gradle :app:assembleRelease` (Gradle 8.10+).

**Signing:** without secrets the APK is signed with the debug key (installable, but builds from different runners can't update each other). To use your own key, add the repo secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.

**GeckoView version:** `gradle.properties` → `geckoViewVersion=142.+` (the manager needs Gecko ≥ 142). Pin an exact version for reproducible builds.

## Not yet verified (first compile/run happens in CI or Android Studio)
- GeckoView API usage was written without a compiler or device available. If a signature differs in your resolved GeckoView version, the CI log will point at the line.
- Blob downloads (backup ZIP) and the file picker need a device test.
- Browser-style JS dialogs (`alert`/`confirm`) are not implemented.
- If the game's login uses another domain, add it in `GameUrls.isInApp`.

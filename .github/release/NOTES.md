# LastHavenPlus v2.5.0 — Addon Manager Phase B

This release expands the native Addon Manager with a GeckoView-integrated resource bridge, improves game navigation and updater feedback, and updates the embedded browser engine to Mozilla GeckoView 161.

## New features

- **Addon Manager Phase B:** Continue managing addons from the in-game overlay without replacing the active game session.
- **Addon resource bridge:** Add bounded, validated addon resource delivery through GeckoView extension APIs, including XHR and Worker interception for supported game resources.
- **Safer addon management:** Confirm before replacing existing addon folders, preserve the current library when an import is cancelled, and show clearer import and operation progress.
- **Alternate game server:** Choose the first-party alternate server. Both first-party hosts launch at `/play`.
- **Updater feedback:** Show download progress and retry feedback, with safer cleanup and fallback behavior.
- **Browser engine:** Upgrade to Mozilla GeckoView 161.

## Bug fixes and reliability

- Restrict game navigation to verified first-party origins while allowing the main domain and its subdomains.
- Disconnect stale addon bridge ports when the game session changes.
- Refresh addon bridge snapshots after library changes and clearly indicate when a game reload is required.
- Improve launcher navigation and Back handling while preserving the active GeckoSession.
- Add bounded validation and tests for addon bridge payloads, XHR/Worker resource interception, and updater behavior.

## Small changes

- Include updated third-party license notices and a GeckoView `about:license` inventory.
- Improve diagnostics, localization resources and launcher feedback.

## Compatibility

- Android 10 (API 29) or newer.
- ARM64 devices only (`arm64-v8a`).
- Mozilla GeckoView 161.
- Existing application ID and established release signing identity are preserved.
- GeckoView uses a separate browser profile; you may need to sign in to the game again after upgrading.

## Known limitations

- The Addon Manager's enabled state is a saved local preference; it is not proof that an addon is active in the game.
- Automated tests cover bridge behavior and resource delivery, but actual in-game Lua initialization and addon functionality have not been verified on a physical device.
- No physical ARM64 device testing is claimed for this release. Automated CI does not replace testing gameplay, rendering, accessibility, and addon behavior on hardware.

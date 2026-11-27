# LastHavenPlus

LastHavenPlus is an Android launcher for the existing [LastHaven Wrath of the Lich King game](https://lasthavens.com/play). It hosts the game and does not recreate or modify the game client.

**Latest stable release:** [LastHavenPlus v2.4.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.4.0)

## Compatibility

| Item | Stable release |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Embedded browser | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |

The application ID and established signing identity are preserved for in-place updates.

## Features in v2.4.0

- **Direct gameplay:** Launches the game directly. Android Back opens the in-game menu with Reload, Settings and Return to game.
- **Session-aware navigation:** Settings, diagnostics and update screens retain the existing game session while the game view is detached.
- **Recovery:** Provides explicit loading, retryable page-load failure and renderer/process recovery states.
- **Accessible interface:** Uses a consistent visual theme, readable typography, clear touch targets, accessible pane/heading semantics and reduced-motion-aware transitions.
- **Diagnostics:** Keeps bounded diagnostic logs on the device and supports viewing, clearing and user-initiated sharing.
- **Manual updater:** Validates release metadata, download size and SHA-256 digests. Differential-update failures retain a verified full-APK fallback. The user chooses when to check, download and install; Android's package installer controls installation.
- **Addon Manager — local library:** Imports and exports addon ZIPs, stores packages in app-private storage and displays metadata and dependency information. The enabled state is local metadata only; this release does not load or activate addons in the game.

## Browser profile

GeckoView uses its own browser profile. A supported migration from the former Cefrium profile has not been established, so a fresh game login may be required. Do not delete or copy old browser-profile data as part of an update.

## Privacy and data

Diagnostics and usage information remain local unless the user explicitly shares a diagnostic export. The launcher does not add analytics or automatic telemetry. App network counters include launcher and updater traffic; they are not game-only measurements.

## Updates

Download updates from the [GitHub Releases page](https://github.com/iNeonyne/LastHavenPlus/releases). Checks, downloads and installation are user-initiated. Preserve the existing application installation and signing identity; never install an APK from an untrusted source.

## Project direction

The strategic plan is maintained in the [LastHavenPlus roadmap](ROADMAP.md). The roadmap links to GitHub Issues for current implementation detail.

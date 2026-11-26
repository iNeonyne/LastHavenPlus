# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game. It hosts the game; it does not recreate, modify or develop the game client.

- **Game:** [lasthavens.com/play](https://lasthavens.com/play)
- **Latest stable release:** [v2.3.0 on GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.3.0)
- **Development:** `beta`; stable releases: `main`

## Compatibility and identity

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView 160 |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

Preserve the application ID and established release signing identity to maintain update compatibility.

## Current beta features

These are features integrated into beta and covered by the repository's automated build and test checks where applicable.

- **Direct launch:** The app opens directly into the game. Android Back opens a responsive in-game menu with Reload, Settings and Return to game; Settings does not initialize a separate game session.
- **Navigation:** Settings, diagnostics and update details are full-screen pages rather than stacked popup dialogs. The existing GeckoSession is retained when the game view is detached for launcher pages.
- **Responsive layout:** The in-game menu adapts to available window space; settings pages cap content width on larger windows. The Activity is resizeable.
- **Launcher icon:** Adaptive launcher icon resources are included.
- **Settings and storage:** Installed APK size, persistent app-private data size and cache size are reported separately. App-private data includes GeckoView data and is not a profile-only measurement.
- **Network usage:** Android per-UID counters report app traffic, including both game and updater traffic; they are not game-only totals.
- **Diagnostics:** Bounded local diagnostic logs support viewing, clearing and explicit user-initiated sharing. Review logs before sharing.
- **Updater:** Stable release checks and downloads remain user-initiated. Manifest, version, URL, payload and digest validation are applied; differential patch failures retain a verified full-APK fallback. Android's package installer controls installation.
- **Addon manager:** Native Phase A local ZIP/library management is integrated by [PR #152](https://github.com/iNeonyne/LastHavenPlus/pull/152) in beta. Imported addons remain library data; local enablement is metadata only and does not activate addons in the game. The supplied Chrome MV3 runtime port (Phase B) is not implemented. The repository owner has authorized Phase B investigation/implementation under Chaty's coordination; GeckoView 160 compatibility and security review remain mandatory.

## Site permissions

The launcher grants only persistent site storage and audible/inaudible autoplay to `lasthavens.com` and its subdomains. Other website permission requests are denied. Android runtime-permission and camera/microphone media-capture requests are also denied, so features that require those capabilities are unavailable unless a future reviewed policy change explicitly enables them.

## GeckoView profile transition

GeckoView uses its own browser profile. No supported, verified migration of Cefrium cookies or other browser data has been established. Do not copy or delete existing Cefrium profile data as part of this transition. A fresh game login may be required.

The repository owner evaluates releases on their own schedule and may report gameplay, profile, browser API, media and updater behavior. Feedback is recorded in GitHub Issues and used to prioritize follow-up fixes; pending feedback is not a publication prerequisite.

## Build and test

Requirements: JDK 25, Gradle 9.7.1 via the committed Gradle Wrapper, Android SDK Platform 37.2 and Android Build Tools 37.0.0.

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

A signed release build requires the established project keystore and credentials. CI obtains these from repository secrets; never commit keystores or credentials.

## Release policy

- Package ARM64 only.
- Preserve application ID and established signing identity.
- Keep update checks, downloads and installation user-initiated.
- Never publish debug APKs or incomplete features.
- Promote beta only after release gates pass and the owner explicitly approves.
- The repository owner may test published releases and report findings on their own schedule. Pending feedback is not a release blocker.

## Project workflow and records

- `beta` is the development, integration and testing branch; `main` is the stable release branch.
- Task PRs target `beta`. Chaty alone reviews and merges into beta.
- Chaty coordinates and reviews; Vibe implements only existing issues explicitly assigned by Chaty.
- GitHub Issues, PRs, reviews, commits and Actions are the only agent-to-agent communication channel.
- Every actionable issue has exactly one `agent:*` state/ownership label: `agent:unassigned` (intake), `agent:queued` (ready, waiting its turn), `agent:chaty`, `agent:vibe`, `agent:blocked` (dependency/hold), `agent:review` or `agent:owner`.
- `ISSUES.md` and GitHub Issues are the task sources of truth.
- `STATUS.md` records verified, unverified and incomplete work plus exact CI evidence.
- `AGENTS.md` and `AGENT_WORK.md` define contributor workflow and active assignments.
- `CHANGELOG-AGENTS.md` and `WORK_LOG.md` preserve project/work-session history.

See [Mozilla GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html), [quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html) and [WebExtensions documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html).

# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game at [lasthavens.com/play](https://lasthavens.com/play). It hosts the game; it does not recreate, modify, or develop the game client.

This is an agent-operated repository. All code and documentation contributions must be made by agents explicitly authorized under [AGENTS.md](AGENTS.md).

- **Game:** https://lasthavens.com/play
- **Stable release:** [v2.3.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.3.0)
- **Development/integration branch:** `beta`
- **Stable branch:** `main`

## Compatibility

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView 161 Stable |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

The application ID and established release-signing identity must be preserved to maintain update compatibility.

## Product outline

The launcher starts directly in the game and provides launcher-owned navigation, settings, diagnostics, and a user-initiated updater. GeckoView uses an app-private browser profile. No supported Cefrium-to-GeckoView profile migration exists; never copy or delete Cefrium profile data as part of the engine transition. A fresh game login may be required.

The Addon Manager includes local library-management foundations. Local import, metadata, planning, or an enabled flag do not prove that an addon is active in the game. Runtime activation remains unverified; consult STATUS.md and the live issues before describing its state.

Site permissions are deny-by-default. Persistent storage and audible/inaudible autoplay are allowed only for lasthavens.com and its subdomains. Other website permissions, Android runtime-permission requests, and camera/microphone capture requests are denied. See [docs/GECKOVIEW_MIGRATION.md](docs/GECKOVIEW_MIGRATION.md) for the source-level contract and evidence.

## Build and test

Use the committed Gradle Wrapper with the pinned toolchain:

- JDK 25
- Gradle 9.7.1
- Android SDK Platform 37.2
- Android Build Tools 37.0.0

Common local checks:

```sh
./gradlew assembleRelease
./gradlew testDebugUnitTest
python3 -m unittest discover -s .github/scripts/tests -v
```

PR and release checks are defined by the live workflows under .github/workflows. PR validation is unsigned and must not require signing secrets. Signed release packaging and publication have separate authorization and verification gates.

## Agent and project records

| File | Purpose |
|---|---|
| [AGENTS.md](AGENTS.md) | Single authoritative source for agent roles, permissions, workflow, and engineering rules |
| [AGENT_PROTOCOL.md](AGENT_PROTOCOL.md) | Startup sequence and bootstrap prompt |
| [AGENT_WORK.md](AGENT_WORK.md) | Current assignments, queues, and historical work records |
| [STATUS.md](STATUS.md) | Verified project state, blockers, and CI evidence |
| [ISSUES.md](ISSUES.md) | Backlog/index; GitHub Issues remain authoritative for live task state |
| [docs/BRANCH_LIFECYCLE.md](docs/BRANCH_LIFECYCLE.md) | Branch naming, reuse, synchronization, retirement, and audit |
| [docs/GECKOVIEW_MIGRATION.md](docs/GECKOVIEW_MIGRATION.md) | GeckoView integration contract, migration notes, and evidence |
| [docs/CI_ACTIONS.md](docs/CI_ACTIONS.md) | CI permissions and supply-chain policy |
| [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md), [WORK_LOG.md](WORK_LOG.md) | Append-only history |

README is a stable overview, not a live assignment queue or work-session log. Check the operational records and live GitHub state before making claims about current progress.

# LastHavenPlus

LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game at [lasthavens.com/play](https://lasthavens.com/play). It hosts the game; it does not recreate, modify, or develop the game client.

This is an agent-operated repository. All code and documentation contributions must be made by agents explicitly authorized under [AGENTS.md](AGENTS.md).

- **Game:** https://lasthavens.com/play
- **Published releases:** [GitHub Releases](https://github.com/iNeonyne/LastHavenPlus/releases/latest) — the live source for the latest stable version. As verified on 2026-11-28, the latest release is v2.4.0.
- **Release history:** [CHANGELOG.md](CHANGELOG.md)
- **Development/integration branch:** `beta`
- **Stable branch:** `main`

## Compatibility

| Item | Configuration |
|---|---|
| Android | Android 10 (API 29) and later |
| CPU architecture | ARM64 only (`arm64-v8a`) |
| Browser engine | Mozilla GeckoView Stable; beta's exact artifact is pinned in [gradle/libs.versions.toml](gradle/libs.versions.toml) |
| UI | Kotlin and Jetpack Compose |
| Application ID | `com.neonyne.lasthavenlauncher` |
| Game URL | `https://lasthavens.com/play` |

The application ID and established release-signing identity must be preserved to maintain update compatibility.

The published v2.4.0 release used GeckoView 160; beta's current engine pin is defined by the version catalog and may differ from a published release. Read the catalog for the exact beta version.

## Product outline

The launcher starts directly in the game and provides launcher-owned navigation, settings, diagnostics, and a user-initiated updater. GeckoView uses an app-private browser profile. No supported Cefrium-to-GeckoView profile migration exists; never copy or delete Cefrium profile data as part of the engine transition. A fresh game login may be required.

The Addon Manager has local library-management and runtime-bridge foundations. Local import, metadata, planning, an enabled flag, or successful source/CI checks do not prove that an addon is active in the game. Physical ARM64 runtime verification and actual in-game addon/Lua initialization have not been established. Check the live [Addon Manager issue](https://github.com/iNeonyne/LastHavenPlus/issues/23), its linked child issues, and current PRs for the latest scope and status.

The beta build excludes all Google Play services modules (`com.google.android.gms`) from the GeckoView runtime. As a result, GeckoView WebAuthn/passkey sign-in is unavailable in this build; use another sign-in method.

Site permissions are deny-by-default. Persistent storage and audible/inaudible autoplay are allowed only for lasthavens.com and its subdomains. Other website permissions, Android runtime-permission requests, and camera/microphone capture requests are denied. See [docs/GECKOVIEW_MIGRATION.md](docs/GECKOVIEW_MIGRATION.md) for the source-level contract and evidence.

## Build and test

Use the committed [Gradle Wrapper](gradle/wrapper/gradle-wrapper.properties) and the environment pins in [the Android workflow](.github/workflows/android.yml). Dependency/plugin versions are recorded in [gradle/libs.versions.toml](gradle/libs.versions.toml). Read these files for the exact current toolchain; do not copy version numbers from historical work logs or audit snapshots.

Common local checks:

```sh
./gradlew assembleRelease
./gradlew testDebugUnitTest
python3 -m unittest discover -s .github/scripts/tests -v
```

PR and release checks are defined by the live workflows under .github/workflows. PR validation is unsigned and must not require signing secrets. Signed release packaging and publication have separate authorization and verification gates.

## License and third-party notices

Original LastHavenPlus code and documentation are **All Rights Reserved** and are provided in this public repository for the copyright holder's personal use. No general license to copy, modify, publish, distribute, or otherwise reuse original project material is granted. See [LICENSE](LICENSE). Third-party components retain their own licenses and terms; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and the in-app Licenses & third-party notices viewer, which includes the packaged GeckoView 161 about:license inventory. This rights notice does not override third-party licenses or the terms governing the hosted LastHaven game. LastHavenPlus hosts the existing game and does not modify its client.

## Agent and project records

| File | Purpose |
|---|---|
| [AGENTS.md](AGENTS.md) | Single authoritative source for agent roles, permissions, workflow, and engineering rules |
| [AGENT_PROTOCOL.md](AGENT_PROTOCOL.md) | Startup sequence and bootstrap prompt |
| [AGENT_WORK.md](AGENT_WORK.md) | Historical work records only; not a current queue |
| [STATUS.md](STATUS.md) | Pointer to live sources; not a current status dashboard |
| [ISSUES.md](ISSUES.md) | Retired backlog mirror; GitHub Issues are authoritative |
| [docs/BRANCH_LIFECYCLE.md](docs/BRANCH_LIFECYCLE.md) | Branch naming, reuse, synchronization, retirement, and audit |
| [docs/GECKOVIEW_MIGRATION.md](docs/GECKOVIEW_MIGRATION.md) | GeckoView integration contract, migration notes, and evidence |
| [docs/CI_ACTIONS.md](docs/CI_ACTIONS.md) | CI permissions and supply-chain policy |
| [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md), [WORK_LOG.md](WORK_LOG.md) | Append-only history |

README is a stable product overview, not a live assignment queue, task backlog, or CI dashboard. Current assignments, blockers, PR state, and test results must be retrieved from GitHub at the time of use.

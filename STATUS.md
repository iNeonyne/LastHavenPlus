# Project status sources

This file is intentionally **not** maintained as a live project-status dashboard. Dated status snapshots became stale as issues and PRs moved faster than Markdown could be updated. Do not use this file to decide current priorities, ownership, blockers, implementation state, release status, or CI health.

Retrieve current facts from their authoritative sources at the time of use:

| Question | Authoritative source |
|---|---|
| Current beta commit | Live `beta` branch ref on GitHub |
| Current stable release and version | [Latest GitHub Release](https://github.com/iNeonyne/LastHavenPlus/releases/latest) and its tag/commit |
| Current GeckoView/toolchain versions | `gradle/libs.versions.toml`, Gradle files and the current build |
| Current issue scope, acceptance and owner | Live GitHub Issue body, labels and latest comments |
| Current integration work | Open pull requests targeting `beta`, including their latest head commits |
| Current verification | GitHub Actions/check runs for the exact commit being evaluated |
| Current runtime/device limitations | Latest issue/PR evidence from actual device testing; source inspection and CI are not device evidence |
| Durable contributor permissions | [AGENTS.md](AGENTS.md) |

## Project invariants

- Android 10 (API 29) and later; ARM64 only (`arm64-v8a`).
- Preserve application ID `com.neonyne.lasthavenlauncher` and the established release-signing identity.
- Use the GeckoView ARM64 Stable artifact pinned in the current version catalog; never infer the beta version from an older release.
- The launcher hosts the existing LastHaven game and must not modify the game client.
- Updates remain user-initiated. Do not introduce analytics, tracking, or silent browser-profile migration.
- Never claim physical ARM64 compatibility or actual in-game addon/Lua initialization without direct, recorded evidence.

This file replaces the former duplicated, dated assignment and status snapshots. Historical records remain available in Git history, [CHANGELOG-AGENTS.md](CHANGELOG-AGENTS.md), and [WORK_LOG.md](WORK_LOG.md).
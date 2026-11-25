# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator and currently owns integration, central documentation, review and release coordination. Ploy has no active assignment.

## Roles

- **Chaty:** intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone reviews and merges accepted PRs into `beta`.
- **Ploy:** may implement only a separately assigned issue. No active Ploy assignment currently exists.

## Shared workflow

- `beta` is the only active development, integration and testing branch. `main` is stable.
- Contributors work on dedicated task branches and open PRs targeting `beta`.
- Parallel work is allowed only for different issues with non-overlapping file scopes.
- Chaty alone reviews and merges accepted PRs into `beta`; contributors must not merge their own PRs or push directly to `beta` or `main`.
- PR CI for `beta` builds the unsigned release variant and runs unit tests; it does not use signing secrets or publish APKs. Chaty must verify signed build, signing identity and ARM64 packaging on the integrated beta commit.
- Promote to `main`, create a tag or publish a release only after release gates pass and the owner explicitly approves.
- GitHub Issues and `ISSUES.md` are the source of truth. TickTick is supplementary only.
- Physical-device behavior remains unverified until tested on real ARM64 hardware.

## Project constraints

- Kotlin and Jetpack Compose; Mozilla GeckoView 160.
- Android 10/API 29+; ARM64 only (`arm64-v8a`).
- Preserve application ID `com.neonyne.lasthavenlauncher` and established release signing identity.
- Do not modify the game/client.
- Do not add automatic update downloads or installation.
- Do not ship debug APKs or partially implemented features.
- Do not change Gradle, AGP, JDK, Android SDK or GeckoView versions without evidence and compatibility validation.
- Do not silently clear, migrate or overwrite GeckoView profile data or legacy Cefrium data.
- No analytics or tracking; never log cookies, tokens or session secrets.
- Do not make speculative performance/build-tool changes; measure before and after.

## Chaty — ACTIVE: release-workflow review (#31 / PR #32)

**Superseded PR:** https://github.com/iNeonyne/LastHavenPlus/pull/32  
**Replacement PR:** https://github.com/iNeonyne/LastHavenPlus/pull/34  
**Candidate head:** `751102ac2e00b4adb10c0ab8b34ac3017c5f6311`

Required before merge:
- Bound release-list API response bytes, not only elapsed time.
- Bound previous APK and manifest download bytes, not only elapsed time.
- Preserve safe full-APK fallback on every optional differential failure.
- Refresh branch from latest beta and resolve docs conflicts.
- Obtain successful exact-head unsigned PR build/unit-test CI.
- Retain final signed release, signer, ARM64 and asset-count verification.

PR #32's exact-head Actions runs #348 and #349 were cancelled; it is superseded. PR #34 run #351 is in progress. No passing validation is claimed yet.

## Chaty — ACTIVE: documentation reconciliation (#29)

Update README, STATUS, ISSUES, AGENT_WORK, CHANGELOG-AGENTS, WORK_LOG and GeckoView migration notes against current beta, current source, exact CI evidence and published v2.0.0 release metadata. Preserve historical changelog entries. Keep #29 open until the documentation PR is merged and verified.

## Chaty — PLANNED: addon manager integration (#23)

The owner explicitly approved resuming the supplied LastHavens Addon Manager XPI on 2026-11-25. A dedicated Addon Manager button must be on launcher Home, not Settings. Opening the manager must not initialize a game session; the existing game GeckoSession must be retained, and manager Play must resume that same session.

The XPI audit and complete requirements are in GitHub issue #23. Before implementation:
1. Verify exact WebExtension APIs against the pinned GeckoView 160 artifact.
2. Confirm supported built-in installation of the unsigned extension from APK assets.
3. Design app-owned session/tab delegation and navigation.
4. Assign separate non-overlapping subissues/files only after feasibility is confirmed.

No addon-manager code has been integrated yet. Do not modify the game client, add automatic app updates or claim addon initialization solely from observed injection requests.

## Owner/device tester — P0: physical ARM64 verification (#5)

Install and test the signed release APK on real ARM64 hardware. Verify gameplay, WebGL/WASM, WebSockets, touch, audio, fullscreen, navigation, profile persistence, process recovery and same-key update installation. Record exact device, Android version, commit, APK digest and outcomes. Do not mark this complete from CI.

## Paused / queued work

- #28 storage measurement accuracy and iterative traversal.
- #9 accessibility follow-up.
- #11 diagnostics test expansion and redaction/log-bound verification.
- #12 load-error details; #14 connection diagnostics.
- #16 gameplay-only orientation/keep-awake; #17 settings backup/restore.
- #7 runtime performance/APK size: measure first.
- #19 opt-in beta channel only if owner prioritizes it.

One active owner per issue. Do not assign overlapping scopes or begin work outside an explicit issue assignment.

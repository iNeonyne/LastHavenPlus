# LastHavenPlus roadmap

**Last updated:** 2026-11-28  
**Latest stable release:** [v2.4.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.4.0)

This is the single strategic roadmap for the project. It records the current priorities and intended sequence, not individual agent assignments. GitHub Issues, their current labels/comments, pull requests and Actions remain authoritative for implementation scope and live task state. The repository owner controls priority changes and release decisions.

## Current priorities

### 1. Complete the Addon Manager software integration

Finish the remaining code-level integration while preserving the existing game session and app-private addon library.

- [#147 — Consolidate launcher navigation and back-stack state](https://github.com/iNeonyne/LastHavenPlus/issues/147). Navigation implementation is submitted in [PR #204](https://github.com/iNeonyne/LastHavenPlus/pull/204); Chaty owns integration and merge.
- [#183 — Disconnect stale addon bridge ports when GeckoSession changes](https://github.com/iNeonyne/LastHavenPlus/issues/183). Fix stale-session port cleanup and cover session replacement/teardown with automated regression tests.
- [#170 — Integrate Addon Manager navigation and same-session behavior](https://github.com/iNeonyne/LastHavenPlus/issues/170). Complete the overlay entry/return flow and automated lifecycle/navigation tests after the preceding work is integrated.

The former physical-device runtime acceptance task [#167](https://github.com/iNeonyne/LastHavenPlus/issues/167) is out of scope and closed. This project does not perform physical-device testing. Acceptance is based on source review, coding/debugging, automated tests and CI; do not make unsupported claims about actual in-game addon execution.

### 2. Resume updater reliability work

After the Addon Manager software integration is safely handed off, resume the coordinated updater reliability work:

- [#4 — Build reproducibility and updater test coverage](https://github.com/iNeonyne/LastHavenPlus/issues/4).
- [#15 — Updater recovery](https://github.com/iNeonyne/LastHavenPlus/issues/15).
- [#111 — Update progress and retry feedback](https://github.com/iNeonyne/LastHavenPlus/issues/111), sequenced after the recovery work.

Keep overlapping updater implementation under one owner. Preserve verified full-APK fallback, bounded downloads, digest/signature validation and explicit user-controlled installation. The live issue assignments determine when this work resumes.

### 3. Improve settings and diagnostics

Once higher-priority reliability work is complete, revisit the scoped settings and diagnostics backlog. Relevant work includes [storage accounting (#28)](https://github.com/iNeonyne/LastHavenPlus/issues/28), [settings (#6)](https://github.com/iNeonyne/LastHavenPlus/issues/6), [orientation and keep-awake (#16)](https://github.com/iNeonyne/LastHavenPlus/issues/16), [load-error details (#12)](https://github.com/iNeonyne/LastHavenPlus/issues/12), [connection diagnostics (#14)](https://github.com/iNeonyne/LastHavenPlus/issues/14), [network totals (#10)](https://github.com/iNeonyne/LastHavenPlus/issues/10) and [settings backup (#17)](https://github.com/iNeonyne/LastHavenPlus/issues/17).

These are strategic areas, not simultaneous assignments. Chaty should sequence them from the live dependencies and owner priorities before work starts.

## Ongoing engineering constraints

- ARM64 only (`arm64-v8a`); preserve the application ID and established signing identity.
- Use the GeckoView ARM64 Stable artifact pinned in the Gradle version catalog.
- Keep the existing game and browser profile data safe; do not modify game-hosted files.
- Keep update checks, downloads and installation user-initiated.
- Prefer coding, debugging and automated regression tests. Physical-device testing is not part of project acceptance.
- Require exact-head CI for changes and avoid speculative dependency/toolchain changes.

## Roadmap maintenance

Update this file when strategic priorities, sequence or release direction changes. Keep detailed acceptance criteria, ownership, blockers and progress in the linked GitHub Issues and pull requests; do not duplicate them here. Release versions and publication require explicit repository-owner approval.

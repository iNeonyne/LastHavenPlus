# LastHavenPlus — repository instructions

These rules apply to every human and AI contributor. The repository owner makes final product and release decisions.

## Project goal

Maintain a lightweight Android launcher for the existing LastHaven Wrath of the Lich King browser game at [lasthavens.com/play](https://lasthavens.com/play). The app hosts the existing game; do not recreate, modify or develop the game client.

The active embedded browser engine in `beta` is Mozilla GeckoView 160. Do not add Cefrium, Android System WebView or a second/fallback engine.

## Branch and integration workflow

- **`beta` is the active development, integration and testing branch.**
- **`main` is the stable release branch.**
- Pull requests are the normal integration path for task work. Open PRs against `beta`; do not target `main` for routine development.
- Parallel agents may work on separate issues only when their file scopes do not overlap. Never let two agents work on the same issue concurrently.
- Contributors work on dedicated task branches, open PRs targeting `beta`, and provide Chaty the PR link, exact head commit, changed files, test evidence and remaining limitations.
- **Chaty is the sole orchestrator and integrator.** Chaty reviews PR diffs and CI, resolves conflicts, requests changes, and merges accepted PRs into `beta`. Chaty coordinates verification and release readiness; no other agent may merge or push directly to `beta`.
- Promote `beta` to `main` only after all release gates pass and the owner explicitly approves. Publishing a release or creating a release tag also requires explicit approval.
- Never develop routinely on `main`, force-push shared branches or bypass required checks.

## Review depth and change sizing

Review effort must match risk without removing the review gate:

- **Lightweight review:** documentation-only, wording/copy, or isolated low-risk changes. Verify scope, links, factual accuracy and absence of behavior/security changes.
- **Standard review:** normal UI, settings, diagnostics and contained application changes. Inspect affected call paths, regression risks, relevant tests and the required unsigned PR build.
- **Strict review:** GeckoView/session lifecycle, updater/download/patching, storage/profile, network/security, dependencies/toolchain, manifest/permissions, signing and CI/release workflows. Inspect success and failure paths, require focused regression tests, exact-head CI, and relevant post-merge signed/packaging checks. Require physical ARM64 testing for claims that depend on device behavior.

All PRs remain subject to Chaty's review and required CI. “Lightweight” means a smaller review scope, not automatic approval, bypassed CI, or permission to merge. Keep one issue per PR, avoid unrelated cleanup, and keep file ownership non-overlapping. Contributors must provide a concise handoff: outcome, scope/non-goals, changed files, exact head SHA, test commands/results and CI links, limitations, and physical-device status. If `beta` advances, refresh the branch and rerun affected checks before merge.

## Before changing code

1. Read `README.md`, this file, `AGENT_WORK.md`, `STATUS.md`, the relevant sections of `ISSUES.md`, and the latest entries in `CHANGELOG-AGENTS.md` and `WORK_LOG.md`.
2. Read the relevant GitHub issue and search open and closed issues for duplicates.
3. Confirm the issue, owner, exact subtask, allowed files, branch and dependencies in `STATUS.md` before editing.
4. Read `docs/GECKOVIEW_MIGRATION.md` before changing or debugging GeckoView integration.
5. Work only within the assigned scope. If the scope or ownership is unclear, stop and ask Chaty to resolve it.

## Ownership

- Chaty owns intake, triage, issue creation/updates, central documentation, assignments, review, integration, CI coordination and release coordination.
- Ploy works only on the assignment explicitly listed in `AGENT_WORK.md`. Ploy must not change central trackers or independently integrate/push to `beta` or `main`.
- One active owner per issue. A handoff is complete only after the previous owner stops and the assignment/status records are updated.
- GitHub Issues and `ISSUES.md` are the project task records. TickTick may be used as a supplementary personal task tracker; it is not the source of truth.

## Technical constraints

- **ARM64 only:** package `arm64-v8a`; do not add other ABIs.
- **Application identity:** preserve `com.neonyne.lasthavenlauncher`.
- **Release signing:** preserve the established release signing identity. Never commit keystores or credentials.
- **Updates:** keep downloads and installation user-initiated. Do not add automatic downloading or installation.
- **Release artifacts:** never publish debug APKs or partially implemented features.
- **Game boundary:** do not modify the game/client or claim game capabilities not verified in the launcher.
- **Data safety:** do not silently clear, migrate or overwrite browser profiles, cookies, LocalStorage, IndexedDB or other game data. Do not copy Cefrium data into GeckoView; no supported cross-engine migration has been established.
- **Privacy:** do not add analytics or tracking. Never log cookies, tokens, authorization headers or other session secrets.
- **Optimization:** do not make speculative performance or build-tool changes. Record before/after measurements and compatibility evidence.

## GeckoView

The current beta uses the ARM64 GeckoView 160 artifact. Keep the runtime process-scoped and manage GeckoSession lifecycle explicitly. Use the exact API from the pinned artifact and official Mozilla documentation; do not infer APIs from unrelated versions.

A successful CI build does not prove browser profile persistence or game compatibility. Physical-device verification must be performed on real ARM64 hardware and recorded with device, Android version, build commit and test results.

See [Mozilla's GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html) and the project-specific [migration notes](docs/GECKOVIEW_MIGRATION.md).

## Addon boundary

The previous ZIP addon package manager has been removed. Addon-manager work is on hold until the owner supplies a new written specification. Do not research, design, implement or restore it before then.

A WoW 3.3.5a addon ZIP/`.toc` package is not a Firefox WebExtension. Local package storage is not game injection. Never claim that the launcher loads game addons unless a separately approved implementation is actually verified.

## Tests and documentation

- Run the checks relevant to the changed scope and report exact commands/results. Never report a build or test as passing without evidence.
- PRs targeting `beta` run an unsigned `assembleRelease` plus `testDebugUnitTest` validation with read-only permissions. This job does not access signing secrets or publish APKs. The normal beta push pipeline remains responsible for signed APK, certificate, ABI and artifact checks.
- Update `STATUS.md` with verified state, unverified behavior, blockers, active ownership and exact CI evidence.
- Update `ISSUES.md` when task scope/progress changes; do not close an issue merely because documentation changed.
- Append meaningful work to `CHANGELOG-AGENTS.md` and `WORK_LOG.md`. Preserve the work log as an append-only history.
- Keep documentation consistent with inspected source and CI. Clearly distinguish implemented, tested, unverified and incomplete behavior.

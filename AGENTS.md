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
- Task branches use `<owner>/<task-slug>` naming, one issue per branch, started from current `beta`. `infra/*` branches never merge wholesale into `beta`; `beta-legacy-*` snapshots are preserved and never wholesale-merged.
- To sync a branch with an advanced `beta`, merge `origin/beta` into it (never rebase pushed history), rerun affected checks, and update the PR handoff with the new exact head SHA.
- Branch deletion/retirement requires explicit owner approval recorded in the disposal log of `docs/BRANCH_LIFECYCLE.md`. That document also holds the current branch inventory, integration history and naming/reuse procedures; refresh it before any cleanup decision.

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

## Agent roles, ownership and communication

- **Chaty** is the orchestrator, issue intake/triage owner, assignment coordinator, reviewer, integration owner, CI/documentation coordinator and release coordinator. Chaty may also implement assigned work.
- **Vibe** is an implementation contributor. Vibe may implement only an existing GitHub issue explicitly assigned by Chaty, then submit a PR targeting `beta`. Vibe must not create implementation PRs for untracked work, change priorities, expand scope without approval, merge PRs, or push to `beta`/`main`.
- **GitHub is the only agent-to-agent communication channel.** Use issue comments for assignments, questions, progress, blockers and handoffs; PR descriptions/reviews for implementation and corrections; commits and Actions for evidence. Do not rely on private agent messages or undocumented handoffs.
- **One active implementation owner per issue.** Chaty checks ownership before assigning work. Chaty and Vibe must not implement the same issue concurrently or edit overlapping files without an explicit coordinated plan.
- Vibe posts a GitHub comment when starting, when blocked, and when handing off a PR. Chaty records assignments and status in GitHub and `AGENT_WORK.md`.
- GitHub Issues and `ISSUES.md` are the task sources of truth. TickTick may be supplementary only.

### Agent ownership labels

Exactly one `agent:*` ownership label should describe each actionable issue's current state:

| Label | Meaning |
|---|---|
| `agent:unassigned` | No agent owns it; Chaty may triage and assign it |
| `agent:chaty` | Chaty owns the work |
| `agent:vibe` | Vibe has an explicit assignment and may start |
| `agent:blocked` | Work is on hold or waiting on a dependency/decision; neither agent starts |
| `agent:review` | Implementation PR is submitted and awaiting Chaty's review |
| `agent:owner` | Waiting for a repository-owner decision or feedback; not an agent assignment |

- New issues receive `agent:unassigned` automatically.
- Only Chaty changes ownership labels. Replace the previous `agent:*` label when an issue changes state; do not leave conflicting ownership labels.
- An `agent:unassigned` label is not permission to begin work. Vibe must wait for Chaty's explicit `agent:vibe` assignment and scope comment.
- On PR submission, Chaty changes the issue to `agent:review`; after review, Chaty either assigns `agent:vibe` for revisions, retains `agent:chaty`, or returns it to `agent:unassigned`.
- After merge, Chaty updates the issue's ownership label and completion state based on verified evidence. Do not close issues merely because documentation changed.
- The label automation is defined in `.github/workflows/agent-ownership.yml`. GitHub issue-event workflows must exist on the repository default branch to run automatically. A beta-only PR therefore prepares the automation but does not activate issue-open labeling until the owner separately approves promotion of that workflow to the default branch.

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

The previous ZIP addon package manager has been removed. Issue #23 and all addon-manager work are on hold by owner instruction (2026-11-25). Do not resume investigation, implementation, assignment or review until the owner explicitly approves resuming it. Do not restore the legacy ZIP manager.

Before any XPI install path is integrated, verify the pinned GeckoView 160 extension APIs, unsigned XPI behavior, install confirmation, session/tab lifecycle and archive/network security constraints. A WoW 3.3.5a addon ZIP/`.toc` package is not a Firefox WebExtension. Local package storage is not game injection. Never claim that the launcher loads game addons unless a separately approved implementation is actually verified.

## Tests and documentation

- Run the checks relevant to the changed scope and report exact commands/results. Never report a build or test as passing without evidence.
- PRs targeting `beta` run an unsigned `assembleRelease` plus `testDebugUnitTest` validation with read-only permissions. This job does not access signing secrets or publish APKs. The normal beta push pipeline remains responsible for signed APK, certificate, ABI and artifact checks.
- Android PR validation runs `lintDebug`, Spotless/ktfmt formatting, the unsigned release build, JVM unit tests and Python updater-helper tests in one Gradle pass with read-only permissions and no signing secrets. On beta pushes, the signed ARM64 build job conditionally runs `lintDebug` and `spotlessCheck` in the same Gradle invocation when relevant source/build/script files changed; it fetches only the exact previous beta commit as the formatting baseline. There is no separate post-merge quality runner. The beta push job remains trusted and performs the established signer and ABI checks.
- Kotlin formatting is incremental against the PR base (or previous beta commit on push). Run `./gradlew spotlessApply` to format changed Kotlin/Kotlin Gradle files and `./gradlew spotlessCheck` to verify them. Documentation-only changes do not trigger Android quality/build workflows.
- Update `STATUS.md` with verified state, unverified behavior, blockers, active ownership and exact CI evidence.
- Update `ISSUES.md` when task scope/progress changes; do not close an issue merely because documentation changed.
- Append meaningful work to `CHANGELOG-AGENTS.md` and `WORK_LOG.md`. Preserve the work log as an append-only history.
- Keep documentation consistent with inspected source and CI. Clearly distinguish implemented, tested, unverified and incomplete behavior.

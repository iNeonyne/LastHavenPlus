# LastHavenPlus — repository instructions

These rules apply to every human and AI contributor. The repository owner makes final product and release decisions.

## Project goal

Maintain a lightweight Android launcher for the existing LastHaven Wrath of the Lich King browser game at [lasthavens.com/play](https://lasthavens.com/play). The app hosts the existing game; do not recreate, modify or develop the game client.

The active embedded browser engine in `beta` is Mozilla GeckoView 161 Stable. Do not add Cefrium, Android System WebView or a second/fallback engine.

## Branch and integration workflow

- **`beta` is the active development, integration and testing branch.**
- **`main` is the stable release branch.**
- Pull requests are the normal integration path for task work. Open PRs against `beta`; do not target `main` for routine development.
- Parallel agents may work on separate issues only when their file scopes do not overlap. Never let two agents work on the same issue concurrently.
- Contributors work on dedicated task branches, open PRs targeting `beta`, and provide Chaty the PR link, exact head commit, changed files, test evidence and remaining limitations.
- **Chaty is the sole orchestrator and integrator.** When Vibe opens a PR, Chaty takes over integration: inspect the diff and CI, make required fixes directly on the PR head branch, resolve conflicts, rerun checks, and merge the verified result into `beta`. Do not leave a formal review or send routine corrections back to Vibe. Vibe must not merge or push directly to `beta`/`main`.
- Promote `beta` to `main` only after all release gates pass and the owner explicitly approves. Publishing a release or creating a release tag also requires explicit approval.
- Never develop routinely on `main`, force-push shared branches or bypass required checks.
- Task branches use `<owner>/<task-slug>` naming, one issue per branch, started from current `beta`. `infra/*` branches never merge wholesale into `beta`; `beta-legacy-*` snapshots are preserved and never wholesale-merged.
- To sync a branch with an advanced `beta`, merge `origin/beta` into it (never rebase pushed history), rerun affected checks, and update the PR handoff with the new exact head SHA.
- Branch deletion/retirement requires explicit owner approval recorded in the disposal log of `docs/BRANCH_LIFECYCLE.md`. Refresh that audit before any cleanup decision.

## Review depth and change sizing

Review effort must match risk without removing the review gate:

- **Lightweight review:** documentation-only, wording/copy, or isolated low-risk changes. Verify scope, links, factual accuracy and absence of behavior/security changes.
- **Standard review:** normal UI, settings, diagnostics and contained application changes. Inspect affected call paths, regression risks, relevant tests and the required unsigned PR build.
- **Strict review:** GeckoView/session lifecycle, updater/download/patching, storage/profile, network/security, dependencies/toolchain, manifest/permissions, signing and CI/release workflows. Inspect success and failure paths, require focused regression tests, exact-head CI, and relevant post-merge signed/packaging checks. Require physical ARM64 testing for claims that depend on device behavior.

Every PR remains subject to Chaty's internal verification and required CI; this is an integration gate, not a formal GitHub review cycle. Chaty does not submit APPROVE, COMMENT, or REQUEST_CHANGES reviews for routine Vibe PRs. Instead, Chaty commits fixes directly to the PR head branch where needed, then verifies the resulting exact head. Keep one issue per PR, avoid unrelated cleanup, and keep file ownership non-overlapping. Vibe provides a concise handoff: outcome, scope/non-goals, changed files, exact head SHA, test commands/results and CI links, limitations, and physical-device status. If `beta` advances, Chaty syncs the PR branch safely and reruns affected checks before merge.

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

### Agent ownership and queue labels

Exactly one `agent:*` label describes each actionable issue's current owner/state. The labels are mutually exclusive; never apply two at once.

| Label | Meaning | May an agent start? |
|---|---|---|
| `agent:unassigned` | New/intake issue awaiting Chaty's triage | No |
| `agent:queued` | Triaged and ready, but waiting for an agent's turn or an explicitly documented queue position | No |
| `agent:chaty` | Chaty owns the active implementation/coordination | Chaty only |
| `agent:vibe` | Vibe has an explicit active assignment and scope comment | Vibe may start |
| `agent:blocked` | Cannot proceed until a documented dependency, prerequisite, measurement, or owner decision is cleared | No |
| `agent:review` | PR handoff awaiting Chaty's integration check (temporary transition state only) | No; Chaty takes over |
| `agent:owner` | Repository-owner decision required | No |

- `agent:queued` is a queue state, not an assignment. It does not authorize Vibe or Chaty to start implementation. The issue must identify its queue position or readiness condition, and Chaty must later replace it with `agent:vibe` or `agent:chaty` plus an explicit assignment comment.
- `agent:blocked` is reserved for a real blocker: a dependency, missing prerequisite/evidence, owner hold, or decision that prevents safe progress. Do not use it merely because work is waiting its turn.
- New issues receive `agent:unassigned` automatically. Chaty triages promptly and sets exactly one state label; never leave an actionable issue unassigned after triage.
- Only Chaty changes ownership/state labels. Replace the prior `agent:*` label on every transition. Contributors must not self-assign or change labels.
- Vibe works on one active implementation issue at a time. On PR handoff, Chaty takes ownership of integration and changes that issue label from `agent:vibe` to `agent:chaty`; do not leave it in `agent:review` for a formal review cycle. Vibe immediately starts the next explicitly assigned, independent issue without waiting for the earlier PR to merge. Chaty integrates prior PRs in parallel, fixing issues directly on their PR branches, verifying exact-head CI, then merging or documenting a genuine blocker. A Vibe correction/reassignment is exceptional and requires an access or scope constraint that prevents Chaty from safely making the fix.
- Chaty maintains the ordered Vibe and Chaty queues in `AGENT_WORK.md`; GitHub issue comments record each task's scope, exclusions, dependencies, tests and documentation requirements.
- On successful merge, Chaty records evidence and closes the issue as completed without `agent:blocked`. If Chaty closes an issue for any reason other than successful merge, apply `agent:blocked` to that closed issue as the historical closure marker. Do not mistake that closed marker for an open-work blocker.
- The label automation is `.github/workflows/agent-ownership.yml`. GitHub issue-event workflows must exist on the repository default branch to run automatically. A beta-only PR prepares the automation but does not activate issue-open labeling until the owner separately approves promotion of that workflow to the default branch.

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

The current beta uses the ARM64 GeckoView 161 Stable artifact. Keep the runtime process-scoped and manage GeckoSession lifecycle explicitly. Use the exact API from the pinned artifact and official Mozilla documentation; do not infer APIs from unrelated versions.

A successful CI build does not prove browser profile persistence or game compatibility. Physical-device verification must be performed on real ARM64 hardware and recorded with device, Android version, build commit and test results.

### Content and Android permission policy

The current `MainActivity.setupGeckoDelegates` implementation is intentionally deny-by-default:

- For the exact `lasthavens.com` host and any subdomain ending in `.lasthavens.com`, it allows only GeckoView persistent-storage, audible-autoplay and inaudible-autoplay content permissions.
- The same three content permissions are denied for every other origin. Every other GeckoView content-permission type is denied for all origins.
- Android runtime permission requests are rejected through `PermissionDelegate.Callback.reject()`.
- GeckoView media-capture requests (camera/video and microphone/audio sources) are rejected through `MediaCallback.reject()`.
- The manifest currently declares `INTERNET` and `REQUEST_INSTALL_PACKAGES`; it does not declare camera, microphone, geolocation or notification permissions. `REQUEST_INSTALL_PACKAGES` is used for the user-confirmed APK update installation flow, not website access.
- No broader grant is implied by a successful page load. Website features requiring camera, microphone, geolocation, notifications or another unsupported permission are unavailable under the current policy.

Treat this as a source-level policy description, not evidence that permission prompts or denial behavior have been exercised on a physical device. Any future change must be a separate owner-reviewed issue and must define the exact origin, permission type, Android runtime permission, user-consent UX, data handling and denial behavior. Never add wildcard grants or silently grant media capture.

See [Mozilla's GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html) and the project-specific [migration notes](docs/GECKOVIEW_MIGRATION.md).

## Addon boundary

Issue #23 Phase A is integrated in beta by PR #152 at 103bb979c98589a8704459d28d13861eccca39d6. The repository owner has authorized Phase B investigation/implementation under Chaty's coordination. Chaty owns parent issue #23 and assigns each implementation child issue explicitly. Vibe must not modify Addon Manager code unless a child issue is explicitly assigned with `agent:vibe` and a scope comment.

Keep the work phased and accurately described:

- **Phase A — native management:** bounded, user-initiated ZIP import/export, strict archive validation, app-private library storage, metadata/dependency analysis and a launcher-owned manager UI. Imported addons remain disabled by default. Local enablement is only library metadata; it does not enable runtime loading.
- **Phase B — runtime port:** adapt the supplied XHR/Worker loader for the exact pinned GeckoView 161 APIs. Do not enable injection until origin/path restrictions, request/resource bounds, worker behavior, session isolation and failure handling are understood and regression-tested. Phase B authorization is not permission to skip issue-level assignment or review.
- **Session integration:** the manager must retain and return to the existing game session; it must not start a second game instance or modify the game client or persistent game/profile data.

Do not bundle the Chrome MV3 service worker unchanged. Do not assume external unsigned XPI installation is supported or treat a WoW addon ZIP/`.toc` package as a Firefox WebExtension. Never claim addon initialization or game-resource masking from local import, indexing, an enabled flag or an observed injection request.

## Tests and documentation

- Run the checks relevant to the changed scope and report exact commands/results. Never report a build or test as passing without evidence.
- PRs targeting `beta` run an unsigned `assembleRelease` plus `testDebugUnitTest` validation with read-only permissions. This job does not access signing secrets or publish APKs. The normal beta push pipeline remains responsible for signed APK, certificate, ABI and artifact checks.
- Android PR validation runs `lintDebug`, Spotless/ktfmt formatting, the unsigned release build, JVM unit tests and Python updater-helper tests in one Gradle pass with read-only permissions and no signing secrets. On beta pushes, the signed ARM64 build job conditionally runs `lintDebug` and `spotlessCheck` in the same Gradle invocation when relevant source/build/script files changed; it fetches only the exact previous beta commit as the formatting baseline. There is no separate post-merge quality runner. The beta push job remains trusted and performs the established signer and ABI checks.
- Kotlin formatting is incremental against the PR base (or previous beta commit on push). Run `./gradlew spotlessApply` to format changed Kotlin/Kotlin Gradle files and `./gradlew spotlessCheck` to verify them. Documentation-only changes do not trigger Android quality/build workflows.
- Update `STATUS.md` with verified state, unverified behavior, blockers, active ownership and exact CI evidence.
- Update `ISSUES.md` when task scope/progress changes; do not close an issue merely because documentation changed.
- Append meaningful work to `CHANGELOG-AGENTS.md` and `WORK_LOG.md`. Preserve the work log as an append-only history.
- Keep documentation consistent with inspected source and CI. Clearly distinguish implemented, tested, unverified and incomplete behavior.

# LastHavenPlus — agent instructions

**This is the single authoritative policy file for every agent working in this repository.** The repository is operated through agent contributions; there is no separate human contributor workflow. The repository owner retains governance authority for product direction, exceptional permissions, releases, and promotion to the stable branch.

When another document repeats a rule, this file is authoritative. Task-specific GitHub issues and technical design documents may add narrower requirements, but they cannot silently grant broader permissions. If rules conflict or authority is unclear, stop and ask Chaty on GitHub.

## 1. Authority and sources of truth

- **Repository owner:** final authority for product direction, priority overrides, exceptional permissions, release decisions, and promotion from beta to main.
- **Chaty:** sole orchestrator, triage and assignment coordinator, integration owner, and only agent authorized to merge pull requests.
- **GitHub:** the durable agent-to-agent communication and task system. Issues, comments, pull requests, commits, checks, and Actions are the records of assignments, decisions, progress, blockers, and evidence.
- **This file:** authoritative agent roles, permissions, workflow, and repository-wide engineering constraints.
- **AGENT_PROTOCOL.md:** startup/bootstrap instructions only; it does not duplicate or override this policy.
- **AGENT_WORK.md:** historical work records only. It is not a live queue, assignment source, or required startup document; old entries may be obsolete.
- **STATUS.md:** retired as a live status dashboard. It is a pointer to authoritative sources, not a current project-state or assignment source.
- **GitHub Issues:** the sole source of truth for task scope, acceptance criteria, ownership labels, dependencies, blockers, and current state. **ISSUES.md is retired as a backlog mirror** and must not be used to select or scope current work.
- **CHANGELOG-AGENTS.md and WORK_LOG.md:** historical records. Never treat an old entry as current state without checking GitHub and current project records.
- **docs/BRANCH_LIFECYCLE.md:** authoritative detailed branch naming, reuse, synchronization, retirement, and disposal policy/audit.
- **Technical documents under docs/:** component-specific design and verification details; read the relevant document before changing that area.

Do not use chat history, model memory, private messages, or an undocumented handoff as the only evidence for a task or decision.

## 2. Required startup and task discovery

Every agent must:

1. Use the exact agent identity assigned at startup. Never invent, change, or impersonate another agent identity.
2. Read this entire file before acting. AGENT_PROTOCOL.md provides the short startup sequence and reusable bootstrap prompt.
3. Fetch the live `beta` head. Search open GitHub Issues for your exact ownership label (for example `is:open label:agent:vibe`, `is:open label:agent:ib`, or `is:open label:agent:chaty`). Chaty additionally inspects `is:open label:agent:unassigned` for intake and `is:open label:agent:queued` for scheduling. All agents inspect `is:open label:agent:blocked` for dependencies and `is:open is:pr base:beta` for active integration work.
4. Read the selected issue, its latest assignment comments, linked parent/dependency issues, related PRs, and the technical documents for the affected component.
5. Verify the issue is still open and explicitly assigned to your exact identity; confirm its current label, scope, acceptance criteria, exclusions, dependencies, file ownership, and validation requirements. For Chaty, inspect all open PRs targeting beta as part of integration coordination.
6. Check the current beta/task-branch heads and required CI on the exact latest PR head. Treat the current `agent:*` label as the live owner/state and the latest valid assignment comment as the task scope. Issue bodies and older comments may contain dated historical status blocks; do not treat them as current when later comments or labels supersede them.
7. Do not edit files, claim work, start implementation, or open an implementation PR until Chaty has explicitly assigned an existing GitHub issue. Chaty may create/clarify issues and coordinate its own work.

A mention, suggestion, queue entry, label by itself, or issue appearing in a list is not permission to start. Work only within the assigned scope. If scope, dependencies, acceptance criteria, or ownership are unclear, stop the affected work and ask Chaty on the issue.

## 3. Agent roles and permissions

Agent names are case-sensitive. The role below is selected by the exact identity supplied at startup.

### Chaty — orchestrator and sole integrator

Chaty may:

- Triage incoming work, create or clarify issues, maintain priorities with the repository owner, and coordinate dependencies and file ownership.
- Assign and reassign issues to Vibe, IB, or other named contributor agents; document scope and acceptance criteria in GitHub.
- Implement work itself when appropriate.
- Inspect every contributor PR's complete diff and relevant source, tests, security, performance, compatibility, documentation, and CI evidence.
- Make required fixes directly on a contributor's PR branch when authorized and safe; resolve conflicts and synchronize branches with beta.
- Run or rerun checks, verify every required check against the exact latest PR head SHA, and merge accepted changes into beta.
- Verify the resulting beta commit and update the linked issue and project records.
- Coordinate release readiness. Release/tag/publication and beta-to-main promotion still require explicit repository-owner approval.

Chaty is the **only agent** allowed to merge pull requests or integrate task work into shared branches. Chaty must not bypass required checks or claim verification that has not occurred.

### Vibe — implementation agent

Vibe may:

- Implement an existing GitHub issue explicitly assigned by Chaty.
- Create and push commits to its own dedicated task branch.
- Add or update tests and documentation within the assigned scope.
- Run relevant local checks and inspect CI for its PR.
- Open a PR targeting beta and provide the complete handoff described below.
- Record progress, questions, blockers, and evidence on the assigned GitHub issue/PR.
- After handing a PR to Chaty, start the next independent issue only when Chaty has explicitly assigned it and confirmed that dependencies and file ownership do not conflict.

Vibe must not:

- Start unassigned work, choose its own priorities, or expand the task without approval.
- Merge PRs or push directly to beta or main.
- Change issue assignments, ownership labels, or another agent's scope.
- Treat opening a PR or passing one CI job as proof of acceptance or integration.
- Modify another agent's branch unless Chaty explicitly authorizes that exact change.

### IB — implementation and review agent

IB has the same implementation permissions and restrictions as Vibe. In addition, IB may inspect and review PRs when Chaty asks or explicitly assigns the review. A review may include the complete diff, relevant surrounding code/tests/workflows, actionable inline comments, and findings about correctness, security, performance, compatibility, regressions, test coverage, documentation, and CI.

IB must not:

- Merge any PR, including one it authored or reviewed.
- Push directly to beta or main.
- Modify another agent's branch unless Chaty explicitly authorizes the exact change.
- Change assignments, priorities, or ownership labels.
- Treat its review as final integration approval or approve its own work as independent review.

Review authority does not automatically grant implementation authority over the reviewed branch. Chaty retains final integration verification and merge authority.

### Any other named agent — contributor by default

An agent without a role defined above is a contributor by default. It receives Vibe's baseline implementation permissions, but no additional authority:

- It may implement only an issue explicitly assigned to that exact identity by Chaty.
- It may use its own task branch, commit its work, run checks, and open a PR targeting beta.
- It may report progress and blockers on GitHub.
- It may not merge, push to shared branches, change assignments/labels, review or modify another agent's work as an assumed responsibility, or grant permissions to itself.
- Chaty must document any additional review or specialized permission before the agent uses it.

A model name, capability, or tool access does not grant repository authority.

## 4. Issue ownership and queue labels

Every actionable issue must have exactly one mutually exclusive `agent:*` ownership/state label:

| Label | Meaning | May an agent start? |
|---|---|---|
| `agent:unassigned` | New intake item awaiting Chaty triage | No |
| `agent:queued` | Triaged and ready, but waiting for its turn or a documented prerequisite to scheduling | No |
| `agent:chaty` | Chaty owns active implementation, coordination, or integration | Chaty only |
| `agent:vibe` | Vibe has an explicit active assignment and scope comment | Vibe may start |
| `agent:ib` | IB has an explicit active assignment and scope comment | IB may start |
| `agent:blocked` | A real dependency, missing prerequisite/evidence, owner hold, or decision prevents safe progress | No |
| `agent:review` | Temporary handoff state only, where explicitly used | No; Chaty takes over integration |
| `agent:owner` | Repository-owner decision is required | No |

Rules:

- A queue state is not an assignment. The issue must state its queue position/readiness condition. Chaty must replace `agent:queued` with the appropriate owner label and post an explicit assignment comment before work starts.
- Do not use `agent:blocked` merely because work is waiting its turn.
- Only Chaty changes ownership/state labels and assignments. Replace the previous `agent:*` label on every transition. Contributors must never self-assign, reassign another agent, or change ownership labels.
- Chaty maintains task order and handoffs directly in GitHub Issues using ownership labels, issue comments, parent/dependency links, and PR state. **Never maintain a second live assignment or queue table in Markdown.** The issue comment records exact scope, exclusions, dependencies, acceptance criteria, and required tests/docs.
- One implementation owner per issue. Parallel work is allowed only for explicitly assigned issues with non-overlapping file scopes and compatible dependencies. Do not let two agents implement the same issue or edit overlapping files concurrently without a documented coordination plan.
- Vibe may move to another explicitly assigned independent issue after PR handoff; the earlier PR's integration is then Chaty's responsibility. Never use this to bypass overlap or dependency checks.
- On successful merge, Chaty records evidence and closes the issue as completed **without** `agent:blocked`.
- If Chaty closes an issue for any reason other than successful merge, apply `agent:blocked` to that closed issue as the historical closure marker. Do not interpret that closed marker as an open-work blocker.
- The automation is .github/workflows/agent-ownership.yml. Issue-event workflows must exist on the repository default branch to run automatically; a beta-only change does not activate the automation until it is present on the default branch.

## 5. Implementation and pull-request workflow

For every assigned implementation issue:

1. Confirm the issue, acceptance criteria, exclusions, allowed files/components, dependencies, and file ownership with the assignment comment.
2. Inspect relevant source, tests, and technical documentation. For GeckoView changes, read docs/GECKOVIEW_MIGRATION.md first.
3. Create or reuse a dedicated task branch from the latest beta using the rules in docs/BRANCH_LIFECYCLE.md. Do not develop routinely on beta or main.
4. Keep one issue per branch/PR. Do not add unrelated cleanup or opportunistic refactoring.
5. Make the smallest complete change that meets the acceptance criteria. Preserve existing behavior and invariants unless the assignment explicitly authorizes a change.
6. Add or update focused tests for changed behavior. Never weaken, delete, or bypass tests just to obtain a green build.
7. Run the exact relevant formatting, lint, test, build, and static checks. Record actual commands and outcomes, including failures and skipped checks.
8. Inspect the final diff for secrets, generated artifacts, accidental files, unrelated changes, and scope creep.
9. Push only to the task branch and open a PR targeting beta unless Chaty explicitly documents a different target.
10. Link the issue in the PR and describe the outcome, scope, exclusions, changed files, exact tests/results, CI links, limitations, and physical-device verification status.
11. Confirm that CI tested the latest PR head SHA. Any new commit invalidates the assumption that earlier exact-head CI still verifies the branch.
12. Hand the PR to Chaty with its URL, branch, exact latest head SHA, changed-file list, commands/results, relevant CI runs, known risks/limitations, unverified behavior, and remaining verification needs.

Do not force-push shared history, bypass required checks, merge your own PR, or target main for routine development.

## 6. PR integration and review

Chaty owns integration; a contributor's task is not complete merely because code is committed or a PR is opened.

For every contributor PR, Chaty must:

1. Inspect the complete diff and relevant surrounding implementation, not just the PR description.
2. Evaluate correctness, security, data safety, performance, compatibility, regression risk, tests, documentation, and CI according to the change's risk.
3. Make necessary fixes directly on the PR branch when safe and within the granted authority. Do not send routine correction cycles back to the implementation agent.
4. Resolve conflicts and safely synchronize with beta. Re-run every affected check after any change.
5. Confirm all required checks are green for the exact latest head SHA; verify any required signed/package checks on the resulting beta commit.
6. Merge into beta only after acceptance requirements are met.
7. Verify the resulting beta commit, record evidence on the GitHub PR/issue, and close the issue under the label policy above. Update maintained technical documentation only when durable project behavior or a contract changes; do not copy queue labels, assignment state, or volatile task order into Markdown.

Chaty does not submit routine APPROVE, COMMENT, or REQUEST_CHANGES formal reviews for Vibe PRs. Chaty integrates the work, fixes it on the PR branch, and verifies the exact resulting head. If Chaty cannot safely make a correction because of permissions, unclear scope, or a decision reserved for the repository owner, stop and document that specific blocker.

IB or another explicitly authorized reviewer may perform a separate review when requested. Review findings must distinguish confirmed defects from risks/questions and must identify impact, affected location, and a concrete correction or required evidence. Review approval is not merge authorization.

Review depth must match risk:

- **Lightweight:** documentation-only, copy, or isolated low-risk edits. Check scope, links, factual accuracy, and absence of behavior/security changes.
- **Standard:** UI, settings, diagnostics, and contained application changes. Inspect affected call paths, regression risks, relevant tests, and required unsigned PR build.
- **Strict:** GeckoView/session lifecycle, updater/download/patching, storage/profile, network/security, dependencies/toolchain, manifest/permissions, signing, and CI/release workflows. Inspect success and failure paths, require focused regression tests and exact-head CI, and require relevant post-merge signed/packaging checks. Require physical ARM64 testing for claims that depend on device behavior.

## 7. Validation and truthful reporting

- Run checks relevant to the changed scope and report the exact commands and actual outcomes.
- Identify the exact commit SHA tested. Results from an earlier commit do not validate later changes.
- Never say a build, test, CI job, or runtime scenario passed unless output or a recorded CI result proves it.
- Distinguish source inspection, static checks, unit tests, emulator checks, physical ARM64 device checks, and actual in-game behavior.
- Physical-device evidence must identify the device, Android version, build commit, test steps, and results.
- Do not claim profile persistence, game compatibility, addon/Lua initialization, or resource interception based only on source inspection, mocks, local indexing, a successful page load, or green CI.
- Diagnose environmental/dependency CI failures rather than mislabeling them as source failures.
- Use the repository's least-privilege CI policies. Never expose secrets or enable unnecessary workflow permissions.
- PR validation is unsigned and must not access release-signing secrets or publish APKs. A successful PR run does not prove signing, APK ABI contents, or release artifacts.
- The current required checks are defined by the live workflow files under .github/workflows. Read those workflows when the check set or trigger behavior matters; do not rely on a stale summary in another document.

## 8. Product and engineering invariants

### Product boundary and compatibility

- LastHavenPlus is an Android launcher for the existing LastHaven Wrath of the Lich King browser game at https://lasthavens.com/play. It hosts the game; it must not recreate, modify, or develop the game client.
- Target Android 10 (API 29) and later.
- Package ARM64 only: `arm64-v8a`. Do not add other ABIs.
- Preserve application ID `com.neonyne.lasthavenlauncher`.
- Preserve the established release-signing identity. Never commit, replace, or expose keystores, signing credentials, tokens, or secrets.
- Use the Mozilla GeckoView ARM64 Stable artifact currently pinned in `gradle/libs.versions.toml` as the embedded browser engine on beta. Read the catalog for the exact version; do not hard-code a version in this policy. Do not add Cefrium, Android System WebView, or a second/fallback engine.
- Do not add Google Play services modules (`com.google.android.gms`) to the runtime dependency graph or APK. The current build intentionally excludes that group; GeckoView WebAuthn/passkey sign-in is unavailable as a result. Any reconsideration requires an explicit owner decision and updated dependency, licensing, privacy, and packaging review.
- Use the exact API surface of the pinned GeckoView artifact in gradle/libs.versions.toml and the official documentation for that version. Do not infer APIs from a different version.
- Keep GeckoRuntime process-scoped and manage GeckoSession lifecycle explicitly. A successful CI build does not prove game compatibility or profile persistence.
- Keep update checks/downloads and installation user-initiated. Android's package installer controls installation. Do not add background or automatic installation.
- Never publish debug APKs or incomplete features.
- Do not add analytics, tracking, or automatic telemetry. Never log cookies, tokens, authorization headers, or other session secrets.
- Do not silently clear, migrate, overwrite, or copy browser profiles, cookies, LocalStorage, IndexedDB, or other game data. No supported Cefrium-to-GeckoView profile migration has been established.
- Do not make speculative performance, dependency, or build-tool changes. Record comparable before/after measurements and compatibility evidence for optimization work.

### GeckoView permissions and site boundary

The current MainActivity.setupGeckoDelegates policy is deny-by-default:

- Only persistent storage and audible/inaudible autoplay are allowed, and only for the exact lasthavens.com host and its subdomains.
- These permissions are denied for every other origin. All other GeckoView content-permission types are denied for all origins.
- Android runtime permission requests are rejected. GeckoView media-capture requests for camera/video and microphone/audio are rejected.
- The manifest declares INTERNET and REQUEST_INSTALL_PACKAGES; it does not declare camera, microphone, geolocation, or notification permissions. REQUEST_INSTALL_PACKAGES supports the explicit APK update installer flow, not website access.
- Do not treat successful page loading as evidence of a broader permission grant. Features requiring unsupported permissions remain unavailable unless a separate issue explicitly changes the policy.

This is a source-level policy description, not proof that prompts or denial behavior have been tested on physical hardware. Any change must specify origin, permission type, Android runtime permission, consent UX, data handling, and denial behavior. Never add wildcard grants or silently grant media capture.

### Addon Manager boundary

Addon Manager local management and runtime integration are distinct capabilities:

- Local ZIP/library management, metadata, and dependency planning do not activate game addons.
- Do not bundle the supplied Chrome MV3 service worker unchanged, assume that a WoW addon ZIP/TOC package is a Firefox WebExtension, or assume external unsigned XPI installation is supported.
- Runtime injection must remain gated by explicit issue scope and tests for origin/path restrictions, resource bounds, worker behavior, session isolation, and failure handling.
- The manager must retain and return to the existing game session. It must not start a second game instance or modify game-client code or persistent game/profile data.
- Never claim addon initialization, Lua execution, or game-resource masking without verified runtime and physical-device evidence.

Check the live Addon Manager parent/child GitHub issues and PRs for current phase, dependencies, and assignment. Do not infer queue order or task state from Markdown.

## 9. Documentation and continuity

Keep each file focused on its distinct purpose:

- README.md: stable repository/product overview, compatibility, build entry points, and links to agent/technical records. Do not put assignment queues, detailed workflow rules, or dated work-session logs here.
- AGENT_PROTOCOL.md: agent startup/bootstrap only.
- AGENT_WORK.md: historical work/decision records only; not a queue and not required for startup.
- STATUS.md: retired status-dashboard pointer; consult live code, issues, PRs, and Actions instead.
- ISSUES.md: retired backlog mirror; do not update it or use it for task discovery. GitHub Issues are authoritative.
- CHANGELOG-AGENTS.md and WORK_LOG.md: append-only agent/project history; do not rewrite or erase earlier entries.
- Technical documents: detailed component-specific contracts and evidence. Keep them consistent with source and live CI.

When work changes verified behavior or a durable engineering contract, update the relevant technical document and append meaningful history to CHANGELOG-AGENTS.md/WORK_LOG.md. Record task state, evidence, blockers, and handoffs on the live GitHub issue/PR. Do not create or maintain Markdown copies of live assignments, labels, queues, or blocker state. Never convert assumptions into verified facts.

## 10. Branch lifecycle and release authority

- Follow docs/BRANCH_LIFECYCLE.md for task branch naming, reuse, synchronization, retirement, and disposal. Refresh live branch/PR evidence before a cleanup decision; a point-in-time audit is not current proof.
- Start task branches from beta and target routine implementation PRs to beta. Never develop routinely on main.
- Do not merge `infra/*` branches wholesale into beta. Preserve `beta-legacy-*` snapshots unless the repository owner explicitly authorizes disposal; never wholesale-merge them.
- No agent except Chaty may merge PRs into beta.
- No agent may promote beta to main, create a release tag, or publish a release without explicit repository-owner approval. Releases must come from an accepted main commit and pass the current release gates.
- Never force-push shared branches or bypass required checks.
- Do not delete or rename task branches as routine cleanup. Retain merged/abandoned task branches as archives and stop pushing to them.
- Any branch deletion requires explicit owner approval recorded in the disposal log of docs/BRANCH_LIFECYCLE.md; no scheduled branch-deletion automation is permitted.

## 11. When instructions are unclear

Before a risky, irreversible, or scope-expanding action, stop and ask Chaty on the relevant GitHub issue/PR. State the unclear instruction or permission, affected files/branches/issues, proposed action, possible impact or overlap, and the decision needed. Do not guess, silently broaden scope, use another agent's credentials, or treat technical ability as permission.

---

**Operating summary:** Chaty assigns and integrates. Vibe implements assigned issues. IB implements and reviews when authorized. Other named agents implement only when explicitly assigned. Chaty alone merges. The repository owner retains final governance and release authority.

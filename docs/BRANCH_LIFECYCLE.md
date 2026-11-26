# Branch lifecycle

Goal: control branch growth without losing useful or unmerged work. This document defines the lifecycle policy and records a point-in-time inventory. **No branch is deleted without explicit owner approval.**

## Policy

### Naming and creation
- Task branches use `<owner>/<task-slug>` where `<owner>` is the working agent's prefix (`chaty/`, `vibe/`, `agent/`, `infra/`) and `<task-slug>` is a short kebab-case description of the task (optionally suffixed with a short issue key such as `-568e1f` when the slug alone is ambiguous or the branch outlives the issue number it served). Dependabot branch names are managed by GitHub and are exempt.
- Recommended persistent worker-branch naming: prefer `<owner>/<task-slug>` keyed to the **issue number** when the issue is known before branching (for example `vibe/updater-progress-111`); use a descriptive slug with a unique suffix only when branching before an issue exists. One issue per branch; do not stack unrelated tasks on one branch.
- Start task branches from current `beta`; all project PRs target `beta`.
- `infra/*` branches remain separate from product integration and are never wholesale-merged into `beta`.
- Dependabot branches are reviewed individually; grouped GitHub Actions updates are configured to target `beta`.

### Safe branch reuse and sync
- Reuse a worker branch only when its scope matches the new task; otherwise start a fresh branch from `beta`. Never reuse a branch whose PR is still under review for unrelated work.
- To sync a reused branch with an advanced `beta`: `git fetch origin --prune`, then merge `origin/beta` into the branch (merge commits are preferred over rebase so pushed history is never rewritten), resolve conflicts, rerun the affected checks, and update the PR description with the new exact head SHA.
- Never force-push shared history. If history must be rewritten, do it only on a branch with no open PR and with the owner's knowledge, and update every record referencing the old head.
- After every `beta` merge that a branch depends on, refresh the branch and rerun exact-head CI before requesting re-review.

### While active
- Keep assignments non-overlapping and reuse a worker branch only when its scope matches.
- Refresh active PR branches after `beta` advances and rerun exact-head checks.
- Open-PR branches remain active. Stale branches are candidates for review, not automatic deletion.

### Retirement
- A branch may be proposed for retirement after its PR is merged, closed with work superseded/abandoned, or the owner declares it obsolete.
- **Repository setting (verified live 2026-11-26 via repository metadata): `delete_branch_on_merge` is enabled.** GitHub automatically deletes a PR's head branch when the PR is merged. Consequences:
  - Merged task branches disappear without a manual step; no separate disposal-log entry is required for an auto-deleted merged head branch.
  - Branches with no open PR (for example `beta-legacy-*`, `infra/*`, staging and historical branches) are **not** affected by auto-delete; their removal still requires explicit owner approval.
  - After merge, references to a deleted head branch remain resolvable through the merge commit and PR record.
- **Approval is required before every manual deletion.** Record the approving statement and deletion in the log.
- Preserve `beta-legacy-*` orphan snapshots until independently verified; never wholesale-merge them.
- A branch whose tip is an ancestor of `beta` (fully integrated, zero unique commits) is the safest retirement candidate, but manual deletion still requires explicit owner approval recorded in the disposal log.

## Audit — 2026-11-26 (live refresh)

Baseline: `beta` at `103bb979c98589a8704459d28d13861eccca39d6`, verified from the live branch ref. The repository has **29 remote branches**: `beta`, `main`, and 27 other branches. The repository default branch is `beta`. Counts use GitHub's compare endpoint for `beta...branch`, shown as commits the branch is behind / ahead of beta. The `main` row uses the same orientation: beta-only / main-only commits. `beta-legacy-2026-11-24` has no common ancestor with beta, so no comparable ahead/behind count is reported. Heads are abbreviated to seven characters for readability; the full SHAs are available from the live branch refs. This is a point-in-time snapshot; refresh before any future cleanup decision. **No branch was deleted, rewritten or otherwise mutated by this audit.**

| Branch | Head (short SHA) | PR / issue status | Behind / ahead vs beta | Unique-content evidence | Disposition |
|---|---|---|---:|---|---|
| beta | 103bb979 | Integration trunk; repository default branch | — | Baseline for all comparisons | Protected integration branch; Chaty alone merges |
| agent/add-issue-agent-workflow | d45c3da | PR #93 closed unmerged | 41 / 1 | 1 commit unique to branch | Infrastructure worker proposal; keep isolated pending owner decision |
| beta-legacy-2026-11-24 | 973a817 | No PR; legacy snapshot | No common ancestor | Independent 187-commit legacy line (prior audit evidence) | Preserve; never wholesale-merge or delete without owner approval |
| chaty/addon-manager-native-phase-a | 108d7b0 | PR #140 closed unmerged | 9 / 80 | 80 commits ahead of beta | Earlier Addon Manager implementation line; superseded iterations, preserve pending review |
| chaty/addon-manager-phase-a-current-beta | 01bb9f3 | PR #145 closed unmerged; superseded by merged #152 | 4 / 74 | 74 commits ahead of beta | Superseded Addon Manager Phase A line; no deletion |
| chaty/ci-runner-consolidation | 7485163 | No open PR; superseded by #132 | 16 / 6 | 6 commits ahead of beta | Earlier CI consolidation iterations; preserve pending review |
| chaty/ci-runner-consolidation-refresh | 0f16fbb | PR #132 merged | 15 / 0 | Tip is ancestor of beta; no unique commits | Fully integrated; auto-deleted only if PR head branch; this separate branch remains, no manual cleanup |
| chaty/community-hygiene | f07f155 | PR #141 closed unmerged | 9 / 50 | 50 commits ahead of beta | Earlier licensing/community hygiene iteration; superseded by refresh branch |
| chaty/community-hygiene-refresh | 1bad014 | PR #144 open draft | 4 / 54 | 54 commits ahead of beta | Active Chaty PR; preserve |
| chaty/housekeeping-docs-followup | 15da82e | No open PR | 13 / 3 | 3 commits ahead of beta | Docs overlap possible with merged #134; retain pending byte-level review |
| chaty/issue-4-interrupted-download-tests | 228a2bd | PR #80 closed unmerged | 45 / 18 | 18 commits ahead of beta | Historical download-test work; retain as reference |
| chaty/issue-81-compatible-library-refresh | 807543c | PR #82 closed unmerged; superseded by #104 | 45 / 6 | 6 commits ahead of beta | Superseded dependency proposal; no toolchain changes without compatibility review |
| chaty/issue-84-official-launcher-icon | 2e64f56 | PRs #85/#135 merged | 44 / 0 | Tip is ancestor of beta; no unique commits | Fully integrated; no manual cleanup |
| chaty/post-v2.3-closeout | e696563 | PR #134 merged | 13 / 0 | Tip is ancestor of beta; no unique commits | Fully integrated; no manual cleanup |
| chaty/pr105-refresh | fcaa438 | PR #105 merged from a related branch | 32 / 5 | 5 commits ahead of beta | Historical updater staging branch; preserve pending review |
| chaty/v2.2.0-selective-main-promotion | c396984 | PRs #87/#91 merged from other branches | 64 / 50 | 50 commits ahead of beta | Historical selective-promotion branch; preserve |
| chaty/v2.4-launcher-icon | 4116934 | No open PR | 13 / 1 | 1 commit ahead of beta | Artwork relationship to merged #135 needs review before reuse |
| dependabot/gradle/gradle-wrapper-9.9.2 | 4413dd3 | PR #71 closed unmerged | 64 / 46 | 46 commits ahead of beta | Superseded Gradle proposal; toolchain change needs compatibility validation |
| infra/issue-pr-agent | 22bd670 | No PR; infrastructure branch | 64 / 53 | 53 commits ahead of beta | Keep isolated; never wholesale-merge into beta |
| main | ebc5b42 | Stable release branch; v2.3.0 | 64 / 56 | 56 commits ahead of beta; beta has 64 commits not in main | Preserve stable line; promotion only with owner approval |
| vibe/branch-lifecycle | 52b3090 | PR #123 open draft; issue #62 reactivated for Chaty | 2 / 10 | 10 commits ahead of beta before this refresh | Active audit branch; no branch cleanup authorized |
| vibe/ci-action-pinning-568e1f | 3422ec1 | PR #110 closed unmerged; superseded by #102/#115 | 26 / 4 | 4 commits ahead of beta | Superseded CI pinning proposal; retain as record |
| vibe/diagnostics-tests-11-6ce186 | 44b96ae | PR #154 open draft; issue #11 | 0 / 3 | 3 commits ahead of beta | Active Vibe PR; preserve |
| vibe/geckoview-audit-refresh | 40ef56c | PR #122 open draft; issue #20 owner-paused | 3 / 5 | 5 commits ahead of beta | Owner-paused; preserve without changes |
| vibe/geckoview-dep-report | 9bb936d | No PR; supports PR #122 | 21 / 1 | 1 commit ahead of beta | Preserve with paused GeckoView audit work |
| vibe/governance-vibe-integrator | dc7f9d2 | PR #94 closed unmerged | 41 / 2 | 2 commits ahead of beta | Rejected governance proposal; retain as history |
| vibe/markdown-validation | c303cac | PR #101 closed unmerged; repair merged via #78 | 41 / 1 | 1 commit ahead of beta | Superseded markdown fix; retain as record |
| vibe/updater-progress-111 | d9b82a9 | PR #143 open draft; issue #111 | 3 / 3 | 3 commits ahead of beta | Active Vibe PR; preserve; wire-through pending review |
| vibe/visual-tokens-568e1f | 6b9a3c8 | PR #113 open draft; issue #107 already integrated via #136 | 26 / 6 | 6 commits ahead of beta | Likely superseded by #136; retain pending review |

## Recent integration history
- 2026-11-26: PR [#153](https://github.com/iNeonyne/LastHavenPlus/pull/153) merged into beta (`02f47e7`, current audit baseline) and PR [#151](https://github.com/iNeonyne/LastHavenPlus/pull/151) merged (`c5bcab3`).
- 2026-11-26: PRs [#140](https://github.com/iNeonyne/LastHavenPlus/pull/140) and [#141](https://github.com/iNeonyne/LastHavenPlus/pull/141) closed unmerged; the Addon Manager Phase A line continues on `chaty/addon-manager-phase-a-current-beta` (PR [#145](https://github.com/iNeonyne/LastHavenPlus/pull/145)) and `vibe/addon-manager-phase-a-23` (PR [#152](https://github.com/iNeonyne/LastHavenPlus/pull/152), Chaty-owned after the owner-directed Vibe handoff).
- 2026-11-26: PR [#154](https://github.com/iNeonyne/LastHavenPlus/pull/154) opened from `vibe/diagnostics-tests-11-6ce186` (issue #11 diagnostics tests, Vibe).
- 2026-11-26: PR [#139](https://github.com/iNeonyne/LastHavenPlus/pull/139) merged into beta (squash `0d5ab71`, issue #9 overlay accessibility, branch `vibe/overlay-accessibility` since removed).
- 2026-11-26: PRs [#138](https://github.com/iNeonyne/LastHavenPlus/pull/138) (`4c508cf`), [#137](https://github.com/iNeonyne/LastHavenPlus/pull/137) (`f800dee`), [#136](https://github.com/iNeonyne/LastHavenPlus/pull/136) (issue #107 visual system, `e096712`, branch `vibe/visual-system-v240` since removed), [#135](https://github.com/iNeonyne/LastHavenPlus/pull/135) (`c24b24e`), [#134](https://github.com/iNeonyne/LastHavenPlus/pull/134) (`e696563`), [#132](https://github.com/iNeonyne/LastHavenPlus/pull/132) (CI consolidation, `7cd898f`), [#130](https://github.com/iNeonyne/LastHavenPlus/pull/130) (`03e96c4`), [#127](https://github.com/iNeonyne/LastHavenPlus/pull/127) (`b7fcd03`), [#125](https://github.com/iNeonyne/LastHavenPlus/pull/125)/[#126](https://github.com/iNeonyne/LastHavenPlus/pull/126) (v2.3.0 selective promotion and updater retry policy) and [#124](https://github.com/iNeonyne/LastHavenPlus/pull/124) (`ec721c3`) merged into beta.
- Earlier: PRs [#112](https://github.com/iNeonyne/LastHavenPlus/pull/112) (game load states, branch `vibe/game-load-states-568e1f` since removed), [#121](https://github.com/iNeonyne/LastHavenPlus/pull/121) (download stream verification, branch `chaty/issue-4-download-stream-verification` since removed), [#120](https://github.com/iNeonyne/LastHavenPlus/pull/120) (markdown lint, branch `chaty/issue-70-markdown-lint` since removed), and [#95](https://github.com/iNeonyne/LastHavenPlus/pull/95) (`7bf41a3`), [#96](https://github.com/iNeonyne/LastHavenPlus/pull/96) (`6c9dadc`), [#105](https://github.com/iNeonyne/LastHavenPlus/pull/105), [#104](https://github.com/iNeonyne/LastHavenPlus/pull/104), [#103](https://github.com/iNeonyne/LastHavenPlus/pull/103), [#102](https://github.com/iNeonyne/LastHavenPlus/pull/102), [#99](https://github.com/iNeonyne/LastHavenPlus/pull/99), [#98](https://github.com/iNeonyne/LastHavenPlus/pull/98) all merged into beta; their task branches are no longer present.
- Closed unmerged: PRs [#93](https://github.com/iNeonyne/LastHavenPlus/pull/93), [#100](https://github.com/iNeonyne/LastHavenPlus/pull/100), [#101](https://github.com/iNeonyne/LastHavenPlus/pull/101), [#110](https://github.com/iNeonyne/LastHavenPlus/pull/110), [#94](https://github.com/iNeonyne/LastHavenPlus/pull/94), [#80](https://github.com/iNeonyne/LastHavenPlus/pull/80), [#82](https://github.com/iNeonyne/LastHavenPlus/pull/82), [#140](https://github.com/iNeonyne/LastHavenPlus/pull/140), [#141](https://github.com/iNeonyne/LastHavenPlus/pull/141), [#71](https://github.com/iNeonyne/LastHavenPlus/pull/71). PR [#97](https://github.com/iNeonyne/LastHavenPlus/pull/97) closed unmerged; PR [#92](https://github.com/iNeonyne/LastHavenPlus/pull/92) merged into main.
- Repository default branch is `beta` (re-verified live 2026-11-26 from repository metadata); `main` remains the stable release branch and is not the default. Product integration PRs target `beta` regardless of default-branch status.

## Disposal log

| Date | Branches | Approval | Record |
|---|---|---|---|
| 2026-11-25 | Prior 16-branch cleanup (pre-v2.2.0) | Owner-approved per #62 history | Issue #62 / PR [#75](https://github.com/iNeonyne/LastHavenPlus/pull/75) |
| 2026-11-26 (observed, not acted on) | 5 merged-task branches already absent from remote: `vibe/game-load-states-568e1f`, `vibe/visual-system-v240`, `vibe/overlay-accessibility`, `chaty/issue-4-download-stream-verification`, `chaty/issue-70-markdown-lint` | Removal observed during this audit; no deletion performed or approved by this document. Consistent with the repository's `delete_branch_on_merge` setting (owner-enabled, verified live 2026-11-26) | This audit snapshot |
| 2026-11-26 | Repository setting `delete_branch_on_merge` enabled by the owner (verified live via `gh api repos/iNeonyne/LastHavenPlus`) | Owner action, recorded here for the lifecycle record | This audit snapshot |

## Previously deleted branches
- 2026-11-25, per #62 prior audit: `chaty/fix-release-workflow-timeouts`, `chaty/fix-release-draft-verification`, `chaty/docs-reconciliation-v2.1`, `chaty/housekeeping-closeout`, `chaty/housekeeping-doc-reconciliation`, `chaty/housekeeping-postrelease-sync`, `chaty/housekeeping-roadmap-reconcile`, `chaty/status-sync-after-51`, `chaty/v2.1-docs-reconcile`, `chaty/v2.1-release-records`, `chaty/workflow-efficiency-policy`, the absorbed Dependabot branches of PRs #57/#58/#59/#68/#69/#71, and completed launch branches `chaty/v2.1-launcher-redesign` and `plow-22-home-screen`.

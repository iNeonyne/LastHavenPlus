# Branch lifecycle

Goal: control branch growth without losing useful or unmerged work. This document defines the lifecycle policy and records a point-in-time inventory. **No branch is deleted without explicit owner approval.**

## Policy

### Naming and creation
- Task branches use `<owner>/<task-slug>`; observed prefixes include `chaty/`, `vibe/`, `agent/`, and Dependabot-managed `dependabot/`.
- Start task branches from current `beta`; all project PRs target `beta`.
- `infra/*` branches remain separate from product integration and are never wholesale-merged into `beta`.
- Dependabot branches are reviewed individually; grouped GitHub Actions updates are configured to target `beta`.

### While active
- Keep assignments non-overlapping and reuse a worker branch only when its scope matches.
- Refresh active PR branches after `beta` advances and rerun exact-head checks.
- Open-PR branches remain active. Stale branches are candidates for review, not automatic deletion.

### Retirement
- A branch may be proposed for retirement after its PR is merged, closed with work superseded/abandoned, or the owner declares it obsolete.
- **Approval is required before every deletion.** Record the approving statement and deletion in the log.
- Preserve `beta-legacy-*` orphan snapshots until independently verified; never wholesale-merge them.

## Audit — 2026-11-26

Baseline: `beta` at `6c9dadc31682db75f706435437d77969fb3e505e` (PR #96 merge). The repository has 16 remote branches: `beta` plus the 15 non-`beta` branches below. Ahead/behind figures are GitHub compare results against this exact baseline. The `vibe/branch-lifecycle` branch was reset to this baseline for this refreshed audit; after this document commit it is expected to be 1 ahead / 0 behind. Counts are point-in-time and must be refreshed before cleanup decisions.

| Branch | Current status / evidence | Compare to beta | Disposition |
|---|---|---:|---|
| `agent/add-issue-agent-workflow` | PR [#93](https://github.com/iNeonyne/LastHavenPlus/pull/93) closed unmerged | 1 ahead / 15 behind | Retain pending owner decision; no deletion |
| `beta-legacy-2026-11-24` | No common ancestor with beta | orphan | Preserve pending owner verification |
| `chaty/issue-4-interrupted-download-tests` | PR [#80](https://github.com/iNeonyne/LastHavenPlus/pull/80) closed unmerged | 18 ahead / 19 behind | Retain as possible updater-test reference |
| `chaty/issue-81-compatible-library-refresh` | PR [#82](https://github.com/iNeonyne/LastHavenPlus/pull/82) closed unmerged; issue #81 later handled by PR #104 | 6 ahead / 19 behind | Candidate for owner review; no deletion |
| `chaty/issue-84-official-launcher-icon` | Legacy icon branch; merged PR [#85](https://github.com/iNeonyne/LastHavenPlus/pull/85) used a different branch | 0 ahead / 18 behind | Candidate for owner review; no deletion |
| `chaty/pr105-refresh` | Temporary staging branch; no PR | 5 ahead / 6 behind | Historical staging; retain pending owner review |
| `chaty/v2.2.0-selective-main-promotion` | Historical promotion work; PR [#87](https://github.com/iNeonyne/LastHavenPlus/pull/87) used another branch | 50 ahead / 38 behind | Historical; retain pending owner review |
| `dependabot/gradle/gradle-wrapper-9.9.2` | PR [#71](https://github.com/iNeonyne/LastHavenPlus/pull/71) closed unmerged | 46 ahead / 38 behind | Historical; no deletion without approval |
| `infra/issue-pr-agent` | Separate infrastructure worker branch | 53 ahead / 38 behind | Keep isolated; do not merge wholesale |
| `main` | Stable branch; intentionally separate history | 52 ahead / 38 behind | Preserve; no beta-to-main promotion without owner approval |
| `vibe/branch-lifecycle` | PR [#100](https://github.com/iNeonyne/LastHavenPlus/pull/100) open; this refreshed audit | 1 ahead / 0 behind after document commit | Active |
| `vibe/ci-action-pinning-568e1f` | PR [#110](https://github.com/iNeonyne/LastHavenPlus/pull/110) open; CI action SHA pins and permissions | 1 ahead / 6 behind | Active; refresh after PR #96 integration and review |
| `vibe/game-load-states-568e1f` | PR [#112](https://github.com/iNeonyne/LastHavenPlus/pull/112) open; game loading/failure UX | 1 ahead / 6 behind | Active; exact-head CI currently fails formatting |
| `vibe/governance-vibe-integrator` | PR [#94](https://github.com/iNeonyne/LastHavenPlus/pull/94) closed unmerged | 2 ahead / 15 behind | Retain pending owner policy decision |
| `vibe/markdown-validation` | PR [#101](https://github.com/iNeonyne/LastHavenPlus/pull/101) closed unmerged; proposed fix withdrawn | 1 ahead / 15 behind | Candidate for owner review; no deletion |

## Recent integration history
- PR [#95](https://github.com/iNeonyne/LastHavenPlus/pull/95) merged into beta; merge commit `7bf41a369a7514c78c2d98fec62ec5296489662f` (checkout v7 pin).
- PR [#96](https://github.com/iNeonyne/LastHavenPlus/pull/96) merged into beta; merge commit `6c9dadc31682db75f706435437d77969fb3e505e` (upload-artifact v7.0.2 pin).
- PR [#105](https://github.com/iNeonyne/LastHavenPlus/pull/105), [#102](https://github.com/iNeonyne/LastHavenPlus/pull/102), and [#98](https://github.com/iNeonyne/LastHavenPlus/pull/98) were merged into beta earlier; their task branches are no longer present.
- PRs [#104](https://github.com/iNeonyne/LastHavenPlus/pull/104) and [#103](https://github.com/iNeonyne/LastHavenPlus/pull/103) also merged into beta. PR [#99](https://github.com/iNeonyne/LastHavenPlus/pull/99) merged the Gradle version catalog. PR [#92](https://github.com/iNeonyne/LastHavenPlus/pull/92) merged into main; PR [#97](https://github.com/iNeonyne/LastHavenPlus/pull/97) closed unmerged.
- Repository default branch remains `main`; it has not been changed. Product integration PRs are still required to target `beta`.

## Disposal log

| Date | Branches | Approval | Record |
|---|---|---|---|
| 2026-11-25 | Prior 16-branch cleanup (pre-v2.2.0) | Owner-approved per #62 history | Issue #62 / PR [#75](https://github.com/iNeonyne/LastHavenPlus/pull/75) |

## Previously deleted branches
- 2026-11-25, per #62 prior audit: `chaty/fix-release-workflow-timeouts`, `chaty/fix-release-draft-verification`, `chaty/docs-reconciliation-v2.1`, `chaty/housekeeping-closeout`, `chaty/housekeeping-doc-reconciliation`, `chaty/housekeeping-postrelease-sync`, `chaty/housekeeping-roadmap-reconcile`, `chaty/status-sync-after-51`, `chaty/v2.1-docs-reconcile`, `chaty/v2.1-release-records`, `chaty/workflow-efficiency-policy`, the absorbed Dependabot branches of PRs #57/#58/#59/#68/#69/#71, and completed launch branches `chaty/v2.1-launcher-redesign` and `plow-22-home-screen`.

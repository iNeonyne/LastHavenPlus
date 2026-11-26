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

## Audit — 2026-11-26 (refreshed)

Baseline: `beta` at `eec16ff` (`ci: update Gradle setup action to v6.4.1 (#118)`), verified live against the remote before this write; this document branch merges that exact baseline (merge commit `b281ec8`). The repository has 21 remote branches: `beta`, `main` and the 19 non-beta/main branches below. The repository default branch is `beta` (verified live from repository metadata); `main` remains the stable release branch. Ahead/behind are `git rev-list --count` results against this exact baseline. Counts are point-in-time and must be refreshed before any cleanup decision. **No branch is deleted or rewritten by this audit.**

| Branch | Head SHA | PR / issue status | Compare vs beta | Unique content evidence | Disposition |
|---|---|---|---:|---|---|
| `beta` | `eec16ff` | Integration trunk and repository default branch; PR target for all product work | — (baseline) | — (this table's baseline; 52 commits of `main` are not in it and vice versa) | Protected trunk; only Chaty merges into it |
| `agent/add-issue-agent-workflow` | `d45c3da` | PR [#93](https://github.com/iNeonyne/LastHavenPlus/pull/93) closed unmerged | 1 ahead / 20 behind | Adds `agent-workflow/` Python worker (never-merge infra), `.agents/` capability docs; functionally superseded by merged PR #114 ownership workflow, but files are not in beta | Retain pending owner decision; do not merge (infra) |
| `beta-legacy-2026-11-24` | `973a817` | None; legacy snapshot | No common ancestor (217 commits) | 187 commits including pre-rename package, legacy updater and Addon Manager assets (see STATUS.md) | **Preserve / owner decision required**; never wholesale-merge |
| `chaty/issue-4-download-stream-verification` | `48749b0` | PR [#121](https://github.com/iNeonyne/LastHavenPlus/pull/121) **open** (issue #4) | 7 ahead / 0 behind | Streamed download policy + interrupted/partial-file tests | Preserve — active PR |
| `chaty/issue-4-interrupted-download-tests` | `228a2bd` | PR [#80](https://github.com/iNeonyne/LastHavenPlus/pull/80) closed unmerged | 18 ahead / 24 behind | Interrupted download test iterations; partly continued by PR #105 and now PR #121 | Retain as reference pending owner decision |
| `chaty/issue-66-independent-apk-rebuilds` | `ab32c31` | PR [#119](https://github.com/iNeonyne/LastHavenPlus/pull/119) **open** (issue #66); head advanced from `610838b` since the prior audit snapshot | 14 ahead / 0 behind | Reproducibility build workflow + corrupt-archive test | Preserve — active PR |
| `chaty/issue-70-markdown-lint` | `29fd533` | PR [#120](https://github.com/iNeonyne/LastHavenPlus/pull/120) **open** (issue #70) | 9 ahead / 0 behind | Markdown lint policy docs | Preserve — active PR |
| `chaty/issue-81-compatible-library-refresh` | `807543c` | PR [#82](https://github.com/iNeonyne/LastHavenPlus/pull/82) closed unmerged | 6 ahead / 24 behind | `app/build.gradle.kts` + tracker docs; superseded by merged PR #104 | Superseded; candidate for owner review, no deletion |
| `chaty/issue-84-official-launcher-icon` | `2e64f56` | No open PR; PR #85 merged from `chaty/issue-84-recreated-launcher-icon` | 0 ahead / 23 behind | Branch tip is an **ancestor of beta** (`merge-base == tip`) | Contents fully integrated; cleanup candidate with owner approval |
| `chaty/pr105-refresh` | `fcaa438` | No PR; staging branch for PR #105 (merged from `chaty/fix-updater-bsdiff-retry`) | 5 ahead / 11 behind | 4 of 5 changed files byte-identical to final beta; `BsdiffPatch.kt` reflects the superseded earlier fix | Superseded by merged PR #105; candidate for owner review |
| `chaty/v2.2.0-selective-main-promotion` | `c396984` | PRs [#87](https://github.com/iNeonyne/LastHavenPlus/pull/87)/[#91](https://github.com/iNeonyne/LastHavenPlus/pull/91) merged from other branches | 50 ahead / 43 behind | Only unreleased v2.2.0 candidate notes beyond promotion history (unique vs main: 2 commits) | Historical; retain pending owner review |
| `dependabot/gradle/gradle-wrapper-9.9.2` | `4413dd3` | PR [#71](https://github.com/iNeonyne/LastHavenPlus/pull/71) closed unmerged | 46 ahead / 43 behind | Proposes Gradle wrapper 9.7.1 → 9.9.2 (beta still on 9.7.1) | Superseded proposal; toolchain change requires owner-approved compatibility validation |
| `infra/issue-pr-agent` | `22bd670` | None; infra worker line | 53 ahead / 43 behind | Issue-to-PR worker agent; policy: infra branches never merge wholesale | Keep isolated; owner decision governs lifecycle |
| `main` | `97353ad` | Stable release branch | — (use main as base) | 52 commits not in beta (stable-only history); beta is 43 ahead | Preserve; main-only commits require review before promotion |
| `vibe/branch-lifecycle` | `da10682903df0c55c331fd68efb90e99732998ba` | PR [#100](https://github.com/iNeonyne/LastHavenPlus/pull/100) closed unmerged; superseded by documentation PR [#123](https://github.com/iNeonyne/LastHavenPlus/pull/123) **open** draft (issue #62) | 4 ahead / 0 behind at audit snapshot (the correction commit after this write adds 1 ahead) | This refreshed audit | Active (Vibe); final handoff head SHA is recorded in the issue #62 handoff comment |
| `vibe/ci-action-pinning-568e1f` | `3422ec1` | PR [#110](https://github.com/iNeonyne/LastHavenPlus/pull/110) closed unmerged | 4 ahead / 5 behind | `dependabot.yml` + CI supply-chain docs; superseded by merged PRs #102/#115 | Superseded; candidate for owner review |
| `vibe/game-load-states-568e1f` | `e3b3dde` | PR [#112](https://github.com/iNeonyne/LastHavenPlus/pull/112) **open** draft (issue #106) | 6 ahead / 0 behind | Game load/failure UX + retry/restore tests; exact-head CI run #45726823497 passed; review corrections and a formatting-only commit (`e3b3dde`, ktfmt KDoc wrap) pushed after it | Preserve — active PR |
| `vibe/governance-vibe-integrator` | `dc7f9d2` | PR [#94](https://github.com/iNeonyne/LastHavenPlus/pull/94) closed unmerged | 2 ahead / 20 behind | Governance proposal for Vibe co-integrator/self-merge; conflicts with adopted #114 workflow | Rejected proposal; retain as record, owner decision |
| `vibe/markdown-validation` | `c303cac` | PR [#101](https://github.com/iNeonyne/LastHavenPlus/pull/101) closed unmerged | 1 ahead / 20 behind | ISSUES.md anchor repair only; same repair merged via PR #78 | Superseded; candidate for owner review |
| `vibe/geckoview-audit-refresh` | `0f00310` | PR [#122](https://github.com/iNeonyne/LastHavenPlus/pull/122) **open** draft (issue #20) | 1 ahead / 0 behind | Documentation-only GeckoView 160 audit evidence refresh | Preserve — active PR |
| `vibe/visual-tokens-568e1f` | `6b9a3c8` | PR [#113](https://github.com/iNeonyne/LastHavenPlus/pull/113) **open** (issue #107) | 6 ahead / 5 behind | Design tokens + DesignTokensTest fix; stacked on PR #112 per Chaty's stack dependency | Preserve — active PR |
## Recent integration history
- PR [#95](https://github.com/iNeonyne/LastHavenPlus/pull/95) merged into beta; merge commit `7bf41a369a7514c78c2d98fec62ec5296489662f` (checkout v7 pin).
- PR [#96](https://github.com/iNeonyne/LastHavenPlus/pull/96) merged into beta; merge commit `6c9dadc31682db75f706435437d77969fb3e505e` (upload-artifact v7.0.2 pin).
- PR [#105](https://github.com/iNeonyne/LastHavenPlus/pull/105), [#102](https://github.com/iNeonyne/LastHavenPlus/pull/102), and [#98](https://github.com/iNeonyne/LastHavenPlus/pull/98) were merged into beta earlier; their task branches are no longer present.
- PRs [#104](https://github.com/iNeonyne/LastHavenPlus/pull/104) and [#103](https://github.com/iNeonyne/LastHavenPlus/pull/103) also merged into beta. PR [#99](https://github.com/iNeonyne/LastHavenPlus/pull/99) merged the Gradle version catalog. PR [#92](https://github.com/iNeonyne/LastHavenPlus/pull/92) merged into main; PR [#97](https://github.com/iNeonyne/LastHavenPlus/pull/97) closed unmerged.
- Repository default branch is `beta` (re-verified live 2026-11-26 from repository metadata); `main` remains the stable release branch and is not the default. Product integration PRs target `beta` regardless of default-branch status.

## Disposal log

| Date | Branches | Approval | Record |
|---|---|---|---|
| 2026-11-25 | Prior 16-branch cleanup (pre-v2.2.0) | Owner-approved per #62 history | Issue #62 / PR [#75](https://github.com/iNeonyne/LastHavenPlus/pull/75) |

## Previously deleted branches
- 2026-11-25, per #62 prior audit: `chaty/fix-release-workflow-timeouts`, `chaty/fix-release-draft-verification`, `chaty/docs-reconciliation-v2.1`, `chaty/housekeeping-closeout`, `chaty/housekeeping-doc-reconciliation`, `chaty/housekeeping-postrelease-sync`, `chaty/housekeeping-roadmap-reconcile`, `chaty/status-sync-after-51`, `chaty/v2.1-docs-reconcile`, `chaty/v2.1-release-records`, `chaty/workflow-efficiency-policy`, the absorbed Dependabot branches of PRs #57/#58/#59/#68/#69/#71, and completed launch branches `chaty/v2.1-launcher-redesign` and `plow-22-home-screen`.

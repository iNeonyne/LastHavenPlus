# Project status — LastHavenPlus

**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest application integration commit:** `2e64f56d9dbf82fa0396d8e23b5e52a62506d9ae` (PR [#83](https://github.com/iNeonyne/LastHavenPlus/pull/83), direct launch and overlay).  
**Current beta HEAD before icon PR:** `2e64f56d9dbf82fa0396d8e23b5e52a62506d9ae`.  
**Latest workflow integration:** PR [#54](https://github.com/iNeonyne/LastHavenPlus/pull/54) merged to beta at `b12da7651445d15080e7593d441459a8c8403070`; exact-head unsigned and post-merge signed CI both passed.  
**Stable documentation:** owner-authorized v2.1.0 corrections are on `main`; README commit `fdae3d90cf3abb209df8a42dd73ed1667bae9808`, CHANGELOG commit `2abe7cc212b67a3b91f5dcdb108aee3a55559e32`.  
**Stable promotion and publication:** PR [#47](https://github.com/iNeonyne/LastHavenPlus/pull/47) promoted to `main`; tag/release `v2.1.0` is published at `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.  
**Current phase:** v2.1.0 is published; v2.2.0 planning is active. The direct-launch/overlay redesign is tracked in #56. Owner release feedback is collected after publication on the owner's schedule; it is not a release blocker. PR #54 fixed draft asset cleanup/verification by release ID; exact-head unsigned validation and post-merge signed beta validation both passed. The release-ID path still requires a future owner-authorized versioned release run.
**Latest workflow integration:** issue #60 merged by PR [#72](https://github.com/iNeonyne/LastHavenPlus/pull/72) at `82e819a43e6d728a9eb8fea620eca47ac9c998df`. Exact-head PR validation passed in [run #45627915789](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627915789). Post-merge Android quality run [#45631250015](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631250015) and beta build run [#45631249817](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631249817) completed successfully. The beta build verified signed release APK assembly, unit tests, established signing certificate and ARM64-only ABI; release-tag publication steps were skipped.

## Current source state

- Mozilla GeckoView 160 is the embedded browser engine; Cefrium is removed from the production build.
- The app opens on a responsive launcher Home screen. Play lazily starts the game; Settings does not initialize GeckoView.
- Android Back from gameplay opens a full-screen menu with exactly Reload, Settings and Return to game. The persistent top-right Menu button is removed.
- Settings, diagnostics and update details are full-screen pages. The existing GeckoSession is retained while its view is detached for launcher navigation.
- Adaptive launcher icon resources and a resizeable Activity are integrated.
- The updater remains manual: no automatic downloads or installation.
- Addon-manager work under #23 is on hold by owner instruction. No addon manager is integrated; do not resume until explicit owner approval.

## Integration and CI evidence

| Check | Result |
|---|---|
| PR #43 head | `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3` |
| PR #43 validation | [Run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) passed unsigned release-variant build and JVM unit tests |
| PR #43 merge | `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` |
| Post-merge beta build | [Run #45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks, as recorded in merged PR #47 |
| Updater tag validation | PR #41 merged at `1d35e520331ca9ffa3a9b41b21402a3110728001`; v2.1.0 tag creation succeeded in release run #45603533787 |
| v2.1.0 tag release | [Run #45603533787](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45603533787): unit tests, signer, established certificate, ARM64-only ABI, differential generation and upload/publish steps passed; final draft-by-tag lookup failed HTTP 404. Release metadata confirms published assets |

PR validation is unsigned and does not access release secrets or publish APKs. A successful PR run does not verify signing, APK ABI contents or release artifacts.

## v2.1.0 UI scope

Integrated from PR #43:
- Responsive branded Home screen and adaptive launcher icon resources.
- Removed persistent gameplay Menu button.
- Gameplay menu has Reload, Settings and Return to game only.
- Settings, diagnostics and update details are full-screen pages.
- Responsive layout policy and JVM breakpoint tests.
- GeckoSession retained while the game view is detached for launcher pages.

Follow-up feedback may be reported by the owner after installing a release. No owner feedback is required before publication.

## Owner release feedback

The repository owner will install releases and report what works and what does not on their own schedule. Feedback is recorded in #5/#21 and converted into focused follow-up issues where needed. Pending feedback is not a release blocker.

## Release constraints and blockers

- Preserve application ID, established signing identity and ARM64-only packaging.
- Keep updater behavior manual and user-confirmed; no automatic download or installation.
- Require exact-commit CI, signed optimized APK verification, package identity/signing checks and artifact inspection before publication.
- Resolve any failing build, test, signing, packaging or security checks before publication.
- Owner release feedback (#5/#21) is post-release and optional; it is not a blocker.
- No tag, promotion to `main` or publication without explicit owner approval.

## v2.2.0 roadmap and active ownership — 2026-11-25

- **Roadmap:** GitHub issue #18 is the v2.2.0 planning source of truth. v2.2.0 is a target, not release authorization.
- **Chaty:** owns issue #18, remaining #70 Markdown lint/stale-reference review and branch audit, central documentation, CI/release coordination, PR review and beta integration. Roadmap/assignment housekeeping merged by PR [#73](https://github.com/iNeonyne/LastHavenPlus/pull/73) at `c16439bfcedc9949d8ec683039fb5df85afea122`.
- **Ploy:** assigned issue #56 only: direct game launch and responsive in-game overlay, including Settings in both orientations and preserved GeckoSession state. Must submit a focused PR targeting beta; see AGENT_WORK.md.
- **Owner feedback:** #5/#21 are post-release feedback trackers. The owner will share findings on their own schedule; pending feedback does not block release.
- **Supersession:** #42's old v2.1.0 Home-screen-first verification is superseded by #56; verify the new UX under #5/#56.
- **CI gate #60:** PR #72 merged at `82e819a43e6d728a9eb8fea620eca47ac9c998df`. Exact PR validation [#45627915789](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627915789), post-merge quality [#45631250015](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631250015), and signed beta build [#45631249817](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631249817) passed. The beta build skipped release-tag publication steps.
- **Addon Manager #23:** remains on hold; no assignment or implementation without explicit owner approval.


## Branch audit — 2026-11-25

- The branch inventory/diff review was performed against beta snapshot `1c82e8b4aee2b0ce9ef1d9e6a68311dca4eb200b`; this is a historical audit baseline, not the current beta HEAD. The beta HEAD at the start of this documentation follow-up is recorded at the top of this file (`1adc890343fb4389ad76144eb00fed2ea33e316e`). No ordinary stale Chaty/Ploy feature branches remained at the time of that inventory.
- The six Dependabot branches correspond to six open dependency PRs and are retained until those PRs are resolved.
- `beta-legacy-2026-11-24` is an orphaned legacy snapshot with no common ancestor with current beta. Its tree contains 91 files versus 62 on beta, including old package-name source, an obsolete layout/activity set, old updater/build files and the legacy Addon Manager assets. These are not safe to merge into current beta and are not part of the active product. The branch is retained as a deletion candidate/archive pending explicit owner confirmation; it has not been deleted.


## Issue ownership map — 2026-11-25

- **Ploy ACTIVE:** #56 direct-launch and responsive overlay. One active issue at a time; all other Ploy work is queued.
- **Ploy queue:** #9 accessibility → #11 diagnostics tests → #12 GeckoView 160 load errors → #14 connection diagnostics → #16 orientation/keep-awake → #10 persistent network totals → #28 storage accuracy → #6 essential settings/HTTPS URL → #17 settings backup/restore. Start each only after the previous Ploy issue is merged by Chaty. Exact narrow scopes and exclusions are recorded in each issue and AGENT_WORK.md.
- **Chaty:** #4/#15 updater reliability, #20 GeckoView dependency record, #31 release workflow, #61–#66 CI/build/security, #70 docs validation, roadmap and integration.
- **Repository owner:** optional release feedback in #5/#21; #7 measured performance work when scheduled.
- **Owner decision:** #67 license selection. #23 Addon Manager remains ON HOLD and has no assigned agent.
- Ploy is an internal agent role, not a confirmed GitHub username; assignments are marked in issue bodies, not through an invented GitHub assignee.


## Documentation validation — issue #70 (2026-11-25)

- Checked all 11 Markdown files in the beta tree for relative link targets and balanced fenced code blocks; no missing relative targets or unclosed fences were found.
- Found and fixed 20 broken backlog-index fragments in `ISSUES.md` by adding explicit stable anchors for issues #4–#23; see PR #78.
- A dedicated `markdownlint` CLI was not available in the execution environment. A basic whitespace/fence scan identified historical formatting noise in append-only logs and issue/status records; these were not mass-reformatted.
- Issue #70 remains open pending a full Markdown parser/linter run and final stale-reference review. Documentation-only change; no application, build, CI, signing, release or device behavior changed.


## Issue #56 implementation candidate — 2026-11-25

- **Owner:** Chaty, taking over from Ploy at the repository owner's direction. Ploy must not work on #56 concurrently.
- **Branch:** `chaty/issue-56-direct-launch-overlay`, based on beta `01af3f488225eaa5321b994a3957e53dcdb70c55`.
- **Implemented on candidate branch:** cold startup now initializes the game directly; Android Back toggles a translucent in-game menu overlay while keeping the Game destination/session mounted; Settings opens as a separate destination and returns to gameplay; gameplay orientation remains landscape while the menu restores system bars; Settings restores normal orientation/system UI; persistent page-load progress strip removed.
- **Still required:** focused navigation/state tests, Spotless, lint, unsigned release build, JVM/Python tests and exact-head CI. Inspect GeckoView rendering order to confirm Compose overlay appears above the game on device. The owner may report portrait/landscape rendering feedback after installing a release; this is not a release gate.
- **Integration:** no PR opened or merge performed yet; beta and main remain unchanged. No release/tag/publication.

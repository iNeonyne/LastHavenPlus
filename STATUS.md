# Project status — LastHavenPlus

## Active task — Versioned CI APK artifact filenames (issue #4)

- Owner: Chaty (`agent:chaty`); no Vibe file overlap.
- Branch: `chaty/versioned-apk-artifact-names`, based on beta `ec721c3da9a4a6f696f644ad9ad78751b36cdd10`.
- Scope: versionName- and channel-qualified filename for the signed Actions APK artifact; preserve the updater's canonical `lasthavenplus-arm64.apk`.
- State: implementation prepared for PR; exact-head validation and signed beta verification not yet run.
- Release safety: no change to APK identity, signing, ABI, updater manifest, canonical release asset, tags, or publication.


**Last updated:** 2026-11-25  
**Active branch:** `beta`  
**Latest beta integration commit:** `932e3ba65f6c8445eafe0ba466ca3952c67bd017` (PR [#99](https://github.com/iNeonyne/LastHavenPlus/pull/99), Gradle dependency/plugin version catalog; previous product integration remains PR #85/#83).  
**Stable promotion commit:** `f22b5c2b92b6f93b201b1a9dc50a8c8095543d76` (PR [#87](https://github.com/iNeonyne/LastHavenPlus/pull/87), selective product/docs promotion to main; stable-only history preserved).  
**v2.2.0 beta validation:** PR #85 exact-head validation [#45657071506](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45657071506) passed lint, formatting, unsigned release build and unit tests. Post-merge beta quality [#45657785104](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45657785104) and signed ARM64 build [#45657785102](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45657785102) passed; signed artifact uploaded; tag/publication steps skipped.
**v2.2.0 main validation:** selective PR #87 merged at `f22b5c2b92b6f93b201b1a9dc50a8c8095543d76`. Exact main quality [#45658476528](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45658476528) and signed ARM64 build [#45658476677](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45658476677) passed; signed artifact uploaded; tag/publication steps skipped.
**Earlier updater workflow integration:** PR [#54](https://github.com/iNeonyne/LastHavenPlus/pull/54) merged to beta at `b12da7651445d15080e7593d441459a8c8403070`; exact-head unsigned and post-merge signed CI both passed.  
**Stable documentation:** owner-authorized v2.1.0 corrections are on `main`; README commit `fdae3d90cf3abb209df8a42dd73ed1667bae9808`, CHANGELOG commit `2abe7cc212b67a3b91f5dcdb108aee3a55559e32`.  
**Stable promotion and publication:** PR [#47](https://github.com/iNeonyne/LastHavenPlus/pull/47) promoted to `main`; tag/release `v2.1.0` is published at `03fcfa9652ce4e7d1034523d640a1693e6e6b9ce`.  
**Current phase:** v2.1.0 remains the latest published release. The v2.2.0 candidate scope (#56 direct launch/overlay and #84 recreated icon) is integrated in beta and selectively promoted to main by PR #87. Beta exact-head PR validation, post-merge quality and signed ARM64 CI passed. Main exact-commit quality [run #45658476528](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45658476528) and signed ARM64 build [run #45658476677](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45658476677) both passed. The signed artifact was uploaded; release-tag, differential-generation and publication steps were skipped. No v2.2.0 version bump, tag or publication has been created. Owner feedback is collected after release on the owner's schedule and is not a blocker. PR #54 fixed draft asset cleanup/verification by release ID; the release-ID path still needs a future owner-authorized versioned release run.
**Earlier quality-gate integration:** issue #60 merged by PR [#72](https://github.com/iNeonyne/LastHavenPlus/pull/72) at `82e819a43e6d728a9eb8fea620eca47ac9c998df`. Exact-head PR validation passed in [run #45627915789](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627915789). Post-merge Android quality run [#45631250015](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631250015) and beta build run [#45631249817](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631249817) completed successfully. The beta build verified signed release APK assembly, unit tests, established signing certificate and ARM64-only ABI; release-tag publication steps were skipped.

## Latest beta integration — PR #99 (2026-11-25)

- **Issue:** #64 — centralize Gradle dependency and plugin versions.
- **PR:** [#99](https://github.com/iNeonyne/LastHavenPlus/pull/99), squash-merged to `beta`.
- **PR head:** `278a2615261d2b5ffc63038ef4192902af630c24`.
- **Merge commit / verified beta HEAD:** `932e3ba65f6c8445eafe0ba466ca3952c67bd017`; GitHub comparison confirms `beta` is identical to this commit.
- **Scope:** added `gradle/libs.versions.toml`; converted root and app Gradle scripts to catalog aliases. Existing pinned versions and dependency coordinates were preserved; no app source, application ID, signing identity, GeckoView version or ABI policy changed.
- **Exact-head PR CI:** [run #45686955854](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45686955854) passed lint, Spotless, unsigned release build, JVM unit tests and Python updater-helper tests for the PR head. Release-tag and signed-build jobs were skipped in PR context, as intended.
- **Post-merge CI:** not yet verified in the available run evidence. Do not treat the PR run as signed/packaging verification for the merge commit.

## Current source state

- Mozilla GeckoView 160 is the embedded browser engine; Cefrium is removed from the production build.
- The app launches directly into the game.
- Android Back opens a responsive translucent overlay above gameplay with exactly Reload, Settings and Return to game; the game remains mounted behind the menu.
- Settings, diagnostics and update details are full-screen pages. The existing GeckoSession is retained while its view is detached for launcher navigation.
- The approved recreated adaptive launcher artwork is integrated for standard and round icons; the Activity remains resizeable.
- The updater remains manual: no automatic downloads or installation.
- Addon-manager work under #23 is on hold by owner instruction. No addon manager is integrated; do not resume until explicit owner approval.

## Integration and CI evidence

| Check | Result |
|---|---|
| v2.2.0 icon PR #85 | Merged to beta at `073f839eb3cccd9a13fae09dd091667bb86a1f0c`; exact PR CI #45657071506 passed |
| v2.2.0 beta quality/build | Runs [#45657785104](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45657785104) and [#45657785102](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45657785102) passed; signed ARM64 APK artifact uploaded |
| v2.2.0 stable promotion PR #87 | Merged to main at `f22b5c2b92b6f93b201b1a9dc50a8c8095543d76`; preserved main-only history |
| v2.2.0 main quality/build | Runs [#45658476528](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45658476528) and [#45658476677](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45658476677) passed; signed ARM64 APK artifact uploaded; no tag or publication |
| PR #43 head | `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3` |
| PR #43 validation | [Run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) passed unsigned release-variant build and JVM unit tests |
| PR #43 merge | `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210` |
| Post-merge beta build | [Run #45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks, as recorded in merged PR #47 |
| Updater tag validation | PR #41 merged at `1d35e520331ca9ffa3a9b41b21402a3110728001`; v2.1.0 tag creation succeeded in release run #45603533787 |
| v2.1.0 tag release | [Run #45603533787](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45603533787): unit tests, signer, established certificate, ARM64-only ABI, differential generation and upload/publish steps passed; final draft-by-tag lookup failed HTTP 404. Release metadata confirms published assets |

PR validation is unsigned and does not access release secrets or publish APKs. A successful PR run does not verify signing, APK ABI contents or release artifacts.

## v2.1.0 UI scope (historical; superseded by v2.2.0)

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
- **Chaty:** completed #56 direct launch/overlay in PR #83 and #84 icon integration in PR #85. Both are selectively promoted to main by PR #87.
- **Owner feedback:** #5/#21 are post-release feedback trackers. The owner will share findings on their own schedule; pending feedback does not block release.
- **Supersession:** #42's old Home-screen-first scope is superseded by #56. The direct-launch implementation is integrated; optional owner feedback is tracked separately in #5/#21.
- **CI gate #60:** PR #72 merged at `82e819a43e6d728a9eb8fea620eca47ac9c998df`. Exact PR validation [#45627915789](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45627915789), post-merge quality [#45631250015](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631250015), and signed beta build [#45631249817](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45631249817) passed. The beta build skipped release-tag publication steps.
- **Addon Manager #23:** remains on hold; no assignment or implementation without explicit owner approval.


## Branch audit — 2026-11-25

- The branch inventory/diff review was performed against beta snapshot `1c82e8b4aee2b0ce9ef1d9e6a68311dca4eb200b`; this is a historical audit baseline, not the current beta HEAD. The beta HEAD at the start of this documentation follow-up is recorded at the top of this file (`1adc890343fb4389ad76144eb00fed2ea33e316e`). No ordinary stale Chaty/Ploy feature branches remained at the time of that inventory.
- The six Dependabot branches correspond to six open dependency PRs and are retained until those PRs are resolved.
- `beta-legacy-2026-11-24` is an orphaned legacy snapshot with no common ancestor with current beta. Its tree contains 91 files versus 62 on beta, including old package-name source, an obsolete layout/activity set, old updater/build files and the legacy Addon Manager assets. These are not safe to merge into current beta and are not part of the active product. The branch is retained as a deletion candidate/archive pending explicit owner confirmation; it has not been deleted.


## Issue ownership map — 2026-11-25 (post-v2.2.0 integration)

- **#56:** completed and integrated; no active Ploy assignment. Supporting work (#9, #11, #12, #14, #16, #10, #28, #6, #17) is deferred to the v2.3.0 backlog in #86. Do not start a new Ploy task until the owner prioritizes it.
- **Chaty:** #4/#15 updater reliability, #20 GeckoView dependency record, #31 release workflow, #61–#66 CI/build/security, #70 docs validation, roadmap and integration.
- **Repository owner:** optional release feedback in #5/#21; #7 measured performance work when scheduled using comparable, repeatable measurements.
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

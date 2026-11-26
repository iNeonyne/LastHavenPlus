# Project status — LastHavenPlus

**Last updated:** 2026-11-26  
**Active branch:** beta  
**Beta HEAD at this verification checkpoint:** 0350acaee3f277e01ac51a3722346ab228e23af3.  
**Stable branch / v2.3.0 tag:** ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88; GitHub confirms published, non-draft, non-prerelease [v2.3.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.3.0) with canonical ARM64 APK, update manifest and v2.2.0-to-v2.3.0 differential package. Main remains unchanged by this housekeeping work.

## Latest integration — v2.4.0 launcher icon

- **PR:** [#135](https://github.com/iNeonyne/LastHavenPlus/pull/135), squash-merged to beta at `c24b24e7125f49e86f465b1786ccfecb537e9694`.
- **Exact-head Android validation:** [run #45742710788](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45742710788) passed lint, Spotless, unsigned release assembly and JVM tests. PR validation remained unsigned; signing/tag/publication jobs were skipped.
- **Exact-head Markdown validation:** [run #45742710826](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45742710826) passed.
- **Post-merge beta validation:** Markdown push run [#45743277532](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45743277532) passed. Signed ARM64 beta build [#45743277446](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45743277446) passed on exact application merge commit `c24b24e`. It built and unit-tested the optimized APK, verified ARM64-only packaging (`arm64-v8a`), matched the established signing certificate SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263`, and uploaded artifact `LastHavenPlus-2.3.0-beta-arm64-v8a` (92,813,021 bytes). Tag and publication jobs were skipped as required.
- **Artwork:** native icy-blue pixel-art castle/moon vector, deep-navy adaptive background and 8% foreground inset; standard and round adaptive icon resources remain wired. Local mask simulations were inspected, but Android-generated launcher screenshots and physical ARM64 appearance remain unverified.
- **Issue #133:** implementation is merged, but the issue was reopened as `agent:owner` because Android-generated mask previews and physical ARM64 launcher appearance still need owner verification.

## Documentation checkpoint — PR #137

- PR [#137](https://github.com/iNeonyne/LastHavenPlus/pull/137) merged at `f800deebf6c8bdaa91df4ac7c3693dcf86f3e6b6`; exact Markdown lint [run #45743490458](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45743490458) passed. This changed documentation only.

## Previous integration — documentation reconciliation

- **PR:** [#134](https://github.com/iNeonyne/LastHavenPlus/pull/134), merged to beta at e69656303d7e1c1e7d50f7211c8cfaeb748f07e9.
- **Verification:** exact-head Markdown quality gate [run #45739130533](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45739130533) passed. This was documentation-only; no Android build or device test was applicable.

## Previous integration — CI consolidation

- **PR:** [#132](https://github.com/iNeonyne/LastHavenPlus/pull/132), squash-merged to beta at 7cd898f4c1f9b7f70787a7000e1ed9343a83540d.
- **Previous attempt:** PR #131 was closed unmerged because its base became stale; it was superseded by #132. No implementation was lost.
- **Exact-head PR validation:** [run #45737717923](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717923) passed lint, Spotless, unsigned release build, JVM tests and Python updater-helper tests. Signing/release jobs were skipped in PR context.
- **Markdown validation:** [run #45737717837](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717837) passed.
- **Post-merge beta validation:** [run #45738073790](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45738073790) passed on the exact merge commit. The changed-path detector selected beta quality checks; the single Gradle invocation ran lintDebug, spotlessCheck, assembleRelease and testDebugUnitTest and succeeded. The signed APK passed established-certificate verification (SHA-256 b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263) and packaged exactly arm64-v8a. Artifact LastHavenPlus-2.3.0-beta-arm64-v8a uploaded (92,815,476 bytes). Tag, differential and publication steps were correctly skipped.
- **Observed CI runner time:** the consolidated beta build job took 94 seconds. The previous comparable baseline used a 258-second signed build job plus a separate 123-second quality job (381 runner-seconds total). This single observation is approximately 75% less combined runner time; treat it as an initial measurement, not a statistically stable benchmark. The combined Gradle build/test/quality tasks completed in 57 seconds.
- **CI security boundary:** PR validation remains unsigned, read-only and secret-free. Signing secrets remain confined to the trusted beta build job. No main workflow, release/tag, app source, dependency or toolchain change was made.

## Current source and release state

- Mozilla GeckoView 160 is the embedded browser engine; Cefrium is removed from production.
- The app launches directly into the game. The Back menu is a responsive overlay; Settings, diagnostics and update details are launcher-owned full-screen pages.
- Updater downloads and installation remain user-initiated. Preserve application ID, established signing identity and ARM64-only packaging.
- Addon Manager issue #23 remains on hold by owner instruction.
- v2.3.0 is published. The owner requested the next three-version roadmap; issue #18 now records v2.4.0 UI/UX, v2.5.0 updater experience and v2.6.0 settings/diagnostics. This is planning only; no new release, tag or promotion is authorized.

## Open work and limitations

- **Housekeeping:** issue #128 is active on branch chaty/post-v2.3-housekeeping; this PR reconciles the current records while preserving historical entries.
- **Updater:** #4 remains open for remaining automated regression coverage. #15, #31 and #111 remain blocked/deferred; no release-ID/differential fallback verification or updater UX work is claimed complete.
- **Reproducibility:** PR [#119](https://github.com/iNeonyne/LastHavenPlus/pull/119) / issue #66 remains open. An earlier exact commit produced byte-identical unsigned APKs; the PR branch is behind current beta and must be refreshed and revalidated before merge.
- **Owner-paused drafts:** PR #122 / issue #20 (GeckoView dependency audit) and PR #123 / issue #62 (branch lifecycle audit) remain draft/open and blocked by explicit owner scope resets. Do not add commits or resume without reprioritization.
- **UI/UX:** issue #107 is assigned to Vibe (`agent:vibe`). PR [#136](https://github.com/iNeonyne/LastHavenPlus/pull/136) contains the visual-system implementation. Original exact-head Android run [#45743147121](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45743147121) passed, but only against the pre-icon base. Beta advanced after PRs #135 and #137; #136 is draft/non-mergeable and Vibe has been asked to merge current beta into the branch, resolve `AGENT_WORK.md`, update the handoff and rerun exact-head CI. PR #113 remains traceability-only.
- **Icon:** PR #135 is merged at `c24b24e`; exact-head PR checks and post-merge signed ARM64 CI passed. Issue #133 is reopened as `agent:owner` for actual Android adaptive-mask/physical launcher appearance verification; CI does not prove launcher rendering.
- **Accessibility:** issue #9 remains unassigned until the visual-system pass is settled, to avoid overlapping Compose screen work.
- **Documentation quality:** PR #120 added repository-wide markdownlint; current beta Markdown quality run [#45736853811](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45736853811) passed. Issue #70 remains open until this central reconciliation is reviewed and merged.
- **Device status:** CI and packaging checks do not prove physical ARM64 gameplay, profile persistence, haptics, updater installation or device-specific behavior. No new physical-device testing was performed.

## Next-version roadmap

- **v2.4.0 — UI/UX:** icon resources (#133, Chaty) and shared Compose visual system (#107, Vibe) proceed in parallel; accessibility/navigation coverage (#9) follows #107 integration. No game/client changes or new home screen.
- **v2.5.0 — updater:** truthful download progress and retry feedback (#111), plus coordinated regression/recovery coverage (#4/#15/#31) after prioritization.
- **v2.6.0 — settings/diagnostics:** essential settings (#6), connection and GeckoView error diagnostics (#12/#14), orientation/keep-awake (#16), storage/network accuracy and safe backup/restore (#10/#17/#28) in dependency order.
- Roadmap source: issue #18. Version targets are not release commitments. #23 remains on hold; #19 and #7 remain deferred.

## Historical integration records

STATUS historical records below this heading are retained for traceability; their dated branch heads, release candidates and roadmap wording describe earlier snapshots and must not be treated as current state.

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
- **Addon Manager #23:** owner reauthorized work on 2026-11-25/26. Native Phase A is in draft PR #145; exact-head CI/review are pending. Runtime injection and in-game addon initialization remain disabled.


## Branch audit — 2026-11-25

- The branch inventory/diff review was performed against beta snapshot `1c82e8b4aee2b0ce9ef1d9e6a68311dca4eb200b`; this is a historical audit baseline, not the current beta HEAD. The beta HEAD at the start of this documentation follow-up is recorded at the top of this file (`1adc890343fb4389ad76144eb00fed2ea33e316e`). No ordinary stale Chaty/Ploy feature branches remained at the time of that inventory.
- The six Dependabot branches correspond to six open dependency PRs and are retained until those PRs are resolved.
- `beta-legacy-2026-11-24` is an orphaned legacy snapshot with no common ancestor with current beta. Its tree contains 91 files versus 62 on beta, including old package-name source, an obsolete layout/activity set, old updater/build files and the legacy Addon Manager assets. These are not safe to merge into current beta and are not part of the active product. The branch is retained as a deletion candidate/archive pending explicit owner confirmation; it has not been deleted.


## Issue ownership map — 2026-11-25 (post-v2.2.0 integration)

- **#56:** completed and integrated; no active Ploy assignment. Supporting work (#9, #11, #12, #14, #16, #10, #28, #6, #17) is deferred to the v2.3.0 backlog in #86. Do not start a new Ploy task until the owner prioritizes it.
- **Chaty:** #4/#15 updater reliability, #20 GeckoView dependency record, #31 release workflow, #61–#66 CI/build/security, #70 docs validation, roadmap and integration.
- **Repository owner:** optional release feedback in #5/#21; #7 measured performance work when scheduled using comparable, repeatable measurements.
- **Owner decision:** #67 license selection. #23 Addon Manager is actively owned by Chaty (`agent:chaty`) under draft PR #145.
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


## 2026-11-26 — Accessibility overlay integrated (#9)

- **PR:** [#139](https://github.com/iNeonyne/LastHavenPlus/pull/139) squash-merged by Chaty to `beta` at `0d5ab712ce5195a3e71023c68da54f054645a8e2` (PR head `4b4d0586d2c80310d2b78e05271bc255dc4f90de`).
- **Pre-merge exact-head CI:** Android validation [#45829421214](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45829421214) and Markdown gate [#45829421210](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45829421210) passed.
- **Scope:** Game menu pane title, heading/traversal semantics, Compose input-focus entry, scrollable menu and live reduced-motion observation. No GeckoSession replacement or game-client change.
- **Outstanding:** physical ARM64 TalkBack pane announcement/focus/traversal, GeckoView-relative order, reduced-motion device behavior and large-font rendering were not tested. `FocusRequester` moves Compose input focus, not TalkBack accessibility focus. Owner authorized merge with these limitations recorded; issue #9 remains open as `agent:owner` for device verification. No physical-device compatibility claim.
- **Post-merge:** merge commit is the current beta HEAD at this checkpoint; post-merge CI has not yet reported a result.


## 2026-11-26 — Addon Manager Phase A review checkpoint (#23)

- **Base:** current `beta` HEAD `0350acaee3f277e01ac51a3722346ab228e23af3`.
- **PR:** [#145](https://github.com/iNeonyne/LastHavenPlus/pull/145), draft, unmerged; branch `chaty/addon-manager-phase-a-current-beta`.
- **Implemented candidate:** bounded ZIP import/export, app-private addon library, extraction and staging cleanup, storage preflight before expanded payload writes, transactional replacement/rollback, TOC metadata including optional dependencies, iterative dependency-cycle analysis, localized removal confirmation, and overlay navigation back to the retained game screen.
- **Latest validation history:** prior exact-head run [#45852470193](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45852470193) failed one JVM test because the rollback fixture encoded a literal backslash-n instead of a newline. The run's `minifyReleaseWithR8` task completed; R8 was not the failure. The fixture was corrected and further preflight/optional-dependency coverage was added. New exact-head validation is pending.
- **Runtime limitation:** this candidate is native local management only. The supplied Chrome MV3 background/service worker, XHR/Worker interception, game-resource masking and in-game Lua initialization are not implemented or enabled. GeckoView 160 runtime compatibility and session/lifecycle integration still require dedicated tests.
- **Verification limits:** no physical ARM64 device test has been performed. No merge, release, tag, or main promotion.

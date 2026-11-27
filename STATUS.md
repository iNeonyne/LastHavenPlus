## Current project snapshot — 2026-11-27

- **Verified beta baseline before PR #203 synchronization:** `ce13c2968c67fe680dc38c35ad13a5c7bb78ab73`.
- **Browser engine:** Mozilla GeckoView 161 Stable, ARM64 only; AndroidX Core 1.19.1; ktfmt 0.65.
- **Integrated:** PR #199 (GeckoView 160 audit), PR #200 (manual dependency-report workflow), and PR #201 (GeckoView 161/dependency audit). Issues #20 and #188 are closed after successful integration.
- **PR #203 / issue #67:** current-beta rights and third-party notice work. The owner has directed that Google Play services must not be used. The entire `com.google.android.gms` group is excluded from GeckoView 161; release CI verifies the resolved dependency graph and DEX class definitions. GeckoView WebAuthn/passkey sign-in is unavailable in this build. The Settings viewer includes the project-rights notice, third-party notices, Apache-2.0/MPL-2.0 texts and the 94-entry GeckoView 161 about:license inventory.
- **PR #203 exact-head verification:** see the live pull request for current check runs. Checks on any pre-synchronization head do not validate a later branch merge; rerun the full required suite after synchronizing with beta.
- **Addon Manager Phase B remains the top project priority**, gated by navigation/back-stack #147, bridge-port work #183, and physical ARM64 acceptance #167.
- No physical ARM64 device test of GeckoView 161 has been performed. No release, tag, or `main` promotion is authorized by this snapshot.
- Dated sections below are historical records; their branch heads and task statuses must not override this current snapshot.

## Current owner-directed assignments — 2026-11-27

- **#147 (P1):** Vibe is the sole active implementation owner for launcher navigation/back-stack consolidation. Dedicated branch and PR to `beta`; Chaty owns review, fixes, exact-head CI, merge and closure.
- **#167 (P0):** Chaty owns physical ARM64 runtime acceptance for the already-integrated GeckoView bridge; do not claim device behavior without direct observation.
- **#183:** remains `agent:blocked` until Vibe hands off #147 and Chaty confirms the next assignment.
- **#170 (P1):** remains `agent:blocked` behind #147, #183 and #167's runtime acceptance gates. Do not start overlapping manager-navigation implementation early.
- **#169:** closed; no active Vibe assignment.

Chaty remains sole reviewer, fixer, exact-head CI verifier and merger into beta. Do not claim physical device testing or actual in-game addon/Lua initialization unless observed. No release, tag, publication or main promotion is authorized.

## 2026-11-27 — Vibe submitted #169 Worker interception and blocks.idx masking implementation

- **Branch:** `vibe/worker-blocks-masking-169`, based on verified beta HEAD `837bfe9` (post-#194 beta). PR targeting `beta` is submitted and awaits Chaty integration; the exact commit SHA is recorded in the PR and the issue comment.
- **Implementation:** new MAIN-world content script `worker-overlay.js` — fail-closed exact-URL Worker allowlist gate (inert while empty), bounded `em-pthread` blob bootstrap with 20 s boot timeout, queued-message cap 64, max 8 concurrent intercepted workers, object-URL revocation and teardown on every failure/termination path, idempotent `assignWasmImports` wrapping, read-only `blocks.idx` TAB-record parser with strict bounded limits and stage diagnostics, masking from bounded read-only OPFS spans: zero column-2 digits and replace the first byte of enabled-addon paths in column 5, and a worker-side XHR forwarder (GET/HEAD only, addon routes, inflight cap 4, sync fail-closed 404, non-addon passthrough) that reuses the `__LHAddonXhrOverlayCore` route/content-type resolution page-side.
- **Local verification (executed):** `node --check` on all five extension JS assets passed; `node --test scripts/test-addon-worker-overlay.mjs` passed 61/61; `node --test scripts/test-addon-xhr-overlay.mjs` and `scripts/test-addon-bridge.mjs` passed unchanged; JVM asset/manifest guard tests updated for the sixth asset and third content-script entry.
- **Not claimed:** no physical ARM64 device test, no in-game addon/Lua initialization, no physical ARM64 test or in-game addon/Lua initialization. The Worker URL allowlist is empty by default, so the overlay is inert until the exact worker URL is hardware-verified and registered.
- **No release, tag, publication or beta-to-main promotion is authorized.**

## 2026-11-27 — PR #191 merged; Worker masking assigned (#169)

- **Verified beta HEAD:** `d160b052370bbcbae6925174ac2cb10ec609ff91`.
- **PR #190:** merged at `1d5f55d0dcfdf37d1fa49c7f795adfdc3244cf5d`; exact-head Android #45950285046, independent unsigned APK reproducibility #45950285094 and Markdown #45950285026 passed.
- **PR #191 / issue #168:** merged at the current beta HEAD `d160b052370bbcbae6925174ac2cb10ec609ff91`; exact-head Android #45951682137, independent unsigned APK reproducibility #45951682107 and Markdown #45951682194 passed.
- **Vibe active assignment:** #169 — bounded target Worker bootstrap and read-only `blocks.idx` masking. Assignment posted on the issue; branch must start from the verified beta HEAD above.
- **Chaty:** #23 coordination/integration and #167 bridge runtime/device verification. #167 remains open; source-level bridge/payload/session wiring is integrated, but physical ARM64 runtime behavior and actual addon/Lua initialization are not verified.
- **#170:** remains blocked until #169 is integrated and its same-session manager-navigation prerequisites are ready.
- **Paused:** updater #4/#15 remain queued pending a safe Addon Manager milestone handoff.
- **No release, tag, publication or beta-to-main promotion is authorized. No physical ARM64 test or in-game addon initialization is claimed.**

## 2026-11-27 — PR #190 merged; PR #191 exact-head checks passed

- PR #190 merged to `beta` at `1d5f55d0dcfdf37d1fa49c7f795adfdc3244cf5d`. Exact-head Android validation #45950285046, independent unsigned APK reproducibility #45950285094 and Markdown #45950285026 passed.
- PR #191 / issue #168 current head `984dfca7dd9563c68d611fa849b8f77d7932cbb5`: Android #45950055468, independent unsigned APK reproducibility #45950055462 and Markdown #45950055420 passed.
- PR #191 must be synchronized with the new beta bridge/session integration and rerun exact-head checks before merge. No physical ARM64 test or in-game addon activation is claimed.

# Project status — LastHavenPlus

## 2026-11-27 — PR #180 integrated; Vibe assigned XHR overlay

- **Owner priority:** Addon Manager Phase B (#23) remains P0. Chaty owns coordination and integration.
- **Integrated bridge work:** PR #177 native-messaging foundation and PR #180 bounded ZIP 0.5.1 payload transport are merged. PR #180 merge commit and verified `beta` HEAD: `892494aa8f0333f907b2e730f6b754ba2dabe2a1`.
- **Vibe active assignment:** #168 — bounded XHR resource overlay. It is Vibe's sole active implementation task, assigned from the verified beta HEAD above.
- **Remaining sequence:** #169 Worker/`blocks.idx` masking remains `agent:blocked` until #168's resource contract is reviewed and integrated. #170 same-session manager navigation remains `agent:blocked` until runtime and navigation prerequisites are satisfied. #167 remains open and Chaty-owned for integration follow-up and physical-device verification.
- **Exact-head PR evidence for #180:** Android PR validation [#45938326655](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45938326655) passed JavaScript checks, lint, Spotless, unsigned release build, JVM tests and MIT-notice verification. Independent unsigned APK reproducibility [#45938326638](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45938326638) passed both clean builds and archive/hash comparison. These are PR-head checks; no post-merge beta workflow result is recorded here.
- **Known limitations:** no XHR/Worker runtime interception, same-session manager navigation, actual in-game addon initialization or physical ARM64 runtime test is verified. The merge does not establish gameplay compatibility.
- **Paused work:** updater #4/#15 remain queued/paused pending a safe Addon Manager milestone handoff.
- **Release safety:** no release, tag, publication or beta-to-main promotion is authorized.

**Last updated:** 2026-11-27  
**Active branch:** beta  
**Verified beta HEAD:** `892494aa8f0333f907b2e730f6b754ba2dabe2a1`  
**Stable release:** [v2.3.0](https://github.com/iNeonyne/LastHavenPlus/releases/tag/v2.3.0), tag commit `ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88`.

## Historical operational checkpoint — 2026-11-27 (before PR #173 integration)

- **PR #165 / issue #149:** squash-merged to beta at `7e39d2553a11332d44326c1704e0d7647cc6aa40`. Exact-head Android/unit tests #45919879256, independent unsigned APK reproducibility #45919879249 and Markdown #45919879248 passed on head `a62c7cd3154586d35d85a99ed1964d33ba007733`. Issue #149 closed. Launcher-owned strings, including UpdateScreen labels/actions, now use default `values/strings.xml`; untranslated English locale duplicates were removed.
- **PR #173 / issue #146 (historical):** Vibe's GameSessionController change was integrated at `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`; post-merge Android/signing/ARM64, unsigned reproducibility and Markdown checks passed (runs #45923143717, #45923143712 and #45923143716). Physical ARM64 gameplay/session testing remains unverified.
- **Vibe handoff:** #167 has been handed off in draft PR #177 (`4fc06e5328bef96aec42b117400c350a2b00d348`); ownership is now `agent:review`. Chaty reviews the bridge before merge. PR #173 is already integrated; its files are no longer a blanket exclusion, but the #167 bridge must remain isolated and must not wire session/navigation behavior reserved for #170.
- **Addon Manager:** PR #166 planner groundwork is merged at `d83e2dfc89087f67f4dbbe71e85668d80303e3ef`; post-merge Android #45919654642 and reproducibility #45919654675 passed. Runtime extension installation, XHR/Worker interception and actual addon activation remain unimplemented.
- **Beta HEAD:** `7e39d2553a11332d44326c1704e0d7647cc6aa40`. Post-merge Android #45920923646, Markdown #45920923679 and unsigned reproducibility #45920923633 were running at this checkpoint.
- **Device/release:** no physical ARM64 testing, release, tag, publication or beta-to-main promotion is claimed or authorized.

## Historical operational checkpoint — 2026-11-26

- **Latest integrations:** PR #158 permission-policy documentation merged at 7451b940dc0adfa1ea14234fe6d489bba688b12c; post-merge Markdown run #45890132670 passed. PR #161 repository housekeeping merged at a0e5271793c9bb7754b7ac41429065e5602df569; exact-head Markdown #45892727113 and post-merge Markdown #45892884986 passed. Both changes are documentation-only; no Android/device test was applicable.
- **Addon Manager (P0):** Phase A and planner groundwork are integrated; Phase B is the top project priority. #23 is Chaty-owned; #167 is Vibe's active bridge assignment; #168/#169 remain dependency-blocked and #170 remains blocked on runtime/navigation prerequisites.
- **UI integrations:** adaptive icon PR #135 merged at c24b24e7125f49e86f465b1786ccfecb537e9694; visual-system PR #136 at e0967121b51ac1c232c92edd80157fc02b714766; accessibility PR #139 at 0d5ab712ce5195a3e71023c68da54f054645a8e2. These are integrated source changes; CI is not physical-device evidence.
- **Updater:** #4 and #15 are coordinated under Chaty. The BSDIFF partial-open cleanup candidate was not merged: PRs #157, #159 and #160 are closed unmerged. On PR #160 head d0cb2f5144257a2270ae0ed42f649833ad688207, Markdown run #45891807077 passed, but reproducibility build A failed resolving org.jetbrains.kotlin.plugin.compose:2.4.21; build B and Android validation were still running when the PR was closed. No updater code from those PRs is integrated. Full #4 acceptance remains incomplete.
- **Diagnostics:** PR #163 merged the bounded diagnostics test work into beta. Issue #11 remains open/blocked only for the explicitly deferred runtime-version startup reporting; do not describe it as Vibe's active task.
- **Housekeeping:** #128 completed through PR #161 at a0e5271793c9bb7754b7ac41429065e5602df569. Exact-head Markdown #45892727113 and post-merge Markdown #45892884986 passed.
- **Open PR inventory:** GitHub returned no open PRs at this checkpoint. Closed unmerged work remains documented in its issues and is not treated as integrated.
- **Assignment model:** every open issue must have exactly one agent label. agent:queued is ready/waiting and does not authorize work; agent:blocked is reserved for real prerequisites/holds. Vibe active: #167 (Addon Manager P0). #168/#169 wait for bridge/security contracts; #170 waits for runtime/navigation prerequisites. Chaty owns #23 coordination; #4/#15 are queued pending the Addon Manager milestone. See AGENT_WORK.md for the live issue inventory.
- **Device status:** no new physical ARM64 device testing was performed or claimed.

## Historical Addon Manager Phase A implementation checkpoint (#23)

The following dated implementation notes are retained as history. Their old draft-PR state, branch heads and pending CI status were superseded by PR #152 merging at 103bb979c98589a8704459d28d13861eccca39d6. Phase B remains unimplemented and owner-controlled.

## 2026-11-26 — Addon Manager Phase A implementation checkpoint (#23)

- **Owner:** Chaty (`agent:chaty`) took over the remaining implementation at the repository owner's explicit direction. Vibe was reassigned to issue #11 (`agent:vibe`) and must not push to the PR #152 branch.
- **PR:** [#152](https://github.com/iNeonyne/LastHavenPlus/pull/152), draft, targeting `beta`; branch `vibe/addon-manager-phase-a-23` is synced with beta `97570eaaca86c9e27d92d04f90da9d930f6bdb33`, current head `cc15080282b24df986dee2a10788e34e1cbbe511` (docs reconciliation commit; exact-head CI rerun required).
- **Scope:** Phase A native local management only — bounded ZIP import/export, app-private library, storage preflight, staging/rollback, TOC dependency metadata, iterative dependency analysis, localized removal confirmation and launcher navigation. Recovery corrections now include replay-safe rollback, explicit old/new index revision decisions, fail-closed malformed journal/index handling, safe in-root journal path validation and crash-window regression tests.
- **Exact-head CI:** Android [#45875013105](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45875013105) and independent reproducibility [#45875013161](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45875013161) are running; Markdown [#45875013185](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45875013185) passed. Prior implementation checkpoint [#45872047540](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45872047540) passed lint, Spotless, unsigned release assembly and 137/137 JVM tests; packaged MIT notice assertion passed on [#45873000772](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45873000772). No merge yet.
- **Runtime limitation:** Chrome MV3 background/service-worker execution, XHR/Worker interception, game-resource masking and Lua initialization remain unimplemented. GeckoView 160 runtime/lifecycle integration remains a separate gate; Phase B is not authorized in this PR.
- **Device status:** no physical ARM64 device test has been performed.

## Historical integration — v2.4.0 launcher icon

- **PR:** [#135](https://github.com/iNeonyne/LastHavenPlus/pull/135), squash-merged to beta at `c24b24e7125f49e86f465b1786ccfecb537e9694`.
- **Exact-head Android validation:** [run #45742710788](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45742710788) passed lint, Spotless, unsigned release assembly and JVM tests. PR validation remained unsigned; signing/tag/publication jobs were skipped.
- **Exact-head Markdown validation:** [run #45742710826](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45742710826) passed.
- **Post-merge beta validation:** Markdown push run [#45743277532](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45743277532) passed. Signed ARM64 beta build [#45743277446](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45743277446) passed on exact application merge commit `c24b24e`. It built and unit-tested the optimized APK, verified ARM64-only packaging (`arm64-v8a`), matched the established signing certificate SHA-256 `b3bbf7520e77dfc384a60b1eee6c5f205b978f9b453849e2be523f642018d263`, and uploaded artifact `LastHavenPlus-2.3.0-beta-arm64-v8a` (92,813,021 bytes). Tag and publication jobs were skipped as required.
- **Artwork:** native icy-blue pixel-art castle/moon vector, deep-navy adaptive background and 8% foreground inset; standard and round adaptive icon resources remain wired. Local mask simulations were inspected, but Android-generated launcher screenshots and physical ARM64 appearance remain unverified.
- **Issue #133:** closed after source integration. The cited CI verifies build/package checks, not Android-generated adaptive-mask output or physical ARM64 launcher appearance.

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
- Addon Manager Phase A (#23) is integrated by PR #152 at 103bb979c98589a8704459d28d13861eccca39d6; local enablement remains metadata only, no addon runtime is active in the game, and Phase B awaits a separate owner decision.
- v2.3.0 is published. The owner requested the next three-version roadmap; issue #18 now records v2.4.0 UI/UX, v2.5.0 updater experience and v2.6.0 settings/diagnostics. This is planning only; no new release, tag or promotion is authorized.

## Open work and limitations

- **Updater reliability:** #4 and #15 are Chaty-owned and sequenced together. PR #157/#159/#160 BSDIFF cleanup candidates were closed without merge; no candidate code is integrated. PR #160 exact-head Markdown passed, but one clean reproducibility build failed resolving Kotlin Compose plugin 2.4.21 and Android/reproducibility were not both complete at closure. The broader adversarial matrix, signed APK-pair reconstruction, benchmark and current-beta signed packaging checks remain open.
- **Release workflow:** #31 is blocked pending owner authorization for release-tag packaging/runtime verification. Do not trigger a release to test it.
- **Diagnostics:** #11 is Vibe's active assignment; the bounded tests were recreated on fresh branch `vibe/diagnostics-tests-11-refresh` from current beta (stale PR #154 kept as reference only).
- **Housekeeping:** #128 is Chaty-owned on chaty/issue-128-housekeeping-beta; exact-head Markdown/link/anchor validation is pending.
- **GeckoView audit:** #20 remains owner-deferred/blocked. PR #122 is closed without merge; technical POM/runtime findings remain useful, but notice packaging, unresolved license/distribution questions and physical-device verification are incomplete.
- **Updater progress UX:** #111 remains blocked until v2.4.0 is released and the owner explicitly authorizes the v2.5.0 integration window. PR #143 is closed without merge.
- **Addon Manager:** Phase A is integrated by PR #152; Phase B remains unimplemented and issue #23 is agent:owner. No runtime injection or game-addon activation is claimed.
- **Rights/notices:** #67 remains agent:owner; PR #144 closed without merge. The repository owner's All Rights Reserved decision applies to original project material; third-party notices/distribution questions remain unresolved.
- **Dependency refresh:** #81's five approved library pins are integrated by PR #104; remaining evidence/documentation reconciliation is queued to Chaty. No toolchain or GeckoView changes are included.
- **Other holds:** #16 waits on #56 and #14; #147 waits on #146; #148 waits on #146 and coordination with #16; #12 waits on #11; #14 waits on #12; #10 waits on #16; #28 waits on #10; #6 waits on #28; #17 waits on #6. #19 waits on #4/#6. #7 and #86 remain blocked coordination/measurement items.
- **Open PRs:** none. Closed unmerged PRs are not considered integrated.
- **Device status:** CI and packaging checks do not prove physical ARM64 gameplay, profile persistence, permission prompts, haptics or updater installation. No new physical-device testing was performed.

## Next-version roadmap

- **v2.4.0 — UI/UX:** icon (#133), visual system (#107) and overlay accessibility (#9) source changes are integrated. Any remaining owner/device verification is tracked separately; no new release is authorized.
- **v2.5.0 — updater:** progress/retry presentation (#111) remains blocked until v2.4.0 release and explicit owner authorization. Core updater reliability (#4/#15) is being handled by Chaty first; #31 remains owner-authorization-gated.
- **v2.6.0 — settings/diagnostics:** #11 diagnostics tests are active with Vibe; #149 string resources and #146 session extraction are queued. #12/#14, #16, #10/#28/#6/#17 remain dependency-gated.
- Roadmap source: issue #18. Version targets are planning only, not release commitments. Addon Manager Phase A is integrated; Phase B remains owner-controlled.

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
- Historical hold superseded: owner reauthorized #23 on 2026-11-25/26. Chaty owns native Phase A in PR #152; no runtime injection or in-game addon activation is implemented or claimed.

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
## Code-review backlog additions — 2026-11-26

The beta code review identified structural follow-ups rather than a broad defect backlog. New issues #146–#150 were created and remain unassigned pending dependency and file-ownership checks:
- #146 GameSessionController and explicit GeckoSession keep-alive/close invariant (P1).
- #147 navigation/back-stack consolidation (P1; depends on #146).
- #148 centralized gameplay orientation/immersive state (P2; coordinate with #16).
- #149 launcher-owned string resources (P2).
- #150 verified documentation of the existing GeckoView/Android permission policy (P2; no runtime permission changes).

Updater findings were appended to existing #15 (installer URI dismissal lifecycle, actionable error mapping, safe cancellation cleanup) and #4 (R8/release smoke verification); no parallel updater issue was created. Existing #111 progress implementation remains separately owned by Vibe.

No code, CI, release, signing, dependency, ABI or device behavior changed in this documentation/triage checkpoint. No physical ARM64 device test is claimed.


## 2026-11-26 — GeckoView/Android permission policy documentation (#150)

- **Source inspected on beta `1279f214b3fdd3ba420e090a6a90914a8a3399e7`:** `app/src/main/AndroidManifest.xml`, `MainActivity.setupGeckoDelegates` and `GeckoRuntimeProvider`.
- **Documented source policy:** only persistent storage and audible/inaudible autoplay are allowed, and only for the exact `lasthavens.com` host and its subdomains. All other content permission types/origins are denied. Android runtime permission callbacks and media capture callbacks reject requests.
- **Manifest:** declares `INTERNET` and `REQUEST_INSTALL_PACKAGES` only. No camera, microphone, geolocation or notification permission is declared. Package-install permission supports the explicit Android installer flow.
- **Limitations:** this is a source audit, not physical-device verification of permission prompts or game features. No manifest, delegate, GeckoView setting or runtime behavior was changed.
- **Validation:** exact-head Markdown quality workflow passed on initial PR head `e2ce22c44508a87f53fc4a57a03f82e0597550bc` (run #45889911748); Android/device tests are not applicable to documentation-only changes.


## Agent PR integration workflow (2026-11-27)

- **Policy change recorded in PR #172:** after Vibe submits a PR targeting `beta`, Chaty takes over integration, changes the linked issue ownership to `agent:chaty`, and makes required corrections directly on the PR head branch. Exact-head Markdown validation passed; merge is pending.
- Routine GitHub formal reviews and return-to-Vibe correction cycles are no longer the default. Chaty still performs full risk-based inspection, exact-head CI verification, conflict resolution, documentation checks and post-merge verification.
- If Chaty cannot safely edit the branch (permissions, unclear ownership/scope, or a decision reserved for the owner), stop and document the specific blocker; do not bypass required checks or merge unverified code.
- This changes the agent handoff process only. It does not authorize release publication, tags, or `beta`-to-`main` promotion.

## 2026-11-27 — GameSessionController integrated (#146)

- PR [#173](https://github.com/iNeonyne/LastHavenPlus/pull/173) squash-merged to `beta` at `5ed6af635954c8f4e2673dab3c60334eff4a8ae0` (PR head `6160ede68fb7073680feef2e35f4176bd08a433d`; Vibe implementation commit `eb9f64b672eced9143bc5689106223c3e7af3d16`).
- Exact PR-head Android validation [#45921162358](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45921162358), independent unsigned APK reproducibility [#45921162325](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45921162325), and Markdown [#45921162310](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45921162310) passed.
- Post-merge beta checks on the merge commit: Markdown [#45923143716](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143716) passed; Android/signing/ARM64 packaging [#45923143717](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143717) and independent unsigned APK reproducibility [#45923143712](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143712) were still in progress at this documentation checkpoint. Do not treat them as passed until their exact run conclusions are verified.
- Scope: extracted GeckoSession lifecycle and load/crash state into `GameSessionController` and pure `GameSessionState` policies. Navigation and gameplay window-state follow-ups remain separate.
- Issue #146 closed as completed. No physical ARM64 device test was performed; no release, tag, publication or `main` promotion occurred.

## 2026-11-27 — Post-merge CI verified for #146

- Android beta-push run [#45923143717](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143717) passed on merge commit `5ed6af635954c8f4e2673dab3c60334eff4a8ae0`: signed optimized release APK build, unit tests, established signing-certificate verification and ARM64-only ABI checks; signed APK artifact upload succeeded. Release-tag/publication and differential-package generation were skipped in this non-release beta run.
- Independent unsigned APK reproducibility [#45923143712](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143712) passed: both clean builds and final APK hash/archive comparison succeeded.
- Post-merge Markdown [#45923143716](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923143716) passed on `5ed6af6`; after documentation reconciliation, Markdown [#45923558131](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45923558131) passed on `3856a4f`.
- These are CI/build/package checks, not physical-device evidence. No physical ARM64 device test, release, tag, publication or `main` promotion occurred.


## 2026-11-27 — PR #192 updater APK cleanup integrated

- PR [#192](https://github.com/iNeonyne/LastHavenPlus/pull/192) squash-merged to `beta` at `a2f943bf23e6b607ab3104c86798bec9130391a1` from exact PR head `b526efc59e4e7b06ef9ebc5b1bb368be44c2544c`. Issue #181 is completed and closed.
- Cleanup now scans a bounded number of direct updater-directory entries, deletes only generated finalized APK names and abandoned partial files, and never follows symlinks or descends into directories. Pending installer APKs, last handoff APKs and crash-recovery candidates remain protected. Installer handoff state is persisted before launch and rolled back if launch fails.
- Exact-head Android validation [#45948554037](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45948554037) passed lint, Spotless, JVM tests, unsigned release assembly and bundled MIT notice verification. Independent unsigned APK reproducibility [#45948554046](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45948554046) passed both clean builds and APK archive/hash comparison.
- This does not complete broader updater reliability issues #4/#15. No automatic installation, signing/ABI/toolchain changes, physical ARM64 update test, release, tag or `main` promotion occurred.


## 2026-11-27 — PR #190 GeckoView bridge session integration (source candidate)

- PR [#190](https://github.com/iNeonyne/LastHavenPlus/pull/190) wires the launcher-owned GeckoView 160 extension bridge to the exact live game GeckoSession before initial navigation and crash restoration. It reuses the process-scoped GeckoRuntime and app-private addon library; refreshes the bounded payload snapshot before explicit reload/restore; and tears down the bridge delegate, ports, pending requests and worker executor with the owning composition.
- This is source-level integration only. It does not implement XHR interception (#168), Worker/OPFS `blocks.idx` masking (#169), same-session Addon Manager navigation/page-world refresh (#170), or addon activation. No physical ARM64 test or in-game addon initialization is claimed.
- Exact PR-head Android validation [#45947192586](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45947192586) and independent unsigned APK reproducibility [#45947192520](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45947192520) passed on head `0a291ca0edd7ac7db03ffe126a02349cbd1b758a`. Documentation updates require fresh exact-head Markdown and Android/reproducibility checks before merge.
- Issue #167 remains open until physical ARM64 runtime acceptance and the remaining Addon Manager workstream gates are satisfied; merging this source integration does not imply runtime compatibility.


## 2026-11-27 — Chaty corrected #169 index masking contract

- PR #195 now reads the root OPFS `blocks.idx` through `navigator.storage.getDirectory()` and `getFileHandle("blocks.idx")`, checks the 2 MiB ceiling before reading, and sends validated spans in the Worker boot message before the original worker script is imported.
- The authoritative five-column loader format is respected: column 5 (`cols[4]`) is the addon path; digits in column 2 (`cols[1]`) are filled with byte 48 (`0`), and the first byte of column 5 is filled with byte 35 (`#`). Byte offsets are calculated from UTF-8 encoded bytes and only enabled addon folders are eligible.
- The Worker validates the complete span set atomically (path/folder eligibility, exact record shape, safe integer spans, index bounds and allowed fill bytes). Any malformed entry rejects the whole set; OPFS/index failures apply no mask and emit only a bounded stage diagnostic. Persistent OPFS bytes are never written.
- The generated bootstrap now includes parser/path-validator dependencies, and the WASM hook checks the actual `scope.wasmImports` object after `assignWasmImports()` runs.
- The exact Worker URL allowlist remains empty pending physical ARM64 observation. No physical device test or in-game addon/Lua initialization is claimed. PR #195 remains unmerged pending exact-head CI and Chaty's integration acceptance.

# Agent work assignments — LastHavenPlus

**Current beta baseline:** 4c508cf5eff07d8a64dc3ec772dcc1a37a1320a8 after the #133 icon integration and its documentation follow-ups; advanced to e0967121b51ac1c232c92edd80157fc02b714766 after PR #136 integrated the #107 visual system.  
**Stable baseline:** main / v2.3.0 tag ebc5b424e83ae66c2b0accc2bec6f050b8dc0d88.

## Roles and integration

- **Chaty:** orchestrator, issue owner/triage, reviewer, documentation coordinator and sole integrator into beta.
- **Vibe:** implementation contributor; starts only after explicit GitHub assignment and agent:vibe ownership.
- **Repository owner:** final product, priority and release authority.
- GitHub Issues, PRs, commits and Actions are the only agent-to-agent coordination channel.
- All task PRs target beta. No direct contributor pushes to beta or main. No promotion, tag or publication without explicit owner approval.

## Active Chaty work

### Issue #128 — post-v2.3.0 housekeeping
- **Owner:** Chaty (agent:chaty).
- **Branch:** chaty/post-v2.3-housekeeping, based on beta 7cd898f4c1f9b7f70787a7000e1ed9343a83540d.
- **Scope:** reconcile STATUS, ISSUES, CHANGELOG-AGENTS, AGENT_WORK and WORK_LOG; audit open PRs/issues/branches; preserve historical records and owner-paused work.
- **Exclusions:** no branch deletion, issue/PR closure, release changes or main changes. The owner subsequently requested a three-version roadmap, now recorded in issue #18.

### Issue #133 — v2.4.0 adaptive launcher icon
- **Owner:** merged; issue #133 is open with `agent:owner` solely for Android adaptive-mask/physical launcher visual signoff. No agent is implementing here.
- **Integration:** PR [#135](https://github.com/iNeonyne/LastHavenPlus/pull/135) squash-merged to `beta` at `c24b24e7125f49e86f465b1786ccfecb537e9694`; exact-head unsigned validation passed before merge.
- **Remaining verification:** Android-generated mask output and physical launcher appearance remain unverified; the owner signoff on issue #133 is the record for it.

### Issue #107 — v2.4.0 visual system (INTEGRATED)
- **Integration:** PR [#136](https://github.com/iNeonyne/LastHavenPlus/pull/136) merged into `beta` by Chaty at `e0967121b51ac1c232c92edd80157fc02b714766`; exact-head unsigned validation passed on final head `94b06eccf2670289474d0e3ad1a7a9c5e30f7c01`. Issue #107 closed on acceptance.
- **Device status:** physical ARM64 rendering/haptics remain unverified.

### Issue #9 — overlay accessibility and navigation (v2.4.0)
- **Owner:** Vibe (agent:vibe), assigned by Chaty on 2026-11-26 after #107 integration.
- **Scope:** Compose semantics (pane title, heading, traversal group) and accessible focus entry for the existing Back menu overlay; TalkBack traversal order verification; reduced-motion gating of the existing overlay fade; return-to-game behavior must keep discarding no session state. No menu redesign, no new actions.
- **Branch:** `vibe/overlay-accessibility`, fresh from beta `e0967121b51ac1c232c92edd80157fc02b714766`.
- **State:** implementation in progress on this branch.
- **Device status:** physical ARM64 TalkBack/gesture verification not performed; JVM-only test infra means focus/TalkBack behavior cannot be fully automated.

### Completed — issue #129 CI consolidation
- PR [#132](https://github.com/iNeonyne/LastHavenPlus/pull/132) merged at 7cd898f4c1f9b7f70787a7000e1ed9343a83540d.
- Exact PR validation [#45737717923](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717923), Markdown validation [#45737717837](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45737717837), and exact-merge beta run [#45738073790](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45738073790) passed.
- Beta lint/Spotless, signed ARM64 build, JVM/Python tests, established signer and ABI checks all passed. Observed runner time 94s versus 381s combined baseline; one-run comparison only.

### Issue #4 — updater regression coverage
- **Owner:** Chaty (agent:chaty).
- Keep updater implementation/test work under one owner; do not parallelize #4/#15/#31.
- Version-qualified Actions APK naming is integrated in PR #127. Remaining updater regression coverage and release-path evidence are still open; no physical updater installation claim.

### Issue #81 — compatible library refresh
- **Owner:** Chaty (agent:chaty).
- Approved scope is limited to the five libraries named in issue #81. No AGP, Kotlin, Gradle, JDK, SDK/Build Tools, GeckoView, signing or ABI changes.
- Do not merge without exact-head dependency resolution, lint, formatting, unsigned build and unit-test evidence.

### Issue #18 — v2.4.0–v2.6.0 roadmap
- Owner requested a three-version roadmap on 2026-11-26; the live issue now records v2.4.0 UI/UX, v2.5.0 updater experience and v2.6.0 settings/diagnostics.
- These are planning targets only, not release/tag/promotion authorization.
- v2.4.0 icon resources (#133, Chaty) and Compose visual-system work (#107, Vibe) proceeded in parallel with separate file scopes; #107 integrated via PR #136 and #9 accessibility is now the active Vibe assignment.

## Review queue and stale-base work

### Issue #66 / PR #119 — independent unsigned APK reproducibility
- PR is open on chaty/issue-66-independent-apk-rebuilds; issue ownership is agent:review.
- Prior independent builds produced byte-identical unsigned APKs (run #45727205562 on its recorded exact commit).
- The PR is based on an older beta commit and must be refreshed against current beta, then exact-head Android validation and reproducibility runs must pass before merge.
- No signing secrets, signed artifact reproducibility, F-Droid or physical-device claim.

### Issue #70 — documentation/security guidance
- Repository-wide markdownlint was integrated by PR #120; latest beta Markdown run #45736853811 passed.
- Final central STATUS/ISSUES/CHANGELOG/AGENT_WORK reconciliation is being handled under #128. Keep #70 open until that PR is reviewed and merged.

## Owner-paused work — do not resume

- **Issue #20 / PR #122:** GeckoView artifact/dependency audit. Owner explicitly deferred it; issue remains agent:blocked, PR draft/open for traceability. No further commits or workflow-dispatch requests.
- **Issue #62 / PR #123:** branch lifecycle audit. Owner explicitly deferred it; issue remains agent:blocked, PR draft/open for traceability. No further commits.
- **Issue #23:** Addon Manager remains on hold. No investigation, implementation or assignment without explicit owner approval.
- **Issue #65:** waiting for owner decision; do not claim emulator/device coverage.
- **Issue #15, #31, #111, #86 and #19:** blocked/deferred per current issue labels and owner priorities.

## Other preserved drafts

- **PR #113:** draft/open for traceability only. Do not merge or push further commits to its stale branch; its substance was superseded by PR #136 on `vibe/visual-system-v240` (merged).
- **PR #131:** closed unmerged because its base became stale; superseded by PR #132. No need to reopen.

## Ownership label rules

Every actionable issue must have exactly one agent:* label:
- agent:unassigned: awaiting assignment; not permission to start.
- agent:chaty: Chaty owns active work.
- agent:vibe: Vibe has explicit assignment and may start.
- agent:blocked: paused or waiting for a dependency/owner decision.
- agent:review: implementation PR submitted and awaiting Chaty's review.
- agent:owner: repository-owner decision required.

Only Chaty changes ownership labels. Assignment comments must define expected behavior, scope, exclusions, constraints, tests and documentation. Do not reassign an agent while they are actively working. After PR submission, move ownership to review; return to the implementation agent only for explicit corrections. Close issues only after acceptance criteria are verified.

## Repository safety constraints

- Kotlin + Jetpack Compose; Mozilla GeckoView 160; Android 10/API 29+; ARM64 only (arm64-v8a).
- Preserve application ID com.neonyne.lasthavenlauncher and established release signing identity.
- Game URL: https://lasthavens.com/play. Never modify the game.
- No automatic downloads or installation. No debug APK publication.
- Validate compatibility before any Gradle, SDK, Build Tools, GeckoView or signing changes.
- Never claim physical-device compatibility without real ARM64 hardware evidence.


### Issue #9 — overlay accessibility and navigation (integrated; device verification pending)
- **PR:** [#139](https://github.com/iNeonyne/LastHavenPlus/pull/139), squash-merged by Chaty to beta at `0d5ab712ce5195a3e71023c68da54f054645a8e2` from exact head `4b4d0586d2c80310d2b78e05271bc255dc4f90de`.
- **Pre-merge CI:** Android [#45829421214](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45829421214) and Markdown [#45829421210](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45829421210) passed.
- **Integrated:** pane title, heading/traversal semantics, Compose input-focus request, scrollable menu and live reduced-motion observer.
- **Remaining:** ARM64 TalkBack announcement/focus/traversal, GeckoView-relative ordering, reduced-motion behavior and large-font rendering require physical-device verification. No device test is claimed. Issue #9 remains `agent:owner` pending verification.


## Issue #67 — repository license and community hygiene

- **Owner decision:** repository owner selected Apache-2.0 on 2026-11-26.
- **Owner:** Chaty (agent:chaty) for implementation and review.
- **Branch:** chaty/community-hygiene-refresh, based on beta.
- **Scope:** canonical Apache-2.0 text, SECURITY.md, README licensing boundary, weekly Gradle Dependabot, v2.2/v2.3 changelog reconciliation, contribution guide alignment, and branch-retention policy. Keep third-party components and the LastHaven game under their respective terms.
- **Exclusions:** no app source, dependency/toolchain version, game, updater, signing, ABI, release, tag, main or branch deletion changes. Owner supplied project attribution: LastHavenPlus, Copyright 2026.
- **Verification:** documentation/Dependabot review and exact PR Markdown CI pending. Direct dependency license review is preliminary; transitive/package-level audit remains outstanding.

## Issue #67 — latest audit checkpoint (2026-11-26)

This entry supersedes the earlier Issue #67 status above.

- Owner selected Apache-2.0 for original LastHavenPlus code and documentation. Attribution is owner-supplied: LastHavenPlus, Copyright 2026.
- PR #144 adds Apache-2.0 and MPL-2.0 resources, a Settings viewer for project/third-party notices, THIRD_PARTY_NOTICES.md, SECURITY.md, Dependabot, changelog and contributor-policy updates.
- The resolved GeckoView 160 runtime graph includes Google Play Services FIDO/Base/Basement/Tasks under Google's Android Software Development Kit License terms. These are not OSI open-source licenses and are disclosed separately.
- PR CI now uploads the unsigned release APK with the runtime dependency report. SettingsDialogs.kt Spotless formatting was corrected after the first exact-head Android run failed.
- Remaining: exact-head Android pass, inspect the unsigned APK and packaged runtime components, finish artifact-level notice verification, then final review. PR remains draft; no merge or release authorization is implied.
- No game, updater, app ID, signing identity, ABI, dependency/toolchain version, GeckoView version, release/tag/main or branch-deletion changes. No physical-device test claimed.

# Agent work assignments — LastHavenPlus

The repository owner makes final product and release decisions. Chaty is the main orchestrator and currently owns all active implementation work. Ploy has no active assignment until Chaty explicitly assigns a separate issue.

## Roles

- **Chaty:** owns intake, issue triage, task assignment, central documentation, review, CI/release coordination and integration. Chaty alone reviews and merges accepted PRs into `beta`.
- **Ploy:** may implement only a separately assigned issue. There is currently no active Ploy assignment. Ploy must not continue PR #25 or work on issue #22 unless Chaty explicitly reassigns it.

## Shared workflow

- `beta` is the only active development, integration and testing branch. `main` is stable.
- Pull requests are allowed and are the standard handoff/integration path. Contributors open PRs targeting `beta` for completed assignments.
- Parallel work is allowed only for different issues with non-overlapping file scopes. Never work on the same issue concurrently.
- Any contributor works from a dedicated task branch based on current `beta`, then opens a PR targeting `beta` and hands Chaty the PR link, exact head commit SHA, changed files, test results and limitations.
- Chaty alone reviews and merges accepted PRs into `beta`; contributors must not merge their own PRs or push directly to `beta`.
- PR CI for `beta` builds the unsigned release variant and runs unit tests; it does not use signing secrets or publish APKs. Chaty must still verify the full signed build and release gates on the resulting beta commit.
- Promote to `main` or publish a release only after release gates pass and the owner explicitly approves.
- GitHub Issues and `ISSUES.md` are the source of truth. TickTick may be used as a supplementary task tracker.
- Physical-device behavior remains unverified until tested on real ARM64 hardware.

## Project constraints

- Keep the application ID `com.neonyne.lasthavenlauncher`, established release signing identity and ARM64-only target unchanged.
- Do not modify the game/client.
- Do not add automatic update downloads or installation.
- Do not ship debug APKs or partially implemented features.
- Do not change Gradle, AGP, JDK, Android SDK or GeckoView versions without evidence and compatibility validation.
- Do not restore or work on the removed addon manager. Issue #23 remains on hold pending a new owner specification.

## Chaty — ACTIVE: updater hardening follow-up (#4 and #15)

**Candidate branch:** `chaty/fix-updater-hardening`  
**Starting beta HEAD:** `b7d9f3256527de41a45ff8c935fac510be635a69`  
**Current candidate HEAD:** `df82f40feca9eed7af318403e93a284ec692edad`

### Issue #4 — updater correctness and tests
- Follow redirects manually; validate HTTPS and exact trusted host at every hop; reject userinfo, unexpected ports, fragments, untrusted hosts and redirect chains beyond five hops.
- Bound release API and update manifest response bodies.
- Bound queued updater work, return a user-visible busy failure when saturated, and let the worker retire after idle timeout.
- Verify streamed download size and SHA-256; use unique temporary and final filenames so a failed replacement cannot delete a previously staged verified APK.
- Ensure diagnostics callback failure cannot suppress full APK fallback.
- Add JVM tests for URL/redirect validation, bounded response bodies, download size/digest checks and fallback resilience.
- Keep manual user confirmation; no automatic download or installation.

### Issue #15 — updater recovery
- Persist the pending installer URI synchronously before opening Android's unknown-app installation settings.
- Restore the pending URI from app preferences after Activity/process recreation.
- Clear the persisted request only after the installer handoff starts successfully.
- Preserve the existing package identity and release signing checks; never expose a partial APK to the installer.

### Verification and handoff
- PR [#30](https://github.com/iNeonyne/LastHavenPlus/pull/30) targets beta; the latest unsigned PR build/unit-test validation is pending. Inspect the final run before accepting the changes.
- Physical same-key installation, permission handoff after force-stop/process death, delta update and full APK fallback remain hardware tests under #5.
- PR must target `beta`; Chaty alone reviews and merges it.
- PR #26 already integrated #22 home/navigation and earlier P0 hardening. Physical navigation remains unverified.
- Draft promotion PR #27 remains unmerged; do not merge it before release gates and owner approval.

## Chaty — integration and release coordination

- Maintain exclusive ownership of central documentation, `STATUS.md`, `ISSUES.md`, `README.md`, `AGENTS.md`, this file, `CHANGELOG-AGENTS.md` and `WORK_LOG.md`.
- Verify the CI optimization run associated with commit `54b947279a783c6c382210e65045edd02bd5ee73`; do not claim performance improvement until elapsed time is compared with a suitable baseline.
- Keep the successful run #308 evidence tied to its actual tested commit `82590635e77c7abfedcaa6fd1fa4ad8c3b5de469`; do not describe it as a build of later documentation/workflow commits.
- #22/#4 implementation merged in PR #26; coordinate signed post-merge CI, remaining GeckoView physical-device checks and beta-to-main reconciliation.
- Do not promote `beta` to `main`, create a release tag or publish a release without explicit owner approval.

## Paused and deferred work

- Issue #9 accessibility follow-up: paused until the home/navigation implementation is integrated and can be checked against the final UI.
- Issue #11 diagnostics test expansion: paused; resume after the post-merge verification checkpoint or when Chaty explicitly reorders it.
- Ploy: no active assignment. Do not work on #22 or PR #25.
- Issue #23 addon manager: blocked by the owner's hold. Do not begin until a new written specification is supplied.

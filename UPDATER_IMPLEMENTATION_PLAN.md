# Main-branch CI updater — audit and implementation plan

**Status:** repository audit completed; updater runtime code is not implemented yet. All development remains on `beta`. The `main` branch has not been changed.

## Audit snapshot (2026-11-23)

- `main` HEAD: `ce762bc02b9683037fb8c6b018d81f61b4996be3`.
- Latest successful main build found: run **#113**, run ID `45090811521`, commit `ce762bc02b9683037fb8c6b018d81f61b4996be3`.
- Main run #113 uploaded `LastHavenPlus-Demo-APKs` (541,071,485 bytes) and `LastHavenPlus-Demo-ARM64` (96,469,317 bytes). Both were unexpired when checked and had an expiry date of 2027-02-21 UTC.
- `beta` HEAD: `27ddce98c96af94e31bfa3392c783d3dcb69a447`.
- Latest successful beta build found: run **#138**, run ID `45097163612`. It also uploaded the two demo artifacts. These beta artifacts must never be offered by the updater.
- Current app metadata on both branches is `com.lasthavens.android`, version name `2.0.0`, version code `2`.
- The current Home screen has Play and Addon Manager entry points only. There is no updater implementation, installer permission, FileProvider, or native updater test suite yet.
- The Android manifest has `allowBackup=false`. The Gecko runtime/profile is owned by the app and is shared by game and manager. App data must be preserved by an in-place package update; uninstalling or clearing app data is not an acceptable update strategy.
- Current main run #113 APK signer SHA-256: `CEA5FFA95F995F121C47BEA059E2A5DC542B91C267E4EF85EF7DC086305CEE77`.
- Current beta run #138 APK signer SHA-256: `64C3F237A95EBC9DAA043213743D2D671538D5FB5C936D2EECB42BC60B9C482E`.
- These signer fingerprints differ. The workflow currently falls back to the runner's Android debug keystore, so builds from different runs/branches are not update-compatible.
- The ARM64-only artifact contains the APK but is uploaded without `SHA256SUMS.txt`. The all-ABI artifact includes checksums, but is over 541 MB; it is too wasteful for a phone updater.

## Non-negotiable safety rules

1. Only completed, successful CI runs built from the repository's exact `main` branch are candidates. Exclude `beta`, pull requests, tags, forks, and all GitHub Releases.
2. Never put a GitHub PAT, OAuth token, signing key, or password inside the APK or source repository.
3. Never uninstall the existing app, clear app data, reset GeckoView, or alter add-on/profile files to force an update.
4. Do not pass an APK to Android's installer until its archive, checksum, package ID, version code, ABI, and signing identity have passed validation.
5. A signing mismatch is a hard stop. Keep the existing installation and all its data untouched; explain the incompatibility.
6. Android installation must remain user-confirmed. No silent installation or privileged install workaround.
7. Failed, cancelled, expired, or interrupted operations must leave the installed app and its profile untouched.

## Required signing decision — no permanent release key required

Android requires APKs to be signed. It is possible to postpone the permanent public-release key, but **it is not possible to install an unsigned APK** or update an installation signed by a different certificate.

The safe demo approach is a separate, persistent **demo/update keystore** stored only as GitHub Actions secrets and used for both beta demo builds and main updater builds. It is not the permanent release key, but it is still a stable signing identity. The keystore and passwords must not be committed to the repository or included in the APK.

If no persistent demo key is configured, the app may check for builds and download/verify an APK, but it must refuse to start an incompatible in-place installation. Never rotate the demo key after users have installed an APK signed with it.

**Existing-install transition risk:** current main and beta CI APKs are signed by different generated debug keys. An app already installed from either current CI artifact cannot be updated in place by an APK signed with a new persistent key. Android will reject it. Uninstalling to change keys can erase the Gecko profile, game login/session, add-ons, and settings. Before switching keys, determine whether a data-bearing install exists; do not ask the user to uninstall it. If one exists, the updater must remain download-only until a separately tested data migration is possible.

## Required CI changes (develop on beta; only take effect on main after owner-approved promotion)

- Use one repository-level demo-signing secret set for both branches; fail the updater-compatible artifact build if the secrets are missing. Do not silently fall back to the runner-generated debug key.
- Keep the permanent release-key workflow separate and paused.
- Give main CI APKs a strictly increasing `versionCode` greater than normal beta demo builds. Use a deterministic formula derived from the app's base version code and GitHub run number, with an explicit Android maximum-version-code guard. Do not rely on the current fixed value `2`.
- Keep beta builds clearly labelled as demos and never include them in the updater feed.
- Create a small, exact-name ARM64 updater artifact containing only:
  - the ARM64 APK;
  - a `SHA256SUMS.txt` file for that APK;
  - a small `build-info.json` containing package ID, app version name/code, ABI, branch, commit SHA, run ID/number, and workflow path.
- Upload this as one dedicated artifact, e.g. `LastHavenPlus-Main-Updater-ARM64`, only for a main-branch successful build. Keep the existing multi-ABI demo artifacts for manual testing.
- Keep and validate the artifact's GitHub-provided SHA-256 digest as an additional check over the downloaded artifact ZIP.
- Set an intentional artifact retention value and always read `expired`/`expires_at`; never assume an artifact remains available forever.
- Build workflow triggers must continue to produce main CI artifacts after an owner-approved promotion. Do not change the current branch-promotion rule.

## Download access — do not embed a token

The public repository's workflow-run and artifact metadata can be queried as public resources. GitHub's documented artifact archive-download endpoint requires an authenticated request; its temporary redirect URL expires after one minute. The Android app must not contain a PAT.

A candidate is **nightly.link**, which provides anonymous end-user downloads for public GitHub Actions artifacts through its read-only GitHub App. This adds a third-party service dependency and requires the owner to install/approve that app for the repository. The other option is an owner-controlled public static download location populated by the main CI job. Choose and test one route before implementing the downloader. Do not silently introduce a third-party service.

## App updater architecture

1. Add an isolated `UpdateManager`; keep network I/O and file hashing off the main thread.
2. Query only the canonical build workflow's completed successful runs on `main`. Require `head_branch == "main"`, the expected repository, the expected workflow, and an allowed event (`push` or an intentional main-branch `workflow_dispatch`). Exclude pull requests, tags, forks, and beta.
3. Read artifacts belonging to that exact run. Require the dedicated ARM64 artifact name, matching run ID/commit, unexpired state, non-empty SHA-256 digest, and an available download route.
4. Cache metadata/check results for a limited period and respect GitHub API rate limits. Show the current app version, candidate version/build, commit, run date, ABI, file size, and artifact expiry.
5. Download to a private app-internal `filesDir/updates` temporary `.part` file. Stream bytes with progress/cancellation, enforce the announced and actual byte limits, and never overwrite an already verified APK.
6. Verify the downloaded artifact ZIP against GitHub's artifact SHA-256 digest. Extract only the exact expected filenames; reject path traversal, duplicate/unexpected entries, oversized entries, and malformed ZIPs.
7. Verify the inner `SHA256SUMS.txt` against the APK. Validate package ID `com.lasthavens.android`, expected ARM64 ABI, a strictly greater version code, and a readable Android package signature.
8. Compare the candidate APK signer with the installed app's signer before launching the installer. Support the app's minimum Android API 26 signing-info APIs; fail closed if comparison cannot be performed.
9. Use a narrowly scoped AndroidX FileProvider path for the internal APK and launch the system package installer with read permission. Declare `REQUEST_INSTALL_PACKAGES`; check `canRequestPackageInstalls()` and guide the user to the per-app unknown-source setting when required.
10. Require the user to confirm installation. Handle installer cancellation, failure, and app process restart. Never claim installation succeeded merely because the installer was launched.
11. Keep the existing installed APK/data untouched until Android completes a same-package, same-signer in-place update. Clean up partial downloads safely; remove temporary verified APKs only after the install outcome is known.

## Test and release gate

Add native unit tests for run selection and branch filtering, artifact selection/expiry, version-code comparison, checksum parsing, ZIP entry/path/size validation, package ID checks, signer equality/mismatch, and interrupted-download cleanup. Add workflow validation for the dedicated artifact's names and contents.

Before enabling the updater on a device with real data, test on a disposable device/profile with:
- same-signer beta-to-main in-place update;
- app process restart and app data/profile persistence;
- existing game login/session and pre-installed add-ons still present;
- no update available, no successful main run, pull-request-only run, missing/expired artifact;
- anonymous download route and its redirect behavior;
- API 403/404/410/429 and network loss;
- partial download, disk full, ZIP corruption, artifact digest mismatch, APK checksum mismatch;
- wrong ABI, wrong package ID, lower/equal version code, and different signer;
- unknown-source permission disabled, installer cancellation, and failed install.

Do not mark the updater complete until an actual physical-device same-signer update has been proven to preserve the game profile, login/session, add-ons, and settings. CI success alone is not enough.

# Instructions for AI agents and contributors

These are mandatory project rules. Read this file and `TODO.md` before doing work. Read `README.md`, `CHANGELOG.md`, `PROJECT_OVERVIEW.md`, relevant source files, and current CI results as needed. The checked-in implementation and current CI configuration take precedence over stale notes.

## 1. Branch and promotion policy — mandatory

- Do all work on the `beta` branch. This includes source code, tests, workflows, documentation, version changes, and fixes.
- Do not commit, push, merge, cherry-pick, rebase onto, or otherwise move work to `main` unless the project owner explicitly instructs you to promote it.
- Keep `main` unchanged during normal development.
- Requests to continue, fix, review, tidy, or improve the project authorize work on `beta` only. They do not authorize promotion to `main`.
- Before editing, verify the checked-out/target branch is `beta`. Before pushing, verify the destination is still `beta`.
- CI demo builds should run from `beta`. Do not change the branch policy without explicit owner approval.

## 2. Product scope

LastHavenPlus is a focused Android client for the Lasthavens game, built with Kotlin and Mozilla GeckoView. It is not a general-purpose browser. Keep work focused on reliable gameplay, add-on management, Android integration, security, and data preservation.

## 3. Addon Manager changes and data preservation

- The project owner has authorized necessary Addon Manager maintenance, provided installed add-ons carry over.
- Preserve add-on files, folder IDs, versions, enabled/disabled state, library metadata, manager settings, and the shared Gecko profile.
- Never reset, wipe, or silently discard existing add-on data.
- Before changing storage or backup formats, inspect existing records and maintain backward-compatible reads or provide a tested migration and rollback path.
- Prefer staged writes and pointer swaps so a failed or interrupted update leaves the last committed add-on files intact.
- Clean up orphaned data only after confirming it is not referenced by the active library.
- Never silently change the manager version or stored data format.

## 4. Architecture and user data

- Keep GeckoView unless a concrete technical reason and migration plan justify changing engines.
- The game and Addon Manager are intended to share the Gecko runtime/profile. Preserve this relationship.
- Protect login/session state, cookies, profile data, installed add-ons, manager settings, and other user data.
- Before changing profile, storage, backup/restore, add-on format, or migration behavior: inspect the current implementation; document what is preserved and excluded; and define a tested backup, migration, and rollback path. The owner has authorized necessary manager maintenance only when existing add-ons carry over.
- Never recommend uninstalling an existing differently signed app without clearly warning that local data may be lost.
- Do not claim backups preserve settings or enabled state unless the implementation demonstrably includes them.

## 5. Security and privacy

- Treat all remote game pages, downloaded files, add-ons, URLs, and imported archives as untrusted.
- Keep URL/navigation allow-lists strict. Do not broaden trusted origins or allow dangerous schemes for convenience.
- Privileged Android functionality must not be exposed to untrusted web content.
- Keep permissions minimal and fail closed when permission checks fail or are unavailable.
- Validate archive paths, file sizes, content, and checksums. Consider decompression bombs, path traversal, oversized downloads, malformed metadata, and partial writes.
- Do not add analytics, telemetry, tracking, or unnecessary data collection.
- Do not put passwords, keystore files, signing material, tokens, or private user data in source control, logs, issues, or chat.

## 6. Build and release

- GitHub Actions is the build and validation path. GitHub Releases are the intended public distribution channel; there is no Google Play release track.
- Normal `beta` builds may produce debug-signed demo APKs for testing. They are not release-ready.
- Never publish a debug-signed APK as a public release.
- Public releases require the permanent release key, verified certificate consistency, secure offline key backups, and a tested same-certificate in-place upgrade.
- Do not experiment with, rotate, or replace the release key casually. A certificate mismatch can prevent upgrades and put user data at risk.
- Do not claim a build, test, artifact, device check, or release succeeded unless its actual result was checked.
- CI success proves only the checks that ran. It does not prove physical-device compatibility, performance, thermals, or gameplay stability.

## 7. Versioning and automatic-update compatibility

- The Android app uses Semantic Versioning for its user-facing `versionName`: `MAJOR.MINOR.PATCH`.
  - **MAJOR:** an incompatible user-facing change or a breaking app/data contract. Any data-format migration still requires a documented, tested backup, migration, and rollback path.
  - **MINOR:** a backward-compatible user-facing feature or substantial capability.
  - **PATCH:** backward-compatible bug fixes, security fixes, and maintenance.
- Do not increase `versionName` for every commit or CI run. Keep it unchanged while producing successive development builds of the same release line. Do not add a leading `v` to `versionName`; Git release tags use `v<versionName>`.
- **Promotion baseline rule:** before any owner-approved promotion from `beta` to `main`, choose and verify the clean stable SemVer release number for the exact promoted commit (for example `2.1.0`, never a CI/run number or a beta suffix). Set the Android `versionName` and matching `v<versionName>` release tag to that stable number as part of the promotion. Never promote a beta/prerelease version string as the main release version.
- **Restart beta from main:** immediately after a promotion, treat that exact main release version as the new beta baseline. The next beta development version must start from that baseline and advance according to SemVer for the next intended change; use a prerelease suffix such as `2.1.1-beta.1` while validating a patch, or `2.2.0-beta.1` while validating a minor release. Increment the beta prerelease counter for subsequent beta builds of that same candidate. When the candidate is approved for main, remove the prerelease suffix and promote the clean stable version (for example `2.2.0`). Never let beta overwrite or move the main release version backwards.
- Keep Android `versionCode` independent of this human-readable release sequence. Its CI counter must remain strictly increasing across both branches; promotion must not reset it, and beta builds must not consume a lower code than a main build already distributed.
- Until version metadata is deliberately centralized, `app/build.gradle.kts` is the authoritative source for the Android app's `versionName`. Any build validation, UI, release, or updater metadata must derive from that value rather than maintaining a second manually edited copy.
- Android `versionCode` is a separate positive integer used for install/update ordering. An updater must compare numeric `versionCode` values, never compare version-name strings, filenames, dates, or commit hashes.
- **When main-branch CI updates are implemented, set the CI APK's `versionCode` from the monotonically increasing GitHub Actions workflow run number (`GITHUB_RUN_NUMBER`) of the existing `.github/workflows/build.yml` workflow.** This number increases across runs on the workflow's branches and tags; a rerun of the same workflow run retains its number and must not be treated as a newer build. Keep the workflow identity stable. If the workflow is renamed/recreated or numbering changes, migrate the counter and prove that the next code is greater than every previously distributed code before publishing another update.
- The first CI-generated code must be greater than the currently distributed/installable app code. On `beta`, `app/build.gradle.kts` now derives `versionCode` from `GITHUB_RUN_NUMBER`; local builds use a development-only fallback and must never be advertised as update candidates. The untouched `main` branch must receive the approved workflow promotion before this policy becomes active there.
- A new main CI build may have the same `versionName` as the previous build but must have a greater `versionCode`. Do not bump the SemVer version just to make an APK install over another APK.
- **Automatic update source is `main` only.** The beta implementation considers only successful completed `push` runs whose source branch is exactly `main`, from the existing workflow ID/path. It ignores beta, pull-request, tag/release, manual-dispatch, failed, cancelled, and in-progress runs. The beta workflow configures a dedicated main update artifact; `main` remains unchanged until owner-approved promotion, so the live main update feed is not yet available.
- Main update artifacts include a machine-readable `update.json` with app `versionName`, resolved `versionCode`, branch/ref, commit SHA, workflow run ID/number, workflow ID, APK filenames/ABIs, sizes and SHA-256 hashes. A tested Python generator rejects non-main contexts, invalid workflow/run metadata, prerelease versions, missing ABIs and oversized/missing APKs. The updater validates metadata, requires `main`, selects a device ABI, verifies the checksum, and checks the APK package ID, version metadata and installed signing certificate before offering installation.
- **Signing is an independent update requirement.** Every main artifact offered to an installed app must use the same stable signing certificate as that installed app. A dedicated stable CI key may be used for a testing channel; it does not have to be the eventual public-release key, but it must be protected and its certificate fingerprint must remain identical across builds. The current fallback to the runner's debug signing configuration is not an approved continuity guarantee. Verify the APK signer fingerprint and perform a same-certificate in-place upgrade test before enabling updates.
- Never work around a package ID, signing-certificate, or version mismatch by uninstalling the existing app or clearing its data. Stop safely and explain the blocker. Preserve the application ID `com.lasthavens.android` for normal upgrades.
- The Addon Manager version remains independent and is read from its bundled `manifest.json`; the GeckoView dependency version is also independent. Do not bump either one just because the Android app version changes, and do not change the bundled Addon Manager without the owner's authorization and the required data-preservation checks.

## 8. Development workflow

1. Verify branch `beta`, inspect the latest commits, workflow runs, and relevant source.
2. Read the related items in `TODO.md`; reconcile stale tasks with the actual implementation.
3. Identify data, security, compatibility, and upgrade risks before changing behavior.
4. Make the smallest complete and reviewable change.
5. Add or update focused tests for behavior changes where practical.
6. Run the narrowest relevant tests, then the full CI build when feasible.
7. Inspect the final diff and confirm no protected files or unrelated changes were included.
8. Update `TODO.md`, `CHANGELOG.md`, `README.md`, or `PROJECT_OVERVIEW.md` only when their information actually changes.
9. Report the branch, commit, changed files, tests and build results, known risks, and work still unverified.

## 9. Documentation responsibilities

- `README.md` is for human readers. Keep it concise and avoid duplicating the roadmap or agent rules.
- `AGENTS.md` is the authoritative instruction file for agents and contributors.
- `TODO.md` is the authoritative AI-actionable development roadmap. Keep it limited to implementation, automated tests, CI, code review, technical investigation, and documentation an agent can perform.
- `MANUAL_CHECKLIST.md` tracks physical-device verification, owner-only actions, private-key handling, legal/permission decisions, and release sign-off. Do not mark these complete without direct evidence from the owner or an actual device/test result.
- `CHANGELOG.md` records actual user-visible changes by version. Do not use it as a task list or claim unreleased work is shipped.
- `PROJECT_OVERVIEW.md` contains the technical architecture and current verified project state.
- Do not let documents contradict source code, version metadata, CI configuration, or one another.

## 10. Service terms and project identity

The Lasthavens Terms of Use restrict modified clients except for add-ons in the in-game AddOn Library. Compatibility of this independent GeckoView client and bundled manager is unconfirmed. Obtain explicit guidance from the Lasthavens team before public distribution. Clearly identify the app as unofficial and not affiliated with or endorsed by Lasthavens or Blizzard Entertainment.

## 11. Scope control

Do not add unrelated browser features, analytics, push notifications, background services, or differential APK updates without a demonstrated requirement and project-owner approval where appropriate. Prefer reliability and safe data handling over feature count.

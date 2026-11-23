# Instructions for AI agents and contributors

These are mandatory project rules. Read this file and `TODO.md` before doing work. Read `README.md`, `CHANGELOG.md`, `PROJECT_OVERVIEW.md`, relevant source files, and current CI results as needed. The checked-in implementation and current CI configuration take precedence over stale notes.

## 1. Branch and promotion policy — mandatory

- `main` is the sole active development, integration, CI, and release branch. All source code, tests, workflows, documentation, version changes, and fixes belong on `main`.
- The former `beta` branch has been consolidated into `main` and is retired. Do not recreate it or target it unless the owner explicitly changes this policy.
- Requests to continue, fix, review, tidy, or improve the project authorize work on `main`.
- Before editing or committing, verify the target branch is `main`; before pushing, verify the destination is still `main`.
- Do not create long-lived development branches unless a task genuinely requires isolation; if one is needed, use a short-lived branch and merge it back to `main` after checks pass.

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
- Main-branch CI builds are update candidates only when all main-only signing, artifact provenance, checksum, package, version, and signer checks pass.
- Never publish a debug-signed APK as a public release or main update candidate.
- Public releases require the permanent release key, verified certificate consistency, secure offline key backups, and a tested same-certificate in-place upgrade.
- Do not experiment with, rotate, or replace the release key casually. A certificate mismatch can prevent upgrades and put user data at risk.
- Do not claim a build, test, artifact, device check, or release succeeded unless its actual result was checked.
- CI success proves only the checks that ran. It does not prove physical-device compatibility, performance, thermals, or gameplay stability.

## 7. Versioning and automatic-update compatibility

- Use Semantic Versioning for the Android app's user-facing `versionName`: `MAJOR.MINOR.PATCH`.
  - **MAJOR:** incompatible user-facing change or breaking app/data contract. Any data-format migration still requires documented, tested backup, migration, and rollback.
  - **MINOR:** backward-compatible user-facing feature or substantial capability.
  - **PATCH:** backward-compatible bug fixes, security fixes, and maintenance.
- Keep `versionName` stable across ordinary commits and CI runs. Do not encode build numbers, dates, commit hashes, or branch names in it. Do not add a leading `v`; Git release tags use `v<versionName>`.
- `app/build.gradle.kts` is the authoritative source for Android `versionName`; build validation, UI, release metadata, and updater metadata must derive from it.
- **Release-number decision:** the first public Android app release must be `1.0.0` (tag `v1.0.0`). Keep it stable until that release is approved and published. After `1.0.0`, use `1.0.1` for the next patch-level release, `1.1.0` for the next backward-compatible feature release, or `2.0.0` only for a genuinely breaking release. Do not bump merely because CI ran.
- Android `versionCode` is a separate positive integer used for install/update ordering. The existing `.github/workflows/build.yml` workflow's `GITHUB_RUN_NUMBER` is the monotonic source. It must remain strictly increasing across main pushes and release tags. Keep the workflow identity stable; if it is ever recreated or numbering changes, migrate the counter and prove the next code exceeds every distributed code before publishing.
- A rerun of an existing workflow run retains its run number and is not a newer build. The updater must compare numeric `versionCode`, never version-name strings, filenames, dates, or commit hashes.
- Local builds use a development-only fallback code and must never be advertised as update candidates. The first CI-generated code must exceed every previously installable/distributed code.
- The single active branch is `main`. The updater may select only successful, completed `push` runs whose source branch is exactly `main`, from the existing workflow ID/path. Ignore pull requests, tags/releases, manual dispatches, failed, cancelled, in-progress, expired, or incompatible runs.
- Release distribution is ARM64-only (`arm64-v8a`); do not build, publish, or offer armeabi-v7a, x86_64, or universal APKs. Each successful main run publishes one metadata/checksum artifact (`update.json` and `SHA256SUMS.txt`) plus exactly one ARM64 APK artifact named `LastHavenPlus-Main-Update-arm64-v8a`. The metadata includes app `versionName`, resolved `versionCode`, branch/ref, commit SHA, workflow run ID/number, workflow ID, APK filename/ABI, size, and SHA-256 hash. The updater validates both artifact types against the same successful main run, offers the ARM64 APK only to devices reporting `arm64-v8a`, verifies checksum and APK package/version, and checks installed signing-certificate compatibility before offering Android's user-confirmed installer. Other architectures must receive a clear unsupported-device result, never a universal fallback.
- Every main APK offered to an installed app must use the same permanent release certificate as that installed app. Main and tagged builds fail closed if the permanent key is missing or its fingerprint differs from the expected SHA-256 certificate. Main run #196 verified that the permanent release keystore matches the expected SHA-256 fingerprint using DER certificate hashing (`keytool -exportcert` piped to `sha256sum`). Keep this direct-byte check; never change the expected fingerprint to accommodate a different key. Verify signer fingerprint and complete a same-certificate in-place upgrade test before declaring updates production-ready.
- Never work around package ID, signing-certificate, or version mismatch by uninstalling the existing app or clearing data. Preserve application ID `com.lasthavens.android`.
- Addon Manager version (from bundled `manifest.json`) and GeckoView dependency version are independent. Do not bump either just because the Android app version changes, and do not change Addon Manager without owner authorization and required data-preservation checks.

## 8. Development workflow

1. Verify branch `main`, inspect the latest commits, workflow runs, and relevant source.
2. Read the related items in `TODO.md`; reconcile stale tasks with the actual implementation.
3. Identify data, security, compatibility, and upgrade risks before changing behavior.
4. Make the smallest complete and reviewable change.
5. Add or update focused tests for behavior changes where practical.
6. Run the narrowest relevant tests, then the full CI build when feasible.
7. Inspect the final diff and confirm no protected files or unrelated changes were included.
8. Update `TODO.md`, `CHANGELOG.md`, `README.md`, or `PROJECT_OVERVIEW.md` only when their information actually changes.
9. Report the branch, commit, changed files, tests and build results, known risks, and work still unverified.

## 9. Tools, plugins, and agent autonomy

- Agents have the owner's authorization to use any available ChatGPT plugins, connectors, integrations, and tools that materially help complete project work. Choose the appropriate tool without asking for separate permission each time.
- Respect each tool's permissions, security boundaries, rate limits, and data-handling rules. Never expose repository secrets, signing material, credentials, or private user data to a plugin or external service unless the owner has explicitly authorized that exact transfer and it is necessary.
- Installing or connecting a plugin may still require an explicit user action in ChatGPT. If a required plugin is unavailable or not connected, use another available tool where practical and explain any remaining limitation.

## 10. Documentation responsibilities

- `README.md` is for human readers. Keep it concise and avoid duplicating the roadmap or agent rules.
- `AGENTS.md` is the authoritative instruction file for agents and contributors.
- `TODO.md` is the authoritative AI-actionable development roadmap. Keep it limited to implementation, automated tests, CI, code review, technical investigation, and documentation an agent can perform.
- `MANUAL_CHECKLIST.md` tracks physical-device verification, owner-only actions, private-key handling, legal/permission decisions, and release sign-off. Do not mark these complete without direct evidence from the owner or an actual device/test result.
- `CHANGELOG.md` records actual user-visible changes by version. Do not use it as a task list or claim unreleased work is shipped.
- `PROJECT_OVERVIEW.md` contains the technical architecture and current verified project state.
- Do not let documents contradict source code, version metadata, CI configuration, or one another.

## 11. Service terms and project identity

The Lasthavens Terms of Use restrict modified clients except for add-ons in the in-game AddOn Library. Compatibility of this independent GeckoView client and bundled manager is unconfirmed. Obtain explicit guidance from the Lasthavens team before public distribution. Clearly identify the app as unofficial and not affiliated with or endorsed by Lasthavens or Blizzard Entertainment.

## 12. Scope control

Do not add unrelated browser features, analytics, push notifications, background services, or differential APK updates without a demonstrated requirement and project-owner approval where appropriate. Prefer reliability and safe data handling over feature count.

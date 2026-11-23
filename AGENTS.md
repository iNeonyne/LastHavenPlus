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

## 7. Development workflow

1. Verify branch `beta`, inspect the latest commits, workflow runs, and relevant source.
2. Read the related items in `TODO.md`; reconcile stale tasks with the actual implementation.
3. Identify data, security, compatibility, and upgrade risks before changing behavior.
4. Make the smallest complete and reviewable change.
5. Add or update focused tests for behavior changes where practical.
6. Run the narrowest relevant tests, then the full CI build when feasible.
7. Inspect the final diff and confirm no protected files or unrelated changes were included.
8. Update `TODO.md`, `CHANGELOG.md`, `README.md`, or `PROJECT_OVERVIEW.md` only when their information actually changes.
9. Report the branch, commit, changed files, tests and build results, known risks, and work still unverified.

## 8. Documentation responsibilities

- `README.md` is for human readers. Keep it concise and avoid duplicating the roadmap or agent rules.
- `AGENTS.md` is the authoritative instruction file for agents and contributors.
- `TODO.md` is the authoritative active roadmap and task tracker. Keep priorities and completion status current.
- `CHANGELOG.md` records actual user-visible changes by version. Do not use it as a task list or claim unreleased work is shipped.
- `PROJECT_OVERVIEW.md` contains the technical architecture and current verified project state.
- Do not let documents contradict source code, version metadata, CI configuration, or one another.

## 9. Service terms and project identity

The Lasthavens Terms of Use restrict modified clients except for add-ons in the in-game AddOn Library. Compatibility of this independent GeckoView client and bundled manager is unconfirmed. Obtain explicit guidance from the Lasthavens team before public distribution. Clearly identify the app as unofficial and not affiliated with or endorsed by Lasthavens or Blizzard Entertainment.

## 10. Scope control

Do not add unrelated browser features, analytics, push notifications, background services, or differential APK updates without a demonstrated requirement and project-owner approval where appropriate. Prefer reliability and safe data handling over feature count.

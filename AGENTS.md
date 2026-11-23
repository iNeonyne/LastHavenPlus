# Contributor and AI-agent instructions

Read `README.md`, this file, the active GitHub Issues, and the relevant source before changing the project. The repository's current implementation and CI workflow take precedence over stale documentation.

## Branching and promotion rule

- All development work must be committed and pushed to the `beta` branch. This includes app code, fixes, tests, build workflows, and documentation.
- Keep `main` unchanged during normal development. Do not commit, push, merge, cherry-pick, or otherwise promote changes to `main` unless the project owner explicitly says to do so.
- Treat `beta` as the active working and demo branch. Validate changes and build demo APKs from `beta`.
- A request to continue working, fix something, or update project files is authorization to work on `beta` only; it is not authorization to update `main`.

## Product scope

LastHavenPlus is a focused Android client for the Lasthavens game, built with Kotlin and Mozilla GeckoView. It bundles the Lasthavens Addon Manager WebExtension. It is not intended to become a general-purpose browser.

## Protected architecture and data

- Keep GeckoView unless a concrete technical reason justifies an engine change.
- The game and Addon Manager are intended to share the Gecko runtime/profile. Do not break this relationship.
- Preserve user profile, login/session state, add-ons, and manager data. Before storage, profile, backup, or migration changes, inspect existing behavior and define a tested backup/migration/rollback path.
- Do not modify, replace, or upgrade the bundled Addon Manager without explicit authorization from the project owner.
- Keep permissions minimal. Treat all web content as untrusted; privileged Android bridges must only be reachable through explicitly validated, trusted app flows.
- Validate URL/navigation boundaries, ZIP paths, imported data, and checksums. Fail closed when permissions or security checks fail.

## Service terms and project identity

- The Lasthavens Terms of Use currently prohibit modified clients except for add-ons in the in-game AddOn Library. Compatibility of this independent GeckoView client and bundled Addon Manager is unconfirmed. Obtain explicit guidance from the Lasthavens team before public distribution; do not imply endorsement or approval.
- Clearly label the app as unofficial and state that it is not affiliated with or endorsed by Lasthavens or Blizzard Entertainment.

## Build and release rules

- GitHub Actions is the build and validation path. GitHub Releases are the intended public distribution channel; there is no Google Play track.
- Normal branch builds may produce debug-signed demo APKs for testing. These are not public releases.
- Never publish a debug-signed APK as a release.
- Before the first public release, verify the permanent release signing certificate, back up the private key securely offline, and test in-place upgrades with the same certificate.
- A signing-key or certificate change can prevent in-place upgrades and put user data at risk. Do not experiment with release signing or rotate keys casually.
- Do not claim a build, test, device check, or release succeeded unless the relevant run actually passed.

## Testing priorities

1. Install and smoke-test the current demo on a real phone as early as possible.
2. Test navigation allow-listing and native-bridge exposure.
3. Test profile sharing, Addon Manager workflows, file handling, lifecycle, and Gecko process recovery.
4. Verify backup/restore scope, including settings and enabled/disabled states.
5. Test release signing and upgrade preservation before public releases.
6. Review GeckoView and Android dependency security updates regularly.

CI success validates only the checks that ran. It does not prove device compatibility, performance, thermal behavior, or real gameplay stability. Record device model, RAM, Android version, and reproducible results.

## Change workflow

- Make small, reviewable changes and inspect related Kotlin, extension, Gradle, manifest, resources, and CI files before editing.
- Add/update tests for behavior changes.
- Keep README, CHANGELOG, app/extension version metadata, and implementation synchronized.
- Do not add unrelated browser features, analytics, push notifications, background services, or differential updates without a demonstrated requirement.
- Report changed files, commit/build links, tests actually run, and remaining unverified work.

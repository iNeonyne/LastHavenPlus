# Instructions for AI agents and contributors

Read this file and TODO.md before changing the repository. Use ROADMAP.md for the approved target architecture and delivery gates. The checked-in source and current CI configuration take precedence over stale historical notes.

## 1. Branch policy

- main is the only active development, integration, CI and release branch.
- Do not recreate or target the retired beta branch.
- Requests to continue, fix, review, tidy or improve the project authorize work on main.
- Verify the target branch is main before editing and before every push. Do not create long-lived development branches.

## 2. Product direction

LastHavenPlus is a focused Android client for the Lasthavens game, not a general-purpose browser.

The approved final architecture is:
- Kotlin native Android application shell.
- Android System WebView (Chromium) for gameplay; the engine is provided by the device and is not bundled in the APK.
- A native-backed Addon Manager hosted from local app assets.
- Native Android updater, storage dashboard, file selection, downloads and navigation.
- AndroidX WebKit document-start scripts and origin-restricted WebMessageListener bridges.

Do not use TWA, bundle Chromium, retain GeckoView as a gameplay fallback, or add unrelated browser functionality. GeckoView may exist only temporarily as a one-time migration exporter for the existing manager profile. Remove that temporary path after native migration is verified.

## 3. User data and manager migration

The owner has authorized the major engine and Addon Manager overhaul, including migration from the existing manager.

Preserve:
- Add-on file bytes and relative paths.
- Add-on folder IDs, versions, enabled/disabled state and load order/dependency metadata.
- Library revision and relevant manager settings/preferences.
- Existing native app data and updater compatibility.

Rules:
- Never clear app data, reset the manager, or silently discard installed add-ons.
- Export existing Gecko-backed manager records before removing the only code capable of reading them.
- Use a versioned migration package, strict validation, file checksums, staged writes, atomic metadata activation, idempotent import and rollback.
- Keep the legacy Gecko profile untouched during source cleanup. Do not delete profile files or unrelated app data as part of removing source code.
- Gecko and WebView cookie/profile stores are separate. Do not promise cookie/login transfer; tell users that sign-in may be required again.
- Existing ZIP backups contain add-on files only; do not treat them as complete manager-state backups.
- Do not mark migration complete until native data is re-read and reconciled against exported metadata and file hashes.

## 4. WebView security

- Treat the remote game, add-ons, downloaded files, catalogue responses and imported archives as untrusted.
- Use the minimum WebView settings and Android permissions required by the game.
- Restrict document-start scripts and WebMessageListener to explicit approved HTTPS origins. Validate source origin, main-frame status, message schema, size and operation on every message.
- The remote game bridge is read-only for add-on payload retrieval and accepts only sanitized diagnostics. It must not expose arbitrary native commands, filesystem access or manager mutation.
- Restrict the manager's privileged bridge to the local app-assets origin. Do not reuse it in the remote game WebView.
- Never expose addJavascriptInterface or @JavascriptInterface to remote game content.
- Keep navigation allow-lists strict; reject insecure HTTP and dangerous/untrusted schemes. Open approved external links through Android.
- Validate ZIP paths, file counts, compressed/expanded sizes, checksums, redirects and streamed byte limits. Fail closed.

## 5. Updater and app identity

The updater is a native Kotlin feature and must remain independent of WebView and the Addon Manager.

Preserve:
- Application ID com.lasthavens.android.
- The existing permanent signing certificate and signing configuration.
- ARM64-only release distribution.
- Monotonic CI versionCode from GITHUB_RUN_NUMBER.
- Metadata-only discovery of the installed version and newer published stable releases.
- Explicit user selection before APK download.
- Full APK download, SHA-256, declared-size, package/version/versionCode, ABI and exact signing-certificate validation.
- Android user-confirmed package installer handoff and accurate success/failure reporting.

The updater must never clear app data, uninstall the app or silently install an APK. Do not bypass package, version, ABI or signer checks. Main CI artifacts, pull-request builds, drafts and prereleases are not updater sources.

## 6. Build and release policy

- GitHub Actions is the build and validation path. GitHub Releases are the distribution channel; there is no Google Play release track.
- Main builds are validation artifacts, not update candidates.
- Tagged builds must use the permanent signing key and create a draft release only. Never publish a release without explicit owner approval.
- The expected signing certificate SHA-256 is B3BBF7520E77DFC384A60B1EEE6C5F205B978F9B453849E2BE523F642018D263. Keep CI's direct exported-certificate-byte fingerprint check; never change the expected fingerprint to accommodate a different key.
- Release tags use stable vMAJOR.MINOR.PATCH and must match app/build.gradle.kts versionName and point to a commit reachable from main.
- Keep versionName stable across ordinary commits. Do not bump it merely because CI ran. versionCode must remain globally monotonic across main and tags; local fallback codes are never update candidates.
- Do not claim a build, test, artifact, migration or release succeeded unless its actual result was checked.
- No physical-device testing tasks should be assigned to the owner. Prefer deterministic unit, integration, screenshot and emulator-based automation. Clearly state what automated tests cannot prove.

## 7. Development workflow

1. Verify main, inspect the latest commits and current workflow status.
2. Read the relevant TODO items and the matching ROADMAP phase.
3. Inspect existing code and data formats before replacing them.
4. Identify user-data, security, upgrade and rollback risks before editing.
5. Make a complete, reviewable change; add focused regression tests.
6. Run targeted tests and then the full CI/build checks where feasible.
7. Review the final diff, packaged APK contents and generated artifacts. Confirm obsolete engine code/assets are actually absent only after replacement is complete.
8. Update TODO and technical documentation when task status or architecture changes. CHANGELOG contains only actual user-visible changes.
9. Report branch, commit, changed files, test/build results and remaining uncertainty.

Do not use or recommend Termux for project workflows.

## 8. Documentation ownership

- ROADMAP.md defines the approved final architecture, phases and acceptance gates.
- TODO.md is the only actionable implementation queue. Keep tasks ordered by dependency and remove completed, superseded or duplicate tasks.
- AGENTS.md contains contributor/agent rules, not a second roadmap.
- PROJECT_OVERVIEW.md describes current verified source state and clearly separates it from the target architecture.
- README.md is concise human-facing product/build information and must not claim planned features are shipped.
- MANUAL_CHECKLIST.md contains only owner-only legal, key-custody and release-approval actions; do not put physical-device testing requests there.
- CHANGELOG.md records user-visible changes that actually shipped. Do not put plans or unreleased work in release entries.

## 9. Scope and distribution

Do not add analytics, tracking, push notifications, background services, differential APK updates, bundled browser engines or unrelated frameworks without a demonstrated requirement and explicit approval where appropriate.

The Lasthavens Terms of Use restrict modified clients except for add-ons in the in-game AddOn Library. Obtain written guidance from the Lasthavens team before public distribution. Identify this as an unofficial independent client; do not imply endorsement by Lasthavens or Blizzard Entertainment.

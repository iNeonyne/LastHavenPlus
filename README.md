# LastHavenPlus

**LastHavenPlus** is a native Android client for the Lasthavens game. It runs the game in Mozilla GeckoView and bundles the Lasthavens Addon Manager, so managed add-ons can be shared with the game through one Gecko profile.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Latest CI: https://github.com/iNeonyne/Lasthavens-Android/actions/workflows/build.yml

## Project brief for AI agents and contributors

**Read this section and the active To-do list before making any change.** Treat this README as the project's shared source of truth; check the current source and CI workflow before relying on documentation alone.

### Project summary

LastHavenPlus is a focused Android game client for [Lasthavens](https://alt.lasthavens.com/play), not a general-purpose browser. It is written in Kotlin and uses Mozilla GeckoView to run the web game. A bundled Lasthavens Addon Manager WebExtension provides add-on management. The game and manager are intended to use the same Gecko runtime/profile so add-ons installed by the manager can be available to the game.

The Android layer provides the app's home/navigation screens and bridges required browser capabilities to Android, including lifecycle handling, fullscreen, JavaScript dialogs, external links/popups, document selection, and downloads. The extension is packaged inside the APK at `app/src/main/assets/extensions/lh-addon-manager/`.

The project is maintained in [this GitHub repository](https://github.com/iNeonyne/Lasthavens-Android). GitHub Actions is the build and validation path, and GitHub Releases are the only intended distribution channel; there is no Google Play release track.

### Technical direction and working rules

- Keep the product focused on reliable Lasthavens gameplay and add-on management.
- Preserve GeckoView unless a concrete technical reason justifies an engine change. The Addon Manager is a core part of the product and may be fixed, improved, redesigned, or upgraded when justified; the bundled extension uses its own semantic version, starting at `1.0.0`.
- Protect user data. Before changing Gecko profiles, extension storage, add-on formats, backup behavior, or migration logic, understand the existing behavior and provide a tested backup/migration/rollback path.
- Prefer small, reviewable changes. Check related Kotlin code, extension code, Gradle configuration, manifests, and CI checks before editing.
- Validate changes with the narrowest relevant tests, then run the repository's CI/release build when feasible. Do not describe a build, test, device check, or release as successful unless it actually ran and passed.
- Defer physical-device testing until the app has completed at least a couple of substantial feature/update cycles and source-level, automated, and CI checks show no known blocking issues. Physical testing is a later validation gate, not the first development task; never claim device compatibility or performance before it is measured.
- Keep permissions minimal, handle untrusted web content carefully, and do not weaken URL/navigation boundaries for convenience.
- Keep this README, version metadata, release notes, and the actual implementation in sync. Update the To-do list when work changes project status.
- Do not add unrelated browser features, analytics, push notifications, background services, or differential updating without a demonstrated requirement.

### Before submitting work

1. Read this project brief and the relevant To-do items.
2. Inspect the current implementation and recent repository/CI state; do not assume older plans are still accurate.
3. Identify data, compatibility, security, and upgrade risks before changing behavior.
4. Make the smallest complete change and add or update tests/checks where practical.
5. Report exactly what changed, what validation ran, what remains unverified, and any follow-up work.

## Current version

| Component | Version | Notes |
|---|---:|---|
| Android app | `2.0.0` | Current major development version; not yet a released or device-verified build |
| Addon Manager | `1.0.1` | Permission checks fail closed when the browser permissions API is unavailable |
| GeckoView | `159.0.20261102144711` | Pinned Mozilla engine build |

The app and Addon Manager versions are independent. The Addon Manager version is defined by `manifest.json`; start at `1.0.0` and update it for subsequent manager changes using semantic versioning. Keep this table, the manager's release notes, and any version checks synchronized with that manifest value.

## What the app does

- **Home screen:** opens Play or the Addon Manager.
- **Play:** loads the Lasthavens game in GeckoView, supports fullscreen, and keeps the game session alive when returning Home.
- **Addon Manager:** opens the bundled manager in a second session using the same Gecko runtime/profile.
- **Add-ons:** the manager installs and stores add-ons; returning to Play requests a game reload so updates can take effect.
- **Files:** Android's document picker handles add-on import and backup restore; downloads are saved to the Downloads/Lasthavens folder on supported Android versions.
- **Web behavior:** native JavaScript alert/confirm/prompt dialogs, external-link routing, popup handling, and load progress feedback.

## Performance and device compatibility

Development prioritizes smooth gameplay, responsive touch controls, stable GeckoView sessions, efficient memory use, and reasonable battery and thermal behavior. During early development, use code review, automated tests, emulator checks where useful, and CI to catch issues without making physical-device testing an immediate blocker. Schedule comprehensive physical-device testing after at least a couple of substantial feature/update cycles, once known bugs and data-handling risks have been addressed. At that stage, test different hardware and display configurations, record device models and Android versions, and add device-specific workarounds only for reproducible compatibility issues. Do not claim real-device compatibility or measured performance before those tests are completed.

## Architecture

- **Kotlin + AndroidX:** native activities, app lifecycle, dialogs, file picker, and downloads.
- **GeckoView:** embeds Mozilla's browser engine. It is deliberately used instead of Android WebView because the bundled manager relies on WebExtension APIs.
- **Built-in WebExtension:** source files live in `app/src/main/assets/extensions/lh-addon-manager/`; GeckoView installs it from the APK assets.
- **Shared runtime:** game and manager sessions use the same Gecko runtime/profile so installed extension data is available to the game.
- **CI checks:** validates bundled JSON and JavaScript, compiles the release APK, and checks that the manager, locale, and GeckoView native engine are packaged.

## Build and downloads

GitHub Actions builds on pushes to `main`, pull requests, and manual dispatches.

**Download the right APK from the latest successful run's `LastHavenPlus-APKs` artifact:**

| File suffix | Intended device |
|---|---|
| `arm64-v8a` | Most modern Android phones; choose this first |
| `armeabi-v7a` | Older 32-bit ARM Android devices |
| `x86_64` | Android emulators and x86-64 devices |
| `universal` | Fallback containing all supported CPU architectures; largest download |

The build produces three ABI-specific APKs to avoid shipping all GeckoView native engines to every phone. The universal APK is retained as a compatibility fallback. Choose `arm64-v8a` for most modern Android phones.

### Verified APK sizes

The successful GitHub Actions run [#46](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45077047753) built and verified all four APKs. Sizes below are exact file sizes from that run:

| APK | Size | Bytes |
|---|---:|---:|
| `arm64-v8a` | 92.53 MiB | 97,023,158 |
| `armeabi-v7a` | 89.91 MiB | 94,275,656 |
| `x86_64` | 97.27 MiB | 102,021,015 |
| `universal` | 238.6 MiB | 250,186,943 |

These are CI artifact sizes, not download-speed estimates. Future builds may differ.

For a local build, open the project in Android Studio with JDK 17 and run `assembleRelease`.

## Releases and version history

A version is published as a GitHub Release only after its build and validation pass. Each release must include the matching APKs and a concise list of user-visible changes. To publish a version, update `appVersionName` and `appVersionCode` in `app/build.gradle.kts`, update this README's version table and history, commit the changes, then push a matching tag such as `v2.0.0`. The release workflow attaches all generated APK variants to that GitHub Release.

### Released versions

| Version | Status | Summary |
|---|---|---|
| 1.0.0 | Baseline; CI build candidate, physical-device validation deferred | First packaged Android app baseline with GeckoView, Home/Play/Addon Manager screens, shared runtime, and bundled Addon Manager. |
| 2.0.0 | Major version in development; not released | Removes the in-game top loading-progress line while retaining the configurable LH diagnostics badge and diagnostic logging. |

### Unreleased changes on `main`

- Added a landscape-specific Home screen layout and set the launcher screen to sensor landscape; Play and Addon Manager behavior is unchanged.
- Migrated the build to a current Android Gradle Plugin/Kotlin toolchain and public Gradle DSLs.
- Added validation for every bundled Addon Manager JSON and JavaScript file.
- Added APK-content checks for the built-in extension, English locale, and GeckoView native engine.
- Added native JavaScript alert, confirm, and prompt dialogs.
- Removed the thin top-edge loading-progress line from the game view; retained the LH diagnostics badge and its logging.
- Fixed CI APK signature verification to call the SDK's explicit `apksigner` path.
- Added SHA-256 checksum generation and verification; checksum files are included with CI artifacts and GitHub Releases.
- Added early release-keystore, alias, and private-key password validation so signing configuration errors are reported before APK packaging.
- Added Addon Manager regression tests for metadata parsing, dependency handling, library merge behavior, archive round-trips, and malicious/duplicate ZIP paths; CI now runs them.
- Fixed Addon Manager permission checks to fail closed when the browser permissions API errors, rather than assuming access was granted.
- Added ABI-specific APK outputs plus a universal fallback (CI validation pending).

For every future release, add a new row under **Released versions** and summarize the user-visible changes. Keep this README in the same commit as the version bump.

## To-do list

### Top priority — Demo making (get a test APK ASAP)

**Goal:** produce an installable demo APK from `main` so the app can be tested on an Android phone before signing and release work are finished. This is a test build, not a public/stable release.

- [ ] Make normal pushes to `main` build and upload debug-signed demo APKs without requiring release-keystore secrets.
- [ ] Keep permanent release signing mandatory for version-tagged GitHub Releases; never publish a debug-signed APK as a release.
- [ ] Get the first successful CI demo artifact and download the `arm64-v8a` APK (use universal only if the ABI-specific build cannot be installed).
- [ ] Install and smoke-test: first launch, Home screen, Play, game loading/sign-in, return Home and resume Play, open Addon Manager, and return to the game.
- [ ] Check basic touch input, orientation/fullscreen, keyboard, network failure/retry, back navigation, and app background/resume.
- [ ] Record crashes, broken screens, loading failures, and device/Android version; fix demo-blocking issues before adding optional features.
- [ ] Verify the demo APK can be installed cleanly. Debug-signed demos cannot upgrade an APK signed with a different certificate; do not uninstall an existing app if its data needs preserving.
- [ ] Keep the demo clearly labelled as a test build and do not call it a validated release.

**Demo scope:** Home, GeckoView game session, bundled Addon Manager, shared profile, and essential Android integration. Defer optional features, performance tuning, full manager redesign, release signing, and public release until the basic demo has been tested.

This is the active development roadmap, ordered by immediate demo delivery first, then reliability and release readiness. The game client and Addon Manager are both core parts of the app. The Addon Manager is not frozen: its code may be fixed, improved, redesigned, or upgraded when useful. Its current version is `1.0.1`. Changes must be tested against real add-on workflows and must protect existing user data.

### Priority 1 — Demo blockers and core reliability (after the first demo artifact)
- [ ] Audit the Kotlin app, bundled extension, Gradle configuration, manifest, and CI workflow against the documented architecture.
- [ ] Build a test matrix for game navigation, shared Gecko profile behavior, add-on workflows, file handling, lifecycle, and error recovery.
- [ ] Add or expand automated tests for URL routing/allow-listing, version parsing, download filename sanitization, and other testable Kotlin logic.
- [ ] Validate bundled extension JSON, JavaScript, manifests, locale files, and APK contents on every relevant CI run.
- [ ] Use emulator tests where practical for launch, navigation, lifecycle, file-picker integration, and basic UI behavior.
- [ ] Reproduce and fix known bugs before advancing major feature/update milestones.
- [ ] Review data-storage behavior and document exactly what is preserved, backed up, or excluded before changing profile or extension storage.
- [ ] Keep a record of unresolved issues and risks; do not treat passing CI as proof that the app is bug-free.

### Priority 2 — Reliable signed releases and upgrades (not required for the first demo)
- [ ] Create and securely back up the permanent release keystore; configure the four documented GitHub Actions secrets. Required for tagged releases, not branch demo APKs.
- [ ] Verify a release-signed APK installs cleanly and a subsequent release upgrades it without uninstalling the app.
- [ ] Verify the signing certificate is identical across ABI APKs and remains unchanged between releases.
- [ ] Verify app upgrades preserve the Gecko profile, login/session data, manager settings, and installed add-ons.
- [ ] Once the later physical-device validation gate has passed, publish a tested GitHub Release with all supported APK variants and SHA-256 checksums.
- [ ] Keep version metadata, release notes, README history, and generated APK names synchronized.
- [ ] Add CI checks for version consistency, manifest permissions, bundled extension files, and APK ABI contents.

### Priority 3 — Addon Manager quality and evolution (demo-critical fixes first)
- [ ] Audit the current bundled Addon Manager source, architecture, permissions, and GeckoView API usage; document its current capabilities and known limitations.
- [ ] Create a manager-specific test checklist covering install, update, enable/disable, remove, import/export, backup/restore, and game integration.
- [ ] Fix reproducible manager bugs and improve error messages, loading states, and recovery from failed operations.
- [ ] Review the manager UI for usability on phone-sized screens, touch targets, accessibility, and large add-on lists.
- [ ] Improve add-on metadata, compatibility reporting, and update handling where the upstream/game ecosystem provides reliable information.
- [x] Add Node-based regression tests for TOC parsing, dependency order/cycles, merge and enabled-state preservation, duplicate detection, and ZIP import/export path validation; run them in CI. Existing JSON, JavaScript, manifest, locale, and APK packaging checks remain in place.
- [ ] Evaluate newer Addon Manager versions or architectural changes when they provide meaningful benefits; record compatibility and migration risks before adopting them.
- [ ] Before any manager migration or data-format change, define and test a backup, migration, and rollback path for existing add-ons and settings.
- [ ] Keep the bundled extension version and app release notes accurate; update `manifest.json`, `README-1.0.0.txt` (or add a version-specific release note), the root README, and `PROJECT_OVERVIEW.md` whenever manager functionality changes.
- [ ] Add a CI consistency check that compares the manager manifest version with the documented current version and release-note version.

### Priority 4 — Game client reliability and browser integration
- [ ] Re-test navigation rules, popup handling, external URL routing, fullscreen, JavaScript dialogs, file prompts, and downloads.
- [ ] Verify the game and Addon Manager continue to share the intended Gecko runtime/profile without unnecessary session reloads.
- [ ] Review lifecycle handling, delegate cleanup, back-button behavior, and recovery after Gecko content-process termination.
- [ ] Verify HTTPS-only network behavior and ensure external navigation cannot unexpectedly expose app data.
- [ ] Add focused Kotlin unit tests for URL routing/allow-listing, download filename sanitization, and version parsing.
- [ ] Keep Android permissions to the minimum required and document any permission changes with their purpose.

### Priority 5 — Physical-device validation (deferred milestone)
- [ ] Begin this phase after at least a couple of substantial feature/update cycles and after known blocking bugs and data risks have been addressed.
- [ ] Install the current `arm64-v8a` APK on a physical Android phone and verify installation, first launch, Home → Play, sign-in, and gameplay.
- [ ] Verify the game and Addon Manager use the intended shared Gecko runtime/profile.
- [ ] Test add-on install, update, enable/disable, removal, import/export, backup/restore, and game integration.
- [ ] Test JavaScript dialogs, file picker, downloads, external links, popups, fullscreen, back navigation, keyboard, and screen insets.
- [ ] Test background/foreground, network loss/reconnect, activity recreation, and Gecko content-process recovery.
- [ ] Establish startup, memory, frame pacing, touch responsiveness, battery, and thermal baselines, including a 30–60 minute gameplay session.
- [ ] Test at least one additional Android device or hardware configuration when practical.
- [ ] Record device model, RAM, Android version, vendor OS build, results, and remaining limitations.

### Priority 6 — Maintenance and useful optional features
- [ ] Review GeckoView stable security updates regularly; test gameplay and Addon Manager compatibility before upgrading either component.
- [ ] Review Android Gradle Plugin, Kotlin, AndroidX, and Gradle updates in separate, build-verified changes.
- [ ] Evaluate R8/resource shrinking against release builds and extension/runtime regression tests.
- [ ] Consider an in-app GitHub Releases checker and installer handoff after signing and upgrade behavior are reliable.
- [ ] Keep full signed APK downloads as the default update method; defer differential updates unless download size becomes a demonstrated problem.
- [ ] Expand OEM/cloud-device testing when real compatibility gaps or user reports justify it.

### Release checklist
- [ ] Run CI validation, unit tests, and a release build.
- [ ] Complete physical-device validation at the deferred milestone before declaring device compatibility or publishing a fully validated stable release.
- [ ] Test game launch and the complete Addon Manager critical workflow on the physical device during that validation phase.
- [ ] Verify app upgrade behavior and data/profile preservation from the previous release.
- [ ] Confirm package name, version, signing certificate, ABI, and requested permissions.
- [ ] Generate and verify SHA-256 checksums for every release APK.
- [ ] Update README release history and publish concise GitHub Release notes.

### Out of scope unless a real requirement emerges
- General-purpose browser features unrelated to Lasthavens gameplay or add-on management.
- Replacing GeckoView without a demonstrated technical reason and migration plan.
- Differential APK patching, analytics, push notifications, or background services without a concrete user need.
- Claiming device or Android-version support before it has been tested.

## Signing

CI is currently configured to require a permanent release keystore for non-PR builds. The latest build reached APK packaging but failed because Android Gradle could not read the configured private key from the keystore. The workflow now validates the keystore, alias, and key password before Gradle packaging so a misconfigured key fails with a clearer step-level error. Recheck the four GitHub Actions secrets below; do not paste their values into chat or commit them. Normal `main` builds are intended to produce debug-signed demo APKs without those secrets. A successful demo build can be installed for testing before release signing is configured.

For stable in-place updates, configure these GitHub repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Do not commit the keystore or passwords to the repository.

## Known limitations

- Live Game Scan observes add-on file requests; it cannot prove that an add-on's Lua code initialized successfully.
- Backup ZIPs contain managed add-on files only. They do not include game settings, SavedVariables, or manager enabled/disabled flags.
- The bundled extension's Gecko minimum version is 142.0; keep GeckoView at or above that version and test extension compatibility before upgrades.
- A successful CI build proves packaging and the checks actually run; it does not prove the app is bug-free or establish real-device functionality. Physical-device validation is intentionally scheduled after at least a couple of substantial feature/update cycles.

## Project structure

```
.github/workflows/build.yml       CI build, validation, and release upload
app/src/main/java/...             Kotlin Android app
app/src/main/assets/extensions/   Bundled Addon Manager WebExtension
app/src/main/res/                 Android layouts, icons, themes, and strings
app/build.gradle.kts              Android config and app version
gradle.properties                 GeckoView version and Gradle settings
PROJECT_OVERVIEW.md               Development plan and device test checklist
README.md                         User-facing project summary, releases, and to-do list
```

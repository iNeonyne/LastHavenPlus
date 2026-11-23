# LastHavenPlus

**LastHavenPlus** is a native Android client for the Lasthavens game. It runs the game in Mozilla GeckoView and bundles the Lasthavens Addon Manager, so managed add-ons can be shared with the game through one Gecko profile.

- Game: https://alt.lasthavens.com/play
- Repository: https://github.com/iNeonyne/Lasthavens-Android
- Latest CI: https://github.com/iNeonyne/Lasthavens-Android/actions/workflows/build.yml

## Project overview

Contributor and AI-agent rules are maintained in [AGENTS.md](AGENTS.md). User-visible changes are tracked in [CHANGELOG.md](CHANGELOG.md). Read those files and the active roadmap before making changes; always verify the current source and CI workflow rather than relying on documentation alone.

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
- Test the first installable demo on a real phone as soon as CI produces it. Emulator and source checks cannot validate touch latency, Gecko process recovery, memory pressure, thermals, or OEM behavior. Record results honestly; never claim device compatibility or measured performance before testing.
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

Development prioritizes smooth gameplay, responsive touch controls, stable GeckoView sessions, efficient memory use, and reasonable battery and thermal behavior. Test the first demo on a real phone immediately, then repeat focused device checks after meaningful changes. Measure memory, touch responsiveness, battery and thermals during longer sessions; record device model, RAM, Android version, and reproducible issues. Do not claim compatibility or measured performance before those tests are completed.

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

## Development priorities

The roadmap below is the current priority order. Detailed task tracking will move to GitHub Issues as tasks are split into actionable, testable work items.

### Top priority — Demo making (get a test APK ASAP)

**Goal:** install and test the current demo on a real Android phone now. This is a debug-signed test build, not a public/stable release.

- [x] Make normal pushes to `main` build and upload debug-signed demo APKs without release-keystore secrets.
- [x] Produce successful CI demo artifacts. Latest landscape build: [run #107](https://github.com/iNeonyne/Lasthavens-Android/actions/runs/45089891036); ARM64 and multi-ABI artifacts uploaded.
- [x] Set the Home screen to landscape and add a dedicated landscape layout; CI build #107 passed.
- [ ] Download and install the `arm64-v8a` demo APK; do not uninstall an existing differently signed app if its data needs preserving.
- [ ] Smoke-test first launch, landscape Home, Play, game loading/sign-in, Home → Play resume, Addon Manager, and return to the game.
- [ ] Check touch, fullscreen, keyboard, network failure/retry, back navigation, and background/resume.
- [ ] Record crashes, loading failures, device model, RAM, and Android version; fix demo blockers.
- [ ] Keep the demo labelled as a test build; CI success is not device validation.

**Demo scope:** Home, GeckoView game session, bundled Addon Manager, shared profile, and essential Android integration. The first real-device smoke test is not deferred until later feature cycles.

### Priority 1 — Immediate device, security, and data validation

- [ ] Install the current `arm64-v8a` demo on a physical phone and complete the smoke test above before expanding scope.
- [ ] Measure memory use, touch responsiveness, Gecko content-process recovery, battery, and thermals during a 30–60 minute gameplay session.
- [ ] Add focused Kotlin unit tests for URL routing/allow-listing and native-bridge exposure; untrusted web content must not reach privileged Android functionality.
- [ ] Audit navigation boundaries, external links, popup handling, HTTPS-only behavior, and permission checks; keep failures closed.
- [ ] Review what backup/restore actually preserves. Current ZIP backups contain managed add-on files only, not manager enabled/disabled state, game settings, or SavedVariables.
- [ ] Decide and implement a tested backup format for settings and enabled/disabled states, or clearly disclose those exclusions in the manager UI before users rely on backups.
- [ ] Audit storage/profile preservation and document exactly what is preserved, backed up, or excluded.
- [ ] Keep a record of unresolved issues; passing CI is not proof of runtime correctness.

### Priority 2 — Permanent signing and safe public upgrades

- [ ] Before the first public GitHub Release, resolve the certificate mismatch, create/confirm the permanent release key, and make at least two secure offline backups.
- [ ] Configure the required GitHub Actions secrets without exposing key material in logs or the repository.
- [ ] Verify a release-signed APK installs cleanly and a subsequent APK with the same certificate upgrades without uninstalling.
- [ ] Verify the certificate is identical across ABI APKs and remains unchanged between releases.
- [ ] Verify upgrades preserve the Gecko profile, login/session data, manager settings, and installed add-ons.
- [ ] Keep debug-signed CI demo artifacts separate from public release APKs; never publish a debug-signed APK as a release.
- [ ] Publish only after the signing and upgrade path has been tested on-device.

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

### Priority 5 — Updates and maintenance

- [ ] Add an in-app GitHub Releases update checker and installer handoff; make current version, latest version, release notes, and download choice clear.
- [ ] Keep full signed APK downloads as the default update method; defer differential updates unless download size becomes a demonstrated problem.
- [ ] Add Dependabot or Renovate checks for GeckoView and other dependencies; review security updates promptly and test game/manager compatibility before upgrading.
- [ ] Review Android Gradle Plugin, Kotlin, AndroidX, and Gradle updates in separate, build-verified changes.
- [ ] Evaluate R8/resource shrinking against release builds and extension/runtime regression tests.
- [ ] Expand OEM/device testing when real compatibility gaps or user reports justify it.
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

Release signing is paused and is not required for development demo APKs. Normal `main` builds produce debug-signed demo APKs without release-keystore secrets; this was verified by successful CI run #99. Version-tagged GitHub Releases still require the permanent release key and must never use a debug-signed APK. The latest tagged signing test failed because the configured keystore certificate did not match the pinned expected release certificate. Do not spend more time on signing until release publication or in-place upgrades become the active priority. Keep keystore material and passwords out of chat and source control.

For stable in-place updates, configure these GitHub repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Do not commit the keystore or passwords to the repository.

## Known limitations

- The Lasthavens Terms of Use prohibit modified clients, except for add-ons in the in-game AddOn Library. Whether this independent GeckoView client and its bundled Addon Manager are permitted is unconfirmed; obtain explicit guidance from the Lasthavens team before public distribution. The app must clearly state that it is unofficial and not endorsed by Lasthavens or Blizzard.
- Live Game Scan observes add-on file requests; it cannot prove that an add-on's Lua code initialized successfully.
- Backup ZIPs contain managed add-on files only. They do not include game settings, SavedVariables, or manager enabled/disabled flags.
- The bundled extension's Gecko minimum version is 142.0; keep GeckoView at or above that version and test extension compatibility before upgrades.
- A successful CI build proves packaging and the checks actually run; it does not prove the app is bug-free or establish real-device functionality. The first physical-device smoke test is an immediate demo gate.

## Project structure

```
.github/workflows/build.yml       CI build, validation, and release upload
app/src/main/java/...             Kotlin Android app
app/src/main/assets/extensions/   Bundled Addon Manager WebExtension
app/src/main/res/                 Android layouts, icons, themes, and strings
app/build.gradle.kts              Android config and app version
gradle.properties                 GeckoView version and Gradle settings
PROJECT_OVERVIEW.md               Development plan and device test checklist
README.md                         User-facing overview, build instructions, and release notes
AGENTS.md                         Contributor and AI-agent working instructions
CHANGELOG.md                      Versioned user-visible change history
```

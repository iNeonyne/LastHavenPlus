# Contributing to LastHavenPlus

The repository owner makes final product and release decisions. `beta` is the development and integration branch; `main` is stable.

## Scope and compatibility

- Target Android 10 (API 29) and later, ARM64 (`arm64-v8a`) only.
- Preserve application ID `com.neonyne.lasthavenlauncher`.
- Preserve the established release signing identity. Never commit or substitute a release key.
- Keep Mozilla GeckoView as the browser engine. Do not reintroduce Cefrium.
- Keep updater checks, downloads and installation user-initiated.
- Do not add analytics, automatic telemetry or background installation.
- Addon-manager work is on hold unless the owner explicitly approves resuming it.

## Development workflow

1. Read `README.md`, `AGENTS.md`, `AGENT_WORK.md`, `STATUS.md`, the relevant section of `ISSUES.md` and the linked GitHub issue before editing.
2. Work on one assigned issue per branch. Keep changes small and file scopes non-overlapping.
3. Open a pull request targeting `beta`; do not target `main` for routine development.
4. Chaty and Vibe are the reviewers/integrators authorized to merge into `beta` (never their own PRs). Contributors must not push directly to `beta` or `main`, merge their own PRs, or promote/publish a release.
5. Include the exact head SHA, changed files, test commands/results, CI links, limitations and physical-device status in the PR handoff.
6. Never force-push shared history. Reuse an existing task branch when practical; avoid creating unnecessary branches.
7. Do not claim physical-device verification unless a real ARM64 device was tested and the device, Android version, build SHA and results are recorded.

## Build, test and quality checks

Use the committed Gradle Wrapper and the pinned JDK/Android SDK documented in `README.md`.

Common local checks:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
python3 -m unittest discover -s .github/scripts/tests -v
./gradlew lintDebug
./gradlew spotlessApply
./gradlew spotlessCheck
```

- `spotlessApply` formats changed Kotlin and Kotlin Gradle files using the pinned ktfmt style.
- `spotlessCheck` verifies formatting. CI ratchets formatting against the PR base so unrelated legacy files are not reformatted.
- Pull requests to beta run Android Lint, Kotlin formatting, an unsigned release build, JVM unit tests and updater-helper tests with read-only permissions.
- The separate quality workflow rechecks Android Lint and Kotlin formatting after relevant source/build changes land on beta.
- Documentation-only edits do not trigger Android build or quality workflows.
- A signed release requires the established project keystore and credentials. Never commit keystores or signing secrets.

## Releases

- Releases must be created only from commits already on `main`, after explicit owner approval.
- Use semantic version tags: `vMAJOR.MINOR.PATCH`.
- Do not replace a published release or retarget a published tag.
- Verify APK signature, certificate fingerprint, ARM64-only native libraries, checksum, manifest and release assets.
- Keep the full APK as the required updater payload. Differential updates are optional and must retain a verified full-APK fallback.
- Physical ARM64 verification remains a separate release-readiness requirement.

## Issues and documentation

- GitHub Issues and `ISSUES.md` are the task sources of truth.
- Link implementation work to an existing issue where possible.
- Close an issue only after every acceptance criterion is verified.
- Keep README, changelog, migration notes and issue status consistent with code on beta.
- Record known limitations instead of implying unsupported behavior.

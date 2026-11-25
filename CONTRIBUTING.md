# Contributing to LastHavenPlus

LastHavenPlus is maintained through a maintainer-led, direct-to-main workflow. The repository owner is the integration authority.

## Scope and compatibility

- Target Android 10 (API 29) and later, ARM64 (arm64-v8a) only.
- Preserve application ID com.neonyne.lasthavenlauncher.
- Preserve the established release signing identity. Never commit or substitute a release key.
- Keep GeckoView as the browser engine. Do not reintroduce Cefrium.
- Keep updater checks, downloads and installation user-initiated.
- Do not add analytics, automatic telemetry or background installation.
- Addon-manager work remains on hold unless the owner explicitly reopens it.

## Development workflow

1. Read AGENTS.md, the README, relevant issue, and affected source/tests before editing.
2. Keep changes small and focused. Prefer updating existing implementations over adding abstractions.
3. Agents may use separate workspaces or existing task branches for parallel work. Reuse an existing worker branch where practical; do not create a new branch for every issue.
4. Only the repository owner/main orchestrator integrates changes and pushes to main. Do not open pull requests unless the owner explicitly requests them.
5. Never force-push or rewrite shared history. Never delete branches as routine cleanup.
6. Review the complete diff and run relevant checks before integration.
7. Do not claim physical-device verification unless a real ARM64 device was tested and device, Android version, build SHA and results are recorded.

## Build and verification

Requirements are documented in the root README. Common checks:

    ./gradlew assembleRelease
    ./gradlew testDebugUnitTest
    ./gradlew lintDebug
    python3 -m unittest discover -s .github/scripts/tests -v

A signed release requires the established project keystore and credentials. Never commit keystores or signing secrets.

## Releases

- Releases must be created from commits already on main.
- Use semantic version tags: vMAJOR.MINOR.PATCH.
- Do not replace a published release or retarget a published tag.
- Verify APK signature, certificate fingerprint, ARM64-only native libraries, checksum, manifest and release assets.
- Keep the full APK as the required updater payload. Differential updates are optional and must retain a verified full-APK fallback.
- Physical ARM64 verification remains a separate release-readiness requirement.

## Issues and documentation

- Link implementation work to an existing issue where possible.
- Close an issue only after its acceptance criteria are verified.
- Keep README, changelog, migration notes and issue status consistent with code on main.
- Record known limitations instead of implying unsupported behavior.

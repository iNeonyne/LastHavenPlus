# Agent instructions

## Authority and integration

- The repository owner/main orchestrator is the sole integration authority.
- Work directly toward main; do not open pull requests unless explicitly requested.
- Parallel workers may use separate workspaces or existing task branches, but must not independently integrate or push to main.
- Reuse persistent worker branches where possible. Do not create a new branch for every issue.
- Never delete branches, tags, releases or repository content unless the owner explicitly asks.
- Never rewrite shared branch history. Any worker-branch synchronization must be reviewed and lease-protected.

## Product constraints

- Android 10/API 29+, ARM64 (arm64-v8a) only.
- Keep application ID com.neonyne.lasthavenlauncher.
- Mozilla GeckoView is the browser engine. Do not restore Cefrium.
- Preserve the established signing certificate and updater compatibility.
- Do not add background update installation, telemetry or analytics.
- Addon manager remains on hold pending explicit owner approval.

## Engineering rules

- Read relevant code, tests, README, changelog and issues before changing behavior.
- Keep changes focused and avoid unnecessary modules, dependencies and abstractions.
- Treat updater inputs and downloaded release metadata as untrusted. Keep strict size, redirect, host, digest and package-identity validation.
- Never remove the verified full-APK fallback.
- Never expose signing secrets or user data in logs.
- Update tests and documentation alongside behavior changes.
- Do not claim device testing based on CI or emulator results.

## Required checks

Run the narrowest relevant tests, then as applicable:

    ./gradlew testDebugUnitTest
    ./gradlew lintDebug
    ./gradlew assembleRelease
    python3 -m unittest discover -s .github/scripts/tests -v

For release changes, verify the signing certificate, ARM64-only ABI contents, APK checksum, update manifest, differential fallback and exact release assets.

## Reporting

Report files changed and why; commands/checks run and actual results; unverified behavior and remaining risks; issue numbers affected; and whether any branch or release operation was performed.

Do not state that work is complete while a required check is failing or has not been run.

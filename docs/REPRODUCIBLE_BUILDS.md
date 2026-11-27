# Independent unsigned APK reproducibility

## Purpose

The `Independent unsigned APK reproducibility` workflow builds the same source commit twice on separate, fresh GitHub-hosted Ubuntu 24.04 runners. Both builds use the repository's pinned Gradle wrapper, JDK 25, Android SDK platform 37.2 and Build Tools 37.0.0.

This measures byte-for-byte reproducibility of the optimized **unsigned** release APK. It does not prove release-signing reproducibility, physical-device compatibility, F-Droid build compatibility, or release publication safety.

## Running the check

- For relevant pull requests targeting `beta`, the workflow runs automatically when application, Gradle, wrapper, SDK configuration, comparison helper, or workflow files change.
- On pushes to `beta`, the workflow runs automatically for relevant application/build/workflow paths. The push run verifies the integrated commit; PR runs verify the proposed head before merge.
- The two matrix jobs each check out the same exact commit SHA, disable Gradle build cache and configuration cache, run `clean assembleRelease`, and explicitly remove signing environment variables.
- The workflow rejects a signed APK input. It does not reference or restore release signing secrets.

## Comparison and artifacts

The comparison job checks:
- Whole-APK SHA-256 and file size.
- ZIP entry names, missing/extra entries, duplicate entries.
- Per-entry uncompressed content SHA-256, sizes, CRC, compression method and timestamp.
- Runner image, JDK, Gradle wrapper and installed Android SDK package information.

It uploads `unsigned-apk-reproducibility-report` for 30 days. The report contains a Markdown summary and JSON detail, plus both runner environment records. Intermediate APK artifacts are retained for one day for the comparison job only.

A whole-file hash mismatch fails the workflow after the report is generated and uploaded. If ZIP entries match but the whole-file hashes differ, inspect ZIP/container-level metadata or ordering. The entry-level report is an equivalent structured archive comparison; it is not a full diffoscope report.

## Latest verified result

- Source commit: `78a3d12eb018e112a3443377e34e234ee9d3da7d` (GeckoView 161 Stable and refreshed dependency pins).
- Workflow run: [#46063532417](https://github.com/iNeonyne/LastHavenPlus/actions/runs/46063532417).
- Result: **byte-identical** unsigned APKs from two independent clean runners.
- APK size: 98,226,200 bytes each.
- SHA-256: `e4847d2d048527a12e316b6444fdafa877af9750d87c03d1ba55fde5250a7110`.
- ZIP entries: 860 in each build; no missing/extra entries, duplicate entries or metadata/content differences.
- Both clean build jobs and the comparison job passed. The source commit is the latest beta application/build change; later beta commits through `ce13c2968c67fe680dc38c35ad13a5c7bb78ab73` changed documentation only.

This result applies to the exact source commit and recorded environments only. Repeat the check after relevant application, toolchain, or build-configuration changes.
## F-Droid

F-Droid compatibility is a separate assessment. GeckoView's binary distribution, licensing, source-build availability, and toolchain requirements must be evaluated independently before making any F-Droid compatibility claim.

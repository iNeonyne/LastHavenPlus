# Independent unsigned APK reproducibility

## Purpose

The `Independent unsigned APK reproducibility` workflow builds the same source commit twice on separate, fresh GitHub-hosted Ubuntu 24.04 runners. Both builds use the repository's pinned Gradle wrapper, JDK 25, Android SDK platform 37.2 and Build Tools 37.0.0.

This measures byte-for-byte reproducibility of the optimized **unsigned** release APK. It does not prove release-signing reproducibility, physical-device compatibility, F-Droid build compatibility, or release publication safety.

## Running the check

- For relevant pull requests targeting `beta`, the workflow runs automatically when application, Gradle, wrapper, SDK configuration, comparison helper, or workflow files change.
- To test an already integrated commit, use **Actions → Independent unsigned APK reproducibility → Run workflow** and select the branch/commit to test.
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

## Current guarantee

No reproducibility result is claimed until the workflow has completed successfully on an exact commit. A successful run establishes byte identity for that specific source commit and the recorded runner/toolchain environment only. Repeat the check after relevant toolchain or build configuration changes.

## F-Droid

F-Droid compatibility is a separate assessment. GeckoView's binary distribution, licensing, source-build availability, and toolchain requirements must be evaluated independently before making any F-Droid compatibility claim.

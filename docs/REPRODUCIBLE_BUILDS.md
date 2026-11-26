# Independent unsigned APK reproducibility

## Purpose

The `Independent unsigned APK reproducibility` workflow builds the same source commit twice on separate, fresh GitHub-hosted Ubuntu 24.04 runners. Both builds use the repository's pinned Gradle wrapper, JDK 25, Android SDK platform 37.2 and Build Tools 37.0.0.

This measures byte-for-byte reproducibility of the optimized **unsigned** release APK. It does not prove release-signing reproducibility, physical-device compatibility, F-Droid build compatibility, or release publication safety.

## Running the check

- For relevant pull requests targeting `beta`, the workflow runs automatically when application, Gradle, wrapper, SDK configuration, comparison helper, or workflow files change.
- On pushes to `beta` and the v2.4.0 selective-promotion branch, the workflow runs automatically for relevant application/build/workflow paths. The push run verifies the integrated or promoted source commit; beta PR runs verify the proposed head before merge.
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

- Source commit: `cd8a661a0089d311157c1c6b6746936d5f85977f` (v2.4.0 selective-promotion branch; application and Gradle inputs are unchanged in the final candidate).
- Workflow run: [#45881898757](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45881898757).
- Result: **byte-identical** unsigned APKs from two independent clean Ubuntu 24.04 runners.
- APK size: 96,125,642 bytes each.
- SHA-256: `cadaa8fb1ada530ad8973f91137d653412c82b4196c7010477f31b9d0de7806f`.
- ZIP entries: 852 in each build; no missing/extra entries, duplicate entries or metadata/content differences.
- The report and runner environment records are attached as the `unsigned-apk-reproducibility-report` Actions artifact.

The two runners produced the same complete APK hash and matching entry-level metadata/content. This result applies to the recorded source commit and unsigned build only; it does not prove release-signing reproducibility, physical-device compatibility or publication safety.

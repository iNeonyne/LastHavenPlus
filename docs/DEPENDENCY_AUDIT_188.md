# Stable Android dependency audit (#188)

Audit performed against `beta` starting at `4d0a6e1be16ee192862d82ea37986ede44e636b7` on 2026-11-27. Version pins live in `gradle/libs.versions.toml`; Android dependency declarations are in `app/build.gradle.kts`.

## Changes selected

| Component | Previous | Selected | Evidence and rationale |
|---|---:|---:|---|
| GeckoView ARM64 Stable | `160.0.20261116130455` | `161.0.20261118180411` | Mozilla Maven metadata reports this as latest/release for `org.mozilla.geckoview:geckoview-arm64-v8a`; the published POM is MPL-2.0 and retains the ARM64 Stable artifact coordinate. Mozilla's API changelog for v161 lists additive credit-card-autofill and R8 mapping UUID APIs; no used API removal is listed. Compile/tests and ARM64 packaging validation remain required; physical runtime behavior remains unverified. |
| AndroidX Core | `1.16.0` | `1.19.1` | Android Developers release notes list 1.19.1 as stable (1.20.0-alpha01 is preview). GeckoView 161's published POM itself requires Core 1.19.1 at runtime, so align the direct pin with the selected engine rather than relying on conflict resolution. |
| ktfmt (Spotless formatter engine) | `0.64` | `0.65` | Kotlin/ktfmt v0.65 is a stable release (2026-10-07). It fixes non-idempotent formatting and adds selected-range formatting. Formatting output may change; exact-head Spotless validation is required. |

## Audited pins retained

| Component | Retained pin | Evidence / compatibility rationale |
|---|---:|---|
| Android Gradle Plugin | `9.5.3` | Android Developers lists 9.5.3 as current stable (API reference last updated 2026-11-27). AGP 9.5 requires Gradle 9.6.0 minimum; pinned Gradle 9.7.1 exceeds that minimum. |
| Kotlin / Compose compiler plugin | `2.4.21` | Kotlin 2.4.21 is the latest stable in the 2.4 line; 2.5.0 remains RC as of this audit. |
| Gradle Wrapper | `9.7.1` | Not changed under this dependency-only scope. JDK 25 is supported for running Gradle from 9.1.0 onward. Kotlin's fully-supported Gradle range for 2.4.21 currently ends at 9.7.0, so 9.7.1 is just beyond that documented range despite satisfying AGP's minimum. Do not interpret this as a compatibility guarantee; exact CI/build validation is required. Toolchain change needs separate authorization/evidence. |
| JDK / SDK / Build Tools | JDK `25`; SDK `37.2`; Build Tools `37.0.0` | Retained. Compose BOM 2026.11.01 requires Kotlin >=2.2 and compileSdk >=37.1; current pins satisfy those published floors. |
| Spotless Gradle plugin | `8.12.0` | Gradle Plugin Portal metadata reports 8.12.0 as latest/release on 2026-11-27. |
| Compose BOM | `2026.11.01` | Android Developers November 2026 Compose announcement marks this BOM stable/current; it maps Compose core libraries to 1.13.0 and Material 3 to 1.5.0. |
| AndroidX Activity Compose | `1.13.0` | AndroidX stable release index lists 1.13.0; next listed 1.14.0-alpha03 is preview. |
| AndroidX AppCompat | `1.8.0` | AndroidX release notes list 1.8.0 stable; no newer stable is listed. |
| AndroidX Browser | `1.10.0` | AndroidX release notes list 1.10.0 stable; no newer stable is listed. |
| AndroidX MediaRouter | `1.9.0` | AndroidX release notes list 1.9.0 stable; no newer stable is listed. |
| Apache Commons Compress | `1.28.0` | Maven Central metadata and Apache release notes list 1.28.0 as latest release. |
| JUnit | `4.13.2` | Maven Central metadata and JUnit project's release page show 4.13.2 as the latest 4.x release. No test framework migration is in scope. |

No direct dependency was added or removed. Direct AndroidX dependency necessity/manifest analysis remains assigned separately to #187; this change does not pre-empt that investigation. GeckoView remains ARM64-only and no Beta/Nightly artifact is selected.

## Source references

- Mozilla Maven metadata: https://maven.mozilla.org/maven2/org/mozilla/geckoview/geckoview-arm64-v8a/maven-metadata.xml
- GeckoView 161 POM: https://maven.mozilla.org/maven2/org/mozilla/geckoview/geckoview-arm64-v8a/161.0.20261118180411/geckoview-arm64-v8a-161.0.20261118180411.pom
- GeckoView API changelog: https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/doc-files/CHANGELOG
- AndroidX stable versions: https://developer.android.com/jetpack/androidx/versions
- AndroidX Core release notes: https://developer.android.com/jetpack/androidx/releases/core
- Compose November 2026 release: https://developer.android.com/blog/posts/jetpack-compose-november-2026-release
- Android Gradle Plugin release/API reference: https://developer.android.com/reference/tools/gradle-api and https://developer.android.com/build/releases/agp-9-5-0-release-notes
- Kotlin compatibility table: https://kotlinlang.org/docs/gradle-configure-project.html
- Gradle Java compatibility: https://docs.gradle.org/current/userguide/compatibility.html
- Spotless plugin metadata: https://plugins.gradle.org/m2/com/diffplug/spotless/com.diffplug.spotless.gradle.plugin/maven-metadata.xml
- ktfmt release: https://github.com/Kotlin/ktfmt/releases/tag/v0.65
- Apache Commons Compress release notes: https://commons.apache.org/proper/commons-compress/changes.html
- JUnit 4 releases: https://github.com/junit-team/junit4/releases

## Validation status

The exact-head Android build and unit-test job passed on commit `ce9620a9abddacaa2e4f791e53f7e41980627d52`: dependency resolution, lint, Spotless, release assembly and 236 JVM tests passed with zero failures. The 145-coordinate GeckoView 161 resolved graph is recorded in [the checked-in report](reports/releaseRuntimeClasspath-geckoview-161.0.20261118180411.txt); the original 62,592-byte report is in [Actions artifact 15228564006](https://github.com/iNeonyne/LastHavenPlus/actions/runs/46062233814), SHA-256 `cd18809e360f7f1e29bfaf8cc03cdb9cd5cb83e5f46f6320103247583c260f9f`. Independent unsigned APK reproducibility passed: both clean builds completed and the comparison job passed. No physical ARM64 device test has been performed; no gameplay/runtime compatibility claim is made.

## Validation still required

Run the repository's exact-head Android PR validation and independent unsigned APK reproducibility workflow. Confirm dependency resolution, lint, Spotless, JVM tests, release assembly, and the packaged APK contains only `arm64-v8a`. This audit was not built or run on a physical ARM64 device; it makes no gameplay/runtime compatibility claim.

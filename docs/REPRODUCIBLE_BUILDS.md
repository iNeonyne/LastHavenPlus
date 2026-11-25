# Reproducible APK builds

## Current guarantees

The release workflow pins the Gradle Wrapper, JDK major version, Android SDK platform/build-tools versions and application dependencies. It verifies the established release signing certificate and checks that packaged native libraries are ARM64-only.

These controls improve build consistency and release integrity. They do not yet prove that two independent builds produce byte-for-byte identical APK contents.

## Local clean build

Build from the exact published tag in a clean checkout:

    git clone https://github.com/iNeonyne/LastHavenPlus.git
    cd LastHavenPlus
    git checkout v2.1.0
    ./gradlew --no-daemon clean assembleRelease

A signed build requires the established release keystore and credentials. Do not share or commit those credentials. For independent comparisons, produce unsigned release artifacts using the same source revision and pinned toolchain, then compare their contents.

## Planned verification procedure

1. Build the same commit twice in clean, isolated environments.
2. Record source commit, Gradle distribution checksum, JDK vendor/version, Android SDK and Build Tools versions, dependency resolution and build command.
3. Compare unsigned APK SHA-256 values.
4. If they differ, use diffoscope or equivalent archive inspection to identify differing entries.
5. Separate harmless signing differences from actual payload differences. Compare unsigned APKs before signing.
6. Preserve the existing signed release artifact and established signing identity. Never replace a published release to test reproducibility.
7. Publish the comparison report as a CI artifact. Do not claim reproducibility until independent outputs match and the process is repeatable.

## F-Droid compatibility

F-Droid-style reproducibility is a separate, later goal. It requires a clean independent build recipe, all dependencies available under the build environment's rules, and a documented signature-copy/verification procedure. GeckoView's native dependency and Android toolchain requirements must be validated before promising F-Droid compatibility.

References:
- https://f-droid.org/docs/Reproducible_Builds/
- https://docs.gradle.org/current/userguide/best_practices_security.html

## Current status

- [x] Pinned Gradle Wrapper and distribution checksum (verify on every wrapper change).
- [x] Pinned JDK major and Android SDK/build-tools versions in CI.
- [x] Verify established APK signing certificate.
- [x] Verify ARM64-only native library packaging.
- [ ] Independent clean unsigned APK rebuild.
- [ ] Diffoscope report and remediation of unexplained differences.
- [ ] Repeatable byte-for-byte unsigned APK comparison.
- [ ] F-Droid recipe and independent build-environment validation.

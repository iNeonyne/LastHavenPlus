# Gradle Wrapper

LastHavenPlus commits the Gradle Wrapper so local development and CI use the same pinned Gradle distribution.

- **Gradle:** 9.7.1
- **Distribution URL and SHA-256:** `gradle/wrapper/gradle-wrapper.properties`
- **Wrapper scripts:** `./gradlew` (Unix-like systems) and `gradlew.bat` (Windows)

Run commands from the repository root:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

Use the committed wrapper rather than a globally installed Gradle version. Do not change the pinned Gradle distribution or checksum without validating compatibility with the Android Gradle Plugin, JDK, Android SDK and GeckoView dependency.

Release signing requires the established project keystore and credentials. Keep those secrets out of the repository.

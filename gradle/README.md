# Gradle Wrapper

The project uses the committed Gradle Wrapper pinned to Gradle 9.7.1. The wrapper distribution URL and SHA-256 are recorded in `gradle/wrapper/gradle-wrapper.properties` and validated by GitHub Actions.

Run builds from the repository root with `./gradlew`, for example:

```bash
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

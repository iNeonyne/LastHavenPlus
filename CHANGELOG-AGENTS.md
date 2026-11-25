## 2026-11-25 — Scope compatible dependency refresh from closed Dependabot PRs

- **Role:** Chaty / dependency review
- **Issue:** [#81](https://github.com/iNeonyne/LastHavenPlus/issues/81)
- **Branch:** `chaty/issue-81-compatible-library-refresh`
- **Baseline beta HEAD:** `01af3f488225eaa5321b994a3957e53dcdb70c55`
- **Source PRs:** closed unmerged Dependabot PRs #68 and #69
- **Candidate changes:** AppCompat 1.8.0, MediaRouter 1.9.0, Browser 1.10.0, Compose BOM 2026.11.01 and Commons Compress 1.28.0.
- **Compatibility review:** the Compose BOM requires Kotlin 2.2+ and compileSdk 37.1; beta uses Kotlin Compose plugin 2.4.10 and compileSdk 37.2. AppCompat/MediaRouter minimum SDK requirements are below the app's API 29 minimum. Commons Compress 1.28.0 requires Java 8+. Official release documentation was checked.
- **Excluded:** AGP 9.5.2/Kotlin 2.4.21 and Gradle wrapper 9.9.2 were not copied; the Kotlin/AGP/Gradle compatibility matrix does not support that proposed combination. Gradle Actions v6 also introduces additional caching license terms and is not included in this library-only change.
- **Verification:** pending exact-head CI; no tests or merge claimed.

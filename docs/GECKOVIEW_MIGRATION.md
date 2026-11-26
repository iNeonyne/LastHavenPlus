# GeckoView integration notes

**Current branch:** `beta`  
**Current engine:** Mozilla GeckoView 160, ARM64 artifact  
**Last reviewed:** 2026-11-26 (artifact evidence re-verified against beta `eec16ff` and the published Mozilla Maven POM/module metadata; transitive POM license verification completed against Google Maven and Maven Central after the audit environment's network access to those hosts was restored — see the license status list below)

This document records the GeckoView integration as it exists in beta. It supplements, but does not replace, Mozilla's documentation.

## Official references

- [GeckoView consumer documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/index.html)
- [Quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html)
- [Architecture and process model](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html)
- [GeckoSession API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.html)
- [GeckoView API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoView.html)
- [Site permissions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html)
- [WebExtensions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [Mozilla Maven repository](https://maven.mozilla.org/maven2/org/mozilla/geckoview/)

Use the API matching the pinned GeckoView artifact. Do not assume moving mozilla-central Javadocs exactly match the release used by this project.

## Current implementation

Beta uses `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`, a process-scoped `GeckoRuntime`, a `GeckoSession` and a `GeckoView` host. Runtime/session initialization is deferred until Play. The game URL is `https://lasthavens.com/play`.

The app opens on a launcher Home screen. Settings does not initialize the game. Android Back from gameplay opens a full-screen menu containing Reload, Settings and Return to game. Settings, diagnostics and update details are full-screen destinations. PR #43 also adds responsive Home/settings layouts and adaptive launcher icon resources.

The existing GeckoSession is retained when its view is detached for launcher navigation. Source and JVM layout-policy tests are integrated; physical session retention and UI behavior still require device verification.

## Planned UX redesign — issue #56

The Home-screen-first flow described above is the **current v2.1.0 beta/stable baseline**, not the next UX target. The owner has requested a direct-launch experience with a responsive in-game overlay; implementation is being developed by Chaty under issue #56 on `chaty/issue-56-direct-launch-overlay`; it is not yet integrated into beta. Until that work is reviewed and merged, do not describe the overlay as implemented. The new flow must preserve the existing GeckoSession and profile, support Settings in portrait and landscape, restore landscape gameplay, and keep Addon Manager out of scope while #23 remains on hold.

## Build configuration and identity

The current beta build uses JDK 25, Gradle 9.7.1, AGP 9.4.0 and Android SDK Platform 37.2. GeckoView's AAR metadata requires compile SDK 37.2. Keep the pinned toolchain unless a reproducible compatibility issue and validated change justify otherwise.

Preserve:
- ARM64 only (`arm64-v8a`)
- Application ID `com.neonyne.lasthavenlauncher`
- Established release signing identity
- User-confirmed update download and Android installer handoff
- Full APK fallback when a differential patch is ineligible or fails validation

## Pinned artifact and dependency audit (issue #20)

### Artifact identity

- Coordinates: `org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455`, packaging AAR.
- Source: [Mozilla Maven](https://maven.mozilla.org/maven2/org/mozilla/geckoview/geckoview-arm64-v8a/160.0.20261116130455/), published with Gradle module metadata (the POM itself defers to it).
- Upstream revision: `mozilla-release` `b1145b0710192f716be5f59f5b2dfeed650d5294` (recorded in the artifact POM's SCM block).
- License declared in the POM: [Mozilla Public License 2.0](http://mozilla.org/MPL/2.0/).

### Gradle module metadata dependency inventory (reviewed 2026-11-25; re-verified 2026-11-26)

Verification refresh (2026-11-26, against beta `eec16ff`): the POM and Gradle module metadata for `160.0.20261116130455` were re-fetched from [Mozilla Maven](https://maven.mozilla.org/maven2/org/mozilla/geckoview/geckoview-arm64-v8a/160.0.20261116130455/) and re-checked against this table. The module metadata lists exactly the dependencies below (`kotlin-stdlib` alone is API-scope; every other entry plus `kotlin-stdlib` is runtime-scope). The metadata additionally records the AAR itself at 91,504,240 bytes (SHA-256 `c05916660c0b0125dada536f01570606227202137ff73feeea8ce7b8595dd443`) and was published by Gradle 9.8.1. The catalog now pins Kotlin 2.4.21 (post-PR #98), matching this requirement.

| Group:artifact | Version | Project-side note |
|---|---|---|
| `org.jetbrains.kotlin:kotlin-stdlib` | 2.4.21 | Catalog (#99) pins Kotlin 2.4.21 after PR #98, matching the artifact requirement |
| `androidx.annotation:annotation` | 1.11.0 | |
| `androidx.annotation:annotation-experimental` | 1.6.0 | |
| `androidx.collection:collection` | 1.7.0 | |
| `androidx.core:core` | 1.19.1 | Catalog (#99) pins `androidxCore` 1.16.0; Gradle conflict resolution is expected to select ≥1.19.1 — verify with the dependency report below |
| `androidx.lifecycle:lifecycle-common` | 2.11.0 | |
| `androidx.lifecycle:lifecycle-process` | 2.11.0 | |
| `com.google.android.gms:play-services-fido` | 21.3.1 | Google Play Services SDK terms apply |
| `org.yaml:snakeyaml` | 2.2 | |
| `androidx.media3:media3-common` | 1.11.1 | media3 family pulled for playback support |
| `androidx.media3:media3-datasource` | 1.11.1 | |
| `androidx.media3:media3-decoder` | 1.11.1 | |
| `androidx.media3:media3-exoplayer` | 1.11.1 | |
| `androidx.media3:media3-exoplayer-hls` | 1.11.1 | |

Resolution caveat (updated 2026-11-26, second pass): this inventory is based on the artifact's POM and module metadata, both re-fetched from the authoritative Mozilla Maven repository. It is still not a resolved-by-Gradle dump (the audit environment has no JDK/Android SDK). The network limitation noted earlier has been partially closed: Google Maven (`dl.google.com/dl/android/maven2`) and Maven Central (`repo1.maven.org`) became reachable from the audit sandbox on 2026-11-26, enabling the transitive POM license verification recorded below; the resolved `releaseRuntimeClasspath` still requires a Gradle-capable environment. The catalog-vs-requirement cross-check against beta `eec16ff` is complete: Kotlin 2.4.21 (aligned, PR #98) and `androidxCore` 1.16.0 (catalog pin below the artifact's 1.19.1 requirement, so the resolved version depends on Gradle conflict resolution and must not be lowered further). Issue #20 remains incomplete until `./gradlew app:dependencies --configuration releaseRuntimeClasspath` is captured as the authoritative resolved set and reconciled against this table; a dispatchable report job that produces exactly this artifact is staged on branch `vibe/geckoview-dep-report`.

### License and notice obligations

- GeckoView is MPL 2.0 (declared in the artifact POM re-fetched from Mozilla Maven). For executable-form distribution, MPL 2.0 §3.2 requires the corresponding Source Code Form also be made available as described in §3.1, and recipients must be told how to obtain it by reasonable means and in a timely manner. Verify the exact GeckoView 160 source revision/archive and provide an accessible recipient-facing route before distributing the APK.
- MPL 2.0 §3.4 preserves license and other notices contained in the Source Code Form; it does not independently require bundling the full license text or a source location in the APK. Do not remove or alter the existing notices.
- This is separate from choosing a license for LastHavenPlus under #67; resolving #67 does not replace GeckoView's MPL obligations.
- Per-artifact transitive license verification status (updated 2026-11-26, second pass; no redistribution claims are made for unverified items). Method: the full Maven POM transitive closure of `geckoview-arm64-v8a:160.0.20261116130455` was walked live — 111 POMs fetched (Mozilla Maven, Google Maven, Maven Central; 0 not-found), each POM's `<licenses>` block recorded. This verifies POM-declared licenses only; it does not inspect artifact contents or replace the MPL 2.0 source obligations above:
  - **Verified from published POMs (gap closed 2026-11-26):** every `androidx.*` POM in the closure (81 artifacts, including all direct dependencies `annotation:1.11.0`, `annotation-experimental:1.6.0`, `collection:1.7.0`, `core:1.19.1`, `lifecycle-common:2.11.0`, `lifecycle-process:2.11.0`, `media3-*:1.11.1` and their transitives) declares **The Apache Software License, Version 2.0**; `org.jetbrains.kotlin:kotlin-stdlib:2.4.21` and `org.yaml:snakeyaml:2.2` declare Apache-2.0 (previous pass).
  - **POM-declared terms confirmed (proprietary):** the `com.google.android.gms` Play-services POMs in the closure (`play-services-fido:21.3.1`, `play-services-base:18.7.2`, `play-services-basement:18.9.0`/`18.7.1`, `play-services-tasks:18.3.2`) declare the **Android Software Development Kit License** — confirming the earlier caution that Play Services SDK terms (not an open-source license) govern these artifacts.
  - **NOT verified — no license declared in POM:** the `com.google.guava` artifacts that enter the closure via `play-services-fido` (`guava:33.3.1-android`, `listenablefuture:1.0`, `listenablefuture:9999.0-empty-to-avoid-conflict-with-guava`, `failureaccess:1.0.2`) carry **no `<licenses>` block in their POMs**. Upstream Guava is Apache-2.0, but that is not confirmed from repository metadata; verify against the artifacts themselves before any redistribution decision.
  - **NOT pinnable from POMs alone:** 9 closure references use version ranges or Gradle-managed versions (`lifecycle-runtime:[2.11.0]`, `lifecycle-common:[2.10.0]`, `lifecycle-common-jvm:[2.10.0]`, `arch.core:core-common:[2.2.0]`, and unversioned `kotlin-stdlib`, `jsr305`, `checker-qual`, `error_prone_annotations`, `j2objc-annotations`). The closure contains POM-verified nodes at the concrete versions these ranges are expected to resolve to (e.g. `lifecycle-common:2.11.0`/`2.10.0`, `lifecycle-runtime:2.10.0`, `core-common:2.2.0`), but which concrete version actually resolves is decided by Gradle conflict resolution; the authoritative set requires the dependency report below.

### Used-API cross-check against official documentation

Verification methods, in order of authority for this audit. Each symbol is labeled below with exactly which methods covered it:
1. **Compile-time proof against the pinned 160 AAR:** every symbol below is compiled against `160.0.20261116130455` in CI (PR validation and signed beta runs compile `assembleRelease`); a missing symbol would fail the build. This is authoritative for symbol existence in the pinned artifact.
2. **Reflection guards against the pinned 160 AAR binaries:** `GeckoViewWebExtensionApiTest` (`testDebugUnitTest` in the same CI runs) reflects `WebExtensionController.installBuiltIn(String)`, `ensureBuiltIn(String, String)` returning `GeckoResult`, `WebExtension$TabDelegate` (`setTabDelegate`, `onNewTab`, `onOpenOptionsPage`), `WebExtension$MetaData` fields (`baseUrl`, `optionsPageUrl`, `openOptionsPageInTab`) and `MessageDelegate` wiring.
3. **Official documentation confirmation — 163 mozilla-central snapshot only, not release-160 documentation:** no release-specific 160 Javadoc is published at the referenced hosts, so documentation links below describe the moving mozilla-central snapshot (geckoview 163.0.20261125035921 at last check); they are corroboration, not release-160 confirmation. Where a used symbol's behavior is version-sensitive, only compile evidence is release-160-specific.

Used surface and status ("basis" states exactly what verified it):

| Symbol | Basis | Exact reference |
|---|---|---|
| `GeckoRuntime.create(context, settings)` | compile (160 AAR) + docs (163 snapshot) | [GeckoRuntime Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoRuntime.html) |
| `GeckoRuntimeSettings.Builder().remoteDebuggingEnabled(boolean).build()` | compile (160 AAR) + docs (163 snapshot) | [GeckoRuntimeSettings.Builder Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoRuntimeSettings.Builder.html); enabled only for debuggable builds |
| `GeckoSession.open(GeckoRuntime)`, `close()`, `reload()`, `loadUri(String)` | compile (160 AAR) + docs (163 snapshot) | [GeckoSession Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.html) |
| `GeckoSession.ProgressDelegate.onPageStart` / `onPageStop` | compile (160 AAR) + docs (163 snapshot) | [ProgressDelegate Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.ProgressDelegate.html) |
| `GeckoSession.ContentDelegate.onCrash`, `onKill`, `onFullScreen`, `onExternalResponse` + `WebResponse` | compile (160 AAR) + docs (163 snapshot) | [ContentDelegate Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.ContentDelegate.html); crash/kill handlers keep the load-state flags; external responses are deliberately unhandled |
| `GeckoSession.NavigationDelegate.onLoadRequest` -> `GeckoResult<AllowOrDeny>` + `LoadRequest` | compile (160 AAR) + docs (163 snapshot) | [NavigationDelegate Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.NavigationDelegate.html); always returns `allow()` |
| `GeckoSession.PermissionDelegate` (onContentPermissionRequest -> `GeckoResult<Integer>`; `ContentPermission.uri`/`.permission`; constants `PERMISSION_PERSISTENT_STORAGE`, `PERMISSION_AUTOPLAY_AUDIBLE`, `PERMISSION_AUTOPLAY_INAUDIBLE`, `VALUE_ALLOW`, `VALUE_DENY`; `onAndroidPermissionsRequest` + `Callback.reject()`; `onMediaPermissionRequest` + `MediaSource`, `MediaCallback.reject()`) | compile (160 AAR) + docs (163 snapshot) | [PermissionDelegate Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.PermissionDelegate.html) + [site-permissions guide](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html); game-origin-scoped allow, everything else denied |
| `GeckoView` (Compose host), `GeckoResult.allow()` / `fromValue()` | compile (160 AAR) + docs (163 snapshot) | [GeckoView Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoView.html), [GeckoResult Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoResult.html), [quick start](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html) |
| `WebExtensionController` / `WebExtension` reflection surface | compile (160 AAR) + reflection guards only; NOT confirmed against any official documentation | [WebExtensionController Javadoc](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/WebExtensionController.html), [WebExtensions consumer guide](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html); used by tests only; addon manager itself stays on hold (#23) |

Javadoc caveat: all documentation links describe the 163 mozilla-central snapshot, not 160. No behavioral differences were observed for the used subset, and compile evidence against the 160 AAR is authoritative for symbol existence; behavior-specific differences remain a what-to-watch item during device verification.

### Measured APK-size impact

Sizes from GitHub release asset metadata (`assets[].size`), fetched 2026-11-25:

| Release | Engine | APK bytes | MiB |
|---|---|---:|---:|
| v1.0.0 | Cefrium | 120,381,565 | 114.8 |
| v2.0.0 | GeckoView 160 | 96,334,724 | 91.9 |
| v2.1.0 | GeckoView 160 | 96,444,836 | 92.0 |
| v2.2.0 | GeckoView 160 | 96,451,277 | 92.0 |

The v1.0.0 -> v2.0.0 change of `24,046,841` bytes (-20.0%) is not a pure engine delta: that changeset also contains the launcher Home redesign and updater hardening. The GeckoView 160 releases since then are stable (within ~0.13 MiB across three builds). A dedicated engine-only size comparison between comparable verified builds is unavailable without rebuilding a Cefrium-based APK, and is not worth producing given the Cefrium code is removed.

## Runtime, session and UI lifecycle

- Keep one runtime per application process.
- Manage the game session lifecycle explicitly.
- Keep the existing GeckoSession while navigating to Home/settings and back; do not reload solely because the visible Compose destination changes.
- Scope immersive UI, gameplay orientation and keep-screen-on to gameplay; Home/settings restore normal system UI.
- Handle navigation, progress/load state, content events, crashes, fullscreen, permissions, prompts, media and external responses using the pinned API and actual game requirements.
- Do not grant broad site or Android permissions automatically. Keep permission decisions origin-scoped and user-mediated where required.
- Do not log cookies, tokens, authorization headers, full sensitive URLs or page content.

A successful build proves compilation and packaging only. It does not prove gameplay, media, permissions, profile persistence or correct lifecycle behavior.

## Profile and data safety

GeckoView uses its own app-private browser data. No supported Cefrium-to-GeckoView profile migration has been established.

- Do not copy Cefrium profile files into GeckoView.
- Do not delete or reset legacy Cefrium profile data as part of this migration.
- A fresh game login may be required.
- Verify cookies, LocalStorage, IndexedDB, Cache Storage, service workers and authentication across force-stop/restart and same-key app update on physical ARM64 hardware.
- Do not report the whole app-private data directory as profile-only size.
- Never clear user data during startup, update or recovery.

## Game compatibility test plan

Record device model, Android version, GeckoView version, beta commit, APK SHA-256 and result for every physical-device test.

| Area | Required result |
|---|---|
| Launch/login | Game opens and login completes |
| WebGL/WebGL2 | Game world renders without blank/black surfaces |
| WebAssembly | Required game modules load and execute |
| WebSockets | Login and live game traffic remain connected; reconnect works |
| Touch/input | Controls, drag, keyboard/IME and focus work |
| Fullscreen/orientation | Gameplay works; Home/settings restore system UI |
| Audio | Playback and pause/resume work and respect Android audio focus |
| Storage/auth | Cookies and site data persist across restart/update |
| Permissions/prompts | Required flows work and unrelated requests remain denied |
| Process recovery | Renderer/process loss does not corrupt profile/session state |
| Navigation | Reload, Back, Home, Return to game, redirects and errors behave correctly |
| Updater | Compatible differential and full APK fallback install with same identity/signature |

The owner has reported that the app and GeckoView work on an ARM64 device. No device model, Android version, exact commit/APK digest or individual test results are recorded, so this is not evidence for the full matrix or PR #43's new UI behavior. CI/emulator results do not replace physical ARM64 verification.

## Addon boundary

Issue #23 and all addon-manager work are on hold by owner instruction (2026-11-25). The previous XPI specification and audit remain historical reference only; do not investigate, assign or implement the manager until the owner explicitly approves resuming it. A WoW 3.3.5a addon ZIP/`.toc` package is not a Firefox WebExtension; GeckoView WebExtension support does not itself load game addons. Do not claim game addon injection.

## CI evidence and remaining work

- PR #43 exact-head unsigned validation passed in [run #386](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45600824024) for `505e4fe4b91e0fcfae5aecf58ef9d9376e5598d3`.
- PR #43 merged to beta at `dffca7e67ef4a8dd1bec8777c2c4c8b8a51a0210`.
- Post-merge signed beta run [#45601558602](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45601558602) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks, as recorded in merged PR #47.
- Published v2.1.0 tag run [#45603533787](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45603533787) built versionName 2.1.0 / versionCode 2001000; signature, established certificate, ARM64-only ABI, differential generation and asset upload/publish steps passed. The final draft-by-tag lookup failed HTTP 404; PR #54 later changed cleanup and verification to use the release ID output from the release action. PR #54 exact-head validation passed in run #45610110307; post-merge signed beta run [#45610518305](https://github.com/iNeonyne/LastHavenPlus/actions/runs/45610518305) passed build, unit tests, established signing-certificate verification and ARM64-only ABI checks.
- Physical ARM64 game/profile/UI/update verification remains unverified; CI does not replace the device matrix.

Remaining work:
1. Complete physical ARM64 game, profile, permission, media, navigation and updater tests.
2. Inspect the adaptive launcher icon in the built APK.
3. Reconcile the resolved `releaseRuntimeClasspath` (via `./gradlew app:dependencies`) with the POM inventory and the #99 catalog once a Gradle-capable environment is available, and package the MPL 2.0 notice text with the APK before any external distribution.
4. Exercise the release-ID draft cleanup/verification path and optional differential fallback failure cases on a future owner-authorized versioned release run; do not republish v2.1.0 for testing.

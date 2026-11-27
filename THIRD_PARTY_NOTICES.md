# Third-party notices

This inventory is reconciled against the resolved GeckoView 161 releaseRuntimeClasspath report and the exact unsigned ARM64 APK built in CI. It is a notice summary, not a relicensing statement. Each component remains under its own license or terms.

OPEN-SOURCE COMPONENTS

AndroidX and Jetpack Compose
- Components: AndroidX UI, activity, appcompat, browser, core, lifecycle, media, collection, and related transitive AndroidX modules in the resolved runtime graph.
- License: Apache License 2.0.
- Copyright: The Android Open Source Project and contributors.
- License text: the applicable Apache-2.0 text is included in the repository and app as `app/src/main/res/raw/apache_2_0.txt`.

Kotlin and Kotlin coroutines
- Components: Kotlin standard library and kotlinx.coroutines runtime modules.
- License: Apache License 2.0.
- Copyright: JetBrains s.r.o. and contributors.
- License text: the applicable Apache-2.0 text is included in the repository and app as `app/src/main/res/raw/apache_2_0.txt`.

Apache Commons
- Components include Commons Compress 1.28.0 and its resolved Commons Codec, Commons IO, and Commons Lang dependencies.
- License: Apache License 2.0.
- Copyright: The Apache Software Foundation and contributors.
- License text: the applicable Apache-2.0 text is included in the repository and app as `app/src/main/res/raw/apache_2_0.txt`.

Other runtime libraries
- Guava 33.3.1-android: Apache License 2.0, verified against the upstream `google/guava` source tag `v33.3.1` (`LICENSE` at that tag). The Maven POM does not declare a license; this source-tag evidence is recorded separately from Maven metadata.
- `failureaccess:1.0.2`: Apache License 2.0, verified in the upstream `google/guava` source tag `v33.3.1`: `futures/failureaccess/pom.xml` declares artifact version `1.0.2`, and the repository-root `LICENSE` is Apache-2.0. The Maven artifact POM itself has no `<licenses>` block; this is source-tree evidence, not Maven license metadata.
- `listenablefuture:9999.0-empty-to-avoid-conflict-with-guava` (selected instead of requested `1.0`): Gradle's conflict-placeholder coordinate; its POM has no `<licenses>` block. No independent license claim is made for this placeholder.
- SnakeYAML 2.2: Apache License 2.0; SnakeYAML contributors.
- JetBrains annotations and JSpecify: Apache License 2.0; respective contributors.
- License text: the applicable Apache-2.0 text is included in the repository and app as `app/src/main/res/raw/apache_2_0.txt`.

Mozilla GeckoView
- Component: org.mozilla.geckoview:geckoview-arm64-v8a:161.0.20261118180411.
- License: Mozilla Public License 2.0 (MPL-2.0).
- Copyright: Mozilla Foundation and contributors.
- The MPL-2.0 text is included in this application as mpl_2_0.txt. GeckoView also contains separately licensed third-party components; consult GeckoView's own about:license inventory and Mozilla's corresponding source notices. The project does not relicense GeckoView or its bundled components.


GeckoView bundled license inventory (161 Stable)
- The exact GeckoView 161 ARM64 APK includes Mozilla's `about:license` page at `assets/omni.ja` → `chrome/toolkit/content/global/license.html`.
- Its complete 94-entry license-name/path inventory is included in this app as `app/src/main/res/raw/geckoview_about_license_inventory.txt` and is visible in Settings → Licenses & third-party notices.
- Source HTML SHA-256: `036e7acd61cc5b7a4f88b6637d26269ffe20bb573a36b715bb98f31e7b70ccdf`. The inventory notes Mozilla's warning that some listed licenses may not apply to a particular product build/version; it does not claim every listed component is present in the launcher APK.
- The full upstream license HTML remains embedded in GeckoView's `omni.ja`; the in-app viewer also displays the complete MPL-2.0 and Apache-2.0 texts separately.

VENDOR SDK TERMS — NOT AN OPEN-SOURCE LICENSE

The resolved runtime graph includes:
- com.google.android.gms:play-services-fido:21.3.1
- com.google.android.gms:play-services-base:18.7.2
- com.google.android.gms:play-services-basement:18.9.0
- com.google.android.gms:play-services-tasks:18.3.2

These Google Play services artifacts are declared under Google's Android Software Development Kit License terms in their published Maven metadata. Those terms are not an OSI-approved open-source license. The project's All Rights Reserved notice does not apply to or change their terms. Review the current agreement and confirm distribution rights for these exact embedded artifacts before publishing or distributing an APK:
https://developer.android.com/studio/terms

The presence of these artifacts is recorded as a licensing and redistribution review gate, not as a legal conclusion. Do not remove, modify, or replace them without checking GeckoView's requirements and compatibility.

SCOPE AND VERIFICATION

- Original LastHavenPlus code and documentation are All Rights Reserved; see the root `LICENSE` and `NOTICE`.
- Third-party license texts are provided only for the corresponding third-party components.
- The hosted LastHaven game and all third-party components/assets retain their own terms.
- The exact unsigned APK was inspected: native libraries are packaged only under `lib/arm64-v8a`; the rights, third-party, Apache, MPL and GeckoView inventory resources are checked by PR CI. The resolved dependency graph is not proof that every listed module is packaged in the APK.
- No physical-device test or signed release verification is claimed.


# Third-party notices

This inventory is based on the resolved releaseRuntimeClasspath dependency report for this branch. It is a notice summary, not a relicensing statement. Each component remains under its own license or terms. The exact APK contents must be checked against the CI-built unsigned APK before this inventory is treated as complete.

OPEN-SOURCE COMPONENTS

AndroidX and Jetpack Compose
- Components: AndroidX UI, activity, appcompat, browser, core, lifecycle, media, collection, and related transitive AndroidX modules in the resolved runtime graph.
- License: Apache License 2.0.
- Copyright: The Android Open Source Project and contributors.
- License text: LICENSE (Apache-2.0).

Kotlin and Kotlin coroutines
- Components: Kotlin standard library and kotlinx.coroutines runtime modules.
- License: Apache License 2.0.
- Copyright: JetBrains s.r.o. and contributors.
- License text: LICENSE (Apache-2.0).

Apache Commons
- Components include Commons Compress 1.28.0 and its resolved Commons Codec, Commons IO, and Commons Lang dependencies.
- License: Apache License 2.0.
- Copyright: The Apache Software Foundation and contributors.
- License text: LICENSE (Apache-2.0).

Other runtime libraries
- Guava / failureaccess: Apache License 2.0; Google and contributors.
- SnakeYAML 2.2: Apache License 2.0; SnakeYAML contributors.
- JetBrains annotations and JSpecify: Apache License 2.0; respective contributors.
- License text: LICENSE (Apache-2.0).

Mozilla GeckoView
- Component: org.mozilla.geckoview:geckoview-arm64-v8a:160.0.20261116130455.
- License: Mozilla Public License 2.0 (MPL-2.0).
- Copyright: Mozilla Foundation and contributors.
- The MPL-2.0 text is included in this application as mpl_2_0.txt. GeckoView also contains separately licensed third-party components; consult GeckoView's own about:license inventory and Mozilla's corresponding source notices. The project does not relicense GeckoView or its bundled components.

VENDOR SDK TERMS — NOT AN OPEN-SOURCE LICENSE

The resolved runtime graph includes:
- com.google.android.gms:play-services-fido:21.3.1
- com.google.android.gms:play-services-base:18.7.2
- com.google.android.gms:play-services-basement:18.9.0
- com.google.android.gms:play-services-tasks:18.3.2

These Google Play services artifacts are declared under Google's Android Software Development Kit License terms in their published Maven metadata. Those terms are not an OSI-approved open-source license. The project Apache-2.0 license does not apply to them. Review the current agreement and confirm distribution rights for these exact embedded artifacts before publishing or distributing an APK:
https://developer.android.com/studio/terms

The presence of these artifacts is recorded as a licensing and redistribution review gate, not as a legal conclusion. Do not remove, modify, or replace them without checking GeckoView's requirements and compatibility.

SCOPE AND VERIFICATION

- The project LICENSE covers original LastHavenPlus code and documentation only.
- The hosted LastHaven game and all third-party components/assets retain their own terms.
- The resolved dependency graph is not proof that every listed module is packaged in the APK. CI uploads the unsigned PR APK for direct inspection; update this notice only from verified package contents.
- No physical-device test or signed release verification is claimed.


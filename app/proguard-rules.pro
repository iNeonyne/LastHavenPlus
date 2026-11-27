# GeckoView uses Java reflection and native-to-Java callbacks. Keep its public
# embedding API and bridge classes intact in minified release builds.
-keep class org.mozilla.geckoview.** { *; }
-keep class org.mozilla.gecko.** { *; }

# GeckoView's runtime configuration parser depends on SnakeYAML package names.
-keepnames class org.yaml.snakeyaml.**

# Commons Compress includes optional codecs that are not dependencies of this app.
# AddonArchive accepts only ZIP STORED/DEFLATED entries, so XZ and Zstandard
# streams are explicitly rejected before any codec implementation can be used.
-dontwarn org.tukaani.xz.**
-dontwarn com.github.luben.zstd.**

# GeckoView references Google FIDO2 types only in its optional WebAuthn/passkey path.
# The com.google.android.gms artifacts are intentionally excluded; suppress R8's
# missing-type warnings without keeping or packaging any Google Play services code.
-dontwarn com.google.android.gms.**

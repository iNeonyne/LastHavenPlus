# GeckoView uses Java reflection and native-to-Java callbacks. Keep its public
# embedding API and bridge classes intact in minified release builds.
-keep class org.mozilla.geckoview.** { *; }
-keep class org.mozilla.gecko.** { *; }

# GeckoView's runtime configuration parser depends on SnakeYAML package names.
-keepnames class org.yaml.snakeyaml.**

#!/bin/sh
# Local build script for LastHavenLauncher using SDK tools (no Gradle)
set -eu

PROJECT="app"
cd "$PROJECT"

MIN_SDK=29
TARGET_SDK=36
VERSION_CODE=1
VERSION_NAME=1.0.0
ANDROID_JAR="$HOME/android-sdk/platforms/android-36/android.jar"
PACKAGE="com.lasthaven.launcher"
KEYSTORE="$HOME/.android/debug.keystore"
KS_ALIAS=androiddebugkey
KS_PASS=android
export KS_PASS

need() { command -v "$1" >/dev/null 2>&1 || { echo "$1 not found"; exit 1; }; }
for tool in aapt2 javac jar d8 apksigner keytool kotlinc; do need "$tool"; done

[ -f src/main/AndroidManifest.xml ] || { echo "Manifest not found"; exit 1; }

rm -rf build
mkdir -p build/classes build/dex build/gen

# Compile resources
aapt2 compile --dir src/main/res -o build/res.zip

# Link resources and generate R.java
aapt2 link -I "$ANDROID_JAR" \
    --manifest src/main/AndroidManifest.xml \
    --java build/gen \
    --min-sdk-version "$MIN_SDK" --target-sdk-version "$TARGET_SDK" \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
    -o build/base.apk build/res.zip

# Find all Kotlin source files
find src/main/java -name '*.kt' > build/kotlin_sources.txt

# Compile Kotlin files
if [ -s build/kotlin_sources.txt ]; then
    CLASSPATH_JARS="$ANDROID_JAR"
    # Add AndroidX and Compose libraries if needed (they'll be resolved via Gradle normally)
    # For now, compile with just the Android jar - dependencies will be missing
    kotlinc -jvm-target 17 -no-stdlib -cp "$CLASSPATH_JARS" -d build/classes @build/kotlin_sources.txt
    jar cf build/classes.jar -C build/classes .
    d8 --min-api "$MIN_SDK" --lib "$ANDROID_JAR" --output build/dex build/classes.jar
fi

# Find all Java source files (for R.java etc.)
find src/main/java -name '*.java' > build/java_sources.txt
find build/gen -name '*.java' >> build/java_sources.txt

# Compile Java files
if [ -s build/java_sources.txt ]; then
    CLASSPATH_JARS="$ANDROID_JAR:build/classes.jar"
    javac -source 17 -target 17 -bootclasspath "$CLASSPATH_JARS" -Xlint:-options \
        -d build/classes @build/java_sources.txt
    jar cf build/classes.jar -C build/classes .
    d8 --min-api "$MIN_SDK" --lib "$ANDROID_JAR" --output build/dex build/classes.jar
fi

# Add DEX to APK
cp build/base.apk build/unsigned.apk
for dex in build/dex/classes*.dex; do
    [ -f "$dex" ] && jar --update --file build/unsigned.apk -C build/dex "$(basename "$dex")"
done

# Sign
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$(dirname "$KEYSTORE")"
    keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" \
        -alias "$KS_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

OUT="build/$PACKAGE.apk"
apksigner sign --ks "$KEYSTORE" --ks-key-alias "$KS_ALIAS" \
    --ks-pass env:KS_PASS --key-pass env:KS_PASS --out "$OUT" build/unsigned.apk
apksigner verify "$OUT"

echo "APK: $(pwd)/$OUT"
echo "Package: $PACKAGE"

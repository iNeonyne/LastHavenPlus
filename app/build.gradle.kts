plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---- Version policy (see README) -------------------------------------------
// App version: change only with project-owner approval.
val appVersionName = "1.0.0"
val appVersionCode = 1
// The bundled Addon Manager must stay at this version until the owner approves a change.
val expectedAddonManagerVersion = "0.11.0"

android {
    namespace = "com.lasthavens.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lasthavens.android"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }

    buildFeatures { buildConfig = true }

    // AAPT ignores asset directories that start with "_" by default, which would drop the
    // Addon Manager's "_locales" folder. This is the default pattern minus "<dir>_*".
    androidResources {
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }

    packaging { jniLibs { useLegacyPackaging = true } }

    val keystorePath: String? = System.getenv("KEYSTORE_PATH")
    val hasReleaseKey = keystorePath != null && file(keystorePath).exists()

    signingConfigs {
        create("release") {
            if (hasReleaseKey) {
                storeFile = file(keystorePath!!)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Falls back to the debug key so the APK is always installable.
            // Use a real keystore (see README) if you want in-place updates between builds.
            signingConfig = signingConfigs.getByName(if (hasReleaseKey) "release" else "debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("org.mozilla.geckoview:geckoview:${providers.gradleProperty("geckoViewVersion").get()}")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
}

// Fails the build if the bundled Addon Manager drifts from the approved version.
val verifyAddonManagerVersion = tasks.register("verifyAddonManagerVersion") {
    val manifest = file("src/main/assets/extensions/lh-addon-manager/manifest.json")
    doLast {
        val v = Regex("\"version\"\\s*:\\s*\"([^\"]+)\"").find(manifest.readText())?.groupValues?.get(1)
        check(v == expectedAddonManagerVersion) {
            "Addon Manager version is $v but must stay $expectedAddonManagerVersion until the project owner approves a change."
        }
    }
}
tasks.named("preBuild") { dependsOn(verifyAddonManagerVersion) }

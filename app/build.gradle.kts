import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersionName = "1.0.0"
val appVersionCode = 1

val keystorePath = System.getenv("KEYSTORE_PATH")
val keystoreFile = keystorePath?.let { file(it) }
val hasReleaseKey = keystoreFile?.exists() == true

extensions.configure<ApplicationExtension>("android") {
    namespace = "com.lasthavens.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.lasthavens.android"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }

    buildFeatures { buildConfig = true }

    // Preserve the extension's _locales directory in Android assets.
    androidResources {
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }

    packaging { jniLibs { useLegacyPackaging = true } }

    signingConfigs {
        create("release") {
            if (hasReleaseKey && keystoreFile != null) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Falls back to the debug key so an APK is always installable for testing.
            signingConfig = signingConfigs.getByName(if (hasReleaseKey) "release" else "debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("org.mozilla.geckoview:geckoview:${providers.gradleProperty("geckoViewVersion").get()}")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
}

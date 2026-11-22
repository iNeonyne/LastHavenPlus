plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersionName = "1.0.0"
val appVersionCode = 1

android {
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

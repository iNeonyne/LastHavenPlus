plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.cefrium") version "0.21.0"
}

android {
    namespace = "com.neonyne.lasthavenlauncher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.neonyne.lasthavenlauncher"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            // Keep debug builds easy to inspect; optimize only distributable builds.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation("com.cefrium:cefrium-sdk:0.21.0")
    implementation("com.cefrium:cefrium-compose:0.21.0")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.mediarouter:mediarouter:1.7.0")
    implementation("androidx.core:core:1.16.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("org.apache.commons:commons-compress:1.27.1")

    testImplementation("junit:junit:4.13.2")

    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
}

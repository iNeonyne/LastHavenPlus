plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

val releaseStoreFile = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseStorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")
val releaseSigningConfigured =
  !releaseStoreFile.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank() &&
    file(releaseStoreFile).isFile

android {
  namespace = "com.neonyne.lasthavenlauncher"
  compileSdk = 37
  compileSdkMinor = 2

  defaultConfig {
    applicationId = "com.neonyne.lasthavenlauncher"
    minSdk = 29
    targetSdk = 36
    versionCode = providers.gradleProperty("appVersionCode").orElse("1").get().toInt()
    versionName = providers.gradleProperty("appVersionName").orElse("0.1.0").get()

    ndk {
      abiFilters += "arm64-v8a"
    }
  }

  signingConfigs {
    if (releaseSigningConfigured) {
      create("release") {
        storeFile = file(releaseStoreFile!!)
        storePassword = releaseStorePassword
        keyAlias = "lasthavenplus"
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      if (releaseSigningConfigured) {
        signingConfig = signingConfigs.getByName("release")
      }
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
  implementation(libs.geckoview.arm64)

  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.mediarouter)
  implementation(libs.androidx.core)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.activity.compose)
  implementation(libs.commons.compress)

  testImplementation(libs.junit)

  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
}

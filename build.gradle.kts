plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.spotless)
}

spotless {
  ratchetFrom(providers.gradleProperty("spotlessRatchetFrom").orElse("origin/beta").get())

  kotlin {
    target("app/src/**/*.kt")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }

  kotlinGradle {
    target("*.gradle.kts", "app/*.gradle.kts")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }
}

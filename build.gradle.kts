plugins {
  id("com.android.application") version "9.4.0" apply false
  id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
  id("com.diffplug.spotless") version "8.12.0"
}

spotless {
  ratchetFrom(providers.gradleProperty("spotlessRatchetFrom").orElse("origin/beta").get())

  kotlin {
    target("app/src/**/*.kt")
    ktfmt("0.64").googleStyle()
  }

  kotlinGradle {
    target("*.gradle.kts", "app/*.gradle.kts")
    ktfmt("0.64").googleStyle()
  }
}

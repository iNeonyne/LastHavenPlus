# Cefrium ProGuard Rules
-keep class com.cefrium.** { *; }
-keep interface com.cefrium.** { *; }
-dontwarn com.cefrium.**

# Keep Cefrium Compose classes
-keep class com.cefrium.compose.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Java/Kotlin standard library
-keep class java.** { *; }
-keep class kotlin.** { *; }

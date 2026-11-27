package com.neonyne.lasthavenlauncher

import android.content.Context
import android.net.Uri
import java.io.File
import java.nio.file.Files

/** Removes updater-owned temporary files and obsolete finalized APKs without touching user files. */
internal object UpdateFileCleanup {
  internal const val PREFERENCES_NAME = "launcher_settings"
  internal const val KEY_PENDING_UPDATE_URI = "pending_update_uri"
  internal const val KEY_INSTALLER_HANDOFF_APK_NAME = "installer_handoff_apk_name"

  private const val FILE_PROVIDER_ROOT = "update_apks"
  private const val MAX_ENTRIES_PER_CLEANUP = 256
  private val managedApkName =
    Regex("""^LastHavenPlus-update-\d+-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\.apk$""")

  /** Returns only a validated APK filename from this app's expected FileProvider URI. */
  fun managedApkName(uri: Uri, packageName: String): String? {
    if (uri.scheme != "content" || uri.authority != "$packageName.updates") return null
    val segments = uri.pathSegments
    if (segments.size != 2 || segments[0] != FILE_PROVIDER_ROOT) return null
    return segments[1].takeIf(::isManagedApkName)
  }

  fun isManagedApkName(name: String): Boolean = managedApkName.matches(name)

  /** Protects the APK awaiting permission/installer handoff and the latest launched installer. */
  fun protectedApkNames(context: Context): Set<String> {
    val preferences =
      context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    val protected = linkedSetOf<String>()
    preferences.getString(KEY_INSTALLER_HANDOFF_APK_NAME, null)
      ?.takeIf(::isManagedApkName)
      ?.let(protected::add)
    preferences.getString(KEY_PENDING_UPDATE_URI, null)?.let { value ->
      runCatching { Uri.parse(value) }
        .getOrNull()
        ?.let { managedApkName(it, context.packageName) }
        ?.let(protected::add)
    }
    return protected
  }

  /**
   * Deletes abandoned partials and obsolete finalized updater APKs in one bounded directory scan.
   * Only direct regular files with updater-owned names are eligible; symlinks and subdirectories
   * are never followed or traversed.
   */
  fun removeObsoleteFiles(directory: File, protectedApkNames: Set<String> = emptySet()) {
    if (Files.isSymbolicLink(directory.toPath()) || !directory.isDirectory) return
    val canonicalDirectory = runCatching { directory.canonicalFile }.getOrNull() ?: return
    val entries = directory.listFiles() ?: return

    entries.take(MAX_ENTRIES_PER_CLEANUP).forEach { file ->
      if (Files.isSymbolicLink(file.toPath()) || !file.isFile) return@forEach
      val canonicalFile = runCatching { file.canonicalFile }.getOrNull() ?: return@forEach
      if (canonicalFile.parentFile != canonicalDirectory) return@forEach

      when {
        file.name.endsWith(".part") -> file.delete()
        isManagedApkName(file.name) && file.name !in protectedApkNames -> file.delete()
      }
    }
  }
}

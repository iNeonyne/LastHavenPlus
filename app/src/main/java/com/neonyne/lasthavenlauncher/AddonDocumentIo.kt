package com.neonyne.lasthavenlauncher

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Launchers for the Android Storage Access Framework.
 *
 * The selected URI is only used for the immediate user-requested operation; no broad storage
 * permission or persistent URI grant is needed.
 */
data class AddonDocumentPickers(
  val importZip: () -> Unit,
  val exportZip: (folder: String) -> Unit,
)

@Composable
fun rememberAddonDocumentPickers(
  onImportSelected: (Uri) -> Unit,
  onExportSelected: (String, Uri) -> Unit,
): AddonDocumentPickers {
  val latestImport by rememberUpdatedState(onImportSelected)
  val latestExport by rememberUpdatedState(onExportSelected)
  val pendingExportFolder = remember { mutableStateOf<String?>(null) }

  val importLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) latestImport(uri)
    }
  val exportLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) {
      uri ->
      val folder = pendingExportFolder.value
      pendingExportFolder.value = null
      if (uri != null && folder != null) latestExport(folder, uri)
    }

  return remember(importLauncher, exportLauncher) {
    AddonDocumentPickers(
      importZip = {
        importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed"))
      },
      exportZip = { folder ->
        pendingExportFolder.value = folder
        exportLauncher.launch("$folder.zip")
      },
    )
  }
}

/**
 * ContentResolver adapter for selected ZIP documents.
 *
 * Some document providers report an unknown length. In that case import still proceeds through the
 * archive reader's hard streaming byte limit; it never trusts an absent or fabricated size. Call
 * these methods from a worker thread, not the main thread.
 */
class AddonDocumentIo(
  context: Context,
  private val repository: AddonLibraryRepository,
) {
  private val resolver: ContentResolver = context.applicationContext.contentResolver

  fun importZip(uri: Uri): AddonLibrarySnapshot {
    val reportedSize = querySize(uri)
    val input =
      resolver.openInputStream(uri)
        ?: throw IllegalStateException("The selected ZIP could not be opened.")
    return repository.importZip(input, reportedSize)
  }

  fun exportZip(folder: String, destination: Uri) {
    val output =
      resolver.openOutputStream(destination, "wt")
        ?: throw IllegalStateException("The selected destination could not be opened.")
    repository.exportZip(folder, output)
  }

  private fun querySize(uri: Uri): Long? {
    val descriptorSize = runCatching {
      resolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
    }.getOrNull()
    if (descriptorSize != null && descriptorSize > 0L) return descriptorSize

    return runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
          if (!cursor.moveToFirst()) return@use null
          val index = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (index < 0 || cursor.isNull(index)) null else cursor.getLong(index).takeIf { it > 0L }
        }
      }
      .getOrNull()
  }
}

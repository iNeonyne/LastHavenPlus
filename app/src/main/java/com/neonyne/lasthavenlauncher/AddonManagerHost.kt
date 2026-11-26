package com.neonyne.lasthavenlauncher

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import java.io.File
import java.util.concurrent.Executors

/**
 * Stateful Android host for the local addon library. File-provider I/O and ZIP processing are
 * always performed off the main thread. The caller owns navigation into and out of this surface.
 */
@Composable
fun AddonManagerHost(onBack: () -> Unit) {
  val context = LocalContext.current
  val importSuccess = stringResource(R.string.addon_manager_import_success)
  val exportSuccess = stringResource(R.string.addon_manager_export_success)
  val exportFailed = stringResource(R.string.addon_manager_export_failed)
  val operationFailed = stringResource(R.string.addon_manager_operation_failed)
  val readFailed = stringResource(R.string.addon_manager_read_failed)
  val enabledMessage = stringResource(R.string.addon_manager_enabled)
  val disabledMessage = stringResource(R.string.addon_manager_disabled)
  val removeSuccess = stringResource(R.string.addon_manager_remove_success)
  val repository = remember(context) { AddonLibraryRepository(File(context.filesDir, "addons")) }
  val documentIo = remember(context, repository) { AddonDocumentIo(context, repository) }
  val executor = remember { Executors.newSingleThreadExecutor() }
  val mainHandler = remember { Handler(Looper.getMainLooper()) }

  var snapshot by remember {
    mutableStateOf(AddonLibrarySnapshot("loading", emptyList()))
  }
  var busy by remember { mutableStateOf(true) }
  var message by remember { mutableStateOf<String?>(null) }

  DisposableEffect(executor) {
    onDispose { executor.shutdown() }
  }

  val finishOperation: ((() -> AddonLibrarySnapshot), String) -> Unit =
    { operation, successMessage ->
      busy = true
      message = null
      executor.execute {
        val result = runCatching(operation)
        mainHandler.post {
          busy = false
          result
            .onSuccess {
              snapshot = it
              message = successMessage
            }
            .onFailure { error ->
              message = error.message ?: operationFailed
            }
        }
      }
    }

  val pickers =
    rememberAddonDocumentPickers(
      onImportSelected = { uri ->
        finishOperation(
          { documentIo.importZip(uri) },
          importSuccess,
        )
      },
      onExportSelected = { folder, uri ->
        busy = true
        message = null
        executor.execute {
          val result = runCatching {
            documentIo.exportZip(folder, uri)
            repository.readLibrary()
          }
          mainHandler.post {
            busy = false
            result
              .onSuccess {
                snapshot = it
                message = exportSuccess
              }
              .onFailure { error ->
                message = error.message ?: exportFailed
              }
          }
        }
      },
    )

  LaunchedEffect(repository) {
    executor.execute {
      val result = runCatching { repository.readLibrary() }
      mainHandler.post {
        busy = false
        result
          .onSuccess { snapshot = it }
          .onFailure { error ->
            message = error.message ?: readFailed
          }
      }
    }
  }

  AddonManagerScreen(
    snapshot = snapshot,
    busy = busy,
    message = message,
    onImport = pickers.importZip,
    onToggle = { folder, enabled ->
      finishOperation(
        { repository.setEnabled(folder, enabled) },
        if (enabled) enabledMessage
        else disabledMessage,
      )
    },
    onExport = pickers.exportZip,
    onRemove = { folder ->
      finishOperation(
        { repository.removeAddon(folder) },
        removeSuccess,
      )
    },
    onBack = onBack,
  )
}

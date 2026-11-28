package com.neonyne.lasthavenlauncher

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/**
 * Stateful Android host for the local addon library. File-provider I/O and ZIP processing are
 * always performed off the main thread. The caller owns navigation into and out of this surface.
 */
@Composable
fun AddonManagerHost(onBack: (libraryChanged: Boolean) -> Unit) {
  val context = LocalContext.current
  val importSuccess = stringResource(R.string.addon_manager_import_success)
  val importCancelled = stringResource(R.string.addon_manager_import_cancelled)
  val exportSuccess = stringResource(R.string.addon_manager_export_success)
  val exportFailed = stringResource(R.string.addon_manager_export_failed)
  val operationFailed = stringResource(R.string.addon_manager_operation_failed)
  val readFailed = stringResource(R.string.addon_manager_read_failed)
  val enabledMessage = stringResource(R.string.addon_manager_enabled)
  val disabledMessage = stringResource(R.string.addon_manager_disabled)
  val removeSuccess = stringResource(R.string.addon_manager_remove_success)
  val loadingLabel = stringResource(R.string.addon_manager_loading)
  val importingLabel = stringResource(R.string.addon_manager_importing)
  val copyingLabel = stringResource(R.string.addon_manager_copying)
  val validatingLabel = stringResource(R.string.addon_manager_validating)
  val extractingLabel = stringResource(R.string.addon_manager_extracting)
  val indexingLabel = stringResource(R.string.addon_manager_indexing)
  val committingLabel = stringResource(R.string.addon_manager_committing)
  val exportingLabel = stringResource(R.string.addon_manager_exporting)
  val savingLabel = stringResource(R.string.addon_manager_saving)
  val removingLabel = stringResource(R.string.addon_manager_removing)
  val repository = remember(context) { AddonLibraryRepository(File(context.filesDir, "addons")) }
  val documentIo = remember(context, repository) { AddonDocumentIo(context, repository) }
  val executor = remember { Executors.newSingleThreadExecutor() }
  val mainHandler = remember { Handler(Looper.getMainLooper()) }

  var snapshot by remember {
    mutableStateOf(AddonLibrarySnapshot("loading", emptyList()))
  }
  var busy by remember { mutableStateOf(true) }
  var message by remember { mutableStateOf<String?>(null) }
  var operationLabel by remember { mutableStateOf(loadingLabel) }
  var initialRevision by remember { mutableStateOf<String?>(null) }
  var libraryChanged by remember { mutableStateOf(false) }
  var replacementFolders by remember { mutableStateOf<List<String>?>(null) }
  var replacementDecision by remember { mutableStateOf<CompletableFuture<Boolean>?>(null) }

  DisposableEffect(executor) {
    onDispose { executor.shutdown() }
  }

  // Do not leave the manager mid-commit; archive and index transactions must finish first.
  BackHandler(enabled = busy) {}

  val finishOperation: ((() -> AddonLibrarySnapshot), String, String) -> Unit =
    { operation, successMessage, label ->
      busy = true
      operationLabel = label
      message = null
      executor.execute {
        val result = runCatching(operation)
        mainHandler.post {
          busy = false
          result
            .onSuccess {
              snapshot = it
              if (initialRevision == null || it.revision != initialRevision) {
                libraryChanged = true
              }
              message = successMessage
            }
            .onFailure { error ->
              message =
                if (error is java.util.concurrent.CancellationException) importCancelled
                else error.message ?: operationFailed
            }
        }
      }
    }

  val pickers =
    rememberAddonDocumentPickers(
      onImportSelected = { uri ->
        finishOperation(
          {
            documentIo.importZip(
              uri,
              onStage = { stage ->
                val label =
                  when (stage) {
                    AddonImportStage.COPYING_ARCHIVE -> copyingLabel
                    AddonImportStage.VALIDATING_ARCHIVE -> validatingLabel
                    AddonImportStage.EXTRACTING_FILES -> extractingLabel
                    AddonImportStage.INDEXING_ADDONS -> indexingLabel
                    AddonImportStage.COMMITTING -> committingLabel
                    AddonImportStage.COMPLETE -> importingLabel
                  }
                mainHandler.post { operationLabel = label }
              },
              confirmReplacement = { folders ->
                val decision = CompletableFuture<Boolean>()
                mainHandler.post {
                  replacementFolders = folders
                  replacementDecision = decision
                }
                decision.get()
              },
            )
          },
          importSuccess,
          importingLabel,
        )
      },
      onExportSelected = { folder, uri ->
        busy = true
        operationLabel = exportingLabel
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
          .onSuccess {
            snapshot = it
            initialRevision = it.revision
          }
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
        if (enabled) enabledMessage else disabledMessage,
        savingLabel,
      )
    },
    onExport = pickers.exportZip,
    onRemove = { folder ->
      finishOperation(
        { repository.removeAddon(folder) },
        removeSuccess,
        removingLabel,
      )
    },
    onBack = { onBack(libraryChanged) },
    reloadRequired = libraryChanged,
    operationLabel = operationLabel,
    replacementFolders = replacementFolders,
    onConfirmReplacement = {
      replacementDecision?.complete(true)
      replacementDecision = null
      replacementFolders = null
    },
    onCancelReplacement = {
      replacementDecision?.complete(false)
      replacementDecision = null
      replacementFolders = null
    },
  )
}

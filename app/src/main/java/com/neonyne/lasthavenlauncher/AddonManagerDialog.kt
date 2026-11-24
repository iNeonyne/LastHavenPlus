package com.neonyne.lasthavenlauncher

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddonManagerDialog(context: Context, onDismiss: () -> Unit) {
    val repository = remember(context) { AddonLibraryRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var addons by remember { mutableStateOf(emptyList<AddonLibraryItem>()) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingReplacement by remember { mutableStateOf<PendingAddonReplacement?>(null) }
    var removeTarget by remember { mutableStateOf<AddonLibraryItem?>(null) }
    var exportTargetId by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            addons = withContext(Dispatchers.IO) { repository.list() }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                busy = true
                message = null
                try {
                    val name = queryDisplayName(context, uri)
                    val stream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Android could not open the selected ZIP.")
                    when (val result = withContext(Dispatchers.IO) {
                        repository.importArchive(stream, name)
                    }) {
                        is AddonImportResult.Imported -> {
                            message = "Imported ${result.added} addon folder(s)."
                            refresh()
                        }
                        is AddonImportResult.RequiresConfirmation -> {
                            pendingReplacement = PendingAddonReplacement(
                                token = result.token,
                                archiveName = name,
                                folders = result.conflictingFolders,
                            )
                        }
                    }
                } catch (error: Exception) {
                    message = error.message ?: "Addon import failed."
                } finally {
                    busy = false
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val itemId = exportTargetId
        exportTargetId = null
        if (uri != null && itemId != null) {
            scope.launch {
                busy = true
                try {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Android could not create the export file.")
                    withContext(Dispatchers.IO) { repository.export(itemId, output) }
                    message = "Addon ZIP exported."
                } catch (error: Exception) {
                    message = error.message ?: "Addon export failed."
                } finally {
                    busy = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    AlertDialog(
        onDismissRequest = { if (!busy && pendingReplacement == null && removeTarget == null) onDismiss() },
        title = { Text("Addon manager") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Import and manage WoW 3.3.5a addon ZIP packages stored on this device. Export saves the original ZIP package, including any other addon folders it contains.")
                Text("Local enable/disable switches do not load addons into the game yet.")
                Button(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Import addon ZIP") }

                if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                message?.let { Text(it) }
                HorizontalDivider()
                if (addons.isEmpty()) {
                    Text("No addons imported.")
                } else {
                    val knownFolders = addons.map { it.folderName.lowercase() }.toSet()
                    addons.forEach { addon ->
                        val missing = addon.dependencies.filter { it.lowercase() !in knownFolders }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(addon.title)
                                Text(addon.folderName + (addon.version?.let { " · $it" } ?: ""))
                                Text("Package: ${addon.archiveName} · ${formatAddonBytes(addon.archiveBytes)}")
                                if (addon.interfaceVersions.isEmpty()) {
                                    Text("Compatibility: TOC has no Interface value.")
                                } else if (30300 !in addon.interfaceVersions) {
                                    Text("Compatibility warning: Interface ${addon.interfaceVersions.joinToString()} (expected 30300).")
                                } else {
                                    Text("Interface: 30300 (WoW 3.3.5a)")
                                }
                                if (missing.isNotEmpty()) {
                                    Text("Missing dependencies: ${missing.joinToString()}")
                                }
                                if (addon.optionalDependencies.isNotEmpty()) {
                                    Text("Optional: ${addon.optionalDependencies.joinToString()}")
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Local only")
                                        Switch(
                                            checked = addon.enabled,
                                            enabled = !busy,
                                            onCheckedChange = { enabled ->
                                                scope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        repository.setEnabled(addon.id, enabled)
                                                    }
                                                    refresh()
                                                }
                                            },
                                        )
                                    }
                                    TextButton(
                                        enabled = !busy,
                                        onClick = { exportTargetId = addon.id; exportLauncher.launch(addon.archiveName) },
                                    ) { Text("Export ZIP package") }
                                    TextButton(
                                        enabled = !busy,
                                        onClick = { removeTarget = addon },
                                    ) { Text("Remove") }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Packages are kept in app-private storage, separate from the Cefrium game profile.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, enabled = !busy) { Text("Done") }
        },
    )

    pendingReplacement?.let { pending ->
        AlertDialog(
            onDismissRequest = {
                scope.launch { withContext(Dispatchers.IO) { repository.cancelPendingImport(pending.token) } }
                pendingReplacement = null
            },
            title = { Text("Replace existing addon?") },
            text = { Text("The ZIP contains addon folder(s) already in your library: ${pending.folders.joinToString()}. Replace those local packages?") },
            confirmButton = {
                Button(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    repository.confirmReplacement(pending.token, pending.archiveName)
                                }
                                message = "Imported ${result.added} addon folder(s); replaced ${result.replaced}."
                                pendingReplacement = null
                                refresh()
                            } catch (error: Exception) {
                                message = error.message ?: "Addon replacement failed."
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) { Text("Replace") }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch { withContext(Dispatchers.IO) { repository.cancelPendingImport(pending.token) } }
                        pendingReplacement = null
                    },
                ) { Text("Cancel") }
            },
        )
    }

    removeTarget?.let { addon ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Remove addon?") },
            text = { Text("Remove ${addon.title} from the local library? The original ZIP package will be deleted if no other addon uses it.") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { repository.remove(addon.id) }
                        removeTarget = null
                        refresh()
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) { Text("Cancel") }
            },
        )
    }
}

private data class PendingAddonReplacement(
    val token: String,
    val archiveName: String,
    val folders: List<String>,
)

private fun queryDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index)
        }
    }
    return "addon.zip"
}

private fun formatAddonBytes(value: Long): String = when {
    value < 1024L -> "$value B"
    value < 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f KiB", value / 1024.0)
    else -> String.format(java.util.Locale.US, "%.1f MiB", value / (1024.0 * 1024.0))
}

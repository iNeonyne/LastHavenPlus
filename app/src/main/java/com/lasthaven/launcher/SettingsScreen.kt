package com.lasthaven.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val disposables = remember { CompositeDisposable() }

    // Settings state
    val gameUrl by viewModel.gameUrl
    var gameUrlInput by remember { mutableStateOf(gameUrl) }
    val fullscreen by viewModel.fullscreen
    var fullscreenInput by remember { mutableStateOf(fullscreen) }
    val orientation by viewModel.orientation
    var orientationInput by remember { mutableStateOf(orientation) }

    // Dialog states
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showEngineInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Game URL Section
            SettingsSection(title = stringResource(R.string.game_url)) {
                OutlinedTextField(
                    value = gameUrlInput,
                    onValueChange = { gameUrlInput = it },
                    label = { Text(stringResource(R.string.game_url)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Url
                    ),
                    keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                        onDone = {
                            viewModel.gameUrl = gameUrlInput
                            viewModel.setGameUrl(gameUrlInput).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                        }
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Divider()

            // Fullscreen Toggle
            SettingsSection(title = stringResource(R.string.fullscreen)) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.fullscreen))
                    Switch(
                        checked = fullscreenInput,
                        onCheckedChange = { checked ->
                            fullscreenInput = checked
                            viewModel.setFullscreen(checked).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                        },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Divider()

            // Orientation
            SettingsSection(title = stringResource(R.string.orientation)) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsStore.Orientation.values().forEach { orient ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .selectable(
                                    selected = orientationInput == orient,
                                    onClick = {
                                        orientationInput = orient
                                        viewModel.setOrientation(orient).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe()
                                    }
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(orient.labelRes))
                            if (orientationInput == orient) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Divider()

            // Actions Section
            SettingsSection(title = "Actions") {
                SettingsActionButton(
                    text = stringResource(R.string.clear_cache),
                    icon = Icons.Default.Refresh,
                    onClick = { showClearCacheDialog = true }
                )
                SettingsActionButton(
                    text = stringResource(R.string.clear_data),
                    icon = Icons.Default.DeleteForever,
                    isDestructive = true,
                    onClick = { showClearDataDialog = true }
                )
                SettingsActionButton(
                    text = stringResource(R.string.reload_game),
                    icon = Icons.Default.Refresh,
                    onClick = {
                        viewModel.cefriumState?.browser?.reload()
                    }
                )
            }

            Divider()

            // Info Section
            SettingsSection(title = "Information") {
                SettingsActionButton(
                    text = stringResource(R.string.engine_info),
                    icon = Icons.Default.Info,
                    onClick = { showEngineInfoDialog = true }
                )
                SettingsActionButton(
                    text = "${stringResource(R.string.app_version)} 1.0.0",
                    icon = Icons.Default.Info,
                    onClick = {}
                )
            }
        }
    }

    // Dialogs
    if (showClearCacheDialog) {
        ConfirmDialog(
            title = stringResource(R.string.clear_cache),
            message = stringResource(R.string.confirm_clear_cache),
            onConfirm = {
                clearCache(context)
                showClearCacheDialog = false
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }

    if (showClearDataDialog) {
        ConfirmDialog(
            title = stringResource(R.string.clear_data),
            message = stringResource(R.string.confirm_clear_data),
            isDestructive = true,
            onConfirm = {
                clearAllData(context)
                showClearDataDialog = false
            },
            onDismiss = { showClearDataDialog = false }
        )
    }

    if (showEngineInfoDialog) {
        EngineInfoDialog(onDismiss = { showEngineInfoDialog = false })
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
        content()
    }
}

@Composable
fun SettingsActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = if (isDestructive) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    androidx.compose.material3.Card(
        onClick = onClick,
        colors = colors,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 12.dp))
                Text(text = text, fontSize = 16.sp)
            }
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = if (isDestructive) {
                    androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            ) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.no))
            }
        }
    )
}

@Composable
fun EngineInfoDialog(onDismiss: () -> Unit) {
    val chromiumVersion = remember { Cefrium.getChromiumVersion() }
    val cefriumVersion = remember { Cefrium.getVersion() }
    val rendererServices = remember { Cefrium.getRendererServices() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.engine_info), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Cefrium SDK", cefriumVersion)
                InfoRow("Chromium Engine", chromiumVersion)
                InfoRow("Renderer Services", rendererServices.toString())
                InfoRow("GPU Process", if (rendererServices.gpuProcessEnabled) "Enabled" else "Disabled")
                InfoRow("Java Renderer", if (rendererServices.javaRenderer) "Yes" else "No")
                InfoRow("Renderer Count", rendererServices.rendererCount.toString())
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// Helper functions for clearing data
private fun clearCache(context: Context) {
    // Clear Cefrium cache (HTTP cache, not cookies/storage)
    // This is done via CefriumProfile.clearSiteData for the game URL
    // For now, we'll just clear the app's cache directory
    context.cacheDir.deleteRecursively()
    // Also clear Cefrium HTTP cache via JS injection (Phase 2)
}

private fun clearAllData(context: Context) {
    // Clear ALL Cefrium data - cookies, IndexedDB, localStorage, Cache Storage, service workers
    // This is the nuclear option - wipes the profile
    val profile = com.cefrium.CefriumProfile.getDefault()
    profile.clearSiteData("https://lasthavens.com") { }
    // Also delete the profile entirely for next start
    com.cefrium.CefriumProfile.delete("default")
    // Clear app cache
    context.cacheDir.deleteRecursively()
    // Clear app data (SharedPreferences, etc.) - requires app restart
    // For now, just clear what we can
}

private fun File.deleteRecursively(): Boolean {
    return if (isDirectory) {
        listFiles()?.all { it.deleteRecursively() } == true && delete()
    } else {
        delete()
    }
}

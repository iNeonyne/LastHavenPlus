package com.neonyne.lasthavenlauncher

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    context: Context,
    usageMeter: AppUsageMeter,
    isCheckingUpdate: Boolean,
    onBack: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenUpdates: () -> Unit,
    onCheckUpdates: () -> Unit,
) {
    var snapshot by remember { mutableStateOf<UsageSnapshot?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        refreshing = true
        snapshot = withContext(Dispatchers.IO) { usageMeter.snapshot() }
        refreshing = false
    }

    SettingsPageFrame(title = "Settings & usage", onBack = onBack) {
        PageSection(title = "Storage") {
            InfoLine("Installed APK", snapshot?.installedApkBytes?.let(::formatBytes) ?: "Measuring…")
            InfoLine(
                "Persistent app data",
                snapshot?.persistentDataBytes?.let(::formatBytes) ?: "Measuring…",
            )
            Text(
                "Includes app-private files and GeckoView data; it is not a profile-only measurement.",
                color = DesignTokens.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            InfoLine("App cache", snapshot?.cacheBytes?.let(::formatBytes) ?: "Measuring…")
            TextButton(onClick = { refreshKey++ }, enabled = !refreshing) {
                Text(if (refreshing) "Refreshing…" else "Refresh storage")
            }
        }

        PageSection(title = "Network usage") {
            InfoLine(
                "Received since device boot",
                snapshot?.receivedSinceBootBytes?.let(::formatBytes) ?: "Not supported",
            )
            InfoLine(
                "Sent since device boot",
                snapshot?.sentSinceBootBytes?.let(::formatBytes) ?: "Not supported",
            )
            InfoLine(
                "Received this app session",
                snapshot?.receivedThisSessionBytes?.let(::formatBytes) ?: "Not available",
            )
            InfoLine(
                "Sent this app session",
                snapshot?.sentThisSessionBytes?.let(::formatBytes) ?: "Not available",
            )
            Text(
                "Per-app totals include game and updater traffic. They are stored locally and reset on device reboot.",
                color = PageMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        PageSection(title = "Application") {
            Text("LastHavenPlus", color = DesignTokens.textPrimary, fontWeight = FontWeight.SemiBold)
            Text("Game engine: Mozilla GeckoView 160", color = PageMuted)
            OutlinedButton(
                onClick = onCheckUpdates,
                enabled = !isCheckingUpdate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isCheckingUpdate) "Checking for updates…" else "Check for updates")
            }
            TextButton(onClick = onOpenUpdates) { Text("Update details") }
            TextButton(onClick = onOpenDiagnostics) { Text("App diagnostics") }
        }
    }
}

@Composable
fun DiagnosticsScreen(
    context: Context,
    onBack: () -> Unit,
) {
    var content by remember { mutableStateOf(Diagnostics.read(context)) }
    SettingsPageFrame(title = "App diagnostics", onBack = onBack) {
        PageSection(title = "Local diagnostic log") {
            Text(
                "Logs are stored on this device. Review them before sharing.",
                color = PageMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DesignTokens.logSurface,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    content,
                    modifier = Modifier.padding(14.dp),
                    color = DesignTokens.textBody,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { content = Diagnostics.read(context) }) {
                    Text("Refresh")
                }
                OutlinedButton(onClick = {
                    Diagnostics.clear(context)
                    content = "Logs cleared."
                }) { Text("Clear logs") }
            }
            Button(
                onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "LastHavenPlus diagnostics")
                        putExtra(Intent.EXTRA_TEXT, content)
                    }
                    context.startActivity(Intent.createChooser(share, "Share diagnostic logs"))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Share logs") }
        }
    }
}

@Composable
fun UpdateScreen(
    update: AppUpdateInfo?,
    message: String?,
    isChecking: Boolean,
    isDownloading: Boolean,
    downloadingDelta: Boolean,
    onBack: () -> Unit,
    onDownload: (preferDelta: Boolean) -> Unit,
) {
    SettingsPageFrame(title = "App updates", onBack = onBack) {
        PageSection(title = "Update status") {
            when {
                isChecking -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Checking the stable release channel…", color = PageMuted)
                }
                update != null -> {
                    Text("LastHavenPlus ${update.version} is available.", color = Color.White)
                    if (update.patch != null) {
                        Text(
                            "A smaller differential package is available (${"%.1f".format(java.util.Locale.US, update.patch.size / 1024.0 / 1024.0)} MiB).",
                            color = PageMuted,
                        )
                    }
                    if (update.releaseNotes.isNotBlank()) {
                        Text(update.releaseNotes, color = PageMuted)
                    }
                    if (isDownloading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            if (downloadingDelta) "Downloading differential update…" else "Downloading full APK…",
                            color = PageMuted,
                        )
                    } else {
                        Button(
                            onClick = { onDownload(update.patch != null) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (update.patch != null) "Download smaller update" else "Download update")
                        }
                        if (update.patch != null) {
                            OutlinedButton(
                                onClick = { onDownload(false) },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Download full APK instead") }
                        }
                    }
                }
                else -> Text(message ?: "No update information yet. Check for updates from Settings.", color = PageMuted)
            }
        }
        if (message != null && update != null) {
            PageSection(title = "Download status") {
                Text(message, color = PageMuted)
            }
        }
    }
}

@Composable
private fun SettingsPageFrame(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.ink),
    ) {
        val widthDp = maxWidth.value.toInt()
        val maxContentWidth = LauncherLayoutPolicy.settingsContentMaxWidthDp(widthDp).dp
        val horizontalPadding = LauncherLayoutPolicy.pageHorizontalPaddingDp(widthDp).dp
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 18.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text("LASTHAVENPLUS", color = DesignTokens.gold, style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onBack) { Text("Back") }
            }
            content()
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PageSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesignTokens.panel),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, color = PageAccent, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = PageMuted, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatBytes(value: Long): String {
    if (value < 1024L) return "$value B"
    val units = listOf("KiB", "MiB", "GiB")
    var amount = value.toDouble()
    var index = -1
    do {
        amount /= 1024.0
        index++
    } while (amount >= 1024.0 && index < units.lastIndex)
    return String.format(java.util.Locale.US, "%.1f %s", amount, units[index])
}

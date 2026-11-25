package com.neonyne.lasthavenlauncher

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsDialog(context: Context, usageMeter: AppUsageMeter, onDismiss: () -> Unit) {
    var snapshot by remember { mutableStateOf<UsageSnapshot?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        refreshing = true
        snapshot = withContext(Dispatchers.IO) { usageMeter.snapshot() }
        refreshing = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings & usage") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Storage")
                Text("Installed APK: ${snapshot?.installedApkBytes?.let(::formatBytes) ?: "Measuring…"}")
                Text("Persistent app data (includes GeckoView): ${snapshot?.persistentDataBytes?.let(::formatBytes) ?: "Measuring…"}")
                Text("This measures app-private files, including GeckoView data; it is not a profile-only measurement.")
                Text("App cache: ${snapshot?.cacheBytes?.let(::formatBytes) ?: "Measuring…"}")
                Text("Network usage")
                Text("Received since device boot: ${snapshot?.receivedSinceBootBytes?.let(::formatBytes) ?: "Not supported"}")
                Text("Sent since device boot: ${snapshot?.sentSinceBootBytes?.let(::formatBytes) ?: "Not supported"}")
                Text("Received this app session: ${snapshot?.receivedThisSessionBytes?.let(::formatBytes) ?: "Not available"}")
                Text("Sent this app session: ${snapshot?.sentThisSessionBytes?.let(::formatBytes) ?: "Not available"}")
                Text("Network totals are per-app (game + updater), local-only, and reset when Android reboots. They are not game-only measurements.")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { refreshKey++ }, enabled = !refreshing) {
                    Text(if (refreshing) "Refreshing…" else "Refresh")
                }
                Button(onClick = onDismiss) { Text("Done") }
            }
        },
    )
}

@Composable
fun DiagnosticsDialog(context: Context, onDismiss: () -> Unit) {
    var content by remember { mutableStateOf(Diagnostics.read(context)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App & debug logs") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(content)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "LastHavenPlus diagnostics")
                        putExtra(Intent.EXTRA_TEXT, content)
                    }
                    context.startActivity(Intent.createChooser(share, "Share diagnostic logs"))
                }) { Text("Share") }
                TextButton(onClick = {
                    Diagnostics.clear(context)
                    content = "Logs cleared."
                }) { Text("Clear") }
                Button(onClick = onDismiss) { Text("Done") }
            }
        },
    )
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

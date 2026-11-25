package com.neonyne.lasthavenlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun GameMenuDialog(
    onReload: () -> Unit,
    onHome: () -> Unit,
    onReturnToGame: () -> Unit,
    onSettings: () -> Unit,
    onDiagnostics: () -> Unit,
    onCheckUpdates: () -> Unit,
    onExit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game menu") },
        text = { Text("Your current game session is kept when you return Home.") },
        confirmButton = {
            Button(onClick = onReload) { Text("Reload") }
        },
        dismissButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onHome) { Text("Home") }
                TextButton(onClick = onReturnToGame) { Text("Return to game") }
                TextButton(onClick = onSettings) { Text("Settings & usage") }
                TextButton(onClick = onDiagnostics) { Text("App diagnostics") }
                TextButton(onClick = onCheckUpdates) { Text("Check for updates") }
                TextButton(onClick = onExit) { Text("Exit app") }
            }
        },
    )
}

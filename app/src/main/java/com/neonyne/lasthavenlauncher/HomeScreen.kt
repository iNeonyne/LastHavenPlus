package com.neonyne.lasthavenlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onStartGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onExit: () -> Unit,
    onBackPressed: () -> Unit,
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var backPressTrigger by remember { mutableStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(backPressTrigger) {
        showExitDialog = true
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080B12)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "LastHavenPlus",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Wrath of the Lich King",
                fontSize = 18.sp,
                color = Color(0xFFB7C0D0),
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .height(56.dp),
            ) {
                Text(text = "Play", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .height(48.dp),
            ) {
                Text(text = "Settings & usage", fontSize = 16.sp)
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit LastHavenPlus?") },
            text = { Text("Your game data remains stored on this device.") },
            confirmButton = {
                Button(onClick = { showExitDialog = false; onExit() }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Cancel") }
            },
        )
    }

    androidx.compose.runtime.DisposableEffect(onBackPressed) {
        backPressTrigger = backPressTrigger + 1
        onDispose {}
    }
}

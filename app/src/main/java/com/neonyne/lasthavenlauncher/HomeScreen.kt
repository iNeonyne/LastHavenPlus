package com.neonyne.lasthavenlauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
  updateVersion: String?,
  onStartGame: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenUpdate: () -> Unit,
  onExit: () -> Unit,
) {
  var showExitDialog by remember { mutableStateOf(false) }
  BackHandler { showExitDialog = true }

  BoxWithConstraints(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(colors = DesignTokens.homeGradient()))
  ) {
    val availableWidth = maxWidth
    val availableHeight = maxHeight
    val wide = LauncherLayoutPolicy.usesWideHome(availableWidth.value.toInt())
    val contentWidth = if (wide) 1040.dp else 560.dp
    val verticalScroll = rememberScrollState()

    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      if (wide) {
        Row(
          modifier =
            Modifier.width(minOf(contentWidth, availableWidth - 48.dp))
              .height(availableHeight.coerceAtMost(720.dp))
              .verticalScroll(verticalScroll),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(36.dp),
        ) {
          HomeBranding(
            modifier = Modifier.weight(1.15f),
            compact = false,
          )
          HomeActions(
            modifier = Modifier.weight(0.85f),
            updateVersion = updateVersion,
            onStartGame = onStartGame,
            onOpenSettings = onOpenSettings,
            onOpenUpdate = onOpenUpdate,
          )
        }
      } else {
        Column(
          modifier =
            Modifier.width(minOf(contentWidth, availableWidth - 40.dp))
              .fillMaxSize()
              .verticalScroll(verticalScroll)
              .padding(vertical = 32.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          HomeBranding(compact = true)
          Spacer(Modifier.height(30.dp))
          HomeActions(
            updateVersion = updateVersion,
            onStartGame = onStartGame,
            onOpenSettings = onOpenSettings,
            onOpenUpdate = onOpenUpdate,
          )
        }
      }
    }
  }

  if (showExitDialog) {
    AlertDialog(
      onDismissRequest = { showExitDialog = false },
      title = { Text("Exit LastHavenPlus?") },
      text = { Text("Your game data remains stored on this device.") },
      confirmButton = {
        Button(
          onClick = {
            showExitDialog = false
            onExit()
          }
        ) {
          Text("Exit")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExitDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun HomeBranding(modifier: Modifier = Modifier, compact: Boolean) {
  Column(
    modifier = modifier,
    horizontalAlignment = if (compact) Alignment.CenterHorizontally else Alignment.Start,
  ) {
    Surface(
      modifier = Modifier.size(if (compact) 116.dp else 144.dp),
      shape = RoundedCornerShape(DesignTokens.cornerBadge),
      color = DesignTokens.panel,
      shadowElevation = 16.dp,
    ) {
      Image(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = "LastHavenPlus",
        contentScale = ContentScale.Fit,
        modifier = Modifier.padding(12.dp),
      )
    }
    Spacer(Modifier.height(24.dp))
    Text(
      "LASTHAVEN",
      color = DesignTokens.textPrimary,
      fontSize = if (compact) 34.sp else 46.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = if (compact) 3.sp else 5.sp,
      textAlign = if (compact) TextAlign.Center else TextAlign.Start,
    )
    Text(
      "PLUS",
      color = DesignTokens.frost,
      fontSize = if (compact) 18.sp else 22.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 8.sp,
      textAlign = if (compact) TextAlign.Center else TextAlign.Start,
    )
    Spacer(Modifier.height(10.dp))
    Text(
      "Return to Azeroth.",
      color = DesignTokens.textSecondary,
      fontSize = 16.sp,
      textAlign = if (compact) TextAlign.Center else TextAlign.Start,
    )
  }
}

@Composable
private fun HomeActions(
  modifier: Modifier = Modifier,
  updateVersion: String?,
  onStartGame: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenUpdate: () -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = DesignTokens.panel),
      shape = MaterialTheme.shapes.medium,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          "READY TO PLAY",
          color = DesignTokens.frost,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp,
        )
        Text(
          "Wrath of the Lich King",
          color = DesignTokens.textPrimary,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          "Your adventure continues where you left it.",
          color = DesignTokens.textSecondary,
          style = MaterialTheme.typography.bodyMedium,
        )
        val hapticView = LocalView.current
        Button(
          onClick = {
            // Haptic confirmation for the explicit Play action only.
            restrainedActionFeedback(hapticView)
            onStartGame()
          },
          modifier = Modifier.fillMaxWidth().height(58.dp),
          shape = RoundedCornerShape(DesignTokens.cornerCard),
          colors = DesignTokens.primaryButtonColors(),
        ) {
          Text("PLAY", fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
        OutlinedButton(
          onClick = onOpenSettings,
          modifier = Modifier.fillMaxWidth().height(52.dp),
          shape = RoundedCornerShape(DesignTokens.cornerCard),
        ) {
          Text("Settings & usage")
        }
      }
    }
    if (updateVersion != null) {
      Card(
        onClick = onOpenUpdate,
        colors = CardDefaults.cardColors(containerColor = DesignTokens.panelBright),
        shape = MaterialTheme.shapes.medium,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(Modifier.weight(1f)) {
            Text("Update available", color = DesignTokens.frost, fontWeight = FontWeight.Bold)
            Text("LastHavenPlus $updateVersion", color = DesignTokens.textPrimary)
          }
          Text("View", color = DesignTokens.frost, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

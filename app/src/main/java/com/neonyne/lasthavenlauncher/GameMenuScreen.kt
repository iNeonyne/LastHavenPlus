package com.neonyne.lasthavenlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GameMenuScreen(
  onReload: () -> Unit,
  onSettings: () -> Unit,
  onReturnToGame: () -> Unit,
) {
  val menuFocus = remember { FocusRequester() }
  LaunchedEffect(menuFocus) { menuFocus.requestFocus() }
  Box(
    modifier =
      Modifier.fillMaxSize().background(DesignTokens.scrim).semantics { paneTitle = "Game menu" },
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier =
        Modifier.widthIn(max = 360.dp)
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp)
          .semantics { isTraversalGroup = true },
      color = DesignTokens.panelOverlay,
      shape = RoundedCornerShape(DesignTokens.cornerDialog),
      tonalElevation = 12.dp,
      shadowElevation = 16.dp,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          "GAME MENU",
          modifier = Modifier.semantics { heading() },
          color = DesignTokens.frost,
          style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(2.dp))
        Button(
          onClick = onReload,
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = DesignTokens.minTouchTarget)
              .focusRequester(menuFocus),
          shape = RoundedCornerShape(DesignTokens.cornerButton),
          colors = DesignTokens.primaryButtonColors(),
        ) {
          Text("Reload", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
          onClick = onSettings,
          modifier = Modifier.fillMaxWidth().heightIn(min = DesignTokens.minTouchTarget),
          shape = RoundedCornerShape(DesignTokens.cornerButton),
        ) {
          Text("Settings")
        }
        OutlinedButton(
          onClick = onReturnToGame,
          modifier = Modifier.fillMaxWidth().heightIn(min = DesignTokens.minTouchTarget),
          shape = RoundedCornerShape(DesignTokens.cornerButton),
        ) {
          Text("Return to game")
        }
      }
    }
  }
}

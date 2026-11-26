package com.neonyne.lasthavenlauncher

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Non-persisted theme and layout previews for the #107 design system; no settings control. */
@Composable
private fun TokenShowcase(darkTheme: Boolean) {
  MaterialTheme(
    colorScheme = DesignTokens.colorScheme(darkTheme),
    shapes = DesignTokens.shapes,
    typography = DesignTokens.typography(),
  ) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(DesignTokens.spaceSection),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceLarge),
      ) {
        Text(
          "LASTHAVENPLUS",
          color = MaterialTheme.colorScheme.primary,
          style = MaterialTheme.typography.labelSmall,
        )
        Text(
          "Screen headline",
          color = MaterialTheme.colorScheme.onBackground,
          style = MaterialTheme.typography.headlineSmall,
        )
        Text(
          "Supporting copy",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(DesignTokens.spaceSection),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMedium),
          ) {
            Text(
              "Section heading",
              color = MaterialTheme.colorScheme.primary,
              style = MaterialTheme.typography.titleSmall,
            )
            Button(onClick = {}, colors = DesignTokens.primaryButtonColors()) {
              Text("Primary action")
            }
            OutlinedButton(onClick = {}) { Text("Secondary action") }
          }
        }
      }
    }
  }
}

@Preview(
  name = "Theme dark",
  showBackground = true,
  backgroundColor = 0xFF080D18,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
  name = "Theme light",
  showBackground = true,
  backgroundColor = 0xFFF4F8FC,
  uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
  name = "System dark",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  widthDp = 360,
  heightDp = 640,
)
@Preview(
  name = "System light",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_NO,
  widthDp = 360,
  heightDp = 640,
)
@Composable
private fun TokenShowcaseThemePreview() {
  TokenShowcase(darkTheme = isSystemInDarkTheme())
}

@Preview(name = "Overlay narrow (portrait phone)", widthDp = 360, heightDp = 640)
@Composable
private fun GameMenuNarrowPreview() {
  GameMenuScreen(onReload = {}, onSettings = {}, onAddonManager = {}, onReturnToGame = {})
}

@Preview(name = "Overlay wide (landscape window)", widthDp = 1040, heightDp = 620)
@Composable
private fun GameMenuWidePreview() {
  GameMenuScreen(onReload = {}, onSettings = {}, onReturnToGame = {})
}

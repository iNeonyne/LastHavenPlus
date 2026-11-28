package com.neonyne.lasthavenlauncher

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Smoke-tests the focused settings navigation and the requested metrics presentation. */
@RunWith(AndroidJUnit4::class)
class SettingsNavigationSmokeTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun settingsUsesSeparateGameNetworkAndApplicationDestinations() {
    composeRule.setContent {
      val context = LocalContext.current
      val usageMeter = remember(context) { AppUsageMeter(context) }
      MaterialTheme(
        colorScheme = DesignTokens.colorScheme(),
        typography = DesignTokens.typography(),
        shapes = DesignTokens.shapes,
      ) {
        SettingsScreen(
          context = context,
          usageMeter = usageMeter,
          isCheckingUpdate = false,
          gameUrl = DEFAULT_GAME_URL,
          onGameUrlChange = {},
          onBack = {},
          onOpenDiagnostics = {},
          onOpenUpdates = {},
          onCheckUpdates = {},
        )
      }
    }

    composeRule.onNodeWithText("Game", useUnmergedTree = true).assertIsDisplayed()
    composeRule.onNodeWithText("Game server").assertIsDisplayed()

    composeRule.onNodeWithText("Network & storage").performClick()
    composeRule.onNodeWithText("Game data size").assertIsDisplayed()
    composeRule.onNodeWithText("This app session").assertIsDisplayed()
    composeRule.onNodeWithText("Tracked total").assertIsDisplayed()
    composeRule.onNodeWithText("Usage updates automatically while this page is open.")
      .assertIsDisplayed()

    // The network page deliberately does not expose APK/cache/since-boot categories.
    composeRule.onNodeWithText("Installed APK").assertDoesNotExist()
    composeRule.onNodeWithText("Received since device boot").assertDoesNotExist()
    composeRule.onNodeWithText("Application").performClick()
    composeRule.onNodeWithText("Check for updates").assertIsDisplayed()
  }
}

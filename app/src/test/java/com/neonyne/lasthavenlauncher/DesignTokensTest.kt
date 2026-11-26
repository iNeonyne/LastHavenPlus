package com.neonyne.lasthavenlauncher

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTokensTest {
  private fun rgb(color: Color): Long = color.value.toLong() ushr 32 and 0xFFFFFFL

  @Test
  fun paletteHasExpectedBrandAnchors() {
    assertEquals(Color(0xFF080D18), DesignTokens.ink)
    assertEquals(Color(0xFFE5B85C), DesignTokens.gold)
    assertEquals(Color(0xFF121D2D), DesignTokens.panel)
    assertEquals(Color(0xFF17130A), DesignTokens.goldOnColor)
  }

  @Test
  fun primaryTextMeetsBodyContrastOnPanel() {
    assertTrue(contrastRatio(rgb(DesignTokens.textPrimary), rgb(DesignTokens.panel)) >= 4.5)
  }

  @Test
  fun secondaryTextMeetsBodyContrastOnPanelAndBackground() {
    assertTrue(contrastRatio(rgb(DesignTokens.textSecondary), rgb(DesignTokens.panel)) >= 4.5)
    assertTrue(contrastRatio(rgb(DesignTokens.textSecondary), rgb(DesignTokens.ink)) >= 4.5)
  }

  @Test
  fun diagnosticBodyTextMeetsContrastOnLogSurface() {
    assertTrue(contrastRatio(rgb(DesignTokens.textBody), rgb(DesignTokens.logSurface)) >= 4.5)
  }

  @Test
  fun primaryButtonLabelMeetsContrastOnGold() {
    assertTrue(contrastRatio(rgb(DesignTokens.goldOnColor), rgb(DesignTokens.gold)) >= 4.5)
  }

  @Test
  fun contrastHelperMatchesBlackAndWhiteReference() {
    assertEquals(21.0, contrastRatio(0xFFFFFFL, 0x000000L), 0.01)
    assertEquals(1.0, contrastRatio(0x808080L, 0x808080L), 0.01)
  }
}

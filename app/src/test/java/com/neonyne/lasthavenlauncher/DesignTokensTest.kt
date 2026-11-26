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
    assertEquals(Color(0xFF9BDCF5), DesignTokens.frost)
    assertEquals(Color(0xFF0A1621), DesignTokens.frostOnColor)
    assertEquals(Color(0xFF0A5E84), DesignTokens.frostDeep)
    assertEquals(Color(0xFF121D2D), DesignTokens.panel)
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
  fun accentMeetsHeadlineContrastOnPanelAndBackground() {
    assertTrue(contrastRatio(rgb(DesignTokens.frost), rgb(DesignTokens.panel)) >= 8.0)
    assertTrue(contrastRatio(rgb(DesignTokens.frost), rgb(DesignTokens.ink)) >= 8.0)
    assertTrue(contrastRatio(rgb(DesignTokens.frost), rgb(DesignTokens.panelBright)) >= 7.0)
  }

  @Test
  fun primaryButtonLabelMeetsContrastOnFrost() {
    assertTrue(contrastRatio(rgb(DesignTokens.frostOnColor), rgb(DesignTokens.frost)) >= 8.0)
  }

  @Test
  fun lightSchemeAccentMeetsContrastOnLightSurfaces() {
    assertTrue(contrastRatio(rgb(DesignTokens.frostDeep), 0xF4F8FCL) >= 4.5)
    assertTrue(contrastRatio(rgb(DesignTokens.frostDeep), 0xE7F0F7L) >= 4.5)
    assertTrue(contrastRatio(0xFFFFFFFCL, rgb(DesignTokens.frostDeep)) >= 4.5)
  }

  @Test
  fun contrastHelperMatchesBlackAndWhiteReference() {
    assertEquals(21.0, contrastRatio(0xFFFFFFL, 0x000000L), 0.01)
    assertEquals(1.0, contrastRatio(0x808080L, 0x808080L), 0.01)
  }
}

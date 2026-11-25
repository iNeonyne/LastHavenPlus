package com.neonyne.lasthavenlauncher

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTokensTest {
  private fun rgb(color: Color): Long = color.value.toLong() ushr 32 and 0xFFFFFFL

  @Test
  fun sharedPaletteIsSingleSourceOfTruth() {
    assertEquals(Color(0xFF080D18), DesignTokens.ink)
    assertEquals(Color(0xFFE5B85C), DesignTokens.gold)
    assertEquals(Color(0xFF121D2D), DesignTokens.panel)
    assertEquals(Color(0xFF17130A), DesignTokens.goldOnColor)
  }

  @Test
  fun homeGradientUsesInkAsMiddleStop() {
    val stops = DesignTokens.homeGradient()
    assertEquals(3, stops.size)
    assertEquals(DesignTokens.ink, stops[1])
  }

  @Test
  fun primaryTextOnPanelMeetsContrast() {
    val ratio = contrastRatio(0xFFFFFFFFL, rgb(DesignTokens.panel))
    assertTrue("Primary text on panel contrast $ratio below 4.5", ratio >= 4.5)
  }

  @Test
  fun secondaryTextOnPanelMeetsContrast() {
    val ratio = contrastRatio(rgb(DesignTokens.textSecondary), rgb(DesignTokens.panel))
    assertTrue("Secondary text on panel contrast $ratio below 4.5", ratio >= 4.5)
  }

  @Test
  fun secondaryTextOnPageBackgroundMeetsContrast() {
    val ratio = contrastRatio(rgb(DesignTokens.textSecondary), rgb(DesignTokens.ink))
    assertTrue("Secondary text on ink contrast $ratio below 4.5", ratio >= 4.5)
  }

  @Test
  fun bodyTextOnLogSurfaceMeetsContrast() {
    val ratio = contrastRatio(rgb(DesignTokens.textBody), rgb(DesignTokens.logSurface))
    assertTrue("Body text on log surface contrast $ratio below 4.5", ratio >= 4.5)
  }

  @Test
  fun buttonLabelOnGoldMeetsContrast() {
    val ratio = contrastRatio(rgb(DesignTokens.goldOnColor), rgb(DesignTokens.gold))
    assertTrue("Button label on gold contrast $ratio below 4.5", ratio >= 4.5)
  }

  @Test
  fun goldAccentOnInkIsAtLeastLargeTextContrast() {
    val ratio = contrastRatio(rgb(DesignTokens.gold), rgb(DesignTokens.ink))
    assertTrue("Gold accent on ink contrast $ratio below 3.0", ratio >= 3.0)
  }

  @Test
  fun contrastHelperMatchesWcagPinnedValues() {
    assertEquals(21.0, contrastRatio(0xFFFFFFFFL, 0x000000L), 0.01)
    assertEquals(1.0, contrastRatio(0x808080L, 0x808080L), 0.01)
  }

  @Test
  fun scrimIsSemiTransparentBlack() {
    assertEquals(0x58000000L, DesignTokens.scrim.value.toLong() ushr 32)
  }
}

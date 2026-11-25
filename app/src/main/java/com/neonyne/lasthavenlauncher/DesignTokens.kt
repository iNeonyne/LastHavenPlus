package com.neonyne.lasthavenlauncher

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * LastHavenPlus visual design tokens (#107). Restrained dark interface with LastHaven gold
 * accents. Launcher-owned screens must consume these instead of inline colors so contrast,
 * spacing and shapes stay consistent; the game itself is never styled from here.
 */
object DesignTokens {
  val ink = Color(0xFF080D18)
  val gradientTop = Color(0xFF17263A)
  val gradientBottom = Color(0xFF05080F)
  val panel = Color(0xFF121D2D)
  val panelBright = Color(0xFF1B2A3E)
  val panelOverlay = Color(0xF2121D2D)
  val logSurface = Color(0xFF080B12)
  val scrim = Color(0x58000000)
  val gold = Color(0xFFE5B85C)
  val goldOnColor = Color(0xFF17130A)
  val textPrimary = Color(0xFFFFFFFF)
  val textSecondary = Color(0xFFAAB7C9)
  val textBody = Color(0xFFD7E0EC)

  val cornerCard = 16.dp
  val cornerButton = 14.dp
  val cornerDialog = 24.dp
  val cornerBadge = 32.dp

  val spaceSmall = 8.dp
  val spaceMedium = 12.dp
  val spaceLarge = 16.dp
  val spaceSection = 18.dp
  val spaceScreen = 28.dp
  val minTouchTarget = 48.dp

  val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(cornerButton),
    medium = RoundedCornerShape(cornerCard),
    large = RoundedCornerShape(cornerDialog),
    extraLarge = RoundedCornerShape(cornerBadge),
  )

  fun colorScheme() = darkColorScheme(
    primary = gold,
    onPrimary = goldOnColor,
    secondary = gold,
    onSecondary = goldOnColor,
    background = ink,
    onBackground = textPrimary,
    surface = panel,
    onSurface = textPrimary,
    surfaceVariant = panelBright,
    onSurfaceVariant = textSecondary,
    error = Color(0xFFED8796),
  )

  fun homeGradient() =
    listOf(gradientTop, ink, gradientBottom)
}

/**
 * Restrained haptic feedback for explicit launcher-owned actions (Play, Download) only.
 * Delegates to the system haptic setting; never fires for passive state changes.
 */
fun restrainedActionFeedback(view: View) {
  val constant = if (android.os.Build.VERSION.SDK_INT >= 30) {
    HapticFeedbackConstants.CONFIRM
  } else {
    HapticFeedbackConstants.VIRTUAL_KEY
  }
  view.performHapticFeedback(constant)
}

/**
 * WCAG 2.x relative contrast ratio helper. Exposed for the design-token contrast tests so
 * color pair accessibility is checked mechanically instead of by eye.
 */
fun relativeLuminance(color: Long): Double {
  fun channel(value: Long): Double {
    val scaled = value / 255.0
    return if (scaled <= 0.04045) scaled / 12.92 else Math.pow((scaled + 0.055) / 1.055, 2.4)
  }

  val red = channel(color shr 16 and 0xFF)
  val green = channel(color shr 8 and 0xFF)
  val blue = channel(color and 0xFF)
  return 0.2126 * red + 0.7152 * green + 0.0722 * blue
}

fun contrastRatio(foreground: Long, background: Long): Double {
  val first = relativeLuminance(foreground)
  val second = relativeLuminance(background)
  val lighter = maxOf(first, second)
  val darker = minOf(first, second)
  return (lighter + 0.05) / (darker + 0.05)
}

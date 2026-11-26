package com.neonyne.lasthavenlauncher

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared palette, spacing and shape tokens for launcher-owned UI only. */
object DesignTokens {
  val ink = Color(0xFF080D18)
  val gradientTop = Color(0xFF17263A)
  val gradientBottom = Color(0xFF05080F)
  val panel = Color(0xFF121D2D)
  val panelBright = Color(0xFF26364B)
  val panelOverlay = Color(0xF2121D2D)
  val logSurface = Color(0xFF080B12)
  val scrim = Color(0x58000000)
  val frost = Color(0xFF9BDCF5)
  val frostOnColor = Color(0xFF0A1621)
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

  val shapes =
    Shapes(
      extraSmall = RoundedCornerShape(8.dp),
      small = RoundedCornerShape(cornerButton),
      medium = RoundedCornerShape(cornerCard),
      large = RoundedCornerShape(cornerDialog),
      extraLarge = RoundedCornerShape(cornerBadge),
    )

  fun colorScheme() =
    darkColorScheme(
      primary = frost,
      onPrimary = frostOnColor,
      secondary = frost,
      onSecondary = frostOnColor,
      background = ink,
      onBackground = textPrimary,
      surface = panel,
      onSurface = textPrimary,
      surfaceVariant = panelBright,
      onSurfaceVariant = textSecondary,
      error = Color(0xFFED8796),
    )

  @Composable
  fun primaryButtonColors() =
    ButtonDefaults.buttonColors(containerColor = frost, contentColor = frostOnColor)

  fun typography(): Typography {
    val base = Typography()
    return base.copy(
      headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
      titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
      labelSmall = base.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold),
    )
  }

  fun homeGradient() = listOf(gradientTop, ink, gradientBottom)
}

/** System-preference-respecting haptic feedback for explicit primary actions only. */
fun restrainedActionFeedback(view: View) {
  val constant =
    if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
    else HapticFeedbackConstants.VIRTUAL_KEY
  view.performHapticFeedback(constant)
}

/** WCAG relative luminance helper used by the design-token contrast tests. */
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
  return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
}

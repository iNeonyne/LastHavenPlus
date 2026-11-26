package com.neonyne.lasthavenlauncher

/**
 * Motion preferences for the in-game menu overlay. Respects the system "remove animations"
 * accessibility setting (zero animator duration scale).
 */
internal object OverlayMotionPolicy {
  const val FADE_IN_MILLIS = 160
  const val FADE_OUT_MILLIS = 120

  fun isReducedMotion(animatorDurationScale: Float): Boolean = animatorDurationScale == 0f

  fun fadeInMillis(animatorDurationScale: Float): Int =
    if (isReducedMotion(animatorDurationScale)) 0 else FADE_IN_MILLIS

  fun fadeOutMillis(animatorDurationScale: Float): Int =
    if (isReducedMotion(animatorDurationScale)) 0 else FADE_OUT_MILLIS
}

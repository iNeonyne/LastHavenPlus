package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayMotionPolicyTest {
  @Test
  fun `reduced motion is triggered only by zero animator scale`() {
    assertTrue(OverlayMotionPolicy.isReducedMotion(0f))
    assertFalse(OverlayMotionPolicy.isReducedMotion(1f))
    assertFalse(OverlayMotionPolicy.isReducedMotion(0.5f))
    assertFalse(OverlayMotionPolicy.isReducedMotion(2f))
  }

  @Test
  fun `normal animator scale keeps the shipped overlay fades`() {
    assertEquals(160, OverlayMotionPolicy.fadeInMillis(1f))
    assertEquals(120, OverlayMotionPolicy.fadeOutMillis(1f))
    assertEquals(160, OverlayMotionPolicy.FADE_IN_MILLIS)
    assertEquals(120, OverlayMotionPolicy.FADE_OUT_MILLIS)
  }

  @Test
  fun `reduced motion collapses both fades to zero`() {
    assertEquals(0, OverlayMotionPolicy.fadeInMillis(0f))
    assertEquals(0, OverlayMotionPolicy.fadeOutMillis(0f))
  }

  @Test
  fun `partial and amplified animator scales keep default durations`() {
    assertEquals(160, OverlayMotionPolicy.fadeInMillis(0.5f))
    assertEquals(120, OverlayMotionPolicy.fadeOutMillis(0.5f))
    assertEquals(160, OverlayMotionPolicy.fadeInMillis(2f))
    assertEquals(120, OverlayMotionPolicy.fadeOutMillis(2f))
  }
}

package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateProgressTest {
  private var nowNanos = 0L
  private val tracker = UpdateProgressTracker { nowNanos }
  private val nanosPerSecond = UpdateProgressTracker.NANOS_PER_SECOND

  @Test
  fun `percentage requires a trustworthy total`() {
    val state = tracker.snapshot()
    assertNull(state.percent)
    assertFalse(state.totalKnown)

    tracker.onTotalBytes(contentLength = -1L, manifestSize = 0L)
    assertNull(tracker.snapshot().percent)

    tracker.onTotalBytes(contentLength = -1L, manifestSize = 2_000L)
    tracker.onBytesWritten(1_000L)
    val stateWithTotal = tracker.snapshot()
    assertTrue(stateWithTotal.totalKnown)
    assertEquals(50, stateWithTotal.percent)
    assertEquals(1_000L, stateWithTotal.remainingBytes)
  }

  @Test
  fun `manifest size is used when the server omits content length`() {
    tracker.onTotalBytes(contentLength = -1L, manifestSize = 4_000L)
    tracker.onBytesWritten(500L)
    assertEquals(12, tracker.snapshot().percent)
  }

  @Test
  fun `unknown total falls back to indeterminate with no eta`() {
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = -1L, manifestSize = -1L)
    tracker.onBytesWritten(1_000L)
    nowNanos += nanosPerSecond
    tracker.onBytesWritten(1_000L)

    val state = tracker.snapshot()
    assertNull(state.percent)
    assertNull(state.remainingBytes)
    assertNull(state.estimatedSecondsRemaining)
    assertNotNull(state.estimatedBytesPerSecond)
    assertEquals(1_000L, state.estimatedBytesPerSecond)
    assertEquals(2_000L, state.receivedBytes)
    assertEquals(state, tracker.snapshot())
  }

  @Test
  fun `speed and eta only appear after a sufficient sample window`() {
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = 4_000L, manifestSize = 4_000L)
    tracker.onBytesWritten(100L)
    nowNanos += 100_000_000L
    tracker.onBytesWritten(200L)
    var snapshot = tracker.snapshot()
    assertNull(snapshot.estimatedBytesPerSecond)
    assertNull(snapshot.estimatedSecondsRemaining)

    nowNanos += 500_000_000L
    tracker.onBytesWritten(400L)
    snapshot = tracker.snapshot()
    val rate = checkNotNull(snapshot.estimatedBytesPerSecond)
    val eta = checkNotNull(snapshot.estimatedSecondsRemaining)
    assertEquals(1_000L, rate)
    assertEquals((4_000L - 700L + rate - 1) / rate, eta)
  }

  @Test
  fun `rate stays measurable when updates arrive far faster than the sample window`() {
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = 1_000_000L, manifestSize = 1_000_000L)

    // 200 updates 1 ms apart: far more than MAX_SAMPLE_COUNT inside the 500 ms window.
    repeat(200) {
      nowNanos += 1_000_000L
      tracker.onBytesWritten(1_000L)
    }
    val duringBurst = tracker.snapshot()
    assertNull(duringBurst.estimatedBytesPerSecond)
    assertNull(duringBurst.estimatedSecondsRemaining)

    // Sustaining the same cadence past 500 ms must produce a measured rate and ETA, not stay null.
    repeat(400) {
      nowNanos += 1_000_000L
      tracker.onBytesWritten(1_000L)
    }
    val state = tracker.snapshot()
    val rate = checkNotNull(state.estimatedBytesPerSecond)
    val eta = checkNotNull(state.estimatedSecondsRemaining)
    assertEquals(1_000_000L, rate)
    assertEquals(600_000L, state.receivedBytes)
    assertEquals((state.remainingBytes!! + rate - 1) / rate, eta)
    assertEquals(1L, eta)
  }

  @Test
  fun `stage transition out of download drops stale rate estimates`() {
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = 10_000L, manifestSize = 10_000L)
    tracker.onBytesWritten(1_000L)
    nowNanos += nanosPerSecond
    tracker.onBytesWritten(2_000L)
    assertNotNull(tracker.snapshot().estimatedBytesPerSecond)

    tracker.onStageChanged(UpdateStage.VERIFYING)
    val verifying = tracker.snapshot()
    assertEquals(UpdateStage.VERIFYING, verifying.stage)
    assertNull(verifying.estimatedBytesPerSecond)
    assertNull(verifying.estimatedSecondsRemaining)
  }

  @Test
  fun `retry resets bytes and samples and bumps the attempt counter`() {
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = 10_000L, manifestSize = 10_000L)
    tracker.onBytesWritten(1_000L)
    nowNanos += nanosPerSecond
    tracker.onBytesWritten(1_000L)

    tracker.onAttemptStarted(2)
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    val retried = tracker.snapshot()
    assertEquals(2, retried.downloadAttempt)
    assertEquals(0L, retried.receivedBytes)
    assertEquals(0, retried.percent)
    assertNull(retried.estimatedBytesPerSecond)
    assertNull(retried.failureMessage)
  }

  @Test
  fun `fallback to full is observable and survives later stage changes`() {
    tracker.onStageChanged(UpdateStage.DIFFERENTIAL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = 300L, manifestSize = 300L)
    tracker.onFallbackToFull()
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onAttemptStarted(1)

    val state = tracker.snapshot()
    assertEquals(UpdateStage.FULL_DOWNLOAD, state.stage)
    assertTrue(state.fallbackToFull)
  }

  @Test
  fun `failure keeps the backend message and only in the failed stage`() {
    tracker.onStageChanged(UpdateStage.FAILED, failureMessage = "Connection timed out")
    val failed = tracker.snapshot()
    assertEquals(UpdateStage.FAILED, failed.stage)
    assertEquals("Connection timed out", failed.failureMessage)

    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onAttemptStarted(2)
    val recovered = tracker.snapshot()
    assertNull(recovered.failureMessage)
  }

  @Test
  fun `completion reaches ready with full payload`() {
    tracker.onStageChanged(UpdateStage.FULL_DOWNLOAD)
    tracker.onTotalBytes(contentLength = 2_000L, manifestSize = 2_000L)
    tracker.onBytesWritten(2_000L)
    tracker.onStageChanged(UpdateStage.VERIFYING)
    tracker.onStageChanged(UpdateStage.READY)
    val ready = tracker.snapshot()
    assertEquals(UpdateStage.READY, ready.stage)
    assertEquals(100, ready.percent)
    assertNull(ready.failureMessage)
    assertNull(ready.estimatedBytesPerSecond)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `negative byte deltas are rejected`() {
    tracker.onBytesWritten(-1L)
  }
}

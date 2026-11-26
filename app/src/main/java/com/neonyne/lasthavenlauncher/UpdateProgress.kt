package com.neonyne.lasthavenlauncher

/**
 * Honest, observable updater progress. All values are measured: the total is reported only when a
 * trustworthy length is known, percentage only with a known total, and speed/ETA only when enough
 * transfer samples exist. Stages mirror the real pipeline (delta download, fallback to a full
 * download, verification, ready for user-confirmed installation, recoverable failure).
 */
internal enum class UpdateStage {
  IDLE,
  DIFFERENTIAL_DOWNLOAD,
  FULL_DOWNLOAD,
  VERIFYING,
  READY,
  FAILED,
}

/**
 * Immutable snapshot handed to the UI thread.
 *
 * @property totalBytes trusted content length, or [UpdateProgressTracker.UNKNOWN_TOTAL].
 * @property receivedBytes measured bytes written during the current download attempt.
 * @property downloadAttempt 1-based attempt counter for the current payload.
 * @property fallbackToFull true when a failed differential stage caused a full download.
 * @property failureMessage present only in [UpdateStage.FAILED].
 * @property estimatedBytesPerSecond measured rate, or null when not yet estimable.
 * @property estimatedSecondsRemaining measured ETA, or null without a known total and rate.
 */
internal data class UpdateProgressState(
  val stage: UpdateStage = UpdateStage.IDLE,
  val totalBytes: Long = UpdateProgressTracker.UNKNOWN_TOTAL,
  val receivedBytes: Long = 0L,
  val downloadAttempt: Int = 1,
  val fallbackToFull: Boolean = false,
  val failureMessage: String? = null,
  val estimatedBytesPerSecond: Long? = null,
  val estimatedSecondsRemaining: Long? = null,
) {
  val totalKnown: Boolean
    get() = totalBytes > 0

  /** Percentage 0..100, or null when the total length is not trustworthy. */
  val percent: Int?
    get() =
      if (totalKnown && totalBytes > 0) {
        ((receivedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
      } else {
        null
      }

  val remainingBytes: Long?
    get() = if (totalKnown) (totalBytes - receivedBytes).coerceAtLeast(0) else null
}

/**
 * Tracks transfer samples on the updater worker thread and produces [UpdateProgressState] snapshots
 * for the UI thread. Not thread-safe by design: a single tracker instance belongs to one download
 * work item.
 *
 * @param clock monotonically increasing timestamp supplier, injectable for tests (nanoseconds).
 */
internal class UpdateProgressTracker(private val clock: () -> Long = System::nanoTime) {
  private val samples = ArrayDeque<TransferSample>()
  private var stage = UpdateStage.IDLE
  private var totalBytes = UpdateProgressTracker.UNKNOWN_TOTAL
  private var receivedBytes = 0L
  private var downloadAttempt = 1
  private var fallbackToFull = false
  private var failureMessage: String? = null

  /** Marks a stage transition. A failure carries its backend message verbatim. */
  fun onStageChanged(stage: UpdateStage, failureMessage: String? = null) {
    this.stage = stage
    this.failureMessage = failureMessage.takeIf { stage == UpdateStage.FAILED }
    if (stage != UpdateStage.DIFFERENTIAL_DOWNLOAD && stage != UpdateStage.FULL_DOWNLOAD) {
      samples.clear()
    }
  }

  /**
   * Reports the trusted total: the server content length when present, otherwise the manifest size,
   * otherwise unknown. Returns the effective total in bytes.
   */
  fun onTotalBytes(contentLength: Long, manifestSize: Long): Long {
    totalBytes =
      when {
        contentLength > 0 -> contentLength
        manifestSize > 0 -> manifestSize
        else -> UpdateProgressTracker.UNKNOWN_TOTAL
      }
    return totalBytes
  }

  /** Reports that a payload attempt starts downloading again and clears the rate window. */
  fun onAttemptStarted(attempt: Int) {
    downloadAttempt = attempt
    receivedBytes = 0
    samples.clear()
  }

  /** Records that the differential pipeline fell back to the full download. */
  fun onFallbackToFull() {
    fallbackToFull = true
  }

  /** Accumulates measured bytes and records a timestamped sample for speed estimation. */
  fun onBytesWritten(delta: Long) {
    require(delta >= 0) { "Byte deltas cannot be negative" }
    if (delta == 0L) return
    receivedBytes += delta
    samples.addLast(TransferSample(clock(), receivedBytes))
    while (samples.size > UpdateProgressTracker.MAX_SAMPLE_COUNT) {
      samples.removeFirst()
    }
  }

  /**
   * Builds a UI snapshot. Speed and ETA are only produced when the retained samples span at least
   * [UpdateProgressTracker.MIN_SPEED_WINDOW_NANOS] and show a positive rate; otherwise both stay
   * null so the UI never fabricates estimates.
   */
  fun snapshot(): UpdateProgressState {
    val rate = estimateBytesPerSecond()
    val base =
      UpdateProgressState(
        stage = stage,
        totalBytes = totalBytes,
        receivedBytes = receivedBytes,
        downloadAttempt = downloadAttempt,
        fallbackToFull = fallbackToFull,
        failureMessage = failureMessage,
        estimatedBytesPerSecond = rate,
        estimatedSecondsRemaining =
          rate?.let { currentRate ->
            remainingBytes(totalBytes, receivedBytes)?.let { remaining ->
              if (remaining == 0L) 0L else (remaining + currentRate - 1) / currentRate
            }
          },
      )
    return base
  }

  private fun estimateBytesPerSecond(): Long? {
    if (stage != UpdateStage.DIFFERENTIAL_DOWNLOAD && stage != UpdateStage.FULL_DOWNLOAD) {
      return null
    }
    if (samples.size < 2) return null
    val oldest = samples.first()
    val newest = samples.last()
    val elapsedNanos = newest.atNanos - oldest.atNanos
    val bytes = newest.cumulativeBytes - oldest.cumulativeBytes
    if (elapsedNanos < UpdateProgressTracker.MIN_SPEED_WINDOW_NANOS || bytes <= 0) return null
    return bytes * UpdateProgressTracker.NANOS_PER_SECOND / elapsedNanos
  }

  private fun remainingBytes(total: Long, received: Long): Long? =
    if (total > 0) (total - received).coerceAtLeast(0) else null

  private data class TransferSample(val atNanos: Long, val cumulativeBytes: Long)

  companion object {
    const val UNKNOWN_TOTAL = -1L
    const val MAX_SAMPLE_COUNT = 16
    const val MIN_SPEED_WINDOW_NANOS = 500_000_000L
    const val NANOS_PER_SECOND = 1_000_000_000L
  }
}

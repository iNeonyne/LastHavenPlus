package com.neonyne.lasthavenlauncher

/**
 * Runs a compatible differential update first, but treats it only as an optimization.
 * Any patch download, base validation, reconstruction, checksum or signature failure
 * triggers the independently verified full APK path.
 */
internal object UpdateFallback {
    fun <T> withFullFallback(
        preferDelta: Boolean,
        deltaAttempt: () -> T,
        fullAttempt: () -> T,
        onDeltaFailure: (Exception) -> Unit = {},
    ): T {
        if (!preferDelta) return fullAttempt()
        return try {
            deltaAttempt()
        } catch (error: Exception) {
            onDeltaFailure(error)
            fullAttempt()
        }
    }
}

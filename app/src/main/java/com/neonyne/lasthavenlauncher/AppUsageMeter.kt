package com.neonyne.lasthavenlauncher

import android.content.Context
import android.net.TrafficStats
import android.os.Process
import java.io.File

data class UsageSnapshot(
    val installedApkBytes: Long,
    val cefriumProfileBytes: Long?,
    val cacheBytes: Long,
    val receivedSinceBootBytes: Long?,
    val sentSinceBootBytes: Long?,
    val receivedThisSessionBytes: Long?,
    val sentThisSessionBytes: Long?,
)

/**
 * Local-only measurements. Android's UID counters include all app traffic
 * (game, updater and other app networking) and reset when the device reboots.
 */
class AppUsageMeter(context: Context) {
    private val appContext = context.applicationContext
    private val uid = Process.myUid()
    private val rxAtStart = TrafficStats.getUidRxBytes(uid).takeIf { it >= 0L }
    private val txAtStart = TrafficStats.getUidTxBytes(uid).takeIf { it >= 0L }

    fun snapshot(): UsageSnapshot {
        val rx = TrafficStats.getUidRxBytes(uid).takeIf { it >= 0L }
        val tx = TrafficStats.getUidTxBytes(uid).takeIf { it >= 0L }
        val profile = File(appContext.applicationInfo.dataDir, "app_cefrium")
        val apkFiles = buildList {
            add(File(appContext.applicationInfo.sourceDir))
            appContext.applicationInfo.splitSourceDirs?.forEach { add(File(it)) }
        }

        return UsageSnapshot(
            installedApkBytes = apkFiles.sumOf { if (it.isFile) it.length() else 0L },
            cefriumProfileBytes = if (profile.exists()) directorySize(profile) else null,
            cacheBytes = directorySize(appContext.cacheDir) + directorySize(appContext.codeCacheDir),
            receivedSinceBootBytes = rx,
            sentSinceBootBytes = tx,
            receivedThisSessionBytes = difference(rx, rxAtStart),
            sentThisSessionBytes = difference(tx, txAtStart),
        )
    }

    private fun difference(current: Long?, initial: Long?): Long? =
        if (current != null && initial != null && current >= initial) current - initial else null

    private fun directorySize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf(::directorySize) ?: 0L
    }
}

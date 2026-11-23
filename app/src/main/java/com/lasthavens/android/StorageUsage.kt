package com.lasthavens.android

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.ArrayDeque

data class StorageSnapshot(
    val persistentAppAndWebViewBytes: Long,
    val cacheBytes: Long,
    val availableDeviceBytes: Long,
    val scanComplete: Boolean,
)

/** Read-only filesystem accounting for app-private files and Android System WebView data. */
object StorageUsage {
    fun measure(context: Context): StorageSnapshot {
        val cacheRoots = (listOf(context.cacheDir, context.codeCacheDir) +
            context.externalCacheDirs.filterNotNull()).distinctBy { it.absolutePath }
        val persistent = scan(context.dataDir, cacheRoots)
        val cache = cacheRoots.fold(Scan(0L, true)) { total, root ->
            val next = scan(root, emptyList())
            Scan(add(total.bytes, next.bytes), total.complete && next.complete)
        }
        val available = runCatching {
            StatFs(Environment.getDataDirectory().absolutePath).availableBytes
        }.getOrDefault(0L)
        return StorageSnapshot(persistent.bytes, cache.bytes, available, persistent.complete && cache.complete)
    }

    private data class Scan(val bytes: Long, val complete: Boolean)

    private fun scan(root: File, excludedRoots: List<File>): Scan {
        val base = runCatching { root.canonicalFile }.getOrNull() ?: return Scan(0L, false)
        if (!base.exists()) return Scan(0L, true)
        val excluded = excludedRoots.mapNotNull { runCatching { it.canonicalPath }.getOrNull() }.toSet()
        val pending = ArrayDeque<File>()
        val visited = HashSet<String>()
        pending.add(base)
        var bytes = 0L
        var complete = true
        while (pending.isNotEmpty()) {
            val file = pending.removeLast()
            val canonical = runCatching { file.canonicalFile }.getOrNull()
            if (canonical == null) {
                complete = false
                continue
            }
            val path = canonical.path
            if (path in excluded || (path != base.path && !path.startsWith(base.path + File.separator))) continue
            if (!visited.add(path)) continue
            if (canonical.isDirectory) {
                val children = canonical.listFiles()
                if (children == null) complete = false else children.forEach(pending::add)
            } else if (canonical.isFile) {
                bytes = add(bytes, canonical.length().coerceAtLeast(0L))
            }
        }
        return Scan(bytes, complete)
    }

    private fun add(a: Long, b: Long): Long = if (Long.MAX_VALUE - a < b) Long.MAX_VALUE else a + b
}

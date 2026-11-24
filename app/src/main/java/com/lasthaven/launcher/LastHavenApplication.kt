package com.lasthaven.launcher

import android.app.Application
import android.content.Context
import android.os.Build
import com.cefrium.Cefrium
import java.io.File

class LastHavenApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Set persistent data directory BEFORE any browser is created
        // This stores cookies, IndexedDB, localStorage, Cache Storage, service workers
        // Survives app updates and is not cleared by Android when storage runs low
        val dataDir = File(filesDir, "app_cefrium")
        Cefrium.setDataDirectory(dataDir)

        // Enable GPU process for better WebGL stability and performance
        // Recommended for devices with >4GB RAM (most modern devices)
        if (isGpuProcessSupported()) {
            Cefrium.setGpuProcessEnabled(true)
        }

        // Initialize Cefrium early (optional - CefriumInitProvider does this automatically)
        // Doing it here gives us control and error handling
        try {
            Cefrium.initialize(this)
        } catch (e: Exception) {
            // Log error but don't crash - Cefrium will retry on first browser creation
            android.util.Log.e("LastHavenLauncher", "Cefrium early initialization failed", e)
        }
    }

    private fun isGpuProcessSupported(): Boolean {
        // GPU process requires Android 10+ (API 29) which is our minSdk
        // Additional check: device should have sufficient RAM
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        // Enable GPU process if device has >2GB available (conservative threshold)
        return memInfo.totalMem > 2L * 1024 * 1024 * 1024
    }

    companion object {
        /** Returns the persistent data directory for Cefrium */
        fun getCefriumDataDir(context: Context): File = File(context.filesDir, "app_cefrium")
    }
}

package com.neonyne.lasthavenlauncher

import android.app.Application
import android.content.pm.ApplicationInfo
import com.cefrium.Cefrium

class LastHavenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Cefrium's init provider performs process setup; the engine itself starts
        // lazily when the first browser is created, so this runs early enough.
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        Cefrium.setRemoteDebuggingEnabled(isDebuggable)
        Diagnostics.info(this, "Application", "Process started; remote debugging enabled=$isDebuggable")
    }
}

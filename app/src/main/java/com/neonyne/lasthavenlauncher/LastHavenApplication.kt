package com.neonyne.lasthavenlauncher

import android.app.Application

class LastHavenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Diagnostics.info(this, "Application", "Process started")
    }
}

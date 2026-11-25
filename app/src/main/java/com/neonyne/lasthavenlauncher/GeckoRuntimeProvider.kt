package com.neonyne.lasthavenlauncher

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.annotation.MainThread
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

/**
 * GeckoView requires one runtime for the lifetime of the app process.
 * Create it lazily from the foreground app process, not from Application.onCreate(),
 * because GeckoView child processes also instantiate the Application class.
 */
object GeckoRuntimeProvider {
    private var runtime: GeckoRuntime? = null

    @MainThread
    fun get(context: Context): GeckoRuntime {
        runtime?.let { return it }

        val appContext = context.applicationContext
        val debuggable = appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val settings = GeckoRuntimeSettings.Builder()
            .remoteDebuggingEnabled(debuggable)
            .build()
        return GeckoRuntime.create(appContext, settings).also {
            runtime = it
            Diagnostics.info(
                appContext,
                "GeckoView",
                "Process runtime created; remote debugging enabled=" + debuggable,
            )
        }
    }
}

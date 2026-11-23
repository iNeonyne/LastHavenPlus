package com.lasthavens.android

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import android.widget.Toast

/** Receives PackageInstaller status and opens Android's explicit confirmation screen. */
class UpdateInstallStatusActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmation = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT) as? Intent
                }
                if (confirmation != null) {
                    confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(confirmation)
                } else {
                    Toast.makeText(this, "Android could not open the installation confirmation.", Toast.LENGTH_LONG).show()
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                val version = intent.getStringExtra(EXTRA_VERSION).orEmpty()
                Toast.makeText(this, "LastHavenPlus $version installed.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                    ?.takeIf { it.isNotBlank() } ?: "Android did not install the update."
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
    companion object { const val EXTRA_VERSION = "com.lasthavens.android.UPDATE_VERSION" }
}

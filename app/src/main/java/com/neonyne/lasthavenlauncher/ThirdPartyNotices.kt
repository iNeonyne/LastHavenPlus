package com.neonyne.lasthavenlauncher

import android.content.Context

/** Loads packaged third-party license notices for display in Settings. */
object ThirdPartyNotices {
  const val ADDON_MANAGER_NOTICE_ASSET = "licenses/ADDON_MANAGER_0.5.1_MIT.txt"

  fun readAddonManagerNotice(context: Context): String? = runCatching {
    context.assets.open(ADDON_MANAGER_NOTICE_ASSET).use { stream ->
      stream.readBytes().toString(Charsets.UTF_8)
    }
  }
    .getOrNull()
}

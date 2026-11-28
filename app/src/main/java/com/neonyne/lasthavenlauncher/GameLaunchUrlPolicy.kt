package com.neonyne.lasthavenlauncher

import java.net.URI

/** Keeps persisted game-server selections on the supported /play entry point. */
internal object GameLaunchUrlPolicy {
  fun normalize(url: String?): String {
    val uri = url?.let { runCatching { URI(it) }.getOrNull() } ?: return DEFAULT_GAME_URL
    if (!uri.scheme.equals("https", ignoreCase = true)) return DEFAULT_GAME_URL
    if (uri.rawUserInfo != null || (uri.port != -1 && uri.port != 443)) return DEFAULT_GAME_URL
    if (uri.rawQuery != null || uri.rawFragment != null) return DEFAULT_GAME_URL

    return when (uri.host?.lowercase()) {
      "lasthavens.com" -> DEFAULT_GAME_URL
      "alt.lasthavens.com" -> ALTERNATE_GAME_URL
      else -> DEFAULT_GAME_URL
    }
  }
}

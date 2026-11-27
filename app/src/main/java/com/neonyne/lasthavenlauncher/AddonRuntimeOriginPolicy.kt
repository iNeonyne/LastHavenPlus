package com.neonyne.lasthavenlauncher

import java.net.URI

/**
 * Strict URL gate for the addon runtime. It is narrower than the launcher's general site policy.
 */
object AddonRuntimeOriginPolicy {
  const val GAME_ORIGIN = "https://lasthavens.com"
  const val ALTERNATE_GAME_ORIGIN = "https://alt.lasthavens.com"
  private const val DEFAULT_GAME_HOST = "lasthavens.com"
  private const val ALTERNATE_GAME_HOST = "alt.lasthavens.com"
  private const val MAX_URL_CHARS = 4096

  fun allows(url: String): Boolean {
    if (url.isBlank() || url.length > MAX_URL_CHARS) return false
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    if (!uri.isAbsolute || uri.isOpaque) return false
    if (!uri.scheme.equals("https", ignoreCase = true)) return false
    val host = uri.host ?: return false
    if (
      !host.equals(DEFAULT_GAME_HOST, ignoreCase = true) &&
        !host.equals(ALTERNATE_GAME_HOST, ignoreCase = true)
    ) {
      return false
    }
    if (uri.rawUserInfo != null || (uri.port != -1 && uri.port != 443)) return false
    val path = uri.rawPath ?: return false
    return path == "/play" || path == "/play/"
  }

  fun canonicalOrigin(url: String): String? {
    if (!allows(url)) return null
    val host = URI(url).host
    return if (host.equals(ALTERNATE_GAME_HOST, ignoreCase = true)) {
      ALTERNATE_GAME_ORIGIN
    } else {
      GAME_ORIGIN
    }
  }
}

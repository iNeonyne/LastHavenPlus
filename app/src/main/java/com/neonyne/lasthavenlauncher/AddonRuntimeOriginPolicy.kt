package com.neonyne.lasthavenlauncher

import java.net.URI

/** Strict URL gate for the addon runtime. It is narrower than the launcher's general site policy. */
object AddonRuntimeOriginPolicy {
  const val GAME_ORIGIN = "https://lasthavens.com"
  private const val MAX_URL_CHARS = 4096

  fun allows(url: String): Boolean {
    if (url.isBlank() || url.length > MAX_URL_CHARS) return false
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    if (!uri.isAbsolute || uri.isOpaque) return false
    if (!uri.scheme.equals("https", ignoreCase = true)) return false
    if (uri.host?.equals("lasthavens.com", ignoreCase = true) != true) return false
    if (uri.rawUserInfo != null || (uri.port != -1 && uri.port != 443)) return false
    val path = uri.rawPath ?: return false
    return path == "/play" || path == "/play/"
  }

  fun canonicalOrigin(url: String): String? =
    if (allows(url)) GAME_ORIGIN else null
}

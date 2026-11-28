package com.neonyne.lasthavenlauncher

import java.net.URI

/**
 * Restricts top-level game navigation to the two official game origins.
 *
 * The browser login/create-account flow is presented by the game login screen itself; the public
 * site does not document a separate identity-provider redirect. Asset/API hosts are not navigation
 * origins and must not be added here without evidence that a top-level redirect requires them.
 */
internal object GameNavigationPolicy {
  private const val DEFAULT_GAME_HOST = "lasthavens.com"
  private const val ALTERNATE_GAME_HOST = "alt.lasthavens.com"
  private const val MAX_URL_CHARS = 4096

  fun allows(url: String): Boolean {
    if (url.isBlank() || url.length > MAX_URL_CHARS) return false
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    if (!uri.isAbsolute || uri.isOpaque) return false
    if (!uri.scheme.equals("https", ignoreCase = true)) return false
    if (uri.rawUserInfo != null) return false
    val host = uri.host ?: return false
    if (
      !host.equals(DEFAULT_GAME_HOST, ignoreCase = true) &&
        !host.equals(ALTERNATE_GAME_HOST, ignoreCase = true)
    ) {
      return false
    }
    if (uri.port != -1 && uri.port != 443) return false
    return true
  }
}

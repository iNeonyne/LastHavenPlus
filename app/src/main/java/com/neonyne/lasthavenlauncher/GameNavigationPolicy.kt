package com.neonyne.lasthavenlauncher

import java.net.URI

/**
 * Restricts top-level game navigation to the official LastHavens domain and its subdomains.
 *
 * This permits first-party auth/game subdomains while keeping unrelated domains and lookalikes
 * blocked. Asset/API hosts on third-party domains are not navigation origins and remain blocked.
 */
internal object GameNavigationPolicy {
  private const val GAME_DOMAIN = "lasthavens.com"
  private const val MAX_URL_CHARS = 4096

  fun allows(url: String): Boolean {
    if (url.isBlank() || url.length > MAX_URL_CHARS) return false
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    if (!uri.isAbsolute || uri.isOpaque) return false
    if (!uri.scheme.equals("https", ignoreCase = true)) return false
    if (uri.rawUserInfo != null) return false
    val host = uri.host ?: return false
    if (
      !host.equals(GAME_DOMAIN, ignoreCase = true) &&
        !host.endsWith(".$GAME_DOMAIN", ignoreCase = true)
    ) {
      return false
    }
    if (uri.port != -1 && uri.port != 443) return false
    return true
  }
}

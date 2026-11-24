package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsTest {
    @Test
    fun redactsAuthorizationCookiesTokensAndSensitiveQueryParameters() {
        val sanitized = Diagnostics.sanitize(
            "Authorization: Bearer abc123\n" +
                "cookie=crumbs token=xyz access_token=hidden\n" +
                "https://example.invalid/play?session_id=private&view=game",
        )

        assertTrue(sanitized.contains("Authorization: [REDACTED]"))
        assertTrue(sanitized.contains("cookie=[REDACTED]"))
        assertTrue(sanitized.contains("token=[REDACTED]"))
        assertTrue(sanitized.contains("access_token=[REDACTED]"))
        assertTrue(sanitized.contains("session_id=[REDACTED]"))
        assertFalse(sanitized.contains("abc123"))
        assertFalse(sanitized.contains("crumbs"))
        assertFalse(sanitized.contains("hidden"))
        assertFalse(sanitized.contains("private"))
    }

    @Test
    fun redactsCompleteCookieHeaders() {
        val sanitized = Diagnostics.sanitize(
            "Cookie: session=first-secret; auth=second-secret\n" +
                "Set-Cookie: refresh=third-secret; HttpOnly",
        )

        assertTrue(sanitized.contains("Cookie: [REDACTED]"))
        assertTrue(sanitized.contains("Set-Cookie: [REDACTED]"))
        assertFalse(sanitized.contains("first-secret"))
        assertFalse(sanitized.contains("second-secret"))
        assertFalse(sanitized.contains("third-secret"))
    }

    @Test
    fun redactsJsonCredentialFields() {
        val sanitized = Diagnostics.sanitize(
            """{"access_token":"json-secret","session_id":"session-secret"}""",
        )

        assertTrue(sanitized.contains("access_token"))
        assertTrue(sanitized.contains("session_id"))
        assertFalse(sanitized.contains("json-secret"))
        assertFalse(sanitized.contains("session-secret"))
    }
}

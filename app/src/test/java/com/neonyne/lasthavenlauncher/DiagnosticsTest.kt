package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsTest {
    @Test
    fun redactsAuthorizationCookiesTokensAndSensitiveQueryParameters() {
        val sanitized = Diagnostics.sanitize(
            "Authorization: Bearer abc123 cookie=crumbs token=xyz " +
                "https://example.invalid/play?session=private&view=game",
        )

        assertTrue(sanitized.contains("Authorization: [REDACTED]"))
        assertTrue(sanitized.contains("cookie=[REDACTED]"))
        assertTrue(sanitized.contains("token=[REDACTED]"))
        assertTrue(sanitized.contains("session=[REDACTED]"))
        assertFalse(sanitized.contains("abc123"))
        assertFalse(sanitized.contains("crumbs"))
        assertFalse(sanitized.contains("private"))
    }
}

package com.lasthavens.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DownloadFileNamePolicyTest {
    @Test
    fun replacesPathSeparatorsAndReservedCharacters() {
        val result = DownloadFileNamePolicy.sanitize("../../folder\\\\evil:?.apk", "fallback.bin")
        assertFalse(result.contains('/'))
        assertFalse(result.contains(92.toChar()))
        assertFalse(result.contains(':'))
        assertFalse(result.contains('?'))
        assertFalse(result.startsWith('.'))
    }

    @Test
    fun replacesControlCharactersAndTrimsWhitespace() {
        assertEquals("backup__file.zip", DownloadFileNamePolicy.sanitize("  backup\n\tfile.zip  ", "fallback.bin"))
    }

    @Test
    fun limitsNamesTo120Characters() {
        assertEquals(120, DownloadFileNamePolicy.sanitize("a".repeat(200), "fallback.bin").length)
    }

    @Test
    fun usesFallbackWhenSanitizationLeavesNoName() {
        assertEquals("lasthavens.bin", DownloadFileNamePolicy.sanitize(" ... ", "lasthavens.bin"))
    }

    @Test
    fun preservesOrdinaryUnicodeNames() {
        assertEquals("addon-東京.zip", DownloadFileNamePolicy.sanitize("addon-東京.zip", "fallback.bin"))
    }
}

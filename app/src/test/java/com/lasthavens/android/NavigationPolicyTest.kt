package com.lasthavens.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationPolicyTest {
    @Test
    fun acceptsOnlyTheGameDomainAndItsSubdomains() {
        assertTrue(NavigationPolicy.isGameHost("https://lasthavens.com"))
        assertTrue(NavigationPolicy.isGameHost("https://ALT.LASTHAVENS.COM/play"))
        assertTrue(NavigationPolicy.isGameHost("https://cdn.alt.lasthavens.com/assets"))
        assertFalse(NavigationPolicy.isGameHost("https://lasthavens.com.attacker.test"))
        assertFalse(NavigationPolicy.isGameHost("https://lasthavens.com@attacker.test"))
        assertFalse(NavigationPolicy.isGameHost("https://notlasthavens.com"))
        assertFalse(NavigationPolicy.isGameHost("not a url"))
    }

    @Test
    fun permitsHttpsGamePagesButRejectsInsecureAndLookalikeHosts() {
        assertTrue(NavigationPolicy.isInApp("https://alt.lasthavens.com/play"))
        assertFalse(NavigationPolicy.isInApp("http://alt.lasthavens.com/play"))
        assertFalse(NavigationPolicy.isInApp("https://alt.lasthavens.com.attacker.test/"))
        assertFalse(NavigationPolicy.isInApp("https://alt.lasthavens.com@attacker.test/"))
    }

    @Test
    fun permitsRequiredGeckoInternalSchemes() {
        assertTrue(NavigationPolicy.isInApp("about:blank"))
        assertTrue(NavigationPolicy.isInApp("moz-extension://addon-id/manager.html"))
        assertTrue(NavigationPolicy.isInApp("resource://android/assets/index.html"))
        assertTrue(NavigationPolicy.isInApp("blob:https://alt.lasthavens.com/1234"))
        assertTrue(NavigationPolicy.isInApp("blob:moz-extension://addon-id/1234"))
    }

    @Test
    fun rejectsExecutableAndUntrustedSchemes() {
        assertFalse(NavigationPolicy.isInApp("javascript:alert(1)"))
        assertFalse(NavigationPolicy.isInApp("data:text/html,<script>alert(1)</script>"))
        assertFalse(NavigationPolicy.isInApp("file:///data/local/tmp/page.html"))
        assertFalse(NavigationPolicy.isInApp("intent://example/#Intent;scheme=https;end"))
        assertFalse(NavigationPolicy.isInApp("blob:https://attacker.test/1234"))
        assertFalse(NavigationPolicy.isInApp("blob:data:text/html,hello"))
    }
}

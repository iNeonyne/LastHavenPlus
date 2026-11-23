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
        assertFalse(NavigationPolicy.isGameHost("http://alt.lasthavens.com/play"))
        assertFalse(NavigationPolicy.isGameHost("javascript://alt.lasthavens.com/alert(1)"))
        assertFalse(NavigationPolicy.isGameHost("ftp://alt.lasthavens.com/file"))
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
    fun permitsOnlyTrustedGameBlobUrls() {
        assertTrue(NavigationPolicy.isInApp("blob:https://alt.lasthavens.com/1234"))
        assertFalse(NavigationPolicy.isInApp("blob:https://attacker.test/1234"))
        assertFalse(NavigationPolicy.isInApp("blob:data:text/html,hello"))
        assertFalse(NavigationPolicy.isInApp("about:blank"))
        assertFalse(NavigationPolicy.isInApp("moz-extension://addon-id/manager.html"))
        assertFalse(NavigationPolicy.isInApp("resource://android/assets/index.html"))
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

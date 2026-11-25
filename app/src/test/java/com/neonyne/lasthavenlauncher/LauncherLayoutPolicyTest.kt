package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherLayoutPolicyTest {
    @Test
    fun homeSwitchesToTwoColumnLayoutAtWideWindowBreakpoint() {
        assertFalse(LauncherLayoutPolicy.usesWideHome(699))
        assertTrue(LauncherLayoutPolicy.usesWideHome(700))
        assertTrue(LauncherLayoutPolicy.usesWideHome(1024))
    }

    @Test
    fun settingsContentWidthIsCappedOnLargeWindows() {
        assertEquals(680, LauncherLayoutPolicy.settingsContentMaxWidthDp(699))
        assertEquals(760, LauncherLayoutPolicy.settingsContentMaxWidthDp(900))
        assertEquals(760, LauncherLayoutPolicy.settingsContentMaxWidthDp(1200))
    }

    @Test
    fun pagePaddingTightensForNarrowWindows() {
        assertEquals(16, LauncherLayoutPolicy.pageHorizontalPaddingDp(360))
        assertEquals(24, LauncherLayoutPolicy.pageHorizontalPaddingDp(420))
    }
}

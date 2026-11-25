package com.neonyne.lasthavenlauncher

/**
 * Width breakpoints shared by launcher pages so window resizing selects the same layout policy.
 */
internal object LauncherLayoutPolicy {
    const val WIDE_HOME_MIN_WIDTH_DP = 700
    const val LARGE_SETTINGS_MIN_WIDTH_DP = 900
    const val COMPACT_PADDING_MAX_WIDTH_DP = 420

    fun usesWideHome(widthDp: Int): Boolean = widthDp >= WIDE_HOME_MIN_WIDTH_DP

    fun settingsContentMaxWidthDp(widthDp: Int): Int =
        if (widthDp >= LARGE_SETTINGS_MIN_WIDTH_DP) 760 else 680

    fun pageHorizontalPaddingDp(widthDp: Int): Int =
        if (widthDp < COMPACT_PADDING_MAX_WIDTH_DP) 16 else 24
}

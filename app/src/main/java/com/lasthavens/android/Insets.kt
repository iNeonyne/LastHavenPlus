package com.lasthavens.android

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Pads [view] so its content stays clear of the status/navigation bars and display cutout. */
fun applySystemBarPadding(view: View, top: Boolean = true, bottom: Boolean = true) {
    val l = view.paddingLeft
    val t = view.paddingTop
    val r = view.paddingRight
    val b = view.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        v.setPadding(l + bars.left, t + (if (top) bars.top else 0), r + bars.right, b + (if (bottom) bars.bottom else 0))
        insets
    }
    ViewCompat.requestApplyInsets(view)
}

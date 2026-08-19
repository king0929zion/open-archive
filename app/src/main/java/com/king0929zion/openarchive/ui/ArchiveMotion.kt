package com.king0929zion.openarchive.ui

import androidx.compose.animation.core.CubicBezierEasing

/**
 * One compact motion language for the whole app.
 *
 * Keep motion short enough to feel responsive and prefer transform/alpha animations
 * over layout-heavy effects so image feeds and streaming text do not fight the frame budget.
 */
object ArchiveMotion {
    const val PressIn = 70
    const val Quick = 100
    const val Fast = 140
    const val Standard = 190
    const val Screen = 220

    val Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}

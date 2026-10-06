package com.muhazri.jejak.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.max

/** Layout per screen shape, from the iPhone Duo design. */
enum class ScreenLayout {
    /** Standard phone: single column. */
    Regular,

    /** Fold closed (outer screen): single column with tighter sizing. */
    Compact,

    /** Fold open (inner screen): context in the left panel, content and decisions in the right panel. */
    Split,
    ;

    /**
     * Extra space between the top inset (status bar) and the first line of content. Phones need none;
     * folded screens can report little or no top inset, so they keep content 32dp from the edge to clear
     * the rounded corners.
     */
    fun topMargin(safeAreaTop: Dp): Dp =
        if (this == Regular) 0.dp else max(8f, 32f - safeAreaTop.value).dp

    companion object {
        fun of(size: DpSize): ScreenLayout {
            val ratio = size.width.value / size.height.value
            return when {
                // Two panels need both the width and the squarer shape an unfolded device or a tablet
                // has; a phone held sideways is far wider than it is tall (~2.2) and stays one column.
                size.width >= 600.dp && ratio > 1f && ratio < 1.9f -> Split
                // A closed fold is much squarer than any phone (~0.69 vs 0.46-0.58 width/height).
                size.height < 600.dp || ratio > 0.62f -> Compact
                else -> Regular
            }
        }

        /** Outer edge margin of the open fold's panels (the fold side keeps 24dp). */
        fun splitOuterMargin(safeAreaInset: Dp): Dp = max(32f, safeAreaInset.value).dp

        /** Motion for switching layouts when the device folds or unfolds. */
        fun <T> transition(reduceMotion: Boolean): FiniteAnimationSpec<T> =
            if (reduceMotion) tween(200) else tween(500, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
    }
}

/** Title size of the onboarding headings. */
val ScreenLayout.titleSize: Int
    get() = if (this == ScreenLayout.Compact) 24 else 32

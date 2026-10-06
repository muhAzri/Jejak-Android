package com.muhazri.jejak.core.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

/** The insets SwiftUI reports as `safeAreaInsets`; screens pad the edges they care about themselves. */
@Immutable
data class SafeArea(val top: Dp, val bottom: Dp, val leading: Dp, val trailing: Dp)

@Composable
fun safeArea(): SafeArea {
    val direction = LocalLayoutDirection.current
    val padding = WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues()
    return SafeArea(
        top = padding.calculateTopPadding(),
        bottom = padding.calculateBottomPadding(),
        leading = padding.calculateStartPadding(direction),
        trailing = padding.calculateEndPadding(direction),
    )
}

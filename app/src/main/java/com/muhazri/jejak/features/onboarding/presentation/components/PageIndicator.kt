package com.muhazri.jejak.features.onboarding.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.JejakTheme

@Composable
fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    val label = stringResource(R.string.onboarding_page_indicator, current + 1, count)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.semantics { contentDescription = label },
    ) {
        repeat(count) { index ->
            val isCurrent = index == current
            val width by animateDpAsState(if (isCurrent) 20.dp else 6.dp, tween(200), label = "dotWidth")
            val color by animateColorAsState(
                if (isCurrent) colors.textPrimary else colors.fillMuted,
                tween(200),
                label = "dotColor",
            )
            Box(Modifier.width(width).height(6.dp).background(color, CircleShape))
        }
    }
}

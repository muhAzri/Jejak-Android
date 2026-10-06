package com.muhazri.jejak.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Full-width row with a trailing radio (design-system `ListRowRadio`). */
@Composable
fun RadioRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                interactionSource = interaction,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(title, style = JejakFont.p1, color = colors.textPrimary)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        RadioIndicator(isSelected)
    }
}

@Composable
private fun RadioIndicator(isSelected: Boolean) {
    val colors = JejakTheme.colors
    val ring by animateColorAsState(
        if (isSelected) colors.strong else colors.textSecondary,
        tween(150),
        label = "radioRing",
    )
    val dot by animateDpAsState(if (isSelected) 10.dp else 0.dp, tween(150), label = "radioDot")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(20.dp)
            .border(2.dp, ring, CircleShape),
    ) {
        Box(Modifier.size(dot).background(colors.strong, CircleShape))
    }
}

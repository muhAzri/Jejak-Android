package com.muhazri.jejak.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.pressScale
import com.muhazri.jejak.core.designsystem.pressable
import com.muhazri.jejak.features.home.domain.entities.ActivityType

/**
 * "Start Run" / "Start Walk" card. A wide row on a phone and in the open fold's left panel,
 * a tile in a two-column grid on the closed fold.
 */
@Composable
fun StartActivityCard(
    activity: ActivityType,
    layout: ScreenLayout,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .scale(pressScale(pressed))
            .alpha(if (isLocked) 0.45f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(activity.tint.copy(alpha = 0.12f))
            .pressable(interaction, enabled && !isLocked, onClick),
    ) {
        if (layout == ScreenLayout.Compact) {
            CardTile(activity, isLocked)
        } else {
            CardRow(activity, isLocked, fillsHeight = layout == ScreenLayout.Split)
        }
    }
}

@Composable
private fun CardRow(activity: ActivityType, isLocked: Boolean, fillsHeight: Boolean) {
    val colors = JejakTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (fillsHeight) Modifier.fillMaxHeight() else Modifier)
            .defaultMinSize(minHeight = 132.dp)
            .padding(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            ActivityChip(activity, colors.surface)
            Text(
                text = stringResource(activity.startTitleRes),
                style = JejakFont.display(24.sp),
                color = colors.textPrimary,
            )
        }
        StartBadge(size = 64.dp, isLocked = isLocked)
    }
}

@Composable
private fun CardTile(activity: ActivityType, isLocked: Boolean) {
    val colors = JejakTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        ActivityChip(activity, colors.surface)
        Text(
            text = stringResource(activity.startTitleRes),
            style = JejakFont.display(20.sp),
            color = colors.textPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        StartBadge(
            size = 48.dp,
            isLocked = isLocked,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/** Round yellow "go" button, or a muted lock when location is off. */
@Composable
private fun StartBadge(size: Dp, isLocked: Boolean, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    val icon = if (isLocked) HeroIcon.LockClosed else HeroIcon.ArrowRight
    val iconSize = when {
        isLocked -> if (size > 56.dp) 24.dp else 22.dp
        else -> if (size > 56.dp) 28.dp else 24.dp
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(if (isLocked) colors.fillMuted else colors.accent, CircleShape),
    ) {
        HeroIconImage(icon, iconSize, if (isLocked) colors.textOnDisabled else colors.accentInk)
    }
}

/** Small activity label: colored glyph + name on a rounded chip. */
@Composable
fun ActivityChip(activity: ActivityType, background: Color, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(start = 8.dp, end = 10.dp, top = 2.dp, bottom = 2.dp),
    ) {
        HeroIconImage(activity.icon, 14.dp, activity.tint)
        Text(stringResource(activity.titleRes), style = JejakFont.p3Bold, color = colors.textPrimary)
    }
}

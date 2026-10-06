package com.muhazri.jejak.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Press feedback shared by every Jejak button: a 0.98 scale over 120ms. */
@Composable
internal fun pressScale(pressed: Boolean, pressedScale: Float = 0.98f): Float =
    animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale",
    ).value

/** Filled yellow capsule — the main action of a screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = JejakTheme.colors
    CapsuleButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = JejakFont.h2,
        tint = colors.accentInk,
        background = colors.accent,
        minHeight = 52.dp,
    )
}

/** Filled red capsule (design-system `Button variant="danger"`), the confirm action of a destructive sheet. */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = JejakTheme.colors
    CapsuleButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = JejakFont.p1Bold,
        tint = colors.textOnDanger,
        background = colors.danger,
        minHeight = 52.dp,
    )
}

/** Full-height outlined capsule (design-system `Button variant="secondary"`), for sheets and cards. */
@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = JejakTheme.colors.textPrimary,
    pressedBackground: Color = JejakTheme.colors.fillInput,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(pressScale(pressed))
            .clip(CircleShape)
            .background(if (pressed) pressedBackground else Color.Transparent)
            .border(1.dp, tint, CircleShape)
            .pressable(interaction, enabled, onClick)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 16.dp),
    ) {
        AutoSizeText(text, JejakFont.p1Bold, tint, minScale = 0.8f, textAlign = TextAlign.Center)
    }
}

/** Outlined red (design-system `Button variant="destructive"`). */
@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = JejakTheme.colors
    OutlinedButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        tint = colors.danger,
        pressedBackground = colors.dangerBackground,
    )
}

/** Outlined capsule button (design-system `Button variant="secondary" size="sm"`). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = JejakTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(pressScale(pressed))
            .clip(CircleShape)
            .background(if (pressed) colors.fillInput else Color.Transparent)
            .border(1.dp, colors.strong, CircleShape)
            .pressable(interaction, enabled, onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 24.dp),
    ) {
        Text(text, style = JejakFont.p1Bold, color = colors.textPrimary)
    }
}

/** Plain secondary-text action, e.g. "Skip" and "Not Now". */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = JejakTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .alpha(if (pressed) 0.6f else 1f)
            .pressable(interaction, enabled, onClick)
            .defaultMinSize(minHeight = 44.dp)
            .padding(horizontal = 8.dp),
    ) {
        Text(text, style = JejakFont.p1Semibold, color = colors.textSecondary)
    }
}

@Composable
private fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: TextStyle,
    tint: Color,
    background: Color,
    minHeight: Dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(pressScale(pressed))
            .clip(CircleShape)
            .background(background)
            .pressable(interaction, enabled, onClick)
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = 16.dp),
    ) {
        AutoSizeText(text, style, tint, minScale = 0.8f, textAlign = TextAlign.Center)
    }
}

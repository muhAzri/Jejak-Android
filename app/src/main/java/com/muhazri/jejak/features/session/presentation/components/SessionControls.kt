package com.muhazri.jejak.features.session.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.OutlinedButton
import com.muhazri.jejak.core.designsystem.PrimaryButton
import com.muhazri.jejak.core.designsystem.Spinner
import com.muhazri.jejak.core.designsystem.pressScale
import com.muhazri.jejak.core.designsystem.pressable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long the stop and unlock gestures have to be held. */
private const val HOLD_MILLIS = 1_000

/** Lock · Pause/Resume · Finish. Finishing needs a one-second hold with a progress ring. */
@Composable
fun SessionControls(
    isPaused: Boolean,
    /** Closed fold: 56/80dp buttons instead of 64/96dp. */
    isCompact: Boolean,
    onLock: () -> Unit,
    onPauseResume: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    val side = if (isCompact) 56.dp else 64.dp
    val main = if (isCompact) 80.dp else 96.dp

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        ControlButton(stringResource(R.string.control_lock), side + 8.dp) {
            RoundIconButton(
                icon = HeroIcon.LockClosed,
                iconSize = if (isCompact) 22.dp else 24.dp,
                diameter = side,
                background = colors.surfaceTint,
                foreground = colors.textPrimary,
                onClick = onLock,
            )
        }
        Spacer(Modifier.weight(1f).widthIn(min = 8.dp))
        ControlButton(
            title = stringResource(if (isPaused) R.string.control_resume else R.string.control_pause),
            width = main,
        ) {
            RoundIconButton(
                icon = if (isPaused) HeroIcon.Play else HeroIcon.Pause,
                iconSize = if (isCompact) 36.dp else 44.dp,
                diameter = main,
                background = colors.accent,
                foreground = colors.accentInk,
                iconOffset = if (isPaused) 3.dp else 0.dp,
                onClick = onPauseResume,
            )
        }
        Spacer(Modifier.weight(1f).widthIn(min = 8.dp))
        HoldToFinishButton(
            isPaused = isPaused,
            diameter = side,
            iconSize = if (isCompact) 26.dp else 30.dp,
            onFinish = onFinish,
        )
    }
}

@Composable
private fun ControlButton(title: String, width: Dp, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.widthIn(min = width),
    ) {
        content()
        Text(title, style = JejakFont.p3, color = JejakTheme.colors.textSecondary)
    }
}

@Composable
private fun RoundIconButton(
    icon: HeroIcon,
    iconSize: Dp,
    diameter: Dp,
    background: Color,
    foreground: Color,
    onClick: () -> Unit,
    iconOffset: Dp = 0.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(pressScale(pressed, pressedScale = 0.94f))
            .size(diameter)
            .background(background, CircleShape)
            .pressable(interaction, onClick = onClick),
    ) {
        HeroIconImage(icon, iconSize, foreground, Modifier.offset(x = iconOffset))
    }
}

/**
 * Stop button that only fires after a one-second press. Paused, it is highlighted and its label says to
 * hold; a short tap while recording shows that hint for a moment.
 */
@Composable
private fun HoldToFinishButton(isPaused: Boolean, diameter: Dp, iconSize: Dp, onFinish: () -> Unit) {
    val colors = JejakTheme.colors
    val progress = remember { Animatable(0f) }
    var showsHint by remember { mutableStateOf(false) }
    val highlighted = isPaused || progress.value > 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(diameter + 8.dp)
                .holdGesture(
                    onCompleted = onFinish,
                    progress = progress,
                    onCancelled = { if (!isPaused) showsHint = true },
                ),
        ) {
            if (highlighted) {
                Canvas(Modifier.size(diameter + 8.dp)) {
                    val stroke = 4.dp.toPx()
                    val inset = stroke / 2
                    val arc = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = colors.textPrimary.copy(alpha = 0.12f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arc,
                        style = Stroke(stroke),
                    )
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.value,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arc,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(diameter)
                    .background(
                        if (highlighted) colors.textPrimary else colors.surfaceTint,
                        CircleShape,
                    ),
            ) {
                HeroIconImage(
                    icon = HeroIcon.Stop,
                    size = iconSize,
                    tint = if (highlighted) colors.surface else colors.textPrimary,
                )
            }
        }

        val holdLabel = isPaused || showsHint
        Text(
            text = stringResource(
                if (holdLabel) R.string.control_hold_to_finish else R.string.control_finish,
            ),
            style = if (holdLabel) JejakFont.p3Bold else JejakFont.p3,
            color = if (holdLabel) colors.textPrimary else colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(diameter + 24.dp),
        )
    }

    if (showsHint) {
        LaunchedEffect(Unit) {
            delay(2_000)
            showsHint = false
        }
    }
}

/** Replaces the controls while locked; a one-second hold unlocks. */
@Composable
fun HoldToUnlockBar(isCompact: Boolean, onUnlock: () -> Unit, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    val knob = if (isCompact) 56.dp else 64.dp
    val progress = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(knob + 8.dp)
            .holdGesture(onCompleted = onUnlock, progress = progress, onCancelled = {}),
    ) {
        val filled = knob + 8.dp + (maxWidth - knob - 8.dp) * progress.value
        Box(Modifier.fillMaxWidth().fillMaxHeight().background(colors.surfaceTint, CircleShape))
        Box(
            Modifier
                .width(filled)
                .fillMaxHeight()
                .background(colors.accent.copy(alpha = 0.25f), CircleShape),
        )
        Text(
            text = stringResource(R.string.control_hold_to_unlock),
            style = JejakFont.p1Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(start = knob, end = 16.dp),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(4.dp)
                .size(knob)
                .background(colors.accent, CircleShape),
        ) {
            HeroIconImage(HeroIcon.LockClosed, 26.dp, colors.accentInk)
        }
    }
}

/**
 * Press-and-hold for [HOLD_MILLIS], driving [onProgress] from 0 to 1. Releasing early rewinds and
 * reports [onCancelled], which is how the stop button knows to flash its hint.
 */
private fun Modifier.holdGesture(
    onCompleted: () -> Unit,
    progress: Animatable<Float, AnimationVector1D>,
    onCancelled: () -> Unit,
): Modifier = pointerInput(onCompleted) {
    coroutineScope {
        detectTapGestures(
            onPress = {
                var completed = false
                // Fires the moment the hold is long enough, as SwiftUI's `onLongPressGesture` does,
                // rather than waiting for the finger to come up.
                val hold = launch {
                    progress.animateTo(1f, tween(HOLD_MILLIS, easing = LinearEasing))
                    completed = true
                    onCompleted()
                }
                tryAwaitRelease()
                hold.cancel()
                if (!completed) onCancelled()
                progress.animateTo(0f, tween(200))
            },
        )
    }
}

/** Shown until the first good fix: wait, or start without one. */
@Composable
fun GPSSearchCard(onCancel: () -> Unit, onStartAnyway: () -> Unit, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(12.dp))
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spinner()
            Text(
                text = stringResource(R.string.gps_card_title),
                style = JejakFont.h2Display,
                color = colors.textPrimary,
            )
        }
        Text(
            text = stringResource(R.string.gps_card_message),
            style = JejakFont.p2,
            color = colors.textSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(stringResource(R.string.gps_card_cancel), onCancel, Modifier.weight(1f))
            PrimaryButton(stringResource(R.string.gps_card_start_anyway), onStartAnyway, Modifier.weight(1f))
        }
    }
}

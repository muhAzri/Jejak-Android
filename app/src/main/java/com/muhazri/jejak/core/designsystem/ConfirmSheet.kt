package com.muhazri.jejak.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bottom confirmation card over a scrim (design-system `ConfirmSheet`). Not dismissible by tapping the
 * scrim: the user has to choose one of the two actions.
 */
@Composable
fun ConfirmSheet(
    title: String,
    message: String,
    cancelTitle: String,
    confirmTitle: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scrim),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(bottom = 8.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            ) {
                Text(title, style = JejakFont.h2Display, color = colors.textPrimary)
                Text(message, style = JejakFont.p1, color = colors.textSecondary)
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            ) {
                OutlinedButton(cancelTitle, onCancel, Modifier.weight(1f))
                DangerButton(confirmTitle, onConfirm, Modifier.weight(1f))
            }
        }
    }
}

/** Indeterminate ring (design-system `Spinner`). */
@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    lineWidth: Dp = 4.dp,
    color: Color = JejakTheme.colors.accent,
) {
    val track = JejakTheme.colors.fillInput
    val angle by rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "spinnerAngle",
    )
    Canvas(modifier.size(size).rotate(angle)) {
        val stroke = lineWidth.toPx()
        val inset = stroke / 2
        val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
        drawArc(
            color = track,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke),
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 0.3f * 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

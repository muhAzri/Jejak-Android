package com.muhazri.jejak.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.isSpecified

/**
 * Tap handling without a ripple: Jejak's controls answer a press by scaling, as the SwiftUI button
 * styles do, so the Material indication would read as a second, foreign effect.
 */
fun Modifier.pressable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    onClick = onClick,
)

/**
 * Text that shrinks to fit instead of wrapping or clipping, standing in for SwiftUI's
 * `minimumScaleFactor`. Shrinks in 5% steps until it fits or reaches [minScale].
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minScale: Float = 0.5f,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
) {
    val scale = remember(text, style) { mutableFloatStateOf(1f) }
    val factor = scale.floatValue
    Text(
        text = text,
        style = style.copy(
            fontSize = style.fontSize * factor,
            lineHeight = if (style.lineHeight.isSpecified) style.lineHeight * factor else style.lineHeight,
        ),
        color = color,
        maxLines = maxLines,
        softWrap = maxLines > 1,
        textAlign = textAlign,
        modifier = modifier,
        onTextLayout = { result ->
            if ((result.didOverflowWidth || result.didOverflowHeight) && factor > minScale) {
                scale.floatValue = maxOf(minScale, factor - 0.05f)
            }
        },
    )
}

package com.muhazri.jejak.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Jejak's own tokens, with a Material scheme underneath so stock components (ripple, text selection)
 * pick up the same colors.
 */
@Composable
fun JejakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val jejak = if (darkTheme) DarkJejakColors else LightJejakColors
    val material = if (darkTheme) {
        darkColorScheme(
            primary = jejak.accent,
            onPrimary = jejak.accentInk,
            background = jejak.surface,
            onBackground = jejak.textPrimary,
            surface = jejak.surface,
            onSurface = jejak.textPrimary,
            error = jejak.danger,
            onError = jejak.textOnDanger,
        )
    } else {
        lightColorScheme(
            primary = jejak.accent,
            onPrimary = jejak.accentInk,
            background = jejak.surface,
            onBackground = jejak.textPrimary,
            surface = jejak.surface,
            onSurface = jejak.textPrimary,
            error = jejak.danger,
            onError = jejak.textOnDanger,
        )
    }

    CompositionLocalProvider(LocalJejakColors provides jejak) {
        MaterialTheme(
            colorScheme = material,
            typography = Typography(bodyLarge = JejakFont.p1),
            content = content,
        )
    }
}

package com.muhazri.jejak.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Color tokens from the F0 design, resolved per appearance. */
@Immutable
data class JejakColorScheme(
    val surface: Color,
    /** Page background behind surfaces; the session screen uses it edge to edge. */
    val canvas: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val fillMuted: Color,
    val fillInput: Color,
    val textOnDisabled: Color,
    /** Primary text in light, near-white in dark; strong borders, radio fill and outlined buttons. */
    val strong: Color,
    val surfaceTint: Color,
    val border: Color,
    val accent: Color,
    val accentInk: Color,
    val danger: Color,
    val textOnDanger: Color,
    val scrim: Color,
    val dangerBackground: Color,
    /** Activity colors: Run = orange, Walk = teal. Card backgrounds use them at 12%. */
    val run: Color,
    val walk: Color,
    /** Route pace scale on the summary map: slow -> fast. */
    val paceSlow: Color,
    val paceFast: Color,
    val accentBackground: Color,
)

internal val LightJejakColors = JejakColorScheme(
    surface = Color(0xFFFFFFFF),
    canvas = Color(0xFFEFEFEF),
    textPrimary = Color(0xFF262626),
    textSecondary = Color(0xFF838383),
    fillMuted = Color(0xFFCCCCCC),
    fillInput = Color(0xFFF6F6F6),
    textOnDisabled = Color(0xFFFFFFFF),
    strong = Color(0xFF262626),
    surfaceTint = Color(0x0D000000),
    border = Color(0x0D000000),
    accent = Color(0xFFFFEE00),
    accentInk = Color(0xFF262626),
    danger = Color(0xFFE5484D),
    textOnDanger = Color(0xFFFFFFFF),
    scrim = Color(0x80000000),
    dangerBackground = Color(0xFFFCEFEF),
    run = Color(0xFFF2552C),
    walk = Color(0xFF0E9A8B),
    paceSlow = Color(0xFF9A9A9A),
    paceFast = Color(0xFFDB0826),
    accentBackground = Color(0x1AFFEE00),
)

internal val DarkJejakColors = JejakColorScheme(
    surface = Color(0xFF161616),
    canvas = Color(0xFF0B0B0B),
    textPrimary = Color(0xFFF2F2F2),
    textSecondary = Color(0xFF9A9A9A),
    fillMuted = Color(0xFF3A3A3A),
    fillInput = Color(0xFF242424),
    textOnDisabled = Color(0xFF7A7A7A),
    strong = Color(0xFFF2F2F2),
    surfaceTint = Color(0x0FFFFFFF),
    border = Color(0x14FFFFFF),
    accent = Color(0xFFFFEE00),
    accentInk = Color(0xFF262626),
    danger = Color(0xFFF0676B),
    textOnDanger = Color(0xFFFFFFFF),
    scrim = Color(0xA3000000),
    dangerBackground = Color(0x29E5484D),
    run = Color(0xFFF2552C),
    walk = Color(0xFF0E9A8B),
    paceSlow = Color(0xFF9A9A9A),
    paceFast = Color(0xFFDB0826),
    accentBackground = Color(0x1FFFEE00),
)

internal val LocalJejakColors = staticCompositionLocalOf { LightJejakColors }

/** The active scheme, read as `JejakTheme.colors.accent`. */
object JejakTheme {
    val colors: JejakColorScheme
        @Composable @ReadOnlyComposable get() = LocalJejakColors.current
}

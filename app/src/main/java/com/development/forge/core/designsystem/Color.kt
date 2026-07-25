package com.development.forge.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Token ramps ───────────────────────────────────────────────────────────────

val AmberA50 = Color(0xFFFFF4E0)
val AmberA100 = Color(0xFFFFE0A8)
val AmberA200 = Color(0xFFFFC766)
val AmberA300 = Color(0xFFFFAA2E)
val AmberA400 = Color(0xFFF58A00)   // primary amber
val AmberA500 = Color(0xFFD46E00)
val AmberA600 = Color(0xFFA85100)
val AmberA700 = Color(0xFF6E3500)

// ── Color scheme data class ───────────────────────────────────────────────────

data class ForgeColorScheme(
    // backgrounds
    val bgDeep: Color,
    val bgBase: Color,
    val bgRaised: Color,
    val bgElev: Color,
    val bgPress: Color,
    // foreground
    val fgPrimary: Color,
    val fgSecondary: Color,
    val fgTertiary: Color,
    val fgDisabled: Color,
    val fgOnAmber: Color,
    // borders (pre-computed with alpha)
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    // accent
    val amber: Color,
    val amber200: Color,
    val amberA300: Color,
    val amberA500: Color,
    // semantic
    val danger: Color,
    // oil (navbar background alias)
    val oil: Color
)

// ── Dark theme (default) ──────────────────────────────────────────────────────

val ForgeDarkColors = ForgeColorScheme(
    bgDeep = Color(0xFF0D0B08),
    bgBase = Color(0xFF14110C),
    bgRaised = Color(0xFF1C180F),
    bgElev = Color(0xFF251F14),
    bgPress = Color(0xFF3A3020),
    fgPrimary = Color(0xFFF5EDDE),
    fgSecondary = Color(0xFFB5A990),
    fgTertiary = Color(0xFF7A7060),
    fgDisabled = Color(0xFF4A4338),
    fgOnAmber = Color(0xFF1A1207),
    borderSubtle = Color(0xFFF5EDDE).copy(alpha = 0.06f),
    borderDefault = Color(0xFFF5EDDE).copy(alpha = 0.10f),
    borderStrong = Color(0xFFF5EDDE).copy(alpha = 0.18f),
    amber = Color(0xFFF58A00),
    amber200 = Color(0xFFFFC766),
    amberA300 = Color(0xFFFFAA2E),
    amberA500 = Color(0xFFD46E00),
    danger = Color(0xFFE8624D),
    oil = Color(0xFF1C180F)
)

// ── Light theme ───────────────────────────────────────────────────────────────

val ForgeLightColors = ForgeColorScheme(
    bgDeep = Color(0xFFF0E6D2),
    bgBase = Color(0xFFF5EDDE),
    bgRaised = Color(0xFFEDE4D2),
    bgElev = Color(0xFFE4D9C6),
    bgPress = Color(0xFFD4C9B4),
    fgPrimary = Color(0xFF1A1207),
    fgSecondary = Color(0xFF4A3C28),
    fgTertiary = Color(0xFF7A6848),
    fgDisabled = Color(0xFFB0A080),
    fgOnAmber = Color(0xFF1A1207),
    borderSubtle = Color(0xFF1A1207).copy(alpha = 0.06f),
    borderDefault = Color(0xFF1A1207).copy(alpha = 0.10f),
    borderStrong = Color(0xFF1A1207).copy(alpha = 0.18f),
    amber = Color(0xFFF58A00),
    amber200 = Color(0xFFFFC766),
    amberA300 = Color(0xFFFFAA2E),
    amberA500 = Color(0xFFD46E00),
    danger = Color(0xFFE8624D),
    oil = Color(0xFFEDE4D2)
)

// ── CompositionLocal ──────────────────────────────────────────────────────────

val LocalForgeColors = compositionLocalOf<ForgeColorScheme> { ForgeDarkColors }

// ── Accessor object ───────────────────────────────────────────────────────────

object ForgeTheme {
    val colors: ForgeColorScheme
        @Composable
        get() = LocalForgeColors.current
}

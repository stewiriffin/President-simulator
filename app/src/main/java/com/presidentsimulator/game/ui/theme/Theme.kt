package com.presidentsimulator.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Nation State Simulator visual system: light professional dashboard, blue primary, emerald accent.
 */
private val NssColors = lightColorScheme(
    primary = NssPrimary,
    onPrimary = StarkWhite,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF0E3A7A),
    secondary = NssSky,
    onSecondary = StarkWhite,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = NssAccent,
    onTertiary = StarkWhite,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    background = NssBackground,
    onBackground = NssForeground,
    surface = NssCard,
    onSurface = NssForeground,
    surfaceVariant = NssSecondary,
    onSurfaceVariant = NssMutedForeground,
    outline = NssBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = NssDestructive,
    onError = StarkWhite,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val NssTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        letterSpacing = 1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 8.sp,
        letterSpacing = 1.2.sp,
    ),
)

@Composable
fun PresidentSimulatorTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NssColors,
        typography = NssTypography,
        content = content,
    )
}

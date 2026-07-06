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
 * Nation State Simulator visual system: warm parchment surfaces, navy primary, gold accent.
 */
private val NssColors = lightColorScheme(
    primary = NssPrimary,
    onPrimary = StarkWhite,
    primaryContainer = Color(0xFFD4E0F0),
    onPrimaryContainer = Color(0xFF0F2347),
    secondary = NssSecondary,
    onSecondary = NssForeground,
    secondaryContainer = Color(0xFFEDE4D0),
    onSecondaryContainer = NssForeground,
    tertiary = NssAccent,
    onTertiary = StarkWhite,
    tertiaryContainer = Color(0xFFF5E6C8),
    onTertiaryContainer = Color(0xFF5C3D0A),
    background = NssBackground,
    onBackground = NssForeground,
    surface = NssCard,
    onSurface = NssForeground,
    surfaceVariant = NssSecondary,
    onSurfaceVariant = NssMutedForeground,
    outline = NssBorder,
    outlineVariant = Color(0xFFE6DDC8),
    error = NssDestructive,
    onError = StarkWhite,
    errorContainer = Color(0xFFF5D5D5),
    onErrorContainer = Color(0xFF6B1515),
)

private val NssTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
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

package com.presidentsimulator.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Nation State Simulator visual system: dark command-center chrome, blue primary, emerald accent.
 */
private val NssColors = darkColorScheme(
    primary = NssPrimary,
    onPrimary = StarkWhite,
    primaryContainer = NssSecondary,
    onPrimaryContainer = NssForeground,
    secondary = NssSky,
    onSecondary = StarkWhite,
    tertiary = NssAccent,
    onTertiary = NssBackground,
    background = NssBackground,
    onBackground = NssForeground,
    surface = NssCard,
    onSurface = NssForeground,
    surfaceVariant = NssSecondary,
    onSurfaceVariant = NssMutedForeground,
    outline = NssBorder,
    error = NssDestructive,
    onError = StarkWhite,
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

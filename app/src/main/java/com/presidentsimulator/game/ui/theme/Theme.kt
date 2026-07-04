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
 * Geopolitical war-room theme: deep navy backgrounds, stark white type,
 * and semantic green/red/orange for economic and crisis feedback.
 */
private val WarRoomDarkColors = darkColorScheme(
    primary = CommandGold,
    onPrimary = DeepNavy,
    primaryContainer = NavySurface,
    onPrimaryContainer = StarkWhite,
    secondary = InfoBlue,
    onSecondary = DeepNavy,
    secondaryContainer = SlateGray,
    onSecondaryContainer = StarkWhite,
    tertiary = WarningOrange,
    onTertiary = DeepNavy,
    tertiaryContainer = CrisisCrimson,
    onTertiaryContainer = StarkWhite,
    background = DeepNavy,
    onBackground = StarkWhite,
    surface = NavySurface,
    onSurface = StarkWhite,
    surfaceVariant = NavySurface,
    onSurfaceVariant = NeutralGray,
    outline = SlateOutline,
    error = DeficitRed,
    onError = StarkWhite,
    errorContainer = CrisisCrimson,
    onErrorContainer = StarkWhite,
)

private val WarRoomTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
)

@Composable
fun PresidentSimulatorTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WarRoomDarkColors,
        typography = WarRoomTypography,
        content = content,
    )
}

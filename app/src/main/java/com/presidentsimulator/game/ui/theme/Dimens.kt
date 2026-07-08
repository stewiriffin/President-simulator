package com.presidentsimulator.game.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Central spacing, sizing, and elevation tokens for NSS layouts.
 * Tuned to the scaled-down Figma Make export (Nation State Simulator UI Design zip).
 */
object Dimens {
    val SpacingXSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 20.dp
    val SpacingXLarge = 28.dp

    /** Standard content inset for scrollable ministry bodies. */
    val ContentPadding = SpacingMedium

    /** Gap between cards in 2-column grids / horizontal rows. */
    val GridGap = 8.dp

    /** Vertical rhythm between stacked sections. */
    val SectionGap = 16.dp

    val CardRadius = 16.dp
    val CardElevation = 1.dp
    val CardTonalElevation = 2.dp

    val BadgeCorner = 6.dp
    val PillCorner = 12.dp

    /** Matches reference HUD `h-12` (~48dp) plus inset breathing room. */
    val HudHeight = 48.dp
    val BottomNavHeight = 56.dp

    /** Matches reference ScreenHeader `h-32`. */
    val ScreenHeaderHeight = 128.dp

    /** Matches reference dashboard hero `h-40`. */
    val DashboardHeroHeight = 160.dp

    val SectorCardPhotoHeight = 96.dp
    val MinistryTilePhotoHeight = 80.dp
    val NationCardHeaderHeight = 80.dp
    val UnitCardPhotoHeight = 80.dp
    val SituationThumbWidth = 80.dp
}

/**
 * Standardized photo overlay alphas for readable light text on imagery.
 * Bottom/strong values target ~4.5:1 contrast for white body text.
 */
object PhotoScrimAlpha {
    const val Transparent = 0f
    const val Soft = 0.22f
    const val Medium = 0.40f
    const val Strong = 0.78f
    const val Max = 0.88f

    const val ScreenHeaderTop = 0.60f
    const val ScreenHeaderBottom = 0.78f
    const val BannerLeftStrong = 0.78f
    const val BannerLeftMid = 0.40f
}

package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.Dimens

/**
 * Breakpoints for phone portrait, phone landscape, and larger handsets.
 * Derived from the zip reference scaled for real Android viewports.
 */
@Immutable
data class NssLayoutSpec(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val isNarrowWidth: Boolean,
    val isCompactHeight: Boolean,
    val gridColumns: Int,
    val heroHeight: Dp,
    val screenHeaderHeight: Dp,
    val heroTitleSp: TextUnit,
    val countrySelectHeroHeight: Dp,
)

@Composable
fun rememberNssLayoutSpec(): NssLayoutSpec {
    val config = LocalConfiguration.current
    return remember(config.screenWidthDp, config.screenHeightDp) {
        val w = config.screenWidthDp
        val h = config.screenHeightDp
        val narrow = w < 380
        val compactH = h < 520
        val gridColumns = when {
            narrow -> 1
            w < 600 && compactH -> 1
            else -> 2
        }
        val heroHeight = when {
            compactH -> 120.dp
            h < 640 -> 140.dp
            else -> Dimens.DashboardHeroHeight
        }
        val headerHeight = when {
            compactH -> Dimens.CompactScreenHeaderHeight
            else -> Dimens.ScreenHeaderHeight
        }
        val heroTitle = when {
            narrow -> 28.sp
            compactH -> 32.sp
            else -> 40.sp
        }
        val factionHero = when {
            compactH -> 180.dp
            else -> 220.dp
        }
        NssLayoutSpec(
            screenWidthDp = w,
            screenHeightDp = h,
            isNarrowWidth = narrow,
            isCompactHeight = compactH,
            gridColumns = gridColumns,
            heroHeight = heroHeight,
            screenHeaderHeight = headerHeight,
            heroTitleSp = heroTitle,
            countrySelectHeroHeight = factionHero,
        )
    }
}

/** Bottom padding so scroll content clears the ministry nav bar. */
fun Modifier.nssMinistryScrollPadding(): Modifier =
    this.then(Modifier.padding(bottom = Dimens.MinistryScrollBottomPadding))

val NssLazyListPadding: PaddingValues
    get() = PaddingValues(bottom = Dimens.MinistryScrollBottomPadding)

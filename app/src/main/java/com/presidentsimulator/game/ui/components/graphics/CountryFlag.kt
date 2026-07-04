package com.presidentsimulator.game.ui.components.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.CommandGold
import com.presidentsimulator.game.ui.theme.DeepNavy
import com.presidentsimulator.game.ui.theme.InfoBlue
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.SlateGray
import com.presidentsimulator.game.ui.theme.WarningOrange

/**
 * Displays a nation flag from `res/drawable/flag_<code>`.
 *
 * Looks up `R.drawable.flag_ke` for [countryCode] `"KE"` via [android.content.res.Resources.getIdentifier],
 * which returns `0` when missing — never throws [android.content.res.Resources.NotFoundException].
 * Missing assets fall back to a solid colored tile with the country code initial.
 */
@Composable
fun CountryFlag(
    countryCode: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val normalized = remember(countryCode) {
        countryCode.trim().lowercase().replace('-', '_').replace(' ', '_')
    }
    val drawableName = remember(normalized) { "flag_$normalized" }
    val resId = remember(drawableName, context.packageName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    val shape = RoundedCornerShape(4.dp)
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Flag $countryCode",
            modifier = modifier
                .size(size)
                .clip(shape),
            contentScale = ContentScale.Crop,
        )
    } else {
        FlagFallback(
            countryCode = countryCode,
            size = size,
            modifier = modifier,
        )
    }
}

@Composable
private fun FlagFallback(
    countryCode: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val initial = countryCode.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val background = remember(countryCode) { fallbackColorForCode(countryCode) }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/**
 * Deterministic accent color so the same code always renders the same placeholder.
 */
private fun fallbackColorForCode(countryCode: String): Color {
    val palette = listOf(
        DeepNavy,
        SlateGray,
        InfoBlue,
        ProfitGreen,
        WarningOrange,
        CommandGold,
        Color(0xFF7C3AED),
        Color(0xFF0EA5E9),
    )
    val index = countryCode.trim().uppercase().sumOf { it.code } % palette.size
    return palette[index]
}

/**
 * Maps in-game rival IDs to ISO-style codes used by drawable names (`flag_nl`, etc.).
 * Drop real PNGs as `flag_<code>` under `res/drawable` when assets are ready.
 */
fun rivalIdToCountryCode(rivalId: String): String = when (rivalId.lowercase()) {
    "northland" -> "NL"
    "eastmark" -> "EM"
    "southreach" -> "SR"
    "westoria" -> "WO"
    "player", "your_nation" -> "US"
    else -> rivalId.take(2).uppercase().ifBlank { "XX" }
}

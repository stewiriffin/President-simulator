package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssDestructive
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.theme.NssSky

@Composable
fun NssCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard)
            .padding(16.dp),
    ) {
        content()
    }
}

@Composable
fun NssSectionHead(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = NssForeground,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun NssBadge(
    label: String,
    large: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = badgeColors(label)
    Text(
        text = label.uppercase(),
        modifier = modifier
            .border(1.dp, colors.border)
            .background(colors.background)
            .padding(
                horizontal = if (large) 8.dp else 6.dp,
                vertical = if (large) 2.dp else 1.dp,
            ),
        color = colors.text,
        fontSize = if (large) 10.sp else 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun NssProgressBar(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val clamped = percent.coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(NssSecondary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped / 100f)
                .background(color),
        )
    }
}

@Composable
fun NssMiniProgressBar(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(80.dp)
            .height(3.dp)
            .background(NssSecondary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                .background(color),
        )
    }
}

@Composable
fun NssTabBar(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NssCard.copy(alpha = 0.6f))
            .border(width = 0.dp, color = Color.Transparent)
            .padding(bottom = 0.dp),
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) NssPrimary else NssMutedForeground,
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(1.dp)
                                .background(NssPrimary),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NssBorder)
                .align(Alignment.Bottom),
        )
    }
}

@Composable
fun NssHeroBanner(
    ministryLabel: String,
    stats: List<HeroStat>,
    accentColor: Color = NssPrimary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        NssBackground,
                        NssSecondary,
                        accentColor.copy(alpha = 0.25f),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NssBackground.copy(alpha = 0.85f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        ) {
            Text(
                text = "MINISTRY OF",
                style = MaterialTheme.typography.labelSmall,
                color = NssMutedForeground,
            )
            Text(
                text = ministryLabel.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = NssForeground,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.forEach { stat ->
                    Column(
                        modifier = Modifier
                            .border(1.dp, NssBorder)
                            .background(NssBackground.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = stat.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = NssMutedForeground,
                        )
                        Text(
                            text = stat.value,
                            color = when (stat.positive) {
                                true -> NssEmerald
                                false -> NssRed
                                null -> NssForeground
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NssKpiCard(
    label: String,
    value: String,
    delta: String,
    positive: Boolean?,
    modifier: Modifier = Modifier,
) {
    NssCard(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = NssMutedForeground,
        )
        Text(
            text = value,
            color = NssForeground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        Text(
            text = delta,
            style = MaterialTheme.typography.bodySmall,
            color = when (positive) {
                true -> NssEmerald
                false -> NssRed
                null -> NssMutedForeground
            },
        )
    }
}

@Composable
fun NssAlertBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NssAmber.copy(alpha = 0.2f))
            .background(NssAmber.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "⚠",
            color = NssAmber,
            fontSize = 14.sp,
        )
        Text(
            text = message.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = NssAmber.copy(alpha = 0.85f),
        )
    }
}

data class HeroStat(
    val label: String,
    val value: String,
    val positive: Boolean? = null,
)

private data class BadgePalette(
    val background: Color,
    val text: Color,
    val border: Color,
)

private fun badgeColors(label: String): BadgePalette {
    val upper = label.uppercase()
    return when {
        listOf("ALLY", "ALLIED", "ACTIVE", "LOW", "OPEN", "COMBAT READY", "PARTNER").any { upper.contains(it) } ->
            BadgePalette(NssEmerald.copy(alpha = 0.15f), NssEmerald, NssEmerald.copy(alpha = 0.3f))
        listOf("PATROL", "ADVANCED", "FINAL", "INFO").any { upper.contains(it) } ->
            BadgePalette(NssSky.copy(alpha = 0.15f), NssSky, NssSky.copy(alpha = 0.3f))
        listOf("NEUTRAL", "TRAINING", "STALLED", "WARN", "REVIEW", "MEDIUM").any { upper.contains(it) } ->
            BadgePalette(NssMutedForeground.copy(alpha = 0.15f), NssMutedForeground, NssMutedForeground.copy(alpha = 0.3f))
        listOf("RIVAL", "HIGH", "RESTRICTED").any { upper.contains(it) } ->
            BadgePalette(NssAmber.copy(alpha = 0.15f), NssAmber, NssAmber.copy(alpha = 0.3f))
        listOf("HOSTILE", "CRITICAL", "CRIT", "EMBARGO", "CONFLICT").any { upper.contains(it) } ->
            BadgePalette(NssDestructive.copy(alpha = 0.15f), NssRed, NssDestructive.copy(alpha = 0.3f))
        else ->
            BadgePalette(NssMutedForeground.copy(alpha = 0.15f), NssMutedForeground, NssMutedForeground.copy(alpha = 0.3f))
    }
}

fun relationBarColor(value: Int): Color =
    when {
        value >= 70 -> NssEmerald
        value >= 40 -> NssAmber
        else -> NssRed
    }

fun strengthBarColor(value: Int): Color =
    when {
        value >= 90 -> NssEmerald
        value >= 75 -> NssAmber
        else -> NssRed
    }

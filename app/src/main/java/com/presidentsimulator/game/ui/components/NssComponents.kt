package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.presidentsimulator.game.ui.theme.NssIndigo
import com.presidentsimulator.game.ui.theme.NssMuted
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOrange
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.theme.NssSky
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.ui.theme.NssOnPhoto

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
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = NssForeground)
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
            .padding(horizontal = if (large) 8.dp else 6.dp, vertical = if (large) 2.dp else 1.dp),
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
    thick: Boolean = false,
) {
    val clamped = percent.coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (thick) 8.dp else 3.dp)
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
fun NssTabBar(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NssMuted),
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
                            fontSize = 11.sp,
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
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NssBorder))
    }
}

@Composable
fun NssMinistryBanner(
    ministryLabel: String,
    statPills: List<String>,
    imageUrl: String? = null,
    gradientColors: List<Color> = listOf(NssBackground, NssSecondary, NssPrimary.copy(alpha = 0.35f)),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = gradientColors,
            modifier = Modifier.matchParentSize(),
            scrimLeftToRight = MinistryBannerLeftScrim,
            scrimTopToBottom = MinistryBannerBottomScrim,
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
                letterSpacing = 4.sp,
            )
            Text(
                text = ministryLabel.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = NssOnPhoto,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statPills.forEach { pill ->
                    Text(
                        text = pill,
                        modifier = Modifier
                            .background(NssForeground.copy(alpha = 0.72f))
                            .border(1.dp, NssOnPhoto.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        color = NssOnPhoto.copy(alpha = 0.95f),
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun NssCompactKpi(
    label: String,
    value: String,
    delta: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
        Text(
            text = value,
            color = NssForeground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        Text(
            text = delta,
            fontSize = 10.sp,
            color = if (positive) NssEmerald else NssRed,
        )
    }
}

@Composable
fun NssStripPhotoCard(
    imageUrl: String?,
    fallbackGradient: List<Color> = listOf(NssSecondary, NssCard),
    modifier: Modifier = Modifier,
    headerHeight: Dp = 72.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = fallbackGradient,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),
            scrimTopToBottom = listOf(Color.Transparent, NssCard.copy(alpha = 0.85f)),
        )
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun NssSectorCard(
    name: String,
    gdpShare: Float,
    employment: Float,
    growth: Float,
    level: Int,
    headerGradient: List<Color>,
    imageUrl: String? = null,
    onInvest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var invested by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                tintGradient = listOf(headerGradient.first().copy(alpha = 0.75f), Color.Transparent),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            Text(
                text = "${if (growth >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(growth))}% YOY",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .border(1.dp, if (growth >= 0) NssEmerald.copy(alpha = 0.4f) else NssRed.copy(alpha = 0.4f))
                    .background(if (growth >= 0) NssEmerald.copy(alpha = 0.25f) else NssRed.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = if (growth >= 0) NssEmerald else NssRed,
                fontSize = 9.sp,
            )
            Text(
                text = name,
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(NssSecondary.copy(alpha = 0.5f))
                        .padding(8.dp),
                ) {
                    Text("GDP SHARE", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                    Text("${"%.1f".format(gdpShare)}%", color = NssForeground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(NssSecondary.copy(alpha = 0.5f))
                        .padding(8.dp),
                ) {
                    Text("EMPLOYMENT", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                    Text("${"%.1f".format(employment)}%", color = NssForeground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("INVESTMENT LEVEL", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text("$level / 5", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(if (index < level) NssPrimary else NssSecondary),
                    )
                }
            }
        }
        Text(
            text = if (invested) "✓ INVESTING" else "INVEST ▸",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    invested = !invested
                    onInvest()
                }
                .background(if (invested) NssPrimary else Color.Transparent)
                .border(width = 0.dp, color = Color.Transparent)
                .padding(vertical = 10.dp),
            color = if (invested) NssOnPhoto else NssPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NssBranchHeader(
    branch: String,
    unitCount: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        Text(
            text = "$branch BRANCH",
            color = accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(NssBorder))
        Text(
            text = "$unitCount UNITS",
            style = MaterialTheme.typography.labelSmall,
            color = NssMutedForeground,
        )
    }
}

@Composable
fun NssUnitCard(
    unitName: String,
    branch: String,
    count: Int,
    strength: Int,
    status: String,
    maintLabel: String,
    headerGradient: List<Color>,
    accentColor: Color,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssBadge(label = branch, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            Text(
                text = "×$count",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(NssForeground.copy(alpha = 0.72f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Text(
                text = unitName,
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("STRENGTH", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.width(56.dp))
                NssProgressBar(percent = strength.toFloat(), color = strengthBarColor(strength), thick = true, modifier = Modifier.weight(1f))
                Text(
                    text = "$strength%",
                    color = strengthTextColor(strength),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NssBadge(label = status)
                Text(maintLabel, style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().border(width = 0.dp, color = Color.Transparent)) {
            Text(
                text = "REDEPLOY",
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                color = NssPrimary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(NssBorder))
            Text(
                text = "UPGRADE ▸",
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                color = NssAmber,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun NssRecruitCard(
    name: String,
    branch: String,
    costLabel: String,
    buildMonths: Int,
    maintLabel: String,
    quantity: Int,
    headerGradient: List<Color>,
    accentColor: Color,
    imageUrl: String? = null,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (quantity > 0) NssPrimary.copy(alpha = 0.6f) else NssBorder,
            )
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssBadge(label = branch, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            if (quantity > 0) {
                Text(
                    text = "×$quantity QUEUED",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(NssPrimary)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(name, color = NssOnPhoto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(costLabel, color = NssOnPhoto.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${buildMonths}mo build", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(maintLabel, style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
            if (quantity > 0) {
                Text(
                    text = "SUBTOTAL: $costLabel",
                    color = NssPrimary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, NssBorder)
                        .background(NssSecondary)
                        .clickable { onQuantityChange(-1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = NssMutedForeground, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = quantity.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NssBorder.copy(alpha = 0.5f))
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, NssBorder)
                        .background(NssSecondary)
                        .clickable { onQuantityChange(1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NssMutedForeground, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun NssNationCard(
    nationName: String,
    flagEmoji: String,
    status: String,
    threat: String,
    relations: Int,
    tradeLabel: String,
    militaryLabel: String,
    headerColor: Color,
    isHostile: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (isHostile) NssRed.copy(alpha = 0.5f) else NssBorder,
            )
            .background(NssCard)
            .clickable(onClick = onAction),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(headerColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = flagEmoji, fontSize = 48.sp)
            Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, NssCard.copy(alpha = 0.8f)))))
            NssBadge(label = status, large = true, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            NssBadge(label = threat, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
            Text(
                text = nationName,
                color = NssForeground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("RELATIONS", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.width(64.dp))
                NssProgressBar(percent = relations.toFloat(), color = relationBarColor(relations), thick = true, modifier = Modifier.weight(1f))
                Text(
                    text = relations.toString(),
                    color = relationTextColor(relations),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TRADE", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(tradeLabel, style = MaterialTheme.typography.labelSmall, color = NssForeground)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MILITARY", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(militaryLabel, style = MaterialTheme.typography.labelSmall, color = NssForeground)
            }
        }
        Text(
            text = if (isHostile) "ESCALATE / NEGOTIATE ▸" else "OPEN DIALOGUE ▸",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            color = if (isHostile) NssRed else NssPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
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
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelMedium, color = NssMutedForeground)
        Text(text = value, color = NssForeground, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
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
            .border(1.dp, NssAmber.copy(alpha = 0.3f))
            .background(NssAmber.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "⚠", color = NssAmber, fontSize = 14.sp)
        Text(text = message.uppercase(), style = MaterialTheme.typography.labelMedium, color = NssAmber)
    }
}

@Composable
fun NssHeroBanner(
    ministryLabel: String,
    stats: List<HeroStat>,
    accentColor: Color = NssPrimary,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    NssMinistryBanner(
        ministryLabel = ministryLabel,
        statPills = stats.map { "${it.label}: ${it.value}" },
        imageUrl = imageUrl,
        gradientColors = listOf(NssBackground, NssSecondary, accentColor.copy(alpha = 0.35f)),
        modifier = modifier,
    )
}

data class HeroStat(val label: String, val value: String, val positive: Boolean? = null)

private data class BadgePalette(val background: Color, val text: Color, val border: Color)

private fun badgeColors(label: String): BadgePalette {
    val upper = label.uppercase()
    return when {
        listOf("ALLY", "ALLIED", "ACTIVE", "LOW", "OPEN", "COMBAT READY", "PARTNER").any { upper.contains(it) } ->
            BadgePalette(NssEmerald.copy(alpha = 0.15f), NssEmerald, NssEmerald.copy(alpha = 0.35f))
        listOf("PATROL", "ADVANCED", "FINAL", "INFO").any { upper.contains(it) } ->
            BadgePalette(NssSky.copy(alpha = 0.15f), NssSky, NssSky.copy(alpha = 0.35f))
        listOf("NEUTRAL", "TRAINING", "STALLED", "WARN", "REVIEW", "MEDIUM", "NONE").any { upper.contains(it) } ->
            BadgePalette(NssMutedForeground.copy(alpha = 0.12f), NssMutedForeground, NssMutedForeground.copy(alpha = 0.3f))
        listOf("RIVAL", "HIGH", "RESTRICTED", "STANDOFF").any { upper.contains(it) } ->
            BadgePalette(NssAmber.copy(alpha = 0.15f), NssAmber, NssAmber.copy(alpha = 0.35f))
        listOf("HOSTILE", "CRITICAL", "CRIT", "EMBARGO", "CONFLICT").any { upper.contains(it) } ->
            BadgePalette(NssDestructive.copy(alpha = 0.12f), NssDestructive, NssDestructive.copy(alpha = 0.35f))
        else ->
            BadgePalette(NssMutedForeground.copy(alpha = 0.12f), NssMutedForeground, NssMutedForeground.copy(alpha = 0.3f))
    }
}

fun relationBarColor(value: Int): Color = when {
    value >= 70 -> NssEmerald
    value >= 40 -> NssAmber
    else -> NssRed
}

fun relationTextColor(value: Int): Color = when {
    value >= 70 -> NssEmerald
    value >= 40 -> NssAmber
    else -> NssRed
}

fun strengthBarColor(value: Int): Color = when {
    value >= 90 -> NssEmerald
    value >= 75 -> NssAmber
    else -> NssRed
}

fun strengthTextColor(value: Int): Color = relationTextColor(value)

fun prgColor(value: Int): Color = when {
    value >= 80 -> NssEmerald
    value >= 50 -> NssSky
    value >= 25 -> NssAmber
    else -> NssRed
}

object NssGradients {
    val Emerald = listOf(Color(0xFFD1FAE5), Color(0xFF6EE7B7), NssCard)
    val Sky = listOf(Color(0xFFE0F2FE), Color(0xFF7DD3FC), NssCard)
    val Indigo = listOf(Color(0xFFE0E7FF), Color(0xFFA5B4FC), NssCard)
    val Violet = listOf(Color(0xFFEDE9FE), Color(0xFFC4B5FD), NssCard)
    val Amber = listOf(Color(0xFFFEF3C7), Color(0xFFFCD34D), NssCard)
    val Orange = listOf(Color(0xFFFFEDD5), Color(0xFFFDBA74), NssCard)
    val Red = listOf(Color(0xFFFEE2E2), Color(0xFFFCA5A5), NssCard)
    val Neutral = listOf(NssSecondary, NssBorder, NssCard)
    val Economy = listOf(NssBackground, Color(0xFFDBEAFE), NssPrimary.copy(alpha = 0.25f))
    val Defense = listOf(NssBackground, Color(0xFFFEE2E2), NssRed.copy(alpha = 0.18f))
    val Foreign = listOf(NssBackground, Color(0xFFE0F2FE), NssSky.copy(alpha = 0.2f))
}

object NssNationColors {
    val Ally = Color(0xFFDBEAFE)
    val Partner = Color(0xFFD1FAE5)
    val Neutral = Color(0xFFE2E8F0)
    val Rival = Color(0xFFFEF3C7)
    val Hostile = Color(0xFFFEE2E2)
}

package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.collectAlertCount
import com.presidentsimulator.game.ui.components.collectAlerts
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import kotlin.math.roundToInt

/**
 * Gamified command center — v3 design reference (mobile-first, game cards not stats).
 */
@Composable
fun MainDashboardScreen(
    state: GameState,
    onNavigate: (GameDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gdp = remember(state) { AnalyticsSaveViewModel().calculateGDP(state) }
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)
    val milPower = state.effectiveCombatStrength.roundToInt()
    val alertCount = collectAlertCount(state)
    val situations = remember(state) { buildSituations(state) }
    val quarter = ((state.month - 1) / 3) + 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(208.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.MAP,
                fallbackGradient = listOf(NssPrimary.copy(alpha = 0.5f), NssBackground),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = listOf(
                    NssPrimary.copy(alpha = 0.35f),
                    Color.Transparent,
                    NssBackground.copy(alpha = 0.88f),
                ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "THE REPUBLIC OF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NssOnPhoto.copy(alpha = 0.8f),
                    letterSpacing = 5.sp,
                )
                Text(
                    text = "VELTRIA",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp,
                    color = NssOnPhoto,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Year ${state.year} · Quarter $quarter",
                    fontSize = 13.sp,
                    color = NssOnPhoto.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeroBadge("⚔ $alertCount Active Events", NssAccent)
                    HeroBadge("👑 Rank #14", NssOnPhoto.copy(alpha = 0.2f), border = true)
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DashboardSection(
                title = "Empire Status",
                subtitle = "Turn ${state.year}.$quarter",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GameVitalCard(
                            icon = Icons.Default.AttachMoney,
                            label = "Treasury",
                            value = formatCompactMoney(state.vitals.budget),
                            sub = if (state.netIncome < 0) "↓ Declining" else "↑ Growing",
                            pct = budgetPct(state),
                            color = NssAccent,
                            warn = state.netIncome < 0,
                            modifier = Modifier.weight(1f),
                            delayMs = 0,
                        )
                        GameVitalCard(
                            icon = Icons.Default.Shield,
                            label = "Stability",
                            value = "${stability.roundToInt()}%",
                            sub = if (stability < 60f) "↓ At risk" else "↑ Steady",
                            pct = stability,
                            color = Color(0xFFD97706),
                            warn = stability < 60f,
                            modifier = Modifier.weight(1f),
                            delayMs = 70,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GameVitalCard(
                            icon = Icons.Default.SportsMartialArts,
                            label = "Mil. Power",
                            value = milPower.toString(),
                            sub = "↑ Increasing",
                            pct = (milPower / 100f).coerceIn(0f, 100f),
                            color = NssPrimary,
                            warn = false,
                            modifier = Modifier.weight(1f),
                            delayMs = 140,
                        )
                        GameVitalCard(
                            icon = Icons.Default.Groups,
                            label = "Approval",
                            value = state.vitals.approval.toApprovalString(),
                            sub = if (state.vitals.approval >= 50f) "↑ Steady" else "↓ Falling",
                            pct = state.vitals.approval.coerceIn(0f, 100f),
                            color = NssEmerald,
                            warn = state.vitals.approval < 50f,
                            modifier = Modifier.weight(1f),
                            delayMs = 210,
                        )
                    }
                }
            }

            if (situations.isNotEmpty()) {
                DashboardSection(
                    title = "Active Situations",
                    subtitle = "${situations.size.coerceAtMost(3)} require action",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        situations.take(3).forEach { situation ->
                            HorizontalSituationCard(
                                situation = situation,
                                onAction = { onNavigate(situation.destination) },
                            )
                        }
                    }
                }
            }

            DashboardSection(title = "Ministries") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MinistryTile(
                            label = "Economy",
                            subtitle = "GDP ${formatCompactMoney(gdp)}",
                            imageUrl = NssCardImages.BANNER_ECONOMY,
                            icon = Icons.Default.AttachMoney,
                            badge = null,
                            onClick = { onNavigate(GameDestination.Economy) },
                            modifier = Modifier.weight(1f),
                        )
                        MinistryTile(
                            label = "Defense",
                            subtitle = "Power $milPower",
                            imageUrl = NssCardImages.BANNER_DEFENSE,
                            icon = Icons.Default.Shield,
                            badge = if (state.diplomacy.activeWar != null) "1 Alert" else null,
                            badgeColor = Color(0xFFF59E0B),
                            onClick = { onNavigate(GameDestination.Military) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MinistryTile(
                            label = "Foreign",
                            subtitle = "${state.diplomacy.rivals.count { it.relationshipScore >= 70 }} Allies",
                            imageUrl = NssCardImages.BANNER_FOREIGN,
                            icon = Icons.Default.Public,
                            badge = if (state.diplomacy.rivals.any { it.relationshipScore < 20 }) "Crisis" else null,
                            badgeColor = NssRed,
                            onClick = { onNavigate(GameDestination.Diplomacy) },
                            modifier = Modifier.weight(1f),
                        )
                        MinistryTile(
                            label = "Domestic",
                            subtitle = "Policy & society",
                            imageUrl = NssCardImages.BANNER_DOMESTIC,
                            icon = Icons.Default.AccountBalance,
                            badge = null,
                            onClick = { onNavigate(GameDestination.LawsSociety) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            DashboardSection(title = "More Ministries") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MinistryTile(
                            label = "Science",
                            subtitle = "Research",
                            imageUrl = NssCardImages.BANNER_SCIENCE,
                            icon = Icons.Default.Science,
                            onClick = { onNavigate(GameDestination.Science) },
                            modifier = Modifier.weight(1f),
                        )
                        MinistryTile(
                            label = "Intel",
                            subtitle = "Classified",
                            imageUrl = NssCardImages.BANNER_INTELLIGENCE,
                            icon = Icons.Default.Shield,
                            badge = "Locked",
                            badgeColor = Color(0xFF57534E),
                            onClick = { onNavigate(GameDestination.SecretService) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MinistryTile(
                            label = "United Nations",
                            subtitle = "Global vote",
                            imageUrl = NssCardImages.BANNER_FOREIGN,
                            icon = Icons.Default.Gavel,
                            onClick = { onNavigate(GameDestination.Governance) },
                            modifier = Modifier.weight(1f),
                        )
                        MinistryTile(
                            label = "Settings",
                            subtitle = "Audio",
                            imageUrl = NssCardImages.BANNER_COMMAND,
                            icon = Icons.Default.Settings,
                            onClick = { onNavigate(GameDestination.AudioSettings) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private fun budgetPct(state: GameState): Float {
    val budget = state.vitals.budget.coerceAtLeast(1L)
    return (budget.toFloat() / (budget + budget.coerceAtLeast(1L)) * 100f).coerceIn(20f, 100f)
}

@Composable
private fun HeroBadge(text: String, bg: Color, border: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(if (border) Modifier.border(1.dp, NssOnPhoto.copy(alpha = 0.3f), RoundedCornerShape(50)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = NssOnPhoto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
    )
}

@Composable
private fun DashboardSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 3.sp,
            )
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 10.sp, color = NssMutedForeground, fontWeight = FontWeight.Medium)
            }
        }
        content()
    }
}

@Composable
private fun GameVitalCard(
    icon: ImageVector,
    label: String,
    value: String,
    sub: String,
    pct: Float,
    color: Color,
    warn: Boolean,
    delayMs: Int,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "warnPulse")
    val warnAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "warnAlpha",
    )

    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(NssGameCard)
            .then(if (warn) Modifier.border(2.dp, Color(0xFFFDE68A), NssCardShape) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            if (warn) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(warnAlpha)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFBBF24)),
                )
            }
        }
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF292524))
        Text(
            text = "$label · $sub",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = NssMutedForeground,
        )
        NssGameBar(percent = pct, color = color, animationDelayMs = delayMs)
    }
}

private data class DashboardSituation(
    val severity: String,
    val emoji: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val action: String,
    val destination: GameDestination,
    val accentColor: Color,
)

private fun buildSituations(state: GameState): List<DashboardSituation> {
    val items = mutableListOf<DashboardSituation>()
    state.diplomacy.activeWar?.let { war ->
        val name = state.diplomacy.rivalById(war.targetCountryId)?.name ?: "enemy forces"
        items += DashboardSituation(
            severity = "CRISIS",
            emoji = "🚨",
            title = "War Against $name!",
            description = "Active conflict requires military response this turn.",
            imageUrl = NssCardImages.INFANTRY,
            action = "⚔ Deploy",
            destination = GameDestination.Military,
            accentColor = NssRed,
        )
    }
    if (state.internalSecurity.coupRisk >= 60f) {
        items += DashboardSituation(
            severity = "WARNING",
            emoji = "⚠️",
            title = "Coup Risk Elevated",
            description = "Instability at ${state.internalSecurity.coupRisk.roundToInt()}% — review domestic policy.",
            imageUrl = NssCardImages.PARLIAMENT,
            action = "💬 Review",
            destination = GameDestination.LawsSociety,
            accentColor = NssAccent,
        )
    }
    if (state.production.foodShortage) {
        items += DashboardSituation(
            severity = "WARNING",
            emoji = "⚠️",
            title = "Food Shortages",
            description = "Agricultural shortfall is driving public unrest nationwide.",
            imageUrl = NssCardImages.AGRICULTURE,
            action = "💬 Economy",
            destination = GameDestination.Economy,
            accentColor = NssAccent,
        )
    }
    if (state.netIncome > 0 && items.size < 3) {
        items += DashboardSituation(
            severity = "OPPORTUNITY",
            emoji = "🚀",
            title = "Treasury Surplus!",
            description = "Positive cash flow — invest in growth sectors now.",
            imageUrl = NssCardImages.TECHNOLOGY,
            action = "⬆ Invest",
            destination = GameDestination.Economy,
            accentColor = NssEmerald,
        )
    }
    collectAlerts(state).forEach { (level, message) ->
        if (items.size >= 3) return@forEach
        if (items.any { it.title == message }) return@forEach
        items += DashboardSituation(
            severity = when (level) {
                "CRIT" -> "CRISIS"
                "WARN" -> "WARNING"
                else -> "OPPORTUNITY"
            },
            emoji = if (level == "CRIT") "🚨" else "ℹ️",
            title = message.take(36),
            description = message,
            imageUrl = NssCardImages.BANNER_FOREIGN,
            action = "Respond",
            destination = GameDestination.Dashboard,
            accentColor = if (level == "CRIT") NssRed else NssAccent,
        )
    }
    return items
}

@Composable
private fun HorizontalSituationCard(
    situation: DashboardSituation,
    onAction: () -> Unit,
) {
    val isCrisis = situation.severity == "CRISIS"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .then(if (isCrisis) Modifier.border(2.dp, Color(0xFFFECACA), NssCardShape) else Modifier)
            .background(NssGameCard),
    ) {
        Box(modifier = Modifier.width(112.dp).height(120.dp)) {
            NssPhotoHeader(
                imageUrl = situation.imageUrl,
                fallbackGradient = listOf(situation.accentColor.copy(alpha = 0.5f), NssGameCard),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                Text(situation.emoji, fontSize = 16.sp)
                Column {
                    Text(
                        text = situation.severity,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(situation.accentColor)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                        color = NssOnPhoto,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = situation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NssForeground,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Text(
                text = situation.description,
                fontSize = 11.sp,
                color = NssMutedForeground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
            )
            Text(
                text = "${situation.action} →",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(situation.accentColor)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = NssOnPhoto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun MinistryTile(
    label: String,
    subtitle: String,
    imageUrl: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    badgeColor: Color = NssAccent,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(NssGameCard)
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = listOf(NssPrimary.copy(alpha = 0.3f), NssGameCard),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = listOf(Color.Transparent, NssPrimary.copy(alpha = 0.55f)),
            )
            if (badge != null) {
                Text(
                    text = badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = NssOnPhoto,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NssOnPhoto,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .size(20.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NssForeground)
                Text(text = subtitle, fontSize = 10.sp, color = NssMutedForeground, fontWeight = FontWeight.Medium)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFD6D3D1),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

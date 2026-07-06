package com.presidentsimulator.game.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.collectAlertCount
import com.presidentsimulator.game.ui.components.collectAlerts
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import kotlin.math.roundToInt

/**
 * Command center overview matching the NSS v2 design reference.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.MAP,
                fallbackGradient = listOf(NssPrimary.copy(alpha = 0.3f), NssBackground),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = listOf(NssBackground.copy(alpha = 0.6f), Color.Transparent, NssBackground.copy(alpha = 0.8f)),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "THE REPUBLIC OF",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssPrimary.copy(alpha = 0.8f),
                    letterSpacing = 4.sp,
                )
                Text(
                    text = "VELTRIA",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Chancellor M. Draven · $alertCount matters require attention",
                    fontSize = 13.sp,
                    color = NssForeground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DashboardSection(title = "Empire Status") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EmpireVitalCard(
                        icon = Icons.Default.AttachMoney,
                        label = "Treasury",
                        value = formatCompactMoney(state.vitals.budget),
                        trend = if (state.vitals.budget > 0) {
                            state.netIncome.toFloat() / state.vitals.budget * 100f
                        } else {
                            0f
                        },
                        warn = state.netIncome < 0,
                        bg = Color(0xFFFFFBEB),
                        border = Color(0xFFFDE68A),
                        iconTint = NssAccent,
                        modifier = Modifier.weight(1f),
                    )
                    EmpireVitalCard(
                        icon = Icons.Default.SportsMartialArts,
                        label = "Mil. Power",
                        value = milPower.toString(),
                        trend = 1f,
                        warn = false,
                        bg = Color(0xFFEFF6FF),
                        border = Color(0xFFBFDBFE),
                        iconTint = NssPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    EmpireVitalCard(
                        icon = Icons.Default.Shield,
                        label = "Stability",
                        value = "${stability.roundToInt()}%",
                        trend = -state.internalSecurity.instabilityScore,
                        warn = stability < 60f,
                        bg = Color(0xFFFFF7ED),
                        border = Color(0xFFFED7AA),
                        iconTint = NssAccent,
                        modifier = Modifier.weight(1f),
                    )
                    EmpireVitalCard(
                        icon = Icons.Default.Groups,
                        label = "Approval",
                        value = state.vitals.approval.toApprovalString(),
                        trend = 1f,
                        warn = state.vitals.approval < 50f,
                        bg = Color(0xFFECFDF5),
                        border = Color(0xFF86EFAC),
                        iconTint = NssEmerald,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (situations.isNotEmpty()) {
                DashboardSection(title = "Active Situations") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        situations.take(3).forEach { situation ->
                            SituationCard(
                                situation = situation,
                                onAction = { onNavigate(situation.destination) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            DashboardSection(title = "Ministries") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MinistryShortcut(
                        label = "Economy",
                        subtitle = "GDP ${formatCompactMoney(gdp)}",
                        imageUrl = NssCardImages.BANNER_ECONOMY,
                        icon = Icons.Default.AttachMoney,
                        onClick = { onNavigate(GameDestination.Economy) },
                        modifier = Modifier.weight(1f),
                    )
                    MinistryShortcut(
                        label = "Defense",
                        subtitle = "$milPower power · ${state.military.morale.roundToInt()}% ready",
                        imageUrl = NssCardImages.BANNER_DEFENSE,
                        icon = Icons.Default.Shield,
                        onClick = { onNavigate(GameDestination.Military) },
                        modifier = Modifier.weight(1f),
                    )
                    MinistryShortcut(
                        label = "Foreign Affs.",
                        subtitle = "${state.diplomacy.rivals.count { it.relationshipScore >= 70 }} allies",
                        imageUrl = NssCardImages.BANNER_FOREIGN,
                        icon = Icons.Default.Public,
                        onClick = { onNavigate(GameDestination.Diplomacy) },
                        modifier = Modifier.weight(1f),
                    )
                    MinistryShortcut(
                        label = "Domestic",
                        subtitle = "Policy & society",
                        imageUrl = NssCardImages.BANNER_DOMESTIC,
                        icon = Icons.Default.AccountBalance,
                        onClick = { onNavigate(GameDestination.LawsSociety) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MinistryShortcut(
                        label = "Science",
                        subtitle = "Research programs",
                        imageUrl = NssCardImages.BANNER_SCIENCE,
                        icon = Icons.Default.Science,
                        onClick = { onNavigate(GameDestination.Science) },
                        modifier = Modifier.weight(1f),
                    )
                    MinistryShortcut(
                        label = "Intelligence",
                        subtitle = "Classified ops",
                        imageUrl = NssCardImages.BANNER_INTELLIGENCE,
                        icon = Icons.Default.Shield,
                        onClick = { onNavigate(GameDestination.SecretService) },
                        modifier = Modifier.weight(1f),
                    )
                    MinistryShortcut(
                        label = "United Nations",
                        subtitle = "Global governance",
                        imageUrl = NssCardImages.BANNER_FOREIGN,
                        icon = Icons.Default.Gavel,
                        onClick = { onNavigate(GameDestination.Governance) },
                        modifier = Modifier.weight(1f),
                    )
                    MinistryShortcut(
                        label = "Settings",
                        subtitle = "Audio & system",
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

@Composable
private fun DashboardSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NssMutedForeground,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold,
        )
        content()
    }
}

@Composable
private fun EmpireVitalCard(
    icon: ImageVector,
    label: String,
    value: String,
    trend: Float,
    warn: Boolean,
    bg: Color,
    border: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .border(2.dp, border, NssCardShape)
            .background(bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (warn) NssAccent else iconTint, modifier = Modifier.size(24.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NssForeground, textAlign = TextAlign.Center)
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = NssMutedForeground)
        Text(
            text = "${if (trend >= 0) "▲" else "▼"} ${"%.0f".format(kotlin.math.abs(trend))}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (trend >= 0) NssEmerald else NssRed,
        )
    }
}

private data class DashboardSituation(
    val severity: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val action: String,
    val destination: GameDestination,
    val headerColor: Color,
    val buttonColor: Color,
)

private fun buildSituations(state: GameState): List<DashboardSituation> {
    val items = mutableListOf<DashboardSituation>()
    state.diplomacy.activeWar?.let { war ->
        val name = state.diplomacy.rivalById(war.targetCountryId)?.name ?: "enemy forces"
        items += DashboardSituation(
            severity = "CRISIS",
            title = "War Against $name",
            description = "Active conflict requires military response this turn.",
            imageUrl = NssCardImages.INFANTRY,
            action = "Deploy Forces",
            destination = GameDestination.Military,
            headerColor = NssRed,
            buttonColor = NssRed,
        )
    }
    if (state.internalSecurity.coupRisk >= 60f) {
        items += DashboardSituation(
            severity = "WARNING",
            title = "Coup Risk Elevated",
            description = "Internal security reports instability at ${state.internalSecurity.coupRisk.roundToInt()}%.",
            imageUrl = NssCardImages.PARLIAMENT,
            action = "Review Policy",
            destination = GameDestination.LawsSociety,
            headerColor = NssAccent,
            buttonColor = NssAccent,
        )
    }
    if (state.production.foodShortage) {
        items += DashboardSituation(
            severity = "WARNING",
            title = "Food Shortages",
            description = "Agricultural shortfall is driving public unrest nationwide.",
            imageUrl = NssCardImages.AGRICULTURE,
            action = "Open Economy",
            destination = GameDestination.Economy,
            headerColor = NssAccent,
            buttonColor = NssAccent,
        )
    }
    if (state.netIncome > 0 && items.size < 3) {
        items += DashboardSituation(
            severity = "OPPORTUNITY",
            title = "Treasury Surplus",
            description = "Positive cash flow — consider investing in growth sectors now.",
            imageUrl = NssCardImages.TECHNOLOGY,
            action = "Invest Now",
            destination = GameDestination.Economy,
            headerColor = NssEmerald,
            buttonColor = NssEmerald,
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
            title = message.take(40),
            description = message,
            imageUrl = NssCardImages.BANNER_FOREIGN,
            action = "Respond",
            destination = GameDestination.Dashboard,
            headerColor = if (level == "CRIT") NssRed else NssAccent,
            buttonColor = if (level == "CRIT") NssRed else NssAccent,
        )
    }
    return items
}

@Composable
private fun SituationCard(
    situation: DashboardSituation,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .border(1.dp, NssBorder, NssCardShape)
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(112.dp)) {
            NssPhotoHeader(
                imageUrl = situation.imageUrl,
                fallbackGradient = listOf(situation.headerColor.copy(alpha = 0.4f), NssCard),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = listOf(Color.Transparent, NssForeground.copy(alpha = 0.55f)),
            )
            Text(
                text = situation.severity,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(situation.headerColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = NssOnPhoto,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = situation.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NssForeground)
            Text(text = situation.description, fontSize = 11.sp, color = NssMutedForeground, lineHeight = 16.sp)
            Text(
                text = "${situation.action} →",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(situation.buttonColor)
                    .clickable(onClick = onAction)
                    .padding(vertical = 8.dp),
                color = NssOnPhoto,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MinistryShortcut(
    label: String,
    subtitle: String,
    imageUrl: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .border(1.dp, NssBorder, NssCardShape)
            .background(NssCard)
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = listOf(NssPrimary.copy(alpha = 0.2f), NssCard),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = listOf(Color.Transparent, NssForeground.copy(alpha = 0.55f)),
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NssOnPhoto,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .size(20.dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NssForeground)
            Text(text = subtitle, fontSize = 10.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssDestructive
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import com.presidentsimulator.game.viewmodel.toPopulationString
import kotlin.math.roundToInt

/**
 * Nation State Simulator top command bar: nation brand, vitals strip, alerts, clock.
 */
@Composable
fun GlobalHud(
    state: GameState,
    isAutoTicking: Boolean,
    nextTurnEnabled: Boolean,
    timeSpeedEnabled: Boolean,
    alertCount: Int,
    onNextTurn: () -> Unit,
    onToggleTimeSpeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gdp = AnalyticsSaveViewModel().calculateGDP(state)
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)
    val milPower = state.effectiveCombatStrength.roundToInt()
    val history = state.analytics.history

    val vitals = listOf(
        makeHudVital("GDP", formatCompactMoney(gdp), trendFromHistory(history.map { it.gdp.toFloat() })),
        makeHudVital("POPULATION", state.vitals.population.toPopulationString(), trendFromHistory(history.map { it.population.toFloat() })),
        makeHudVital("STABILITY", "${stability.roundToInt()}%", trendFromHistory(listOf(stability))),
        makeHudVital("MIL. POWER", milPower.toString(), trendFromHistory(listOf(milPower.toFloat()))),
        makeHudVital("TREASURY", formatCompactMoney(state.vitals.budget), trendFromHistory(history.map { it.budget.toFloat() })),
        makeHudVital("APPROVAL", state.vitals.approval.toApprovalString(), trendFromHistory(history.map { it.approval })),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(NssCard.copy(alpha = 0.8f))
            .border(width = 0.dp, color = Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Brand block
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .border(width = 0.dp, color = Color.Transparent)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.width(20.dp).height(2.dp).background(NssPrimary))
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(NssPrimary.copy(alpha = 0.6f)))
                Box(modifier = Modifier.width(16.dp).height(2.dp).background(NssPrimary.copy(alpha = 0.3f)))
            }
            Column {
                Text(
                    text = "VELTRIA",
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Chancellor M. Draven",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssMutedForeground,
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(NssBorder),
        )

        // Vitals strip
        Row(modifier = Modifier.weight(1f)) {
            vitals.forEach { vital ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(width = 0.dp, color = Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = vital.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = NssMutedForeground,
                        maxLines = 1,
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = vital.value,
                            color = if (vital.warn) Color(0xFFFCD34D) else NssForeground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = vital.trendTag,
                            fontSize = 9.sp,
                            color = if (vital.trendPositive) NssEmerald else NssRed,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(NssBorder.copy(alpha = 0.5f)),
                )
            }
        }

        // Alerts
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .border(width = 0.dp, color = Color.Transparent)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NssAmber,
                    modifier = Modifier.size(18.dp),
                )
                if (alertCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(6.dp)
                            .background(NssDestructive),
                    )
                }
            }
            Column {
                Text(
                    text = alertCount.toString(),
                    color = NssAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "ALERTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssMutedForeground,
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(NssBorder),
        )

        // Time controls + clock
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HudControlIcon(
                icon = Icons.Default.Pause,
                enabled = timeSpeedEnabled && isAutoTicking,
                onClick = onToggleTimeSpeed,
            )
            HudControlIcon(
                icon = Icons.Default.PlayArrow,
                enabled = timeSpeedEnabled && !isAutoTicking,
                onClick = onToggleTimeSpeed,
            )
            HudControlIcon(
                icon = Icons.Default.FastForward,
                enabled = nextTurnEnabled,
                onClick = onNextTurn,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = state.year.toString(),
                    color = NssForeground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Q${((state.month - 1) / 3) + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssMutedForeground,
                )
            }
        }
    }
}

@Composable
private fun HudControlIcon(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (enabled) NssForeground else NssMutedForeground.copy(alpha = 0.4f),
        modifier = Modifier
            .size(20.dp)
            .clickable(enabled = enabled, onClick = onClick),
    )
}

private data class HudVital(
    val label: String,
    val value: String,
    val trendPositive: Boolean,
    val trendTag: String,
    val warn: Boolean = false,
)

private data class TrendDelta(
    val positive: Boolean,
    val tag: String,
)

private fun makeHudVital(label: String, value: String, trend: TrendDelta): HudVital =
    HudVital(label, value, trend.positive, trend.tag, warn = !trend.positive)

private fun trendFromHistory(values: List<Float>): TrendDelta {
    if (values.size < 2) {
        return TrendDelta(positive = true, tag = "—")
    }
    val delta = values.last() - values.first()
    val positive = delta >= 0
    val tag = when {
        delta == 0f -> "—"
        kotlin.math.abs(delta) >= 100 -> "${if (positive) "+" else ""}${formatCompactNumber(delta.toLong())}"
        else -> "${if (positive) "+" else ""}${"%.1f".format(delta)}"
    }
    return TrendDelta(positive = positive, tag = tag)
}

fun formatCompactMoney(amount: Long): String {
    val abs = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        abs >= 1_000_000_000_000L -> "$sign$${"%.1f".format(abs / 1_000_000_000_000.0)}T"
        abs >= 1_000_000_000L -> "$sign$${"%.1f".format(abs / 1_000_000_000.0)}B"
        abs >= 1_000_000L -> "$sign$${"%.1f".format(abs / 1_000_000.0)}M"
        else -> "$sign$$abs"
    }
}

private fun formatCompactNumber(value: Long): String {
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_000_000_000L -> "${"%.1f".format(abs / 1_000_000_000.0)}B"
        abs >= 1_000_000L -> "${"%.1f".format(abs / 1_000_000.0)}M"
        else -> abs.toString()
    }
}

fun collectAlertCount(state: GameState): Int {
    var count = 0
    if (state.diplomacy.activeWar != null) count++
    if (state.internalSecurity.coupRisk >= 60f) count++
    if (state.internalSecurity.instabilityScore >= 50f) count++
    if (state.production.foodShortage) count++
    if (state.production.energyShortage) count++
    if (state.governance.activeResolution != null) count++
    if (state.gameOver.isGameOver) count++
    return count.coerceAtLeast(if (count == 0) 1 else count)
}

fun collectAlerts(state: GameState): List<Pair<String, String>> {
    val alerts = mutableListOf<Pair<String, String>>()
    state.diplomacy.activeWar?.let { war ->
        val name = state.diplomacy.rivalById(war.targetCountryId)?.name ?: "enemy"
        alerts += "CRIT" to "Active war against $name"
    }
    if (state.internalSecurity.coupRisk >= 60f) {
        alerts += "WARN" to "Coup risk elevated at ${state.internalSecurity.coupRisk.roundToInt()}%"
    }
    if (state.production.foodShortage) {
        alerts += "WARN" to "Food shortages causing public unrest"
    }
    if (state.governance.activeResolution != null) {
        alerts += "INFO" to "UN vote: ${state.governance.activeResolution!!.type.displayName}"
    }
    if (state.netIncome > 0) {
        alerts += "INFO" to "Treasury surplus trending positive"
    }
    return alerts.take(3)
}

// Legacy helpers kept for ministry screens
fun formatMa2Date(month: Int, year: Int): String = "%02d.%02d.%04d".format(1, month, year)

fun formatMa2Money(amount: Long): String = formatCompactMoney(amount)

fun formatMa2Delta(amount: Long): String {
    val body = formatMa2Money(amount)
    return if (amount >= 0) "+$body" else body
}

fun statusTicker(state: GameState): String = when {
    state.gameOver.isGameOver -> state.gameOver.reason
    state.diplomacy.activeWar != null -> {
        val name = state.diplomacy.rivalById(state.diplomacy.activeWar!!.targetCountryId)?.name ?: "enemy"
        "War continues against $name"
    }
    state.internalSecurity.coupRisk >= 75f -> "Coup risk is critically high"
    state.production.foodShortage -> "Food shortages are causing public unrest"
    state.production.energyShortage -> "Energy shortages are slowing industry"
    state.governance.activeResolution != null ->
        "UN vote in progress: ${state.governance.activeResolution!!.type.displayName}"
    else -> "National command systems operational"
}

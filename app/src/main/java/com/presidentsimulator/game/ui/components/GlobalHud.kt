package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import kotlin.math.roundToInt

/**
 * Nation State Simulator top command bar — navy HUD with vitals strip and End Turn.
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
        makeHudVital(Icons.Default.AttachMoney, "GDP", formatCompactMoney(gdp), trendFromHistory(history.map { it.gdp.toFloat() })),
        makeHudVital(Icons.Default.Shield, "Stability", "${stability.roundToInt()}%", trendFromHistory(listOf(stability))),
        makeHudVital(Icons.Default.SportsMartialArts, "Military", milPower.toString(), trendFromHistory(listOf(milPower.toFloat()))),
        makeHudVital(Icons.Default.AccountBalance, "Treasury", formatCompactMoney(state.vitals.budget), trendFromHistory(history.map { it.budget.toFloat() })),
        makeHudVital(Icons.Default.Groups, "Approval", state.vitals.approval.toApprovalString(), trendFromHistory(history.map { it.approval })),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(NssPrimary),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NssOnPhoto.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(
                    text = "VELTRIA",
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "${state.year} · Quarter ${((state.month - 1) / 3) + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssOnPhoto.copy(alpha = 0.6f),
                )
            }
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(NssOnPhoto.copy(alpha = 0.2f)))

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            vitals.forEach { vital ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NssOnPhoto.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(vital.icon, contentDescription = null, tint = NssOnPhoto.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    Column {
                        Text(text = vital.label, fontSize = 8.sp, color = NssOnPhoto.copy(alpha = 0.6f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vital.value,
                                color = vital.trendColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (vital.trendPositive) " ▲" else " ▼",
                                fontSize = 8.sp,
                                color = vital.trendColor,
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(NssOnPhoto.copy(alpha = 0.2f)))

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Color(0xFFF87171)))
            Text(
                text = "$alertCount Alerts",
                color = NssOnPhoto.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "End Turn ▶",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (nextTurnEnabled) NssAccent else NssAccent.copy(alpha = 0.4f))
                    .clickable(enabled = nextTurnEnabled, onClick = onNextTurn)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = NssOnPhoto,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private data class HudVital(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val trendPositive: Boolean,
) {
    val trendColor: Color
        get() = if (trendPositive) Color(0xFF86EFAC) else Color(0xFFFCD34D)
}

private fun makeHudVital(icon: ImageVector, label: String, value: String, trend: TrendDelta): HudVital =
    HudVital(icon, label, value, trend.positive)

private data class TrendDelta(
    val positive: Boolean,
    val tag: String,
)

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

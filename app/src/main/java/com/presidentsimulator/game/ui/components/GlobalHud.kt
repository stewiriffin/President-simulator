package com.presidentsimulator.game.ui.components

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssHudMetricsBar
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.viewmodel.TimeSpeedMode
import kotlin.math.roundToInt

/**
 * Zip-reference HUD — brand row + inset metrics strip, icon-only time controls.
 */
@Composable
fun GlobalHud(
    state: GameState,
    timeSpeedMode: TimeSpeedMode,
    timeSpeedEnabled: Boolean,
    alertCount: Int,
    onTimeSpeedModeSelected: (TimeSpeedMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = rememberNssLayoutSpec()
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)
    val milPower = state.effectiveCombatStrength.roundToInt()
    val treasuryWarn = state.netIncome < 0
    val stabilityWarn = stability < 60f
    val monthsToElection = monthsUntilElection(state)
    val electionWarn = monthsToElection in 0..12
    val quarter = ((state.month - 1) / 3) + 1

    val pulseTransition = rememberInfiniteTransition(label = "alertPulse")
    val hudShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(hudShape)
            .background(NssPrimary)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = Dimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NssAccent, Color(0xFFD97706)),
                            ),
                        )
                        .border(1.dp, NssOnPhoto.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = NssOnPhoto,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column {
                    Text(
                        text = state.playerNation.name.uppercase(),
                        color = NssOnPhoto,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = if (layout.isNarrowWidth) 12.sp else 14.sp,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Q$quarter ${state.year}",
                            fontSize = 10.sp,
                            color = NssOnPhoto.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                        if (electionWarn) {
                            Text(
                                text = if (monthsToElection <= 0) "ELECTION NOW" else "ELECTION IN ${electionCountdownLabel(monthsToElection)}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFCD34D),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33F59E0B))
                                    .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
            TimeSpeedControl(
                mode = timeSpeedMode,
                enabled = timeSpeedEnabled,
                onModeSelected = onTimeSpeedModeSelected,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.SpacingSmall, end = Dimens.SpacingSmall, bottom = Dimens.SpacingSmall)
                .clip(RoundedCornerShape(12.dp))
                .background(NssHudMetricsBar)
                .border(1.dp, NssOnPhoto.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(horizontal = Dimens.SpacingMedium, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HudMetric(Icons.Default.AttachMoney, formatCompactMoney(state.vitals.budget), treasuryWarn, compact = layout.isNarrowWidth)
            HudMetric(Icons.Default.Shield, "${stability.roundToInt()}%", stabilityWarn, compact = layout.isNarrowWidth)
            if (!layout.isNarrowWidth) {
                HudMetric(Icons.Default.SportsMartialArts, formatCompactMil(milPower), warn = false, compact = false)
            }

            if (alertCount > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(NssOnPhoto.copy(alpha = 0.1f)),
                )
                val alertPulse by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                    label = "alertPulse",
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33EF4444))
                        .border(1.dp, Color(0x4DEF4444), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(alertPulse)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF87171)),
                    )
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(12.dp))
                    Text(
                        text = "$alertCount ALERT${if (alertCount == 1) "" else "S"}",
                        color = Color(0xFFFCA5A5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

@Composable
private fun HudMetric(icon: ImageVector, value: String, warn: Boolean, compact: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (warn) Color(0x33F59E0B) else NssOnPhoto.copy(alpha = 0.1f))
                .padding(4.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (warn) Color(0xFFFCD34D) else NssOnPhoto.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = value,
            color = if (warn) Color(0xFFFCD34D) else NssOnPhoto,
            fontSize = if (compact) 10.sp else 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TimeSpeedControl(
    mode: TimeSpeedMode,
    enabled: Boolean,
    onModeSelected: (TimeSpeedMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NssHudMetricsBar)
            .border(1.dp, NssOnPhoto.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeSpeedMode.entries.forEach { speed ->
            val selected = mode == speed
            val icon = when (speed) {
                TimeSpeedMode.PAUSED -> Icons.Default.Pause
                TimeSpeedMode.NORMAL -> Icons.Default.PlayArrow
                TimeSpeedMode.FAST -> Icons.Default.FastForward
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) NssAccent else Color.Transparent)
                    .clickable(enabled = enabled) { onModeSelected(speed) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = speed.label,
                    tint = if (selected) NssOnPhoto else NssOnPhoto.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

fun monthsUntilElection(state: GameState): Int {
    val current = state.year * 12 + (state.month - 1)
    val election = state.nextElectionYear * 12
    return (election - current).coerceAtLeast(0)
}

fun electionCountdownLabel(months: Int): String = when {
    months <= 0 -> "NOW"
    months < 12 -> "${months}M"
    else -> "${months / 12}Y"
}

fun formatCompactMil(value: Int): String = when {
    value >= 1_000 -> "${"%.1f".format(value / 1000f)}K"
    else -> value.toString()
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

fun collectAlertCount(state: GameState): Int {
    var count = 0
    if (state.diplomacy.activeWar != null) count++
    if (state.internalSecurity.coupRisk >= 60f) count++
    if (state.internalSecurity.instabilityScore >= 50f) count++
    if (state.production.foodShortage) count++
    if (state.production.energyShortage) count++
    if (state.governance.activeResolution != null) count++
    if (state.gameOver.isGameOver) count++
    return count
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

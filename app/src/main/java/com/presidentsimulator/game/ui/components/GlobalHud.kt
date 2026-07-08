package com.presidentsimulator.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.viewmodel.toApprovalString
import kotlin.math.roundToInt

/**
 * Gamified v3 top HUD — compact vitals, alert pill, pulsing End Turn CTA.
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
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)
    val milPower = state.effectiveCombatStrength.roundToInt()
    val treasuryWarn = state.netIncome < 0
    val stabilityWarn = stability < 60f

    val monthsToElection = monthsUntilElection(state)
    val electionWarn = monthsToElection in 0..12

    val vitals = listOf(
        HudChip(Icons.Default.AttachMoney, formatCompactMoney(state.vitals.budget), treasuryWarn),
        HudChip(Icons.Default.Shield, "${stability.roundToInt()}%", stabilityWarn),
        HudChip(Icons.Default.SportsMartialArts, formatCompactMil(milPower), warn = false),
        HudChip(Icons.Default.Groups, state.vitals.approval.toApprovalString(), state.vitals.approval < 50f),
        HudChip(Icons.Default.HowToVote, electionCountdownLabel(monthsToElection), electionWarn),
    )

    val pulseTransition = rememberInfiniteTransition(label = "endTurnPulse")
    val endTurnScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1250, easing = LinearEasing), RepeatMode.Reverse),
        label = "endTurnScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NssPrimary)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            )
            .height(Dimens.HudHeight)
            .padding(horizontal = Dimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(start = Dimens.SpacingSmall, end = Dimens.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(NssAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(16.dp))
            }
            Column {
                Text("VELTRIA", color = NssOnPhoto, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                Text(
                    text = "${state.year} · Q${((state.month - 1) / 3) + 1}",
                    fontSize = 9.sp,
                    color = NssOnPhoto.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            vitals.forEach { chip ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (chip.warn) Color(0x33F59E0B) else NssOnPhoto.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        chip.icon,
                        contentDescription = null,
                        tint = if (chip.warn) Color(0xFFFCD34D) else NssOnPhoto.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = chip.value,
                        color = if (chip.warn) Color(0xFFFCD34D) else NssOnPhoto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (alertCount > 0) {
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
                    Text(text = alertCount.toString(), color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (timeSpeedEnabled) {
                            if (isAutoTicking) NssAccent.copy(alpha = 0.55f) else NssOnPhoto.copy(alpha = 0.12f)
                        } else {
                            NssOnPhoto.copy(alpha = 0.06f)
                        },
                    )
                    .clickable(enabled = timeSpeedEnabled, onClick = onToggleTimeSpeed)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (isAutoTicking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isAutoTicking) "Pause auto-tick" else "Start auto-tick",
                    tint = NssOnPhoto,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (isAutoTicking) "Auto" else "1x",
                    color = NssOnPhoto,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Row(
                modifier = Modifier
                    .scale(if (nextTurnEnabled) endTurnScale else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (nextTurnEnabled) NssAccent else NssAccent.copy(alpha = 0.4f))
                    .clickable(enabled = nextTurnEnabled, onClick = onNextTurn)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(14.dp))
                Text("End Turn", color = NssOnPhoto, fontSize = 11.sp, fontWeight = FontWeight.Black)
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
    months <= 0 -> "Vote"
    months < 12 -> "${months}m"
    else -> "${months / 12}y"
}

private data class HudChip(val icon: ImageVector, val value: String, val warn: Boolean)

private fun formatCompactMil(value: Int): String = when {
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

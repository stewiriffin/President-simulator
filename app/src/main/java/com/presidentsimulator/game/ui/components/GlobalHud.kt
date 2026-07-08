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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
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

    val vitals = listOf(
        HudChip(Icons.Default.AttachMoney, formatCompactMoney(state.vitals.budget), treasuryWarn),
        HudChip(Icons.Default.Shield, "${stability.roundToInt()}%", stabilityWarn),
        HudChip(Icons.Default.SportsMartialArts, formatCompactMil(milPower), warn = false),
        HudChip(Icons.Default.Groups, state.vitals.approval.toApprovalString(), state.vitals.approval < 50f),
    )

    val pulseTransition = rememberInfiniteTransition(label = "endTurnPulse")
    val endTurnScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1250, easing = LinearEasing), RepeatMode.Reverse),
        label = "endTurnScale",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = NssPrimary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            )
            .padding(bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NssAccent, Color(0xFFB87333)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = NssOnPhoto,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "VELTRIA",
                        color = NssOnPhoto,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = "${state.year} · Q${((state.month - 1) / 3) + 1}",
                        fontSize = 10.sp,
                        color = NssOnPhoto.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            }

            // Spacer
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

            // Vitals row (3 key metrics)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                vitals.take(3).forEach { chip ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (chip.warn) Color(0x40F59E0B) else NssOnPhoto.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            chip.icon,
                            contentDescription = null,
                            tint = if (chip.warn) Color(0xFFFCD34D) else NssOnPhoto.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp),
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x40EF4444))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
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
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(13.dp),
                        )
                        Text(text = alertCount.toString(), color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Bottom row: End Turn / Auto speed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Approval chip
            val approvalChip = vitals.getOrNull(3)
            if (approvalChip != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (approvalChip.warn) Color(0x40F59E0B) else NssOnPhoto.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        approvalChip.icon,
                        contentDescription = null,
                        tint = if (approvalChip.warn) Color(0xFFFCD34D) else NssOnPhoto.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = approvalChip.value,
                        color = if (approvalChip.warn) Color(0xFFFCD34D) else NssOnPhoto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

            // Auto-speed button
            if (timeSpeedEnabled) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAutoTicking) Color(0xFF374151) else NssOnPhoto.copy(alpha = 0.1f))
                        .clickable(onClick = onToggleTimeSpeed)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (isAutoTicking) Color(0xFFFCD34D) else NssOnPhoto.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = if (isAutoTicking) "AUTO" else "PAUSE",
                        color = if (isAutoTicking) Color(0xFFFCD34D) else NssOnPhoto.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
            }

            // End Turn CTA
            Row(
                modifier = Modifier
                    .scale(if (nextTurnEnabled) endTurnScale else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (nextTurnEnabled) NssAccent else NssAccent.copy(alpha = 0.35f))
                    .clickable(enabled = nextTurnEnabled, onClick = onNextTurn)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(14.dp))
                Text("End Turn", color = NssOnPhoto, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
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

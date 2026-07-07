package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.WarState
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.progressFraction
import com.presidentsimulator.game.viewmodel.progressLabel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toCasualtyString
import kotlin.math.roundToInt

@Composable
fun ActiveWarPanel(
    state: GameState,
    war: WarState,
    armisticeCost: Long,
    onLaunchOffensive: () -> Unit,
    onHoldDefensiveLine: () -> Unit,
    onProposeArmistice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rivalName = state.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
    val canAffordPeace = state.vitals.budget >= armisticeCost
    val progressColor = when {
        war.warProgress >= 25f -> Color(0xFF16A34A)
        war.warProgress <= -25f -> NssRed
        else -> NssAccent
    }
    val progressPct = ((war.progressFraction() * 200f) - 100f).coerceIn(-100f, 100f)
    val barPct = ((progressPct + 100f) / 2f).coerceIn(0f, 100f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.BANNER_DEFENSE,
                fallbackGradient = NssGradients.Defense,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
        NssPanel(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFECACA), NssCardShape),
            highlighted = true,
        ) {
        NssBadge(label = "WAR ROOM", large = true)
        Text(
            text = "⚔ Engaged with $rivalName",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = NssForeground,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Month ${war.monthsActive} · Posture: ${state.military.deployment.name}",
            fontSize = 11.sp,
            color = NssMutedForeground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("War Progress", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NssForeground)
            Text(war.progressLabel(), fontWeight = FontWeight.Black, fontSize = 13.sp, color = progressColor)
        }
        NssGameBar(percent = barPct, color = progressColor, thick = true)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            WarStat("Our Losses", war.playerCasualties.toCasualtyString())
            WarStat("Enemy Losses", war.enemyCasualties.toCasualtyString())
            WarStat("Readiness", state.effectiveCombatStrength.roundToInt().toString())
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("TACTICAL ORDERS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NssMutedForeground, letterSpacing = 2.sp)

        WarActionButton(
            label = if (state.military.deployment == DeploymentStatus.MOBILIZED) "Launch Offensive (Active)" else "⚔ Launch Offensive",
            color = NssRed,
            onClick = onLaunchOffensive,
        )
        WarActionButton(
            label = if (state.military.deployment == DeploymentStatus.DEFENSIVE) "Hold Line (Active)" else "🛡 Hold Defensive Line",
            color = Color(0xFF1E3A6E),
            onClick = onHoldDefensiveLine,
        )
        WarActionButton(
            label = "Propose Armistice (${armisticeCost.toBudgetString()})",
            color = if (canAffordPeace) NssMutedForeground else NssMutedForeground.copy(alpha = 0.4f),
            enabled = canAffordPeace,
            onClick = onProposeArmistice,
        )
        }
    }
}

@Composable
private fun WarStat(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = NssForeground)
        Text(label, fontSize = 9.sp, color = NssMutedForeground, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WarActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(NssCardShape)
            .background(if (enabled) color else color.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        color = NssOnPhoto,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
    )
}

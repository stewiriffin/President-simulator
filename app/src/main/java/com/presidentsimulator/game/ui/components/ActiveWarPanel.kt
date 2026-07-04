package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.WarState
import com.presidentsimulator.game.ui.theme.DeficitRed
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.WarningOrange
import com.presidentsimulator.game.viewmodel.progressFraction
import com.presidentsimulator.game.viewmodel.progressLabel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toCasualtyString
import kotlin.math.roundToInt

/**
 * Dominant War Room HUD shown only while a conflict is active.
 * Progress spans -100% (defeat) to +100% (victory).
 */
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
    val rivalName = state.diplomacy.rivalById(war.targetCountryId)?.name
        ?: war.targetCountryId
    val canAffordPeace = state.vitals.budget >= armisticeCost
    val progressColor = when {
        war.warProgress >= 25f -> ProfitGreen
        war.warProgress <= -25f -> DeficitRed
        else -> WarningOrange
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WAR ROOM — ACTIVE CONFLICT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = WarningOrange,
            )
            Text(
                text = "Engaged with $rivalName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Month ${war.monthsActive} of hostilities · Posture: ${state.military.deployment.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "War Progress: ${war.progressLabel()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = progressColor,
            )
            Text(
                text = "Defeat (−100%)  ←————————→  Victory (+100%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { war.progressFraction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.25f),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CasualtyBlock(
                    label = "Our Casualties",
                    value = war.playerCasualties.toCasualtyString(),
                )
                CasualtyBlock(
                    label = "Enemy Casualties",
                    value = war.enemyCasualties.toCasualtyString(),
                )
                CasualtyBlock(
                    label = "Readiness",
                    value = state.effectiveCombatStrength.roundToInt().toString(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tactical Orders",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onLaunchOffensive,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeficitRed,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(
                    if (state.military.deployment == DeploymentStatus.MOBILIZED) {
                        "Launch Offensive (Active)"
                    } else {
                        "Launch Offensive"
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onHoldDefensiveLine,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (state.military.deployment == DeploymentStatus.DEFENSIVE) {
                        "Hold Defensive Line (Active)"
                    } else {
                        "Hold Defensive Line"
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onProposeArmistice,
                enabled = canAffordPeace,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("Propose Armistice (${armisticeCost.toBudgetString()})")
            }
        }
    }
}

@Composable
private fun CasualtyBlock(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NeutralGray,
        )
    }
}

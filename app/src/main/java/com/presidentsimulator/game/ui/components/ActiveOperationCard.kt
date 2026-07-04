package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.WarningOrange
import kotlin.math.roundToInt

/**
 * Tracks a deployed spy mission: target, type, tick progress, and cancel action.
 */
@Composable
fun ActiveOperationCard(
    state: GameState,
    mission: CovertMission,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetName = state.diplomacy.rivalById(mission.targetCountryId)?.name
        ?: mission.targetCountryId
    val ticksRemaining = (mission.requiredTicks - mission.progressTicks).coerceAtLeast(0)
    val isActive = mission.status == MissionStatus.ACTIVE

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = targetName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = mission.missionType.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralGray,
                    )
                }
                Text(
                    text = when (mission.status) {
                        MissionStatus.ACTIVE -> "ACTIVE"
                        MissionStatus.SUCCESS -> "SUCCESS"
                        MissionStatus.FAILED -> "FAILED"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (mission.status) {
                        MissionStatus.ACTIVE -> WarningOrange
                        MissionStatus.SUCCESS -> ProfitGreen
                        MissionStatus.FAILED -> MaterialTheme.colorScheme.error
                    },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isActive) {
                Text(
                    text = "Progress ${mission.progressTicks}/${mission.requiredTicks} · " +
                        "$ticksRemaining month(s) remaining · " +
                        "Success odds ${(mission.successProbability * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { mission.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = WarningOrange,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel Operation")
                }
            } else {
                Text(
                    text = "Mission concluded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                )
            }
        }
    }
}

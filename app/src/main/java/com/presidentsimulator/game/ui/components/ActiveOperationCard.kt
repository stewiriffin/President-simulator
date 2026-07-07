package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.roundToInt

@Composable
fun ActiveOperationCard(
    state: GameState,
    mission: CovertMission,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetName = state.diplomacy.rivalById(mission.targetCountryId)?.name ?: mission.targetCountryId
    val ticksRemaining = (mission.requiredTicks - mission.progressTicks).coerceAtLeast(0)
    val isActive = mission.status == MissionStatus.ACTIVE
    val statusLabel = when (mission.status) {
        MissionStatus.ACTIVE -> "ACTIVE"
        MissionStatus.SUCCESS -> "SUCCESS"
        MissionStatus.FAILED -> "FAILED"
    }

    NssPanel(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(targetName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                Text(mission.missionType.displayName, fontSize = 11.sp, color = NssMutedForeground)
            }
            NssBadge(label = statusLabel)
        }

        if (isActive) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${mission.progressTicks}/${mission.requiredTicks} ticks · $ticksRemaining mo left · " +
                    "${(mission.successProbability * 100f).roundToInt()}% success",
                fontSize = 10.sp,
                color = NssMutedForeground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            NssGameBar(percent = mission.progressFraction * 100f, color = NssAccent, thick = true)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Cancel Operation",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(Color(0xFFF5F5F4))
                    .clickable(onClick = onCancel)
                    .padding(vertical = 8.dp),
                color = NssForeground,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        } else {
            Text("Mission concluded.", fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

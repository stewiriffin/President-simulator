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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSky

@Composable
fun MissionResultDialog(
    mission: CovertMission,
    state: GameState,
    onDismiss: () -> Unit,
) {
    val rival = state.diplomacy.rivalById(mission.targetCountryId)
    val targetName = rival?.name ?: mission.targetCountryId
    val isSuccess = mission.status == MissionStatus.SUCCESS
    
    val color = if (isSuccess) NssSky else NssRed
    val icon = if (isSuccess) Icons.Default.CheckCircleOutline else Icons.Default.ErrorOutline
    val title = if (isSuccess) "OPERATION SUCCESSFUL" else "OPERATION COMPROMISED"
    val description = if (isSuccess) {
        "Our operatives successfully completed the ${mission.missionType.displayName} mission in $targetName."
    } else {
        "Our operatives were caught attempting the ${mission.missionType.displayName} mission in $targetName. The operation has failed and diplomatic blowback is expected."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .border(2.dp, color.copy(alpha = 0.5f), NssCardShape)
                .background(NssBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                color = NssForeground,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ACKNOWLEDGE",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(color)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

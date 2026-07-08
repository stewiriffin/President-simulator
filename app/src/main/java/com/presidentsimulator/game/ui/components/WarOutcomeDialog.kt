package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.WarOutcome
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toCasualtyString
import kotlin.math.roundToInt

@Composable
fun WarOutcomeDialog(
    outcome: WarOutcome,
    onDismiss: () -> Unit,
) {
    val color = if (outcome.victory) NssEmerald else NssRed
    val icon = if (outcome.victory) Icons.Default.MilitaryTech else Icons.Default.SentimentVeryDissatisfied
    val title = if (outcome.victory) "VICTORY" else "DEFEAT"

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
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "War with ${outcome.targetName} ended after ${outcome.monthsActive} months.",
                color = NssForeground,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = buildString {
                    append("Casualties · ours ${outcome.playerCasualties.toCasualtyString()}")
                    append(" · enemy ${outcome.enemyCasualties.toCasualtyString()}\n")
                    append("Settlement · ${if (outcome.budgetDelta >= 0) "+" else ""}${outcome.budgetDelta.toBudgetString()}")
                    append(" · approval ${if (outcome.approvalDelta >= 0) "+" else ""}${outcome.approvalDelta.roundToInt()}")
                    append(" · front ${outcome.finalProgress.roundToInt()}%")
                },
                color = NssMutedForeground,
                fontSize = 13.sp,
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

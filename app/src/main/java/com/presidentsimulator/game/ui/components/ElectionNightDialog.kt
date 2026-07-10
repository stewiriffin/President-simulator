package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.ElectionNightResult
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.roundToInt

@Composable
fun ElectionNightDialog(
    result: ElectionNightResult,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "ELECTION NIGHT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = if (result.victory) "YOU HOLD THE OFFICE" else "DEFEAT",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = if (result.victory) NssEmerald else NssRed,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            Text(
                text = result.narrative,
                fontSize = 13.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("NATIONAL SPLIT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NssPrimary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                VoteBar(label = "You", percent = result.playerShare, color = NssEmerald)
                Spacer(modifier = Modifier.height(6.dp))
                VoteBar(label = result.challengerName, percent = result.challengerShare, color = NssRed)
            }

            Spacer(modifier = Modifier.height(10.dp))
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("COHORT READOUT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NssPrimary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                CohortLine("Working class", result.workingShare)
                CohortLine("Business elite", result.businessShare)
                CohortLine("Military", result.militaryShare)
                CohortLine("Academics", result.academicsShare)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append(if (result.economyOk) "Economy: pass" else "Economy: fail")
                        append(" · ")
                        append(if (result.stabilityOk) "Stability: pass" else "Stability: fail")
                        append(" · Opp. drag −${result.oppositionPenalty.roundToInt()}")
                    },
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                )
            }

            Text(
                text = if (result.victory) "ACCEPT MANDATE" else "CONCEDE",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(NssCardShape)
                    .background(if (result.victory) NssEmerald else NssRed)
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun VoteBar(label: String, percent: Float, color: androidx.compose.ui.graphics.Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NssForeground)
            Text("${percent.roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
        }
        NssGameBar(percent = percent, color = color, thick = true)
    }
}

@Composable
private fun CohortLine(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp, color = NssMutedForeground)
        Text("${value.roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NssForeground)
    }
}

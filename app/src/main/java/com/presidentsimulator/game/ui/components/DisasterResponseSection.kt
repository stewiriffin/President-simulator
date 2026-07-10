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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.ResponseFocus
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun DisasterResponseSection(
    state: GameState,
    onAllocate: (ResponseFocus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val disaster = state.disaster
    val active = disaster.active

    NssPanel(modifier = modifier.fillMaxWidth()) {
        Text(
            "DISASTER RESPONSE COMMAND",
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = NssRed,
            letterSpacing = 2.sp,
        )
        Text(
            text = "Readiness ${disaster.readiness.roundToInt()}% · handled ${disaster.disastersHandled} · mismanaged ${disaster.disastersMismanaged}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (active == null) {
            Text(
                "No active emergency. Keep readiness high via Health ministry and clean responses.",
                fontSize = 12.sp,
                color = NssForeground,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (disaster.lastCommandNote.isNotBlank()) {
                Text(
                    disaster.lastCommandNote,
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            return@NssPanel
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${active.type.displayName} — ${active.stageLabel}",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = NssForeground,
        )
        Text(
            active.type.blurb,
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            "Severity ${active.severity.roundToInt()} · month ${active.monthsActive} · response ${active.responsePoints.roundToInt()} pts",
            fontSize = 11.sp,
            color = NssAccent,
            modifier = Modifier.padding(top = 6.dp),
        )
        NssGameBar(
            percent = active.severity,
            color = when {
                active.severity >= 60f -> NssRed
                active.severity >= 35f -> NssAccent
                else -> NssEmerald
            },
        )
        if (disaster.lastCommandNote.isNotBlank()) {
            Text(
                disaster.lastCommandNote,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NssForeground,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        val canAct = disaster.responseCooldownMonths == 0
        Text(
            if (!canAct) "Next allocation in ${disaster.responseCooldownMonths} mo" else "ALLOCATE RESPONSE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = NssPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        ResponseFocus.entries.forEach { focus ->
            val afford = state.vitals.budget >= focus.cost && canAct
            Text(
                text = "${focus.displayName} · ${focus.cost.toBudgetString()}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(NssCardShape)
                    .background(if (afford) NssPrimary else NssMutedForeground.copy(alpha = 0.35f))
                    .clickable(enabled = afford) { onAllocate(focus) }
                    .padding(vertical = 10.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Spent ${active.fundsSpent.toBudgetString()}",
                fontSize = 10.sp,
                color = NssMutedForeground,
            )
            Text(
                "~${active.livesSavedEstimate / 1000}k aided",
                fontSize = 10.sp,
                color = NssEmerald,
            )
        }
    }
}

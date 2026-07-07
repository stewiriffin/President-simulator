package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.EventChoice
import com.presidentsimulator.game.data.EventConsequence
import com.presidentsimulator.game.data.GameEvent
import com.presidentsimulator.game.ui.components.graphics.EventIllustration
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun EventCrisisDialog(
    event: GameEvent,
    onChoiceSelected: (EventChoice) -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(20.dp),
        ) {
            Text("🚨 NATIONAL CRISIS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NssRed, letterSpacing = 3.sp)
            Text(
                text = event.title,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = NssForeground,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            EventIllustration(eventType = event.id, modifier = Modifier.padding(bottom = 10.dp))
            Text(event.description, fontSize = 13.sp, color = NssMutedForeground, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("CHOOSE A RESPONSE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NssPrimary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
            ) {
                items(event.choices, key = { it.text }) { choice ->
                    NssPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChoiceSelected(choice) },
                    ) {
                        Text(choice.text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                        Text(
                            text = choice.consequence.toEffectSummary(),
                            fontSize = 11.sp,
                            color = NssMutedForeground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            Text(
                text = "⏸ Time paused until resolved",
                fontSize = 10.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            )
        }
    }
}

fun EventConsequence.toEffectSummary(): String {
    val parts = buildList {
        if (budgetChange != 0L) {
            val prefix = if (budgetChange > 0) "+" else ""
            add("Treasury $prefix${budgetChange.toBudgetString()}")
        }
        if (approvalChange != 0f) {
            val prefix = if (approvalChange > 0) "+" else ""
            add("Approval $prefix${approvalChange.roundToInt()}%")
        }
        if (populationChange != 0L) add("Population ${populationChange.toSignedCount()}")
        if (factoriesChange != 0) add("Infrastructure ${factoriesChange.toSignedInt()}")
        if (farmsChange != 0) add("Farms ${farmsChange.toSignedInt()}")
        if (housingChange != 0) add("Housing ${housingChange.toSignedInt()}")
        if (armySizeChange != 0L) add("Army ${armySizeChange.toSignedCount()}")
        if (defconChange != 0) add("DEFCON ${defconChange.toSignedInt()}")
    }
    return parts.joinToString(", ").ifEmpty { "No direct effect" }
}

private fun Int.toSignedInt(): String = if (this > 0) "+$this" else "$this"

private fun Long.toSignedCount(): String {
    val sign = when {
        this > 0L -> "+"
        this < 0L -> "-"
        else -> ""
    }
    val abs = kotlin.math.abs(this)
    val body = when {
        abs >= 1_000_000_000L -> "%.2fB".format(abs / 1_000_000_000.0)
        abs >= 1_000_000L -> "%.1fM".format(abs / 1_000_000.0)
        abs >= 1_000L -> "%.1fK".format(abs / 1_000.0)
        else -> abs.toString()
    }
    return "$sign$body"
}

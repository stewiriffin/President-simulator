package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.EventChoice
import com.presidentsimulator.game.data.EventConsequence
import com.presidentsimulator.game.data.GameEvent
import com.presidentsimulator.game.ui.components.graphics.EventIllustration
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.WarningOrange
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

/**
 * Non-dismissible crisis overlay.
 * Each choice lists exact state modifiers for informed strategic decisions.
 */
@Composable
fun EventCrisisDialog(
    event: GameEvent,
    onChoiceSelected: (EventChoice) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Column {
                Text(
                    text = "NATIONAL CRISIS",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarningOrange,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarningOrange,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
            ) {
                EventIllustration(
                    eventType = event.id,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralGray,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Choose a response",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(event.choices, key = { it.text }) { choice ->
                        CrisisChoiceCard(
                            choice = choice,
                            onClick = { onChoiceSelected(choice) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Text(
                text = "Time paused until resolved",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralGray,
            )
        },
    )
}

@Composable
private fun CrisisChoiceCard(
    choice: EventChoice,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = choice.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Effect: ${choice.consequence.toEffectSummary()}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
        }
    }
}

/**
 * Explicit modifier readout, e.g.
 * "Treasury -$50.0M, Approval -5%, Factories +1"
 */
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
        if (populationChange != 0L) {
            add("Population ${populationChange.toSignedCount()}")
        }
        if (factoriesChange != 0) {
            add("Infrastructure ${factoriesChange.toSignedInt()}")
        }
        if (farmsChange != 0) {
            add("Farms ${farmsChange.toSignedInt()}")
        }
        if (housingChange != 0) {
            add("Housing ${housingChange.toSignedInt()}")
        }
        if (armySizeChange != 0L) {
            add("Army ${armySizeChange.toSignedCount()}")
        }
        if (defconChange != 0) {
            add("DEFCON ${defconChange.toSignedInt()}")
        }
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

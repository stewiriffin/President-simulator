package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private data class PolicyTab(val label: String, val category: LawCategory)

private val policyTabs = listOf(
    PolicyTab("Constitution", LawCategory.MILITARY),
    PolicyTab("Economy", LawCategory.ECONOMIC),
    PolicyTab("Social", LawCategory.SOCIAL),
)

/**
 * Ministry of Laws & Society — tabbed policy sectors with toggleable statutes.
 */
@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingToggle by remember { mutableStateOf<LawToggleRequest?>(null) }

    val selectedCategory = policyTabs[selectedTab].category
    val laws = remember(selectedCategory) { LawCatalog.byCategory(selectedCategory) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Ministry of Laws & Society",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${state.legal.activeLawIds.size} active laws · " +
                    "Upkeep ${state.legal.totalUpkeep.toBudgetString()}/mo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        ScrollableTabRow(selectedTabIndex = selectedTab) {
            policyTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.label) },
                )
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(laws, key = { it.id }) { law ->
                val isActive = state.legal.isActive(law.id)
                PolicyLawRow(
                    law = law,
                    isActive = isActive,
                    effectSummary = buildLawEffectSummary(law),
                    enabled = isActive || viewModel.canEnactLaw(law.id),
                    onToggle = { enabled ->
                        pendingToggle = LawToggleRequest(law = law, enabling = enabled)
                    },
                )
            }
        }
    }

    pendingToggle?.let { request ->
        LawToggleConfirmationDialog(
            request = request,
            onConfirm = {
                if (request.enabling) {
                    viewModel.enactLaw(request.law.id)
                } else {
                    viewModel.repealLaw(request.law.id)
                }
                pendingToggle = null
            },
            onDismiss = { pendingToggle = null },
        )
    }
}

private data class LawToggleRequest(val law: Law, val enabling: Boolean)

@Composable
private fun PolicyLawRow(
    law: Law,
    isActive: Boolean,
    effectSummary: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = law.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = effectSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "Upkeep ${law.upkeepCost.toBudgetString()}/mo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Switch(
                checked = isActive,
                onCheckedChange = { checked ->
                    if (checked != isActive) {
                        onToggle(checked)
                    }
                },
                enabled = enabled || isActive,
            )
        }
    }
}

@Composable
private fun LawToggleConfirmationDialog(
    request: LawToggleRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val law = request.law
    val title = if (request.enabling) "Enact ${law.name}?" else "Repeal ${law.name}?"
    val body = if (request.enabling) {
        buildString {
            append("Activation cost: ${law.activationCost.toBudgetString()}\n")
            append("Monthly upkeep: ${law.upkeepCost.toBudgetString()}\n")
            append("Parliament approval required: ${law.approvalThreshold.roundToInt()}%\n\n")
            append("Ongoing effects: ${buildLawEffectSummary(law)}")
        }
    } else {
        "Repealing this law removes its modifiers immediately. " +
            "No refund is issued for prior activation costs."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (request.enabling) "Enact" else "Repeal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun buildLawEffectSummary(law: Law): String {
    val parts = buildList {
        if (law.approvalModifier != 0f) {
            add("${formatSigned(law.approvalModifier)}% Approval")
        }
        if (law.upkeepCost > 0L) {
            add("-${law.upkeepCost.toBudgetString()} Budget/mo")
        }
        if (law.productionModifier != 1f) {
            val pct = ((law.productionModifier - 1f) * 100f).roundToInt()
            add("${formatSigned(pct.toFloat())}% Production")
        }
        if (law.foodDemandModifier != 1f) {
            val pct = ((law.foodDemandModifier - 1f) * 100f).roundToInt()
            add("${formatSigned(pct.toFloat())}% Food demand")
        }
        if (law.energyDemandModifier != 1f) {
            val pct = ((law.energyDemandModifier - 1f) * 100f).roundToInt()
            add("${formatSigned(pct.toFloat())}% Energy demand")
        }
        if (law.militaryRecruitModifier != 1f) {
            val pct = ((law.militaryRecruitModifier - 1f) * 100f).roundToInt()
            add("${formatSigned(pct.toFloat())}% Recruitment")
        }
    }
    return parts.joinToString(", ").ifEmpty { "No direct modifiers" }
}

private fun formatSigned(value: Float): String {
    val body = if (value % 1f == 0f) {
        value.roundToInt().toString()
    } else {
        "%.1f".format(value)
    }
    return if (value > 0f) "+$body" else body
}

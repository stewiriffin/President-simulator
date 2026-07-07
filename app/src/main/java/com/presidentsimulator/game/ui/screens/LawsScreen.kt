package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private data class PolicyTab(val label: String, val category: LawCategory)

private val policyTabs = listOf(
    PolicyTab("CONSTITUTION", LawCategory.MILITARY),
    PolicyTab("ECONOMY", LawCategory.ECONOMIC),
    PolicyTab("SOCIAL", LawCategory.SOCIAL),
)

@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("CONSTITUTION") }
    var pendingToggle by remember { mutableStateOf<LawToggleRequest?>(null) }
    val selectedCategory = policyTabs.first { it.label == selectedTab }.category
    val laws = remember(selectedCategory) { LawCatalog.byCategory(selectedCategory) }

    Column(modifier = modifier.fillMaxSize().background(NssBackground)) {
        NssScreenHeader(
            title = "Domestic Policy",
            imageUrl = NssCardImages.BANNER_DOMESTIC,
            statPills = listOf(
                "Active" to "${state.legal.activeLawIds.size}",
                "Upkeep" to state.legal.totalUpkeep.toBudgetString(),
                "Laws" to "${laws.size}",
            ),
            gradientColors = NssGradients.Indigo,
        )

        NssTabBar(
            tabs = policyTabs.map { it.label },
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(laws, key = { it.id }) { law ->
                val isActive = state.legal.isActive(law.id)
                PolicyLawRow(
                    law = law,
                    category = selectedCategory,
                    isActive = isActive,
                    effectSummary = buildLawEffectSummary(law),
                    enabled = isActive || viewModel.canEnactLaw(law.id),
                    onToggle = { enabled -> pendingToggle = LawToggleRequest(law = law, enabling = enabled) },
                )
            }
        }
    }

    pendingToggle?.let { request ->
        LawToggleConfirmationDialog(
            request = request,
            onConfirm = {
                if (request.enabling) viewModel.enactLaw(request.law.id) else viewModel.repealLaw(request.law.id)
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
    category: LawCategory,
    isActive: Boolean,
    effectSummary: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .then(
                if (isActive) Modifier
                    .background(NssGameCard)
                    .border(2.dp, NssAccent.copy(alpha = 0.4f), NssCardShape)
                else Modifier.background(NssGameCard),
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.lawCategoryImage(category),
                fallbackGradient = NssGradients.Indigo,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(law.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                    if (isActive) NssBadge(label = "ACTIVE")
                }
                Text(effectSummary, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
                Text("Upkeep ${law.upkeepCost.toBudgetString()}/mo", fontSize = 10.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 2.dp))
            }
            Switch(
                checked = isActive,
                onCheckedChange = { checked -> if (checked != isActive) onToggle(checked) },
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
        "Repealing this law removes its modifiers immediately. No refund is issued for prior activation costs."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(if (request.enabling) "Enact" else "Repeal") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun buildLawEffectSummary(law: Law): String {
    val parts = buildList {
        if (law.approvalModifier != 0f) add("${formatSigned(law.approvalModifier)}% Approval")
        if (law.upkeepCost > 0L) add("-${law.upkeepCost.toBudgetString()} Budget/mo")
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
    val body = if (value % 1f == 0f) value.roundToInt().toString() else "%.1f".format(value)
    return if (value > 0f) "+$body" else body
}

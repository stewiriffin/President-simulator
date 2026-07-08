package com.presidentsimulator.game.ui.legacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.data.ResourceFlow
import com.presidentsimulator.game.data.ResourceType
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.StarkWhite
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.ProductionLawViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toResourceString
import kotlin.math.roundToInt

/**
 * Ministry of Industry: high-density resource balance dashboard.
 */
@Composable
fun IndustryMinistryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val production = state.production
    var plantAmount by remember { mutableIntStateOf(1) }
    var mineAmount by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Ministry of Industry",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Production modifier: " +
                "${(state.legal.combinedProductionModifier * 100f).roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (production.energyShortage || production.foodShortage) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (production.energyShortage) {
                        Text(
                            text = "Energy shortage — industrial output penalized to 30%.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (production.foodShortage) {
                        Text(
                            text = "Food shortage — approval and population are falling.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        ResourceType.entries.forEach { resource ->
            ResourceBalanceCard(
                resource = resource,
                stock = when (resource) {
                    ResourceType.ENERGY -> production.energy
                    ResourceType.FOOD -> production.food
                    ResourceType.MATERIALS -> production.materials
                    ResourceType.GOODS -> production.goods
                },
                flow = production.flow(resource),
                extra = when (resource) {
                    ResourceType.GOODS ->
                        "Revenue last tick: ${production.lastGoodsRevenue.toBudgetString()}"
                    else -> null
                },
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Industrial Capacity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                CapacityRow("Power Plants", production.powerPlants)
                CapacityRow("Mines", production.mines)
                CapacityRow("Factories", state.economy.factories)
                CapacityRow("Farms", state.economy.farms)
                CapacityRow("Housing", state.economy.housing)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Expand Capacity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IndustryBuildRow(
                    type = InfrastructureType.POWER_PLANT,
                    selectedAmount = plantAmount,
                    maxAffordable = viewModel.maxAffordable(InfrastructureType.POWER_PLANT),
                    onAmountSelected = { plantAmount = it },
                    onBuild = {
                        viewModel.buildPowerPlant(it)
                        plantAmount = 1
                    },
                )
                HorizontalDivider()
                IndustryBuildRow(
                    type = InfrastructureType.MINE,
                    selectedAmount = mineAmount,
                    maxAffordable = viewModel.maxAffordable(InfrastructureType.MINE),
                    onAmountSelected = { mineAmount = it },
                    onBuild = {
                        viewModel.buildMine(it)
                        mineAmount = 1
                    },
                )
            }
        }
    }
}

/**
 * Parliament: ideology readout and enactable laws by category.
 */
@Composable
fun ParliamentScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Parliament",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "National Ideology",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = state.legal.ideology.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Active laws: ${state.legal.activeLawIds.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Law upkeep: ${state.legal.totalUpkeep.toBudgetString()}/mo",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Approval modifier: " +
                        "${formatSigned(state.legal.combinedApprovalModifier)}%",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Production modifier: " +
                        "${(state.legal.combinedProductionModifier * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        LawCategory.entries.forEach { category ->
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
            LawCatalog.byCategory(category).forEach { law ->
                LawCard(
                    law = law,
                    isActive = state.legal.isActive(law.id),
                    canEnact = ProductionLawViewModel.canEnact(state, law),
                    onEnact = { viewModel.enactLaw(law.id) },
                    onRepeal = { viewModel.repealLaw(law.id) },
                )
            }
        }
    }
}

// ── Resource cards ───────────────────────────────────────────────────────────

@Composable
private fun ResourceBalanceCard(
    resource: ResourceType,
    stock: Long,
    flow: ResourceFlow,
    extra: String?,
) {
    val surplusColor = if (flow.isDeficit) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val ratio = if (flow.consumed <= 0L) {
        1f
    } else {
        (flow.produced.toFloat() / flow.consumed.toFloat()).coerceIn(0f, 1.5f) / 1.5f
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = resource.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Stock: ${stock.toResourceString()}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "+${flow.produced.toResourceString()} / -${flow.consumed.toResourceString()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = surplusColor,
            )
            Text(
                text = if (flow.isDeficit) {
                    "Deficit ${(-flow.surplus).toResourceString()}"
                } else {
                    "Surplus ${flow.surplus.toResourceString()}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = surplusColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = surplusColor,
            )
            if (extra != null) {
                Text(
                    text = extra,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CapacityRow(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun IndustryBuildRow(
    type: InfrastructureType,
    selectedAmount: Int,
    maxAffordable: Int,
    onAmountSelected: (Int) -> Unit,
    onBuild: (Int) -> Unit,
) {
    val presets = listOf(1, 10)
    val amount = selectedAmount.coerceIn(0, maxAffordable.coerceAtLeast(0))
    val canBuild = amount > 0

    Column {
        Text(
            text = "Build ${type.displayName}",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "Unit cost: ${type.unitCost.toBudgetString()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                FilterChip(
                    selected = selectedAmount == preset,
                    onClick = { if (maxAffordable >= preset) onAmountSelected(preset) },
                    enabled = maxAffordable >= preset,
                    label = { Text("${preset}x") },
                )
            }
            FilterChip(
                selected = selectedAmount == maxAffordable &&
                    maxAffordable > 0 &&
                    selectedAmount !in presets,
                onClick = { if (maxAffordable > 0) onAmountSelected(maxAffordable) },
                enabled = maxAffordable > 0,
                label = { Text(if (maxAffordable > 0) "Max ($maxAffordable)" else "Max") },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = { onBuild(amount) },
            enabled = canBuild,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (canBuild) {
                    "Commit $amount × ${type.displayName} " +
                        "(${(type.unitCost * amount).toBudgetString()})"
                } else {
                    "Cannot afford"
                },
            )
        }
    }
}

// ── Law cards ────────────────────────────────────────────────────────────────

@Composable
private fun LawCard(
    law: Law,
    isActive: Boolean,
    canEnact: Boolean,
    onEnact: () -> Unit,
    onRepeal: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                NssEmerald.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = law.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (isActive) {
                    ActiveBadge()
                }
            }
            Text(
                text = law.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildModifierSummary(law),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Activation: ${law.activationCost.toBudgetString()} · " +
                    "Upkeep: ${law.upkeepCost.toBudgetString()}/mo · " +
                    "Needs ${law.approvalThreshold.roundToInt()}% approval",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (isActive) {
                OutlinedButton(
                    onClick = onRepeal,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Repeal Law")
                }
            } else {
                Button(
                    onClick = onEnact,
                    enabled = canEnact,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(if (canEnact) "Enact" else "Cannot Enact")
                }
            }
        }
    }
}

@Composable
private fun ActiveBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ProfitGreen)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = "Active",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = StarkWhite,
        )
    }
}

private fun buildModifierSummary(law: Law): String {
    val parts = buildList {
        if (law.approvalModifier != 0f) {
            add("Approval ${formatSigned(law.approvalModifier)}%")
        }
        if (law.productionModifier != 1f) {
            val pct = ((law.productionModifier - 1f) * 100f).roundToInt()
            add("Production ${formatSigned(pct.toFloat())}%")
        }
        if (law.foodDemandModifier != 1f) {
            val pct = ((law.foodDemandModifier - 1f) * 100f).roundToInt()
            add("Food demand ${formatSigned(pct.toFloat())}%")
        }
        if (law.energyDemandModifier != 1f) {
            val pct = ((law.energyDemandModifier - 1f) * 100f).roundToInt()
            add("Energy demand ${formatSigned(pct.toFloat())}%")
        }
        if (law.militaryRecruitModifier != 1f) {
            val pct = ((law.militaryRecruitModifier - 1f) * 100f).roundToInt()
            add("Recruitment ${formatSigned(pct.toFloat())}%")
        }
    }
    return parts.joinToString(" · ").ifEmpty { "No direct modifiers" }
}

private fun formatSigned(value: Float): String {
    val rounded = if (value % 1f == 0f) {
        value.roundToInt().toString()
    } else {
        "%.1f".format(value)
    }
    return if (value > 0f) "+$rounded" else rounded
}

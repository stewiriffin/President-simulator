package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.ui.components.BulkBuildControls
import com.presidentsimulator.game.ui.theme.DeficitRed
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private data class BuildableAsset(
    val type: InfrastructureType,
    val currentCount: Int,
)

/**
 * Dense economy ministry: live tax projections and bulk infrastructure builds.
 */
@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    val economy = state.economy
    val population = state.vitals.population

    var draftTaxRate by remember(economy.taxRate) {
        mutableFloatStateOf(economy.taxRate.coerceIn(0f, 0.50f))
    }

    val currentTaxRevenue = economy.taxRevenue(population)
    val projectedRevenue = viewModel.projectTaxRevenue(draftTaxRate)
    val projectedChange = projectedRevenue - currentTaxRevenue
    val changePrefix = if (projectedChange >= 0L) "+" else ""

    val assets = listOf(
        BuildableAsset(InfrastructureType.FACTORY, economy.factories),
        BuildableAsset(InfrastructureType.FARM, economy.farms),
        BuildableAsset(InfrastructureType.POWER_PLANT, state.production.powerPlants),
    )

    var selectedAmounts by remember {
        mutableStateOf(
            assets.associate { it.type to 1 },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Ministry of Economy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Taxation Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Income Tax: ${(draftTaxRate * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Slider(
                        value = draftTaxRate,
                        onValueChange = { draftTaxRate = it },
                        onValueChangeFinished = { viewModel.adjustTaxes(draftTaxRate) },
                        valueRange = 0f..0.50f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "Current Revenue: ${currentTaxRevenue.toBudgetString()} | " +
                            "Projected Change: $changePrefix${projectedChange.toBudgetString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (projectedChange >= 0L) ProfitGreen else DeficitRed,
                    )
                    Text(
                        text = "Projected Monthly Revenue: ${projectedRevenue.toBudgetString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item {
            Text(
                text = "Infrastructure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Select quantity with [ 1x | 10x | Max ], then commit once.",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
        }

        items(assets, key = { it.type.name }) { asset ->
            val selectedAmount = selectedAmounts[asset.type] ?: 1
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BulkBuildControls(
                        assetName = asset.type.displayName,
                        currentCount = asset.currentCount,
                        unitCost = asset.type.unitCost,
                        selectedAmount = selectedAmount,
                        maxAffordable = viewModel.maxAffordable(asset.type),
                        onAmountSelected = { amount ->
                            selectedAmounts = selectedAmounts.toMutableMap().apply {
                                put(asset.type, amount)
                            }
                        },
                        onBuild = { amount ->
                            when (asset.type) {
                                InfrastructureType.FACTORY -> viewModel.buildFactory(amount)
                                InfrastructureType.FARM -> viewModel.buildFarm(amount)
                                InfrastructureType.POWER_PLANT -> viewModel.buildPowerPlant(amount)
                                else -> viewModel.buildInfrastructure(asset.type, amount)
                            }
                            audio.playBuildSuccess()
                            selectedAmounts = selectedAmounts.toMutableMap().apply {
                                put(asset.type, 1)
                            }
                        },
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Cash Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CashFlowRow("Taxes", economy.taxRevenue(population).toBudgetString())
                    CashFlowRow("Exports", economy.effectiveExports.toBudgetString())
                    CashFlowRow("Goods sales", state.production.lastGoodsRevenue.toBudgetString())
                    CashFlowRow("Imports", "-${economy.effectiveImports.toBudgetString()}")
                    CashFlowRow("Infra upkeep", "-${economy.upkeep.toBudgetString()}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    CashFlowRow(
                        label = "Net",
                        value = state.netIncome.toBudgetString(),
                        emphasize = true,
                        positive = state.netIncome >= 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun CashFlowRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    positive: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = when {
                !emphasize -> MaterialTheme.colorScheme.onSurface
                positive -> ProfitGreen
                else -> DeficitRed
            },
        )
    }
}

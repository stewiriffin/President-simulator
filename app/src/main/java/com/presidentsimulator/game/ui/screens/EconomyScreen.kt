package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.ui.components.HeroStat
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssCard
import com.presidentsimulator.game.ui.components.NssHeroBanner
import com.presidentsimulator.game.ui.components.NssSectionHead
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.theme.GameIcons
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.StarkWhite
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toResourceString
import kotlin.math.roundToInt

private data class InfraRowModel(
    val type: InfrastructureType,
    val owned: Int,
    val outputLabel: String,
)

@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf("POLICY") }
    val tabs = listOf("POLICY", "INFRASTRUCTURE", "RESOURCES")
    val gdp = remember(state) { AnalyticsSaveViewModel().calculateGDP(state) }
    val tradeBalance = state.economy.effectiveExports - state.economy.effectiveImports

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        NssHeroBanner(
            ministryLabel = "ECONOMY",
            stats = listOf(
                HeroStat("Total GDP", formatMa2Money(gdp), true),
                HeroStat("Tax Rate", "${(state.economy.taxRate * 100).roundToInt()}%", null),
                HeroStat("Trade Bal.", formatMa2Money(tradeBalance), tradeBalance >= 0),
                HeroStat("Net / mo", formatMa2Money(state.netIncome), state.netIncome >= 0),
            ),
        )

        NssTabBar(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                "POLICY" -> TaxesTab(state = state, viewModel = viewModel)
                "INFRASTRUCTURE" -> InfrastructureTab(state = state, viewModel = viewModel)
                else -> ResourcesTab(state = state)
            }
        }
    }
}

@Composable
private fun TaxesTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val economy = state.economy
    val population = state.vitals.population
    var draftTaxRate by remember(economy.taxRate) {
        mutableFloatStateOf(economy.taxRate.coerceIn(0f, 0.50f))
    }
    val currentRevenue = economy.taxRevenue(population)
    val projectedRevenue = viewModel.projectTaxRevenue(draftTaxRate)
    val projectedChange = projectedRevenue - currentRevenue
    val changePrefix = if (projectedChange >= 0L) "+" else ""

    NssSectionHead(
        title = "Economic Policy Controls",
        subtitle = "Changes take effect at start of next month",
    )

    NssCard {
        Text(
            text = "INCOME TAX",
            style = MaterialTheme.typography.labelLarge,
            color = NssMutedForeground,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Higher tax → more revenue, lower growth",
                style = MaterialTheme.typography.bodySmall,
                color = NssMutedForeground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(draftTaxRate * 100).roundToInt()}%",
                color = NssPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Slider(
            value = draftTaxRate,
            onValueChange = { draftTaxRate = it },
            onValueChangeFinished = { viewModel.adjustTaxes(draftTaxRate) },
            valueRange = 0f..0.50f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = NssPrimary,
                activeTrackColor = NssPrimary,
                inactiveTrackColor = NssBorder,
            ),
        )
        Text(
            text = "Current Revenue: ${formatMa2Money(currentRevenue)} | " +
                "Projected Change: $changePrefix${formatMa2Money(projectedChange)}",
            style = MaterialTheme.typography.bodySmall,
            color = if (projectedChange >= 0L) NssEmerald else NssRed,
        )
    }

    NssCard {
        LedgerLine("Taxes", formatMa2Money(economy.taxRevenue(population)), NssEmerald)
        LedgerLine("Exports", formatMa2Money(economy.effectiveExports), NssEmerald)
        LedgerLine("Goods sales", formatMa2Money(state.production.lastGoodsRevenue), NssEmerald)
        LedgerLine("Imports", "-${formatMa2Money(economy.effectiveImports)}", NssMutedForeground)
        LedgerLine("Upkeep", "-${formatMa2Money(economy.upkeep)}", NssMutedForeground)
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 8.dp))
        LedgerLine(
            "Net / month",
            formatMa2Money(state.netIncome),
            if (state.netIncome >= 0) NssEmerald else NssRed,
            bold = true,
        )
    }

    NssAlertBanner(
        message = "Pending tax changes commit at start of next month · review before confirming",
    )
}

@Composable
private fun InfrastructureTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    val economy = state.economy
    val production = state.production

    val rows = listOf(
        InfraRowModel(InfrastructureType.FACTORY, economy.factories, "+${formatMa2Money(economy.factories * 120_000_000L)}/mo"),
        InfraRowModel(InfrastructureType.FARM, economy.farms, "+${economy.farms * 180} food/mo"),
        InfraRowModel(InfrastructureType.POWER_PLANT, production.powerPlants, "+${production.powerPlants * 120} energy/mo"),
        InfraRowModel(InfrastructureType.HOUSING, economy.housing, "Cap ${economy.housing * 1_500_000}"),
        InfraRowModel(InfrastructureType.MINE, production.mines, "+${production.mines * 90} mat/mo"),
    )

    var selectedAmounts by remember {
        mutableStateOf(rows.associate { it.type to 1 })
    }

    NssSectionHead(
        title = "Infrastructure Procurement",
        subtitle = "Bulk build controls · 1x | 10x | Max",
    )

    rows.forEach { row ->
        val selected = selectedAmounts[row.type] ?: 1
        val maxAffordable = viewModel.maxAffordable(row.type)
        InfrastructureLedgerRow(
            icon = GameIcons.forInfrastructure(row.type),
            name = row.type.displayName,
            owned = row.owned,
            outputLabel = row.outputLabel,
            unitCost = row.type.unitCost,
            selectedAmount = selected,
            maxAffordable = maxAffordable,
            onAmountSelected = { amount ->
                selectedAmounts = selectedAmounts.toMutableMap().apply { put(row.type, amount) }
            },
            onBuild = { amount ->
                when (row.type) {
                    InfrastructureType.FACTORY -> viewModel.buildFactory(amount)
                    InfrastructureType.FARM -> viewModel.buildFarm(amount)
                    InfrastructureType.HOUSING -> viewModel.buildHousing(amount)
                    InfrastructureType.POWER_PLANT -> viewModel.buildPowerPlant(amount)
                    InfrastructureType.MINE -> viewModel.buildMine(amount)
                }
                audio.playBuildSuccess()
                selectedAmounts = selectedAmounts.toMutableMap().apply { put(row.type, 1) }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun InfrastructureLedgerRow(
    icon: ImageVector,
    name: String,
    owned: Int,
    outputLabel: String,
    unitCost: Long,
    selectedAmount: Int,
    maxAffordable: Int,
    onAmountSelected: (Int) -> Unit,
    onBuild: (Int) -> Unit,
) {
    val presets = listOf(1, 10)
    val amount = selectedAmount.coerceIn(0, maxAffordable.coerceAtLeast(0))
    val totalCost = unitCost * amount
    val canBuild = amount > 0

    NssCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NssPrimary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Owned: $owned", color = NssMutedForeground, fontSize = 11.sp)
                Text(outputLabel, color = NssEmerald, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Cost: ${formatMa2Money(if (canBuild) totalCost else unitCost)}",
                    color = NssAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = selectedAmount == preset,
                            onClick = { if (maxAffordable >= preset) onAmountSelected(preset) },
                            enabled = maxAffordable >= preset,
                            label = { Text("${preset}x", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NssPrimary,
                                selectedLabelColor = StarkWhite,
                            ),
                        )
                    }
                    FilterChip(
                        selected = selectedAmount == maxAffordable &&
                            maxAffordable > 0 &&
                            selectedAmount !in presets,
                        onClick = { if (maxAffordable > 0) onAmountSelected(maxAffordable) },
                        enabled = maxAffordable > 0,
                        label = { Text("Max", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NssPrimary,
                            selectedLabelColor = StarkWhite,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onBuild(amount) },
                    enabled = canBuild,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NssAccent,
                        contentColor = NssBackground,
                    ),
                ) {
                    Text("BUILD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ResourcesTab(state: GameState) {
    val production = state.production
    NssSectionHead(title = "Resource Balance", subtitle = "Production vs consumption")
    NssCard {
        ResourceRow(GameIcons.ResourceEnergy, "Energy", production.energy, production.lastEnergyProduced, production.lastEnergyConsumed)
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 8.dp))
        ResourceRow(GameIcons.ResourceFood, "Food", production.food, production.lastFoodProduced, production.lastFoodConsumed)
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 8.dp))
        ResourceRow(GameIcons.ResourceMaterials, "Materials", production.materials, production.lastMaterialsProduced, production.lastMaterialsConsumed)
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 8.dp))
        ResourceRow(GameIcons.ResourceConsumerGoods, "Goods", production.goods, production.lastGoodsProduced, production.lastGoodsSold)
    }
}

@Composable
private fun ResourceRow(
    icon: ImageVector,
    name: String,
    stock: Long,
    produced: Long,
    consumed: Long,
) {
    val surplus = produced - consumed
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = NssPrimary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = NssForeground, fontWeight = FontWeight.Bold)
            Text("Stock: ${stock.toResourceString()}", color = NssMutedForeground, fontSize = 11.sp)
        }
        Text(
            "+${produced.toResourceString()} / -${consumed.toResourceString()}",
            color = if (surplus >= 0) NssEmerald else NssRed,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun LedgerLine(
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            color = NssForeground,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
        )
        Text(
            value,
            color = color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssCard
import com.presidentsimulator.game.ui.components.NssCompactKpi
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssMinistryBanner
import com.presidentsimulator.game.ui.components.NssProgressBar
import com.presidentsimulator.game.ui.components.NssSectorCard
import com.presidentsimulator.game.ui.components.NssStripPhotoCard
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.theme.GameIcons
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.ui.theme.NssIndigo
import com.presidentsimulator.game.ui.theme.NssOrange
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toResourceString
import kotlin.math.roundToInt

private data class SectorModel(
    val name: String,
    val gdpShare: Float,
    val employment: Float,
    val growth: Float,
    val level: Int,
    val gradient: List<Color>,
    val imageUrl: String,
)

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
    var selectedTab by remember { mutableStateOf("SECTORS") }
    val tabs = listOf("SECTORS", "POLICY", "BUDGET", "TRADE")
    val gdp = remember(state) { AnalyticsSaveViewModel().calculateGDP(state) }
    val tradeBalance = state.economy.effectiveExports - state.economy.effectiveImports
    val sectors = remember(state) { buildSectors(state, gdp) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        NssMinistryBanner(
            ministryLabel = "ECONOMY",
            imageUrl = NssCardImages.BANNER_ECONOMY,
            statPills = listOf(
                "GDP ${formatMa2Money(gdp)}",
                "Growth +${(state.netIncome.coerceAtLeast(0) * 100 / gdp.coerceAtLeast(1)).coerceAtMost(99)}%",
                "Inflation 3.7%",
            ),
            gradientColors = NssGradients.Economy,
        )

        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                "SECTORS" -> SectorsTab(state = state, gdp = gdp, sectors = sectors)
                "POLICY" -> PolicyTab(state = state, viewModel = viewModel)
                "BUDGET" -> BudgetTab(state = state)
                else -> TradeTab(state = state)
            }
        }
    }
}

@Composable
private fun SectorsTab(state: GameState, gdp: Long, sectors: List<SectorModel>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NssCompactKpi("Total GDP", formatMa2Money(gdp), "▲ YOY", true, Modifier.weight(1f))
        NssCompactKpi("Net / mo", formatMa2Money(state.netIncome), if (state.netIncome >= 0) "▲ surplus" else "▼ deficit", state.netIncome >= 0, Modifier.weight(1f))
        NssCompactKpi("Factories", state.economy.factories.toString(), "Industrial base", true, Modifier.weight(1f))
        NssCompactKpi("Approval", "${state.vitals.approval.roundToInt()}%", "Public support", state.vitals.approval >= 50f, Modifier.weight(1f))
    }

    sectors.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { sector ->
                NssSectorCard(
                    name = sector.name,
                    gdpShare = sector.gdpShare,
                    employment = sector.employment,
                    growth = sector.growth,
                    level = sector.level,
                    headerGradient = sector.gradient,
                    imageUrl = sector.imageUrl,
                    onInvest = { },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PolicyTab(state: GameState, viewModel: GameViewModel) {
    val economy = state.economy
    val population = state.vitals.population
    var draftTaxRate by remember(economy.taxRate) { mutableFloatStateOf(economy.taxRate.coerceIn(0f, 0.50f)) }
    val currentRevenue = economy.taxRevenue(population)
    val projectedRevenue = viewModel.projectTaxRevenue(draftTaxRate)
    val projectedChange = projectedRevenue - currentRevenue

    val policies = listOf(
        "TAX RATE" to draftTaxRate,
    )

    policies.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (key, _) ->
                NssStripPhotoCard(
                    imageUrl = NssCardImages.BANNER_ECONOMY,
                    fallbackGradient = NssGradients.Economy,
                    modifier = Modifier.weight(1f),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(key, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${(draftTaxRate * 100).roundToInt()}%", color = NssPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Slider(
                        value = draftTaxRate,
                        onValueChange = { draftTaxRate = it },
                        onValueChangeFinished = { viewModel.adjustTaxes(draftTaxRate) },
                        valueRange = 0f..0.50f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = NssPrimary, activeTrackColor = NssPrimary, inactiveTrackColor = NssBorder),
                    )
                    NssProgressBar(percent = draftTaxRate * 200f, color = NssPrimary, thick = true)
                }
            }
        }
    }

    NssCard {
        LedgerLine("Current Revenue", formatMa2Money(currentRevenue), NssEmerald)
        LedgerLine("Projected Change", "${if (projectedChange >= 0) "+" else ""}${formatMa2Money(projectedChange)}", if (projectedChange >= 0) NssEmerald else NssRed)
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 8.dp))
        LedgerLine("Net / month", formatMa2Money(state.netIncome), if (state.netIncome >= 0) NssEmerald else NssRed, bold = true)
    }

    NssAlertBanner("Pending changes commit at start of next month")
}

@Composable
private fun BudgetTab(state: GameState) {
    val budgetLines = listOf(
        BudgetLine("Social Services", state.society.totalMinistryUpkeep, NssEmerald, NssCardImages.SERVICES, NssGradients.Emerald),
        BudgetLine("Defense", state.military.monthlyUpkeep, NssPrimary, NssCardImages.DEFENSE_IND, NssGradients.Defense),
        BudgetLine("Security", state.internalSecurity.monthlyUpkeep, NssViolet, NssCardImages.BANNER_INTELLIGENCE, NssGradients.Violet),
        BudgetLine("Legal / Admin", state.legal.totalUpkeep, NssIndigo, NssCardImages.BANNER_DOMESTIC, NssGradients.Indigo),
        BudgetLine("Infrastructure", state.economy.upkeep, NssOrange, NssCardImages.INDUSTRY, NssGradients.Amber),
    )
    val total = budgetLines.sumOf { it.spent }.coerceAtLeast(1L)

    budgetLines.forEach { line ->
        val pct = (line.spent.toFloat() / total * 100f).coerceIn(0f, 100f)
        NssStripPhotoCard(
            imageUrl = line.imageUrl,
            fallbackGradient = line.fallbackGradient,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.width(4.dp).height(48.dp).background(line.accent))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(line.dept, color = NssForeground, fontWeight = FontWeight.Bold)
                        Text("${pct.roundToInt()}% of budget", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NssProgressBar(percent = pct, color = line.accent, thick = true)
                    Text(
                        text = "Spent: ${formatMa2Money(line.spent)}/mo",
                        style = MaterialTheme.typography.labelSmall,
                        color = NssMutedForeground,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

private data class BudgetLine(
    val dept: String,
    val spent: Long,
    val accent: Color,
    val imageUrl: String,
    val fallbackGradient: List<Color>,
)

@Composable
private fun TradeTab(state: GameState) {
    state.diplomacy.rivals.filter { it.hasTradeTreaty || it.relationshipScore >= 40 }.take(6).chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { rival ->
                val exports = if (rival.hasTradeTreaty) 30_000_000_000L else 10_000_000_000L
                val imports = 15_000_000_000L
                val balance = exports - imports
                NssStripPhotoCard(
                    imageUrl = NssCardImages.BANNER_FOREIGN,
                    fallbackGradient = NssGradients.Foreign,
                    modifier = Modifier.weight(1f),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(rival.name, color = NssForeground, fontWeight = FontWeight.Bold)
                        Text(
                            if (rival.hasTradeTreaty) "PARTNER" else "NEUTRAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = NssPrimary,
                        )
                    }
                    Text(
                        text = "${if (balance >= 0) "+" else ""}${formatMa2Money(balance)}",
                        color = if (balance >= 0) NssEmerald else NssRed,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Text("TRADE BALANCE", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("EXPORTS", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                            Text(formatMa2Money(exports), color = NssEmerald, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("IMPORTS", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                            Text(formatMa2Money(imports), color = NssRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun buildSectors(state: GameState, gdp: Long): List<SectorModel> {
    val economy = state.economy
    val production = state.production
    val total = (economy.factories + economy.farms + production.powerPlants + production.mines + 1).toFloat()
    return listOf(
        SectorModel("Services", 37.6f, 42.3f, 3.1f, 4, NssGradients.Emerald, NssCardImages.SERVICES),
        SectorModel("Heavy Industry", economy.factories / total * 100f, 18.7f, 1.8f, economy.factories.coerceIn(1, 5), NssGradients.Sky, NssCardImages.INDUSTRY),
        SectorModel("Manufacturing", production.lastGoodsProduced.coerceAtMost(100).toFloat(), 14.2f, 0.9f, 3, NssGradients.Indigo, NssCardImages.MANUFACTURING),
        SectorModel("Technology", state.research.unlockedTechIds.size.toFloat() * 8f, 8.9f, 6.7f, state.research.unlockedTechIds.size.coerceIn(1, 5), NssGradients.Violet, NssCardImages.TECHNOLOGY),
        SectorModel("Agriculture", economy.farms / total * 100f, 6.1f, -0.3f, economy.farms.coerceIn(1, 5), NssGradients.Amber, NssCardImages.AGRICULTURE),
        SectorModel("Energy", production.powerPlants / total * 100f, 3.4f, -1.2f, production.powerPlants.coerceIn(1, 5), NssGradients.Orange, NssCardImages.ENERGY),
        SectorModel("Defense Ind.", state.military.tanks.coerceAtMost(20).toFloat(), 6.4f, 2.1f, 4, NssGradients.Red, NssCardImages.DEFENSE_IND),
    )
}

@Composable
private fun LedgerLine(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = NssForeground, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
        Text(value, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

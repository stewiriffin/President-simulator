package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
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
import com.presidentsimulator.game.data.ResourceType
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssCard
import com.presidentsimulator.game.ui.components.NssCompactKpi
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssProgressBar
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssSectorBarColors
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssSectorCard
import com.presidentsimulator.game.ui.components.NssStripPhotoCard
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.mutableIntStateOf
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.GameIcons
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.ui.theme.NssIndigo
import com.presidentsimulator.game.ui.theme.NssOrange
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toResourceString
import kotlin.math.roundToInt

private enum class SectorInvestAction {
    FACTORY, FARM, HOUSING, POWER_PLANT, MINE, UNIVERSITY, TANKS
}

private data class SectorModel(
    val name: String,
    val gdpShare: Float,
    val employment: Float,
    val growth: Float,
    val level: Int,
    val xp: Int,
    val gradient: List<Color>,
    val imageUrl: String,
    val investAction: SectorInvestAction? = null,
    val investLabel: String = "⬆ Invest",
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
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var selectedTab by remember { mutableStateOf("SECTORS") }
    val tabs = listOf("SECTORS", "INDUSTRY", "POLICY", "BUDGET", "TRADE")
    val gdp = remember(state) { AnalyticsSaveViewModel().calculateGDP(state) }
    val sectors = remember(state) { buildSectors(state, gdp) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "Economy",
            imageUrl = NssCardImages.BANNER_ECONOMY,
            statPills = listOf(
                "GDP" to formatMa2Money(gdp),
                "Growth" to "+${(state.netIncome.coerceAtLeast(0) * 100 / gdp.coerceAtLeast(1)).coerceAtMost(99)}%",
                "Revenue" to formatMa2Money(gdp / 12),
            ),
            gradientColors = NssGradients.Economy,
        )

        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall),
        ) {
            when (selectedTab) {
                "SECTORS" -> SectorsTab(state, gdp, sectors, viewModel, audio)
                "INDUSTRY" -> IndustryTab(state = state, viewModel = viewModel, audio = audio)
                "POLICY" -> PolicyTab(state = state, viewModel = viewModel)
                "BUDGET" -> BudgetTab(state = state)
                else -> TradeTab(state = state, viewModel = viewModel, audio = audio)
            }
        }
    }
}

@Composable
private fun IndustryTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val production = state.production
    var plantAmount by remember { mutableIntStateOf(1) }
    var mineAmount by remember { mutableIntStateOf(1) }

    Text(
        text = "Production modifier ${(state.legal.combinedProductionModifier * 100f).roundToInt()}%",
        fontSize = 12.sp,
        color = NssMutedForeground,
    )

    if (production.energyShortage || production.foodShortage) {
        NssPanel(modifier = Modifier.fillMaxWidth()) {
            if (production.energyShortage) {
                Text(
                    "Energy shortage — industrial output penalized to 30%.",
                    color = NssRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
            if (production.foodShortage) {
                Text(
                    "Food shortage — approval and population are falling.",
                    color = NssRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = if (production.energyShortage) 6.dp else 0.dp),
                )
            }
        }
    }

    val energyFlow = production.flow(ResourceType.ENERGY)
    IndustryResourceCard(ResourceType.ENERGY, production.energy, energyFlow.produced, energyFlow.consumed)
    val foodFlow = production.flow(ResourceType.FOOD)
    IndustryResourceCard(ResourceType.FOOD, production.food, foodFlow.produced, foodFlow.consumed)
    val materialsFlow = production.flow(ResourceType.MATERIALS)
    IndustryResourceCard(ResourceType.MATERIALS, production.materials, materialsFlow.produced, materialsFlow.consumed)
    val goodsFlow = production.flow(ResourceType.GOODS)
    IndustryResourceCard(
        resource = ResourceType.GOODS,
        stock = production.goods,
        produced = goodsFlow.produced,
        consumed = goodsFlow.consumed,
        extra = "Goods revenue last tick ${production.lastGoodsRevenue.toBudgetString()}",
    )

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("INDUSTRIAL CAPACITY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        CapacityLine("Power Plants", production.powerPlants)
        CapacityLine("Mines", production.mines)
        CapacityLine("Factories", state.economy.factories)
        CapacityLine("Farms", state.economy.farms)
        CapacityLine("Housing", state.economy.housing)
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("EXPAND CAPACITY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        IndustryBuildControls(
            label = "Power Plants",
            amount = plantAmount,
            maxAffordable = viewModel.maxAffordable(InfrastructureType.POWER_PLANT),
            onAmountChange = { plantAmount = it },
            onBuild = {
                viewModel.buildPowerPlant(plantAmount)
                audio.playBuildSuccess()
                plantAmount = 1
            },
        )
        IndustryBuildControls(
            label = "Mines",
            amount = mineAmount,
            maxAffordable = viewModel.maxAffordable(InfrastructureType.MINE),
            onAmountChange = { mineAmount = it },
            onBuild = {
                viewModel.buildMine(mineAmount)
                audio.playBuildSuccess()
                mineAmount = 1
            },
        )
    }
}

@Composable
private fun IndustryResourceCard(
    resource: ResourceType,
    stock: Long,
    produced: Long,
    consumed: Long,
    extra: String? = null,
) {
    val surplus = produced - consumed
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(resource.displayName, fontWeight = FontWeight.Bold, color = NssForeground)
            Text("Stock ${stock.toResourceString()}", color = NssMutedForeground, fontSize = 12.sp)
        }
        Text(
            "+${produced.toResourceString()} / −${consumed.toResourceString()}",
            fontSize = 12.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        NssGameBar(
            percent = ((produced.toFloat() / (produced + consumed).coerceAtLeast(1).toFloat()) * 100f)
                .coerceIn(5f, 100f),
            color = if (surplus >= 0) NssEmerald else NssRed,
            thick = true,
        )
        Text(
            text = if (surplus >= 0) "Surplus ${surplus.toResourceString()}" else "Deficit ${(-surplus).toResourceString()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (surplus >= 0) NssEmerald else NssRed,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (extra != null) {
            Text(extra, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun CapacityLine(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = NssForeground, fontSize = 13.sp)
        Text(count.toString(), fontWeight = FontWeight.Bold, color = NssPrimary)
    }
}

@Composable
private fun IndustryBuildControls(
    label: String,
    amount: Int,
    maxAffordable: Int,
    onAmountChange: (Int) -> Unit,
    onBuild: () -> Unit,
) {
    val cappedMax = maxAffordable.coerceAtLeast(1)
    Text(
        "$label · build $amount (max $maxAffordable)",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NssForeground,
        modifier = Modifier.padding(top = 10.dp),
    )
    Slider(
        value = amount.toFloat().coerceIn(1f, cappedMax.toFloat()),
        onValueChange = { onAmountChange(it.roundToInt().coerceIn(1, cappedMax)) },
        valueRange = 1f..cappedMax.toFloat(),
        steps = (cappedMax - 2).coerceAtLeast(0),
        colors = SliderDefaults.colors(thumbColor = NssPrimary, activeTrackColor = NssPrimary, inactiveTrackColor = NssBorder),
    )
    Text(
        text = "BUILD $label",
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .background(if (maxAffordable > 0) NssPrimary else NssMutedForeground.copy(alpha = 0.35f))
            .clickable(enabled = maxAffordable > 0, onClick = onBuild)
            .padding(vertical = 10.dp),
        color = NssOnPhoto,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun SectorsTab(
    state: GameState,
    gdp: Long,
    sectors: List<SectorModel>,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val layout = rememberNssLayoutSpec()
    GdpBreakdownCard(sectors = sectors)

    Text(
        text = "SECTOR MANAGEMENT",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 3.sp,
        modifier = Modifier.padding(top = Dimens.SpacingSmall, bottom = Dimens.SpacingXSmall),
    )

    sectors.chunked(layout.gridColumns).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
            row.forEach { sector ->
                NssSectorCard(
                    name = sector.name,
                    gdpShare = sector.gdpShare,
                    employment = sector.employment,
                    growth = sector.growth,
                    level = sector.level,
                    headerGradient = sector.gradient,
                    imageUrl = sector.imageUrl,
                    xpPercent = sector.xp,
                    revenueLabel = "${formatMa2Money((gdp * sector.gdpShare / 100f).toLong())}/yr",
                    investEnabled = sector.investAction != null,
                    investLabel = sector.investLabel,
                    onInvest = {
                        when (sector.investAction) {
                            SectorInvestAction.FACTORY -> viewModel.buildFactory(1)
                            SectorInvestAction.FARM -> viewModel.buildFarm(1)
                            SectorInvestAction.HOUSING -> viewModel.buildHousing(1)
                            SectorInvestAction.POWER_PLANT -> viewModel.buildPowerPlant(1)
                            SectorInvestAction.MINE -> viewModel.buildMine(1)
                            SectorInvestAction.UNIVERSITY -> viewModel.buildUniversity()
                            SectorInvestAction.TANKS -> viewModel.purchaseMilitaryHardware(
                                com.presidentsimulator.game.data.MilitaryHardware.TANKS,
                                1,
                            )
                            null -> Unit
                        }
                        if (sector.investAction != null) audio.playBuildSuccess()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun GdpBreakdownCard(sectors: List<SectorModel>) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .background(NssGameCard)
            .padding(Dimens.ContentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("TOTAL GDP BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NssMutedForeground, letterSpacing = 1.sp)
            Text("🏆 ${sectors.size} Active Sectors", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NssAccent)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            sectors.forEachIndexed { index, sector ->
                val animShare by animateFloatAsState(
                    targetValue = if (started) sector.gdpShare.coerceAtLeast(0.5f) else 0f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing),
                    label = "gdpSeg$index",
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(animShare.coerceAtLeast(0.01f))
                        .background(NssSectorBarColors[index % NssSectorBarColors.size]),
                )
            }
        }
        sectors.forEachIndexed { index, sector ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(NssSectorBarColors[index % NssSectorBarColors.size]),
                )
                Text(
                    text = "${sector.name} ${"%.1f".format(sector.gdpShare)}%",
                    fontSize = 9.sp,
                    color = NssMutedForeground,
                    fontWeight = FontWeight.Medium,
                )
            }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
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
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = Dimens.SpacingSmall))
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall)) {
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
private fun TradeTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    var draftTariff by remember(state.trade.tariffRate) { mutableFloatStateOf(state.trade.tariffRate) }
    var selectedPartnerId by remember { mutableStateOf(state.diplomacy.rivals.firstOrNull()?.id) }
    var selectedCommodity by remember { mutableStateOf(TradeCommodity.OIL) }
    var selectedType by remember { mutableStateOf(TradeType.EXPORT) }

    val forecastRevenue = viewModel.forecastTariffRevenue(draftTariff)
    val forecastPenalty = viewModel.forecastTariffApprovalPenalty(draftTariff)
    val partner = state.diplomacy.rivals.find { it.id == selectedPartnerId }
    val dealPrice = partner?.let { viewModel.negotiatedDealPrice(it.id, selectedCommodity, selectedType) } ?: 0L

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("TARIFF POLICY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        Text(
            text = "${(draftTariff * 100f).roundToInt()}% · forecast ${formatMa2Money(forecastRevenue)}/mo · approval ${"%.1f".format(forecastPenalty)}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Slider(
            value = draftTariff,
            onValueChange = { draftTariff = it },
            onValueChangeFinished = { viewModel.setTariffRate(draftTariff) },
            valueRange = 0f..0.40f,
            colors = SliderDefaults.colors(thumbColor = NssPrimary, activeTrackColor = NssPrimary, inactiveTrackColor = NssBorder),
        )
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("SPOT MARKET", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        state.market.resources.forEach { quote ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(quote.commodity.displayName, fontWeight = FontWeight.Bold, color = NssForeground)
                    Text(formatMa2Money(quote.currentPrice) + "/u", fontSize = 11.sp, color = NssMutedForeground)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "BUY",
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(NssEmerald.copy(alpha = 0.2f))
                            .clickable {
                                viewModel.buyFromMarket(quote.commodity)
                                audio.playBuildSuccess()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = NssEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                    Text(
                        "SELL",
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(NssRed.copy(alpha = 0.15f))
                            .clickable {
                                viewModel.sellToMarket(quote.commodity)
                                audio.playBuildSuccess()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = NssRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("ACTIVE DEALS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        if (state.trade.activeDeals.isEmpty()) {
            Text("No active contracts.", fontSize = 12.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 6.dp))
        } else {
            state.trade.activeDeals.forEach { deal ->
                val name = state.diplomacy.rivalById(deal.partnerCountryId)?.name ?: deal.partnerCountryId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$name · ${deal.commodity.displayName}", fontWeight = FontWeight.Bold, color = NssForeground)
                        Text(
                            "${deal.type.name} ×${deal.amountPerTick} · ${formatMa2Money(deal.pricePerUnit)}/u",
                            fontSize = 11.sp,
                            color = NssMutedForeground,
                        )
                    }
                    Text(
                        "CANCEL",
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(NssBorder)
                            .clickable { viewModel.cancelTradeDeal(deal.dealId) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NssForeground,
                    )
                }
            }
        }
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("PROPOSE CONTRACT", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        Text("Partners", fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.diplomacy.rivals.take(4).forEach { rival ->
                FilterChip(
                    selected = selectedPartnerId == rival.id,
                    onClick = { selectedPartnerId = rival.id },
                    label = { Text(rival.name, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NssPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
        Text("Commodity", fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TradeCommodity.entries.forEach { commodity ->
                FilterChip(
                    selected = selectedCommodity == commodity,
                    onClick = { selectedCommodity = commodity },
                    label = { Text(commodity.displayName, fontSize = 10.sp) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
            TradeType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.name, fontSize = 10.sp) },
                )
            }
        }
        val canPropose = partner != null && TradeMarketViewModel.canProposeDeal(state, partner.id)
        Text(
            text = if (partner == null) "Select a partner" else "Unit price ${formatMa2Money(dealPrice)}",
            fontSize = 12.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "SUBMIT DEAL",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(NssCardShape)
                .background(if (canPropose) NssPrimary else NssBorder)
                .clickable(enabled = canPropose) {
                    partner?.let {
                        viewModel.proposeTradeDeal(it.id, selectedCommodity, 100L, selectedType)
                        audio.playBuildSuccess()
                    }
                }
                .padding(vertical = 12.dp),
            color = if (canPropose) Color.White else NssMutedForeground,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private fun buildSectors(state: GameState, gdp: Long): List<SectorModel> {
    val economy = state.economy
    val production = state.production
    val total = (
        economy.factories + economy.farms + economy.housing +
            production.powerPlants + production.mines +
            state.research.unlockedTechIds.size.coerceAtLeast(1)
        ).toFloat().coerceAtLeast(1f)

    fun share(count: Int): Float = (count / total * 100f).coerceIn(1f, 80f)

    return listOf(
        SectorModel(
            name = "Heavy Industry",
            gdpShare = share(economy.factories),
            employment = 18.7f,
            growth = 1.8f,
            level = economy.factories.coerceIn(1, 5),
            xp = (economy.factories * 8).coerceIn(10, 95),
            gradient = NssGradients.Sky,
            imageUrl = NssCardImages.INDUSTRY,
            investAction = SectorInvestAction.FACTORY,
            investLabel = "⬆ Build Factory",
        ),
        SectorModel(
            name = "Agriculture",
            gdpShare = share(economy.farms),
            employment = 6.1f,
            growth = if (production.foodShortage) -1.2f else 0.8f,
            level = economy.farms.coerceIn(1, 5),
            xp = (economy.farms * 7).coerceIn(10, 95),
            gradient = NssGradients.Amber,
            imageUrl = NssCardImages.AGRICULTURE,
            investAction = SectorInvestAction.FARM,
            investLabel = "⬆ Build Farm",
        ),
        SectorModel(
            name = "Housing",
            gdpShare = share(economy.housing),
            employment = 12.4f,
            growth = 1.1f,
            level = economy.housing.coerceIn(1, 5),
            xp = (economy.housing * 6).coerceIn(10, 95),
            gradient = NssGradients.Emerald,
            imageUrl = NssCardImages.SERVICES,
            investAction = SectorInvestAction.HOUSING,
            investLabel = "⬆ Build Housing",
        ),
        SectorModel(
            name = "Energy",
            gdpShare = share(production.powerPlants),
            employment = 3.4f,
            growth = if (production.energyShortage) -1.5f else 0.6f,
            level = production.powerPlants.coerceIn(1, 5),
            xp = (production.powerPlants * 9).coerceIn(10, 95),
            gradient = NssGradients.Orange,
            imageUrl = NssCardImages.ENERGY,
            investAction = SectorInvestAction.POWER_PLANT,
            investLabel = "⬆ Build Power Plant",
        ),
        SectorModel(
            name = "Mining",
            gdpShare = share(production.mines),
            employment = 8.2f,
            growth = 0.9f,
            level = production.mines.coerceIn(1, 5),
            xp = (production.mines * 8).coerceIn(10, 95),
            gradient = NssGradients.Indigo,
            imageUrl = NssCardImages.MANUFACTURING,
            investAction = SectorInvestAction.MINE,
            investLabel = "⬆ Build Mine",
        ),
        SectorModel(
            name = "Technology",
            gdpShare = (state.research.unlockedTechIds.size * 6f).coerceIn(4f, 25f),
            employment = 8.9f,
            growth = 6.7f,
            level = state.society.universities.coerceIn(1, 5),
            xp = (state.society.universities * 8).coerceIn(10, 95),
            gradient = NssGradients.Violet,
            imageUrl = NssCardImages.TECHNOLOGY,
            investAction = SectorInvestAction.UNIVERSITY,
            investLabel = "⬆ Build University",
        ),
        SectorModel(
            name = "Defense Ind.",
            gdpShare = (state.military.tanks.coerceAtMost(40) / 2f).coerceIn(3f, 18f),
            employment = 6.4f,
            growth = 2.1f,
            level = (state.military.tanks / 5).coerceIn(1, 5),
            xp = (state.military.tanks * 2).coerceIn(10, 95),
            gradient = NssGradients.Red,
            imageUrl = NssCardImages.DEFENSE_IND,
            investAction = SectorInvestAction.TANKS,
            investLabel = "⬆ Build Tanks",
        ),
    )
}

@Composable
private fun LedgerLine(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = NssForeground, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
        Text(value, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

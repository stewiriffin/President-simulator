package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssBranchHeader
import com.presidentsimulator.game.ui.components.NssCompactKpi
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssMinistryBanner
import com.presidentsimulator.game.ui.components.NssRecruitCard
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.NssUnitCard
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.theme.NssAccent
import androidx.compose.material3.MaterialTheme
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssSky
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private data class ForceUnit(
    val name: String,
    val count: Int,
    val strength: Int,
    val status: String,
    val imageUrl: String,
)

@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var selectedTab by remember { mutableStateOf("FORCES") }
    val tabs = listOf("FORCES", "RECRUITMENT", "LOGISTICS")
    val military = state.military
    val activeWar = state.diplomacy.activeWar

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        NssMinistryBanner(
            ministryLabel = "DEFENSE",
            imageUrl = NssCardImages.BANNER_DEFENSE,
            statPills = listOf(
                "Power: ${state.effectiveCombatStrength.roundToInt()} pts",
                "Personnel: ${military.personnel.toArmyString()}",
                "Readiness: ${military.morale.roundToInt()}%",
                "Budget: ${military.monthlyUpkeep.toBudgetString()}",
            ),
            gradientColors = NssGradients.Defense,
        )

        if (activeWar != null) {
            ActiveWarPanel(
                state = state,
                war = activeWar,
                armisticeCost = viewModel.armisticeCost(),
                onLaunchOffensive = viewModel::launchOffensive,
                onHoldDefensiveLine = viewModel::holdDefensiveLine,
                onProposeArmistice = viewModel::signArmistice,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                "FORCES" -> ForcesTab(state)
                "RECRUITMENT" -> RecruitmentTab(state, viewModel, audio)
                else -> LogisticsTab(state)
            }
        }
    }
}

@Composable
private fun ForcesTab(state: GameState) {
    val military = state.military
    val armyUnits = listOf(
        ForceUnit("Infantry Corps", military.personnel.toInt().coerceAtMost(999), military.morale.roundToInt(), "COMBAT READY", NssCardImages.INFANTRY),
        ForceUnit("Armored Brigade", military.tanks, 88, "COMBAT READY", NssCardImages.ARMORED),
        ForceUnit("Artillery Regiment", (military.tanks / 2).coerceAtLeast(1), 91, "TRAINING", NssCardImages.ARTILLERY),
        ForceUnit("Special Ops", (military.personnel / 50).coerceAtLeast(1).toInt(), 96, "ACTIVE OPS", NssCardImages.SPECIAL_OPS),
    )
    val navyUnits = listOf(
        ForceUnit("Destroyer", military.ships.coerceAtMost(20), 82, "PATROL", NssCardImages.DESTROYER),
        ForceUnit("Frigate", (military.ships * 1.5).roundToInt(), 79, "PATROL", NssCardImages.FRIGATE),
        ForceUnit("Submarine", military.ships.coerceAtMost(8), 95, "COMBAT READY", NssCardImages.SUBMARINE),
        ForceUnit("Carrier Group", military.ships.coerceAtMost(2), 90, "DEPLOYED", NssCardImages.CARRIER),
    )
    val airUnits = listOf(
        ForceUnit("Fighter Squadron", military.jets, 91, "COMBAT READY", NssCardImages.FIGHTER),
        ForceUnit("Bomber Wing", (military.jets / 3).coerceAtLeast(1), 76, "TRAINING", NssCardImages.BOMBER),
        ForceUnit("Drone Fleet", (military.jets / 2).coerceAtLeast(1), 98, "ACTIVE OPS", NssCardImages.DRONE),
    )

    BranchSection("ARMY", armyUnits.sumOf { it.count }, NssEmerald, Icons.Default.Security, NssGradients.Emerald, armyUnits)
    BranchSection("NAVY", navyUnits.sumOf { it.count }, NssSky, Icons.Default.Anchor, NssGradients.Sky, navyUnits)
    BranchSection("AIR", airUnits.sumOf { it.count }, NssViolet, Icons.Default.AirplanemodeActive, NssGradients.Violet, airUnits)
}

@Composable
private fun BranchSection(
    branch: String,
    unitCount: Int,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    units: List<ForceUnit>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NssBranchHeader(branch = branch, unitCount = unitCount, accentColor = accent, icon = icon)
        units.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { unit ->
                    NssUnitCard(
                        unitName = unit.name,
                        branch = branch,
                        count = unit.count,
                        strength = unit.strength,
                        status = unit.status,
                        maintLabel = "$${"%.1f".format((unit.count * 0.2).coerceAtLeast(0.1))}B/yr",
                        headerGradient = gradient,
                        accentColor = accent,
                        imageUrl = unit.imageUrl,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecruitmentTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    var personnelQty by remember { mutableIntStateOf(0) }
    var hardwareQtys by remember { mutableStateOf(MilitaryHardware.entries.associateWith { 0 }) }
    val batchSize = DiplomacyViewModel.RECRUIT_BATCH_SIZE
    val personnelCost = personnelQty * DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER * batchSize
    val hardwareCost = hardwareQtys.entries.sumOf { (hw, qty) -> hw.unitCost * qty }
    val totalCost = personnelCost + hardwareCost

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, NssPrimary.copy(alpha = 0.4f))
                .background(NssPrimary.copy(alpha = 0.08f))
                .padding(16.dp),
        ) {
            Text("TOTAL COMMISSION COST", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = com.presidentsimulator.game.ui.theme.NssMutedForeground)
            Text(formatMa2Money(totalCost), color = NssPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, com.presidentsimulator.game.ui.theme.NssBorder)
                .background(com.presidentsimulator.game.ui.theme.NssCard)
                .padding(16.dp),
        ) {
            Text("AVAILABLE TREASURY", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = com.presidentsimulator.game.ui.theme.NssMutedForeground)
            Text(formatMa2Money(state.vitals.budget), color = com.presidentsimulator.game.ui.theme.NssForeground, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    NssBranchHeader("ARMY", 1, NssEmerald, Icons.Default.Security)
    NssRecruitCard(
        name = "Infantry Division",
        branch = "ARMY",
        costLabel = formatMa2Money(DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER * batchSize),
        buildMonths = 6,
        maintLabel = "${formatMa2Money(state.military.monthlyUpkeep / 10)}/yr",
        quantity = personnelQty,
        headerGradient = NssGradients.Emerald,
        accentColor = NssEmerald,
        imageUrl = NssCardImages.INFANTRY,
        onQuantityChange = { delta -> personnelQty = (personnelQty + delta).coerceAtLeast(0) },
        modifier = Modifier.fillMaxWidth(),
    )

    NssBranchHeader("HARDWARE", MilitaryHardware.entries.size, NssSky, Icons.Default.Anchor)
    MilitaryHardware.entries.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { hardware ->
                val qty = hardwareQtys[hardware] ?: 0
                NssRecruitCard(
                    name = hardware.displayName,
                    branch = "HARDWARE",
                    costLabel = formatMa2Money(hardware.unitCost),
                    buildMonths = 12,
                    maintLabel = "Str ${hardware.unitStrength}",
                    quantity = qty,
                    headerGradient = NssGradients.Sky,
                    accentColor = NssSky,
                    imageUrl = hardwareImage(hardware),
                    onQuantityChange = { delta ->
                        hardwareQtys = hardwareQtys.toMutableMap().apply {
                            put(hardware, ((get(hardware) ?: 0) + delta).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }

    Text(
        text = if (totalCost > 0) "SUBMIT PROCUREMENT ORDER — ${formatMa2Money(totalCost)}" else "SUBMIT PROCUREMENT ORDER",
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (totalCost > 0) NssPrimary else com.presidentsimulator.game.ui.theme.NssBorder)
            .background(if (totalCost > 0) NssPrimary.copy(alpha = 0.15f) else com.presidentsimulator.game.ui.theme.NssCard)
            .clickable(enabled = totalCost > 0) {
                if (personnelQty > 0) {
                    viewModel.recruitPersonnel(personnelQty.toLong() * batchSize)
                }
                hardwareQtys.forEach { (hardware, qty) ->
                    if (qty > 0) viewModel.purchaseMilitaryHardware(hardware, qty)
                }
                audio.playBuildSuccess()
                personnelQty = 0
                hardwareQtys = MilitaryHardware.entries.associateWith { 0 }
            }
            .padding(vertical = 16.dp),
        color = if (totalCost > 0) NssPrimary else com.presidentsimulator.game.ui.theme.NssMutedForeground,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
    )
}

@Composable
private fun LogisticsTab(state: GameState) {
    val military = state.military
    val kpis = listOf(
        Triple("TOTAL MAINT COST", military.monthlyUpkeep.toBudgetString(), false),
        Triple("OVERALL READINESS", "${military.morale.roundToInt()}%", true),
        Triple("COMBAT STRENGTH", state.effectiveCombatStrength.roundToInt().toString(), true),
        Triple("PERSONNEL", military.personnel.toArmyString(), true),
        Triple("DEFCON", military.defcon.toString(), military.defcon >= 3),
        Triple("DEPLOYMENT", military.deployment.name, true),
    )

    kpis.chunked(3).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (label, value, good) ->
                NssCompactKpi(label, value, if (good) "Operational" else "Attention needed", good, Modifier.weight(1f))
            }
            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
        }
    }

    if (military.morale < 50f) {
        NssAlertBanner("MORALE BELOW OPERATIONAL THRESHOLD — INCREASE SALARY FUNDING")
    }
}

private fun hardwareImage(hardware: MilitaryHardware): String = when (hardware) {
    MilitaryHardware.TANKS -> NssCardImages.ARMORED
    MilitaryHardware.FIGHTER_JETS -> NssCardImages.FIGHTER
    MilitaryHardware.NAVAL_SHIPS -> NssCardImages.DESTROYER
    MilitaryHardware.NUCLEAR_ARSENAL -> NssCardImages.ARTILLERY
}

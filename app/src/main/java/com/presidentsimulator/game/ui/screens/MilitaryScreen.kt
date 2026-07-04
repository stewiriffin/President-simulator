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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.BulkBuildControls
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.WarningOrange
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

/**
 * Ministry of Defense: Personnel and Hardware tabs with bulk procurement.
 */
@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Personnel", "Hardware")
    val activeWar = state.diplomacy.activeWar

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Ministry of Defense",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        if (activeWar != null) {
            ActiveWarPanel(
                state = state,
                war = activeWar,
                armisticeCost = viewModel.armisticeCost(),
                onLaunchOffensive = viewModel::launchOffensive,
                onHoldDefensiveLine = viewModel::holdDefensiveLine,
                onProposeArmistice = viewModel::signArmistice,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> PersonnelTab(state = state, viewModel = viewModel, audio = audio)
            else -> HardwareTab(state = state, viewModel = viewModel, audio = audio)
        }
    }
}

@Composable
private fun PersonnelTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val military = state.military
    var draftFunding by remember(military.salaryFunding) {
        mutableFloatStateOf(military.salaryFunding)
    }
    var recruitAmount by remember { mutableIntStateOf(1) }

    val batchSize = DiplomacyViewModel.RECRUIT_BATCH_SIZE
    val maxRecruits = DiplomacyViewModel.maxRecruitable(state)
    val maxBatches = (maxRecruits / batchSize).toInt().coerceAtLeast(0)
    val selectedTroops = (recruitAmount.coerceAtMost(maxBatches.coerceAtLeast(0)) * batchSize)
        .coerceAtMost(maxRecruits.toLong())

    val projectedUpkeep = remember(draftFunding, military) {
        military.copy(salaryFunding = draftFunding).monthlyUpkeep
    }
    val projectedMorale = remember(draftFunding) {
        (35f + draftFunding.coerceIn(0.5f, 1.5f) * 45f).coerceIn(0f, 100f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Force Strength",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                MetricRow("Total troops", military.personnel.toArmyString())
                MetricRow("Monthly upkeep", military.monthlyUpkeep.toBudgetString())
                MetricRow("Army morale", "${military.morale.roundToInt()}%")
                MetricRow("Combat readiness", state.effectiveCombatStrength.roundToInt().toString())
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Military Funding / Salaries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Funding level: ${(draftFunding * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = draftFunding,
                    onValueChange = { draftFunding = it },
                    onValueChangeFinished = { viewModel.setSalaryFunding(draftFunding) },
                    valueRange = DiplomacyViewModel.MIN_SALARY_FUNDING..DiplomacyViewModel.MAX_SALARY_FUNDING,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Projected morale: ${projectedMorale.roundToInt()}% · " +
                        "Projected upkeep: ${projectedUpkeep.toBudgetString()}/mo",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (draftFunding >= 1f) ProfitGreen else WarningOrange,
                )
                Text(
                    text = "Higher salaries improve morale and combat effectiveness but raise payroll.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recruitment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Batch size: ${batchSize.toArmyString()} troops · " +
                        "Unit cost: ${DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER.toBudgetString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                )
                Spacer(modifier = Modifier.height(8.dp))
                BulkBuildControls(
                    assetName = "Train Soldiers",
                    currentCount = military.personnel.toInt().coerceAtMost(Int.MAX_VALUE),
                    unitCost = DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER * batchSize,
                    selectedAmount = recruitAmount,
                    maxAffordable = maxBatches,
                    onAmountSelected = { recruitAmount = it },
                    onBuild = { batches ->
                        val troops = batches.toLong() * batchSize
                        viewModel.recruitPersonnel(troops)
                        audio.playBuildSuccess()
                        recruitAmount = 1
                    },
                )
                if (selectedTroops > 0L) {
                    Text(
                        text = "Order will train ${selectedTroops.toArmyString()} personnel.",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeutralGray,
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val military = state.military
    var selectedAmounts by remember {
        mutableStateOf(MilitaryHardware.entries.associateWith { 1 })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Hardware Inventory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Purchase with [ 1x | 10x | Max ]. Weapons bans and nuclear embargoes may block items.",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
        }

        items(MilitaryHardware.entries, key = { it.name }) { hardware ->
            val inventory = when (hardware) {
                MilitaryHardware.TANKS -> military.tanks
                MilitaryHardware.FIGHTER_JETS -> military.jets
                MilitaryHardware.NAVAL_SHIPS -> military.ships
                MilitaryHardware.NUCLEAR_ARSENAL -> military.nuclearArsenal
            }
            val selected = selectedAmounts[hardware] ?: 1
            val maxAffordable = DiplomacyViewModel.maxAffordableHardware(state, hardware)
            val blockedReason = when {
                hardware == MilitaryHardware.NUCLEAR_ARSENAL &&
                    state.governance.nuclearEmbargoActive -> "Blocked by UN Nuclear Embargo"
                hardware != MilitaryHardware.NUCLEAR_ARSENAL &&
                    state.governance.weaponsBanActive -> "Blocked by UN Weapons Ban"
                else -> null
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = hardware.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Strength/unit: ${hardware.unitStrength}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeutralGray,
                        )
                    }
                    if (blockedReason != null) {
                        Text(
                            text = blockedReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningOrange,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    BulkBuildControls(
                        assetName = "Inventory",
                        currentCount = inventory,
                        unitCost = hardware.unitCost,
                        selectedAmount = selected,
                        maxAffordable = maxAffordable,
                        onAmountSelected = { amount ->
                            selectedAmounts = selectedAmounts.toMutableMap().apply {
                                put(hardware, amount)
                            }
                        },
                        onBuild = { amount ->
                            viewModel.purchaseMilitaryHardware(hardware, amount)
                            audio.playBuildSuccess()
                            selectedAmounts = selectedAmounts.toMutableMap().apply {
                                put(hardware, 1)
                            }
                        },
                        actionLabel = "Buy",
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

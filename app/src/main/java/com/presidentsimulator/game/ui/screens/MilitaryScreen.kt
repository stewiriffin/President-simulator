package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssBranchHeader
import com.presidentsimulator.game.ui.components.NssCompactKpi
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssRecruitCard
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.NssUnitCard
import com.presidentsimulator.game.ui.components.formatMa2Money
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
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
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "Defense",
            imageUrl = NssCardImages.BANNER_DEFENSE,
            statPills = listOf(
                "Power" to "${state.effectiveCombatStrength.roundToInt()}",
                "Personnel" to military.personnel.toArmyString(),
                "Readiness" to "${military.morale.roundToInt()}%",
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
                modifier = Modifier.padding(horizontal = Dimens.ContentPadding, vertical = Dimens.SpacingSmall),
            )
        }

        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall),
        ) {
            when (selectedTab) {
                "FORCES" -> ForcesTab(state)
                "RECRUITMENT" -> RecruitmentTab(state, viewModel, audio)
                else -> LogisticsTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun ForcesTab(state: GameState) {
    var branch by remember { mutableStateOf("ARMY") }
    val military = state.military
    val armyUnits = listOf(
        ForceUnit("Infantry Corps", military.personnel.toInt().coerceAtMost(999), military.morale.roundToInt(), "READY", NssCardImages.INFANTRY),
        ForceUnit("Armored Brigade", military.tanks, 88, "READY", NssCardImages.ARMORED),
        ForceUnit("Artillery Regiment", (military.tanks / 2).coerceAtLeast(1), 91, "TRAINING", NssCardImages.ARTILLERY),
        ForceUnit("Special Ops", (military.personnel / 50).coerceAtLeast(1).toInt(), 96, "READY", NssCardImages.SPECIAL_OPS),
    )
    val navyUnits = listOf(
        ForceUnit("Destroyer", military.ships.coerceAtMost(20), 82, "PATROL", NssCardImages.DESTROYER),
        ForceUnit("Frigate", (military.ships * 1.5).roundToInt(), 79, "PATROL", NssCardImages.FRIGATE),
        ForceUnit("Submarine", military.ships.coerceAtMost(8), 95, "READY", NssCardImages.SUBMARINE),
        ForceUnit("Carrier Group", military.ships.coerceAtMost(2), 90, "REFIT", NssCardImages.CARRIER),
    )
    val airUnits = listOf(
        ForceUnit("Fighter Squadron", military.jets, 91, "READY", NssCardImages.FIGHTER),
        ForceUnit("Bomber Wing", (military.jets / 3).coerceAtLeast(1), 76, "TRAINING", NssCardImages.BOMBER),
        ForceUnit("Drone Fleet", (military.jets / 2).coerceAtLeast(1), 98, "ACTIVE", NssCardImages.DRONE),
    )
    val branches = listOf(
        Triple("ARMY", armyUnits.sumOf { it.count }, NssEmerald to Icons.Default.Security),
        Triple("NAVY", navyUnits.sumOf { it.count }, NssSky to Icons.Default.Anchor),
        Triple("AIR", airUnits.sumOf { it.count }, NssViolet to Icons.Default.AirplanemodeActive),
    )
    val activeUnits = when (branch) {
        "NAVY" -> navyUnits
        "AIR" -> airUnits
        else -> armyUnits
    }
    val activeGradient = when (branch) {
        "NAVY" -> NssGradients.Sky
        "AIR" -> NssGradients.Violet
        else -> NssGradients.Emerald
    }
    val activeAccent = when (branch) {
        "NAVY" -> NssSky
        "AIR" -> NssViolet
        else -> NssEmerald
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
        branches.forEach { (name, count, colorIcon) ->
            val (accent, icon) = colorIcon
            val selected = branch == name
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardShape)
                    .background(if (selected) accent else com.presidentsimulator.game.ui.theme.NssGameCard)
                    .clickable { branch = name }
                    .padding(Dimens.ContentPadding - Dimens.SpacingXSmall),
            ) {
                Icon(icon, contentDescription = null, tint = if (selected) com.presidentsimulator.game.ui.theme.NssOnPhoto else com.presidentsimulator.game.ui.theme.NssMutedForeground, modifier = Modifier.size(20.dp))
                Text(name, fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (selected) com.presidentsimulator.game.ui.theme.NssOnPhoto else com.presidentsimulator.game.ui.theme.NssForeground, modifier = Modifier.padding(top = 6.dp))
                Text("$count units", fontSize = 10.sp, color = if (selected) com.presidentsimulator.game.ui.theme.NssOnPhoto.copy(alpha = 0.7f) else com.presidentsimulator.game.ui.theme.NssMutedForeground)
            }
        }
    }

    Text(
        text = "$branch UNITS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 3.sp,
        modifier = Modifier.padding(top = Dimens.SpacingSmall, bottom = Dimens.SpacingXSmall),
    )

    activeUnits.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
            row.forEach { unit ->
                NssUnitCard(
                    unitName = unit.name,
                    branch = branch,
                    count = unit.count,
                    strength = unit.strength,
                    status = unit.status,
                    maintLabel = "$${"%.1f".format((unit.count * 0.2).coerceAtLeast(0.1))}B/yr",
                    headerGradient = activeGradient,
                    accentColor = activeAccent,
                    imageUrl = unit.imageUrl,
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
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

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, NssPrimary.copy(alpha = 0.4f))
                .background(NssPrimary.copy(alpha = 0.08f))
                .padding(Dimens.ContentPadding),
        ) {
            Text("TOTAL COMMISSION COST", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = com.presidentsimulator.game.ui.theme.NssMutedForeground)
            Text(formatMa2Money(totalCost), color = NssPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, com.presidentsimulator.game.ui.theme.NssBorder)
                .background(com.presidentsimulator.game.ui.theme.NssCard)
                .padding(Dimens.ContentPadding),
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
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
            .padding(vertical = Dimens.ContentPadding),
        color = if (totalCost > 0) NssPrimary else com.presidentsimulator.game.ui.theme.NssMutedForeground,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
    )
}

@Composable
private fun LogisticsTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val military = state.military
    var draftSalary by remember(military.salaryFunding) { mutableFloatStateOf(military.salaryFunding) }
    val atWar = state.diplomacy.activeWar != null
    val kpis = listOf(
        Triple("TOTAL MAINT COST", military.monthlyUpkeep.toBudgetString(), false),
        Triple("OVERALL READINESS", "${military.morale.roundToInt()}%", true),
        Triple("COMBAT STRENGTH", state.effectiveCombatStrength.roundToInt().toString(), true),
        Triple("PERSONNEL", military.personnel.toArmyString(), true),
        Triple("DEFCON", military.defcon.toString(), military.defcon >= 3),
        Triple("DEPLOYMENT", military.deployment.name, true),
    )

    kpis.chunked(3).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
            row.forEach { (label, value, good) ->
                NssCompactKpi(label, value, if (good) "Operational" else "Attention needed", good, Modifier.weight(1f))
            }
            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
        }
    }

    Text(
        text = "DEPLOYMENT POSTURE",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = Dimens.SpacingSmall),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
        listOf(DeploymentStatus.DEFENSIVE, DeploymentStatus.MOBILIZED).forEach { status ->
            val selected = military.deployment == status
            val enabled = status != DeploymentStatus.DEFENSIVE || !atWar
            Text(
                text = status.name,
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardShape)
                    .background(if (selected) NssPrimary else com.presidentsimulator.game.ui.theme.NssGameCard)
                    .clickable(enabled = enabled) { viewModel.setDeployment(status) }
                    .padding(vertical = 12.dp),
                color = when {
                    !enabled -> com.presidentsimulator.game.ui.theme.NssMutedForeground
                    selected -> com.presidentsimulator.game.ui.theme.NssOnPhoto
                    else -> com.presidentsimulator.game.ui.theme.NssForeground
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
            )
        }
    }

    Text(
        text = "SALARY FUNDING  ${(draftSalary * 100f).roundToInt()}%",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = Dimens.SpacingSmall),
    )
    Slider(
        value = draftSalary,
        onValueChange = { draftSalary = it },
        onValueChangeFinished = { viewModel.setSalaryFunding(draftSalary) },
        valueRange = 0.5f..1.5f,
        colors = androidx.compose.material3.SliderDefaults.colors(
            thumbColor = NssPrimary,
            activeTrackColor = NssPrimary,
        ),
    )
    val forecastUpkeep = military.copy(salaryFunding = draftSalary).monthlyUpkeep
    val forecastMorale = military.copy(salaryFunding = draftSalary).morale
    Text(
        text = "Forecast · upkeep ${forecastUpkeep.toBudgetString()} · morale ${forecastMorale.roundToInt()}%",
        fontSize = 12.sp,
        color = com.presidentsimulator.game.ui.theme.NssMutedForeground,
        modifier = Modifier.padding(top = 4.dp),
    )

    Text(
        text = "DEFCON",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = Dimens.SpacingSmall),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
        (1..5).forEach { level ->
            val selected = military.defcon == level
            Text(
                text = level.toString(),
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardShape)
                    .background(if (selected) NssPrimary else com.presidentsimulator.game.ui.theme.NssGameCard)
                    .clickable { viewModel.setDefcon(level) }
                    .padding(vertical = 12.dp),
                color = if (selected) {
                    com.presidentsimulator.game.ui.theme.NssOnPhoto
                } else {
                    com.presidentsimulator.game.ui.theme.NssForeground
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
            )
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

package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.ui.components.ActiveOperationCard
import com.presidentsimulator.game.ui.components.graphics.CountryFlag
import com.presidentsimulator.game.ui.components.graphics.rivalIdToCountryCode
import com.presidentsimulator.game.ui.theme.DeficitRed
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.WarningOrange
import com.presidentsimulator.game.viewmodel.EspionageSecurityViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toRiskString
import kotlin.math.roundToInt

/**
 * Secret Service hub: Internal Security and Foreign Intelligence tabs.
 */
@Composable
fun SecurityScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Internal Security", "Foreign Intelligence")

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Secret Service",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

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
            0 -> InternalSecurityView(state = state, viewModel = viewModel)
            else -> ForeignIntelligenceView(state = state, viewModel = viewModel)
        }
    }
}

// ── Internal Security ────────────────────────────────────────────────────────

@Composable
private fun InternalSecurityView(
    state: GameState,
    viewModel: GameViewModel,
) {
    val security = state.internalSecurity
    var fundAmount by remember { mutableIntStateOf(1) }
    val maxFund = EspionageSecurityViewModel.maxAffordableSecurityUnits(state)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Vitals Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            RiskMeterCard(
                title = "Instability Score",
                value = security.instabilityScore,
                flashWhenCritical = false,
            )
        }

        item {
            RiskMeterCard(
                title = "Coup Risk",
                value = security.coupRisk,
                flashWhenCritical = true,
            )
        }

        if (security.coupRisk >= 75f || security.instabilityScore >= 75f) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Critical unrest — activate domestic operations or fund security immediately.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emergency Security Funding",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Each unit costs " +
                            "${EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST.toBudgetString()} " +
                            "and immediately lowers instability and coup risk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 5, 10).forEach { amount ->
                            FilterChip(
                                selected = fundAmount == amount,
                                onClick = { if (maxFund >= amount) fundAmount = amount },
                                enabled = maxFund >= amount,
                                label = { Text("${amount}x") },
                            )
                        }
                        FilterChip(
                            selected = fundAmount == maxFund && maxFund > 10,
                            onClick = { if (maxFund > 0) fundAmount = maxFund },
                            enabled = maxFund > 0,
                            label = { Text(if (maxFund > 0) "Max ($maxFund)" else "Max") },
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.fundInternalSecurity(fundAmount) },
                        enabled = fundAmount > 0 && maxFund >= fundAmount,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val cost = fundAmount * EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST
                        Text("Allocate ${cost.toBudgetString()}")
                    }
                }
            }
        }

        item {
            Text(
                text = "Domestic Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Monthly protocol upkeep: ${security.monthlyUpkeep.toBudgetString()}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
        }

        items(SecurityProtocol.entries, key = { it.name }) { protocol ->
            SecurityMeasureCard(
                protocol = protocol,
                isActive = security.isProtocolActive(protocol),
                onToggle = { viewModel.toggleSecurityProtocol(protocol) },
            )
        }
    }
}

/**
 * Risk meter: green → yellow → red. Coup risk flashes when critical.
 */
@Composable
private fun RiskMeterCard(
    title: String,
    value: Float,
    flashWhenCritical: Boolean,
) {
    val baseColor = when {
        value >= 75f -> DeficitRed
        value >= 40f -> WarningOrange
        else -> ProfitGreen
    }
    val label = when {
        value >= 75f -> "CRITICAL"
        value >= 40f -> "ELEVATED"
        else -> "STABLE"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coupFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "coupFlashAlpha",
    )
    val alpha = if (flashWhenCritical && value >= 75f) flashAlpha else 1f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(alpha),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$label · ${value.toRiskString()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = baseColor,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (value / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = baseColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun SecurityMeasureCard(
    protocol: SecurityProtocol,
    isActive: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = protocol.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (isActive) "ACTIVE" else "OFF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) ProfitGreen else NeutralGray,
                )
            }
            Text(
                text = protocol.description,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
            Text(
                text = "Upkeep: ${protocol.monthlyUpkeep.toBudgetString()}/mo · " +
                    "Instability: -${protocol.instabilityReduction.toRiskString()} · " +
                    "Approval: ${protocol.approvalPenalty.toRiskString()}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isActive) "Deactivate" else "Activate")
            }
        }
    }
}

// ── Foreign Intelligence ─────────────────────────────────────────────────────

@Composable
private fun ForeignIntelligenceView(
    state: GameState,
    viewModel: GameViewModel,
) {
    val espionage = state.espionage
    var expandedRivalId by remember { mutableStateOf<String?>(null) }
    val activeMissions = espionage.activeMissions.filter { it.status == MissionStatus.ACTIVE }
    val recentOutcomes = espionage.activeMissions.filter { it.status != MissionStatus.ACTIVE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spy Network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Available spies: ${espionage.availableSpies} / ${espionage.spyCount}")
                    Text("Intelligence budget: ${espionage.intelligencePoints} pts")
                    Text("Active operations: ${espionage.activeMissionCount}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::recruitSpy,
                        enabled = state.vitals.budget >= EspionageSecurityViewModel.SPY_RECRUIT_COST,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Recruit Spy (" +
                                "${EspionageSecurityViewModel.SPY_RECRUIT_COST.toBudgetString()})",
                        )
                    }
                }
            }
        }

        if (activeMissions.isNotEmpty()) {
            item {
                Text(
                    text = "Ongoing Missions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(activeMissions, key = { it.id }) { mission ->
                ActiveOperationCard(
                    state = state,
                    mission = mission,
                    onCancel = { viewModel.cancelCovertMission(mission.id) },
                )
            }
        }

        if (recentOutcomes.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Outcomes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(recentOutcomes.take(4), key = { "done-${it.id}" }) { mission ->
                ActiveOperationCard(
                    state = state,
                    mission = mission,
                    onCancel = { },
                )
            }
        }

        item {
            Text(
                text = "Target Selection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        items(state.diplomacy.rivals, key = { it.id }) { rival ->
            RivalIntelTargetCard(
                state = state,
                rival = rival,
                expanded = expandedRivalId == rival.id,
                onToggle = {
                    expandedRivalId = if (expandedRivalId == rival.id) null else rival.id
                },
                estimateSuccess = { missionType ->
                    viewModel.estimateMissionSuccess(rival.id, missionType)
                },
                canDeploy = { missionType ->
                    viewModel.canDeploySpy(rival.id, missionType)
                },
                onDeploy = { missionType ->
                    viewModel.deploySpy(rival.id, missionType)
                },
            )
        }
    }
}

@Composable
private fun RivalIntelTargetCard(
    state: GameState,
    rival: RivalNation,
    expanded: Boolean,
    onToggle: () -> Unit,
    estimateSuccess: (MissionType) -> Float,
    canDeploy: (MissionType) -> Boolean,
    onDeploy: (MissionType) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountryFlag(
                    countryCode = rivalIdToCountryCode(rival.id),
                    size = 36.dp,
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(
                        text = rival.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Security level: ${rival.militaryStrength.roundToInt()} · " +
                            "Relations: ${rival.relationshipScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray,
                    )
                }
                OutlinedButton(onClick = onToggle) {
                    Text(if (expanded) "Hide" else "Operations")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Covert Operations",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    MissionType.entries.forEach { missionType ->
                        CovertOperationRow(
                            missionType = missionType,
                            successChance = estimateSuccess(missionType),
                            enabled = canDeploy(missionType),
                            onDeploy = { onDeploy(missionType) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CovertOperationRow(
    missionType: MissionType,
    successChance: Float,
    enabled: Boolean,
    onDeploy: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = missionType.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = missionType.description,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
            Text(
                text = "Cost: ${missionType.budgetCost.toBudgetString()} · " +
                    "Intel: -${missionType.intelCost} · " +
                    "Duration: ${missionType.durationTicks} mo · " +
                    "Success: ${(successChance * 100f).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onDeploy,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (enabled) "Deploy Spy" else "Unavailable")
            }
        }
    }
}

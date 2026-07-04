package com.presidentsimulator.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.viewmodel.EspionageSecurityViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toRiskString
import kotlin.math.roundToInt

/**
 * Secret Service hub: Internal Security and Foreign Intelligence tabs.
 */
@Composable
fun EspionageSecurityScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Secret Service",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Internal Security") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Foreign Intelligence") },
            )
        }

        when (selectedTab) {
            0 -> InternalSecurityPanel(
                state = state,
                viewModel = viewModel,
            )
            else -> ForeignIntelligencePanel(
                state = state,
                viewModel = viewModel,
            )
        }
    }
}

// ── Internal Security ────────────────────────────────────────────────────────

@Composable
private fun InternalSecurityPanel(
    state: GameState,
    viewModel: GameViewModel,
) {
    val security = state.internalSecurity
    var fundAmount by remember { mutableIntStateOf(1) }
    val maxFund = EspionageSecurityViewModel.maxAffordableSecurityUnits(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RiskMeterCard(
            title = "Instability Score",
            value = security.instabilityScore,
            warningThreshold = 50f,
            criticalThreshold = 75f,
        )
        RiskMeterCard(
            title = "Coup Risk",
            value = security.coupRisk,
            warningThreshold = 40f,
            criticalThreshold = 75f,
        )

        if (security.instabilityScore >= 75f) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Critical unrest — coup risk is accelerating. Fund security or restore approval.",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Fund Internal Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Each unit costs " +
                        "${EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST.toBudgetString()} " +
                        "and lowers instability.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

        Text(
            text = "Security Protocols",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Monthly upkeep: ${security.monthlyUpkeep.toBudgetString()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SecurityProtocol.entries.forEach { protocol ->
            ProtocolToggleCard(
                protocol = protocol,
                isActive = security.isProtocolActive(protocol),
                onToggle = { viewModel.toggleSecurityProtocol(protocol) },
            )
        }
    }
}

@Composable
private fun RiskMeterCard(
    title: String,
    value: Float,
    warningThreshold: Float,
    criticalThreshold: Float,
) {
    val color = when {
        value >= criticalThreshold -> MaterialTheme.colorScheme.tertiary
        value >= warningThreshold -> Color(0xFFE9C46A)
        else -> MaterialTheme.colorScheme.secondary
    }
    val label = when {
        value >= criticalThreshold -> "CRITICAL"
        value >= warningThreshold -> "ELEVATED"
        else -> "STABLE"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    color = color,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (value / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = color,
            )
        }
    }
}

@Composable
private fun ProtocolToggleCard(
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
                    color = if (isActive) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = protocol.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Upkeep: ${protocol.monthlyUpkeep.toBudgetString()}/mo · " +
                    "Approval: ${protocol.approvalPenalty.toRiskString()} · " +
                    "Instability: -${protocol.instabilityReduction.toRiskString()}",
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
private fun ForeignIntelligencePanel(
    state: GameState,
    viewModel: GameViewModel,
) {
    val espionage = state.espionage
    var expandedRivalId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Agency Network",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Spies: ${espionage.spyCount} (${espionage.availableSpies} available)")
                Text("Intelligence points: ${espionage.intelligencePoints}")
                Text("Active missions: ${espionage.activeMissionCount}")
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

        val activeMissions = espionage.activeMissions.filter { it.status == MissionStatus.ACTIVE }
        val recentOutcomes = espionage.activeMissions.filter { it.status != MissionStatus.ACTIVE }

        if (activeMissions.isNotEmpty()) {
            Text(
                text = "Active Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            activeMissions.forEach { mission ->
                MissionProgressCard(state = state, mission = mission)
            }
        }

        if (recentOutcomes.isNotEmpty()) {
            Text(
                text = "Recent Outcomes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            recentOutcomes.take(5).forEach { mission ->
                MissionOutcomeCard(state = state, mission = mission)
            }
        }

        Text(
            text = "Rival Targets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        state.diplomacy.rivals.forEach { rival ->
            RivalIntelCard(
                state = state,
                rival = rival,
                expanded = expandedRivalId == rival.id,
                onToggleExpand = {
                    expandedRivalId = if (expandedRivalId == rival.id) null else rival.id
                },
                onDeploy = { missionType ->
                    viewModel.deploySpy(rival.id, missionType)
                },
                estimateSuccess = { missionType ->
                    viewModel.estimateMissionSuccess(rival.id, missionType)
                },
                canDeploy = { missionType ->
                    viewModel.canDeploySpy(rival.id, missionType)
                },
            )
        }
    }
}

@Composable
private fun RivalIntelCard(
    state: GameState,
    rival: RivalNation,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeploy: (MissionType) -> Unit,
    estimateSuccess: (MissionType) -> Float,
    canDeploy: (MissionType) -> Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${rival.flagEmoji} ${rival.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Security level: ${rival.militaryStrength.roundToInt()} · " +
                            "Relations: ${rival.relationshipScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onToggleExpand) {
                    Text(if (expanded) "Hide" else "Deploy Spy")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Select mission type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    MissionType.entries.forEach { missionType ->
                        MissionDeployRow(
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
private fun MissionDeployRow(
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Text(if (enabled) "Deploy" else "Unavailable")
            }
        }
    }
}

@Composable
private fun MissionProgressCard(state: GameState, mission: CovertMission) {
    val rivalName = state.diplomacy.rivalById(mission.targetCountryId)?.name
        ?: mission.targetCountryId
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${mission.missionType.displayName} → $rivalName",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Progress ${mission.progressTicks}/${mission.requiredTicks} · " +
                    "Success odds ${(mission.successProbability * 100f).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { mission.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
    }
}

@Composable
private fun MissionOutcomeCard(state: GameState, mission: CovertMission) {
    val rivalName = state.diplomacy.rivalById(mission.targetCountryId)?.name
        ?: mission.targetCountryId
    val success = mission.status == MissionStatus.SUCCESS
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (success) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (success) "SUCCESS" else "FAILED / COMPROMISED",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${mission.missionType.displayName} → $rivalName",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

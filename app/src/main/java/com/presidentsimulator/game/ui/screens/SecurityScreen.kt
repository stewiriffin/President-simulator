package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.ui.components.ActiveOperationCard
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.graphics.CountryFlag
import com.presidentsimulator.game.ui.components.graphics.rivalIdToCountryCode
import com.presidentsimulator.game.ui.theme.NssAccent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
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
    var selectedTab by remember { mutableStateOf("INTERNAL") }
    val tabs = listOf("INTERNAL", "FOREIGN")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "Intelligence",
            imageUrl = NssCardImages.BANNER_INTELLIGENCE,
            statPills = listOf(
                "Missions" to "${state.espionage.activeMissionCount}",
                "Risk" to "${state.internalSecurity.coupRisk.roundToInt()}%",
                "Budget" to state.internalSecurity.monthlyUpkeep.toBudgetString(),
            ),
            gradientColors = NssGradients.Violet,
        )

        NssTabBar(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        when (selectedTab) {
            "INTERNAL" -> InternalSecurityView(state = state, viewModel = viewModel)
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
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            top = Dimens.ContentPadding,
            bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "VITALS DASHBOARD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
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
                NssAlertBanner("Critical unrest — activate domestic operations or fund security immediately.")
            }
        }

        item {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("Emergency Security Funding", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                Text(
                    text = "Each unit costs ${EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST.toBudgetString()} and lowers instability and coup risk. Cap ${EspionageSecurityViewModel.MONTHLY_SECURITY_FUND_CAP}/mo (used ${security.securityFundsThisMonth}).",
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 4.dp),
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
                Text(
                    text = "Allocate ${(fundAmount * EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST).toBudgetString()}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NssCardShape)
                        .background(if (fundAmount > 0 && maxFund >= fundAmount) NssPrimary else NssPrimary.copy(alpha = 0.35f))
                        .clickable(enabled = fundAmount > 0 && maxFund >= fundAmount) { viewModel.fundInternalSecurity(fundAmount) }
                        .padding(vertical = 10.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        item {
            Text(
                text = "DOMESTIC OPERATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = "Protocol upkeep: ${security.monthlyUpkeep.toBudgetString()}/mo",
                fontSize = 11.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        items(SecurityProtocol.entries, key = { it.name }) { protocol ->
            val cooldown = EspionageSecurityViewModel.protocolCooldownRemaining(state, protocol)
            SecurityMeasureCard(
                protocol = protocol,
                isActive = security.isProtocolActive(protocol),
                cooldownMonths = cooldown,
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
    val barColor = when {
        value >= 75f -> NssRed
        value >= 40f -> NssAccent
        else -> NssEmerald
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

    NssPanel(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
            NssBadge(label = "$label · ${value.toRiskString()}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        NssGameBar(
            percent = (value / 100f).coerceIn(0f, 1f),
            color = barColor,
            thick = true,
        )
    }
}

@Composable
private fun SecurityMeasureCard(
    protocol: SecurityProtocol,
    isActive: Boolean,
    cooldownMonths: Int,
    onToggle: () -> Unit,
) {
    val canToggle = cooldownMonths <= 0
    NssPanel(modifier = Modifier.fillMaxWidth(), highlighted = isActive) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = protocol.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NssForeground,
                modifier = Modifier.weight(1f),
            )
            NssBadge(label = if (isActive) "ACTIVE" else "OFF")
        }
        Text(
            text = protocol.description,
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "Upkeep ${protocol.monthlyUpkeep.toBudgetString()}/mo · Instability -${protocol.instabilityReduction.toRiskString()} · Approval ${protocol.approvalPenalty.toRiskString()}",
            fontSize = 10.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                !canToggle -> "Cooldown ($cooldownMonths mo)"
                isActive -> "Deactivate Protocol"
                else -> "Activate Protocol"
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(
                    when {
                        !canToggle -> NssPrimary.copy(alpha = 0.35f)
                        isActive -> NssRed.copy(alpha = 0.85f)
                        else -> NssPrimary
                    },
                )
                .clickable(enabled = canToggle, onClick = onToggle)
                .padding(vertical = 10.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
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
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            top = Dimens.ContentPadding,
            bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("Spy Network", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                Text("Available spies: ${espionage.availableSpies} / ${espionage.spyCount}", fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
                Text("Intelligence budget: ${espionage.intelligencePoints} pts", fontSize = 11.sp, color = NssMutedForeground)
                Text("Active operations: ${espionage.activeMissionCount}", fontSize = 11.sp, color = NssMutedForeground)
                Text(
                    text = "Global exposure: ${espionage.exposureLevel.roundToInt()}%",
                    fontSize = 11.sp,
                    color = if (espionage.exposureLevel >= 55f) NssRed else NssMutedForeground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val canRecruit = state.vitals.budget >= EspionageSecurityViewModel.SPY_RECRUIT_COST
                Text(
                    text = "Recruit Spy (${EspionageSecurityViewModel.SPY_RECRUIT_COST.toBudgetString()})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NssCardShape)
                        .background(if (canRecruit) NssPrimary else NssPrimary.copy(alpha = 0.35f))
                        .clickable(enabled = canRecruit) { viewModel.recruitSpy() }
                        .padding(vertical = 10.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (activeMissions.isNotEmpty()) {
            item {
                Text(
                    text = "ONGOING MISSIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
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
                    text = "RECENT OUTCOMES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
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
                text = "TARGET SELECTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
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
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(72.dp).padding(bottom = 8.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.nationCardImage(rival.name.hashCode().mod(6).let { if (it < 0) -it else it }),
                fallbackGradient = NssGradients.Violet,
                modifier = Modifier.fillMaxSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NssForeground,
                )
                Text(
                    text = "Security ${rival.militaryStrength.roundToInt()} · Relations ${rival.relationshipScore}",
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                )
            }
            Text(
                text = if (expanded) "Hide" else "Ops",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NssPrimary.copy(alpha = 0.12f))
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = NssPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "COVERT OPERATIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
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

@Composable
private fun CovertOperationRow(
    missionType: MissionType,
    successChance: Float,
    enabled: Boolean,
    onDeploy: () -> Unit,
) {
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = missionType.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NssForeground,
        )
        Text(
            text = missionType.description,
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = "Cost ${missionType.budgetCost.toBudgetString()} · Intel -${missionType.intelCost} · ${missionType.durationTicks} mo · ${(successChance * 100f).roundToInt()}% success",
            fontSize = 10.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (enabled) "Deploy Spy" else "Unavailable",
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(if (enabled) NssPrimary else NssPrimary.copy(alpha = 0.35f))
                .clickable(enabled = enabled, onClick = onDeploy)
                .padding(vertical = 8.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
    }
}

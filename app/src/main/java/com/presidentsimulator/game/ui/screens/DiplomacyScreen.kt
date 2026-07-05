package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCard
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssMinistryBanner
import com.presidentsimulator.game.ui.components.NssNationCard
import com.presidentsimulator.game.ui.components.NssNationColors
import com.presidentsimulator.game.ui.components.NssProgressBar
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.prgColor
import com.presidentsimulator.game.ui.components.relationBarColor
import com.presidentsimulator.game.ui.components.relationTextColor
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.StarkWhite
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.GovernanceViewModel
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel

@Composable
fun DiplomacyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf("RELATIONS") }
    var selectedRivalId by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("RELATIONS", "TREATIES", "NEGOTIATIONS")
    val activeWar = state.diplomacy.activeWar
    val selectedRival = state.diplomacy.rivals.find { it.id == selectedRivalId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        NssMinistryBanner(
            ministryLabel = "FOREIGN AFFAIRS",
            statPills = listOf(
                "Allies: ${state.diplomacy.rivals.count { it.relationshipScore >= 70 }}",
                "Treaties: ${state.diplomacy.rivals.count { it.hasTradeTreaty || it.hasNonAggressionPact }}",
                "Threats: ${state.diplomacy.rivals.count { it.relationshipScore < 20 }}",
                "Influence: ${state.diplomacy.diplomaticInfluence}",
            ),
            gradientColors = NssGradients.Foreign,
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
                "RELATIONS" -> {
                    state.diplomacy.rivals.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { rival ->
                                NssNationCard(
                                    nationName = rival.name,
                                    flagEmoji = rivalFlagEmoji(rival),
                                    status = rivalStatus(rival),
                                    threat = rivalThreat(rival),
                                    relations = rival.relationshipScore.coerceIn(0, 100),
                                    tradeLabel = if (rival.hasTradeTreaty) "OPEN" else "STANDARD",
                                    militaryLabel = if (activeWar?.targetCountryId == rival.id) "ACTIVE CONFLICT" else rival.stance.label.uppercase(),
                                    headerColor = rivalHeaderColor(rival),
                                    isHostile = rival.relationshipScore < 20 || activeWar?.targetCountryId == rival.id,
                                    onAction = { selectedRivalId = if (selectedRivalId == rival.id) null else rival.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    selectedRival?.let { rival ->
                        RivalActionPanel(
                            state = state,
                            rival = rival,
                            warActive = activeWar != null,
                            isWarTarget = activeWar?.targetCountryId == rival.id,
                            onProposeTradeDeal = {
                                viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                            },
                            onFormAlliance = {
                                viewModel.formAlliance("Pact with ${rival.name}", listOf(rival.id))
                            },
                            onDeclareWar = { viewModel.declareWar(rival.id) },
                        )
                    }
                }

                "TREATIES" -> {
                    state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { rival ->
                                NssCard(modifier = Modifier.weight(1f)) {
                                    Text(rival.name, color = NssForeground, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (rival.hasTradeTreaty) "Free Trade Agreement" else "Non-Aggression Pact",
                                        color = NssMutedForeground,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        NssBadge(if (rival.hasTradeTreaty) "TRADE" else "DEFENSE")
                                        NssBadge("ACTIVE", large = true)
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                "NEGOTIATIONS" -> {
                    state.diplomacy.rivals.filter { it.relationshipScore in 20..80 }.forEach { rival ->
                        val progress = rival.relationshipScore
                        NssCard {
                            Text(
                                text = "${rival.name} — Diplomatic Channel",
                                color = NssForeground,
                                fontWeight = FontWeight.Bold,
                            )
                            Text("Relationship normalization talks", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text("PROGRESS", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
                                NssProgressBar(percent = progress.toFloat(), color = prgColor(progress), thick = true, modifier = Modifier.weight(1f))
                                Text("$progress%", color = relationTextColor(progress), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RivalActionPanel(
    state: GameState,
    rival: RivalNation,
    warActive: Boolean,
    isWarTarget: Boolean,
    onProposeTradeDeal: () -> Unit,
    onFormAlliance: () -> Unit,
    onDeclareWar: () -> Unit,
) {
    val canTrade = TradeMarketViewModel.canProposeDeal(state, rival.id)
    val canAlliance = rival.relationshipScore >= GovernanceViewModel.ALLIANCE_MIN_RELATION &&
        state.diplomacy.activeWar?.targetCountryId != rival.id &&
        state.governance.diplomaticInfluence >= GovernanceViewModel.ALLIANCE_INFLUENCE_COST
    val canWar = DiplomacyViewModel.canDeclareWar(state, rival.id)

    NssCard {
        Text("Actions — ${rival.name}", color = NssPrimary, fontWeight = FontWeight.Bold)
        AnimatedVisibility(visible = true) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (isWarTarget) {
                    Text("Active war in progress.", color = NssRed, fontWeight = FontWeight.SemiBold)
                } else {
                    OutlinedButton(onClick = onProposeTradeDeal, enabled = canTrade && !warActive, modifier = Modifier.fillMaxWidth()) {
                        Text("Propose Trade Deal")
                    }
                    OutlinedButton(onClick = onFormAlliance, enabled = canAlliance, modifier = Modifier.fillMaxWidth()) {
                        Text("Form Alliance")
                    }
                    Button(
                        onClick = onDeclareWar,
                        enabled = canWar,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NssRed, contentColor = StarkWhite),
                    ) {
                        Text(if (canWar) "Declare War" else "Declare War (blocked)")
                    }
                }
            }
        }
    }
}

private fun rivalFlagEmoji(rival: RivalNation): String = when {
    rival.relationshipScore >= 70 -> "🟦"
    rival.relationshipScore >= 40 -> "🟩"
    rival.relationshipScore >= 20 -> "⬜"
    else -> "🔴"
}

private fun rivalStatus(rival: RivalNation): String = when {
    rival.relationshipScore >= 80 -> "ALLY"
    rival.relationshipScore >= 60 -> "PARTNER"
    rival.relationshipScore >= 40 -> "NEUTRAL"
    rival.relationshipScore >= 20 -> "RIVAL"
    else -> "HOSTILE"
}

private fun rivalThreat(rival: RivalNation): String = when {
    rival.militaryStrength > 8000 -> "CRITICAL"
    rival.militaryStrength > 5000 -> "HIGH"
    rival.militaryStrength > 2500 -> "MEDIUM"
    else -> "LOW"
}

private fun rivalHeaderColor(rival: RivalNation): Color = when {
    rival.relationshipScore >= 80 -> NssNationColors.Ally
    rival.relationshipScore >= 60 -> NssNationColors.Partner
    rival.relationshipScore >= 40 -> NssNationColors.Neutral
    rival.relationshipScore >= 20 -> Color(0xFF451A03)
    else -> NssNationColors.Hostile
}

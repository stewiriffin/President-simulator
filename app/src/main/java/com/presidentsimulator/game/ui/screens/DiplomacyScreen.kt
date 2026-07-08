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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCard
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssNationCard
import com.presidentsimulator.game.ui.components.NssNationColors
import com.presidentsimulator.game.ui.components.NssProgressBar
import com.presidentsimulator.game.ui.components.NssStripPhotoCard
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.prgColor
import com.presidentsimulator.game.ui.components.relationBarColor
import com.presidentsimulator.game.ui.components.relationTextColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
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
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "Foreign Affairs",
            imageUrl = NssCardImages.BANNER_FOREIGN,
            statPills = listOf(
                "Allies" to "${state.diplomacy.rivals.count { it.relationshipScore >= 70 }}",
                "Treaties" to "${state.diplomacy.rivals.count { it.hasTradeTreaty || it.hasNonAggressionPact }}",
                "Crises" to "${state.diplomacy.rivals.count { it.relationshipScore < 20 }}",
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
                modifier = Modifier.padding(horizontal = Dimens.ContentPadding, vertical = Dimens.SpacingSmall),
            )
        }

        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                "RELATIONS" -> {
                    RelationsLegend()
                    state.diplomacy.rivals.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { colIndex, rival ->
                                val cardIndex = rowIndex * 2 + colIndex
                                NssNationCard(
                                    nationName = rival.name,
                                    flagEmoji = rivalFlagEmoji(rival),
                                    status = rivalStatus(rival),
                                    threat = rivalThreat(rival),
                                    relations = rival.relationshipScore.coerceIn(0, 100),
                                    tradeLabel = if (rival.hasTradeTreaty) "OPEN" else "STANDARD",
                                    militaryLabel = if (activeWar?.targetCountryId == rival.id) "ACTIVE CONFLICT" else rival.stance.label.uppercase(),
                                    headerColor = rivalHeaderColor(rival),
                                    imageUrl = NssCardImages.nationCardImage(cardIndex),
                                    headerGradient = NssGradients.Foreign,
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
                            onSendAid = { viewModel.sendForeignAid(rival.id) },
                            onStateVisit = { viewModel.conductStateVisit(rival.id) },
                            onNegotiateTradeTreaty = {
                                viewModel.negotiateTreaty(rival.id, TreatyType.TRADE)
                            },
                            onNegotiateNonAggression = {
                                viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION)
                            },
                            onFormAlliance = {
                                viewModel.formAlliance("Pact with ${rival.name}", listOf(rival.id))
                            },
                            onDeclareWar = { viewModel.declareWar(rival.id) },
                        )
                    }
                }

                "TREATIES" -> {
                    state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }.forEach { rival ->
                        NssStripPhotoCard(
                            imageUrl = NssCardImages.BANNER_FOREIGN,
                            fallbackGradient = NssGradients.Foreign,
                        ) {
                            Text(rival.name, color = NssForeground, fontWeight = FontWeight.Bold)
                            val treatyLabel = when {
                                rival.hasTradeTreaty && rival.hasNonAggressionPact -> "Trade + Non-Aggression"
                                rival.hasTradeTreaty -> "Free Trade Agreement"
                                else -> "Non-Aggression Pact"
                            }
                            Text(
                                treatyLabel,
                                color = NssMutedForeground,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (rival.hasTradeTreaty) NssBadge("TRADE")
                                if (rival.hasNonAggressionPact) NssBadge("NAP")
                                NssBadge("ACTIVE", large = true)
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 10.dp),
                            ) {
                                if (rival.hasTradeTreaty) {
                                    OutlinedButton(
                                        onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Break Trade")
                                    }
                                }
                                if (rival.hasNonAggressionPact) {
                                    OutlinedButton(
                                        onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Break NAP")
                                    }
                                }
                            }
                        }
                    }
                }

                "NEGOTIATIONS" -> {
                    state.diplomacy.rivals.forEach { rival ->
                        val progress = rival.relationshipScore
                        val warActive = activeWar != null
                        val canTradeDeal = TradeMarketViewModel.canProposeDeal(state, rival.id)
                        val canTreaty = !warActive && rival.relationshipScore >= 35
                        NssStripPhotoCard(
                            imageUrl = NssCardImages.BANNER_FOREIGN,
                            fallbackGradient = NssGradients.Foreign,
                        ) {
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 10.dp),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                                    },
                                    enabled = canTradeDeal && !warActive,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Propose Grain Export")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.sendForeignAid(rival.id) },
                                    enabled = !warActive && state.vitals.budget >= DiplomacyViewModel.FOREIGN_AID_COST,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Send Foreign Aid")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.conductStateVisit(rival.id) },
                                    enabled = !warActive &&
                                        state.vitals.budget >= DiplomacyViewModel.STATE_VISIT_BUDGET_COST &&
                                        state.diplomacy.diplomaticInfluence >= DiplomacyViewModel.STATE_VISIT_INFLUENCE_COST,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Conduct State Visit")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE) },
                                    enabled = canTreaty && !rival.hasTradeTreaty,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(if (rival.hasTradeTreaty) "Trade Treaty Active" else "Negotiate Trade")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION) },
                                    enabled = canTreaty && !rival.hasNonAggressionPact,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(if (rival.hasNonAggressionPact) "NAP Active" else "Negotiate Non-Aggression")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationsLegend() {
    val items = listOf(
        "ALLY" to Color(0xFF22C55E),
        "PARTNER" to Color(0xFF3B82F6),
        "NEUTRAL" to Color(0xFFA8A29E),
        "RIVAL" to Color(0xFFF97316),
        "HOSTILE" to Color(0xFFDC2626),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NssGameCard)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("RELATIONS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground, letterSpacing = 1.sp)
        items.forEach { (label, color) ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
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
    onSendAid: () -> Unit,
    onStateVisit: () -> Unit,
    onNegotiateTradeTreaty: () -> Unit,
    onNegotiateNonAggression: () -> Unit,
    onFormAlliance: () -> Unit,
    onDeclareWar: () -> Unit,
) {
    val canTrade = TradeMarketViewModel.canProposeDeal(state, rival.id)
    val canAlliance = rival.relationshipScore >= GovernanceViewModel.ALLIANCE_MIN_RELATION &&
        state.diplomacy.activeWar?.targetCountryId != rival.id &&
        state.governance.diplomaticInfluence >= GovernanceViewModel.ALLIANCE_INFLUENCE_COST
    val canWar = DiplomacyViewModel.canDeclareWar(state, rival.id)
    val canTreaty = !warActive && rival.relationshipScore >= 35
    val canAid = !warActive && state.vitals.budget >= DiplomacyViewModel.FOREIGN_AID_COST
    val canVisit = !warActive &&
        state.vitals.budget >= DiplomacyViewModel.STATE_VISIT_BUDGET_COST &&
        state.diplomacy.diplomaticInfluence >= DiplomacyViewModel.STATE_VISIT_INFLUENCE_COST

    NssCard {
        Text("Actions — ${rival.name}", color = NssPrimary, fontWeight = FontWeight.Bold)
        AnimatedVisibility(visible = true) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (isWarTarget) {
                    Text("Active war in progress.", color = NssRed, fontWeight = FontWeight.SemiBold)
                } else {
                    OutlinedButton(onClick = onProposeTradeDeal, enabled = canTrade && !warActive, modifier = Modifier.fillMaxWidth()) {
                        Text("Propose Grain Export")
                    }
                    OutlinedButton(onClick = onSendAid, enabled = canAid, modifier = Modifier.fillMaxWidth()) {
                        Text("Send Foreign Aid")
                    }
                    OutlinedButton(onClick = onStateVisit, enabled = canVisit, modifier = Modifier.fillMaxWidth()) {
                        Text("Conduct State Visit")
                    }
                    OutlinedButton(
                        onClick = onNegotiateTradeTreaty,
                        enabled = canTreaty && !rival.hasTradeTreaty,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (rival.hasTradeTreaty) "Trade Treaty Active" else "Negotiate Trade Treaty")
                    }
                    OutlinedButton(
                        onClick = onNegotiateNonAggression,
                        enabled = canTreaty && !rival.hasNonAggressionPact,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (rival.hasNonAggressionPact) "NAP Active" else "Negotiate Non-Aggression")
                    }
                    OutlinedButton(onClick = onFormAlliance, enabled = canAlliance, modifier = Modifier.fillMaxWidth()) {
                        Text("Form Alliance")
                    }
                    Button(
                        onClick = onDeclareWar,
                        enabled = canWar,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NssRed,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text(if (canWar) "Declare War" else "Declare War (blocked)")
                    }
                }
            }
        }
    }
}

private fun rivalFlagEmoji(rival: RivalNation): String = rival.flagEmoji.ifBlank {
    when {
        rival.relationshipScore >= 70 -> "🟦"
        rival.relationshipScore >= 40 -> "🟩"
        rival.relationshipScore >= 20 -> "⬜"
        else -> "🔴"
    }
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
    rival.relationshipScore >= 20 -> NssNationColors.Rival
    else -> NssNationColors.Hostile
}

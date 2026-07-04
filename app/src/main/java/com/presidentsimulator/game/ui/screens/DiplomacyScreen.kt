package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.graphics.CountryFlag
import com.presidentsimulator.game.ui.components.graphics.rivalIdToCountryCode
import com.presidentsimulator.game.ui.theme.DeficitRed
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.GovernanceViewModel
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel

/**
 * Foreign Affairs: rival list, relationship bars, and war/trade/alliance actions.
 */
@Composable
fun DiplomacyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var expandedRivalId by remember { mutableStateOf<String?>(null) }
    val activeWar = state.diplomacy.activeWar

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Foreign Affairs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Diplomatic influence: ${state.diplomacy.diplomaticInfluence}",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray,
            )
        }

        if (activeWar != null) {
            item {
                ActiveWarPanel(
                    state = state,
                    war = activeWar,
                    armisticeCost = viewModel.armisticeCost(),
                    onLaunchOffensive = viewModel::launchOffensive,
                    onHoldDefensiveLine = viewModel::holdDefensiveLine,
                    onProposeArmistice = viewModel::signArmistice,
                )
            }
        }

        item {
            Text(
                text = "Neighboring Powers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        items(state.diplomacy.rivals, key = { it.id }) { rival ->
            RivalDiplomacyCard(
                state = state,
                rival = rival,
                expanded = expandedRivalId == rival.id,
                isWarTarget = activeWar?.targetCountryId == rival.id,
                warActive = activeWar != null,
                onToggle = {
                    expandedRivalId = if (expandedRivalId == rival.id) null else rival.id
                },
                onProposeTradeDeal = {
                    viewModel.proposeTradeDeal(
                        partnerCountryId = rival.id,
                        commodity = TradeCommodity.GRAIN,
                        amount = 100L,
                        type = TradeType.EXPORT,
                    )
                },
                onFormAlliance = {
                    viewModel.formAlliance(
                        name = "Pact with ${rival.name}",
                        invitees = listOf(rival.id),
                    )
                },
                onDeclareWar = { viewModel.declareWar(rival.id) },
            )
        }
    }
}

@Composable
private fun RivalDiplomacyCard(
    state: GameState,
    rival: RivalNation,
    expanded: Boolean,
    isWarTarget: Boolean,
    warActive: Boolean,
    onToggle: () -> Unit,
    onProposeTradeDeal: () -> Unit,
    onFormAlliance: () -> Unit,
    onDeclareWar: () -> Unit,
) {
    val canTrade = TradeMarketViewModel.canProposeDeal(state, rival.id)
    val canAlliance = rival.relationshipScore >= GovernanceViewModel.ALLIANCE_MIN_RELATION &&
        state.diplomacy.activeWar?.targetCountryId != rival.id &&
        state.governance.diplomaticInfluence >= GovernanceViewModel.ALLIANCE_INFLUENCE_COST
    val canWar = DiplomacyViewModel.canDeclareWar(state, rival.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarTarget) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountryFlag(
                    countryCode = rivalIdToCountryCode(rival.id),
                    size = 42.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rival.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Military power: ${rival.militaryStrength.toInt()} · " +
                            "Stance: ${rival.stance.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray,
                    )
                }
                Text(
                    text = if (expanded) "Hide" else "Actions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            RelationshipBar(score = rival.relationshipScore)

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isWarTarget) {
                        Text(
                            text = "Active war — manage conflict in the War Room panel above.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DeficitRed,
                        )
                    } else {
                        OutlinedButton(
                            onClick = onProposeTradeDeal,
                            enabled = canTrade && !warActive,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Propose Trade Deal")
                        }
                        OutlinedButton(
                            onClick = onFormAlliance,
                            enabled = canAlliance,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Form Alliance")
                        }
                        Button(
                            onClick = onDeclareWar,
                            enabled = canWar,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeficitRed,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(
                                when {
                                    rival.hasNonAggressionPact -> "Declare War (blocked by NAP)"
                                    warActive -> "Declare War (already at war)"
                                    else -> "Declare War"
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Relationship meter from -100 (hostile/red) to +100 (friendly/green).
 */
@Composable
private fun RelationshipBar(score: Int) {
    val fraction = ((score + 100f) / 200f).coerceIn(0f, 1f)
    val tone = when {
        score >= 40 -> ProfitGreen
        score <= -40 -> DeficitRed
        else -> NeutralGray
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Relations",
                style = MaterialTheme.typography.labelMedium,
                color = NeutralGray,
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = tone,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(DeficitRed, NeutralGray, ProfitGreen),
                    ),
                ),
        ) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = Color.Transparent,
                trackColor = Color.Transparent,
            )
        }
        // Marker line using a secondary progress overlay for the current score position.
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(top = 2.dp),
            color = tone,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

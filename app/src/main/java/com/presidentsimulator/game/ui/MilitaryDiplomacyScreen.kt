package com.presidentsimulator.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.DiplomaticStance
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.data.WarState
import com.presidentsimulator.game.ui.theme.DeficitRed
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.WarningOrange
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.progressFraction
import com.presidentsimulator.game.viewmodel.progressLabel
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toCasualtyString
import kotlin.math.roundToInt

/**
 * Foreign Affairs dashboard and War Room panels.
 * Bound to [GameViewModel] actions that apply immutable geopolitics transforms.
 */
@Composable
fun ForeignAffairsScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val activeWar = state.diplomacy.activeWar

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ministry of Foreign Affairs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Diplomatic influence: ${state.diplomacy.diplomaticInfluence}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (activeWar != null) {
            WarRoomPanel(
                state = state,
                war = activeWar,
                armisticeCost = viewModel.armisticeCost(),
                onSignArmistice = viewModel::signArmistice,
            )
        }

        Text(
            text = "Neighboring Powers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        state.diplomacy.rivals.forEach { rival ->
            RivalNationCard(
                rival = rival,
                state = state,
                isWarTarget = activeWar?.targetCountryId == rival.id,
                warActive = activeWar != null,
                onSignTrade = {
                    viewModel.negotiateTreaty(rival.id, TreatyType.TRADE)
                },
                onSignNonAggression = {
                    viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION)
                },
                onDeclareWar = { viewModel.declareWar(rival.id) },
            )
        }
    }
}

/**
 * Defense ministry: force composition, posture controls, and procurement.
 */
@Composable
fun DefenseMinistryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val military = state.military
    val activeWar = state.diplomacy.activeWar

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ministry of Defense",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        if (activeWar != null) {
            WarRoomPanel(
                state = state,
                war = activeWar,
                armisticeCost = viewModel.armisticeCost(),
                onSignArmistice = viewModel::signArmistice,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Force Readiness",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReadinessRow("Personnel", military.personnel.toArmyString())
                ReadinessRow("Tanks", military.tanks.toString())
                ReadinessRow("Jets", military.jets.toString())
                ReadinessRow("DEFCON", military.defcon.toString())
                ReadinessRow(
                    "Combat strength",
                    state.effectiveCombatStrength.roundToInt().toString(),
                )
                ReadinessRow("Monthly upkeep", military.monthlyUpkeep.toBudgetString())
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Deployment Posture",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = military.deployment == DeploymentStatus.DEFENSIVE,
                        onClick = { viewModel.setDeployment(DeploymentStatus.DEFENSIVE) },
                        enabled = activeWar == null,
                        label = { Text("Defensive") },
                    )
                    FilterChip(
                        selected = military.deployment == DeploymentStatus.MOBILIZED,
                        onClick = { viewModel.setDeployment(DeploymentStatus.MOBILIZED) },
                        label = { Text("Mobilized") },
                    )
                }
                Text(
                    text = if (activeWar != null) {
                        "Forces are locked in wartime mobilization."
                    } else {
                        "Mobilization raises combat power but alarms neighbors and increases upkeep."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Procurement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ProcurementButton(
                    label = "Recruit 10K personnel",
                    cost = 10_000L * DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER,
                    enabled = state.vitals.budget >= 10_000L * DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER,
                    onClick = { viewModel.recruitPersonnel(10_000L) },
                )
                ProcurementButton(
                    label = "Buy 50 tanks",
                    cost = 50L * DiplomacyViewModel.TANK_UNIT_COST,
                    enabled = state.vitals.budget >= 50L * DiplomacyViewModel.TANK_UNIT_COST,
                    onClick = { viewModel.purchaseTanks(50) },
                )
                ProcurementButton(
                    label = "Buy 10 jets",
                    cost = 10L * DiplomacyViewModel.JET_UNIT_COST,
                    enabled = state.vitals.budget >= 10L * DiplomacyViewModel.JET_UNIT_COST,
                    onClick = { viewModel.purchaseJets(10) },
                )
            }
        }
    }
}

// ── War Room ─────────────────────────────────────────────────────────────────

@Composable
private fun WarRoomPanel(
    state: GameState,
    war: WarState,
    armisticeCost: Long,
    onSignArmistice: () -> Unit,
) {
    val rival = state.diplomacy.rivalById(war.targetCountryId)
    val canAffordPeace = state.vitals.budget >= armisticeCost

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WAR ROOM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Conflict with ${rival?.name ?: war.targetCountryId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Month ${war.monthsActive} of hostilities",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "War progress: ${war.progressLabel()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Defeat ←————————→ Victory",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { war.progressFraction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReadinessRow(
                label = "Our casualties",
                value = war.playerCasualties.toCasualtyString(),
                onErrorContainer = true,
            )
            ReadinessRow(
                label = "Enemy casualties",
                value = war.enemyCasualties.toCasualtyString(),
                onErrorContainer = true,
            )
            ReadinessRow(
                label = "Military readiness",
                value = state.effectiveCombatStrength.roundToInt().toString(),
                onErrorContainer = true,
            )
            ReadinessRow(
                label = "Deployment",
                value = state.military.deployment.name,
                onErrorContainer = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSignArmistice,
                enabled = canAffordPeace,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("Sign Armistice / Peace Treaty (${armisticeCost.toBudgetString()})")
            }
        }
    }
}

// ── Rival cards ──────────────────────────────────────────────────────────────

@Composable
private fun RivalNationCard(
    rival: RivalNation,
    state: GameState,
    isWarTarget: Boolean,
    warActive: Boolean,
    onSignTrade: () -> Unit,
    onSignNonAggression: () -> Unit,
    onDeclareWar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarTarget) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = rival.flagEmoji, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rival.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Military power: ${rival.militaryStrength.roundToInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StanceBadge(stance = rival.stance)
            }

            Spacer(modifier = Modifier.height(8.dp))

            RelationshipBar(score = rival.relationshipScore)

            Text(
                text = buildTreatySummary(rival),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            if (isWarTarget) {
                Text(
                    text = "Active war — manage conflict in the War Room.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    text = "Diplomatic Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                DiplomaticActionButton(
                    label = "Sign Trade Treaty",
                    detail = costDetail(TreatyType.TRADE),
                    enabled = !rival.hasTradeTreaty &&
                        !warActive &&
                        DiplomacyViewModel.treatyAffordable(state, TreatyType.TRADE),
                    onClick = onSignTrade,
                )
                Spacer(modifier = Modifier.height(6.dp))
                DiplomaticActionButton(
                    label = "Offer Non-Aggression Pact",
                    detail = costDetail(TreatyType.NON_AGGRESSION),
                    enabled = !rival.hasNonAggressionPact &&
                        !warActive &&
                        DiplomacyViewModel.treatyAffordable(state, TreatyType.NON_AGGRESSION),
                    onClick = onSignNonAggression,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onDeclareWar,
                    enabled = DiplomacyViewModel.canDeclareWar(state, rival.id),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                ) {
                    Text(
                        text = if (rival.hasNonAggressionPact) {
                            "Declare War (blocked by NAP)"
                        } else if (warActive) {
                            "Declare War (already at war)"
                        } else {
                            "Declare War"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StanceBadge(stance: DiplomaticStance) {
    val color = when (stance) {
        DiplomaticStance.FRIENDLY -> ProfitGreen
        DiplomaticStance.NEUTRAL -> WarningOrange
        DiplomaticStance.HOSTILE -> DeficitRed
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stance.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun RelationshipBar(score: Int) {
    val fraction = ((score + 100f) / 200f).coerceIn(0f, 1f)
    Column {
        Text(
            text = "Relations: $score",
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
    }
}

@Composable
private fun DiplomaticActionButton(
    label: String,
    detail: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label)
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun ProcurementButton(
    label: String,
    cost: Long,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("$label — ${cost.toBudgetString()}")
    }
}

@Composable
private fun ReadinessRow(
    label: String,
    value: String,
    onErrorContainer: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onErrorContainer) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (onErrorContainer) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

private fun buildTreatySummary(rival: RivalNation): String {
    val treaties = buildList {
        if (rival.hasTradeTreaty) add("Trade Treaty")
        if (rival.hasNonAggressionPact) add("Non-Aggression Pact")
    }
    return if (treaties.isEmpty()) {
        "No active treaties"
    } else {
        "Treaties: ${treaties.joinToString(", ")}"
    }
}

private fun costDetail(type: TreatyType): String =
    "Cost: ${type.budgetCost.toBudgetString()} | Influence: -${type.influenceCost}"

package com.presidentsimulator.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.presidentsimulator.game.data.Alliance
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.PLAYER_COUNTRY_ID
import com.presidentsimulator.game.data.ResolutionType
import com.presidentsimulator.game.data.UNResolution
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.GovernanceViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

/**
 * Global Summit terminal: UN Assembly voting and coalition management.
 */
@Composable
fun GovernanceUNScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateAlliance by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Global Summit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            Text(
                text = "UN Diplomatic Influence: ${state.governance.diplomaticInfluence}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("The Assembly") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Coalitions") },
                )
            }

            when (selectedTab) {
                0 -> AssemblyPanel(state = state, viewModel = viewModel)
                else -> CoalitionsPanel(state = state, viewModel = viewModel)
            }
        }

        if (selectedTab == 1) {
            FloatingActionButton(
                onClick = { showCreateAlliance = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New Coalition",
                )
            }
        }
    }

    if (showCreateAlliance) {
        CreateAllianceDialog(
            state = state,
            onDismiss = { showCreateAlliance = false },
            onConfirm = { name, invitees ->
                viewModel.formAlliance(name, invitees)
                showCreateAlliance = false
            },
        )
    }
}

// ── Assembly ─────────────────────────────────────────────────────────────────

@Composable
private fun AssemblyPanel(
    state: GameState,
    viewModel: GameViewModel,
) {
    val resolution = state.governance.activeResolution

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.governance.lastResolutionResult.isNotBlank()) {
            Text(
                text = state.governance.lastResolutionResult,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        ActiveModifiersCard(state = state)

        if (resolution == null) {
            Text(
                text = "Propose a Resolution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ResolutionType.entries.forEach { type ->
                ProposalCard(
                    state = state,
                    type = type,
                    onPropose = { target ->
                        viewModel.proposeResolution(type, target)
                    },
                )
            }
        } else {
            LiveVoteTracker(resolution = resolution)
            Text(
                text = "Undecided Nations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val undecided = GovernanceViewModel.undecidedNations(state)
            if (undecided.isEmpty()) {
                Text(
                    text = "All nations have voted. Awaiting session close.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                undecided.forEach { countryId ->
                    BribeNationRow(
                        state = state,
                        countryId = countryId,
                        canAfford = state.vitals.budget >= GovernanceViewModel.BRIBE_COST,
                        onBribeFor = { viewModel.bribeCountryVote(countryId, true) },
                        onBribeAgainst = { viewModel.bribeCountryVote(countryId, false) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveModifiersCard(state: GameState) {
    val active = buildList {
        if (state.governance.nuclearEmbargoActive) add("Nuclear Embargo")
        if (state.governance.globalTaxActive) add("Global Tax")
        if (state.governance.peacekeepingActive) add("Peacekeeping")
        if (state.governance.weaponsBanActive) add("Weapons Ban")
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Active Global Mandates",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (active.isEmpty()) {
                    "None currently in force"
                } else {
                    active.joinToString(" · ")
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ProposalCard(
    state: GameState,
    type: ResolutionType,
    onPropose: (String?) -> Unit,
) {
    var selectedTarget by remember { mutableStateOf(state.diplomacy.rivals.firstOrNull()?.id) }
    val canPropose = GovernanceViewModel.canPropose(state, type)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = type.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Influence cost: ${type.influenceCost} · Voting window: ${type.votingDurationTicks} months",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (type.requiresTarget) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Target nation", style = MaterialTheme.typography.labelLarge)
                state.diplomacy.rivals.forEach { rival ->
                    OutlinedButton(
                        onClick = { selectedTarget = rival.id },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = if (selectedTarget == rival.id) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                    ) {
                        Text("${rival.flagEmoji} ${rival.name}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onPropose(if (type.requiresTarget) selectedTarget else null)
                },
                enabled = canPropose && (!type.requiresTarget || selectedTarget != null),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (canPropose) {
                        "Submit to Assembly"
                    } else if (state.governance.activeResolution != null) {
                        "Floor occupied"
                    } else {
                        "Insufficient influence"
                    },
                )
            }
        }
    }
}

@Composable
private fun LiveVoteTracker(resolution: UNResolution) {
    val forCount = resolution.votesForCount
    val againstCount = resolution.votesAgainstCount
    val total = (forCount + againstCount).coerceAtLeast(1)
    val forFraction = forCount.toFloat() / total.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "LIVE VOTE — ${resolution.type.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Countdown: ${resolution.votingTimeRemaining} month(s) remaining",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "For: $forCount",
                    color = Color(0xFF2A9D8F),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Against: $againstCount",
                    color = Color(0xFFE76F51),
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { forFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color(0xFF2A9D8F),
                trackColor = Color(0xFFE76F51),
            )
            Text(
                text = "Support share: ${(forFraction * 100f).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun BribeNationRow(
    state: GameState,
    countryId: String,
    canAfford: Boolean,
    onBribeFor: () -> Unit,
    onBribeAgainst: () -> Unit,
) {
    val name = GovernanceViewModel.countryDisplayName(state, countryId)
    val rival = state.diplomacy.rivalById(countryId)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${rival?.flagEmoji.orEmpty()} $name",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Relations: ${rival?.relationshipScore ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onBribeFor,
                    enabled = canAfford,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A9D8F),
                    ),
                ) {
                    Text("Bribe For")
                }
                Button(
                    onClick = onBribeAgainst,
                    enabled = canAfford,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE76F51),
                    ),
                ) {
                    Text("Bribe Against")
                }
            }
            Text(
                text = "Cost: ${GovernanceViewModel.BRIBE_COST.toBudgetString()}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ── Coalitions ───────────────────────────────────────────────────────────────

@Composable
private fun CoalitionsPanel(
    state: GameState,
    viewModel: GameViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Active Coalitions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (state.governance.activeAlliances.isEmpty()) {
            Text(
                text = "No coalitions formed. Use the + button to invite friendly nations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.governance.activeAlliances.forEach { alliance ->
                AllianceCard(
                    state = state,
                    alliance = alliance,
                    onDissolve = {
                        if (alliance.leaderCountryId == PLAYER_COUNTRY_ID) {
                            viewModel.dissolveAlliance(alliance.allianceId)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AllianceCard(
    state: GameState,
    alliance: Alliance,
    onDissolve: () -> Unit,
) {
    val power = GovernanceViewModel.allianceMilitaryPower(state, alliance)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = alliance.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Leader: ${GovernanceViewModel.countryDisplayName(state, alliance.leaderCountryId)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Shared DEFCON: ${alliance.sharedDefconLevel} · " +
                    "Combined power: ${power.roundToInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Members",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            alliance.memberCountryIds.forEach { memberId ->
                val rival = state.diplomacy.rivalById(memberId)
                val label = if (memberId == PLAYER_COUNTRY_ID) {
                    "🏛 Your Nation"
                } else {
                    "${rival?.flagEmoji.orEmpty()} ${rival?.name ?: memberId}"
                }
                Text(text = "• $label", style = MaterialTheme.typography.bodyMedium)
            }
            if (alliance.leaderCountryId == PLAYER_COUNTRY_ID) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDissolve,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Dissolve Coalition")
                }
            }
        }
    }
}

@Composable
private fun CreateAllianceDialog(
    state: GameState,
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }

    val eligible = state.diplomacy.rivals.filter { rival ->
        rival.relationshipScore >= GovernanceViewModel.ALLIANCE_MIN_RELATION &&
            state.diplomacy.activeWar?.targetCountryId != rival.id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Coalition") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Coalition name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Invite friendly nations (relations ≥ ${GovernanceViewModel.ALLIANCE_MIN_RELATION}). " +
                        "Influence cost: ${GovernanceViewModel.ALLIANCE_INFLUENCE_COST}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (eligible.isEmpty()) {
                    Text(
                        text = "No eligible partners. Improve relations first.",
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                } else {
                    eligible.forEach { rival ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rival.id in selected,
                                onCheckedChange = { checked ->
                                    selected = if (checked) {
                                        selected + rival.id
                                    } else {
                                        selected - rival.id
                                    }
                                },
                            )
                            Text("${rival.flagEmoji} ${rival.name} (${rival.relationshipScore})")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selected.toList()) },
                enabled = selected.isNotEmpty() &&
                    state.governance.diplomaticInfluence >= GovernanceViewModel.ALLIANCE_INFLUENCE_COST,
            ) {
                Text("Found Coalition")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

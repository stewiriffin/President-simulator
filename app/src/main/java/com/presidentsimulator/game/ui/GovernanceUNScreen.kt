package com.presidentsimulator.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.presidentsimulator.game.data.Alliance
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.PLAYER_COUNTRY_ID
import com.presidentsimulator.game.data.ResolutionType
import com.presidentsimulator.game.data.UNResolution
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
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
    var selectedTab by remember { mutableStateOf("ASSEMBLY") }
    var showCreateAlliance by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        NssScreenHeader(
            title = "United Nations",
            imageUrl = NssCardImages.BANNER_FOREIGN,
            statPills = listOf(
                "Influence" to "${state.governance.diplomaticInfluence}",
                "Alliances" to "${state.governance.activeAlliances.size}",
                "Resolution" to if (state.governance.activeResolution != null) "ACTIVE" else "NONE",
            ),
            gradientColors = NssGradients.Sky,
        )

        NssTabBar(
            tabs = listOf("ASSEMBLY", "COALITIONS"),
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        when (selectedTab) {
            "ASSEMBLY" -> AssemblyPanel(state = state, viewModel = viewModel)
            else -> CoalitionsPanel(
                state = state,
                viewModel = viewModel,
                onCreateAlliance = { showCreateAlliance = true },
            )
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
            NssPanel(modifier = Modifier.fillMaxWidth(), highlighted = true) {
                Text(
                    text = state.governance.lastResolutionResult,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NssForeground,
                )
            }
        }

        ActiveModifiersCard(state = state)

        if (resolution == null) {
            Text(
                text = "PROPOSE A RESOLUTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
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
                text = "UNDECIDED NATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            val undecided = GovernanceViewModel.undecidedNations(state)
            if (undecided.isEmpty()) {
                Text(
                    text = "All nations have voted. Awaiting session close.",
                    fontSize = 12.sp,
                    color = NssMutedForeground,
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
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("Active Global Mandates", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
        Text(
            text = if (active.isEmpty()) "None currently in force" else active.joinToString(" · "),
            fontSize = 12.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
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

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text(type.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
        Text(type.description, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
        Text(
            text = "Influence ${type.influenceCost} · Window ${type.votingDurationTicks} months",
            fontSize = 10.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (type.requiresTarget) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Target nation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NssPrimary)
            state.diplomacy.rivals.forEach { rival ->
                val selected = selectedTarget == rival.id
                Text(
                    text = "${rival.flagEmoji} ${rival.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) NssPrimary.copy(alpha = 0.15f) else NssMutedForeground.copy(alpha = 0.08f))
                        .clickable { selectedTarget = rival.id }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    color = if (selected) NssPrimary else NssForeground,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val enabled = canPropose && (!type.requiresTarget || selectedTarget != null)
        Text(
            text = when {
                canPropose -> "Submit to Assembly"
                state.governance.activeResolution != null -> "Floor occupied"
                else -> "Insufficient influence"
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(if (enabled) NssPrimary else NssPrimary.copy(alpha = 0.35f))
                .clickable(enabled = enabled) {
                    onPropose(if (type.requiresTarget) selectedTarget else null)
                }
                .padding(vertical = 10.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LiveVoteTracker(resolution: UNResolution) {
    val forCount = resolution.votesForCount
    val againstCount = resolution.votesAgainstCount
    val total = (forCount + againstCount).coerceAtLeast(1)
    val forFraction = forCount.toFloat() / total.toFloat()

    NssPanel(modifier = Modifier.fillMaxWidth(), highlighted = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "LIVE VOTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            NssBadge(label = resolution.type.displayName)
        }
        Text(
            text = "${resolution.votingTimeRemaining} month(s) remaining",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "For: $forCount", color = NssEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = "Against: $againstCount", color = NssRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        NssGameBar(percent = forFraction, color = NssEmerald, thick = true)
        Text(
            text = "Support share: ${(forFraction * 100f).roundToInt()}%",
            fontSize = 10.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
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

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${rival?.flagEmoji.orEmpty()} $name",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NssForeground,
        )
        Text(
            text = "Relations: ${rival?.relationshipScore ?: 0} · Cost ${GovernanceViewModel.BRIBE_COST.toBudgetString()}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Bribe For",
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardShape)
                    .background(if (canAfford) NssEmerald else NssEmerald.copy(alpha = 0.35f))
                    .clickable(enabled = canAfford, onClick = onBribeFor)
                    .padding(vertical = 10.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Bribe Against",
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardShape)
                    .background(if (canAfford) NssRed else NssRed.copy(alpha = 0.35f))
                    .clickable(enabled = canAfford, onClick = onBribeAgainst)
                    .padding(vertical = 10.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Coalitions ───────────────────────────────────────────────────────────────

@Composable
private fun CoalitionsPanel(
    state: GameState,
    viewModel: GameViewModel,
    onCreateAlliance: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "ACTIVE COALITIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = "+ New Coalition",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NssPrimary)
                    .clickable(onClick = onCreateAlliance)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }

        if (state.governance.activeAlliances.isEmpty()) {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No coalitions formed. Invite friendly nations to build a bloc.",
                    fontSize = 12.sp,
                    color = NssMutedForeground,
                )
            }
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

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text(alliance.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NssForeground)
        Text(
            text = "Leader: ${GovernanceViewModel.countryDisplayName(state, alliance.leaderCountryId)}",
            fontSize = 12.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "DEFCON ${alliance.sharedDefconLevel} · Combined power ${power.roundToInt()}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text("Members", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NssPrimary)
        alliance.memberCountryIds.forEach { memberId ->
            val rival = state.diplomacy.rivalById(memberId)
            val label = if (memberId == PLAYER_COUNTRY_ID) {
                "🏛 Your Nation"
            } else {
                "${rival?.flagEmoji.orEmpty()} ${rival?.name ?: memberId}"
            }
            Text(text = "• $label", fontSize = 12.sp, color = NssForeground)
        }
        if (alliance.leaderCountryId == PLAYER_COUNTRY_ID) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dissolve Coalition",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(NssRed.copy(alpha = 0.85f))
                    .clickable(onClick = onDissolve)
                    .padding(vertical = 10.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
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
    val canConfirm = selected.isNotEmpty() &&
        state.governance.diplomaticInfluence >= GovernanceViewModel.ALLIANCE_INFLUENCE_COST

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(20.dp),
        ) {
            Text("CREATE COALITION", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NssPrimary, letterSpacing = 2.sp)
            Text("New Coalition", fontWeight = FontWeight.Black, fontSize = 18.sp, color = NssForeground, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Coalition name") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Invite nations with relations ≥ ${GovernanceViewModel.ALLIANCE_MIN_RELATION}. Influence cost: ${GovernanceViewModel.ALLIANCE_INFLUENCE_COST}",
                fontSize = 11.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (eligible.isEmpty()) {
                Text("No eligible partners. Improve relations first.", fontSize = 12.sp, color = NssRed)
            } else {
                eligible.forEach { rival ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rival.id in selected,
                            onCheckedChange = { checked ->
                                selected = if (checked) selected + rival.id else selected - rival.id
                            },
                        )
                        Text("${rival.flagEmoji} ${rival.name} (${rival.relationshipScore})", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Text(
                    text = "Found Coalition",
                    modifier = Modifier
                        .weight(1f)
                        .clip(NssCardShape)
                        .background(if (canConfirm) NssPrimary else NssPrimary.copy(alpha = 0.35f))
                        .clickable(enabled = canConfirm) { onConfirm(name, selected.toList()) }
                        .padding(vertical = 10.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

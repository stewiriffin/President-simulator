package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.presidentsimulator.game.ui.components.NssConfirmDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.presidentsimulator.game.data.Ideology
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.data.SocietyMinistry
import com.presidentsimulator.game.data.StateReligion
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssAccent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.ProductionLawViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private data class PolicyTab(val label: String, val category: LawCategory?)

private val policyTabs = listOf(
    PolicyTab("CONSTITUTION", LawCategory.MILITARY),
    PolicyTab("ECONOMY", LawCategory.ECONOMIC),
    PolicyTab("SOCIAL", LawCategory.SOCIAL),
    PolicyTab("SOCIETY", null),
)

@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("CONSTITUTION") }
    var pendingToggle by remember { mutableStateOf<LawToggleRequest?>(null) }
    val selected = policyTabs.first { it.label == selectedTab }
    val laws = remember(selected.category) {
        selected.category?.let { LawCatalog.byCategory(it) }.orEmpty()
    }

    Column(modifier = modifier.fillMaxSize().background(NssBackground).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        NssScreenHeader(
            title = "Domestic Policy",
            imageUrl = NssCardImages.BANNER_DOMESTIC,
            statPills = listOf(
                "Active" to "${state.legal.activeLawIds.size}",
                "Upkeep" to state.legal.totalUpkeep.toBudgetString(),
                "Election" to state.nextElectionYear.toString(),
            ),
            gradientColors = NssGradients.Indigo,
        )

        NssTabBar(
            tabs = policyTabs.map { it.label },
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        if (selectedTab == "SOCIETY") {
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
                item { IdeologyPanel(state = state, viewModel = viewModel) }
                item { SocietyMinistriesPanel(state = state, viewModel = viewModel) }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.ContentPadding,
                    end = Dimens.ContentPadding,
                    top = Dimens.ContentPadding,
                    bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { IdeologyPanel(state = state, viewModel = viewModel) }
                if (state.legal.pendingLaws.isNotEmpty()) {
                    item { PendingLawsPanel(state = state, viewModel = viewModel) }
                }
                items(laws, key = { it.id }) { law ->
                    val isActive = state.legal.isActive(law.id)
                    val pending = state.legal.pendingLaws.find { it.lawId == law.id }
                    PolicyLawRow(
                        law = law,
                        category = selected.category ?: LawCategory.SOCIAL,
                        isActive = isActive,
                        pendingLabel = pending?.let {
                            if (it.enabling) "PENDING ENACT · ${it.ticksRemaining} mo"
                            else "PENDING REPEAL · ${it.ticksRemaining} mo"
                        },
                        effectSummary = buildLawEffectSummary(law),
                        enabled = pending == null && (isActive || viewModel.canEnactLaw(law.id)),
                        onToggle = { enabled -> pendingToggle = LawToggleRequest(law = law, enabling = enabled) },
                    )
                }
            }
        }
    }

    pendingToggle?.let { request ->
        LawToggleConfirmationDialog(
            request = request,
            onConfirm = {
                if (request.enabling) viewModel.enactLaw(request.law.id) else viewModel.repealLaw(request.law.id)
                pendingToggle = null
            },
            onDismiss = { pendingToggle = null },
        )
    }
}

@Composable
private fun IdeologyPanel(
    state: com.presidentsimulator.game.data.GameState,
    viewModel: GameViewModel,
) {
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("IDEOLOGY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        Text(
            text = "Shift cost ${ProductionLawViewModel.IDEOLOGY_SHIFT_COST.toBudgetString()}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Ideology.entries.forEach { ideology ->
                FilterChip(
                    selected = state.legal.ideology == ideology,
                    onClick = { viewModel.setIdeology(ideology) },
                    label = { Text(ideology.displayName, fontSize = 10.sp) },
                )
            }
        }
    }
}

@Composable
private fun PendingLawsPanel(
    state: com.presidentsimulator.game.data.GameState,
    viewModel: GameViewModel,
) {
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("PARLIAMENT QUEUE", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        Text(
            text = "Bills resolve over months unless rushed (${ProductionLawViewModel.RUSH_LAW_COST.toBudgetString()}).",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        state.legal.pendingLaws.forEach { pending ->
            val lawName = LawCatalog.byId(pending.lawId)?.name ?: pending.lawId
            val verb = if (pending.enabling) "Enact" else "Repeal"
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(
                    "$verb $lawName · ${pending.ticksRemaining} mo left",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = NssForeground,
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Rush",
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(NssAccent)
                            .clickable { viewModel.rushPendingLaw(pending.lawId) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = NssOnPhoto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(NssMutedForeground.copy(alpha = 0.35f))
                            .clickable { viewModel.cancelPendingLaw(pending.lawId) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = NssForeground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SocietyMinistriesPanel(
    state: com.presidentsimulator.game.data.GameState,
    viewModel: GameViewModel,
) {
    val society = state.society
    var health by remember(society.healthFunding) { mutableFloatStateOf(society.healthFunding) }
    var education by remember(society.educationFunding) { mutableFloatStateOf(society.educationFunding) }
    var culture by remember(society.cultureFunding) { mutableFloatStateOf(society.cultureFunding) }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("MINISTRY FUNDING", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        FundingSlider("Health", health, society.healthLevel) {
            health = it
            viewModel.adjustMinistryFunding(SocietyMinistry.HEALTH, it)
        }
        FundingSlider("Education", education, society.educationLevel) {
            education = it
            viewModel.adjustMinistryFunding(SocietyMinistry.EDUCATION, it)
        }
        FundingSlider("Culture", culture, society.cultureScore) {
            culture = it
            viewModel.adjustMinistryFunding(SocietyMinistry.CULTURE, it)
        }
        Text(
            text = "Monthly social upkeep ${society.totalMinistryUpkeep.toBudgetString()}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("STATE RELIGION", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StateReligion.entries.forEach { religion ->
                FilterChip(
                    selected = society.stateReligion == religion,
                    onClick = { viewModel.changeStateReligion(religion) },
                    label = { Text(religion.displayName, fontSize = 10.sp) },
                )
            }
        }
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("UNIVERSITIES", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
        Text(
            text = "${society.universities} campuses · next build ${AdvancementViewModel.UNIVERSITY_COST.toBudgetString()}",
            fontSize = 12.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
        )
        Text(
            text = "BUILD UNIVERSITY",
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(NssPrimary)
                .clickable { viewModel.buildUniversity() }
                .padding(vertical = 12.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FundingSlider(
    label: String,
    value: Float,
    level: Float,
    onCommit: (Float) -> Unit,
) {
    var draft by remember(value) { mutableFloatStateOf(value) }
    Text(
        text = "$label  ${(draft * 100f).roundToInt()}% · Level ${level.roundToInt()}",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NssForeground,
        modifier = Modifier.padding(top = 10.dp),
    )
    Slider(
        value = draft,
        onValueChange = { draft = it },
        onValueChangeFinished = { onCommit(draft) },
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(thumbColor = NssPrimary, activeTrackColor = NssPrimary, inactiveTrackColor = NssBorder),
    )
}

private data class LawToggleRequest(val law: Law, val enabling: Boolean)

@Composable
private fun PolicyLawRow(
    law: Law,
    category: LawCategory,
    isActive: Boolean,
    pendingLabel: String?,
    effectSummary: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .then(
                if (isActive) Modifier
                    .background(NssGameCard)
                    .border(2.dp, NssAccent.copy(alpha = 0.4f), NssCardShape)
                else Modifier.background(NssGameCard),
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.lawCategoryImage(category),
                fallbackGradient = NssGradients.Indigo,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ContentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(law.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                    if (isActive) NssBadge(label = "ACTIVE")
                    if (pendingLabel != null) NssBadge(label = "QUEUED")
                }
                if (pendingLabel != null) {
                    Text(pendingLabel, fontSize = 11.sp, color = NssAccent, modifier = Modifier.padding(top = 4.dp))
                }
                Text(effectSummary, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
                Text("Upkeep ${law.upkeepCost.toBudgetString()}/mo", fontSize = 10.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 2.dp))
            }
            Switch(
                checked = isActive,
                onCheckedChange = { checked -> if (checked != isActive) onToggle(checked) },
                enabled = enabled || isActive,
            )
        }
    }
}

@Composable
private fun LawToggleConfirmationDialog(
    request: LawToggleRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val law = request.law
    val title = if (request.enabling) "Enact ${law.name}?" else "Repeal ${law.name}?"
    val body = if (request.enabling) {
        buildString {
            append("Activation cost: ${law.activationCost.toBudgetString()}\n")
            append("Monthly upkeep: ${law.upkeepCost.toBudgetString()}\n")
            append("Parliament approval required: ${law.approvalThreshold.roundToInt()}%\n\n")
            append("If national approval is below that threshold, the bill enters a 3-month parliament queue.\n\n")
            append("Ongoing effects: ${buildLawEffectSummary(law)}")
        }
    } else {
        "If approval is below the law's threshold, repeal is delayed 3 months. Otherwise it resolves immediately. No refund for prior activation costs."
    }
    NssConfirmDialog(
        title = title,
        body = body,
        confirmLabel = if (request.enabling) "Enact" else "Repeal",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

private fun buildLawEffectSummary(law: Law): String = buildList {
    if (law.approvalModifier != 0f) add("Approval ${if (law.approvalModifier > 0) "+" else ""}${law.approvalModifier.roundToInt()}")
    if (law.productionModifier != 1f) add("Production ×${"%.2f".format(law.productionModifier)}")
    if (law.foodDemandModifier != 1f) add("Food demand ×${"%.2f".format(law.foodDemandModifier)}")
    if (law.energyDemandModifier != 1f) add("Energy demand ×${"%.2f".format(law.energyDemandModifier)}")
    if (law.militaryRecruitModifier != 1f) add("Recruit ×${"%.2f".format(law.militaryRecruitModifier)}")
}.joinToString(" · ").ifBlank { law.description }

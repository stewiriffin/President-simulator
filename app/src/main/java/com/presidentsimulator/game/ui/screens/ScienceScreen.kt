package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.Technology
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.theme.NssGameCard
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
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun ScienceScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val research = state.research
    val sciencePerMonth = viewModel.projectedSciencePerTick()
    val activeTech = research.activeTechnology
    val queuedTech = research.queuedTechnology

    Column(modifier = modifier.fillMaxSize().background(NssBackground).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        NssScreenHeader(
            title = "Science",
            imageUrl = NssCardImages.BANNER_SCIENCE,
            statPills = listOf(
                "Unlocked" to "${research.unlockedTechIds.size}",
                "Reserve" to "${research.sciencePoints} pts",
                "Rate" to "+$sciencePerMonth/mo",
            ),
            gradientColors = NssGradients.Violet,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                top = Dimens.ContentPadding,
                bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionTitle("Current Research")
                CurrentResearchPanel(
                    activeTech = activeTech,
                    queuedTech = queuedTech,
                    progressPercent = research.progressPercent(),
                    daysRemaining = research.daysRemaining(sciencePerMonth),
                    extraFundingTier = research.extraFundingTier,
                    canAllocateFunding = viewModel.canAllocateExtraResearchFunding(),
                    fundingCostLabel = AdvancementViewModel.EXTRA_RESEARCH_FUNDING_COST.formatScienceBudget(),
                    onAllocateFunding = viewModel::allocateExtraResearchFunding,
                )
            }
            item { SectionTitle("Tech Tree") }
            items(TechCatalog.all, key = { it.id }) { tech ->
                TechTreeRow(
                    tech = tech,
                    isUnlocked = research.isUnlocked(tech.id),
                    isActive = research.activeTechId == tech.id,
                    isQueued = research.queuedTechId == tech.id,
                    prerequisitesMet = research.prerequisitesMet(tech),
                    canStart = viewModel.canStartResearch(tech.id),
                    canUnlock = viewModel.canUnlockTechnology(tech.id),
                    hasActiveResearch = research.activeTechId != null,
                    onStartResearch = { viewModel.startResearch(tech.id) },
                    onUnlock = { viewModel.unlockTechnology(tech.id) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 3.sp,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun CurrentResearchPanel(
    activeTech: Technology?,
    queuedTech: Technology?,
    progressPercent: Float,
    daysRemaining: Int,
    extraFundingTier: Int,
    canAllocateFunding: Boolean,
    fundingCostLabel: String,
    onAllocateFunding: () -> Unit,
) {
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        if (activeTech != null) {
            Box(modifier = Modifier.fillMaxWidth().height(88.dp).padding(bottom = 8.dp)) {
                NssPhotoHeader(
                    imageUrl = NssCardImages.techCategoryImage(activeTech.category),
                    fallbackGradient = NssGradients.Violet,
                    modifier = Modifier.matchParentSize(),
                    scrimTopToBottom = CardHeaderBottomScrim,
                )
            }
        }
        if (activeTech == null) {
            Text("No active research project", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NssForeground)
            if (queuedTech != null) {
                Text(
                    "Queued next: ${queuedTech.name}",
                    fontSize = 12.sp,
                    color = NssEmerald,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text("Select a technology below to begin.", fontSize = 12.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
            }
        } else {
            Text(activeTech.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = NssForeground)
            Text(activeTech.effect.description, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Research XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
                Text("${progressPercent.toInt()}%", fontWeight = FontWeight.Black, color = NssPrimary)
            }
            NssGameBar(percent = progressPercent, color = NssPrimary, thick = true)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Est. remaining", fontSize = 11.sp, color = NssMutedForeground)
                Text(if (daysRemaining == 0) "< 1 day" else "$daysRemaining days", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(
                text = "Extra funding tier: $extraFundingTier / ${com.presidentsimulator.game.data.ResearchState.MAX_EXTRA_FUNDING_TIER}",
                fontSize = 10.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "⬆ Allocate Extra Funding ($fundingCostLabel)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(NssCardShape)
                    .background(if (canAllocateFunding) NssAccent else NssAccent.copy(alpha = 0.35f))
                    .clickable(enabled = canAllocateFunding, onClick = onAllocateFunding)
                    .padding(vertical = 10.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
            if (queuedTech != null) {
                Text(
                    "Up next: ${queuedTech.name}",
                    fontSize = 11.sp,
                    color = NssEmerald,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun TechTreeRow(
    tech: Technology,
    isUnlocked: Boolean,
    isActive: Boolean,
    isQueued: Boolean,
    prerequisitesMet: Boolean,
    canStart: Boolean,
    canUnlock: Boolean,
    hasActiveResearch: Boolean,
    onStartResearch: () -> Unit,
    onUnlock: () -> Unit,
) {
    val status = when {
        isUnlocked -> "UNLOCKED"
        isActive -> "IN PROGRESS"
        isQueued -> "QUEUED"
        !prerequisitesMet -> "LOCKED"
        else -> "AVAILABLE"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .then(if (isActive) Modifier.background(NssGameCard).border(2.dp, NssAccent.copy(alpha = 0.4f), NssCardShape) else Modifier.background(NssGameCard)),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(72.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.techCategoryImage(tech.category),
                fallbackGradient = NssGradients.Violet,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
        Column(modifier = Modifier.padding(Dimens.ContentPadding)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                    Text(tech.category.displayName, fontSize = 10.sp, color = NssMutedForeground)
                }
                NssBadge(label = status)
            }
            Text(tech.effect.description, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 6.dp))
            Text("Cost: ${tech.scienceCost} science pts", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NssForeground, modifier = Modifier.padding(top = 4.dp))
            if (tech.prerequisiteIds.isNotEmpty() && !prerequisitesMet) {
                Text(
                    text = "Requires: " + tech.prerequisiteIds.joinToString { id -> TechCatalog.byId(id)?.name ?: id },
                    fontSize = 10.sp,
                    color = NssAccent,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (!isUnlocked && !isActive && !isQueued) {
                Text(
                    text = when {
                        !canStart -> "Cannot start"
                        hasActiveResearch -> "⏳ Queue Research"
                        else -> "▶ Start Research"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(NssCardShape)
                        .background(if (canStart) NssEmerald else NssMutedForeground.copy(alpha = 0.3f))
                        .clickable(enabled = canStart, onClick = onStartResearch)
                        .padding(vertical = 8.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (canUnlock) "⚡ Unlock Instantly (${tech.scienceCost} pts)" else "Instant unlock unavailable",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(NssCardShape)
                        .background(if (canUnlock) NssAccent else NssMutedForeground.copy(alpha = 0.3f))
                        .clickable(enabled = canUnlock, onClick = onUnlock)
                        .padding(vertical = 8.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun Long.formatScienceBudget(): String = when {
    this >= 1_000_000_000L -> "$${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000L -> "$${"%.1f".format(this / 1_000_000.0)}M"
    else -> "$$this"
}

package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.Technology
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel

/**
 * Ministry of Science — active research queue and unlockable technology tree.
 */
@Composable
fun ScienceScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val research = state.research
    val sciencePerMonth = viewModel.projectedSciencePerTick()
    val activeTech = research.activeTechnology

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MinistryHeader(
                title = "Ministry of Science",
                subtitle = "${research.unlockedTechIds.size} technologies unlocked · " +
                    "${research.sciencePoints} science points in reserve",
            )
        }

        item {
            SectionLabel("Current Research")
            CurrentResearchPanel(
                activeTech = activeTech,
                progressPercent = research.progressPercent(),
                daysRemaining = research.daysRemaining(sciencePerMonth),
                extraFundingTier = research.extraFundingTier,
                canAllocateFunding = viewModel.canAllocateExtraResearchFunding(),
                fundingCostLabel = AdvancementViewModel.EXTRA_RESEARCH_FUNDING_COST.formatScienceBudget(),
                onAllocateFunding = viewModel::allocateExtraResearchFunding,
            )
        }

        item {
            SectionLabel("Tech Tree")
        }

        items(TechCatalog.all, key = { it.id }) { tech ->
            TechTreeRow(
                tech = tech,
                isUnlocked = research.isUnlocked(tech.id),
                isActive = research.activeTechId == tech.id,
                prerequisitesMet = research.prerequisitesMet(tech),
                canStart = viewModel.canStartResearch(tech.id),
                onStartResearch = { viewModel.startResearch(tech.id) },
            )
        }
    }
}

@Composable
private fun MinistryHeader(
    title: String,
    subtitle: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun CurrentResearchPanel(
    activeTech: Technology?,
    progressPercent: Float,
    daysRemaining: Int,
    extraFundingTier: Int,
    canAllocateFunding: Boolean,
    fundingCostLabel: String,
    onAllocateFunding: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (activeTech == null) {
                Text(
                    text = "No active research project",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Select a technology below and tap Start Research to begin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = activeTech.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = activeTech.effect.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Research progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${progressPercent.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Est. days remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (daysRemaining == 0) "< 1 day" else "$daysRemaining days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = "Extra funding tier: $extraFundingTier / ${com.presidentsimulator.game.data.ResearchState.MAX_EXTRA_FUNDING_TIER}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onAllocateFunding,
                    enabled = canAllocateFunding,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Allocate Extra Funding ($fundingCostLabel)")
                }
            }
        }
    }
}

@Composable
private fun TechTreeRow(
    tech: Technology,
    isUnlocked: Boolean,
    isActive: Boolean,
    prerequisitesMet: Boolean,
    canStart: Boolean,
    onStartResearch: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUnlocked -> MaterialTheme.colorScheme.primaryContainer
                isActive -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tech.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = tech.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(
                    label = when {
                        isUnlocked -> "UNLOCKED"
                        isActive -> "IN PROGRESS"
                        !prerequisitesMet -> "LOCKED"
                        else -> "AVAILABLE"
                    },
                )
            }
            Text(
                text = tech.effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Research cost: ${tech.scienceCost} science points",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (tech.prerequisiteIds.isNotEmpty() && !prerequisitesMet) {
                Text(
                    text = "Requires: " + tech.prerequisiteIds.joinToString { id ->
                        TechCatalog.byId(id)?.name ?: id
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (!isUnlocked && !isActive) {
                OutlinedButton(
                    onClick = onStartResearch,
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (canStart) "Start Research" else "Cannot start research")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun Long.formatScienceBudget(): String = when {
    this >= 1_000_000_000L -> "$${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000L -> "$${"%.1f".format(this / 1_000_000.0)}M"
    else -> "$$this"
}

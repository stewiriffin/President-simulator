package com.presidentsimulator.game.ui.legacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.SocietyMinistry
import com.presidentsimulator.game.data.StateReligion
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.TechCategory
import com.presidentsimulator.game.data.Technology
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssMinistryBanner
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

/**
 * Ministry of Science: categorized tech tree with science generation readout.
 */
@Composable
fun ScienceMinistryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val research = state.research
    val sciencePerTick = viewModel.projectedSciencePerTick()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        NssMinistryBanner(
            ministryLabel = "SCIENCE",
            imageUrl = NssCardImages.BANNER_SCIENCE,
            statPills = listOf(
                "Tech unlocked: ${research.unlockedTechIds.size}",
                "Science/mo: +${sciencePerTick.toInt()}",
                "Points: ${research.sciencePoints}",
            ),
            gradientColors = NssGradients.Violet,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Text(
            text = "Research Programs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Science Points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${research.sciencePoints}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "+$sciencePerTick / month " +
                        "(education, universities, tech & religion bonuses)",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Universities: ${state.society.universities} · " +
                        "Education level: ${state.society.educationLevel.roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::buildUniversity,
                    enabled = state.vitals.budget >= AdvancementViewModel.UNIVERSITY_COST,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Build University (" +
                            "${AdvancementViewModel.UNIVERSITY_COST.toBudgetString()})",
                    )
                }
            }
        }

        TechCategory.entries.forEach { category ->
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
            TechCatalog.byCategory(category).forEach { tech ->
                TechnologyCard(
                    tech = tech,
                    isUnlocked = research.isUnlocked(tech.id),
                    prerequisitesMet = research.prerequisitesMet(tech),
                    canAfford = research.sciencePoints >= tech.scienceCost,
                    onUnlock = { viewModel.unlockTechnology(tech.id) },
                )
            }
        }
        }
    }
}

/**
 * Social ministries dashboard: Health, Education, Culture, and Religion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyMinistriesScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val society = state.society
    var healthFunding by remember(society.healthFunding) {
        mutableFloatStateOf(society.healthFunding)
    }
    var educationFunding by remember(society.educationFunding) {
        mutableFloatStateOf(society.educationFunding)
    }
    var cultureFunding by remember(society.cultureFunding) {
        mutableFloatStateOf(society.cultureFunding)
    }
    var religionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Social Ministries",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Tourism income: ${society.tourismIncome.toBudgetString()}/mo · " +
                "Ministry upkeep: ${society.totalMinistryUpkeep.toBudgetString()}/mo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val forecastEngine = remember { AdvancementViewModel() }

        MinistryFundingCard(
            title = "Ministry of Health",
            levelLabel = "Health level: ${society.healthLevel.roundToInt()}%",
            funding = healthFunding,
            forecast = forecastEngine.forecastMinistryText(
                society.copy(healthFunding = healthFunding),
                SocietyMinistry.HEALTH,
            ),
            onFundingChange = { healthFunding = it },
            onFundingCommit = {
                viewModel.adjustMinistryFunding(SocietyMinistry.HEALTH, healthFunding)
            },
        )

        MinistryFundingCard(
            title = "Ministry of Education",
            levelLabel = "Education level: ${society.educationLevel.roundToInt()}%",
            funding = educationFunding,
            forecast = forecastEngine.forecastMinistryText(
                society.copy(educationFunding = educationFunding),
                SocietyMinistry.EDUCATION,
            ),
            onFundingChange = { educationFunding = it },
            onFundingCommit = {
                viewModel.adjustMinistryFunding(SocietyMinistry.EDUCATION, educationFunding)
            },
        )

        MinistryFundingCard(
            title = "Ministry of Culture",
            levelLabel = "Culture score: ${society.cultureScore.roundToInt()}%",
            funding = cultureFunding,
            forecast = forecastEngine.forecastMinistryText(
                society.copy(cultureFunding = cultureFunding),
                SocietyMinistry.CULTURE,
            ),
            onFundingChange = { cultureFunding = it },
            onFundingCommit = {
                viewModel.adjustMinistryFunding(SocietyMinistry.CULTURE, cultureFunding)
            },
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "State Religion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Current: ${society.stateReligion.displayName}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = religionBuffSummary(society.stateReligion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Changing religion costs " +
                        "-${AdvancementViewModel.RELIGION_CONVERSION_APPROVAL_COST.roundToInt()} " +
                        "approval and +" +
                        "${AdvancementViewModel.RELIGION_CONVERSION_INSTABILITY_COST.roundToInt()}% " +
                        "instability.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = religionExpanded,
                    onExpandedChange = { religionExpanded = it },
                ) {
                    TextField(
                        value = society.stateReligion.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select religion") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = religionExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = religionExpanded,
                        onDismissRequest = { religionExpanded = false },
                    ) {
                        StateReligion.entries.forEach { religion ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(religion.displayName)
                                        Text(
                                            text = religionBuffSummary(religion),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                },
                                onClick = {
                                    religionExpanded = false
                                    viewModel.changeStateReligion(religion)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MinistryFundingCard(
    title: String,
    levelLabel: String,
    funding: Float,
    forecast: String,
    onFundingChange: (Float) -> Unit,
    onFundingCommit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = levelLabel,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Funding allocation: ${(funding * 100f).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Slider(
                value = funding,
                onValueChange = onFundingChange,
                onValueChangeFinished = onFundingCommit,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = forecast,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TechnologyCard(
    tech: Technology,
    isUnlocked: Boolean,
    prerequisitesMet: Boolean,
    canAfford: Boolean,
    onUnlock: () -> Unit,
) {
    val enabled = !isUnlocked && prerequisitesMet && canAfford
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUnlocked -> NssEmerald.copy(alpha = 0.15f)
                prerequisitesMet -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = tech.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked || prerequisitesMet) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = if (isUnlocked) "UNLOCKED" else "${tech.scienceCost} SP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = tech.effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (tech.prerequisiteIds.isNotEmpty() && !prerequisitesMet) {
                Text(
                    text = "Requires: " + tech.prerequisiteIds.joinToString { id ->
                        TechCatalog.byId(id)?.name ?: id
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUnlock,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            !prerequisitesMet -> "Locked"
                            !canAfford -> "Insufficient Science"
                            else -> "Unlock"
                        },
                    )
                }
            }
        }
    }
}

private fun religionBuffSummary(religion: StateReligion): String =
    "Approval ${formatSigned(religion.approvalBonus)} · " +
        "Instability ${formatSigned(religion.instabilityModifier)} · " +
        "Science ×${"%.2f".format(religion.scienceMultiplier)} · " +
        "Military ×${"%.2f".format(religion.militaryMultiplier)} · " +
        "Production ×${"%.2f".format(religion.productionMultiplier)}"

private fun formatSigned(value: Float): String {
    val body = if (value % 1f == 0f) {
        value.roundToInt().toString()
    } else {
        "%.1f".format(value)
    }
    return if (value > 0f) "+$body" else body
}

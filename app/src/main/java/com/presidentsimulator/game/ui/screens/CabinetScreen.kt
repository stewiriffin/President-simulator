package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.CabinetEngine
import com.presidentsimulator.game.data.CabinetPortfolio
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.Minister
import com.presidentsimulator.game.data.MinisterCandidate
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMuted
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun CabinetScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val cabinet = state.cabinet
    val effects = cabinet.combinedEffects()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "Cabinet & Advisors",
            imageUrl = NssCardImages.BANNER_DOMESTIC,
            statPills = listOf(
                "Seats" to "${cabinet.filledCount}/${CabinetPortfolio.entries.size}",
                "Cohesion" to cabinet.cohesionLabel,
                "Vacant" to "${cabinet.vacancyCount}",
            ),
            gradientColors = NssGradients.Indigo,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "CABINET ROOM",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
                )
                Text(
                    cabinet.lastCabinetNote.ifBlank { "No overnight cables." },
                    fontSize = 12.sp,
                    color = NssForeground,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cohesion ${cabinet.cohesion.roundToInt()}%", fontSize = 11.sp, color = NssMutedForeground)
                NssGameBar(
                    percent = cabinet.cohesion,
                    color = when {
                        cabinet.cohesion >= 55f -> NssEmerald
                        cabinet.cohesion >= 35f -> NssAccent
                        else -> NssRed
                    },
                )
                Text(
                    "Prod ×${"%.2f".format(effects.productionMultiplier)} · Sci ×${"%.2f".format(effects.scienceMultiplier)} · " +
                        "Mil ×${"%.2f".format(effects.militaryStrengthMultiplier)} · Society ×${"%.2f".format(effects.societyGainMultiplier)}",
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (effects.budgetSkimPerMonth > 0L) {
                    Text(
                        "Corruption skim ${effects.budgetSkimPerMonth.toBudgetString()}/mo",
                        fontSize = 11.sp,
                        color = NssRed,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text(
                    "Resignations ${cabinet.resignationsThisTerm} · Firings ${cabinet.firingsThisTerm} · Scandals ${cabinet.scandalsThisTerm}",
                    fontSize = 10.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Text(
                "SITTING MINISTERS",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            CabinetPortfolio.entries.forEach { portfolio ->
                val minister = cabinet.ministerFor(portfolio)
                if (minister != null) {
                    MinisterCard(
                        minister = minister,
                        canFire = cabinet.fireCooldownMonths == 0 &&
                            state.vitals.budget >= CabinetEngine.FIRE_COST,
                        fireLabel = when {
                            cabinet.fireCooldownMonths > 0 ->
                                "Fire cooldown ${cabinet.fireCooldownMonths}mo"
                            else -> "Fire (${CabinetEngine.FIRE_COST.toBudgetString()})"
                        },
                        onFire = { viewModel.fireCabinetMinister(portfolio) },
                    )
                } else {
                    VacancyCard(portfolio = portfolio)
                }
            }

            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "SHORTLIST",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = NssPrimary,
                        letterSpacing = 2.sp,
                    )
                    val canReshuffle = cabinet.reshuffleCooldownMonths == 0 &&
                        state.vitals.budget >= 800_000_000L
                    Text(
                        text = when {
                            cabinet.reshuffleCooldownMonths > 0 ->
                                "Refresh ${cabinet.reshuffleCooldownMonths}mo"
                            else -> "Refresh shortlist"
                        },
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(if (canReshuffle) NssAccent else NssMutedForeground.copy(alpha = 0.35f))
                            .clickable(enabled = canReshuffle) { viewModel.reshuffleCabinetCandidates() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = NssOnPhoto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
                if (cabinet.candidates.isEmpty()) {
                    Text(
                        if (cabinet.vacancyCount == 0) {
                            "All seats filled — fire someone to open a shortlist."
                        } else {
                            "No candidates. Refresh the shortlist."
                        },
                        fontSize = 12.sp,
                        color = NssMutedForeground,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                } else {
                    cabinet.candidates.forEach { candidate ->
                        CandidateCard(
                            candidate = candidate,
                            canAppoint = cabinet.appointCooldownMonths == 0 &&
                                state.vitals.budget >= candidate.hireCost &&
                                cabinet.ministerFor(candidate.portfolio) == null,
                            onAppoint = { viewModel.appointCabinetCandidate(candidate.id) },
                        )
                    }
                }
            }

            if (cabinet.cabinetLog.isNotEmpty()) {
                NssPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "CABINET LOG",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = NssPrimary,
                        letterSpacing = 2.sp,
                    )
                    cabinet.cabinetLog.takeLast(8).asReversed().forEach { line ->
                        Text(
                            "• $line",
                            fontSize = 11.sp,
                            color = NssMutedForeground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinisterCard(
    minister: Minister,
    canFire: Boolean,
    fireLabel: String,
    onFire: () -> Unit,
) {
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(minister.portfolio.displayName, fontSize = 10.sp, color = NssPrimary, fontWeight = FontWeight.Bold)
                Text(minister.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = NssForeground)
                Text(
                    "Grade ${minister.grade} · ${minister.monthsInOffice}mo in office",
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                )
            }
            Text(
                minister.grade,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = when {
                    minister.competence >= 65f -> NssEmerald
                    minister.competence >= 45f -> NssAccent
                    else -> NssRed
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        StatRow("Competence", minister.competence)
        StatRow("Loyalty", minister.loyalty)
        StatRow(
            "Scandal heat",
            minister.scandalHeat,
            warn = minister.scandalHeat >= 50f,
        )
        if (minister.traits.isNotEmpty()) {
            Text(
                minister.traits.joinToString(" · ") { it.displayName },
                fontSize = 11.sp,
                color = NssAccent,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Text(
            text = fireLabel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(NssCardShape)
                .background(if (canFire) NssRed.copy(alpha = 0.85f) else NssMutedForeground.copy(alpha = 0.35f))
                .clickable(enabled = canFire, onClick = onFire)
                .padding(vertical = 10.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VacancyCard(portfolio: CabinetPortfolio) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .border(1.dp, NssBorder, NssCardShape)
            .background(NssMuted.copy(alpha = 0.4f))
            .padding(12.dp),
    ) {
        Text(portfolio.displayName, fontSize = 10.sp, color = NssRed, fontWeight = FontWeight.Bold)
        Text("VACANT", fontWeight = FontWeight.Black, fontSize = 15.sp, color = NssForeground)
        Text("Appoint from the shortlist below.", fontSize = 11.sp, color = NssMutedForeground)
    }
}

@Composable
private fun CandidateCard(
    candidate: MinisterCandidate,
    canAppoint: Boolean,
    onAppoint: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(NssCardShape)
            .border(1.dp, NssBorder, NssCardShape)
            .background(NssMuted.copy(alpha = 0.3f))
            .padding(12.dp),
    ) {
        Text(candidate.portfolio.displayName, fontSize = 10.sp, color = NssPrimary, fontWeight = FontWeight.Bold)
        Text(candidate.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
        Text(candidate.pitch, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
        Text(
            "Competence ${candidate.competence.roundToInt()} · Loyalty ${candidate.loyalty.roundToInt()} · " +
                candidate.traits.joinToString { it.displayName },
            fontSize = 11.sp,
            color = NssAccent,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "Appoint (${candidate.hireCost.toBudgetString()})",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(NssCardShape)
                .background(if (canAppoint) NssPrimary else NssMutedForeground.copy(alpha = 0.35f))
                .clickable(enabled = canAppoint, onClick = onAppoint)
                .padding(vertical = 10.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatRow(label: String, value: Float, warn: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.weight(1f))
        Text(
            "${value.roundToInt()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (warn) NssRed else NssForeground,
        )
    }
    NssGameBar(
        percent = value,
        color = when {
            warn -> NssRed
            value >= 60f -> NssEmerald
            value >= 40f -> NssAccent
            else -> NssRed
        },
    )
}

package com.presidentsimulator.game.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.DemographicsState
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssScreenHeader
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.CampaignAction
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

data class Demographic(
    val name: String,
    val icon: ImageVector,
    val populationShare: Float,
    val approval: Float,
    val reasons: List<String>,
)

private fun demographicsFromState(state: GameState): List<Demographic> {
    val demo = state.demographics
    val taxRate = state.economy.taxRate
    return listOf(
        Demographic(
            name = "Working Class",
            icon = Icons.Default.Construction,
            populationShare = DemographicsState.SHARE_WORKING,
            approval = demo.workingClass,
            reasons = listOf(
                if (taxRate > 0.2f) "Tax burden on households" else "Manageable tax climate",
                if (state.production.foodShortage) "Food pressure on consumers" else "Staple supply stable",
            ),
        ),
        Demographic(
            name = "Business & Elite",
            icon = Icons.Default.BusinessCenter,
            populationShare = DemographicsState.SHARE_BUSINESS,
            approval = demo.businessElite,
            reasons = listOf(
                if (taxRate > 0.3f) "Corporate tax friction" else "Investment-friendly rates",
                if (state.trade.activeDeals.isNotEmpty()) "Active trade contracts" else "Thin commercial pipeline",
            ),
        ),
        Demographic(
            name = "Military & Defense",
            icon = Icons.Default.Security,
            populationShare = DemographicsState.SHARE_MILITARY,
            approval = demo.military,
            reasons = listOf(
                if (state.military.salaryFunding >= 1f) "Salary funding respected" else "Pay package lagging",
                if (state.diplomacy.activeWar != null) "Combat footing active" else "Peacetime posture",
            ),
        ),
        Demographic(
            name = "Academics & Youth",
            icon = Icons.Default.School,
            populationShare = DemographicsState.SHARE_ACADEMICS,
            approval = demo.academics,
            reasons = listOf(
                if (state.research.unlockedTechIds.size > 5) "Strong research ecosystem" else "Innovation backlog",
                if (state.society.universities >= 8) "Campus capacity expanding" else "Higher-ed capacity limited",
            ),
        ),
    )
}

@Composable
fun ApprovalDemographicsScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val demographics = remember(state) { demographicsFromState(state) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "National Demographics",
            imageUrl = NssCardImages.BANNER_DOMESTIC,
            statPills = listOf(
                "Overall" to "${state.vitals.approval.roundToInt()}%",
                "Election" to state.nextElectionYear.toString(),
                "Blocs" to "${demographics.size}",
            ),
            gradientColors = NssGradients.Indigo,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("CAMPAIGN ACTIONS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
                Text(
                    text = "Spend political capital before the ${state.nextElectionYear} election.",
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                CampaignAction.entries.forEach { action ->
                    val canAfford = state.vitals.budget >= action.cost
                    Text(
                        text = "${action.displayName} · ${action.cost.toBudgetString()}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(NssCardShape)
                            .background(if (canAfford) NssAccent else NssMutedForeground.copy(alpha = 0.3f))
                            .clickable(enabled = canAfford) { viewModel.runCampaignAction(action) }
                            .padding(vertical = 10.dp),
                        color = NssOnPhoto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            demographics.forEach { bloc ->
                NssPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(bloc.icon, contentDescription = null, tint = NssPrimary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bloc.name, fontWeight = FontWeight.Bold, color = NssForeground)
                            Text(
                                "${(bloc.populationShare * 100f).roundToInt()}% of population",
                                fontSize = 11.sp,
                                color = NssMutedForeground,
                            )
                        }
                        Text(
                            "${bloc.approval.roundToInt()}%",
                            fontWeight = FontWeight.Black,
                            color = if (bloc.approval >= 50f) NssEmerald else NssRed,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NssGameBar(percent = bloc.approval, color = if (bloc.approval >= 50f) NssEmerald else NssRed)
                    bloc.reasons.forEach { reason ->
                        Text("• $reason", fontSize = 12.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            if (state.demographics.recentReasons.isNotEmpty()) {
                NssPanel(modifier = Modifier.fillMaxWidth()) {
                    Text("RECENT SHIFTS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
                    state.demographics.recentReasons.takeLast(5).asReversed().forEach { note ->
                        Text("• $note", fontSize = 12.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

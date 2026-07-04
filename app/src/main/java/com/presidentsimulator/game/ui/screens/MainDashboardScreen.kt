package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.components.HeroStat
import com.presidentsimulator.game.ui.components.NssHeroBanner
import com.presidentsimulator.game.ui.components.NssKpiCard
import com.presidentsimulator.game.ui.components.NssSectionHead
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import com.presidentsimulator.game.viewmodel.toPopulationString
import kotlin.math.roundToInt

/**
 * Command center overview matching the NSS design system.
 */
@Composable
fun MainDashboardScreen(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val gdp = remember(state) { AnalyticsSaveViewModel().calculateGDP(state) }
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)
    val tradeBalance = state.economy.effectiveExports - state.economy.effectiveImports

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        NssHeroBanner(
            ministryLabel = "COMMAND CENTER",
            stats = listOf(
                HeroStat("Total GDP", formatCompactMoney(gdp), true),
                HeroStat("Approval", state.vitals.approval.toApprovalString(), state.vitals.approval >= 50f),
                HeroStat("Stability", "${stability.roundToInt()}%", stability >= 50f),
                HeroStat("Trade Bal.", formatMa2Money(tradeBalance), tradeBalance >= 0),
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NssSectionHead(
                title = "National Overview",
                subtitle = "${state.dateLabel} · ${state.vitals.population.toPopulationString()} citizens",
            )

            val kpis = listOf(
                Triple("Treasury", formatCompactMoney(state.vitals.budget), state.netIncome >= 0),
                Triple("Net Income", formatMa2Money(state.netIncome) + "/mo", state.netIncome >= 0),
                Triple("Military Power", state.effectiveCombatStrength.roundToInt().toString(), true),
                Triple("Coup Risk", "${state.internalSecurity.coupRisk.roundToInt()}%", state.internalSecurity.coupRisk < 50f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                kpis.take(2).forEach { (label, value, positive) ->
                    NssKpiCard(
                        label = label,
                        value = value,
                        delta = if (positive) "Within operational range" else "Requires attention",
                        positive = positive,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                kpis.drop(2).forEach { (label, value, positive) ->
                    NssKpiCard(
                        label = label,
                        value = value,
                        delta = if (positive) "Within operational range" else "Requires attention",
                        positive = positive,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

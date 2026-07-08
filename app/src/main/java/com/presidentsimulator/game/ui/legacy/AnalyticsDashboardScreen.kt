package com.presidentsimulator.game.ui.legacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.HistoricalSnapshot
import com.presidentsimulator.game.data.SaveLoadFeedback
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toPopulationString
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Ministry of Statistics: historical trends, GDP readout, and save/load controls.
 */
@Composable
fun AnalyticsDashboardScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val feedback by viewModel.saveLoadFeedback.collectAsStateWithLifecycle()
    val history = state.analytics.history
    val currentGdp = viewModel.currentGdp()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Ministry of Statistics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Rolling ledger: ${history.size} / ${com.presidentsimulator.game.data.AnalyticsState.MAX_HISTORY_SIZE} months",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CurrentMetricsCard(state = state, gdp = currentGdp)

        TrendVisualizerCard(history = history)

        SystemControlsSection(
            feedback = feedback,
            hasAutomatedSave = viewModel.hasAutomatedSave(),
            onSave = viewModel::saveGameProgress,
            onLoad = viewModel::loadLastAutomatedSave,
        )
    }
}

@Composable
private fun CurrentMetricsCard(state: GameState, gdp: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Indicators — ${state.dateLabel}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow("Budget", state.vitals.budget.toBudgetString())
            MetricRow("Approval", state.vitals.approval.toApprovalString())
            MetricRow("Population", state.vitals.population.toPopulationString())
            MetricRow("GDP", gdp.toBudgetString())
            MetricRow("Net income", state.netIncome.toBudgetString())
        }
    }
}

@Composable
private fun TrendVisualizerCard(history: List<HistoricalSnapshot>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Trend Visualizer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Bars compare each month against the series minimum. " +
                    "Delta is first → latest snapshot in the window.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Text(
                    text = "No history yet. Advance time to begin recording analytics.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                TrendSeries(
                    title = "Budget",
                    history = history,
                    values = history.map { it.budget.toDouble() },
                    deltaLabel = AnalyticsSaveViewModel.trendDelta(history) { it.budget }
                        .toBudgetString()
                        .withTrendSign(
                            AnalyticsSaveViewModel.trendDelta(history) { it.budget },
                        ),
                    positiveTrend = AnalyticsSaveViewModel.trendDelta(history) { it.budget } >= 0,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                TrendSeries(
                    title = "Approval",
                    history = history,
                    values = history.map { it.approval.toDouble() },
                    deltaLabel = formatApprovalDelta(
                        AnalyticsSaveViewModel.trendDeltaFloat(history) { it.approval },
                    ),
                    positiveTrend = AnalyticsSaveViewModel.trendDeltaFloat(history) { it.approval } >= 0f,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                TrendSeries(
                    title = "GDP",
                    history = history,
                    values = history.map { it.gdp.toDouble() },
                    deltaLabel = AnalyticsSaveViewModel.trendDelta(history) { it.gdp }
                        .toBudgetString()
                        .withTrendSign(
                            AnalyticsSaveViewModel.trendDelta(history) { it.gdp },
                        ),
                    positiveTrend = AnalyticsSaveViewModel.trendDelta(history) { it.gdp } >= 0,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                TrendSeries(
                    title = "Population",
                    history = history,
                    values = history.map { it.population.toDouble() },
                    deltaLabel = AnalyticsSaveViewModel.trendDelta(history) { it.population }
                        .toPopulationDeltaString(),
                    positiveTrend = AnalyticsSaveViewModel.trendDelta(history) { it.population } >= 0,
                )
            }
        }
    }
}

@Composable
private fun TrendSeries(
    title: String,
    history: List<HistoricalSnapshot>,
    values: List<Double>,
    deltaLabel: String,
    positiveTrend: Boolean,
) {
    val trendColor = if (positiveTrend) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val span = (max - min).takeIf { it > 0.0 } ?: 1.0

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (positiveTrend) "▲ $deltaLabel" else "▼ $deltaLabel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = trendColor,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, value ->
                val normalized = ((value - min) / span).toFloat().coerceIn(0.08f, 1f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(normalized)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(trendColor.copy(alpha = 0.35f + normalized * 0.65f)),
                    )
                    if (history.size <= 12 || index == 0 || index == history.lastIndex) {
                        Text(
                            text = history[index].month.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (history.isNotEmpty()) {
            Text(
                text = "${history.first().dateLabel} → ${history.last().dateLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SystemControlsSection(
    feedback: SaveLoadFeedback,
    hasAutomatedSave: Boolean,
    onSave: () -> Unit,
    onLoad: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "System Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Serialize the full GameState tree to local storage and restore it on demand.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Game Progress")
            }
            OutlinedButton(
                onClick = onLoad,
                enabled = hasAutomatedSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (hasAutomatedSave) {
                        "Load Last Automated Save"
                    } else {
                        "Load Last Automated Save (none)"
                    },
                )
            }

            HorizontalDivider()

            Text(
                text = "Save status",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = feedback.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (feedback.success) {
                    MaterialTheme.colorScheme.secondary
                } else if (feedback.payloadBytes == 0 && feedback.message.contains("failed", true)) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (feedback.payloadBytes > 0) {
                Text(
                    text = "Serialized package size: " +
                        AnalyticsSaveViewModel.formatBytes(feedback.payloadBytes) +
                        " (${feedback.payloadBytes} bytes)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatApprovalDelta(delta: Float): String {
    val sign = if (delta > 0f) "+" else ""
    return "$sign${delta.roundToInt()}%"
}

private fun String.withTrendSign(delta: Long): String {
    if (delta > 0L && !startsWith("+") && !startsWith("-")) {
        return "+$this"
    }
    return this
}

private fun Long.toPopulationDeltaString(): String {
    val sign = when {
        this > 0L -> "+"
        this < 0L -> "-"
        else -> ""
    }
    val absValue = abs(this)
    val body = when {
        absValue >= 1_000_000_000L -> "%.2fB".format(absValue / 1_000_000_000.0)
        absValue >= 1_000_000L -> "%.1fM".format(absValue / 1_000_000.0)
        absValue >= 1_000L -> "%.1fK".format(absValue / 1_000.0)
        else -> absValue.toString()
    }
    return "$sign$body"
}

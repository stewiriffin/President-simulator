package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.HistoricalSnapshot
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString

@Composable
fun AnalyticsScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val history = state.analytics.history
    val feedback by viewModel.saveLoadFeedback.collectAsState()
    val hasSave by viewModel.hasSave.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssScreenHeader(
            title = "Analytics",
            imageUrl = NssCardImages.BANNER_ECONOMY,
            statPills = listOf(
                "Records" to "${history.size}",
                "Years" to "${(history.size / 12).coerceAtLeast(0)}",
                "Election" to state.nextElectionYear.toString(),
            ),
            gradientColors = NssGradients.Sky,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("SYSTEM CONTROLS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssPrimary, letterSpacing = 2.sp)
                Text(
                    text = "Autosave plus three manual slots for the full GameState snapshot.",
                    fontSize = 12.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                Text(
                    text = "SAVE AUTOSAVE",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NssCardShape)
                        .background(NssPrimary)
                        .clickable { viewModel.saveGameProgress() }
                        .padding(vertical = 12.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (hasSave) "LOAD AUTOSAVE" else "LOAD AUTOSAVE (NONE)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(NssCardShape)
                        .background(if (hasSave) NssBorder else NssBorder.copy(alpha = 0.4f))
                        .clickable(enabled = hasSave) { viewModel.loadLastAutomatedSave() }
                        .padding(vertical = 12.dp),
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "MANUAL SLOTS",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
                )
                viewModel.listSaveSlots().forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "SAVE ${slot.slotIndex}",
                            modifier = Modifier
                                .weight(1f)
                                .clip(NssCardShape)
                                .background(NssPrimary.copy(alpha = 0.85f))
                                .clickable { viewModel.saveToSlot(slot.slotIndex) }
                                .padding(vertical = 10.dp),
                            color = NssOnPhoto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (slot.occupied) "LOAD ${slot.slotIndex}" else "EMPTY",
                            modifier = Modifier
                                .weight(1f)
                                .clip(NssCardShape)
                                .background(if (slot.occupied) NssBorder else NssBorder.copy(alpha = 0.4f))
                                .clickable(enabled = slot.occupied) { viewModel.loadFromSlot(slot.slotIndex) }
                                .padding(vertical = 10.dp),
                            color = NssForeground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (slot.occupied) {
                        Text(
                            text = slot.label,
                            fontSize = 11.sp,
                            color = NssMutedForeground,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                Text(
                    text = feedback.message.ifBlank { "Ready." },
                    fontSize = 12.sp,
                    color = when {
                        feedback.success -> NssEmerald
                        feedback.message.contains("fail", ignoreCase = true) -> NssRed
                        else -> NssMutedForeground
                    },
                    modifier = Modifier.padding(top = 10.dp),
                )
                if (feedback.payloadBytes > 0) {
                    Text(
                        text = "Package size: ${AnalyticsSaveViewModel.formatBytes(feedback.payloadBytes)}",
                        fontSize = 11.sp,
                        color = NssMutedForeground,
                    )
                }
            }

            if (history.size < 2) {
                NssPanel {
                    Text(
                        text = "Insufficient Data",
                        fontWeight = FontWeight.Bold,
                        color = NssForeground,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Advance time to begin recording historical analytics.",
                        color = NssMutedForeground,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                val advisories = remember(history, state) {
                    AnalyticsSaveViewModel.buildAdvisories(state)
                }
                if (advisories.isNotEmpty()) {
                    NssPanel(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STRATEGIC ADVISORIES",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = NssPrimary,
                            letterSpacing = 2.sp,
                        )
                        advisories.forEach { tip ->
                            Text(
                                text = "• $tip",
                                fontSize = 12.sp,
                                color = NssForeground,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }

                LineChartCard(
                    title = "Approval Rating",
                    history = history,
                    values = history.map { it.approval.toDouble() },
                    color = Color(0xFF10B981),
                    formatValue = { "%.1f%%".format(it) }
                )

                LineChartCard(
                    title = "Treasury Budget",
                    history = history,
                    values = history.map { it.budget.toDouble() },
                    color = Color(0xFF3B82F6),
                    formatValue = { it.toLong().toBudgetString() }
                )

                LineChartCard(
                    title = "Gross Domestic Product (GDP)",
                    history = history,
                    values = history.map { it.gdp.toDouble() },
                    color = Color(0xFF8B5CF6),
                    formatValue = { it.toLong().toBudgetString() }
                )
            }
        }
    }
}

@Composable
private fun LineChartCard(
    title: String,
    history: List<HistoricalSnapshot>,
    values: List<Double>,
    color: Color,
    formatValue: (Double) -> String,
) {
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val span = (max - min).takeIf { it > 0.0 } ?: 1.0
    val latest = values.lastOrNull() ?: 0.0

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = NssForeground,
                letterSpacing = 1.sp,
            )
            Text(
                text = formatValue(latest),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val width = size.width
            val height = size.height

            // Draw grid lines
            val gridColor = Color.White.copy(alpha = 0.1f)
            drawLine(
                color = gridColor,
                start = Offset(0f, 0f),
                end = Offset(width, 0f),
                strokeWidth = 1f
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 1f
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1f
            )

            // Draw data line
            val stepX = width / (values.size - 1).coerceAtLeast(1).toFloat()
            val path = Path()

            values.forEachIndexed { index, value ->
                val normalizedY = 1f - ((value - min) / span).toFloat()
                val x = index * stepX
                val y = (normalizedY * height).coerceIn(0f, height)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 4.dp.toPx())
            )
            
            // Draw points
            values.forEachIndexed { index, value ->
                val normalizedY = 1f - ((value - min) / span).toFloat()
                val x = index * stepX
                val y = (normalizedY * height).coerceIn(0f, height)
                
                drawCircle(
                    color = color,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = NssBackground,
                    radius = 1.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min: ${formatValue(min)}",
                fontSize = 10.sp,
                color = NssMutedForeground
            )
            Text(
                text = "Max: ${formatValue(max)}",
                fontSize = 10.sp,
                color = NssMutedForeground
            )
        }
    }
}

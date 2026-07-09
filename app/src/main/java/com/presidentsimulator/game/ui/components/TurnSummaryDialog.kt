package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.TurnSummary
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toPopulationString

@Composable
fun TurnSummaryDialog(
    summary: TurnSummary,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            val dateLabel = "${GameState.monthName(summary.month)} ${summary.year}"

            Text(
                text = "MONTHLY BULLETIN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = dateLabel,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = NssForeground,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryRow(
                        label = "Net Income",
                        value = summary.netIncome.toBudgetString(),
                        isPositive = summary.netIncome >= 0,
                    )
                    SummaryRow(
                        label = "Budget Delta",
                        value = (if (summary.budgetDelta > 0) "+" else "") + summary.budgetDelta.toBudgetString(),
                        isPositive = summary.budgetDelta >= 0,
                    )
                    SummaryRow(
                        label = "Approval Delta",
                        value = (if (summary.approvalDelta > 0) "+" else "") + String.format("%.1f%%", summary.approvalDelta),
                        isPositive = summary.approvalDelta >= 0f,
                    )
                    SummaryRow(
                        label = "Population Growth",
                        value = (if (summary.populationDelta > 0) "+" else "") + summary.populationDelta.toPopulationString(),
                        isPositive = summary.populationDelta >= 0,
                    )
                    SummaryRow(
                        label = "GDP Delta",
                        value = (if (summary.gdpDelta > 0) "+" else "") + summary.gdpDelta.toBudgetString(),
                        isPositive = summary.gdpDelta >= 0,
                    )
                }
            }

            if (summary.bulletin.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                NssPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "WHAT HAPPENED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = NssPrimary,
                        letterSpacing = 2.sp,
                    )
                    summary.bulletin.forEach { line ->
                        Text(
                            text = "• $line",
                            fontSize = 12.sp,
                            color = NssMutedForeground,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Continue",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(NssPrimary)
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isPositive: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = NssMutedForeground,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) NssEmerald else NssRed,
        )
    }
}

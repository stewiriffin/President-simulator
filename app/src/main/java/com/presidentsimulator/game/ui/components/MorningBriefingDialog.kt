package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.AgendaItem
import com.presidentsimulator.game.data.AgendaPriority
import com.presidentsimulator.game.data.AgendaState
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun MorningBriefingDialog(
    agenda: AgendaState,
    year: Int,
    month: Int,
    onDismiss: () -> Unit,
    onJumpToAction: (AgendaItem) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
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
            val dateLabel = "${GameState.monthName(month)} $year"

            Text(
                text = "MORNING BRIEFING",
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
                modifier = Modifier.padding(top = 4.dp),
            )

            if (agenda.briefingIntro.isNotBlank()) {
                Text(
                    text = agenda.briefingIntro,
                    fontSize = 12.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BriefingStatChip(
                    label = "FILES",
                    value = "${agenda.items.size}",
                    color = NssPrimary,
                    modifier = Modifier.weight(1f),
                )
                BriefingStatChip(
                    label = "CRITICAL",
                    value = "${agenda.criticalCount}",
                    color = if (agenda.criticalCount > 0) NssRed else NssEmerald,
                    modifier = Modifier.weight(1f),
                )
                BriefingStatChip(
                    label = "STREAK",
                    value = "${agenda.criticalAddressedStreak}",
                    color = NssAccent,
                    modifier = Modifier.weight(1f),
                )
            }

            if (agenda.resolvedLastMonth.isNotEmpty()) {
                NssPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "RESOLVED SINCE LAST BRIEFING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = NssEmerald,
                        letterSpacing = 1.sp,
                    )
                    agenda.resolvedLastMonth.take(3).forEach { id ->
                        Text(
                            text = "• ${id.replace('_', ' ')}",
                            fontSize = 11.sp,
                            color = NssMutedForeground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = "TODAY'S AGENDA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            agenda.items.forEach { item ->
                AgendaItemCard(
                    item = item,
                    acted = agenda.isActed(item.id),
                    onJump = { onJumpToAction(item) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "ACKNOWLEDGE BRIEFING",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(NssCardShape)
                    .background(NssPrimary)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Time stays paused until you acknowledge. Jump to a file to act immediately.",
                fontSize = 10.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BriefingStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = color)
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground, letterSpacing = 1.sp)
    }
}

@Composable
private fun AgendaItemCard(
    item: AgendaItem,
    acted: Boolean,
    onJump: () -> Unit,
) {
    val accent = when (item.priority) {
        AgendaPriority.CRITICAL -> NssRed
        AgendaPriority.HIGH -> NssAccent
        AgendaPriority.MEDIUM -> NssPrimary
        AgendaPriority.OPPORTUNITY -> NssEmerald
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .background(NssBackground)
            .border(1.dp, accent.copy(alpha = 0.45f), NssCardShape)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NssBadge(label = item.priority.label, large = false)
            if (acted) {
                Text("ACTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NssEmerald)
            }
        }
        Text(
            text = item.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NssForeground,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = item.detail,
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "→ ${item.recommendedAction}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(NssCardShape)
                .background(accent)
                .clickable(onClick = onJump)
                .padding(vertical = 8.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

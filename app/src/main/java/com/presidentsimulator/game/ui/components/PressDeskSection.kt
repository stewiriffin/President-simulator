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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.HeadlineTone
import com.presidentsimulator.game.data.PressDesk
import com.presidentsimulator.game.data.PressHeadline
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMuted
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun PressDeskSection(
    state: GameState,
    onSpin: (String) -> Unit,
    onSuppress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = state.press
    val canSpin = press.spinCooldownMonths == 0 && state.vitals.budget >= PressDesk.SPIN_COST
    val canSuppress = press.suppressCooldownMonths == 0 && state.vitals.budget >= PressDesk.SUPPRESS_COST

    NssPanel(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "NATIONAL PRESS DESK",
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = NssPrimary,
            letterSpacing = 2.sp,
        )
        Text(
            text = "${press.sentimentLabel} · credibility ${press.credibilityLabel} " +
                "(${press.credibility.roundToInt()}) · freedom ${press.pressFreedom.roundToInt()}",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        NssGameBar(
            percent = press.mediaSentiment,
            color = when {
                press.mediaSentiment >= 55f -> NssEmerald
                press.mediaSentiment >= 40f -> NssAccent
                else -> NssRed
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Leak ${press.leakRisk.roundToInt()}%",
                fontSize = 10.sp,
                color = if (press.leakRisk >= 40f) NssRed else NssMutedForeground,
            )
            Text(
                "Spun ${press.storiesSpun} · Buried ${press.storiesSuppressed} · Leaks ${press.suppressLeaks}",
                fontSize = 10.sp,
                color = NssMutedForeground,
            )
        }
        if (press.lastDeskNote.isNotBlank()) {
            Text(
                press.lastDeskNote,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NssForeground,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        press.hottestArc?.takeIf { it.intensity >= 35f }?.let { arc ->
            Text(
                "Arc: ${arc.title} · heat ${arc.intensity.roundToInt()}",
                fontSize = 11.sp,
                color = NssAccent,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        press.frontPage.take(4).forEach { headline ->
            PressHeadlineRow(
                headline = headline,
                canSpin = canSpin && !headline.handled && headline.tone != HeadlineTone.POSITIVE,
                canSuppress = canSuppress && !headline.handled,
                spinLabel = when {
                    press.spinCooldownMonths > 0 -> "Spin ${press.spinCooldownMonths}mo"
                    else -> "Spin ${PressDesk.SPIN_COST.toBudgetString()}"
                },
                suppressLabel = when {
                    press.suppressCooldownMonths > 0 -> "Bury ${press.suppressCooldownMonths}mo"
                    else -> "Bury ${PressDesk.SUPPRESS_COST.toBudgetString()}"
                },
                onSpin = { onSpin(headline.id) },
                onSuppress = { onSuppress(headline.id) },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (press.headlines.isEmpty()) {
            Text(
                "No copy on the wire yet — advance a month.",
                fontSize = 12.sp,
                color = NssMutedForeground,
            )
        }
    }
}

@Composable
private fun PressHeadlineRow(
    headline: PressHeadline,
    canSpin: Boolean,
    canSuppress: Boolean,
    spinLabel: String,
    suppressLabel: String,
    onSpin: () -> Unit,
    onSuppress: () -> Unit,
) {
    val toneColor = when (headline.tone) {
        HeadlineTone.POSITIVE -> NssEmerald
        HeadlineTone.NEUTRAL -> NssMutedForeground
        HeadlineTone.NEGATIVE -> NssAccent
        HeadlineTone.SCANDAL -> NssRed
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .border(1.dp, NssBorder, NssCardShape)
            .background(NssMuted.copy(alpha = 0.35f))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = headline.tone.name,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = toneColor,
                letterSpacing = 1.sp,
            )
            Text(
                text = " · ${headline.outlet.displayName}",
                fontSize = 9.sp,
                color = NssMutedForeground,
            )
            if (headline.handled) {
                Text(
                    text = when {
                        headline.backfired -> " · BACKFIRE"
                        headline.leaked -> " · LEAKED"
                        else -> " · HANDLED"
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (headline.backfired || headline.leaked) NssRed else NssEmerald,
                )
            }
        }
        Text(
            text = headline.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NssForeground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = headline.lede,
            fontSize = 11.sp,
            color = NssMutedForeground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (!headline.handled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DeskActionChip(
                    label = spinLabel,
                    enabled = canSpin,
                    color = NssPrimary,
                    onClick = onSpin,
                    modifier = Modifier.weight(1f),
                )
                DeskActionChip(
                    label = suppressLabel,
                    enabled = canSuppress,
                    color = Color(0xFF7C3AED),
                    onClick = onSuppress,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DeskActionChip(
    label: String,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .clip(NssCardShape)
            .background(if (enabled) color else NssMutedForeground.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        color = NssOnPhoto,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

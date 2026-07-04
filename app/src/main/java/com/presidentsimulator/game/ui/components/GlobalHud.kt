package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.DeficitRed
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.ui.theme.WarningOrange
import com.presidentsimulator.game.viewmodel.toApprovalString
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toPopulationString

/**
 * Persistent sticky HUD visible across all main screens.
 * Treasury text is green when net income is positive, red when negative.
 */
@Composable
fun GlobalHud(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val netIncome = state.netIncome
    val treasuryColor = when {
        netIncome > 0L -> ProfitGreen
        netIncome < 0L -> DeficitRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    val approvalColor = when {
        state.vitals.approval >= 60f -> ProfitGreen
        state.vitals.approval <= 35f -> DeficitRed
        else -> WarningOrange
    }
    val netPrefix = if (netIncome >= 0L) "+" else ""

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HudMetric(
                icon = Icons.Default.DateRange,
                label = "Date",
                value = state.dateLabel,
                valueColor = MaterialTheme.colorScheme.onSurface,
            )
            HudMetric(
                icon = Icons.Default.AttachMoney,
                label = "Treasury",
                value = state.vitals.budget.toBudgetString(),
                valueColor = treasuryColor,
                subtitle = "$netPrefix${netIncome.toBudgetString()}/mo",
                subtitleColor = treasuryColor,
            )
            HudMetric(
                icon = Icons.Default.ThumbUp,
                label = "Approval",
                value = state.vitals.approval.toApprovalString(),
                valueColor = approvalColor,
            )
            HudMetric(
                icon = Icons.Default.Groups,
                label = "Population",
                value = state.vitals.population.toPopulationString(),
                valueColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HudMetric(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    subtitle: String? = null,
    subtitleColor: Color = NeutralGray,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NeutralGray,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NeutralGray,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = subtitleColor,
            )
        }
    }
}

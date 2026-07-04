package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.viewmodel.toBudgetString

/**
 * Standard bulk selector: [ 1x | 10x | Max ] plus a single commit button.
 * Prevents repetitive tapping for builds and purchases.
 */
@Composable
fun BulkBuildControls(
    assetName: String,
    currentCount: Int,
    unitCost: Long,
    selectedAmount: Int,
    maxAffordable: Int,
    onAmountSelected: (Int) -> Unit,
    onBuild: (Int) -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Build",
) {
    val presets = listOf(1, 10)
    val amount = selectedAmount.coerceIn(0, maxAffordable.coerceAtLeast(0))
    val totalCost = unitCost * amount
    val canBuild = amount > 0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = assetName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Owned: $currentCount",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
            )
        }
        Text(
            text = "Unit cost: ${unitCost.toBudgetString()} · Order: ${amount}x",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralGray,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                FilterChip(
                    selected = selectedAmount == preset,
                    onClick = { if (maxAffordable >= preset) onAmountSelected(preset) },
                    enabled = maxAffordable >= preset,
                    label = { Text("${preset}x") },
                )
            }
            FilterChip(
                selected = selectedAmount == maxAffordable &&
                    maxAffordable > 0 &&
                    selectedAmount !in presets,
                onClick = { if (maxAffordable > 0) onAmountSelected(maxAffordable) },
                enabled = maxAffordable > 0,
                label = {
                    Text(if (maxAffordable > 0) "Max ($maxAffordable)" else "Max")
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onBuild(amount) },
            enabled = canBuild,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (canBuild) {
                    "$actionLabel (Cost: ${totalCost.toBudgetString()})"
                } else {
                    "Cannot Afford"
                },
            )
        }
    }
}

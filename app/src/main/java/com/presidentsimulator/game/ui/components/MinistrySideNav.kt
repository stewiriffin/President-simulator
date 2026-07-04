package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed

data class MinistryNavItem(
    val destination: GameDestination,
    val label: String,
    val icon: ImageVector,
)

val ministryNavItems = listOf(
    MinistryNavItem(GameDestination.Economy, "Economy", Icons.Default.AttachMoney),
    MinistryNavItem(GameDestination.Military, "Defense", Icons.Default.Shield),
    MinistryNavItem(GameDestination.Diplomacy, "Foreign Affairs", Icons.Default.Public),
    MinistryNavItem(GameDestination.LawsSociety, "Domestic Policy", Icons.Default.Groups),
    MinistryNavItem(GameDestination.SecretService, "Intelligence", Icons.Default.Visibility),
    MinistryNavItem(GameDestination.Science, "Science", Icons.Default.Science),
    MinistryNavItem(GameDestination.Governance, "United Nations", Icons.Default.Gavel),
    MinistryNavItem(GameDestination.AudioSettings, "Settings", Icons.Default.Settings),
)

@Composable
fun MinistrySideNav(
    state: GameState,
    currentRoute: String?,
    onNavigate: (GameDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alerts = collectAlerts(state)

    Column(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(NssCard.copy(alpha = 0.6f)),
    ) {
        Text(
            text = "GOVERNMENT",
            style = MaterialTheme.typography.labelSmall,
            color = NssMutedForeground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        HorizontalDivider(color = NssBorder)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ministryNavItems.forEach { item ->
                val selected = currentRoute == item.destination.route
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(item.destination) }
                        .background(if (selected) NssPrimary.copy(alpha = 0.08f) else NssCard.copy(alpha = 0f)),
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(2.dp)
                                .height(44.dp)
                                .background(NssPrimary),
                        )
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) NssPrimary else NssMutedForeground,
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) NssPrimary else NssMutedForeground,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = NssPrimary.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = NssBorder)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "ALERTS",
                style = MaterialTheme.typography.labelSmall,
                color = NssMutedForeground,
            )
            alerts.forEach { (level, message) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NssBorder.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "[$level]",
                        style = MaterialTheme.typography.labelSmall,
                        color = when (level) {
                            "CRIT" -> NssRed
                            "WARN" -> NssAmber
                            else -> NssMutedForeground
                        },
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = NssForeground.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed

data class BottomNavItem(
    val destination: GameDestination,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(GameDestination.Dashboard, "Overview", Icons.Default.AccountBalance),
    BottomNavItem(GameDestination.Economy, "Economy", Icons.Default.AttachMoney),
    BottomNavItem(GameDestination.Military, "Defense", Icons.Default.Shield),
    BottomNavItem(GameDestination.Diplomacy, "Foreign", Icons.Default.Public),
    BottomNavItem(GameDestination.SecretService, "Intel", Icons.Default.Visibility),
)

fun bottomNavAlertCount(state: GameState, destination: GameDestination): Int = when (destination) {
    GameDestination.Dashboard -> collectAlertCount(state)
    GameDestination.Military -> if (state.diplomacy.activeWar != null) 1 else 0
    GameDestination.Diplomacy -> state.diplomacy.rivals.count { it.relationshipScore < 25 }
    else -> 0
}

@Composable
fun MinistryBottomNav(
    state: GameState,
    currentRoute: String?,
    onNavigate: (GameDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(NssPrimary.copy(alpha = 0.95f)),
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.destination.route
            val alerts = bottomNavAlertCount(state, item.destination)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(item.destination) },
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(NssOnPhoto.copy(alpha = 0.1f)),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-8).dp)
                                    .fillMaxWidth(0.5f)
                                    .height(2.dp)
                                    .background(NssAccent),
                            )
                        }
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) NssOnPhoto else NssOnPhoto.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp),
                        )
                        if (alerts > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-4).dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(NssRed),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = alerts.coerceAtMost(9).toString(),
                                    color = NssOnPhoto,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Text(
                        text = item.label,
                        color = if (selected) NssOnPhoto else NssOnPhoto.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

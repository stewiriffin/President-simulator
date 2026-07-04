package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playClick
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.CommandGold
import com.presidentsimulator.game.ui.theme.GameIcons
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.viewmodel.toBudgetString

/**
 * Central cabinet hub. GlobalHud is provided by the navigation shell;
 * this screen renders the ministry grid and national snapshot.
 */
@Composable
fun MainDashboardScreen(
    state: GameState,
    onOpenMinistry: (GameDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Cabinet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        Text(
            text = "Select a ministry to manage national policy.",
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralGray,
        )

        Spacer(modifier = Modifier.height(12.dp))

        NationalSnapshotCard(state = state)

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(DashboardMinistry.entries) { ministry ->
                MinistryCard(
                    ministry = ministry,
                    onClick = {
                        audio.playClick()
                        onOpenMinistry(ministry.destination)
                    },
                )
            }
        }
    }
}

private enum class DashboardMinistry(
    val title: String,
    val subtitle: String,
    val destination: GameDestination,
    val icon: ImageVector,
) {
    ECONOMY(
        title = "Ministry of Economy",
        subtitle = "Taxes & infrastructure",
        destination = GameDestination.Economy,
        icon = GameIcons.MinistryEconomy,
    ),
    DEFENSE(
        title = "Ministry of Defense",
        subtitle = "Forces & war room",
        destination = GameDestination.Military,
        icon = GameIcons.MinistryDefense,
    ),
    FOREIGN(
        title = "Foreign Affairs",
        subtitle = "Diplomacy & treaties",
        destination = GameDestination.Diplomacy,
        icon = GameIcons.MinistryForeignAffairs,
    ),
    SECRET_SERVICE(
        title = "Secret Service",
        subtitle = "Security & espionage",
        destination = GameDestination.SecretService,
        icon = GameIcons.MinistrySecretService,
    ),
    SCIENCE(
        title = "Science & Technology",
        subtitle = "Research & tech tree",
        destination = GameDestination.Science,
        icon = GameIcons.MinistryScience,
    ),
    LAWS(
        title = "Laws & Society",
        subtitle = "Parliament & social policy",
        destination = GameDestination.LawsSociety,
        icon = GameIcons.MinistryLawsSociety,
    ),
    AUDIO(
        title = "Audio Settings",
        subtitle = "Music, SFX & diagnostics",
        destination = GameDestination.AudioSettings,
        icon = GameIcons.forDestination(GameDestination.AudioSettings),
    ),
}

@Composable
private fun NationalSnapshotCard(state: GameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "National Snapshot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SnapshotStat(
                    label = "Revenue",
                    value = state.economy.totalRevenue(state.vitals.population).toBudgetString(),
                )
                SnapshotStat(
                    label = "Expenses",
                    value = state.economy.totalExpenses.toBudgetString(),
                )
                SnapshotStat(
                    label = "Military",
                    value = state.effectiveCombatStrength.toInt().toString(),
                )
                SnapshotStat(
                    label = "DEFCON",
                    value = state.military.defcon.toString(),
                )
            }
        }
    }
}

@Composable
private fun SnapshotStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NeutralGray,
        )
    }
}

@Composable
private fun MinistryCard(
    ministry: DashboardMinistry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = ministry.icon,
                contentDescription = null,
                tint = CommandGold,
            )
            Column {
                Text(
                    text = ministry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ministry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open ${ministry.title}",
                    tint = CommandGold,
                )
            }
        }
    }
}

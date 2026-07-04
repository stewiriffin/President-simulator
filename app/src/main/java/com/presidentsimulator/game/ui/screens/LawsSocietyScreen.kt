package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.ParliamentScreen
import com.presidentsimulator.game.ui.SocietyMinistriesScreen
import com.presidentsimulator.game.viewmodel.GameViewModel

/**
 * Combined Laws & Society ministry: parliament statutes and social funding.
 */
@Composable
fun LawsSocietyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Parliament") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Social Ministries") },
            )
        }
        when (selectedTab) {
            0 -> ParliamentScreen(state = state, viewModel = viewModel)
            else -> SocietyMinistriesScreen(state = state, viewModel = viewModel)
        }
    }
}

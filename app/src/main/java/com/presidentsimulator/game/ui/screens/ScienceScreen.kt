package com.presidentsimulator.game.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.ScienceMinistryScreen
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun ScienceScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    ScienceMinistryScreen(
        state = state,
        viewModel = viewModel,
        modifier = modifier,
    )
}

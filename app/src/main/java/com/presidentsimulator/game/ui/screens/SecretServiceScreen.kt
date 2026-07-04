package com.presidentsimulator.game.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.viewmodel.GameViewModel

/**
 * Navigation entry for the Secret Service ministry.
 */
@Composable
fun SecretServiceScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    SecurityScreen(
        state = state,
        viewModel = viewModel,
        modifier = modifier,
    )
}

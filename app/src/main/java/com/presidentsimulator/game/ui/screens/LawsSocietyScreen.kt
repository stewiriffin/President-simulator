package com.presidentsimulator.game.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun LawsSocietyScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    LawsScreen(
        viewModel = viewModel,
        modifier = modifier,
    )
}

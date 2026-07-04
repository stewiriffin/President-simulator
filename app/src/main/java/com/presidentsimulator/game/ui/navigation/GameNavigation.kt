package com.presidentsimulator.game.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.presidentsimulator.game.audio.GameAudioBridge
import com.presidentsimulator.game.audio.GameAudioCrisisEffect
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playClick
import com.presidentsimulator.game.ui.components.EventCrisisDialog
import com.presidentsimulator.game.ui.components.GlobalHud
import com.presidentsimulator.game.ui.screens.DiplomacyScreen
import com.presidentsimulator.game.ui.screens.EconomyScreen
import com.presidentsimulator.game.ui.screens.LawsSocietyScreen
import com.presidentsimulator.game.ui.screens.MainDashboardScreen
import com.presidentsimulator.game.ui.screens.MilitaryScreen
import com.presidentsimulator.game.ui.screens.ScienceScreen
import com.presidentsimulator.game.ui.screens.SecretServiceScreen
import com.presidentsimulator.game.ui.screens.SettingsAudioScreen
import com.presidentsimulator.game.ui.theme.DeepNavy
import com.presidentsimulator.game.ui.theme.StarkWhite
import com.presidentsimulator.game.viewmodel.GameViewModel

/**
 * Type-safe navigation destinations for the presidential dashboard.
 */
sealed class GameDestination(val route: String, val title: String) {
    data object Dashboard : GameDestination("dashboard", "Nation State Simulator")
    data object Economy : GameDestination("economy", "Ministry of Economy")
    data object Military : GameDestination("military", "Ministry of Defense")
    data object Diplomacy : GameDestination("diplomacy", "Foreign Affairs")
    data object SecretService : GameDestination("secret_service", "Secret Service")
    data object Science : GameDestination("science", "Science & Technology")
    data object LawsSociety : GameDestination("laws_society", "Laws & Society")
    data object AudioSettings : GameDestination("audio_settings", "Audio Settings")
}

/**
 * Root navigation host.
 * Hoists [GameViewModel] state and passes it into every ministry screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameNavigation(
    viewModel: GameViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val state by viewModel.state.collectAsState()
    val isAutoTicking by viewModel.isAutoTicking.collectAsState()
    val activeEvent by viewModel.currentActiveEvent.collectAsState()
    val gameOver = state.gameOver.isGameOver
    val timeBlocked = activeEvent != null || gameOver

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTitle = when (currentRoute) {
        GameDestination.Economy.route -> GameDestination.Economy.title
        GameDestination.Military.route -> GameDestination.Military.title
        GameDestination.Diplomacy.route -> GameDestination.Diplomacy.title
        GameDestination.SecretService.route -> GameDestination.SecretService.title
        GameDestination.Science.route -> GameDestination.Science.title
        GameDestination.LawsSociety.route -> GameDestination.LawsSociety.title
        GameDestination.AudioSettings.route -> GameDestination.AudioSettings.title
        else -> GameDestination.Dashboard.title
    }
    val showBack = currentRoute != null && currentRoute != GameDestination.Dashboard.route

    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    GameAudioBridge(state = state)
    GameAudioCrisisEffect(hasActiveEvent = activeEvent != null)

    activeEvent?.let { event ->
        EventCrisisDialog(
            event = event,
            onChoiceSelected = { choice ->
                audio.playClick()
                viewModel.resolveEvent(choice)
            },
        )
    }

    if (gameOver) {
        AlertDialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            title = {
                Text(
                    text = "Coup d'État",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = { Text(text = state.gameOver.reason) },
            confirmButton = {
                TextButton(onClick = viewModel::loadLastAutomatedSave) {
                    Text("Load Last Save")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentTitle) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to dashboard",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::advanceTimeTick,
                        enabled = !timeBlocked,
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next month",
                        )
                    }
                    IconButton(
                        onClick = viewModel::toggleAutoTick,
                        enabled = !gameOver,
                    ) {
                        Icon(
                            imageVector = if (isAutoTicking) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (isAutoTicking) {
                                "Pause auto-tick"
                            } else {
                                "Start auto-tick"
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = StarkWhite,
                    navigationIconContentColor = StarkWhite,
                    actionIconContentColor = StarkWhite,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GlobalHud(state = state)

            NavHost(
                navController = navController,
                startDestination = GameDestination.Dashboard.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(GameDestination.Dashboard.route) {
                    MainDashboardScreen(
                        state = state,
                        onOpenMinistry = { destination ->
                            navController.navigate(destination.route)
                        },
                    )
                }
                composable(GameDestination.Economy.route) {
                    EconomyScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.Military.route) {
                    MilitaryScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.Diplomacy.route) {
                    DiplomacyScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.SecretService.route) {
                    SecretServiceScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.Science.route) {
                    ScienceScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.LawsSociety.route) {
                    LawsSocietyScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.AudioSettings.route) {
                    SettingsAudioScreen()
                }
            }
        }
    }
}

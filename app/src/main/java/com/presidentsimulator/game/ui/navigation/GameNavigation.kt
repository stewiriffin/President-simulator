package com.presidentsimulator.game.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.presidentsimulator.game.audio.GameAudioBridge
import com.presidentsimulator.game.audio.GameAudioCrisisEffect
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playClick
import com.presidentsimulator.game.ui.GovernanceUNScreen
import com.presidentsimulator.game.ui.components.EventCrisisDialog
import com.presidentsimulator.game.ui.components.GlobalHud
import com.presidentsimulator.game.ui.components.MinistrySideNav
import com.presidentsimulator.game.ui.components.collectAlertCount
import com.presidentsimulator.game.ui.screens.DiplomacyScreen
import com.presidentsimulator.game.ui.screens.EconomyScreen
import com.presidentsimulator.game.ui.screens.LawsSocietyScreen
import com.presidentsimulator.game.ui.screens.MainDashboardScreen
import com.presidentsimulator.game.ui.screens.MilitaryScreen
import com.presidentsimulator.game.ui.screens.ScienceScreen
import com.presidentsimulator.game.ui.screens.SecretServiceScreen
import com.presidentsimulator.game.ui.screens.SettingsAudioScreen
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.viewmodel.GameViewModel

sealed class GameDestination(val route: String, val title: String) {
    data object Dashboard : GameDestination("dashboard", "Command Center")
    data object Economy : GameDestination("economy", "Economy")
    data object Military : GameDestination("military", "Defense")
    data object Diplomacy : GameDestination("diplomacy", "Foreign Affairs")
    data object SecretService : GameDestination("secret_service", "Intelligence")
    data object Science : GameDestination("science", "Science")
    data object LawsSociety : GameDestination("laws_society", "Domestic Policy")
    data object Governance : GameDestination("governance", "United Nations")
    data object AudioSettings : GameDestination("audio_settings", "Settings")
}

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
                Text(text = "Coup d'État", fontWeight = FontWeight.Bold)
            },
            text = { Text(text = state.gameOver.reason) },
            confirmButton = {
                TextButton(onClick = viewModel::loadLastAutomatedSave) {
                    Text("Load Last Save")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        GlobalHud(
            state = state,
            isAutoTicking = isAutoTicking,
            nextTurnEnabled = !timeBlocked,
            timeSpeedEnabled = !gameOver,
            alertCount = collectAlertCount(state),
            onNextTurn = {
                audio.playClick()
                viewModel.advanceTimeTick()
            },
            onToggleTimeSpeed = {
                audio.playClick()
                viewModel.toggleAutoTick()
            },
        )

        HorizontalDivider(color = NssBorder)

        Row(modifier = Modifier.fillMaxSize()) {
            MinistrySideNav(
                state = state,
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    audio.playClick()
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                        popUpTo(GameDestination.Economy.route) { saveState = true }
                        restoreState = true
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(NssBorder),
            )

            NavHost(
                navController = navController,
                startDestination = GameDestination.Economy.route,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                composable(GameDestination.Dashboard.route) {
                    MainDashboardScreen(state = state)
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
                composable(GameDestination.Governance.route) {
                    GovernanceUNScreen(state = state, viewModel = viewModel)
                }
                composable(GameDestination.AudioSettings.route) {
                    SettingsAudioScreen()
                }
            }
        }
    }
}

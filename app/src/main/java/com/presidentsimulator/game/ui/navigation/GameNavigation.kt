package com.presidentsimulator.game.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.presidentsimulator.game.ui.components.MinistryBottomNav
import com.presidentsimulator.game.ui.components.MissionResultDialog
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.TurnSummaryDialog
import com.presidentsimulator.game.ui.components.WarOutcomeDialog
import com.presidentsimulator.game.ui.components.collectAlertCount
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.screens.AnalyticsScreen
import com.presidentsimulator.game.ui.screens.ApprovalDemographicsScreen
import com.presidentsimulator.game.ui.screens.DiplomacyScreen
import com.presidentsimulator.game.ui.screens.EconomyScreen
import com.presidentsimulator.game.ui.screens.CountrySelectScreen
import com.presidentsimulator.game.ui.screens.LaunchScreen
import com.presidentsimulator.game.ui.screens.LawsScreen
import com.presidentsimulator.game.ui.screens.MainDashboardScreen
import com.presidentsimulator.game.ui.screens.MilitaryScreen
import com.presidentsimulator.game.ui.screens.ScienceScreen
import com.presidentsimulator.game.ui.screens.SecurityScreen
import com.presidentsimulator.game.ui.screens.SettingsAudioScreen
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun GameNavigation(
    viewModel: GameViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val state by viewModel.state.collectAsState()
    val timeSpeedMode by viewModel.timeSpeedMode.collectAsState()
    val activeEvent by viewModel.currentActiveEvent.collectAsState()
    val turnSummary by viewModel.turnSummary.collectAsState()
    val missionResults by viewModel.missionResults.collectAsState()
    val warOutcome by viewModel.warOutcome.collectAsState()
    val showLaunch by viewModel.showLaunchScreen.collectAsState()
    val hasSave by viewModel.hasSave.collectAsState()
    val gameOver = state.gameOver.isGameOver
    val isVictory = state.gameOver.isVictory

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var showCountrySelect by remember { mutableStateOf(false) }

    LaunchedEffect(showLaunch) {
        if (showLaunch) showCountrySelect = false
    }

    val navigate: (GameDestination) -> Unit = { destination ->
        audio.playClick()
        navController.navigate(destination.route) {
            launchSingleTop = true
            popUpTo(GameDestination.Dashboard.route) { saveState = true }
            restoreState = true
        }
    }

    if (showLaunch) {
        if (showCountrySelect) {
            CountrySelectScreen(
                nations = viewModel.playableNations(),
                onBack = {
                    audio.playClick()
                    showCountrySelect = false
                },
                onSelectCountry = { countryId ->
                    audio.playClick()
                    viewModel.startNewGame(countryId)
                    showCountrySelect = false
                },
            )
        } else {
            LaunchScreen(
                hasSave = hasSave,
                onContinueGame = {
                    audio.playClick()
                    viewModel.continueGame()
                },
                onNewGame = {
                    audio.playClick()
                    showCountrySelect = true
                },
                slots = viewModel.listSaveSlots(),
                onLoadSlot = { slot ->
                    audio.playClick()
                    viewModel.loadFromSlot(slot)
                },
            )
        }
        return
    }

    GameAudioBridge(state = state)
    GameAudioCrisisEffect(hasActiveEvent = activeEvent != null)

    // Overlay priority: crisis > war end > missions > turn summary > campaign end
    activeEvent?.let { event ->
        EventCrisisDialog(
            event = event,
            onChoiceSelected = { choice ->
                audio.playClick()
                viewModel.resolveEvent(choice)
            },
        )
    }

    if (activeEvent == null) {
        warOutcome?.let { outcome ->
            WarOutcomeDialog(
                outcome = outcome,
                onDismiss = {
                    audio.playClick()
                    viewModel.clearWarOutcome()
                },
            )
        }
    }

    val pendingMission = missionResults.firstOrNull()
    if (activeEvent == null && warOutcome == null && pendingMission != null) {
        MissionResultDialog(
            mission = pendingMission,
            state = state,
            onDismiss = {
                audio.playClick()
                viewModel.dismissMissionResult()
            },
        )
    }

    if (activeEvent == null && warOutcome == null && pendingMission == null) {
        turnSummary?.let { summary ->
            TurnSummaryDialog(
                summary = summary,
                onDismiss = {
                    audio.playClick()
                    viewModel.clearTurnSummary()
                },
            )
        }
    }

    if (gameOver) {
        CampaignEndDialog(
            isVictory = isVictory,
            reason = state.gameOver.reason,
            onLoadSave = { viewModel.loadLastAutomatedSave() },
            onReturnToLaunch = { viewModel.returnToLaunch() },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        GlobalHud(
            state = state,
            timeSpeedMode = timeSpeedMode,
            timeSpeedEnabled = !gameOver,
            alertCount = collectAlertCount(state),
            onTimeSpeedModeSelected = { mode ->
                audio.playClick()
                viewModel.setTimeSpeedMode(mode)
            },
        )

        NavHost(
            navController = navController,
            startDestination = GameDestination.Dashboard.route,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            composable(GameDestination.Dashboard.route) {
                MainDashboardScreen(
                    state = state,
                    onNavigate = navigate,
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
                SecurityScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Science.route) {
                ScienceScreen(viewModel = viewModel)
            }
            composable(GameDestination.LawsSociety.route) {
                LawsScreen(viewModel = viewModel)
            }
            composable(GameDestination.Governance.route) {
                GovernanceUNScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.AudioSettings.route) {
                SettingsAudioScreen(viewModel = viewModel)
            }
            composable(GameDestination.Analytics.route) {
                AnalyticsScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Demographics.route) {
                ApprovalDemographicsScreen(state = state, viewModel = viewModel)
            }
        }

        MinistryBottomNav(
            state = state,
            currentRoute = currentRoute,
            onNavigate = navigate,
        )
    }
}

@Composable
private fun CampaignEndDialog(
    isVictory: Boolean,
    reason: String,
    onLoadSave: () -> Unit,
    onReturnToLaunch: () -> Unit,
) {
    val accent = if (isVictory) NssEmerald else NssRed
    val headline = if (isVictory) "VICTORY" else "GAME OVER"
    val title = if (isVictory) "Mandate Secured" else "Regime Collapsed"

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(20.dp),
        ) {
            Text(headline, fontSize = 10.sp, fontWeight = FontWeight.Black, color = accent, letterSpacing = 3.sp)
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = NssForeground,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text(reason, fontSize = 13.sp, color = NssMutedForeground, lineHeight = 18.sp)
            }
            Text(
                text = "Load Last Save",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(NssCardShape)
                    .background(NssPrimary)
                    .clickable(onClick = onLoadSave)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Return to Title",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(NssCardShape)
                    .background(NssAccent)
                    .clickable(onClick = onReturnToLaunch)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

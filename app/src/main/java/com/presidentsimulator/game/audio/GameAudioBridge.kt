package com.presidentsimulator.game.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.presidentsimulator.game.data.GameState
import kotlin.math.roundToInt

/**
 * Compose-side bridge: keeps BGM aligned with geopolitics and plays one-shot SFX
 * when war or coup transitions occur.
 */
@Composable
fun GameAudioBridge(state: GameState) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    val isActiveWar = state.diplomacy.activeWar != null
    val coupRisk = state.internalSecurity.coupRisk.roundToInt()

    LaunchedEffect(isActiveWar, coupRisk, audio.musicEnabled) {
        audio.updateBgmForGameState(isActiveWar = isActiveWar, coupRisk = coupRisk)
    }

    val gameOver = state.gameOver.isGameOver
    var wasGameOver by remember { mutableStateOf(false) }
    LaunchedEffect(gameOver) {
        if (gameOver && !wasGameOver) {
            audio.playSfx(SfxType.COUP_GAME_OVER)
        }
        wasGameOver = gameOver
    }

    val warTarget = state.diplomacy.activeWar?.targetCountryId
    var previousWarTarget by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(warTarget) {
        if (warTarget != null && previousWarTarget == null) {
            audio.playSfx(SfxType.WAR_DECLARED)
        }
        previousWarTarget = warTarget
    }

    val defcon = state.military.defcon
    var previousDefcon by remember { mutableStateOf(5) }
    LaunchedEffect(defcon) {
        if (defcon < previousDefcon && defcon <= 2) {
            audio.playSfx(SfxType.CRISIS_ALERT)
        }
        previousDefcon = defcon
    }
}

/**
 * Plays crisis alert when an event becomes active.
 */
@Composable
fun GameAudioCrisisEffect(hasActiveEvent: Boolean) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var wasActive by remember { mutableStateOf(false) }

    LaunchedEffect(hasActiveEvent) {
        if (hasActiveEvent && !wasActive) {
            audio.playSfx(SfxType.CRISIS_ALERT)
        }
        wasActive = hasActiveEvent
    }
}

fun GameAudioManager.playClick() = playSfx(SfxType.CLICK)

fun GameAudioManager.playBuildSuccess() = playSfx(SfxType.BUILD_SUCCESS)

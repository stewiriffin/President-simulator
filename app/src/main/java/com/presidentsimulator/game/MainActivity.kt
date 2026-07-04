package com.presidentsimulator.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.ui.navigation.GameNavigation
import com.presidentsimulator.game.ui.theme.PresidentSimulatorTheme
import com.presidentsimulator.game.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameAudioManager.getInstance(this)
        enableEdgeToEdge()
        setContent {
            PresidentSimulatorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GameNavigation(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            GameAudioManager.releaseInstance()
        }
        super.onDestroy()
    }
}

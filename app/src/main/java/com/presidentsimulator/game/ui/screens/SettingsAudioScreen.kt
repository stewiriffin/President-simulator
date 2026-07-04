package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.BgmTrack
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.SfxType
import com.presidentsimulator.game.ui.theme.NeutralGray
import kotlin.math.roundToInt

/**
 * Audio controls panel: music/SFX volumes, test playback, and engine diagnostics.
 */
@Composable
fun SettingsAudioScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    var musicVolume by remember { mutableFloatStateOf(audio.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(audio.sfxVolume) }
    var musicEnabled by remember { mutableStateOf(audio.musicEnabled) }
    var sfxEnabled by remember { mutableStateOf(audio.sfxEnabled) }
    var diagnosticsTick by remember { mutableStateOf(0) }

    // Re-read diagnostics when tick changes (after test sound / slider moves).
    val diagnostics = remember(diagnosticsTick) { audio.diagnostics.asReversed() }
    val loadedHandles = remember(diagnosticsTick) { audio.loadedSfxHandles }
    val currentTrack = remember(diagnosticsTick) { audio.currentBgmTrack }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Audio Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Independent music and effects channels. Missing R.raw assets are skipped safely.",
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralGray,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Master Music Volume",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = {
                            musicEnabled = it
                            audio.musicEnabled = it
                            diagnosticsTick++
                        },
                    )
                }
                Text(
                    text = "${(musicVolume * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = musicVolume,
                    onValueChange = {
                        musicVolume = it
                        audio.musicVolume = it
                    },
                    onValueChangeFinished = { diagnosticsTick++ },
                    valueRange = 0f..1f,
                    enabled = musicEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Current BGM track: ${currentTrack.displayLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SFX Volume",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Switch(
                        checked = sfxEnabled,
                        onCheckedChange = {
                            sfxEnabled = it
                            audio.sfxEnabled = it
                            diagnosticsTick++
                        },
                    )
                }
                Text(
                    text = "${(sfxVolume * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = sfxVolume,
                    onValueChange = {
                        sfxVolume = it
                        audio.sfxVolume = it
                    },
                    onValueChangeFinished = { diagnosticsTick++ },
                    valueRange = 0f..1f,
                    enabled = sfxEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        audio.playSfx(SfxType.CLICK)
                        diagnosticsTick++
                    },
                    enabled = sfxEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Test Sound")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Audio Diagnostic Readout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loaded SFX handles",
                    style = MaterialTheme.typography.labelLarge,
                )
                if (loadedHandles.isEmpty()) {
                    Text(
                        text = "No SFX assets loaded (add files under res/raw).",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray,
                    )
                } else {
                    loadedHandles.forEach { (type, handle) ->
                        Text(
                            text = "• ${type.name}: handle $handle",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    text = "Engine log",
                    style = MaterialTheme.typography.labelLarge,
                )
                if (diagnostics.isEmpty()) {
                    Text(
                        text = "No events yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray,
                    )
                } else {
                    diagnostics.take(16).forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralGray,
                        )
                    }
                }
            }
        }
    }
}

private fun BgmTrack.displayLabel(): String = when (this) {
    BgmTrack.NONE -> "None"
    BgmTrack.PEACE -> "Peace"
    BgmTrack.WAR -> "War"
    BgmTrack.CRISIS -> "Crisis"
}

package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.BgmTrack
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.SfxType
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssScreenHeader
import com.presidentsimulator.game.ui.theme.NssAccent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsAudioScreen(
    viewModel: GameViewModel? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    var musicVolume by remember { mutableFloatStateOf(audio.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(audio.sfxVolume) }
    var musicEnabled by remember { mutableStateOf(audio.musicEnabled) }
    var sfxEnabled by remember { mutableStateOf(audio.sfxEnabled) }
    var diagnosticsTick by remember { mutableStateOf(0) }

    val diagnostics = remember(diagnosticsTick) { audio.diagnostics.asReversed() }
    val loadedHandles = remember(diagnosticsTick) { audio.loadedSfxHandles }
    val currentTrack = remember(diagnosticsTick) { audio.currentBgmTrack }

    Column(modifier = modifier.fillMaxSize().background(NssBackground).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        NssScreenHeader(
            title = "Settings",
            imageUrl = NssCardImages.BANNER_COMMAND,
            statPills = listOf(
                "Music" to if (musicEnabled) "On" else "Off",
                "SFX" to if (sfxEnabled) "On" else "Off",
                "Track" to currentTrack.displayLabel(),
            ),
            gradientColors = NssGradients.Indigo,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Master Music", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                    Switch(checked = musicEnabled, onCheckedChange = { musicEnabled = it; audio.musicEnabled = it; diagnosticsTick++ })
                }
                Text("${(musicVolume * 100f).roundToInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = NssPrimary)
                Slider(
                    value = musicVolume,
                    onValueChange = { musicVolume = it; audio.musicVolume = it },
                    onValueChangeFinished = { diagnosticsTick++ },
                    valueRange = 0f..1f,
                    enabled = musicEnabled,
                    colors = SliderDefaults.colors(thumbColor = NssPrimary, activeTrackColor = NssPrimary, inactiveTrackColor = NssBorder),
                )
            }

            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SFX Volume", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NssForeground)
                    Switch(checked = sfxEnabled, onCheckedChange = { sfxEnabled = it; audio.sfxEnabled = it; diagnosticsTick++ })
                }
                Text("${(sfxVolume * 100f).roundToInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = NssPrimary)
                Slider(
                    value = sfxVolume,
                    onValueChange = { sfxVolume = it; audio.sfxVolume = it },
                    onValueChangeFinished = { diagnosticsTick++ },
                    valueRange = 0f..1f,
                    enabled = sfxEnabled,
                    colors = SliderDefaults.colors(thumbColor = NssPrimary, activeTrackColor = NssPrimary, inactiveTrackColor = NssBorder),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔊 Test Sound",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NssCardShape)
                        .background(if (sfxEnabled) NssAccent else NssAccent.copy(alpha = 0.35f))
                        .clickable(enabled = sfxEnabled) { audio.playSfx(SfxType.CLICK); diagnosticsTick++ }
                        .padding(vertical = 10.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }

            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("Audio Diagnostics", fontWeight = FontWeight.Black, fontSize = 14.sp, color = NssForeground)
                Text("Loaded SFX handles", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground, modifier = Modifier.padding(top = 10.dp))
                if (loadedHandles.isEmpty()) {
                    Text("No SFX assets loaded (add files under res/raw).", fontSize = 11.sp, color = NssMutedForeground)
                } else {
                    loadedHandles.forEach { (type, handle) ->
                        Text("• ${type.name}: handle $handle", fontSize = 11.sp, color = NssMutedForeground)
                    }
                }
                HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 10.dp))
                Text("Engine log", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
                if (diagnostics.isEmpty()) {
                    Text("No events yet.", fontSize = 11.sp, color = NssMutedForeground)
                } else {
                    diagnostics.take(16).forEach { line ->
                        Text(line, fontSize = 11.sp, color = NssMutedForeground)
                    }
                }
            }

            if (viewModel != null) {
                SettingsSavePanel(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SettingsSavePanel(viewModel: GameViewModel) {
    val feedback by viewModel.saveLoadFeedback.collectAsState()
    val hasSave by viewModel.hasSave.collectAsState()
    val slots = viewModel.listSaveSlots()
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text("SAVE / LOAD", fontWeight = FontWeight.Black, fontSize = 14.sp, color = NssForeground)
        Text(
            text = "Autosave plus three manual slots. Also available from Analytics.",
            fontSize = 11.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        )
        Text(
            text = "SAVE AUTOSAVE",
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(NssPrimary)
                .clickable { viewModel.saveGameProgress() }
                .padding(vertical = 12.dp),
            color = NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (hasSave) "LOAD AUTOSAVE" else "LOAD AUTOSAVE (NONE)",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(NssCardShape)
                .background(NssBorder)
                .clickable(enabled = hasSave) { viewModel.loadLastAutomatedSave() }
                .padding(vertical = 12.dp),
            color = NssForeground,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "MANUAL SLOTS",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = NssPrimary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
        )
        slots.forEach { slot ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "SAVE ${slot.slotIndex}",
                    modifier = Modifier
                        .weight(1f)
                        .clip(NssCardShape)
                        .background(NssPrimary.copy(alpha = 0.85f))
                        .clickable { viewModel.saveToSlot(slot.slotIndex) }
                        .padding(vertical = 10.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (slot.occupied) "LOAD ${slot.slotIndex}" else "EMPTY",
                    modifier = Modifier
                        .weight(1f)
                        .clip(NssCardShape)
                        .background(if (slot.occupied) NssBorder else NssBorder.copy(alpha = 0.4f))
                        .clickable(enabled = slot.occupied) { viewModel.loadFromSlot(slot.slotIndex) }
                        .padding(vertical = 10.dp),
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
            if (slot.occupied) {
                Text(
                    text = slot.label,
                    fontSize = 11.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text(
            text = feedback.message.ifBlank { "Ready." },
            fontSize = 12.sp,
            color = when {
                feedback.success -> NssEmerald
                feedback.message.contains("fail", ignoreCase = true) -> NssRed
                else -> NssMutedForeground
            },
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

private fun BgmTrack.displayLabel(): String = when (this) {
    BgmTrack.NONE -> "None"
    BgmTrack.PEACE -> "Peace"
    BgmTrack.WAR -> "War"
    BgmTrack.CRISIS -> "Crisis"
}

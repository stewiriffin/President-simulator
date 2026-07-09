package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.components.HeroHeaderScrim
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.viewmodel.SaveSlotInfo

@Composable
fun LaunchScreen(
    hasSave: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    slots: List<SaveSlotInfo> = emptyList(),
    onLoadSlot: ((Int) -> Unit)? = null,
) {
    val layout = rememberNssLayoutSpec()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        NssPhotoHeader(
            imageUrl = NssCardImages.MAP,
            fallbackGradient = listOf(NssPrimary, Color(0xFF0A0A0A)),
            modifier = Modifier.matchParentSize(),
            scrimTopToBottom = HeroHeaderScrim,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC1C1810),
                            Color.Transparent,
                            Color(0xCC1C1810),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "GLOBAL COMMAND INTERFACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = NssAccent,
                letterSpacing = 4.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NATION STATE",
                fontFamily = FontFamily.Serif,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = NssOnPhoto,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "SIMULATOR",
                fontFamily = FontFamily.Serif,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = NssOnPhoto,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(if (layout.isNarrowWidth) 0.92f else 0.72f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (hasSave) {
                    LaunchActionButton(
                        label = "RESUME CAMPAIGN",
                        icon = Icons.Default.PlayArrow,
                        primary = true,
                        onClick = onContinueGame,
                    )
                }
                LaunchActionButton(
                    label = "NEW CAMPAIGN",
                    icon = Icons.Default.LocalFireDepartment,
                    primary = !hasSave,
                    onClick = onNewGame,
                )
                val occupiedSlots = slots.filter { it.occupied }
                if (occupiedSlots.isNotEmpty() && onLoadSlot != null) {
                    occupiedSlots.forEach { slot ->
                        LaunchActionButton(
                            label = slot.label.ifBlank { "LOAD SLOT ${slot.slotIndex}" }.uppercase(),
                            icon = Icons.Default.Settings,
                            primary = false,
                            onClick = { onLoadSlot(slot.slotIndex) },
                        )
                    }
                }
            }
        }

        Text(
            text = "v1.4.0_BETA // SECURE CONNECTION ESTABLISHED",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            fontSize = 9.sp,
            color = NssMutedForeground,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LaunchActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val shape = NssCardShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (primary) {
                    Brush.horizontalGradient(listOf(NssAccent, Color(0xFFD97706)))
                } else {
                    Brush.horizontalGradient(
                        listOf(NssPrimary.copy(alpha = 0.6f), NssPrimary.copy(alpha = 0.45f)),
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = Dimens.SpacingMedium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (primary) NssOnPhoto else Color(0xFFD4C8A8), modifier = Modifier.padding(end = 8.dp))
        Text(
            text = label,
            color = if (primary) NssOnPhoto else Color(0xFFD4C8A8),
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
    }
}

package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.components.NssCardShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.viewmodel.SaveSlotInfo

@Composable
fun LaunchScreen(
    hasSave: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    slots: List<SaveSlotInfo> = emptyList(),
    onLoadSlot: ((Int) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(Dimens.SpacingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "PRESIDENT",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = NssPrimary,
            letterSpacing = 2.sp,
        )
        Text(
            text = "SIMULATOR",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = NssForeground,
            letterSpacing = 2.sp,
        )

        Spacer(modifier = Modifier.height(64.dp))

        if (hasSave) {
            Text(
                text = "Continue Game",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(NssPrimary)
                    .clickable { onContinueGame() }
                    .padding(vertical = 16.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "New Game",
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardShape)
                .background(if (hasSave) NssPrimary.copy(alpha = 0.2f) else NssPrimary)
                .clickable { onNewGame() }
                .padding(vertical = 16.dp),
            color = if (hasSave) NssPrimary else NssOnPhoto,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )

        val occupiedSlots = slots.filter { it.occupied }
        if (occupiedSlots.isNotEmpty() && onLoadSlot != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "LOAD SLOT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            occupiedSlots.forEach { slot ->
                Text(
                    text = slot.label.ifBlank { "Slot ${slot.slotIndex}" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(NssCardShape)
                        .background(NssBorder)
                        .clickable { onLoadSlot(slot.slotIndex) }
                        .padding(vertical = 12.dp),
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

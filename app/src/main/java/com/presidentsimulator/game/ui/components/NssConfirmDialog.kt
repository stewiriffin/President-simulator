package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary

@Composable
fun NssConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    dismissLabel: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingMedium)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(Dimens.SpacingLarge),
        ) {
            Text(
                text = title.uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = NssPrimary,
                letterSpacing = 1.sp,
            )
            NssPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingMedium),
            ) {
                Text(
                    text = body,
                    fontSize = 13.sp,
                    color = NssMutedForeground,
                    lineHeight = 18.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
            ) {
                Text(
                    text = dismissLabel,
                    modifier = Modifier
                        .weight(1f)
                        .clip(NssCardShape)
                        .background(NssPrimary.copy(alpha = 0.15f))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    color = NssPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = confirmLabel,
                    modifier = Modifier
                        .weight(1f)
                        .clip(NssCardShape)
                        .background(NssAccent)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 12.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

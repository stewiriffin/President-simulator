package com.presidentsimulator.game.ui.components.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.PhotoHeaderBottomScrim

/**
 * Wide thematic banner for crisis events.
 *
 * Prefers bundled `R.drawable.event_<type>` assets; otherwise loads a themed Unsplash photo.
 */
@Composable
fun EventIllustration(
    eventType: String,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
) {
    val context = LocalContext.current
    val normalized = remember(eventType) {
        eventType.trim().lowercase()
            .replace('-', '_')
            .replace(' ', '_')
            .replace(Regex("[^a-z0-9_]"), "")
    }
    val drawableName = remember(normalized) { "event_$normalized" }
    val resId = remember(drawableName, context.packageName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    val shape = RoundedCornerShape(12.dp)
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Event illustration: $eventType",
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape),
            contentScale = ContentScale.Crop,
        )
    } else {
        NssPhotoHeader(
            imageUrl = NssCardImages.eventImage(eventType),
            fallbackGradient = NssGradients.Indigo,
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape),
            scrimTopToBottom = PhotoHeaderBottomScrim,
        )
    }
}

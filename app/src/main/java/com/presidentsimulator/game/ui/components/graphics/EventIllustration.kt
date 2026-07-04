package com.presidentsimulator.game.ui.components.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.ui.theme.GameIcons
import com.presidentsimulator.game.ui.theme.NavySurface
import com.presidentsimulator.game.ui.theme.NeutralGray
import com.presidentsimulator.game.ui.theme.WarningOrange

/**
 * Wide thematic banner for crisis events.
 *
 * Loads `R.drawable.event_<type>` (e.g. `event_epidemic` for `"epidemic"`).
 * Missing drawables fall back to a dark panel with a Material icon from [GameIcons].
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
        EventIllustrationFallback(
            eventType = eventType,
            height = height,
            modifier = modifier,
        )
    }
}

@Composable
private fun EventIllustrationFallback(
    eventType: String,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = GameIcons.forEventType(eventType),
            contentDescription = eventType,
            tint = when {
                eventType.contains("epidemic", ignoreCase = true) -> NeutralGray
                eventType.contains("boom", ignoreCase = true) -> WarningOrange
                else -> WarningOrange
            },
            modifier = Modifier.height(height * 0.45f),
        )
    }
}

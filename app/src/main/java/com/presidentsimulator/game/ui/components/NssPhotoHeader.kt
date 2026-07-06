package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssForeground

/**
 * Photo header for NSS cards — loads a remote image with gradient fallback and overlay scrim.
 */
@Composable
fun NssPhotoHeader(
    imageUrl: String?,
    fallbackGradient: List<Color>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    tintGradient: List<Color>? = null,
    scrimTopToBottom: List<Color> = listOf(Color.Transparent, NssCard),
    scrimLeftToRight: List<Color>? = null,
) {
    Box(modifier = modifier) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(fallbackGradient)))
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(fallbackGradient)))
                },
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(fallbackGradient)))
        }

        tintGradient?.let { colors ->
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        }

        scrimLeftToRight?.let { colors ->
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors)))
        }

        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(scrimTopToBottom)))
    }
}

val CardHeaderBottomScrim = listOf(
    Color.Transparent,
    NssForeground.copy(alpha = 0.62f),
)

/** Ministry banner scrims — parchment wash for navy titles on photos */
val MinistryBannerLeftScrim = listOf(
    NssBackground.copy(alpha = 0.92f),
    NssBackground.copy(alpha = 0.55f),
    Color.Transparent,
)

val MinistryBannerBottomScrim = listOf(
    Color.Transparent,
    NssBackground.copy(alpha = 0.35f),
)

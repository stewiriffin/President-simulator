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
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
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
    scrimTopToBottom: List<Color> = PhotoHeaderBottomScrim,
    scrimLeftToRight: List<Color>? = null,
) {
    Box(modifier = modifier) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(fallbackGradient)))
                },
                success = {
                    SubcomposeAsyncImageContent()
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

        if (scrimTopToBottom.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(scrimTopToBottom)))
        }
    }
}

/** Darkens the bottom edge so titles stay readable while the photo stays visible above. */
val PhotoHeaderBottomScrim = listOf(
    Color.Transparent,
    Color.Black.copy(alpha = 0.18f),
    Color.Black.copy(alpha = 0.72f),
)

/** Card headers — matches v3 `from-black/80 via-black/20 to-transparent`. */
val CardHeaderBottomScrim = listOf(
    Color.Transparent,
    Color.Black.copy(alpha = 0.2f),
    Color.Black.copy(alpha = 0.78f),
)

/** Ministry banner scrims — light parchment wash so photos read through on the left. */
val MinistryBannerLeftScrim = listOf(
    NssBackground.copy(alpha = 0.72f),
    NssBackground.copy(alpha = 0.35f),
    Color.Transparent,
)

val MinistryBannerBottomScrim = listOf(
    Color.Transparent,
    NssBackground.copy(alpha = 0.25f),
)

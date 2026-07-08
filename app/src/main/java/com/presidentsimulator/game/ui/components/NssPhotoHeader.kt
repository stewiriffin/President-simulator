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
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.PhotoScrimAlpha

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

/** Default card/hero bottom scrim — keeps white titles readable (~WCAG AA on photos). */
val PhotoHeaderBottomScrim = listOf(
    Color.Transparent,
    Color.Black.copy(alpha = PhotoScrimAlpha.Soft),
    Color.Black.copy(alpha = PhotoScrimAlpha.Strong),
)

/** Card image headers across feature categories share these alphas. */
val CardHeaderBottomScrim = PhotoHeaderBottomScrim

/** Full-width ministry screen header: navy wash for title + stat pills. */
val ScreenHeaderScrim = listOf(
    NssPrimary.copy(alpha = PhotoScrimAlpha.ScreenHeaderTop),
    Color.Transparent,
    NssPrimary.copy(alpha = PhotoScrimAlpha.ScreenHeaderBottom),
)

/** Dashboard / map hero: soft top brand wash, strong bottom for centered title. */
val HeroHeaderScrim = listOf(
    NssPrimary.copy(alpha = PhotoScrimAlpha.Medium),
    Color.Transparent,
    NssBackground.copy(alpha = PhotoScrimAlpha.Max),
)

/** Compact strip headers under titles (secondary overlays). */
val StripHeaderBottomScrim = listOf(
    Color.Transparent,
    NssPrimary.copy(alpha = PhotoScrimAlpha.Strong),
)

/** Ministry banner left parchment wash so copy stays legible over photography. */
val MinistryBannerLeftScrim = listOf(
    NssBackground.copy(alpha = PhotoScrimAlpha.BannerLeftStrong),
    NssBackground.copy(alpha = PhotoScrimAlpha.BannerLeftMid),
    Color.Transparent,
)

val MinistryBannerBottomScrim = listOf(
    Color.Transparent,
    NssBackground.copy(alpha = PhotoScrimAlpha.Soft),
)

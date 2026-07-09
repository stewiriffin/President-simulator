package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import coil.compose.AsyncImage
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.components.HeroHeaderScrim
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary

@Composable
fun CountrySelectScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onSelectCountry: (String) -> Unit,
) {
    var selectedIndex by remember(nations) { mutableIntStateOf(0) }
    val nation = nations.getOrElse(selectedIndex) { nations.first() }
    val layout = rememberNssLayoutSpec()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingMedium, vertical = Dimens.SpacingSmall)
                .border(width = 0.dp, color = Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onBack),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NssMutedForeground, modifier = Modifier.size(16.dp))
                Text("BACK", color = NssMutedForeground, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
            Text(
                text = "SELECT NATION",
                color = NssAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layout.countrySelectHeroHeight)
                    .clip(NssCardShape)
                    .border(1.dp, Color(0x4DD4C8A8), NssCardShape),
            ) {
                NssPhotoHeader(
                    imageUrl = nation.leaderImageUrl,
                    fallbackGradient = listOf(NssPrimary, NssBackground),
                    modifier = Modifier.matchParentSize(),
                    scrimTopToBottom = HeroHeaderScrim,
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xE61C1810)),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Dimens.SpacingMedium),
                ) {
                    Text(
                        text = nation.governmentLabel.uppercase(),
                        color = Color(0xFFD4C8A8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = nation.name.uppercase(),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = if (layout.isCompactHeight) 26.sp else 32.sp,
                        color = NssOnPhoto,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(NssPrimary.copy(alpha = 0.4f), NssPrimary.copy(alpha = 0.2f)),
                        ),
                    )
                    .border(1.dp, NssPrimary.copy(alpha = 0.8f), NssCardShape)
                    .padding(Dimens.SpacingMedium),
            ) {
                Text(
                    text = "NATIONAL PERK",
                    color = NssAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                    Text(nation.nationalPerk, color = NssOnPhoto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                nations.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(width = 72.dp, height = 80.dp)
                            .clip(NssCardShape)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) NssAccent else Color(0x33FFFFFF),
                                shape = NssCardShape,
                            )
                            .clickable { selectedIndex = index },
                    ) {
                        AsyncImage(
                            model = item.leaderImageUrl,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC000000)),
                                    ),
                                ),
                        )
                        Text(
                            text = item.name.uppercase(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp),
                            color = NssOnPhoto,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(72.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, NssBackground, NssBackground),
                    ),
                )
                .padding(Dimens.SpacingMedium),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(Brush.horizontalGradient(listOf(NssAccent, Color(0xFFD97706))))
                    .clickable { onSelectCountry(nation.id) }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "INITIATE SEQUENCE",
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                )
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = NssOnPhoto,
                    modifier = Modifier.padding(start = 8.dp).size(18.dp),
                )
            }
        }
    }
}

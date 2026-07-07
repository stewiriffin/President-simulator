package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssDestructive
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssIndigo
import com.presidentsimulator.game.ui.theme.NssMuted
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOrange
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.theme.NssSky
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.StarkWhite

val NssCardShape = RoundedCornerShape(16.dp)

/** Sector colors for GDP breakdown bar (v3 reference). */
val NssSectorBarColors = listOf(
    Color(0xFF3B82F6),
    Color(0xFF16A34A),
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFD97706),
    Color(0xFFF97316),
    Color(0xFFDC2626),
)

@Composable
fun NssGameBar(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier,
    thick: Boolean = false,
    animate: Boolean = true,
    animationDelayMs: Int = 0,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(animate) {
        if (animate) {
            kotlinx.coroutines.delay(animationDelayMs.toLong())
            started = true
        } else {
            started = true
        }
    }
    val animatedPct by animateFloatAsState(
        targetValue = if (started) percent.coerceIn(0f, 100f) else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "gameBar",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (thick) 12.dp else 8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF5F5F4)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPct / 100f)
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

@Composable
fun NssXpBar(
    percent: Float,
    modifier: Modifier = Modifier,
    animationDelayMs: Int = 0,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelayMs.toLong())
        started = true
    }
    val animatedPct by animateFloatAsState(
        targetValue = if (started) percent.coerceIn(0f, 100f) else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "xpBar",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFEF3C7)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPct / 100f)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(listOf(NssAccent, Color(0xFFFBBF24))),
                ),
        )
    }
}

@Composable
fun NssLvBadge(level: Int, modifier: Modifier = Modifier) {
    Text(
        text = "LV.$level",
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NssPrimary)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = NssOnPhoto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
    )
}

@Composable
fun NssScreenHeader(
    title: String,
    imageUrl: String?,
    statPills: List<Pair<String, String>>,
    gradientColors: List<Color> = listOf(NssPrimary, NssPrimary.copy(alpha = 0.7f)),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = gradientColors,
            modifier = Modifier.matchParentSize(),
            scrimTopToBottom = listOf(
                NssPrimary.copy(alpha = 0.55f),
                Color.Transparent,
                NssPrimary.copy(alpha = 0.72f),
            ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = "MINISTRY OF",
                style = MaterialTheme.typography.labelSmall,
                color = NssOnPhoto.copy(alpha = 0.6f),
                letterSpacing = 4.sp,
            )
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = NssOnPhoto,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statPills.take(3).forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NssOnPhoto.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(text = label, fontSize = 9.sp, color = NssOnPhoto.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
                        Text(text = value, fontSize = 13.sp, color = NssOnPhoto, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun NssPanel(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .then(
                if (highlighted) Modifier.border(2.dp, NssAccent.copy(alpha = 0.4f), NssCardShape)
                else Modifier,
            )
            .background(NssGameCard)
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun NssCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .border(1.dp, NssBorder, NssCardShape)
            .background(NssCard)
            .padding(16.dp),
    ) {
        content()
    }
}

@Composable
fun NssSectionHead(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = NssForeground)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun NssBadge(
    label: String,
    large: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = badgeColors(label)
    Text(
        text = label.uppercase(),
        modifier = modifier
            .border(1.dp, colors.border)
            .background(colors.background)
            .padding(horizontal = if (large) 8.dp else 6.dp, vertical = if (large) 2.dp else 1.dp),
        color = colors.text,
        fontSize = if (large) 10.sp else 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun NssProgressBar(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier,
    thick: Boolean = false,
) {
    val clamped = percent.coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (thick) 8.dp else 3.dp)
            .background(NssSecondary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped / 100f)
                .background(color),
        )
    }
}

@Composable
fun NssTabBar(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NssMuted),
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 11.sp,
                            color = if (selected) NssPrimary else NssMutedForeground,
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(1.dp)
                                    .background(NssPrimary),
                            )
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NssBorder))
    }
}

@Composable
fun NssStars(
    count: Int,
    max: Int = 5,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(max) { index ->
            Icon(
                imageVector = if (index < count) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (index < count) NssAccent else NssBorder,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
fun NssMinistryBanner(
    ministryLabel: String,
    statPills: List<String>,
    imageUrl: String? = null,
    gradientColors: List<Color> = listOf(NssBackground, NssSecondary, NssPrimary.copy(alpha = 0.2f)),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = gradientColors,
            modifier = Modifier.matchParentSize(),
            scrimLeftToRight = MinistryBannerLeftScrim,
            scrimTopToBottom = MinistryBannerBottomScrim,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MINISTRY OF",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssPrimary.copy(alpha = 0.7f),
                    letterSpacing = 4.sp,
                )
                Text(
                    text = ministryLabel.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NssPrimary,
                    letterSpacing = 2.sp,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statPills.take(3).forEach { pill ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StarkWhite.copy(alpha = 0.82f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = pill,
                            color = NssForeground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NssCompactKpi(
    label: String,
    value: String,
    delta: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
        Text(
            text = value,
            color = NssForeground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        Text(
            text = delta,
            fontSize = 10.sp,
            color = if (positive) NssEmerald else NssRed,
        )
    }
}

@Composable
fun NssStripPhotoCard(
    imageUrl: String?,
    fallbackGradient: List<Color> = listOf(NssSecondary, NssCard),
    modifier: Modifier = Modifier,
    headerHeight: Dp = 72.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = fallbackGradient,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),
            scrimTopToBottom = listOf(Color.Transparent, NssPrimary.copy(alpha = 0.65f)),
        )
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun NssSectorCard(
    name: String,
    gdpShare: Float,
    employment: Float,
    growth: Float,
    level: Int,
    headerGradient: List<Color>,
    imageUrl: String? = null,
    xpPercent: Int = (level * 20).coerceIn(10, 95),
    revenueLabel: String? = null,
    onInvest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var invested by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(NssGameCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(128.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssLvBadge(
                level = level,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            )
            Text(
                text = "${if (growth >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(growth))}%",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (growth >= 0) NssEmerald else NssRed)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = NssOnPhoto,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(
                    text = name,
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                )
                NssStars(count = level)
            }
        }
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("GDP Share", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, fontWeight = FontWeight.Bold)
                Text(
                    "${"%.1f".format(gdpShare)}%",
                    color = NssForeground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("XP to Level ${level + 1}", fontSize = 9.sp, color = NssMutedForeground, fontWeight = FontWeight.Bold)
                Text("$xpPercent%", fontSize = 9.sp, color = NssMutedForeground, fontWeight = FontWeight.Bold)
            }
            NssXpBar(percent = xpPercent.toFloat())
            if (revenueLabel != null) {
                Text(
                    text = "💰 $revenueLabel",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NssMutedForeground,
                )
            }
        }
        Text(
            text = if (invested) "✓ Investing" else "⬆ Invest",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    invested = !invested
                    onInvest()
                }
                .background(if (invested) NssEmerald else NssAccent)
                .padding(vertical = 10.dp),
            color = NssOnPhoto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NssBranchHeader(
    branch: String,
    unitCount: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        Text(
            text = "$branch BRANCH",
            color = accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(NssBorder))
        Text(
            text = "$unitCount UNITS",
            style = MaterialTheme.typography.labelSmall,
            color = NssMutedForeground,
        )
    }
}

@Composable
fun NssUnitCard(
    unitName: String,
    branch: String,
    count: Int,
    strength: Int,
    status: String,
    maintLabel: String,
    headerGradient: List<Color>,
    accentColor: Color,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssBadge(label = branch, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            Text(
                text = "×$count",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(NssForeground.copy(alpha = 0.72f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Text(
                text = unitName,
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("STRENGTH", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.width(56.dp))
                NssProgressBar(percent = strength.toFloat(), color = strengthBarColor(strength), thick = true, modifier = Modifier.weight(1f))
                Text(
                    text = "$strength%",
                    color = strengthTextColor(strength),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NssBadge(label = status)
                Text(maintLabel, style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().border(width = 0.dp, color = Color.Transparent)) {
            Text(
                text = "REDEPLOY",
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                color = NssPrimary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(NssBorder))
            Text(
                text = "UPGRADE ▸",
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                color = NssAmber,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun NssRecruitCard(
    name: String,
    branch: String,
    costLabel: String,
    buildMonths: Int,
    maintLabel: String,
    quantity: Int,
    headerGradient: List<Color>,
    accentColor: Color,
    imageUrl: String? = null,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (quantity > 0) NssPrimary.copy(alpha = 0.6f) else NssBorder,
            )
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssBadge(label = branch, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            if (quantity > 0) {
                Text(
                    text = "×$quantity QUEUED",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(NssPrimary)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(name, color = NssOnPhoto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(costLabel, color = NssOnPhoto.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${buildMonths}mo build", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(maintLabel, style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
            if (quantity > 0) {
                Text(
                    text = "SUBTOTAL: $costLabel",
                    color = NssPrimary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NssSecondary)
                        .clickable { onQuantityChange(-1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = NssForeground, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = quantity.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NssBorder.copy(alpha = 0.5f))
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NssPrimary)
                        .clickable { onQuantityChange(1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun NssNationCard(
    nationName: String,
    flagEmoji: String,
    status: String,
    threat: String,
    relations: Int,
    tradeLabel: String,
    militaryLabel: String,
    headerColor: Color,
    isHostile: Boolean,
    onAction: () -> Unit,
    imageUrl: String? = null,
    headerGradient: List<Color>? = null,
    modifier: Modifier = Modifier,
) {
    val gradient = headerGradient ?: listOf(headerColor, headerColor.copy(alpha = 0.65f))
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (isHostile) NssRed.copy(alpha = 0.5f) else NssBorder,
            )
            .background(NssCard)
            .clickable(onClick = onAction),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = gradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            Text(text = flagEmoji, fontSize = 48.sp)
            NssBadge(label = status, large = true, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            NssBadge(label = threat, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
            Text(
                text = nationName,
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("RELATIONS", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.width(64.dp))
                NssProgressBar(percent = relations.toFloat(), color = relationBarColor(relations), thick = true, modifier = Modifier.weight(1f))
                Text(
                    text = relations.toString(),
                    color = relationTextColor(relations),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TRADE", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(tradeLabel, style = MaterialTheme.typography.labelSmall, color = NssForeground)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MILITARY", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(militaryLabel, style = MaterialTheme.typography.labelSmall, color = NssForeground)
            }
        }
        Text(
            text = if (isHostile) "ESCALATE / NEGOTIATE ▸" else "OPEN DIALOGUE ▸",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            color = if (isHostile) NssRed else NssPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NssKpiCard(
    label: String,
    value: String,
    delta: String,
    positive: Boolean?,
    modifier: Modifier = Modifier,
) {
    NssCard(modifier = modifier) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelMedium, color = NssMutedForeground)
        Text(text = value, color = NssForeground, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = delta,
            style = MaterialTheme.typography.bodySmall,
            color = when (positive) {
                true -> NssEmerald
                false -> NssRed
                null -> NssMutedForeground
            },
        )
    }
}

@Composable
fun NssAlertBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NssAmber.copy(alpha = 0.3f))
            .background(NssAmber.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "⚠", color = NssAmber, fontSize = 14.sp)
        Text(text = message.uppercase(), style = MaterialTheme.typography.labelMedium, color = NssAmber)
    }
}

@Composable
fun NssHeroBanner(
    ministryLabel: String,
    stats: List<HeroStat>,
    accentColor: Color = NssPrimary,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    NssMinistryBanner(
        ministryLabel = ministryLabel,
        statPills = stats.map { "${it.label}: ${it.value}" },
        imageUrl = imageUrl,
        gradientColors = listOf(NssBackground, NssSecondary, accentColor.copy(alpha = 0.35f)),
        modifier = modifier,
    )
}

data class HeroStat(val label: String, val value: String, val positive: Boolean? = null)

private data class BadgePalette(val background: Color, val text: Color, val border: Color)

private fun badgeColors(label: String): BadgePalette {
    val upper = label.uppercase()
    return when {
        listOf("ALLY", "ALLIED", "ACTIVE", "LOW", "OPEN", "COMBAT READY", "PARTNER", "READY").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFDCFCE7), Color(0xFF166534), Color(0xFF86EFAC))
        listOf("PATROL", "ADVANCED", "FINAL", "INFO", "MEDIUM").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFDBEAFE), Color(0xFF1E40AF), Color(0xFF93C5FD))
        listOf("NEUTRAL", "TRAINING", "REFIT", "STALLED", "WARN", "REVIEW", "NONE").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFF5F5F4), Color(0xFF57534E), Color(0xFFD6D3D1))
        listOf("RIVAL", "HIGH", "RESTRICTED", "STANDOFF", "WARNING").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFFEF3C7), Color(0xFF92400E), Color(0xFFFCD34D))
        listOf("HOSTILE", "CRITICAL", "CRIT", "EMBARGO", "CONFLICT", "CRISIS").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFFEE2E2), Color(0xFF991B1B), Color(0xFFFCA5A5))
        else ->
            BadgePalette(Color(0xFFF5F5F4), Color(0xFF57534E), Color(0xFFD6D3D1))
    }
}

fun relationBarColor(value: Int): Color = when {
    value >= 70 -> NssEmerald
    value >= 40 -> NssAmber
    else -> NssRed
}

fun relationTextColor(value: Int): Color = when {
    value >= 70 -> NssEmerald
    value >= 40 -> NssAmber
    else -> NssRed
}

fun strengthBarColor(value: Int): Color = when {
    value >= 90 -> NssEmerald
    value >= 75 -> NssAmber
    else -> NssRed
}

fun strengthTextColor(value: Int): Color = relationTextColor(value)

fun prgColor(value: Int): Color = when {
    value >= 80 -> NssEmerald
    value >= 50 -> NssSky
    value >= 25 -> NssAmber
    else -> NssRed
}

object NssGradients {
    val Emerald = listOf(Color(0xFFD8F0E4), Color(0xFF9FD4B8), NssCard)
    val Sky = listOf(Color(0xFFD4E0F0), Color(0xFF9BB4D8), NssCard)
    val Indigo = listOf(Color(0xFFD4DCF0), Color(0xFF9BAED8), NssCard)
    val Violet = listOf(Color(0xFFE8DDF5), Color(0xFFC4A8E0), NssCard)
    val Amber = listOf(Color(0xFFF5E6C8), Color(0xFFE8C878), NssCard)
    val Orange = listOf(Color(0xFFF5DDD0), Color(0xFFE8B088), NssCard)
    val Red = listOf(Color(0xFFF5D5D5), Color(0xFFE09898), NssCard)
    val Neutral = listOf(NssSecondary, NssBorder, NssCard)
    val Economy = listOf(NssBackground, Color(0xFFE8DFC8), NssPrimary.copy(alpha = 0.15f))
    val Defense = listOf(NssBackground, Color(0xFFE8D8D0), NssRed.copy(alpha = 0.12f))
    val Foreign = listOf(NssBackground, Color(0xFFD8E0F0), NssPrimary.copy(alpha = 0.12f))
}

object NssNationColors {
    val Ally = Color(0xFFBFDBFE)
    val Partner = Color(0xFFD1FAE5)
    val Neutral = Color(0xFFE7E5E4)
    val Rival = Color(0xFFFDE68A)
    val Hostile = Color(0xFFFECACA)
}

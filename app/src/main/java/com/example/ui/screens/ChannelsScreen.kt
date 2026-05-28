package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.viewmodel.AcademicVideo
import kotlin.math.sin
import kotlin.math.cos
import com.example.viewmodel.ScienceViewModel

@Composable
fun ChannelsScreen(
    viewModel: ScienceViewModel,
    modifier: Modifier = Modifier
) {
    val videoList by viewModel.videoList.collectAsState()
    val activeVideo by viewModel.activeVideoForPlayback.collectAsState()
    
    // Automatically progress fake video player timeline if isPlaying
    LaunchedEffect(viewModel.isPlaying.value) {
        if (viewModel.isPlaying.value) {
            while (viewModel.isPlaying.value) {
                viewModel.videoProgress.value = (viewModel.videoProgress.value + 0.012f)
                if (viewModel.videoProgress.value >= 1.0f) {
                    viewModel.videoProgress.value = 0f // loop
                }
                kotlinx.coroutines.delay(400)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Screen Header Display
            Text(
                text = "کانال‌ها و محتوای ویدیوئی معتبر",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "دسترسی آزاد به بهترین متدهای تدریس اساتید ایران",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // TOP PIN: Custom Interactive Video Player Simulator
            AnimatedContent(
                targetState = activeVideo,
                transitionSpec = {
                    slideInVertically() + fadeIn() togetherWith slideOutVertically() + fadeOut()
                },
                label = "video_player_anim"
            ) { targetVideo ->
                if (targetVideo != null) {
                    SimulatedVideoPlayer(
                        video = targetVideo,
                        isPlaying = viewModel.isPlaying.value,
                        progress = viewModel.videoProgress.value,
                        resolution = viewModel.videoResolution.value,
                        onPlayClick = { viewModel.isPlaying.value = !viewModel.isPlaying.value },
                        onProgressChange = { viewModel.videoProgress.value = it },
                        onClosePlayer = { viewModel.selectVideoForPlayback(null) }
                    )
                } else {
                    // Video Channels Promo Banner
                    PromoChannelsBanner()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Channels List Header
            Text(
                text = "برگزیده جلسات درسی تدریسی",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Video playlist list
            LazyColumn(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .testTag("video_playlist_column"),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(videoList) { video ->
                    val isActivePlay = activeVideo?.id == video.id
                    VideoListItem(
                        video = video,
                        isActive = isActivePlay,
                        onClick = { viewModel.selectVideoForPlayback(video) }
                    )
                }
            }
        }
    }
}

@Composable
fun SimulatedVideoPlayer(
    video: AcademicVideo,
    isPlaying: Boolean,
    progress: Float,
    resolution: String,
    onPlayClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color.White.copy(alpha = 0.25f),
                RoundedCornerShape(20.dp)
            )
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Simulated Video Display Area with beautiful visual graphics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                LiquidIndigo.copy(alpha = 0.9f),
                                GlassSlateDark.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                // Background visual equalizer waves or orbits
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw flowing video waves inside player
                    val wavePath1 = Path()
                    val wavePath2 = Path()
                    
                    wavePath1.moveTo(0f, h * 0.5f)
                    wavePath2.moveTo(0f, h * 0.61f)
                    
                    for (x in 0..w.toInt() step 20) {
                        val radians = x * 0.008726646f + progress * 6.2831855f
                        val y1 = h * 0.5f + (15.dp.toPx() * sin(radians))
                        val y2 = h * 0.61f + (22.dp.toPx() * cos(radians))
                        
                        wavePath1.lineTo(x.toFloat(), y1)
                        wavePath2.lineTo(x.toFloat(), y2)
                    }
                    
                    drawPath(
                        path = wavePath1,
                        color = NeonCyan.copy(alpha = 0.2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawPath(
                        path = wavePath2,
                        color = NeonPurple.copy(alpha = 0.15f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Close Button Top-Right
                IconButton(
                    onClick = onClosePlayer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن ویدیوپلیر",
                        tint = Color.White
                    )
                }

                // Simulated Subtitles / captions Track based on elapsed time progress
                val simulatedCaption = when {
                    progress < 0.25f -> "استاد: سلام دانش‌آموزان عزیز، امروز آزمایش ساختار درونی اتم بور داریم..."
                    progress < 0.50f -> "استاد: به این پیوند هیدروژنی متراکم مولکولی خوب نگاه کنید..."
                    progress < 0.75f -> "استاد: حالا اسید را اضافه کرده و فوران گاز را محاسبه میکنیم..."
                    else -> "استاد: فرمول نهایی جریان الکتریکی برابر است با ولتاژ تقسیم بر مقاومتها..."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = simulatedCaption,
                        fontSize = 11.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // High fidelity play-paused overlay floating indicator icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .clickable { onPlayClick() }
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "کنترل پخش",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // HD Badge Bottom Left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = resolution,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }


            // Controls Seek Slider and title details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Title
                Text(
                    text = video.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // stats label
                Text(
                    text = "${video.channelName} • ${video.views}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Slider for Timeline seek
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "0:00",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    
                    Slider(
                        value = progress,
                        onValueChange = onProgressChange,
                        modifier = Modifier.weight(1.0f).testTag("video_seek_bar"),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    Text(
                        text = video.duration,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun PromoChannelsBanner() {
    GlassyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "ویدیو پلیر",
                        tint = NeonPurple
                    )
                }
                Column {
                    Text(
                        text = "تلویزیون هوشمند صوتی تجربی",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "جهت استارت کافیست روی ویدیوهای برگزیده کلیک کنید",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoListItem(
    video: AcademicVideo,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val playBorderColor = if (isActive) NeonCyan else Color.White.copy(alpha = 0.12f)
    val playBgColor = if (isActive) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(playBgColor)
            .border(1.dp, playBorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("video_item_${video.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Play Icon Indicator backplane
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                    contentDescription = "آزمایشگاه ویدیوئی",
                    tint = if (isActive) NeonCyan else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = video.category,
                        fontSize = 10.sp,
                        color = if (isActive) NeonCyan else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = video.duration,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = video.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) NeonCyan else Color.White,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = video.description,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

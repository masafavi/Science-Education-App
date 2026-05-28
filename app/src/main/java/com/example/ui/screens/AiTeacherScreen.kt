package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.*
import com.example.viewmodel.ScienceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTeacherScreen(
    viewModel: ScienceViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Automatically scroll to the latest message
    LaunchedEffect(chatMessages.size, isGenerating) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val presets = listOf(
        "چرا برگ درختان سبز است؟",
        "فرمول جادویی مدار اهم چیه؟",
        "مدل اتمی بور رو خلاصه بگو"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Screen Header Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "هوش مصنوعی معلمان",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "پاسخگویی لحظه‌ای استاد خلاق علوم به سوالات شما",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Delete chats history button
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("clear_chat_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "پاک کردن پیام‌ها",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat Scroll List Area
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .testTag("chat_messages_column"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(chatMessages) { msg ->
                    BubbleChatMessage(message = msg)
                }

                // Generative progress indicator
                if (isGenerating) {
                    item {
                        BubbleAiTeacherGenerating()
                    }
                }
            }

            // Presets row shown when chat text is clean
            if (textInput.trim().isEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("presets_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { preset: String ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.askTeacher(preset)
                                }
                                .background(GlassWhitebg)
                                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Input Send Field capsule inside Liquid Glass style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Glass Textfield backplane
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "سوال خود از استاد بپرسید...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .testTag("chat_text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassWhitebg,
                        unfocusedContainerColor = GlassWhitebg.copy(alpha = 0.03f),
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassWhiteBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(27.dp),
                    singleLine = true
                )

                // High contrast send button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (textInput.trim().isNotEmpty()) {
                                viewModel.askTeacher(textInput)
                                textInput = ""
                            }
                        }
                        .background(Brush.horizontalGradient(listOf(LiquidTeal, NeonCyan)))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "فرستادن سوال",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BubbleChatMessage(message: ChatMessage) {
    val isTeacher = message.sender == "teacher"
    val alignment = if (isTeacher) Alignment.Start else Alignment.End
    
    val bubbleBg = if (isTeacher) {
        Brush.horizontalGradient(
            listOf(
                Color.White.copy(alpha = 0.09f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                LiquidTeal.copy(alpha = 0.75f),
                LiquidTeal.copy(alpha = 0.5f)
            )
        )
    }

    val bubbleBorder = if (isTeacher) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.sender}"),
        horizontalAlignment = alignment
    ) {
        // Sender Badge Label
        Text(
            text = if (isTeacher) "استاد دانشمند" else "شما (دانش‌آموز)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTeacher) NeonCyan else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isTeacher) 4.dp else 20.dp,
                        bottomEnd = if (isTeacher) 20.dp else 4.dp
                    )
                )
                .background(bubbleBg)
                .border(
                    1.dp,
                    bubbleBorder,
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isTeacher) 4.dp else 20.dp,
                        bottomEnd = if (isTeacher) 20.dp else 4.dp
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = Color.White,
                textAlign = if (isTeacher) TextAlign.Justify else TextAlign.Right
            )
        }
    }
}

@Composable
fun BubbleAiTeacherGenerating() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_generating"),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "استاد در حال تفکر...",
            fontSize = 11.sp,
            color = NeonCyan,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = dot1Alpha))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = dot2Alpha))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = dot3Alpha))
                )
            }
        }
    }
}

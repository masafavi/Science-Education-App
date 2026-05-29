package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.viewmodel.AppTab
import com.example.viewmodel.LessonCategory
import com.example.viewmodel.LessonTopic
import com.example.viewmodel.ScienceViewModel

import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ScienceViewModel,
    authViewModel: AuthViewModel? = null,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val currentUser by (authViewModel?.currentUser ?: MutableStateFlow(null)).collectAsState()
    
    val lessons = remember(selectedCategory) {
        if (selectedCategory == LessonCategory.ALL) {
            viewModel.lessonTopics
        } else {
            viewModel.lessonTopics.filter { it.category == selectedCategory }
        }
    }

    // Force RTL local layout direction for Persian support
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header Display
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SCIENCE PLUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row {
                            Text(
                                text = "کاوش در ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "علوم",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                    }
                    // Gamification Dashboard (Duolingo Style)
                    if (currentUser != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Streak
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser?.streak ?: 0}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF97316),
                                    fontSize = 14.sp
                                )
                            }
                            // XP
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💎", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser?.xp ?: 0}",
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 14.sp
                                )
                            }
                            // Hearts
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("❤️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser?.hearts ?: 5}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Liquid Premium icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassWhiteNormal)
                                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(NeonCyan, Blue600)))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Lesson Category Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth().testTag("category_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(LessonCategory.values()) { category ->
                        val isSelected = category == selectedCategory
                        val bgBrush = if (isSelected) {
                            Brush.horizontalGradient(listOf(LiquidTeal, NeonCyan))
                        } else {
                            Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f)))
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectCategory(category) }
                                .background(bgBrush)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = category.farsiName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lessons Grid / List
                LazyColumn(
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("lessons_list"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(lessons) { topic ->
                        LessonCard(
                            topic = topic,
                            onClick = { viewModel.selectLesson(topic) }
                        )
                    }
                }
            }

            // Bottom Sheet Modal for Lesson details
            AnimatedVisibility(
                visible = selectedLesson != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedLesson?.let { lesson ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = true) { viewModel.selectLesson(null) } // dismiss
                    ) {
                        GlassyLessonSheet(
                            lesson = lesson,
                            onDismiss = { viewModel.selectLesson(null) },
                            onGoToLab = {
                                viewModel.selectLesson(null)
                                // Navigate to corresponding lab
                                when (lesson.id) {
                                    "atom_struct" -> viewModel.selectLab("ساختار اتم الکترومغناطیسی")
                                    "prism_refract" -> viewModel.selectLab("شکست نور در منشور اپتیکی")
                                    "volcano_reaction" -> viewModel.selectLab("شبیه‌ساز واکنش آتشفشان شیمی")
                                    "elec_circuit" -> viewModel.selectLab("مدار الکتریکی سری و موازی")
                                    else -> {}
                                }
                                viewModel.selectTab(AppTab.VIRTUAL_LAB)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clickable(enabled = false) {} // prevent dismiss clicks inside UI
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    topic: LessonTopic,
    onClick: () -> Unit
) {
    val categoryIcon = when (topic.category) {
        LessonCategory.PHYSICS -> Icons.Default.FlashOn
        LessonCategory.CHEMISTRY -> Icons.Default.Science
        LessonCategory.BIOLOGY -> Icons.Default.WorkspacePremium
        LessonCategory.GEOLOGY -> Icons.Default.Terrain
        else -> Icons.Default.School
    }

    val glowColor = when (topic.category) {
        LessonCategory.PHYSICS -> NeonCyan
        LessonCategory.CHEMISTRY -> NeonPurple
        LessonCategory.BIOLOGY -> Color(0xFF4ADE80)
        LessonCategory.GEOLOGY -> Color(0xFFFBBF24)
        else -> Color.White
    }

    GlassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("lesson_card_${topic.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Rounded Glassy Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(glowColor.copy(alpha = 0.15f))
                    .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = topic.category.farsiName,
                    tint = glowColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                // Info badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = topic.category.farsiName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = glowColor
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = topic.duration,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = topic.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = topic.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "باز کردن",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GlassyLessonSheet(
    lesson: LessonTopic,
    onDismiss: () -> Unit,
    onGoToLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (lesson.category) {
        LessonCategory.PHYSICS -> NeonCyan
        LessonCategory.CHEMISTRY -> NeonPurple
        LessonCategory.BIOLOGY -> Color(0xFF4ADE80)
        LessonCategory.GEOLOGY -> Color(0xFFFBBF24)
        else -> Color.White
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20F172A)) // deep frost slate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Bottomsheet Notch
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lesson.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Specs badges
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = lesson.category.farsiName,
                        fontSize = 11.sp,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "سختی: ${lesson.difficulty}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "زمان: ${lesson.duration}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "خلاصه درس و مفاهیم بنیادین",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = lesson.summary,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "کلیدواژه‌های طلایی امتحان",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPurple,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Flowing layout for key concepts
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lesson.keyConcepts.forEach { concept ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "# $concept",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // CTA action
            RoundedGlassyButton(
                onClick = onGoToLab,
                modifier = Modifier.fillMaxWidth().testTag("sheet_action_lab"),
                backgroundColor = LiquidTeal
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "آزمایشگاه",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ورود به آزمایشگاه ۳بعدی واقعیت افزوده",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

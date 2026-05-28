package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.GlassyBackground
import com.example.ui.components.LiquidTeal
import com.example.ui.components.NeonCyan
import com.example.ui.screens.AiTeacherScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.VirtualLabScreen
import com.example.viewmodel.AppTab
import com.example.viewmodel.ScienceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ScienceViewModel = viewModel()
                val activeTab by viewModel.activeTab.collectAsState()

                // Force RTL Persian support
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Liquid Glass Glowing Animated Space Background
                        GlassyBackground {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = Color.Transparent, // Let background shine
                                bottomBar = {
                                    LiquidFloatingNavBar(
                                        activeTab = activeTab,
                                        onTabSelected = { viewModel.selectTab(it) }
                                    )
                                }
                            ) { innerPadding ->
                                // Screen Container transitioning with crossfade
                                AnimatedContent(
                                    targetState = activeTab,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith
                                                fadeOut(animationSpec = tween(300))
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    label = "tab_crossfade"
                                ) { tab ->
                                    when (tab) {
                                        AppTab.HOME -> HomeScreen(viewModel = viewModel)
                                        AppTab.VIRTUAL_LAB -> VirtualLabScreen(viewModel = viewModel)
                                        AppTab.AI_TEACHER -> AiTeacherScreen(viewModel = viewModel)
                                        AppTab.CHANNELS -> ChannelsScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidFloatingNavBar(
    activeTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(), // Support dynamic Android notches
        contentAlignment = Alignment.Center
    ) {
        // Floating premium capsule Card representing Apple Liquid Glass and Android 17 design
        Card(
            modifier = Modifier
                .padding(bottom = 12.dp, top = 4.dp)
                .padding(horizontal = 16.dp)
                .height(68.dp)
                .fillMaxWidth(0.95f)
                .shadow(
                    elevation = 0.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .border(
                    width = 1.dp,
                    color = com.example.ui.components.GlassWhiteBorder,
                    shape = RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.components.GlassWhitebg) // Immersive glass pill
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    tab = AppTab.HOME,
                    activeIcon = Icons.Filled.School,
                    inactiveIcon = Icons.Outlined.School,
                    label = "خواندنی‌ها",
                    isActive = activeTab == AppTab.HOME,
                    onClick = { onTabSelected(AppTab.HOME) }
                )
                
                NavBarItem(
                    tab = AppTab.VIRTUAL_LAB,
                    activeIcon = Icons.Filled.BlurOn,
                    inactiveIcon = Icons.Outlined.BlurOn,
                    label = "کیت سه بعدی",
                    isActive = activeTab == AppTab.VIRTUAL_LAB,
                    onClick = { onTabSelected(AppTab.VIRTUAL_LAB) }
                )

                NavBarItem(
                    tab = AppTab.AI_TEACHER,
                    activeIcon = Icons.Filled.SmartToy,
                    inactiveIcon = Icons.Outlined.SmartToy,
                    label = "استاد هوشمند",
                    isActive = activeTab == AppTab.AI_TEACHER,
                    onClick = { onTabSelected(AppTab.AI_TEACHER) }
                )

                NavBarItem(
                    tab = AppTab.CHANNELS,
                    activeIcon = Icons.Filled.Tv,
                    inactiveIcon = Icons.Outlined.Tv,
                    label = "فیلم و رسانه",
                    isActive = activeTab == AppTab.CHANNELS,
                    onClick = { onTabSelected(AppTab.CHANNELS) }
                )
            }
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    tab: AppTab,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    // Dynamic transition states
    val activeColor = NeonCyan
    val inactiveColor = Color.White.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("nav_item_${tab.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing pill indicator matching modern Material 3/Android 17
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isActive) com.example.ui.components.LiquidTeal.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActive) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) activeColor else inactiveColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

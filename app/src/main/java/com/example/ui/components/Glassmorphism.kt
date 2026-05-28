package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

// Theme Colors for Apple Liquid Glass + Android 17 design
val GlassSlateDark = Color(0xFF030712)
val LiquidIndigo = Color(0xFF1E1B4B)
val LiquidTeal = Color(0xFF0D9488)
val LiquidPink = Color(0xFFDB2777)
val GlassWhiteNormal = Color(0x1FFFFFFF)
val GlassWhiteBorder = Color(0x3BFFFFFF)
val GlassDarkNormal = Color(0x59000000)
val NeonCyan = Color(0xFF22D3EE)
val NeonPurple = Color(0xFFC084FC)

@Composable
fun GlassyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Elegant liquid animation states for background blobs
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_bg")
    
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassSlateDark)
            .drawBehind {
                // Base deep radial gradient for liquid space
                val cosmicGlow = Brush.radialGradient(
                    colors = listOf(LiquidIndigo, GlassSlateDark),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width * 0.9f
                )
                drawRect(cosmicGlow)

                // First floating liquid glass blob
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidTeal.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(
                            size.width * 0.2f + pulse1 * 4,
                            size.height * 0.3f + pulse2 * 2
                        ),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(
                        size.width * 0.2f + pulse1 * 4,
                        size.height * 0.3f + pulse2 * 2
                    )
                )

                // Second floating liquid pink blob
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidPink.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(
                            size.width * 0.8f - pulse2 * 3,
                            size.height * 0.7f + pulse1 * 3
                        ),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(
                        size.width * 0.8f - pulse2 * 3,
                        size.height * 0.7f + pulse1 * 3
                    )
                )
            }
    ) {
        content()
    }
}

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.8f)
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Black.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.09f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                )
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun RoundedGlassyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LiquidTeal,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.85f)
        ),
        interactionSource = interactionSource,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

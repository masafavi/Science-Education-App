package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.viewmodel.ScienceViewModel
import kotlin.math.*

@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VirtualLabScreen(
    viewModel: ScienceViewModel,
    modifier: Modifier = Modifier
) {
    val currentLab by viewModel.currentLab.collectAsState()
    val isArView = viewModel.isArView.value

    // Animation transition for atom spinning
    val infiniteTransition = rememberInfiniteTransition(label = "labs_anim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "atom_spin"
    )

    // Pulse animation for AR scan target
    val scanPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ar_scan_pulse"
    )

    // Physics dynamic state calculations
    val current = remember(viewModel.circuitVoltage.value, viewModel.circuitResistance.value) {
        viewModel.circuitVoltage.value / viewModel.circuitResistance.value
    }

    // Reaction timer triggers bubble generator automatically
    LaunchedEffect(viewModel.reactionActive.value) {
        if (viewModel.reactionActive.value) {
            while (viewModel.reactionActive.value) {
                viewModel.reactionBubblesCount.value = (viewModel.reactionBubblesCount.value + 1) % 40
                kotlinx.coroutines.delay(80)
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

            // Lab Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "آزمایشگاه ۳بعدی واقعیت افزوده",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "کیت آزمایشگاهی تعاملی با تغییر پارامتر فیزیکی",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // AR Live Camera simulator switcher button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.isArView.value = !viewModel.isArView.value }
                        .background(
                            if (isArView) Brush.horizontalGradient(listOf(LiquidPink, Color(0xFFC084FC)))
                            else Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (isArView) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("ar_switcher")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isArView) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "جلوه واقعیت افزوده (AR)",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isArView) "واقعیت افزوده روشن" else "شبیه‌ساز سه‌بعدی",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Choose Lab horizontally
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("lab_selection_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(viewModel.availableLabs) { labName ->
                    val isSelected = labName == currentLab
                    val activeBrush = Brush.horizontalGradient(listOf(LiquidTeal, NeonCyan))
                    val inactiveBrush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.03f)))
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectLab(labName) }
                            .background(if (isSelected) activeBrush else inactiveBrush)
                            .border(
                                1.dp,
                                if (isSelected) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("select_lab_$labName")
                    ) {
                        Text(
                            text = labName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Simulator Canvas Card
            GlassyCard(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = if (isArView) 0.55f else 0.83f))
                ) {
                    val textMeasurer = rememberTextMeasurer()

                    // Simulated AR Camera view grid overlay
                    if (isArView) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw tracking reticle
                            val center = Offset(size.width * 0.5f, size.height * 0.5f)
                            val reticleRadius = 45.dp.toPx() * scanPulse
                            
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.25f),
                                radius = reticleRadius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.6f),
                                radius = 6.dp.toPx(),
                                center = center
                            )
                            
                            // 4 Corner brackets representing augmented reality tracking
                            val bracketLength = 24.dp.toPx()
                            val spacing = 80.dp.toPx()
                            
                            // Top Left corner
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.5f),
                                start = Offset(center.x - spacing, center.y - spacing),
                                end = Offset(center.x - spacing + bracketLength, center.y - spacing),
                                strokeWidth = 3.dp.toPx()
                            )
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.5f),
                                start = Offset(center.x - spacing, center.y - spacing),
                                end = Offset(center.x - spacing, center.y - spacing + bracketLength),
                                strokeWidth = 3.dp.toPx()
                            )
                            
                            // Bottom Right corner
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.5f),
                                start = Offset(center.x + spacing, center.y + spacing),
                                end = Offset(center.x + spacing - bracketLength, center.y + spacing),
                                strokeWidth = 3.dp.toPx()
                            )
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.5f),
                                start = Offset(center.x + spacing, center.y + spacing),
                                end = Offset(center.x + spacing, center.y + spacing - bracketLength),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }

                    // Render Selected Lab
                    when (currentLab) {
                        "ساختار اتم الکترومغناطیسی" -> {
                            val protons = viewModel.atomProtons.value
                            val electrons = viewModel.atomElectrons.value
                            
                            val elementName = when (protons) {
                                1 -> "هیدروژن (Hydrogen - H)"
                                2 -> "هلیوم (Helium - He)"
                                3 -> "لیتیوم (Lithium - Li)"
                                4 -> "بریلیوم (Beryllium - Be)"
                                5 -> "بور (Boron - B)"
                                6 -> "کربن (Carbon - C)"
                                else -> "اتم سنگین رادیواکتیو"
                            }
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width * 0.5f
                                val centerY = size.height * 0.5f
                                
                                // Draw Electron Orbits
                                val radiusScale = viewModel.atomOrbitScale.value
                                val orbit1 = 70.dp.toPx() * radiusScale
                                val orbit2 = 120.dp.toPx() * radiusScale
                                
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.15f),
                                    radius = orbit1,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.1f),
                                    radius = orbit2,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                
                                // Draw Nucleus (Protons + Neutrons packed together)
                                val sphereRadius = 10.dp.toPx()
                                
                                // Draw Protons (Red spheres with + sign)
                                for (i in 0 until protons) {
                                    val angle = (i * 360f / protons).toDouble()
                                    val rad = Math.toRadians(angle)
                                    val offsetDistance = 12.dp.toPx()
                                    val px = centerX + (offsetDistance * cos(rad)).toFloat()
                                    val py = centerY + (offsetDistance * sin(rad)).toFloat()
                                    
                                    drawCircle(
                                        color = Color(0xFFEF4444),
                                        radius = sphereRadius,
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.5f),
                                        radius = sphereRadius * 0.5f,
                                        center = Offset(px - 3f, py - 3f)
                                    )
                                }
                                
                                // Draw Neutrons of similar size (Gray spheres)
                                val neutrons = protons + (if (protons > 1) 1 else 0)
                                for (i in 0 until neutrons) {
                                    val angle = (i * 360f / neutrons + 45f).toDouble()
                                    val rad = Math.toRadians(angle)
                                    val offsetDistance = 8.dp.toPx()
                                    val px = centerX + (offsetDistance * cos(rad)).toFloat()
                                    val py = centerY + (offsetDistance * sin(rad)).toFloat()
                                    
                                    drawCircle(
                                        color = Color(0xFF9CA3AF),
                                        radius = sphereRadius,
                                        center = Offset(px, py)
                                    )
                                }
                                
                                // Draw Electrons Spinning on orbits
                                for (e in 0 until electrons) {
                                    val isInnerShell = e < 2
                                    val activeOrbitRadius = if (isInnerShell) orbit1 else orbit2
                                    val individualOffsetAngle = e * (360f / if (isInnerShell) 2 else max(1, electrons - 2))
                                    val currentAngleRad = Math.toRadians((rotationAngle + individualOffsetAngle).toDouble())
                                    
                                    val ex = centerX + (activeOrbitRadius * cos(currentAngleRad)).toFloat()
                                    val ey = centerY + (activeOrbitRadius * sin(currentAngleRad)).toFloat()
                                    
                                    // Electron glowing cyan sphere
                                    drawCircle(
                                        color = NeonCyan,
                                        radius = 6.dp.toPx(),
                                        center = Offset(ex, ey)
                                    )
                                }
                                
                                // Overlay Element Badge info inside canvas
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "مکث عنصری: $elementName",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    topLeft = Offset(20f, 20f)
                                )
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "الکترون: $electrons | پروتون: $protons",
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    ),
                                    topLeft = Offset(20f, 75f)
                                )
                            }
                        }
                        
                        "شکست نور در منشور اپتیکی" -> {
                            val waveType = viewModel.prismWaveType.value
                            val angleVal = viewModel.prismAngle.value
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width * 0.5f
                                val centerY = size.height * 0.5f + 20.dp.toPx()
                                
                                // 1. Draw Glass Triangular Prism (central)
                                val side = 150.dp.toPx()
                                val triHeight = side * sqrt(3f) / 2
                                val path = Path().apply {
                                    moveTo(centerX, centerY - triHeight * 0.6f) // Top vertex
                                    lineTo(centerX - side * 0.5f, centerY + triHeight * 0.4f) // Bottom Left
                                    lineTo(centerX + side * 0.5f, centerY + triHeight * 0.4f) // Bottom Right
                                    close()
                                }
                                
                                drawPath(
                                    path = path,
                                    color = Color.White.copy(alpha = 0.15f)
                                )
                                drawPath(
                                    path = path,
                                    color = Color.White.copy(alpha = 0.4f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                
                                // Calculation of ray offsets driven by angle dial
                                val radDial = Math.toRadians(angleVal.toDouble())
                                val sourceX = 20.dp.toPx()
                                val sourceY = centerY + (30.dp.toPx() * sin(radDial)).toFloat()
                                
                                val rayColor = when (waveType) {
                                    "قرمز" -> Color(0xFFEF4444)
                                    "سبز" -> Color(0xFF10B981)
                                    "آبی" -> Color(0xFF3B82F6)
                                    else -> Color.White // سفید
                                }
                                
                                // Draw source ray from left
                                val hitPointX = centerX - side * 0.25f
                                val hitPointY = centerY + triHeight * 0.15f
                                
                                drawLine(
                                    color = rayColor,
                                    start = Offset(sourceX, sourceY),
                                    end = Offset(hitPointX, hitPointY),
                                    strokeWidth = 3.dp.toPx()
                                )
                                
                                // Refraction inside the glass
                                val exitPointX = centerX + side * 0.23f
                                val exitPointY = centerY + triHeight * 0.2f
                                drawLine(
                                    color = rayColor.copy(alpha = 0.7f),
                                    start = Offset(hitPointX, hitPointY),
                                    end = Offset(exitPointX, exitPointY),
                                    strokeWidth = 2.dp.toPx()
                                )
                                
                                // Spectral Exit
                                val spectralColors = listOf(
                                    Color(0xFFEF4444), // Red
                                    Color(0xFFF97316), // Orange
                                    Color(0xFFEAB308), // Yellow
                                    Color(0xFF10B981), // Green
                                    Color(0xFF06B6D4), // Cyan
                                    Color(0xFF3B82F6), // Blue
                                    Color(0xFF8B5CF6)  // Violet
                                )
                                
                                if (waveType == "سفید") {
                                    // Split white light into colorful fan spectrum
                                    spectralColors.forEachIndexed { index, color ->
                                        val fanOffset = (index - 3) * 8.dp.toPx()
                                        drawLine(
                                            color = color,
                                            start = Offset(exitPointX, exitPointY),
                                            end = Offset(size.width - 20.dp.toPx(), centerY + 50.dp.toPx() + fanOffset),
                                            strokeWidth = 3.dp.toPx()
                                        )
                                    }
                                } else {
                                    // Single color wave bend
                                    drawLine(
                                        color = rayColor,
                                        start = Offset(exitPointX, exitPointY),
                                        end = Offset(size.width - 20.dp.toPx(), centerY + 65.dp.toPx()),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                                
                                // Labels Info
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "طیف موج ورودی: $waveType",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    topLeft = Offset(20f, 20f)
                                )
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "زاویه تفرّق: $angleVal° (بر اساس ضریب شکست شیشه کراون)",
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    ),
                                    topLeft = Offset(20f, 75f)
                                )
                            }
                        }
                        
                        "شبیه‌ساز واکنش آتشفشان شیمی" -> {
                            val isActive = viewModel.reactionActive.value
                            val bubbleBase = viewModel.reactionBubblesCount.value
                            val acid = viewModel.acidAmount.value
                            val soda = viewModel.sodaAmount.value
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width * 0.5f
                                val bottomY = size.height * 0.75f
                                
                                // Draw Flask beaker outlines
                                val neckW = 50.dp.toPx()
                                val baseW = 180.dp.toPx()
                                val h = 200.dp.toPx()
                                val topY = bottomY - h
                                
                                val flaskPath = Path().apply {
                                    moveTo(centerX - neckW * 0.5f, topY)
                                    lineTo(centerX + neckW * 0.5f, topY)
                                    lineTo(centerX + neckW * 0.5f, topY + 40.dp.toPx())
                                    lineTo(centerX + baseW * 0.5f, bottomY)
                                    lineTo(centerX - baseW * 0.5f, bottomY)
                                    lineTo(centerX - neckW * 0.5f, topY + 40.dp.toPx())
                                    close()
                                }
                                
                                // Acid level (liquid height is proportional to acid ml)
                                val liquidH = 65.dp.toPx() * (acid / 100f) + 15.dp.toPx()
                                val fillPath = Path().apply {
                                    moveTo(centerX - (baseW * 0.5f - 10.dp.toPx()), bottomY)
                                    lineTo(centerX + (baseW * 0.5f - 10.dp.toPx()), bottomY)
                                    lineTo(centerX + (baseW * 0.5f - 40.dp.toPx()), bottomY - liquidH)
                                    lineTo(centerX - (baseW * 0.5f - 40.dp.toPx()), bottomY - liquidH)
                                    close()
                                }
                                
                                // Liquid fill
                                val chemicalLiquidColor = if (isActive) Color(0xFFE879F9).copy(alpha = 0.45f) else Color(0xFF67E8F9).copy(alpha = 0.35f)
                                drawPath(
                                    path = fillPath,
                                    color = chemicalLiquidColor
                                )
                                
                                // Draw Beaker frame itself
                                drawPath(
                                    path = flaskPath,
                                    color = Color.White.copy(alpha = 0.4f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                
                                // Reactions Carbon bubbles
                                if (isActive) {
                                    val count = (soda / 2).toInt() + 10
                                    for (b in 0 until count) {
                                        // Pseudo random bubble distribution based on indices
                                        val bx = centerX - 60.dp.toPx() + ((b * 4731) % 120.dp.toPx().toInt())
                                        val liftHeight = (bottomY - 10.dp.toPx() - ((bubbleBase * 5 + b * 13) % (liquidH + 110.dp.toPx())).toFloat())
                                        
                                        // Constrain bubbles within bottleneck width if high up
                                        val restrictedX = if (liftHeight < topY + 60.dp.toPx()) {
                                            centerX - 15.dp.toPx() + ((bx - centerX) * 0.2f)
                                        } else {
                                            bx
                                        }
                                        
                                        if (liftHeight > topY - 10.dp.toPx()) {
                                            // Glowing bubble circles
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.8f),
                                                radius = (3.dp.toPx() + (b % 4).dp.toPx()),
                                                center = Offset(restrictedX, liftHeight)
                                            )
                                        }
                                    }
                                }
                                
                                // Flask info
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "واکنش فورانی کربن دی‌اکسید (CO2)",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    topLeft = Offset(20f, 20f)
                                )
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = if (isActive) "وضعیت: فرآیند آزاد شدن گاز در حجم بالاست" else "وضعیت: غیرفعال (جهت فوران کلیک کنید)",
                                    style = TextStyle(
                                        color = if (isActive) Color(0xFFF472B6) else Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    ),
                                    topLeft = Offset(20f, 75f)
                                )
                            }
                        }
                        
                        "مدار الکتریکی سری و موازی" -> {
                            val isParallel = viewModel.circuitParallelMode.value
                            val voltage = viewModel.circuitVoltage.value
                            val resistance = viewModel.circuitResistance.value
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width * 0.5f
                                val centerY = size.height * 0.5f + 10.dp.toPx()
                                
                                val marginW = 100.dp.toPx()
                                val marginH = 65.dp.toPx()
                                
                                val leftX = centerX - marginW
                                val rightX = centerX + marginW
                                val topY = centerY - marginH
                                val botY = centerY + marginH
                                
                                // 1. Draw loop wiring rectangular outline
                                val circuitPath = Path().apply {
                                    moveTo(leftX, topY)
                                    lineTo(rightX, topY)
                                    lineTo(rightX, botY)
                                    lineTo(leftX, botY)
                                    close()
                                }
                                
                                drawPath(
                                    path = circuitPath,
                                    color = Color.White.copy(alpha = 0.3f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                
                                // 2. Draw Battery Cell at the bottom wire
                                val batW = 30.dp.toPx()
                                drawLine(
                                    color = Color(0xFFEF4444), // positive
                                    start = Offset(centerX - 8.dp.toPx(), botY - 14.dp.toPx()),
                                    end = Offset(centerX - 8.dp.toPx(), botY + 14.dp.toPx()),
                                    strokeWidth = 4.dp.toPx()
                                )
                                drawLine(
                                    color = Color(0xFF3B82F6), // negative
                                    start = Offset(centerX + 8.dp.toPx(), botY - 8.dp.toPx()),
                                    end = Offset(centerX + 8.dp.toPx(), botY + 8.dp.toPx()),
                                    strokeWidth = 2.dp.toPx()
                                )
                                
                                // 3. Draw Filament Lightbulb with glow proportional to Voltage/Resistance
                                val bulbX = leftX
                                val bulbY = centerY
                                val brightnessMultiplier = current * 1.5f // brightness coefficient
                                
                                // Filament Circle Back Glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFBBF24).copy(alpha = min(0.85f, brightnessMultiplier * 0.25f)),
                                            Color.Transparent
                                        ),
                                        center = Offset(bulbX, bulbY),
                                        radius = 35.dp.toPx()
                                    ),
                                    radius = 35.dp.toPx(),
                                    center = Offset(bulbX, bulbY)
                                )
                                
                                // Bulb outline
                                drawCircle(
                                    color = Color(0xFFFBBF24),
                                    radius = 12.dp.toPx(),
                                    center = Offset(bulbX, bulbY),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                // Filaments Cross
                                drawLine(
                                    color = Color(0xFFFBBF24),
                                    start = Offset(bulbX - 4.dp.toPx(), bulbY - 4.dp.toPx()),
                                    end = Offset(bulbX + 4.dp.toPx(), bulbY + 4.dp.toPx()),
                                    strokeWidth = 2.dp.toPx()
                                )
                                drawLine(
                                    color = Color(0xFFFBBF24),
                                    start = Offset(bulbX + 4.dp.toPx(), bulbY - 4.dp.toPx()),
                                    end = Offset(bulbX - 4.dp.toPx(), bulbY + 4.dp.toPx()),
                                    strokeWidth = 2.dp.toPx()
                                )
                                
                                // Parallel Bulb drawing if flag toggled
                                if (isParallel) {
                                    val pBulbX = rightX
                                    val pBulbY = centerY
                                    
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFBBF24).copy(alpha = min(0.85f, brightnessMultiplier * 0.25f)),
                                                Color.Transparent
                                            ),
                                            center = Offset(pBulbX, pBulbY),
                                            radius = 35.dp.toPx()
                                        ),
                                        radius = 35.dp.toPx(),
                                        center = Offset(pBulbX, pBulbY)
                                    )
                                    
                                    drawCircle(
                                        color = Color(0xFFFBBF24),
                                        radius = 12.dp.toPx(),
                                        center = Offset(pBulbX, pBulbY),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    // Filaments Cross
                                    drawLine(
                                        color = Color(0xFFFBBF24),
                                        start = Offset(pBulbX - 4.dp.toPx(), pBulbY - 4.dp.toPx()),
                                        end = Offset(pBulbX + 4.dp.toPx(), pBulbY + 4.dp.toPx()),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    drawLine(
                                        color = Color(0xFFFBBF24),
                                        start = Offset(pBulbX + 4.dp.toPx(), pBulbY - 4.dp.toPx()),
                                        end = Offset(pBulbX - 4.dp.toPx(), pBulbY + 4.dp.toPx()),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                
                                // 4. Draw Electron Charges drifting along wiring
                                val electronSpeed = current * 2.5f // physical velocity
                                val eOffset = (rotationAngle * electronSpeed) % (marginW * 4 + marginH * 4)
                                
                                // Draw 5 drifting yellow dots
                                for (i in 0 until 6) {
                                    val phase = eOffset + (i * 70.dp.toPx())
                                    val perimeter = marginW * 4 + marginH * 4
                                    val normPos = phase % perimeter
                                    
                                    // Coordinates translation along wire loop
                                    var ex = leftX
                                    var ey = topY
                                    
                                    if (normPos < marginW * 2) {
                                        ex = leftX + normPos
                                        ey = topY
                                    } else if (normPos < marginW * 2 + marginH * 2) {
                                        ex = rightX
                                        ey = topY + (normPos - marginW * 2)
                                    } else if (normPos < marginW * 4 + marginH * 2) {
                                        ex = rightX - (normPos - marginW * 2 - marginH * 2)
                                        ey = botY
                                    } else {
                                        ex = leftX
                                        ey = botY - (normPos - marginW * 4 - marginH * 2)
                                    }
                                    
                                    drawCircle(
                                        color = Color(0xFFFDE047),
                                        radius = 4.dp.toPx(),
                                        center = Offset(ex, ey)
                                    )
                                }
                                
                                // Circuit specs details
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = if (isParallel) "مدار موازی دو لامپ" else "مدار تک لامپ ساده",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    topLeft = Offset(20f, 20f)
                                )
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "جریان اهمی کل (I): " + String.format("%.2f", current) + " آمپر",
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    ),
                                    topLeft = Offset(20f, 75f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-Panel displaying Interactive Sliders mapped per lab
            GlassyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp)
                    .testTag("lab_controls_card")
            ) {
                Text(
                    text = "اهرم‌های کنترل فیزیکی آزمایش",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (currentLab) {
                    "ساختار اتم الکترومغناطیسی" -> {
                        // Sliders for Proton & Orbit scale
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تعداد پروتون هسته (+): ${viewModel.atomProtons.value}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.atomProtons.value.toFloat(),
                                    onValueChange = { viewModel.atomProtons.value = it.roundToInt() },
                                    valueRange = 1f..6f,
                                    modifier = Modifier.width(180.dp).testTag("slider_protons")
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تعداد الکترون‌ها (-): ${viewModel.atomElectrons.value}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.atomElectrons.value.toFloat(),
                                    onValueChange = { viewModel.atomElectrons.value = it.roundToInt() },
                                    valueRange = 1f..6f,
                                    modifier = Modifier.width(180.dp).testTag("slider_electrons")
                                )
                            }
                        }
                    }
                    
                    "شکست نور در منشور اپتیکی" -> {
                        // Sliders for Prism Angle & Wave type selector
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "جهت‌گیری در زاویه منشور: ${viewModel.prismAngle.value.toInt()}°",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.prismAngle.value,
                                    onValueChange = { viewModel.prismAngle.value = it },
                                    valueRange = 10f..180f,
                                    modifier = Modifier.width(180.dp).testTag("slider_angle")
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "طیف موج نوری:",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("سفید", "قرمز", "سبز", "آبی").forEach { type ->
                                        val isSel = viewModel.prismWaveType.value == type
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.prismWaveType.value = type }
                                                .background(if (isSel) LiquidTeal else Color.White.copy(alpha = 0.08f))
                                                .border(1.dp, Color.White.copy(alpha = if (isSel) 0.5f else 0.1f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(type, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    "شبیه‌ساز واکنش آتشفشان شیمی" -> {
                        // Slider academic elements
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "حجم اسید (سرکه): ${viewModel.acidAmount.value.toInt()} ml",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.acidAmount.value,
                                    onValueChange = { viewModel.acidAmount.value = it },
                                    valueRange = 10f..100f,
                                    modifier = Modifier.width(180.dp).testTag("slider_acid")
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "پودر واکنش (جوش شیرین): ${viewModel.sodaAmount.value.toInt()} گرم",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.sodaAmount.value,
                                    onValueChange = { viewModel.sodaAmount.value = it },
                                    valueRange = 5f..30f,
                                    modifier = Modifier.width(180.dp).testTag("slider_soda")
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Trigger Button
                            Button(
                                onClick = { viewModel.reactionActive.value = !viewModel.reactionActive.value },
                                modifier = Modifier.fillMaxWidth().testTag("trigger_reaction"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.reactionActive.value) Color(0xFFEF4444) else LiquidTeal
                                )
                            ) {
                                Icon(
                                    imageVector = if (viewModel.reactionActive.value) Icons.Default.Pause else Icons.Default.Science,
                                    contentDescription = "فعال‌سازی",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (viewModel.reactionActive.value) "توقف فوران شیمیایی" else "مخلوط کردن مواد و شروع فوران",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    "مدار الکتریکی سری و موازی" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ولتاژ منبع باتری (V): ${viewModel.circuitVoltage.value.toInt()} ولت",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.circuitVoltage.value,
                                    onValueChange = { viewModel.circuitVoltage.value = it },
                                    valueRange = 1.5f..24f,
                                    modifier = Modifier.width(180.dp).testTag("slider_voltage")
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مقاومت مدار (R): ${viewModel.circuitResistance.value.toInt()} اهم",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Slider(
                                    value = viewModel.circuitResistance.value,
                                    onValueChange = { viewModel.circuitResistance.value = it },
                                    valueRange = 2f..50f,
                                    modifier = Modifier.width(180.dp).testTag("slider_resistance")
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "آرایش مدارهای جانبی:",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { viewModel.circuitParallelMode.value = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!viewModel.circuitParallelMode.value) LiquidTeal else Color.White.copy(alpha = 0.08f)
                                        ),
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("لامپ تک سری", fontSize = 11.sp, color = Color.White)
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.circuitParallelMode.value = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (viewModel.circuitParallelMode.value) LiquidTeal else Color.White.copy(alpha = 0.08f)
                                        ),
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("دو لامپ موازی", fontSize = 11.sp, color = Color.White)
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

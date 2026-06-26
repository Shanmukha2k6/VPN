package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blobatic.shieldfoxvpn.R
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    // 1. Ticking progress percentage (0% to 100% in 2.2 seconds)
    var progressCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val duration = 2200L
        val steps = 100
        val delayPerStep = duration / steps
        for (i in 0..100) {
            progressCount = i
            delay(delayPerStep)
        }
        onLoadingComplete()
    }

    // 2. Slow breathing scale animation for the logo card
    val infiniteTransition = rememberInfiniteTransition(label = "logo_motion")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // 3. Smooth sweeping progress for the minimal line loader
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF2FF)), // Beautiful light sapphire-tinted canvas
        contentAlignment = Alignment.Center
    ) {
        // Tech background grid layout drawn faintly
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFFC7D2FE).copy(alpha = 0.15f)
            val step = 40.dp.toPx()
            
            // Vertical grid lines
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            
            // Horizontal grid lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant, floating square app icon (NOT rounded to circle)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
            ) {
                // Soft background shadow glow matching Sapphire primary
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // The square app icon (as requested, unrounded)
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "ShieldFox Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(Modifier.height(16.dp))

            // Ticking percentage text - super cool digital look
            Text(
                text = "$progressCount%",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF3B82F6), // Sapphire primary
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(12.dp))

            // Tracked-out title
            Text(
                text = "SHIELDFOX VPN",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0F172A).copy(alpha = 0.8f), // Deep navy
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(32.dp))

            // Thin horizontal line loader (width 120dp, height 2dp)
            val currentPrimary = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
            ) {
                val trackWidth = size.width
                val trackHeight = size.height
                
                // Draw background track
                drawRoundRect(
                    color = Color(0xFFC7D2FE).copy(alpha = 0.6f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2)
                )
                
                // Draw sliding progress glow (clipped inside the track)
                val pillWidth = trackWidth * 0.35f
                val startX = (sweepProgress * (trackWidth + pillWidth)) - pillWidth
                
                clipRect {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                currentPrimary.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(startX, 0f),
                        size = size.copy(width = pillWidth),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2)
                    )
                }
            }
        }
    }
}

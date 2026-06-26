package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
    // Elegant, slow breathing logo opacity
    val infiniteTransition = rememberInfiniteTransition(label = "logo_fade")
    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )

    // Smooth sweeping progress for the minimal line loader
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepProgress"
    )

    LaunchedEffect(Unit) {
        delay(2200)
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070913)), // Flat elegant solid SpaceBlack
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Centered circular logo
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "ShieldFox Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .alpha(logoAlpha)
            )

            Spacer(Modifier.height(24.dp))

            // Minimal, clean tracked-out title
            Text(
                text = "SHIELDFOX VPN",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(36.dp))

            // Thin, futuristic elegant horizontal line loader (width 120dp, height 2dp)
            val currentSecondary = MaterialTheme.colorScheme.secondary
            Canvas(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
            ) {
                val trackWidth = size.width
                val trackHeight = size.height
                
                // Draw background track
                drawRoundRect(
                    color = Color(0xFF1E2640),
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
                                currentSecondary.copy(alpha = 0.9f),
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

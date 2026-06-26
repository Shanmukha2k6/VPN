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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blobatic.shieldfoxvpn.R

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    // Smooth progress filling from 0f to 1f over 2.2 seconds
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF2FF)), // Clean solid cool-blue/sapphire canvas
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant unrounded square logo with soft outlines
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "ShieldFox Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(0.5.dp, Color(0xFFC7D2FE), RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(28.dp))

            // Tracked-out clean minimal text
            Text(
                text = "SHIELDFOX VPN",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0F172A).copy(alpha = 0.8f), // Deep navy
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(36.dp))

            // Thin elegant loading line (width 120dp, height 2dp) that fills up once
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
                    color = Color(0xFFC7D2FE).copy(alpha = 0.4f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2)
                )
                
                // Draw active progress line filling up smoothly
                val filledWidth = trackWidth * progress.value
                drawRoundRect(
                    color = currentPrimary,
                    size = size.copy(width = filledWidth),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2)
                )
            }
        }
    }
}

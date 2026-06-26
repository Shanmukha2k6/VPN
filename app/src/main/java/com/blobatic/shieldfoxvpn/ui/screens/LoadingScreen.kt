package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    // Pulse animation for the logo glow
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Trigger navigation after loading delay
    LaunchedEffect(Unit) {
        delay(2200)
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070913), // SpaceBlack
                        Color(0xFF0F1322)  // SurfaceGlass dark base
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo container with animated glow and scaling
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
            ) {
                // Soft breathing background glow behind the logo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // The circular launcher logo itself
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "ShieldFox Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Application Name
            Text(
                text = "ShieldFox VPN",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle loading message
            Text(
                text = "Securing your connection gateways...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(48.dp))

            // Smooth loading indicator
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

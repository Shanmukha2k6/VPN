package com.blobatic.shieldfoxvpn.ui.screens

import android.app.Activity
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import kotlinx.coroutines.launch

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var adDismissed by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }
    var isAdShowing by remember { mutableStateOf(false) }
    var isAnimationComplete by remember { mutableStateOf(false) }

    // Smooth progress filling from 0f to 1f over 2.2 seconds
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Run progress bar animation
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
            )
            isAnimationComplete = true
        }

        // 2. Load AdMob App Open Ad in parallel
        val adUnitId = if (com.blobatic.shieldfoxvpn.BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9257395921" // Test App Open Ad ID
        } else {
            "ca-app-pub-3940256099942544/9257395921" // Replace with production ID
        }

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adUnitId,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            adDismissed = true
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            adFailed = true
                        }
                    }
                    if (context is Activity && !context.isFinishing) {
                        isAdShowing = true
                        ad.show(context)
                    } else {
                        adFailed = true
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adFailed = true
                }
            }
        )
    }

    LaunchedEffect(isAnimationComplete, adDismissed, adFailed, isAdShowing) {
        if (isAnimationComplete) {
            if (isAdShowing) {
                if (adDismissed || adFailed) {
                    onLoadingComplete()
                }
            } else {
                onLoadingComplete()
            }
        }
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

            Spacer(Modifier.height(6.dp))

            Text(
                text = "ShieldFox: Fast and secure VPN loading",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569).copy(alpha = 0.7f), // Slate 600 subtext
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
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

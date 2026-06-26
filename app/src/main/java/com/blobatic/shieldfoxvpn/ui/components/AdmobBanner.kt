package com.blobatic.shieldfoxvpn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.blobatic.shieldfoxvpn.BuildConfig

/**
 * A policy-compliant AdMob Medium Rectangle banner ad container.
 * Displays only the raw 300x250 ad centered on the screen, without border boxes or headers.
 */
@Composable
fun AdmobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/6300978111" // Official Google Test Medium Rectangle ID
    } else {
        "ca-app-pub-3940256099942544/6300978111" // Fallback test ID (replace with production ID for final releases)
    }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isAdLoaded by remember { mutableStateOf(false) }
    var hasFailedToLoad by remember { mutableStateOf(false) }

    // Reusable AdView instance within this composable scope
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isAdLoaded = true
                    hasFailedToLoad = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoaded = false
                    hasFailedToLoad = true
                }
            }
        }
    }

    // Trigger ad loading when this composition starts
    LaunchedEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
    }

    // Safely bridge Android Lifecycle events to the AdView instance
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    // If ad failed to load, collapse the UI to reclaim screen estate
    if (hasFailedToLoad) {
        return
    }

    // Center the raw 300x250 ad box directly on the screen without borders or "ADVERTISEMENT" labels
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.size(width = 300.dp, height = 250.dp),
            factory = { adView }
        )
    }
}

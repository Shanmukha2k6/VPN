package com.securevpn.app.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.securevpn.app.data.model.VpnServer
import com.securevpn.app.data.model.VpnState
import com.securevpn.app.ui.components.AdmobBanner
import com.securevpn.app.ui.components.VpnPowerButton
import com.securevpn.app.ui.theme.*
import com.securevpn.app.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToServers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestVpnPermission: (callback: (Boolean) -> Unit) -> Unit,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLightTheme = MaterialTheme.colorScheme.background != BackgroundDark

    // Subdued background color shift based on connection state, adapting to Light/Dark theme
    val bgGradient = when (uiState.vpnState) {
        is VpnState.Connected -> Brush.verticalGradient(
            colors = if (isLightTheme) {
                listOf(Color(0xFFE6F4EA), MaterialTheme.colorScheme.background)
            } else {
                listOf(Color(0xFF071B14), MaterialTheme.colorScheme.background)
            }
        )
        is VpnState.Error -> Brush.verticalGradient(
            colors = if (isLightTheme) {
                listOf(Color(0xFFFCE8E6), MaterialTheme.colorScheme.background)
            } else {
                listOf(Color(0xFF1B0A0A), MaterialTheme.colorScheme.background)
            }
        )
        is VpnState.Connecting, is VpnState.Disconnecting -> Brush.verticalGradient(
            colors = if (isLightTheme) {
                listOf(Color(0xFFE8F0FE), MaterialTheme.colorScheme.background)
            } else {
                listOf(Color(0xFF09141B), MaterialTheme.colorScheme.background)
            }
        )
        else -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
        )
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = VpnGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "WingerVpn",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            // ─── Network Dot Grid Background Canvas ──────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotColor = if (isLightTheme) Color(0x0A000000) else Color(0x0CFFFFFF)
                val dotRadius = 1.5.dp.toPx()
                val gap = 24.dp.toPx()
                
                var x = gap / 2
                while (x < size.width) {
                    var y = gap / 2
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        y += gap
                    }
                    x += gap
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // ─── Status Indicator ──────────────────────────────────────────
                ConnectionStatusIndicator(uiState.vpnState)

                Spacer(Modifier.height(48.dp))

                // ─── Power Button with Concentric Pulse Waves ──────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp)
                ) {
                    ConcentricPulseWaves(
                        vpnState = uiState.vpnState,
                        size = 170.dp
                    )
                    VpnPowerButton(
                        vpnState = uiState.vpnState,
                        onClick = {
                            viewModel.onConnectButtonTapped(onRequestVpnPermission)
                        },
                        size = 170.dp
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ─── Timer ─────────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.vpnState is VpnState.Connected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SECURE CONNECTION TIME",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = viewModel.formatTimer(uiState.connectedSeconds),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraLight,
                            letterSpacing = 3.sp
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ─── Selected Server Card ──────────────────────────────────────
                ServerSelectorCard(
                    selectedServer = uiState.selectedServer,
                    onClick = onNavigateToServers
                )

                Spacer(Modifier.height(24.dp))

                // ─── AdMob Banner Ad ───────────────────────────────────────────
                AdmobBanner()



                // Error Snackbar
                uiState.errorMessage?.let { msg ->
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.dismissError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(state: VpnState) {
    val (label, color, shouldPulse) = when (state) {
        is VpnState.Connected     -> Triple("Protected", VpnGreen, false)
        is VpnState.Connecting    -> Triple("Securing...", VpnTeal, true)
        is VpnState.Disconnecting -> Triple("Disconnecting...", WarningAmber, true)
        is VpnState.Error         -> Triple("Connection Failed", ErrorRed, false)
        else                      -> Triple("Not Protected", MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), false)
    }

    val dotAlpha by if (shouldPulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "dot_pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.05f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Glowing status dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = dotAlpha))
        )
        Text(
            text = label.uppercase(),
            color = if (state is VpnState.Connected) VpnGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun ConcentricPulseWaves(
    vpnState: VpnState,
    modifier: Modifier = Modifier,
    size: Dp = 170.dp
) {
    val isConnecting = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isConnected = vpnState is VpnState.Connected
    val isVisible = isConnected || isConnecting
    
    if (!isVisible) return

    val pulseColor = if (isConnected) VpnGreen else VpnTeal
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_waves")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, delayMillis = 900, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, delayMillis = 900, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, delayMillis = 1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse3"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, delayMillis = 1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha3"
    )

    Box(
        modifier = modifier.size(size * 1.6f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulse1)
                .clip(CircleShape)
                .background(pulseColor.copy(alpha = alpha1))
        )
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulse2)
                .clip(CircleShape)
                .background(pulseColor.copy(alpha = alpha2))
        )
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulse3)
                .clip(CircleShape)
                .background(pulseColor.copy(alpha = alpha3))
        )
    }
}

@Composable
private fun ServerSelectorCard(
    selectedServer: VpnServer?,
    onClick: () -> Unit
) {
    val displayName = selectedServer?.countryName ?: "Auto Select (Fastest)"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedServer != null) {
                Text(
                    text = countryFlag(selectedServer.countryCode),
                    fontSize = 26.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = VpnGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "LOCATION",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Change Server",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}



// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun countryFlag(countryCode: String): String {
    val offset = 0x1F1E6 - 'A'.code
    return countryCode.uppercase().map {
        Character.toChars(it.code + offset).concatToString()
    }.joinToString("")
}



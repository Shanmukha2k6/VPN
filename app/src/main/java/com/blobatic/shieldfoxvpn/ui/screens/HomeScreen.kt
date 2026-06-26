package com.blobatic.shieldfoxvpn.ui.screens

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
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.ui.components.AdmobBanner
import com.blobatic.shieldfoxvpn.ui.components.VpnPowerButton
import com.blobatic.shieldfoxvpn.ui.theme.*
import com.blobatic.shieldfoxvpn.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToServers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestVpnPermission: (callback: (Boolean) -> Unit) -> Unit,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLightTheme = MaterialTheme.colorScheme.background != BgDark

    // ── Ambient background color shift per VPN state ─────────────────────────
    val bgGradient = when (uiState.vpnState) {
        is VpnState.Connected -> Brush.verticalGradient(
            colors = if (isLightTheme) listOf(Color(0xFFDCFCE7), MaterialTheme.colorScheme.background)
                     else listOf(Color(0xFF061410), MaterialTheme.colorScheme.background),
            endY = 900f
        )
        is VpnState.Error -> Brush.verticalGradient(
            colors = if (isLightTheme) listOf(Color(0xFFFEE2E2), MaterialTheme.colorScheme.background)
                     else listOf(Color(0xFF1A0808), MaterialTheme.colorScheme.background),
            endY = 900f
        )
        is VpnState.Connecting, is VpnState.Disconnecting -> Brush.verticalGradient(
            colors = if (isLightTheme) listOf(Color(0xFFE0F2FE), MaterialTheme.colorScheme.background)
                     else listOf(Color(0xFF071420), MaterialTheme.colorScheme.background),
            endY = 900f
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
                        // Shield logo mark
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AccentBlue, AccentBlueDim)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "ShieldFox",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
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
            // ── Subtle dot-grid backdrop ─────────────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotColor = if (isLightTheme) Color(0x08000000) else Color(0x0AFFFFFF)
                val dotR = 1.2.dp.toPx()
                val gap  = 26.dp.toPx()
                var x = gap / 2
                while (x < size.width) {
                    var y = gap / 2
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotR,
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
                Spacer(Modifier.height(8.dp))

                // ── Status pill ────────────────────────────────────────────────
                ConnectionStatusPill(uiState.vpnState)

                Spacer(Modifier.height(44.dp))

                // ── Power button with radial pulse waves ───────────────────────
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

                Spacer(Modifier.height(28.dp))

                // ── Tap hint label ──────────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.vpnState is VpnState.Disconnected,
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(200))
                ) {
                    Text(
                        text = "Tap to connect",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 0.8.sp
                    )
                }

                // ── Timer (when connected) ──────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.vpnState is VpnState.Connected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SECURE TIME",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatTimer(uiState.connectedSeconds),
                            style = MaterialTheme.typography.headlineLarge,
                            color = SecureGreen,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 4.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Server selector card ───────────────────────────────────────
                ServerSelectorCard(
                    selectedServer = uiState.selectedServer,
                    onClick = onNavigateToServers,
                    vpnState = uiState.vpnState
                )

                Spacer(Modifier.height(20.dp))

                // ── AdMob Banner ───────────────────────────────────────────────
                AdmobBanner()

                // ── Error toast ────────────────────────────────────────────────
                uiState.errorMessage?.let { msg ->
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close, "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─── Status Pill ──────────────────────────────────────────────────────────────

@Composable
private fun ConnectionStatusPill(state: VpnState) {
    val (label, color) = when (state) {
        is VpnState.Connected     -> "Protected" to SecureGreen
        is VpnState.Connecting    -> "Securing…" to AccentBlue
        is VpnState.Disconnecting -> "Disconnecting…" to WarningAmber
        is VpnState.Error         -> "Connection Failed" to ErrorRed
        else                      -> "Not Protected" to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val shouldPulse = state is VpnState.Connecting || state is VpnState.Disconnecting

    val dotAlpha by if (shouldPulse) {
        val t = rememberInfiniteTransition(label = "pill_pulse")
        t.animateFloat(
            initialValue = 0.3f, targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                tween(700, easing = EaseInOutSine), RepeatMode.Reverse
            ), label = "alpha"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.06f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = dotAlpha))
        )
        Text(
            text = label.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp
        )
    }
}

// ─── Concentric Pulse Waves ───────────────────────────────────────────────────

@Composable
fun ConcentricPulseWaves(
    vpnState: VpnState,
    modifier: Modifier = Modifier,
    size: Dp = 170.dp
) {
    val isConnecting = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isConnected  = vpnState is VpnState.Connected
    val isVisible    = isConnected || isConnecting
    if (!isVisible) return

    val pulseColor = if (isConnected) SecureGreen else AccentBlue
    val t = rememberInfiniteTransition(label = "pulses")

    data class Wave(val delayMs: Int)
    val waves = listOf(Wave(0), Wave(900), Wave(1800))

    Box(
        modifier = modifier.size(size * 1.7f),
        contentAlignment = Alignment.Center
    ) {
        waves.forEach { wave ->
            val scale by t.animateFloat(
                initialValue = 1.0f, targetValue = 1.6f,
                animationSpec = infiniteRepeatable(
                    tween(2600, delayMillis = wave.delayMs, easing = EaseOutQuad),
                    RepeatMode.Restart
                ), label = "wave_scale_${wave.delayMs}"
            )
            val alpha by t.animateFloat(
                initialValue = 0.28f, targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    tween(2600, delayMillis = wave.delayMs, easing = EaseOutQuad),
                    RepeatMode.Restart
                ), label = "wave_alpha_${wave.delayMs}"
            )
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(pulseColor.copy(alpha = alpha))
            )
        }
    }
}

// ─── Server Selector Card ─────────────────────────────────────────────────────

@Composable
private fun ServerSelectorCard(
    selectedServer: VpnServer?,
    vpnState: VpnState,
    onClick: () -> Unit
) {
    val displayName = selectedServer?.countryName ?: "Auto Select (Fastest)"
    val isActive = vpnState is VpnState.Connected

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            brush = if (isActive) {
                Brush.horizontalGradient(listOf(SecureGreen.copy(0.5f), AccentBlue.copy(0.3f)))
            } else {
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.colorScheme.outline
                    )
                )
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag emoji or globe icon
            if (selectedServer != null) {
                Text(
                    text = countryFlag(selectedServer.countryCode),
                    fontSize = 28.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "SERVER LOCATION",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedServer?.city?.isNotBlank() == true) {
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = selectedServer.city,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Change Server",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
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

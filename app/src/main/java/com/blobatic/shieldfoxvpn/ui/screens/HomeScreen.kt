package com.blobatic.shieldfoxvpn.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import com.blobatic.shieldfoxvpn.viewmodel.VpnViewModel
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.ui.components.AdmobBanner
import com.blobatic.shieldfoxvpn.ui.components.VpnPowerButton
import com.blobatic.shieldfoxvpn.ui.components.VpnTelemetry
import com.blobatic.shieldfoxvpn.ui.theme.*
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToServers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestVpnPermission: (callback: (Boolean) -> Unit) -> Unit,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cyber-Scanner Rotator
    val scannerTransition = rememberInfiniteTransition(label = "scanner_rotation")
    val scannerAngle by scannerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ShieldFox VPN",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Location Selector Card ─────────────────────────────────────────
            LocationSelector(
                server = uiState.selectedServer,
                onClick = onNavigateToServers
            )

            Spacer(Modifier.height(28.dp))

            // ── Status Info (Under location, above connect button/switch) ──────
            StatusLabel(uiState.vpnState)

            Spacer(Modifier.height(4.dp))

            // ── Radar Canvas & Power Button ──────────────────────────────────
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radar / Satellite cyber-grid scanner animation in background
                val currentPrimary = MaterialTheme.colorScheme.primary
                val currentSecondary = MaterialTheme.colorScheme.secondary
                val isVpnConnected = uiState.vpnState is VpnState.Connected

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val scannerColor = currentPrimary.copy(alpha = 0.05f)

                    // Concentric coordinate rings
                    drawCircle(
                        color = scannerColor,
                        radius = 60.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = scannerColor,
                        radius = 90.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = scannerColor.copy(alpha = 0.03f),
                        radius = 100.dp.toPx(),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )

                    // Axes coordinates
                    drawLine(
                        color = scannerColor.copy(alpha = 0.02f),
                        start = Offset(center.x - 110.dp.toPx(), center.y),
                        end = Offset(center.x + 110.dp.toPx(), center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = scannerColor.copy(alpha = 0.02f),
                        start = Offset(center.x, center.y - 110.dp.toPx()),
                        end = Offset(center.x, center.y + 110.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Radar sweep rotation
                    rotate(degrees = scannerAngle, pivot = center) {
                        val path = Path().apply {
                            moveTo(center.x, center.y)
                            arcTo(
                                rect = Rect(
                                    center.x - 90.dp.toPx(),
                                    center.y - 90.dp.toPx(),
                                    center.x + 90.dp.toPx(),
                                    center.y + 90.dp.toPx()
                                ),
                                startAngleDegrees = -90f,
                                sweepAngleDegrees = 45f,
                                forceMoveTo = false
                            )
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isVpnConnected) currentSecondary.copy(alpha = 0.1f) else currentPrimary.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = 90.dp.toPx()
                            )
                        )
                    }
                }

                // Tactile central power button
                VpnPowerButton(
                    vpnState = uiState.vpnState,
                    onClick  = { viewModel.onConnectButtonTapped(onRequestVpnPermission) },
                    size     = 180.dp
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Connection timer ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.vpnState is VpnState.Connected,
                enter   = fadeIn(tween(400)) + expandVertically(),
                exit    = fadeOut(tween(250)) + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.formatTimer(uiState.connectedSeconds),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.vpnState is VpnState.Disconnected,
                enter   = fadeIn(tween(400)),
                exit    = fadeOut(tween(200))
            ) {
                Text(
                    text = "Tap to initialize tunnel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Live Stats / Telemetry (Connected or connecting states) ────────
            VpnTelemetry(vpnState = uiState.vpnState)

            // ── Error Banner ──────────────────────────────────────────────────
            val errorMsg = uiState.errorMessage
            if (errorMsg != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Rose.copy(alpha = 0.08f))
                        .border(0.5.dp, Rose.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning, null,
                        tint = Rose,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = errorMsg,
                        color = Rose.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick  = { viewModel.dismissError() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close, "Dismiss",
                            tint = Rose.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            AdmobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 12.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Status Label ─────────────────────────────────────────────────────────────

@Composable
private fun StatusLabel(state: VpnState) {
    val currentSecondary = MaterialTheme.colorScheme.secondary
    val currentPrimary = MaterialTheme.colorScheme.primary
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val (text, subtitle, color) = when (state) {
        is VpnState.Connected     -> Triple("SECURED", "ShieldFox protection active", currentSecondary)
        is VpnState.Connecting    -> Triple("CONNECTING", "Routing traffic through tunnel", currentPrimary)
        is VpnState.Disconnecting -> Triple("DISCONNECTING", "Tearing down tunnel interface", Amber)
        is VpnState.Error         -> Triple("UNSECURED", "Connection failed", Rose)
        else                      -> Triple("NOT SECURED", "Traffic is unprotected", currentOnSurfaceVariant.copy(alpha = 0.6f))
    }

    val shouldPulse = state is VpnState.Connecting || state is VpnState.Disconnecting
    val pAlpha by if (shouldPulse) {
        rememberInfiniteTransition(label = "status_pulse").animateFloat(
            initialValue = 0.4f,
            targetValue  = 1.0f,
            animationSpec = infiniteRepeatable(
                tween(700, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "pa"
        )
    } else remember { mutableStateOf(1f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = pAlpha))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text          = text,
                style         = MaterialTheme.typography.labelLarge,
                color         = color,
                letterSpacing = 3.sp,
                fontWeight    = FontWeight.Bold
            )
        }
    }
}

// ─── Location Selector Card ───────────────────────────────────────────────────

@Composable
private fun LocationSelector(server: VpnServer?, onClick: () -> Unit) {
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val currentOutline = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, currentOutline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Emoji or Globe Icon
            if (server != null) {
                AsyncImage(
                    model = "https://flagcdn.com/w80/${server.countryCode.lowercase()}.png",
                    contentDescription = "${server.countryName} Flag",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(currentOutline),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Public, null,
                        tint     = currentOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET GATEWAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = currentOnSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text  = server?.countryName ?: "Smart Connection",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = server?.city?.takeIf { it.isNotBlank() } ?: "Fastest Server",
                    style = MaterialTheme.typography.bodySmall,
                    color = currentOnSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight, null,
                tint     = currentOnSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Flag Helper ──────────────────────────────────────────────────────────────

private fun countryFlag(countryCode: String): String {
    val offset = 0x1F1E6 - 'A'.code
    return countryCode.uppercase().map {
        Character.toChars(it.code + offset).concatToString()
    }.joinToString("")
}

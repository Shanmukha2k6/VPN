package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.blobatic.shieldfoxvpn.data.model.VpnProtocol
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.ui.components.AdmobBanner
import com.blobatic.shieldfoxvpn.ui.components.VpnPowerButton
import com.blobatic.shieldfoxvpn.ui.components.VpnTelemetry
import com.blobatic.shieldfoxvpn.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToServers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestVpnPermission: (callback: (Boolean) -> Unit) -> Unit,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showProtocolSheet by remember { mutableStateOf(false) }
    var killSwitchEnabled by remember { mutableStateOf(false) }

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
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Sapphire, NeonEmerald)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security, null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ShieldFox",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassBorder.copy(alpha = 0.4f))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            AdmobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(bottom = 4.dp)
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

            // ── Status Info ──────────────────────────────────────────────────
            StatusLabel(uiState.vpnState)

            Spacer(Modifier.height(20.dp))

            // ── Location Selector Card ─────────────────────────────────────────
            LocationSelector(
                server = uiState.selectedServer,
                onClick = onNavigateToServers
            )

            Spacer(Modifier.height(24.dp))

            // ── Radar Canvas & Power Button ──────────────────────────────────
            Box(
                modifier = Modifier
                    .size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radar / Satellite cyber-grid scanner animation in background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val scannerColor = Sapphire.copy(alpha = 0.05f)

                    // Concentric coordinate rings
                    drawCircle(
                        color = scannerColor,
                        radius = 80.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = scannerColor,
                        radius = 115.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = scannerColor.copy(alpha = 0.03f),
                        radius = 135.dp.toPx(),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )

                    // Axes coordinates
                    drawLine(
                        color = scannerColor.copy(alpha = 0.02f),
                        start = Offset(center.x - 140.dp.toPx(), center.y),
                        end = Offset(center.x + 140.dp.toPx(), center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = scannerColor.copy(alpha = 0.02f),
                        start = Offset(center.x, center.y - 140.dp.toPx()),
                        end = Offset(center.x, center.y + 140.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Radar sweep rotation
                    rotate(degrees = scannerAngle, pivot = center) {
                        val path = Path().apply {
                            moveTo(center.x, center.y)
                            arcTo(
                                rect = Rect(
                                    center.x - 115.dp.toPx(),
                                    center.y - 115.dp.toPx(),
                                    center.x + 115.dp.toPx(),
                                    center.y + 115.dp.toPx()
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
                                    if (uiState.vpnState is VpnState.Connected) NeonEmerald.copy(alpha = 0.1f) else Sapphire.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = 115.dp.toPx()
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

            Spacer(Modifier.height(24.dp))

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
                        color = NeonEmerald,
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SECURE SESSION",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        letterSpacing = 2.sp
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
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Live Stats / Telemetry (Connected or connecting states) ────────
            VpnTelemetry(vpnState = uiState.vpnState)

            Spacer(Modifier.height(24.dp))

            // ── Quick Controls Chips ───────────────────────────────────────────
            QuickControls(
                currentProtocol = uiState.selectedProtocol,
                killSwitchEnabled = killSwitchEnabled,
                onProtocolClick = { showProtocolSheet = true },
                onKillSwitchToggle = { killSwitchEnabled = !killSwitchEnabled }
            )

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

            Spacer(Modifier.height(36.dp))
        }
    }

    // ─── Protocol Picker Sheet ────────────────────────────────────────────────
    if (showProtocolSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProtocolSheet = false },
            containerColor = SurfaceGlass,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Select Protocol",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Protocols dictate how data is packetized and routed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(20.dp))

                VpnProtocol.values().forEach { protocol ->
                    val isSelected = uiState.selectedProtocol == protocol
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Sapphire.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable {
                                viewModel.selectProtocol(protocol)
                                showProtocolSheet = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Sapphire else GlassBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = protocol.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Sapphire else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            val desc = when (protocol) {
                                VpnProtocol.AUTO -> "Automatically pick fastest protocol"
                                VpnProtocol.WIREGUARD -> "Next-gen, ultra-fast and lightweight"
                                VpnProtocol.OPENVPN_UDP -> "Highly secure, default open tunnel"
                                VpnProtocol.OPENVPN_TCP -> "Robust fallback connection for strict firewalls"
                                VpnProtocol.IKEV2 -> "Excellent mobile stability, fast handshakes"
                                VpnProtocol.HTTP_PROXY -> "Route browser and app proxy ports only"
                                VpnProtocol.SOCKS5_PROXY -> "Low latency socks port routing"
                            }
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Status Label ─────────────────────────────────────────────────────────────

@Composable
private fun StatusLabel(state: VpnState) {
    val (text, subtitle, color) = when (state) {
        is VpnState.Connected     -> Triple("SECURED", "ShieldFox protection active", NeonEmerald)
        is VpnState.Connecting    -> Triple("CONNECTING", "Routing traffic through tunnel", Sapphire)
        is VpnState.Disconnecting -> Triple("DISCONNECTING", "Tearing down tunnel interface", Amber)
        is VpnState.Error         -> Triple("UNSECURED", "Connection failed", Rose)
        else                      -> Triple("NOT SECURED", "Traffic is unprotected", TextMuted)
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
        Spacer(Modifier.height(4.dp))
        Text(
            text          = subtitle,
            style         = MaterialTheme.typography.bodySmall,
            color         = TextSecondary,
            textAlign     = TextAlign.Center
        )
    }
}

// ─── Location Selector Card ───────────────────────────────────────────────────

@Composable
private fun LocationSelector(server: VpnServer?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Emoji or Globe Icon
            if (server != null) {
                Text(
                    text = countryFlag(server.countryCode),
                    fontSize = 24.sp,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GlassBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Public, null,
                        tint     = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET GATEWAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text  = server?.countryName ?: "Smart Connection",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = server?.city?.takeIf { it.isNotBlank() } ?: "Fastest Server",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight, null,
                tint     = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Quick Controls Chips ─────────────────────────────────────────────────────

@Composable
private fun QuickControls(
    currentProtocol: VpnProtocol,
    killSwitchEnabled: Boolean,
    onProtocolClick: () -> Unit,
    onKillSwitchToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Protocol Chip
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGlass)
                .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onProtocolClick)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Link, null,
                    tint = Sapphire,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = currentProtocol.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        // Kill Switch Chip
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (killSwitchEnabled) Rose.copy(alpha = 0.08f) else SurfaceGlass)
                .border(
                    width = 0.5.dp,
                    color = if (killSwitchEnabled) Rose.copy(alpha = 0.3f) else GlassBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onKillSwitchToggle)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Block, null,
                    tint = if (killSwitchEnabled) Rose else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (killSwitchEnabled) "Kill Switch: On" else "Kill Switch: Off",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (killSwitchEnabled) Rose else TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
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

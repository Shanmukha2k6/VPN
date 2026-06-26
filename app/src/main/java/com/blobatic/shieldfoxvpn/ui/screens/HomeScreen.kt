package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ShieldFox",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
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
                    containerColor = MaterialTheme.colorScheme.background
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

            Spacer(Modifier.height(32.dp))

            // ── Status label ──────────────────────────────────────────────────
            StatusLabel(uiState.vpnState)

            Spacer(Modifier.height(52.dp))

            // ── Power button ──────────────────────────────────────────────────
            VpnPowerButton(
                vpnState = uiState.vpnState,
                onClick  = { viewModel.onConnectButtonTapped(onRequestVpnPermission) },
                size     = 190.dp
            )

            Spacer(Modifier.height(52.dp))

            // ── Timer (connected only) ────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.vpnState is VpnState.Connected,
                enter   = fadeIn(tween(500)) + expandVertically(),
                exit    = fadeOut(tween(300)) + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.formatTimer(uiState.connectedSeconds),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Emerald,
                        letterSpacing = 6.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "CONNECTED",
                        style = MaterialTheme.typography.labelLarge,
                        color = Emerald.copy(alpha = 0.5f),
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }

            // ── Hint (disconnected only) ──────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.vpnState is VpnState.Disconnected,
                enter   = fadeIn(tween(400)),
                exit    = fadeOut(tween(200))
            ) {
                Text(
                    text = "Tap to connect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Divider ───────────────────────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )

            Spacer(Modifier.height(0.dp))

            // ── Server selector ───────────────────────────────────────────────
            ServerSelector(
                server  = uiState.selectedServer,
                onClick = onNavigateToServers
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )

            Spacer(Modifier.height(24.dp))

            // ── AdMob ─────────────────────────────────────────────────────────
            AdmobBanner()

            // ── Error ─────────────────────────────────────────────────────────
            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Rose.copy(alpha = 0.08f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning, null,
                        tint = Rose,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        msg,
                        color = Rose.copy(alpha = 0.85f),
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
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Status Label ─────────────────────────────────────────────────────────────

@Composable
private fun StatusLabel(state: VpnState) {
    val (text, color) = when (state) {
        is VpnState.Connected     -> "PROTECTED"       to Emerald
        is VpnState.Connecting    -> "CONNECTING…"     to Indigo
        is VpnState.Disconnecting -> "DISCONNECTING…"  to Amber
        is VpnState.Error         -> "CONNECTION FAILED" to Rose
        else                      -> "NOT PROTECTED"   to White35
    }

    // Pulse alpha for transitional states
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

    Text(
        text          = text,
        style         = MaterialTheme.typography.labelLarge,
        color         = color.copy(alpha = pAlpha),
        letterSpacing = 3.sp,
        textAlign     = TextAlign.Center
    )
}

// ─── Server Selector ──────────────────────────────────────────────────────────

@Composable
private fun ServerSelector(server: VpnServer?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag / globe
        if (server != null) {
            Text(
                text = countryFlag(server.countryCode),
                fontSize = 26.sp,
                modifier = Modifier.size(36.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(White15),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Public, null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = server?.countryName ?: "Auto Select",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text  = server?.city?.takeIf { it.isNotBlank() } ?: "Fastest available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Icon(
            Icons.Default.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun countryFlag(countryCode: String): String {
    val offset = 0x1F1E6 - 'A'.code
    return countryCode.uppercase().map {
        Character.toChars(it.code + offset).concatToString()
    }.joinToString("")
}

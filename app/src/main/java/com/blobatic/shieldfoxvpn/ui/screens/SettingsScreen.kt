package com.blobatic.shieldfoxvpn.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blobatic.shieldfoxvpn.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var autoConn   by remember { mutableStateOf(false) }
    var kill       by remember { mutableStateOf(false) }
    var dns        by remember { mutableStateOf(true) }
    var notifs     by remember { mutableStateOf(true) }

    val currentPrimary = MaterialTheme.colorScheme.primary
    val currentSecondary = MaterialTheme.colorScheme.secondary
    val currentOnBackground = MaterialTheme.colorScheme.onBackground
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val currentOutline = MaterialTheme.colorScheme.outline
    val currentSurface = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = currentOnBackground,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(currentOutline.copy(alpha = 0.4f))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = currentOnBackground,
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Connection Settings Group ──────────────────────────────────────
            SettingsGroupHeader("CONNECTION TUNNEL")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(0.5.dp, currentOutline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = currentSurface)
            ) {
                Column {
                    ToggleRow(
                        icon = Icons.Default.Wifi,
                        iconBg = currentPrimary.copy(alpha = 0.12f),
                        iconTint = currentPrimary,
                        title = "Auto Connect",
                        sub = "Initialize VPN on untrusted Wi-Fi",
                        checked = autoConn,
                        onChange = { autoConn = it }
                    )
                    Divider()
                    ToggleRow(
                        icon = Icons.Default.Block,
                        iconBg = Rose.copy(alpha = 0.12f),
                        iconTint = Rose,
                        title = "Kill Switch",
                        sub = "Block network if connection drops",
                        checked = kill,
                        onChange = { kill = it }
                    )
                    Divider()
                    ToggleRow(
                        icon = Icons.Default.Security,
                        iconBg = currentSecondary.copy(alpha = 0.12f),
                        iconTint = currentSecondary,
                        title = "DNS Leak Protection",
                        sub = "Force private DNS resolver query routing",
                        checked = dns,
                        onChange = { dns = it }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Appearance Settings Group ──────────────────────────────────────
            SettingsGroupHeader("PREFERENCES")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(0.5.dp, currentOutline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = currentSurface)
            ) {
                Column {
                    ToggleRow(
                        icon = Icons.Default.Notifications,
                        iconBg = currentPrimary.copy(alpha = 0.12f),
                        iconTint = currentPrimary,
                        title = "Notifications",
                        sub = "Push alert on connection state changes",
                        checked = notifs,
                        onChange = { notifs = it }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── About Settings Group ───────────────────────────────────────────
            SettingsGroupHeader("ABOUT SHIELDFOX")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(0.5.dp, currentOutline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = currentSurface)
            ) {
                Column {
                    NavRow(
                        icon = Icons.Default.Info,
                        iconBg = currentOutline,
                        iconTint = currentOnBackground,
                        title = "System Information",
                        sub = "ShieldFox VPN Core Client v1.0.0",
                        onClick = {}
                    )
                    Divider()
                    NavRow(
                        icon = Icons.Default.PrivacyTip,
                        iconBg = currentOutline,
                        iconTint = currentOnBackground,
                        title = "Privacy Policy",
                        sub = "No-log routing commitment details",
                        onClick = {}
                    )
                    Divider()
                    NavRow(
                        icon = Icons.Default.Gavel,
                        iconBg = currentOutline,
                        iconTint = currentOnBackground,
                        title = "Terms of Service",
                        sub = "Usage boundaries and fair routing rules",
                        onClick = {}
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Footer branding
            Text(
                text = "SHIELDFOX • BY BLOBATIC",
                style = MaterialTheme.typography.labelSmall,
                color = currentOnSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 36.dp)
            )
        }
    }
}

// ─── Settings Group Header ───────────────────────────────────────────────────

@Composable
private fun SettingsGroupHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 28.dp, bottom = 4.dp)
    )
}

// ─── Hairline Divider ─────────────────────────────────────────────────────────

@Composable
private fun Divider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 72.dp, end = 16.dp)
    )
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────

@Composable
private fun ToggleRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    sub: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    val currentOnBackground = MaterialTheme.colorScheme.onBackground
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val currentPrimary = MaterialTheme.colorScheme.primary
    val currentOutline = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored Icon Plate
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = currentOnBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = currentOnSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = currentPrimary,
                uncheckedThumbColor = currentOnSurfaceVariant.copy(alpha = 0.6f),
                uncheckedTrackColor = currentOutline,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            )
        )
    }
}

// ─── Nav Row ──────────────────────────────────────────────────────────────────

@Composable
private fun NavRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    sub: String,
    onClick: () -> Unit
) {
    val currentOnBackground = MaterialTheme.colorScheme.onBackground
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored Icon Plate
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = currentOnBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = currentOnSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = currentOnSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

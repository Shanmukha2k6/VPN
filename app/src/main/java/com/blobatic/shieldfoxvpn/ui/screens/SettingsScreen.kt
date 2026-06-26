package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blobatic.shieldfoxvpn.ui.theme.AccentBlue
import com.blobatic.shieldfoxvpn.ui.theme.SecureGreen
import com.blobatic.shieldfoxvpn.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val isDarkTheme by ThemeManager.isDarkTheme.collectAsState()
    var autoConnect   by remember { mutableStateOf(false) }
    var killSwitch    by remember { mutableStateOf(false) }
    var dnsLeak       by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack, null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Connection section ──────────────────────────────────────────
            SettingsGroup(title = "Connection") {
                SettingsToggleRow(
                    icon = Icons.Default.Wifi,
                    iconTint = AccentBlue,
                    title = "Auto Connect",
                    subtitle = "Connect VPN on network change",
                    checked = autoConnect,
                    onCheckedChange = { autoConnect = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Block,
                    iconTint = AccentBlue,
                    title = "Kill Switch",
                    subtitle = "Block internet if VPN disconnects",
                    checked = killSwitch,
                    onCheckedChange = { killSwitch = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Security,
                    iconTint = SecureGreen,
                    title = "DNS Leak Protection",
                    subtitle = "Route DNS through the secure tunnel",
                    checked = dnsLeak,
                    onCheckedChange = { dnsLeak = it }
                )
            }

            // ── Appearance section ─────────────────────────────────────────
            SettingsGroup(title = "Appearance") {
                SettingsToggleRow(
                    icon = Icons.Default.LightMode,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Light Theme",
                    subtitle = "Switch to light color scheme",
                    checked = !isDarkTheme,
                    onCheckedChange = { ThemeManager.setDarkTheme(!it) }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    iconTint = AccentBlue,
                    title = "Notifications",
                    subtitle = "Show VPN status notifications",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }

            // ── About section ──────────────────────────────────────────────
            SettingsGroup(title = "About") {
                SettingsNavRow(
                    icon = Icons.Default.Info,
                    iconTint = AccentBlue,
                    title = "About ShieldFox",
                    subtitle = "Version 1.0.0",
                    onClick = {}
                )
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Default.PrivacyTip,
                    iconTint = SecureGreen,
                    title = "Privacy Policy",
                    subtitle = "View how we handle your data",
                    onClick = {}
                )
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Default.Gavel,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "Terms of Service",
                    subtitle = "Read our terms and conditions",
                    onClick = {}
                )
            }

            // ── Footer ─────────────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Text(
                text = "ShieldFox v1.0.0 • Protected by Blobatic",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 0.5.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Settings Group ───────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)
        )
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

// ─── Divider ──────────────────────────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = AccentBlue,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor   = Color.Transparent
            )
        )
    }
}

// ─── Nav Row ──────────────────────────────────────────────────────────────────

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

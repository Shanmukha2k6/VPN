package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blobatic.shieldfoxvpn.ui.theme.Emerald
import com.blobatic.shieldfoxvpn.ui.theme.Indigo
import com.blobatic.shieldfoxvpn.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val isDark     by ThemeManager.isDarkTheme.collectAsState()
    var autoConn   by remember { mutableStateOf(false) }
    var kill       by remember { mutableStateOf(false) }
    var dns        by remember { mutableStateOf(true) }
    var notifs     by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, null,
                            tint = MaterialTheme.colorScheme.onBackground,
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
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(8.dp))

            // ── Connection ────────────────────────────────────────────────────
            SectionLabel("Connection")

            ToggleRow(
                icon    = Icons.Default.Wifi,
                title   = "Auto Connect",
                sub     = "Connect on network change",
                checked = autoConn,
                onChange = { autoConn = it }
            )
            Divider()
            ToggleRow(
                icon    = Icons.Default.Block,
                title   = "Kill Switch",
                sub     = "Block traffic if VPN drops",
                checked = kill,
                onChange = { kill = it }
            )
            Divider()
            ToggleRow(
                icon    = Icons.Default.Security,
                title   = "DNS Leak Protection",
                sub     = "Route DNS through tunnel",
                checked = dns,
                onChange = { dns = it }
            )

            Spacer(Modifier.height(32.dp))

            // ── Appearance ────────────────────────────────────────────────────
            SectionLabel("Appearance")

            ToggleRow(
                icon    = Icons.Default.LightMode,
                title   = "Light Mode",
                sub     = "Switch color scheme",
                checked = !isDark,
                onChange = { ThemeManager.setDarkTheme(!it) }
            )
            Divider()
            ToggleRow(
                icon    = Icons.Default.Notifications,
                title   = "Notifications",
                sub     = "VPN status alerts",
                checked = notifs,
                onChange = { notifs = it }
            )

            Spacer(Modifier.height(32.dp))

            // ── About ─────────────────────────────────────────────────────────
            SectionLabel("About")

            NavRow(
                icon  = Icons.Default.Info,
                title = "About ShieldFox",
                sub   = "Version 1.0.0",
                onClick = {}
            )
            Divider()
            NavRow(
                icon  = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                sub   = "How we handle your data",
                onClick = {}
            )
            Divider()
            NavRow(
                icon  = Icons.Default.Gavel,
                title = "Terms of Service",
                sub   = "Terms & conditions",
                onClick = {}
            )

            Spacer(Modifier.height(40.dp))

            Text(
                "ShieldFox • by Blobatic",
                style  = MaterialTheme.typography.labelMedium,
                color  = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f),
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelMedium,
        color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
        letterSpacing = 2.sp,
        modifier      = Modifier.padding(
            start = 24.dp, end = 24.dp,
            top = 0.dp, bottom = 8.dp
        )
    )
}

// ─── Hairline Divider ─────────────────────────────────────────────────────────

@Composable
private fun Divider() {
    HorizontalDivider(
        color     = MaterialTheme.colorScheme.outline.copy(0.3f),
        thickness = 0.5.dp,
        modifier  = Modifier.padding(start = 64.dp, end = 24.dp) // indent from icon
    )
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    sub: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint     = if (checked) Indigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style  = MaterialTheme.typography.bodyLarge,
                color  = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f)
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor    = Color.White,
                checkedTrackColor    = Indigo,
                uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor   = Color.Transparent
            )
        )
    }
}

// ─── Nav Row ──────────────────────────────────────────────────────────────────

@Composable
private fun NavRow(
    icon: ImageVector,
    title: String,
    sub: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style  = MaterialTheme.typography.bodyLarge,
                color  = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f)
            )
        }
        Icon(
            Icons.Default.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f),
            modifier = Modifier.size(17.dp)
        )
    }
}

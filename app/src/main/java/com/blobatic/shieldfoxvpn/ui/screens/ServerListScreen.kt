package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import com.blobatic.shieldfoxvpn.ui.theme.*
import com.blobatic.shieldfoxvpn.viewmodel.VpnViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    onBack: () -> Unit,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    val filtered = uiState.servers.filter { s ->
        query.isBlank() ||
        s.countryName.contains(query, ignoreCase = true) ||
        s.city.contains(query, ignoreCase = true)
    }.sortedBy { it.countryName }

    val currentPrimary = MaterialTheme.colorScheme.primary
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
                        text = "VPN Locations",
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
                actions = {
                    IconButton(
                        onClick = { viewModel.loadServers() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(currentOutline.copy(alpha = 0.4f))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh list",
                            tint = currentPrimary,
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
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Glassmorphic Search Bar ──────────────────────────────────────
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(0.5.dp, currentOutline, RoundedCornerShape(14.dp)),
                placeholder = {
                    Text(
                        text = "Search gateways...",
                        color = currentOnSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = currentOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = currentOnBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor           = currentOnBackground,
                    unfocusedTextColor         = currentOnBackground,
                    focusedContainerColor      = currentSurface,
                    unfocusedContainerColor    = currentSurface,
                    focusedIndicatorColor      = Color.Transparent,
                    unfocusedIndicatorColor    = Color.Transparent,
                    cursorColor                = currentPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoadingServers) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color       = currentPrimary,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Analyzing network routing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentOnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ── Auto Select (Smart Location) Card ─────────────────────
                    item {
                        AutoCard(
                            isSelected = uiState.selectedServer == null,
                            onClick = { viewModel.selectServer(null); onBack() }
                        )
                    }

                    // ── Locations Header ──────────────────────────────────────
                    if (filtered.isNotEmpty()) {
                        item {
                            Text(
                                text = "AVAILABLE GATEWAYS",
                                style = MaterialTheme.typography.labelSmall,
                                color = currentOnSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    start = 24.dp, end = 24.dp,
                                    top = 12.dp, bottom = 4.dp
                                )
                            )
                        }

                        items(filtered, key = { it.id }) { server ->
                            ServerCard(
                                server     = server,
                                isSelected = uiState.selectedServer?.id == server.id,
                                onClick    = { viewModel.selectServer(server); onBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Auto (Smart) Option Card ─────────────────────────────────────────────────

@Composable
private fun AutoCard(isSelected: Boolean, onClick: () -> Unit) {
    val currentPrimary = MaterialTheme.colorScheme.primary
    val currentOutline = MaterialTheme.colorScheme.outline
    val currentSurface = MaterialTheme.colorScheme.surface
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) currentPrimary.copy(alpha = 0.08f) else currentSurface)
            .border(
                width = 0.5.dp,
                color = if (isSelected) currentPrimary else currentOutline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) currentPrimary.copy(alpha = 0.15f) else currentOutline),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint     = if (isSelected) currentPrimary else currentOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Smart Connection (Auto)",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) currentPrimary else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Route dynamically through the lowest latency nodes",
                    style = MaterialTheme.typography.bodySmall,
                    color = currentOnSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = currentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Server Card Capsule ──────────────────────────────────────────────────────

@Composable
private fun ServerCard(server: VpnServer, isSelected: Boolean, onClick: () -> Unit) {
    val currentSecondary = MaterialTheme.colorScheme.secondary
    val currentPrimary = MaterialTheme.colorScheme.primary
    val currentOutline = MaterialTheme.colorScheme.outline
    val currentSurface = MaterialTheme.colorScheme.surface
    val currentOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val currentOnBackground = MaterialTheme.colorScheme.onBackground

    val highlightColor by animateColorAsState(
        targetValue = if (isSelected) currentSecondary else currentOnBackground,
        animationSpec = tween(250),
        label = "highlight"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) currentSecondary.copy(alpha = 0.08f) else currentSurface)
            .border(
                width = 0.5.dp,
                color = if (isSelected) currentSecondary else currentOutline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag
            AsyncImage(
                model = "https://flagcdn.com/w80/${server.countryCode.lowercase()}.png",
                contentDescription = "${server.countryName} Flag",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = server.countryName,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = highlightColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (server.city.isNotBlank()) {
                    Text(
                        text = server.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = currentOnSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            
            // Ping HUD
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ping = server.ping.takeIf { it > 0 } ?: 45
                Text(
                    text = "${ping}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        ping < 100 -> currentSecondary
                        ping < 200 -> Amber
                        else       -> Rose
                    },
                    fontWeight = FontWeight.SemiBold
                )
                SignalBars(ping)
            }

            Spacer(Modifier.width(12.dp))
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = currentSecondary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint     = currentOnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Custom Signal Bars ───────────────────────────────────────────────────────

@Composable
private fun SignalBars(ping: Int) {
    val currentSecondary = MaterialTheme.colorScheme.secondary
    val currentOutline = MaterialTheme.colorScheme.outline

    val (bars, color) = when {
        ping < 100 -> 4 to currentSecondary
        ping < 200 -> 3 to currentSecondary
        ping < 300 -> 2 to Amber
        else       -> 1 to Rose
    }
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 1..4) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = (i * 3 + 4).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (i <= bars) color
                        else currentOutline
                    )
            )
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun countryFlag(code: String): String {
    val offset = 0x1F1E6 - 'A'.code
    return code.uppercase().map { Character.toChars(it.code + offset).concatToString() }.joinToString("")
}

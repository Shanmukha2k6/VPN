package com.blobatic.shieldfoxvpn.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Servers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadServers() }) {
                        Icon(
                            Icons.Default.Refresh, "Refresh",
                            tint = Indigo,
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
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search ────────────────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        "Search…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Default.Clear, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor           = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor         = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor      = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor    = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor      = Color.Transparent,
                    unfocusedIndicatorColor    = Color.Transparent,
                    cursorColor                = Indigo
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoadingServers) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color       = Indigo,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Loading…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // ── Auto option ───────────────────────────────────────────
                    item {
                        AutoRow(
                            isSelected = uiState.selectedServer == null,
                            onClick = { viewModel.selectServer(null); onBack() }
                        )
                        Divider()
                    }

                    // ── Section label ─────────────────────────────────────────
                    if (filtered.isNotEmpty()) {
                        item {
                            Text(
                                text = "LOCATIONS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(
                                    start = 24.dp, end = 24.dp,
                                    top = 20.dp, bottom = 8.dp
                                )
                            )
                        }
                        items(filtered, key = { it.id }) { server ->
                            ServerRow(
                                server     = server,
                                isSelected = uiState.selectedServer?.id == server.id,
                                onClick    = { viewModel.selectServer(server); onBack() }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

// ─── Auto Row ─────────────────────────────────────────────────────────────────

@Composable
private fun AutoRow(isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(
                if (isSelected) Indigo.copy(0.05f) else Color.Transparent
            )
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Indigo.copy(0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Bolt, null,
                tint     = if (isSelected) Indigo else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Auto Select",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Fastest available server",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
            )
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = Indigo, modifier = Modifier.size(17.dp))
        }
    }
}

// ─── Server Row ───────────────────────────────────────────────────────────────

@Composable
private fun ServerRow(server: VpnServer, isSelected: Boolean, onClick: () -> Unit) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Emerald else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(250),
        label = "tc"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(if (isSelected) Emerald.copy(0.04f) else Color.Transparent)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            countryFlag(server.countryCode),
            fontSize = 22.sp,
            modifier = Modifier.width(30.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                server.countryName,
                style      = MaterialTheme.typography.bodyLarge,
                color      = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (server.city.isNotBlank()) {
                Text(
                    server.city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        SignalBars(server.ping)
        Spacer(Modifier.width(12.dp))
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = Emerald, modifier = Modifier.size(17.dp))
        } else {
            Icon(
                Icons.Default.ChevronRight, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

// ─── Hairline Divider ─────────────────────────────────────────────────────────

@Composable
private fun Divider() {
    HorizontalDivider(
        color     = MaterialTheme.colorScheme.outline.copy(0.35f),
        thickness = 0.5.dp,
        modifier  = Modifier.padding(horizontal = 24.dp)
    )
}

// ─── Signal Bars ──────────────────────────────────────────────────────────────

@Composable
private fun SignalBars(ping: Int) {
    val (bars, color) = when {
        ping == 0  -> 1 to MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f)
        ping < 150 -> 4 to Emerald
        ping < 250 -> 3 to Emerald
        ping < 350 -> 2 to Amber
        else       -> 1 to Amber
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
                        else MaterialTheme.colorScheme.outline.copy(0.35f)
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

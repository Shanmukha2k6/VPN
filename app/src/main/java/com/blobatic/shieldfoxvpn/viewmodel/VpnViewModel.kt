package com.blobatic.shieldfoxvpn.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blobatic.shieldfoxvpn.data.model.VpnProtocol
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.data.repository.VpnServerRepository
import com.blobatic.shieldfoxvpn.service.VpnTunnelService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class HomeUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val servers: List<VpnServer> = emptyList(),
    val selectedServer: VpnServer? = null,
    val isLoadingServers: Boolean = false,
    val errorMessage: String? = null,
    val connectedSeconds: Long = 0L,
    val selectedProtocol: VpnProtocol = VpnProtocol.AUTO,
    val showRewardedAdButton: Boolean = false
)

@HiltViewModel
class VpnViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverRepository: VpnServerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Tracks VPN permission callback
    private var vpnPermissionCallback: ((Boolean) -> Unit)? = null

    init {
        observeVpnState()
        loadServers()
    }

    // ─── Server Loading ──────────────────────────────────────────────────────────

    fun loadServers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingServers = true, errorMessage = null) }
            serverRepository.fetchServers()
                .onSuccess { servers ->
                    _uiState.update {
                        it.copy(
                            servers = servers,
                            selectedServer = it.selectedServer,
                            isLoadingServers = false
                        )
                    }
                    measureAndFillPings(servers)
                }
                .onFailure { e ->
                    val fallback = serverRepository.getCachedFallbackServers()
                    _uiState.update {
                        it.copy(
                            servers = fallback,
                            selectedServer = it.selectedServer,
                            isLoadingServers = false,
                            errorMessage = "Using cached servers"
                        )
                    }
                    measureAndFillPings(fallback)
                }
        }
    }

    private fun measureAndFillPings(servers: List<VpnServer>) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedServers = servers.map { server ->
                val start = System.currentTimeMillis()
                var measuredPing = -1
                try {
                    val address = java.net.InetAddress.getByName(server.ipAddress)
                    if (address.isReachable(800)) {
                        measuredPing = (System.currentTimeMillis() - start).toInt()
                    }
                } catch (e: Exception) {
                    // ignore
                }

                val finalPing = if (measuredPing > 0) {
                    measuredPing
                } else {
                    val basePing = when (server.countryCode.uppercase()) {
                        "IN" -> 32  // Local
                        "SG" -> 68  // Singapore
                        "JP" -> 138 // Japan
                        "KR" -> 150 // South Korea
                        "DE" -> 122 // Germany
                        "UK", "GB" -> 132 // United Kingdom
                        "FR" -> 128 // France
                        "NL" -> 136 // Netherlands
                        "CH" -> 134 // Switzerland
                        "ES" -> 142 // Spain
                        "SE" -> 148 // Sweden
                        "US" -> 208 // United States
                        "CA" -> 218 // Canada
                        "AU" -> 268 // Australia
                        else -> 90
                    }
                    basePing + (-6..6).random()
                }

                server.copy(ping = finalPing)
            }

            _uiState.update { state ->
                val currentSelected = state.selectedServer?.let { sel ->
                    updatedServers.find { it.id == sel.id } ?: sel
                }
                state.copy(
                    servers = updatedServers,
                    selectedServer = currentSelected
                )
            }
        }
    }

    fun selectServer(server: VpnServer?) {
        _uiState.update { it.copy(selectedServer = server) }
    }

    fun selectProtocol(protocol: VpnProtocol) {
        _uiState.update { it.copy(selectedProtocol = protocol) }
    }

    // ─── VPN Connect / Disconnect ────────────────────────────────────────────────

    /**
     * Called when user taps the power button.
     * Shows interstitial ad first, then connects.
     */
    fun onConnectButtonTapped(
        onRequestVpnPermission: (callback: (Boolean) -> Unit) -> Unit
    ) {
        val current = _uiState.value.vpnState
        if (current is VpnState.Connected || current is VpnState.Connecting) {
            disconnect()
            return
        }

        onRequestVpnPermission { granted ->
            if (granted) {
                viewModelScope.launch {
                    delay(300)
                    connectToSelectedServer()
                }
            }
        }
    }

    private fun connectToSelectedServer() {
        val selected = _uiState.value.selectedServer
        val targetServer = selected ?: _uiState.value.servers.minByOrNull { it.ping } ?: _uiState.value.servers.firstOrNull()
        
        if (targetServer == null) {
            _uiState.update { it.copy(errorMessage = "No servers available to connect") }
            return
        }

        val intent = Intent(context, VpnTunnelService::class.java).apply {
            action = VpnTunnelService.ACTION_CONNECT
            putExtra(VpnTunnelService.EXTRA_SERVER, targetServer)
        }
        
        context.startService(intent)
    }

    fun disconnect() {
        VpnTunnelService.updateState(VpnState.Disconnecting)
        val intent = Intent(context, VpnTunnelService::class.java).apply {
            action = VpnTunnelService.ACTION_DISCONNECT
        }
        context.startService(intent)
        viewModelScope.launch {
            delay(500)
            VpnTunnelService.updateState(VpnState.Disconnected)
        }
    }

    // ─── Timer & Stats ───────────────────────────────────────────────────────────

    private fun observeVpnState() {
        viewModelScope.launch {
            VpnTunnelService.vpnState.collect { state ->
                _uiState.update { it.copy(vpnState = state) }
            }
        }

        // Session timer
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value.vpnState
                if (state is VpnState.Connected) {
                    val elapsed = (System.currentTimeMillis() - state.connectedAt) / 1000
                    _uiState.update { it.copy(connectedSeconds = elapsed) }
                } else {
                    _uiState.update { it.copy(connectedSeconds = 0L) }
                }
            }
        }
    }

    fun formatTimer(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

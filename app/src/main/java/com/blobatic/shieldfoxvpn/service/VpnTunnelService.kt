package com.blobatic.shieldfoxvpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.blobatic.shieldfoxvpn.MainActivity
import com.blobatic.shieldfoxvpn.R
import com.blobatic.shieldfoxvpn.data.model.VpnProtocol
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import com.blobatic.shieldfoxvpn.data.model.VpnState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import java.net.InetAddress

/**
 * Core VPN Service.
 *
 * Architecture:
 * - For OpenVPN: This service orchestrates the OpenVPN process via native layer.
 *   In production, integrate the ics-openvpn library (LGPL):
 *   https://github.com/schwabe/ics-openvpn
 *
 * - For IKEv2: Use Android's built-in IKEv2/IPSec via IkeSession API (API 31+)
 *   or Strongswan Android SDK for older devices.
 *
 * This implementation provides:
 * 1. The complete service shell with proper lifecycle
 * 2. Notification channel setup
 * 3. VPN interface builder
 * 4. State management via StateFlow
 * 5. A demo tunnel (for testing UI without a real server)
 */
class VpnTunnelService : VpnService() {

    companion object {
        private const val TAG = "VpnTunnelService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "vpn_channel"

        const val ACTION_CONNECT = "com.blobatic.shieldfoxvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.blobatic.shieldfoxvpn.DISCONNECT"
        const val EXTRA_SERVER = "extra_server"

        // Singleton state exposed to the rest of the app
        private val _vpnState = MutableStateFlow<VpnState>(VpnState.Disconnected)
        val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

        fun updateState(state: VpnState) {
            _vpnState.value = state
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var statsJob: Job? = null
    private var localProxyServer: LocalAuthProxyServer? = null

    // ─── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val server = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_SERVER, VpnServer::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_SERVER) as? VpnServer
                }
                server?.let { connect(it) }
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Connect / Disconnect ───────────────────────────────────────────────────

    private fun connect(server: VpnServer) {
        serviceScope.launch {
            try {
                updateState(VpnState.Connecting)
                startForeground(NOTIFICATION_ID, buildNotification("Connecting to ${server.countryName}..."))

                when (server.protocol) {
                    VpnProtocol.OPENVPN_UDP,
                    VpnProtocol.OPENVPN_TCP -> connectOpenVpn(server)
                    VpnProtocol.WIREGUARD   -> connectWireGuard(server)
                    VpnProtocol.HTTP_PROXY,
                    VpnProtocol.SOCKS5_PROXY -> connectHttpProxy(server)
                    VpnProtocol.IKEV2       -> connectIkeV2(server)
                    VpnProtocol.AUTO        -> connectWireGuard(server) // Default to WireGuard for best performance
                }

            } catch (e: Exception) {
                Log.e(TAG, "Connect error: ${e.message}", e)
                updateState(VpnState.Error(e.message ?: "Connection failed"))
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    /**
     * OpenVPN Connection.
     *
     * INTEGRATION GUIDE:
     * 1. Add ics-openvpn as a module: https://github.com/schwabe/ics-openvpn
     * 2. Or use the DE.blinkt.openvpn library approach:
     *    - Write the .ovpn config to a temp file
     *    - Use VpnProfile + OpenVpnService
     *
     * The ovpnConfig string in VpnServer contains the full .ovpn config.
     * Write it to: context.cacheDir/temp.ovpn
     * Then launch the OpenVPN thread/process with that config.
     */
    private suspend fun connectOpenVpn(server: VpnServer) {
        withContext(Dispatchers.Main) { // Run on main thread to start activities
            try {
                // Write OpenVPN configuration to secure cache directory
                val configDir = java.io.File(cacheDir, "vpn_configs")
                if (!configDir.exists()) configDir.mkdirs()
                
                val configFile = java.io.File(configDir, "server_${server.id.hashCode()}.ovpn")
                configFile.writeText(server.ovpnConfig)

                // Get a secure content:// URI via FileProvider
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this@VpnTunnelService,
                    "$packageName.fileprovider",
                    configFile
                )

                // Explicitly target the popular openvpn app
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/x-openvpn-profile")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    startActivity(intent)
                    updateState(VpnState.Error("OpenVPN Config Ready! Please tap the import/save icon in the app that opens to start the global VPN."))
                } catch (e: android.content.ActivityNotFoundException) {
                    // Send user to Play Store to grab the OpenVPN core
                    val playIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=de.blinkt.openvpn"))
                    playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(playIntent)
                    updateState(VpnState.Error("Required Component Missing! Please install 'OpenVPN for Android' from the Play Store, then tap Connect again."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "OpenVPN execution error", e)
                updateState(VpnState.Error("Failed to prepare OpenVPN config: ${e.message}"))
            }
        }
    }

    private suspend fun connectIkeV2(server: VpnServer) {
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    // VPNBook uses the hostname as the remote identity for their IKEv2 EAP-MSCHAPv2 setup.
                    val builder = android.net.Ikev2VpnProfile.Builder(server.ikev2Host, server.ikev2Host)
                        .setAuthUsernamePassword(server.ikev2User, server.ikev2Pass, null)
                        .setBypassable(false) // Force global routing
                        
                    val profile = builder.build()
                    val vpnManager = getSystemService(android.net.VpnManager::class.java)
                    
                    val intent = vpnManager.provisionVpnProfile(profile)
                    if (intent != null) {
                        Log.e(TAG, "VPN profile provisioning required user consent.")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        updateState(VpnState.Error("Action Required: Please tap 'Allow' on the Android system dialog, then tap Connect again to activate the global VPN."))
                    } else {
                        vpnManager.startProvisionedVpnProfile()
                        
                        val connectedState = VpnState.Connected(
                            server = server,
                            connectedAt = System.currentTimeMillis(),
                            virtualIp = "Native IPsec"
                        )
                        updateState(connectedState)
                        updateNotification("Connected to ${server.countryName} globally")
                        startWgStatsTracking()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "IKEv2 Connection failed", e)
                    updateState(VpnState.Error("IKEv2 Engine Failed: ${e.message}"))
                }
            } else {
                updateState(VpnState.Error("Global IKEv2 requires Android 11+"))
            }
        }
    }

    // WireGuard Backend Instance
    private var wgBackend: Backend? = null
    
    // WireGuard Tunnel implementation tailored for our service
    private val wgTunnel = object : Tunnel {
        override fun getName(): String = "wg0"
        override fun onStateChange(newState: Tunnel.State) {
            Log.d(TAG, "WireGuard Tunnel State Changed: \$newState")
        }
    }

    private suspend fun connectWireGuard(server: VpnServer) {
        withContext(Dispatchers.IO) {
            try {
                if (wgBackend == null) {
                    wgBackend = GoBackend(applicationContext)
                }

                // 1. Build the WireGuard Interface configuration
                val interfaceBuilder = Interface.Builder()
                    .addAddress(com.wireguard.config.InetNetwork.parse(server.wgAddress))
                    .parsePrivateKey(server.wgPrivateKey)
                
                if (server.wgDns.isNotEmpty()) {
                    interfaceBuilder.addDnsServer(InetAddress.getByName(server.wgDns))
                }
                
                // 2. Build the Peer (Remote Server) configuration
                val peer = Peer.Builder()
                    .addAllowedIp(com.wireguard.config.InetNetwork.parse("0.0.0.0/0")) // Global routing
                    .setEndpoint(com.wireguard.config.InetEndpoint.parse(server.wgEndpoint))
                    .parsePublicKey(server.wgPublicKey)
                    .build()

                // 3. Assemble the final Config
                val config = Config.Builder()
                    .setInterface(interfaceBuilder.build())
                    .addPeer(peer)
                    .build()

                // 4. Start the Native Go Backend
                val backend = wgBackend ?: throw IllegalStateException("Backend not initialized")
                backend.setState(wgTunnel, Tunnel.State.UP, config)

                // 5. Update UI State completely globally connected
                val connectedState = VpnState.Connected(
                    server = server,
                    connectedAt = System.currentTimeMillis(),
                    virtualIp = server.wgAddress.substringBefore("/")
                )
                withContext(Dispatchers.Main) {
                    updateState(connectedState)
                    updateNotification("Protected via WireGuard: \${server.countryName}")
                    startWgStatsTracking()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect Native WireGuard", e)
                withContext(Dispatchers.Main) {
                    updateState(VpnState.Error("WireGuard Error: \${e.message}"))
                }
                stopSelf()
            }
        }
    }

    private suspend fun connectHttpProxy(server: VpnServer) {
        withContext(Dispatchers.IO) {
            try {
                val hasAuth = server.proxyUser.isNotEmpty()
                val isSocks = server.protocol == VpnProtocol.SOCKS5_PROXY
                val useLocalBridge = hasAuth || isSocks

                val proxyHost: String
                val proxyPort: Int

                if (useLocalBridge) {
                    // Find a free local port
                    val tempSocket = java.net.ServerSocket(0)
                    val localPort = tempSocket.localPort
                    tempSocket.close()

                    // Stop existing if any
                    localProxyServer?.stop()

                    // Start local auth proxy server
                    val bridge = LocalAuthProxyServer(
                        vpnService = this@VpnTunnelService,
                        localPort = localPort,
                        upstreamHost = server.ipAddress,
                        upstreamPort = server.port,
                        username = server.proxyUser,
                        password = server.proxyPass,
                        isUpstreamSocks = isSocks
                    )
                    bridge.start()
                    localProxyServer = bridge

                    proxyHost = "10.8.0.2"
                    proxyPort = localPort
                } else {
                    proxyHost = server.ipAddress
                    proxyPort = server.port
                }

                // Native HTTP Proxy via VpnService
                val proxyInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    android.net.ProxyInfo.buildDirectProxy(proxyHost, proxyPort)
                } else {
                    @Suppress("DEPRECATION")
                    android.net.ProxyInfo.buildDirectProxy(proxyHost, proxyPort)
                }

                val builder = Builder()
                    .setSession("ShieldFox VPN - ${server.countryName} (Proxy)")
                    .addAddress("10.8.0.2", 24)
                    .addRoute("10.8.0.2", 32)
                    .setHttpProxy(proxyInfo)
                    .setMtu(1500)
                    .setBlocking(true)

                builder.addDisallowedApplication(packageName)

                var retryCount = 0
                while (vpnInterface == null && retryCount < 3) {
                    try {
                        vpnInterface = builder.establish()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to establish VPN interface (attempt ${retryCount + 1}): ${e.message}")
                    }
                    if (vpnInterface == null) {
                        retryCount++
                        delay(300)
                    }
                }

                if (vpnInterface != null) {
                    val virtualIpStr = if (isSocks) {
                        "SOCKS5 Proxy: ${server.ipAddress}:${server.port}"
                    } else {
                        "HTTP Proxy: ${server.ipAddress}:${server.port}"
                    }
                    val connectedState = VpnState.Connected(
                        server = server,
                        connectedAt = System.currentTimeMillis(),
                        virtualIp = virtualIpStr
                    )
                    withContext(Dispatchers.Main) {
                        updateState(connectedState)
                        updateNotification("Proxy Active: ${server.countryName}")
                        startWgStatsTracking() // Re-used for UI speed demo
                    }
                } else {
                    throw Exception("Failed to bind Proxy VPN interface")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Proxy setup failed", e)
                withContext(Dispatchers.Main) {
                    updateState(VpnState.Error("Proxy Engine Failed: ${e.message}"))
                }
                stopSelf()
            }
        }
    }

    private fun disconnect() {
        statsJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val vpnManager = getSystemService(android.net.VpnManager::class.java)
                vpnManager.stopProvisionedVpnProfile()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop Platform VPN", e)
            }
        }
        try {
            wgBackend?.setState(wgTunnel, Tunnel.State.DOWN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping WireGuard tunnel", e)
        }

        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        try {
            localProxyServer?.stop()
            localProxyServer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping local proxy", e)
        }
        updateState(VpnState.Disconnected)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ─── Stats Tracking ─────────────────────────────────────────────────────────

    private fun startWgStatsTracking() {
        statsJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                try {
                    val stats = wgBackend?.getStatistics(wgTunnel)
                    if (stats != null) {
                        val current = _vpnState.value
                        if (current is VpnState.Connected) {
                            val peerKey = Key.fromBase64(current.server.wgPublicKey)
                            val peerStats = stats.peer(peerKey)
                            val rx = peerStats?.rxBytes ?: 0L
                            val tx = peerStats?.txBytes ?: 0L
                            
                            updateState(current.copy(bytesIn = rx, bytesOut = tx))
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Stats reading tick failed")
                }
            }
        }
    }

    // ─── Notifications ───────────────────────────────────────────────────────────

    private fun buildNotification(message: String): Notification {
        createNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val disconnectIntent = PendingIntent.getService(
            this, 1,
            Intent(this, VpnTunnelService::class.java).setAction(ACTION_DISCONNECT),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShieldFox VPN")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_delete, "Disconnect", disconnectIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(message: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(message))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VPN Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows VPN connection status"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

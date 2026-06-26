package com.blobatic.shieldfoxvpn.data.model

import java.io.Serializable
import androidx.annotation.DrawableRes

// ─── VPN Server ───────────────────────────────────────────────────────────────

data class VpnServer(
    val id: String,
    val countryName: String,
    val countryCode: String,       // ISO 2-letter e.g. "US", "JP"
    val city: String,
    val hostname: String,
    val ipAddress: String,
    val protocol: VpnProtocol,
    val port: Int,
    val ping: Int = 0,             // ms, updated dynamically
    val load: Int = 0,             // 0–100 %
    val isPremium: Boolean = false,
    val ovpnConfig: String = "",   // OpenVPN .ovpn config content
    val ikev2Host: String = "",
    val ikev2User: String = "",
    val ikev2Pass: String = "",
    val wgEndpoint: String = "",
    val wgPrivateKey: String = "",
    val wgPublicKey: String = "",
    val wgAddress: String = "",
    val wgDns: String = "1.1.1.1",
    val proxyUser: String = "",
    val proxyPass: String = ""
) : Serializable

enum class VpnProtocol(val displayName: String) : Serializable {
    OPENVPN_UDP("OpenVPN UDP"),
    OPENVPN_TCP("OpenVPN TCP"),
    IKEV2("IKEv2/IPSec"),
    WIREGUARD("WireGuard"),
    HTTP_PROXY("HTTP Proxy"),
    SOCKS5_PROXY("SOCKS5 Proxy"),
    AUTO("Auto")
}

// ─── Connection State ─────────────────────────────────────────────────────────

sealed class VpnState {
    object Disconnected : VpnState()
    object Connecting : VpnState()
    data class Connected(
        val server: VpnServer,
        val connectedAt: Long = System.currentTimeMillis(),
        val bytesIn: Long = 0L,
        val bytesOut: Long = 0L,
        val virtualIp: String = ""
    ) : VpnState()
    object Disconnecting : VpnState()
    data class Error(val message: String) : VpnState()
}

// ─── Server Groups ────────────────────────────────────────────────────────────

data class ServerGroup(
    val region: String,
    val servers: List<VpnServer>
)

// ─── User Preferences ─────────────────────────────────────────────────────────

data class AppSettings(
    val autoConnect: Boolean = false,
    val selectedProtocol: VpnProtocol = VpnProtocol.AUTO,
    val killSwitch: Boolean = false,
    val lastServerId: String? = null
)

// ─── Stats ────────────────────────────────────────────────────────────────────

data class SessionStats(
    val duration: Long = 0L,         // seconds
    val bytesDownloaded: Long = 0L,
    val bytesUploaded: Long = 0L
) {
    val downloadSpeed: String get() = formatSpeed(bytesDownloaded)
    val uploadSpeed: String get() = formatSpeed(bytesUploaded)

    private fun formatSpeed(bytes: Long): String {
        return when {
            bytes > 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
            bytes > 1_000     -> "%.1f KB".format(bytes / 1_000.0)
            else              -> "$bytes B"
        }
    }
}

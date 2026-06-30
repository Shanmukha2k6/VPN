package com.blobatic.shieldfoxvpn.data.repository

import android.content.Context
import android.util.Log
import com.blobatic.shieldfoxvpn.data.model.VpnProtocol
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import com.blobatic.shieldfoxvpn.data.remote.RemoteCredentialStore
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

// ─── JSON model for each server entry ─────────────────────────────────────────
// This matches the exact structure of the original VpnServer class.
data class RemoteServer(
    @SerializedName("id")          val id: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("city")        val city: String,
    @SerializedName("hostname")    val hostname: String,
    @SerializedName("ipAddress")   val ipAddress: String,
    @SerializedName("protocol")    val protocol: String,
    @SerializedName("port")        val port: Int,
    @SerializedName("isPremium")   val isPremium: Boolean = false,
    @SerializedName("ovpnConfig")   val ovpnConfig: String = "",
    @SerializedName("ikev2Host")    val ikev2Host: String = "",
    @SerializedName("ikev2User")    val ikev2User: String = "",
    @SerializedName("ikev2Pass")    val ikev2Pass: String = "",
    @SerializedName("wgEndpoint")   val wgEndpoint: String = "",
    @SerializedName("wgPrivateKey") val wgPrivateKey: String = "",
    @SerializedName("wgPublicKey")  val wgPublicKey: String = "",
    @SerializedName("wgAddress")    val wgAddress: String = "",
    @SerializedName("wgDns")        val wgDns: String = "1.1.1.1"
)

/**
 * Repository that manages the VPN server list.
 *
 * ALL configuration comes from Firebase Remote Config:
 *   - proxy_username  → your proxy username
 *   - proxy_password  → your proxy password
 *   - server_list     → JSON array of servers matching the original app configuration
 */
@Singleton
class VpnServerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "VpnServerRepository"

        // Emergency fallback — matches the exact original 15 servers originally in the app.
        // No credentials here — SOCKS5 and HTTP credentials are dynamically injected from Remote Config.
        private val EMERGENCY_FALLBACK = listOf(
            RemoteServer(
                id = "user_proxy_socks5_1",
                countryName = "United States",
                countryCode = "US",
                city = "New York",
                hostname = "151.247.124.10",
                ipAddress = "151.247.124.10",
                protocol = "SOCKS5_PROXY",
                port = 50101
            ),
            RemoteServer(
                id = "user_proxy_socks5_2",
                countryName = "United Kingdom",
                countryCode = "GB",
                city = "London",
                hostname = "151.247.124.11",
                ipAddress = "151.247.124.11",
                protocol = "SOCKS5_PROXY",
                port = 50101
            ),
            RemoteServer(
                id = "user_proxy_http_2",
                countryName = "Germany",
                countryCode = "DE",
                city = "Frankfurt",
                hostname = "151.247.124.12",
                ipAddress = "151.247.124.12",
                protocol = "HTTP_PROXY",
                port = 50100
            ),
            RemoteServer(
                id = "wireguard_japan",
                countryName = "Japan",
                countryCode = "JP",
                city = "Tokyo",
                hostname = "tokyo-wg.example.com",
                ipAddress = "1.2.3.4",
                protocol = "WIREGUARD",
                port = 51820,
                wgPublicKey = "YOUR_SERVER_PUBLIC_KEY",
                wgPrivateKey = "YOUR_CLIENT_PRIVATE_KEY",
                wgAddress = "10.0.0.2/32",
                wgEndpoint = "1.2.3.4:51820"
            ),
            RemoteServer(
                id = "openvpn_canada",
                countryName = "Canada",
                countryCode = "CA",
                city = "Toronto",
                hostname = "ca-ovpn.example.com",
                ipAddress = "5.6.7.8",
                protocol = "OPENVPN_UDP",
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.8 1194\n..."
            ),
            RemoteServer(
                id = "user_proxy_socks5_france",
                countryName = "France",
                countryCode = "FR",
                city = "Paris",
                hostname = "151.247.124.13",
                ipAddress = "151.247.124.13",
                protocol = "SOCKS5_PROXY",
                port = 50101
            ),
            RemoteServer(
                id = "user_proxy_http_netherlands",
                countryName = "Netherlands",
                countryCode = "NL",
                city = "Amsterdam",
                hostname = "151.247.124.14",
                ipAddress = "151.247.124.14",
                protocol = "HTTP_PROXY",
                port = 50100
            ),
            RemoteServer(
                id = "wireguard_singapore",
                countryName = "Singapore",
                countryCode = "SG",
                city = "Singapore",
                hostname = "sg-wg.example.com",
                ipAddress = "1.2.3.5",
                protocol = "WIREGUARD",
                port = 51820,
                wgPublicKey = "YOUR_SG_SERVER_PUBLIC_KEY",
                wgPrivateKey = "YOUR_SG_CLIENT_PRIVATE_KEY",
                wgAddress = "10.0.0.3/32",
                wgEndpoint = "1.2.3.5:51820"
            ),
            RemoteServer(
                id = "openvpn_australia",
                countryName = "Australia",
                countryCode = "AU",
                city = "Sydney",
                hostname = "au-ovpn.example.com",
                ipAddress = "5.6.7.9",
                protocol = "OPENVPN_UDP",
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.9 1194\n..."
            ),
            RemoteServer(
                id = "user_proxy_http_india",
                countryName = "India",
                countryCode = "IN",
                city = "Mumbai",
                hostname = "151.247.124.15",
                ipAddress = "151.247.124.15",
                protocol = "HTTP_PROXY",
                port = 50100
            ),
            RemoteServer(
                id = "user_proxy_socks5_korea",
                countryName = "South Korea",
                countryCode = "KR",
                city = "Seoul",
                hostname = "151.247.124.16",
                ipAddress = "151.247.124.16",
                protocol = "SOCKS5_PROXY",
                port = 50101
            ),
            RemoteServer(
                id = "user_proxy_http_switzerland",
                countryName = "Switzerland",
                countryCode = "CH",
                city = "Zurich",
                hostname = "151.247.124.17",
                ipAddress = "151.247.124.17",
                protocol = "HTTP_PROXY",
                port = 50100
            ),
            RemoteServer(
                id = "openvpn_brazil",
                countryName = "Brazil",
                countryCode = "BR",
                city = "Sao Paulo",
                hostname = "br-ovpn.example.com",
                ipAddress = "5.6.7.10",
                protocol = "OPENVPN_UDP",
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.10 1194\n..."
            ),
            RemoteServer(
                id = "wireguard_sweden",
                countryName = "Sweden",
                countryCode = "SE",
                city = "Stockholm",
                hostname = "se-wg.example.com",
                ipAddress = "1.2.3.6",
                protocol = "WIREGUARD",
                port = 51820,
                wgPublicKey = "YOUR_SE_SERVER_PUBLIC_KEY",
                wgPrivateKey = "YOUR_SE_CLIENT_PRIVATE_KEY",
                wgAddress = "10.0.0.4/32",
                wgEndpoint = "1.2.3.6:51820"
            ),
            RemoteServer(
                id = "user_proxy_http_spain",
                countryName = "Spain",
                countryCode = "ES",
                city = "Madrid",
                hostname = "151.247.124.18",
                ipAddress = "151.247.124.18",
                protocol = "HTTP_PROXY",
                port = 50100
            )
        )
    }

    /**
     * Main entry point. Fetches everything from Firebase and returns the server list.
     */
    suspend fun fetchServers(): Result<List<VpnServer>> = withContext(Dispatchers.IO) {
        // Fetch credentials + server list from Firebase Remote Config
        RemoteCredentialStore.fetchAll()

        // Parse the server list JSON from Remote Config
        val remoteServers = parseServerList(RemoteCredentialStore.getServerListJson())

        // Inject credentials and return
        Result.success(remoteServers.map { it.toVpnServer() })
    }

    /**
     * Parses the JSON string from Firebase Remote Config into a list of RemoteServer.
     * Falls back to EMERGENCY_FALLBACK if the JSON is empty or malformed.
     */
    private fun parseServerList(json: String): List<RemoteServer> {
        if (json.isBlank() || json == "[]") {
            Log.w(TAG, "Server list from Firebase is empty, using emergency fallback")
            return EMERGENCY_FALLBACK
        }
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<RemoteServer>>() {}.type
            val parsed = gson.fromJson<List<RemoteServer>>(json, type)
            if (parsed.isNullOrEmpty()) EMERGENCY_FALLBACK else parsed
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse server list JSON: ${e.message}")
            EMERGENCY_FALLBACK
        }
    }

    /**
     * Converts a RemoteServer (from Firebase JSON) into a VpnServer used by the app.
     * Credentials are injected HERE — they never appear in the JSON.
     */
    private fun RemoteServer.toVpnServer(): VpnServer {
        val u = RemoteCredentialStore.getUsername()
        val p = RemoteCredentialStore.getPassword()
        val proto = when (protocol.uppercase()) {
            "SOCKS5_PROXY" -> VpnProtocol.SOCKS5_PROXY
            "HTTP_PROXY"   -> VpnProtocol.HTTP_PROXY
            "WIREGUARD"    -> VpnProtocol.WIREGUARD
            "OPENVPN_UDP"  -> VpnProtocol.OPENVPN_UDP
            "OPENVPN_TCP"  -> VpnProtocol.OPENVPN_TCP
            "IKEV2"        -> VpnProtocol.IKEV2
            else           -> VpnProtocol.SOCKS5_PROXY
        }
        val needsAuth = proto == VpnProtocol.HTTP_PROXY || proto == VpnProtocol.SOCKS5_PROXY
        return VpnServer(
            id          = id,
            countryName = countryName,
            countryCode = countryCode,
            city        = city,
            hostname    = hostname,
            ipAddress   = ipAddress,
            protocol    = proto,
            port        = port,
            isPremium   = isPremium,
            ovpnConfig  = ovpnConfig,
            ikev2Host   = ikev2Host,
            ikev2User   = ikev2User,
            ikev2Pass   = ikev2Pass,
            wgEndpoint  = wgEndpoint,
            wgPrivateKey = wgPrivateKey,
            wgPublicKey = wgPublicKey,
            wgAddress   = wgAddress,
            wgDns       = wgDns,
            proxyUser   = if (needsAuth) u else "",
            proxyPass   = if (needsAuth) p else ""
        )
    }

    fun getCachedFallbackServers(): List<VpnServer> =
        parseServerList(RemoteCredentialStore.getServerListJson()).map { it.toVpnServer() }
}

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
// This matches exactly what you type in Firebase Remote Config → server_list

data class RemoteServer(
    @SerializedName("id")          val id: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("city")        val city: String,
    @SerializedName("hostname")    val hostname: String,
    @SerializedName("ipAddress")   val ipAddress: String,
    @SerializedName("protocol")    val protocol: String,
    @SerializedName("port")        val port: Int,
    @SerializedName("isPremium")   val isPremium: Boolean = false
)

/**
 * Repository that manages the VPN server list.
 *
 * ALL configuration comes from Firebase Remote Config:
 *   - proxy_username  → your proxy username
 *   - proxy_password  → your proxy password
 *   - server_list     → JSON array of servers (IPs, ports, countries)
 *
 * Nothing sensitive is in the APK. To update anything:
 *   → Firebase Console → Remote Config → edit → Publish
 *   → Users get the update within 1 hour. No app update needed.
 *
 * Fallback chain if Firebase is unreachable:
 *   1. Firebase SDK's own local cache (automatic, survives app restarts)
 *   2. EMERGENCY_FALLBACK below (same structure, no credentials)
 */
@Singleton
class VpnServerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "VpnServerRepository"

        // Emergency fallback — used ONLY if Firebase has never been reached
        // (brand new install, never had internet). No credentials here.
        private val EMERGENCY_FALLBACK = listOf(
            RemoteServer("us_1", "United States", "US", "New York",
                "151.247.124.10", "151.247.124.10", "SOCKS5_PROXY", 50101),
            RemoteServer("gb_1", "United Kingdom", "GB", "London",
                "151.247.124.11", "151.247.124.11", "SOCKS5_PROXY", 50101),
            RemoteServer("de_1", "Germany", "DE", "Frankfurt",
                "151.247.124.12", "151.247.124.12", "HTTP_PROXY", 50100),
            RemoteServer("fr_1", "France", "FR", "Paris",
                "151.247.124.13", "151.247.124.13", "SOCKS5_PROXY", 50101),
            RemoteServer("nl_1", "Netherlands", "NL", "Amsterdam",
                "151.247.124.14", "151.247.124.14", "HTTP_PROXY", 50100),
            RemoteServer("in_1", "India", "IN", "Mumbai",
                "151.247.124.15", "151.247.124.15", "HTTP_PROXY", 50100),
            RemoteServer("kr_1", "South Korea", "KR", "Seoul",
                "151.247.124.16", "151.247.124.16", "SOCKS5_PROXY", 50101),
            RemoteServer("ch_1", "Switzerland", "CH", "Zurich",
                "151.247.124.17", "151.247.124.17", "HTTP_PROXY", 50100),
            RemoteServer("es_1", "Spain", "ES", "Madrid",
                "151.247.124.18", "151.247.124.18", "HTTP_PROXY", 50100),
            RemoteServer("jp_1", "Japan", "JP", "Tokyo",
                "", "", "SOCKS5_PROXY", 50101),
            RemoteServer("ca_1", "Canada", "CA", "Toronto",
                "", "", "SOCKS5_PROXY", 50101),
            RemoteServer("sg_1", "Singapore", "SG", "Singapore",
                "", "", "SOCKS5_PROXY", 50101),
            RemoteServer("au_1", "Australia", "AU", "Sydney",
                "", "", "SOCKS5_PROXY", 50101),
            RemoteServer("br_1", "Brazil", "BR", "Sao Paulo",
                "", "", "SOCKS5_PROXY", 50101),
            RemoteServer("se_1", "Sweden", "SE", "Stockholm",
                "", "", "SOCKS5_PROXY", 50101)
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
            proxyUser   = if (needsAuth) u else "",
            proxyPass   = if (needsAuth) p else ""
        )
    }

    fun getCachedFallbackServers(): List<VpnServer> =
        parseServerList(RemoteCredentialStore.getServerListJson()).map { it.toVpnServer() }
}

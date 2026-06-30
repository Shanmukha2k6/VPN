package com.blobatic.shieldfoxvpn.data.repository

import android.content.Context
import android.content.SharedPreferences
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
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

// ─── Remote JSON model (no credentials — only IPs, ports, countries) ──────────

data class RemoteServer(
    @SerializedName("id")          val id: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("city")        val city: String,
    @SerializedName("hostname")    val hostname: String,
    @SerializedName("ipAddress")   val ipAddress: String,
    @SerializedName("protocol")    val protocol: String,   // "SOCKS5_PROXY", "HTTP_PROXY", etc.
    @SerializedName("port")        val port: Int,
    @SerializedName("isPremium")   val isPremium: Boolean = false
)

/**
 * Repository that fetches the server list from a remote JSON URL.
 *
 * HOW IT WORKS:
 * 1. App starts → fetches credentials from Firebase Remote Config (no credentials in APK)
 * 2. App starts → fetches server list from YOUR_SERVER_LIST_URL (no credentials in JSON either)
 * 3. Credentials are injected into server objects in memory only at connection time
 * 4. If offline → falls back to the last cached server list (stored in SharedPreferences)
 * 5. If never connected before → falls back to the built-in FALLBACK_SERVERS list
 *
 * TO ADD/REMOVE/CHANGE SERVERS: Edit the JSON file at YOUR_SERVER_LIST_URL → done.
 * TO CHANGE CREDENTIALS: Update Firebase Remote Config → done.
 * NO APP UPDATE EVER NEEDED for either.
 */
@Singleton
class VpnServerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vpn_server_cache", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "VpnServerRepository"

        // ── ⚠️ SET THIS TO YOUR JSON URL ─────────────────────────────────────
        // Host this file on GitHub (raw), your website, Firebase Storage, etc.
        // The file contains only IPs, ports, and country info — NO credentials.
        // Example: "https://raw.githubusercontent.com/Shanmukha2k6/VPN/main/servers.json"
        const val SERVER_LIST_URL = "https://raw.githubusercontent.com/Shanmukha2k6/VPN/main/servers.json"
        // ─────────────────────────────────────────────────────────────────────

        private const val CACHE_KEY = "cached_server_json"

        // Hardcoded emergency fallback — used only if BOTH remote fetch AND cache fail
        // (e.g. brand new install with no internet)
        // No credentials here — they are always injected from RemoteCredentialStore
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
                "151.247.124.18", "151.247.124.18", "HTTP_PROXY", 50100)
        )
    }

    /**
     * Main entry point. Call this when loading the server list.
     *
     * Flow:
     *   1. Fetch credentials (Firebase Remote Config)
     *   2. Fetch server list (remote JSON URL)
     *   3. Inject credentials into servers
     *   4. Return result (with fallback chain if anything fails)
     */
    suspend fun fetchServers(): Result<List<VpnServer>> = withContext(Dispatchers.IO) {
        // Step 1: Get credentials (returns cached if already fetched this session)
        RemoteCredentialStore.fetchCredentials()

        // Step 2: Fetch server list from remote JSON
        val remoteServers = fetchRemoteServerList()

        // Step 3: Inject credentials and convert to VpnServer
        val servers = remoteServers.map { it.toVpnServer() }

        Result.success(servers)
    }

    /**
     * Fetches the JSON server list from the remote URL.
     * Falls back to:
     *   1. SharedPreferences cache (last successful fetch)
     *   2. EMERGENCY_FALLBACK (hardcoded, no credentials)
     */
    private fun fetchRemoteServerList(): List<RemoteServer> {
        return try {
            val request = Request.Builder()
                .url(SERVER_LIST_URL)
                .header("Cache-Control", "no-cache")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: return loadCached()
                // Cache this successful response for offline use
                prefs.edit().putString(CACHE_KEY, json).apply()
                Log.d(TAG, "Remote server list fetched successfully")
                parseJson(json) ?: loadCached()
            } else {
                Log.w(TAG, "Remote fetch failed (HTTP ${response.code}), using cache")
                loadCached()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote fetch failed (${e.message}), using cache")
            loadCached()
        }
    }

    /** Loads from SharedPreferences cache (last successful remote fetch) */
    private fun loadCached(): List<RemoteServer> {
        val json = prefs.getString(CACHE_KEY, null)
        return if (json != null) {
            Log.d(TAG, "Using cached server list")
            parseJson(json) ?: EMERGENCY_FALLBACK
        } else {
            Log.d(TAG, "No cache available, using emergency fallback")
            EMERGENCY_FALLBACK
        }
    }

    /** Parses the JSON array into a list of RemoteServer objects */
    private fun parseJson(json: String): List<RemoteServer>? {
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<RemoteServer>>() {}.type
            gson.fromJson<List<RemoteServer>>(json, type)
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            null
        }
    }

    /**
     * Converts a RemoteServer (from JSON) into a VpnServer (used by the app).
     * Credentials are injected HERE from RemoteCredentialStore — never from the JSON.
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
            // Credentials injected from Remote Config — not from JSON
            proxyUser   = if (proto == VpnProtocol.HTTP_PROXY || proto == VpnProtocol.SOCKS5_PROXY) u else "",
            proxyPass   = if (proto == VpnProtocol.HTTP_PROXY || proto == VpnProtocol.SOCKS5_PROXY) p else ""
        )
    }

    fun getCachedFallbackServers(): List<VpnServer> = loadCached().map { it.toVpnServer() }
}

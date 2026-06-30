package com.blobatic.shieldfoxvpn.data.remote

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Single source of truth for all remote configuration.
 * Everything lives in Firebase Remote Config — credentials AND server list.
 *
 * Firebase Console → Remote Config → set these keys:
 *
 *   proxy_username  →  your proxy username
 *   proxy_password  →  your proxy password
 *   server_list     →  JSON array of servers (see servers.json in project root for format)
 *
 * To update anything: change in Firebase Console → Publish → done.
 * No app update needed. No GitHub needed. Everything is private.
 */
object RemoteCredentialStore {

    private const val TAG = "RemoteCredentialStore"

    // ─── Firebase Remote Config keys ─────────────────────────────────────────
    private const val KEY_USERNAME    = "proxy_username"
    private const val KEY_PASSWORD    = "proxy_password"
    private const val KEY_SERVER_LIST = "server_list"

    // ─── In-memory values (never written to disk) ─────────────────────────────
    private var proxyUsername: String = ""
    private var proxyPassword: String = ""
    private var serverListJson: String = ""

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().also { rc ->
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // Always fetch fresh on every app open
                .build()
            rc.setConfigSettingsAsync(settings)

            // Safe defaults — empty until first fetch
            // ⚠️ Do NOT put real credentials or IPs here
            rc.setDefaultsAsync(mapOf(
                KEY_USERNAME    to "",
                KEY_PASSWORD    to "",
                KEY_SERVER_LIST to "[]"
            ))
        }
    }

    /**
     * Fetches all remote config values from Firebase.
     * Call this once at app startup. Subsequent calls use cached values
     * unless the 1-hour interval has passed.
     *
     * @return true if credentials are available (either fresh or cached)
     */
    suspend fun fetchAll(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
            proxyUsername  = remoteConfig.getString(KEY_USERNAME)
            proxyPassword  = remoteConfig.getString(KEY_PASSWORD)
            serverListJson = remoteConfig.getString(KEY_SERVER_LIST)
            Log.d(TAG, "Remote config fetched. Username set: ${proxyUsername.isNotEmpty()}, Servers: ${serverListJson.length} chars")
            proxyUsername.isNotEmpty()
        } catch (e: Exception) {
            // Use cached values from the last successful fetch (Firebase caches locally)
            Log.w(TAG, "Remote config fetch failed (${e.message}), using cached values")
            proxyUsername  = remoteConfig.getString(KEY_USERNAME)
            proxyPassword  = remoteConfig.getString(KEY_PASSWORD)
            serverListJson = remoteConfig.getString(KEY_SERVER_LIST)
            proxyUsername.isNotEmpty()
        }
    }

    // Kept for backward compatibility with existing code
    suspend fun fetchCredentials(): Boolean = fetchAll()

    fun getUsername(): String    = proxyUsername
    fun getPassword(): String    = proxyPassword
    fun getServerListJson(): String = serverListJson

    fun isReady(): Boolean =
        proxyUsername.isNotEmpty() && proxyPassword.isNotEmpty()

    fun clearMemory() {
        proxyUsername  = ""
        proxyPassword  = ""
        serverListJson = ""
    }
}

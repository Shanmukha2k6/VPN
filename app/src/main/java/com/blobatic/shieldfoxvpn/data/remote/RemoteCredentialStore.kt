package com.blobatic.shieldfoxvpn.data.remote

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Fetches proxy credentials securely from Firebase Remote Config.
 * No credentials are stored in the APK — they live only in Firebase Console.
 *
 * To update credentials: change values in Firebase Console → Publish.
 * No app update required.
 */
object RemoteCredentialStore {

    private const val TAG = "RemoteCredentialStore"

    // In-memory only. Never written to disk.
    private var proxyUsername: String = ""
    private var proxyPassword: String = ""

    // Remote Config keys — these key names are safe to be in source code,
    // only the VALUES are sensitive and live in Firebase Console.
    private const val KEY_USERNAME = "proxy_username"
    private const val KEY_PASSWORD = "proxy_password"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().also { rc ->
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // Cache for 1 hour
                .build()
            rc.setConfigSettingsAsync(settings)
            // Safe defaults — empty strings until fetched.
            // Do NOT put real credentials here.
            rc.setDefaultsAsync(mapOf(
                KEY_USERNAME to "",
                KEY_PASSWORD to ""
            ))
        }
    }

    /**
     * Call this once at app startup (e.g., in Application.onCreate or ViewModel init).
     * Fetches and activates Remote Config values.
     * Falls back to cached values if offline.
     */
    suspend fun fetchCredentials(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
            proxyUsername = remoteConfig.getString(KEY_USERNAME)
            proxyPassword = remoteConfig.getString(KEY_PASSWORD)
            Log.d(TAG, "Credentials fetched (username length: ${proxyUsername.length})")
            proxyUsername.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch credentials: ${e.message}")
            // Try to use cached values from last successful fetch
            proxyUsername = remoteConfig.getString(KEY_USERNAME)
            proxyPassword = remoteConfig.getString(KEY_PASSWORD)
            false
        }
    }

    fun getUsername(): String = proxyUsername
    fun getPassword(): String = proxyPassword
    fun clearMemory() { proxyUsername = ""; proxyPassword = "" }
    fun isReady(): Boolean = proxyUsername.isNotEmpty() && proxyPassword.isNotEmpty()
}

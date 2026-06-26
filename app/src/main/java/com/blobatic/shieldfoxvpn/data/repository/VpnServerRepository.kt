package com.blobatic.shieldfoxvpn.data.repository

import android.content.Context
import com.blobatic.shieldfoxvpn.data.model.VpnProtocol
import com.blobatic.shieldfoxvpn.data.model.VpnServer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository providing custom private proxy nodes purchased for SecureVPN.
 * All public free servers (VPNGate, WireGuard public, VPNBook) are removed.
 */
@Singleton
class VpnServerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private val FALLBACK_SERVERS = listOf(
            VpnServer(
                id = "user_proxy_socks5_1",
                countryName = "United States",
                countryCode = "US",
                city = "New York",
                hostname = "151.247.124.10",
                ipAddress = "151.247.124.10",
                protocol = VpnProtocol.SOCKS5_PROXY,
                port = 50101,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "user_proxy_http_1",
                countryName = "United States",
                countryCode = "US",
                city = "New York",
                hostname = "151.247.124.10",
                ipAddress = "151.247.124.10",
                protocol = VpnProtocol.HTTP_PROXY,
                port = 50100,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "user_proxy_socks5_2",
                countryName = "United Kingdom",
                countryCode = "GB",
                city = "London",
                hostname = "151.247.124.11", // Placeholder IP
                ipAddress = "151.247.124.11",
                protocol = VpnProtocol.SOCKS5_PROXY,
                port = 50101,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "user_proxy_http_2",
                countryName = "Germany",
                countryCode = "DE",
                city = "Frankfurt",
                hostname = "151.247.124.12", // Placeholder IP
                ipAddress = "151.247.124.12",
                protocol = VpnProtocol.HTTP_PROXY,
                port = 50100,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "wireguard_example",
                countryName = "Japan",
                countryCode = "JP",
                city = "Tokyo",
                hostname = "tokyo-wg.example.com",
                ipAddress = "1.2.3.4",
                protocol = VpnProtocol.WIREGUARD,
                port = 51820,
                wgPublicKey = "YOUR_SERVER_PUBLIC_KEY",
                wgPrivateKey = "YOUR_CLIENT_PRIVATE_KEY",
                wgAddress = "10.0.0.2/32",
                wgEndpoint = "1.2.3.4:51820"
            ),
            VpnServer(
                id = "openvpn_example",
                countryName = "Canada",
                countryCode = "CA",
                city = "Toronto",
                hostname = "ca-ovpn.example.com",
                ipAddress = "5.6.7.8",
                protocol = VpnProtocol.OPENVPN_UDP,
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.8 1194\n..."
            )
        )
    }

    /**
     * Return only our purchased private proxy servers.
     */
    suspend fun fetchServers(): Result<List<VpnServer>> = withContext(Dispatchers.IO) {
        Result.success(FALLBACK_SERVERS)
    }

    fun getCachedFallbackServers(): List<VpnServer> = FALLBACK_SERVERS
}

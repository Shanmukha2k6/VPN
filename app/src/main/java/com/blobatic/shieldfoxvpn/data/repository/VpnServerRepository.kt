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
                id = "wireguard_japan",
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
                id = "openvpn_canada",
                countryName = "Canada",
                countryCode = "CA",
                city = "Toronto",
                hostname = "ca-ovpn.example.com",
                ipAddress = "5.6.7.8",
                protocol = VpnProtocol.OPENVPN_UDP,
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.8 1194\n..."
            ),
            VpnServer(
                id = "user_proxy_socks5_france",
                countryName = "France",
                countryCode = "FR",
                city = "Paris",
                hostname = "151.247.124.13",
                ipAddress = "151.247.124.13",
                protocol = VpnProtocol.SOCKS5_PROXY,
                port = 50101,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "user_proxy_http_netherlands",
                countryName = "Netherlands",
                countryCode = "NL",
                city = "Amsterdam",
                hostname = "151.247.124.14",
                ipAddress = "151.247.124.14",
                protocol = VpnProtocol.HTTP_PROXY,
                port = 50100,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "wireguard_singapore",
                countryName = "Singapore",
                countryCode = "SG",
                city = "Singapore",
                hostname = "sg-wg.example.com",
                ipAddress = "1.2.3.5",
                protocol = VpnProtocol.WIREGUARD,
                port = 51820,
                wgPublicKey = "YOUR_SG_SERVER_PUBLIC_KEY",
                wgPrivateKey = "YOUR_SG_CLIENT_PRIVATE_KEY",
                wgAddress = "10.0.0.3/32",
                wgEndpoint = "1.2.3.5:51820"
            ),
            VpnServer(
                id = "openvpn_australia",
                countryName = "Australia",
                countryCode = "AU",
                city = "Sydney",
                hostname = "au-ovpn.example.com",
                ipAddress = "5.6.7.9",
                protocol = VpnProtocol.OPENVPN_UDP,
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.9 1194\n..."
            ),
            VpnServer(
                id = "user_proxy_http_india",
                countryName = "India",
                countryCode = "IN",
                city = "Mumbai",
                hostname = "151.247.124.15",
                ipAddress = "151.247.124.15",
                protocol = VpnProtocol.HTTP_PROXY,
                port = 50100,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "user_proxy_socks5_korea",
                countryName = "South Korea",
                countryCode = "KR",
                city = "Seoul",
                hostname = "151.247.124.16",
                ipAddress = "151.247.124.16",
                protocol = VpnProtocol.SOCKS5_PROXY,
                port = 50101,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "user_proxy_http_switzerland",
                countryName = "Switzerland",
                countryCode = "CH",
                city = "Zurich",
                hostname = "151.247.124.17",
                ipAddress = "151.247.124.17",
                protocol = VpnProtocol.HTTP_PROXY,
                port = 50100,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
            ),
            VpnServer(
                id = "openvpn_brazil",
                countryName = "Brazil",
                countryCode = "BR",
                city = "Sao Paulo",
                hostname = "br-ovpn.example.com",
                ipAddress = "5.6.7.10",
                protocol = VpnProtocol.OPENVPN_UDP,
                port = 1194,
                ovpnConfig = "client\ndev tun\nproto udp\nremote 5.6.7.10 1194\n..."
            ),
            VpnServer(
                id = "wireguard_sweden",
                countryName = "Sweden",
                countryCode = "SE",
                city = "Stockholm",
                hostname = "se-wg.example.com",
                ipAddress = "1.2.3.6",
                protocol = VpnProtocol.WIREGUARD,
                port = 51820,
                wgPublicKey = "YOUR_SE_SERVER_PUBLIC_KEY",
                wgPrivateKey = "YOUR_SE_CLIENT_PRIVATE_KEY",
                wgAddress = "10.0.0.4/32",
                wgEndpoint = "1.2.3.6:51820"
            ),
            VpnServer(
                id = "user_proxy_http_spain",
                countryName = "Spain",
                countryCode = "ES",
                city = "Madrid",
                hostname = "151.247.124.18",
                ipAddress = "151.247.124.18",
                protocol = VpnProtocol.HTTP_PROXY,
                port = 50100,
                proxyUser = "shanmukha2k6",
                proxyPass = "CVwUQwnj2G",
                isPremium = false
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

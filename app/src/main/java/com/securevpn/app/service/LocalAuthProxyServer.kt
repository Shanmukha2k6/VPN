package com.securevpn.app.service

import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.concurrent.thread

/**
 * A local authentication proxy server.
 * Intercepts HTTP proxy requests from Android's VpnService, adds authentication headers,
 * and forwards them to the authenticated upstream HTTP or SOCKS5 proxy server.
 */
class LocalAuthProxyServer(
    private val vpnService: android.net.VpnService,
    val localPort: Int,
    private val upstreamHost: String,
    private val upstreamPort: Int,
    private val username: String,
    private val password: String,
    private val isUpstreamSocks: Boolean
) {
    companion object {
        private const val TAG = "LocalAuthProxyServer"
    }

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    fun start() {
        isRunning = true
        try {
            serverSocket = ServerSocket(localPort)
            Log.d(TAG, "Local proxy bridge started on port $localPort (Upstream: $upstreamHost:$upstreamPort, Socks: $isUpstreamSocks)")
            thread(start = true, isDaemon = true, name = "LocalProxyBridgeAcceptor") {
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        thread(start = true, isDaemon = true, name = "LocalProxyBridgeHandler") {
                            handleClient(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "Accept error", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start local proxy on port $localPort", e)
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverSocket = null
        Log.d(TAG, "Local proxy bridge stopped")
    }

    private fun handleClient(clientSocket: Socket) {
        var upstreamSocket: Socket? = null
        try {
            clientSocket.keepAlive = true
            val clientIn = clientSocket.getInputStream()
            val clientOut = clientSocket.getOutputStream()

            // 1. Read the request headers from the client
            val requestHeader = readHeader(clientIn)
            if (requestHeader == null || requestHeader.isBlank()) {
                return
            }

            // Parse destination host and port from request
            val firstLine = requestHeader.substringBefore("\r\n")
            val parts = firstLine.split(" ")
            if (parts.size < 2) return

            val method = parts[0]
            val url = parts[1]

            var destHost = ""
            var destPort = 80

            if (method.equals("CONNECT", ignoreCase = true)) {
                val hostPort = url.split(":")
                destHost = hostPort[0]
                if (hostPort.size > 1) {
                    destPort = hostPort[1].toIntOrNull() ?: 443
                } else {
                    destPort = 443
                }
            } else {
                // HTTP GET/POST etc.
                // Format: GET http://example.com/path HTTP/1.1 or /path
                if (url.startsWith("http://", ignoreCase = true)) {
                    val cleanUrl = url.substring(7)
                    val slashIdx = cleanUrl.indexOf('/')
                    val hostPortStr = if (slashIdx != -1) cleanUrl.substring(0, slashIdx) else cleanUrl
                    val hostPort = hostPortStr.split(":")
                    destHost = hostPort[0]
                    if (hostPort.size > 1) {
                        destPort = hostPort[1].toIntOrNull() ?: 80
                    }
                } else {
                    // Extract from Host header
                    val hostLine = requestHeader.lines().firstOrNull { it.startsWith("Host:", ignoreCase = true) }
                    if (hostLine != null) {
                        val hostPortStr = hostLine.substring(5).trim()
                        val hostPort = hostPortStr.split(":")
                        destHost = hostPort[0]
                        if (hostPort.size > 1) {
                            destPort = hostPort[1].toIntOrNull() ?: 80
                        }
                    }
                }
            }

            if (destHost.isBlank()) {
                return
            }

            // 2. Connect to upstream proxy
            var upstreamSocketConnected = false
            var activeSocket: Socket? = null
            var activeIn: InputStream? = null
            var activeOut: java.io.OutputStream? = null

            // Try SOCKS5 connection first if requested
            if (isUpstreamSocks) {
                var socket: Socket? = null
                try {
                    socket = Socket()
                    socket.keepAlive = true
                    vpnService.protect(socket)
                    socket.connect(java.net.InetSocketAddress(upstreamHost, upstreamPort), 5000)
                    
                    val out = socket.getOutputStream()
                    val `in` = socket.getInputStream()
                    
                    // SOCKS5 Greeting
                    out.write(byteArrayOf(0x05, 0x02, 0x00, 0x02))
                    out.flush()
                    
                    val greeting = ByteArray(2)
                    if (readFully(`in`, greeting) == 2 && greeting[0] == 0x05.toByte()) {
                        val socksAuthMethod = greeting[1].toInt()
                        var authOk = true
                        if (socksAuthMethod == 0x02) {
                            val userBytes = username.toByteArray(StandardCharsets.UTF_8)
                            val passBytes = password.toByteArray(StandardCharsets.UTF_8)
                            val authReq = ByteArray(3 + userBytes.size + passBytes.size)
                            authReq[0] = 0x01
                            authReq[1] = userBytes.size.toByte()
                            System.arraycopy(userBytes, 0, authReq, 2, userBytes.size)
                            authReq[2 + userBytes.size] = passBytes.size.toByte()
                            System.arraycopy(passBytes, 0, authReq, 3 + userBytes.size, passBytes.size)
                            
                            out.write(authReq)
                            out.flush()
                            
                            val authResp = ByteArray(2)
                            if (readFully(`in`, authResp) != 2 || authResp[1] != 0x00.toByte()) {
                                authOk = false
                            }
                        } else if (socksAuthMethod != 0x00) {
                            authOk = false
                        }
                        
                        if (authOk) {
                            // SOCKS5 connect request
                            val destHostBytes = destHost.toByteArray(StandardCharsets.UTF_8)
                            val connRequest = ByteArray(7 + destHostBytes.size)
                            connRequest[0] = 0x05
                            connRequest[1] = 0x01
                            connRequest[2] = 0x00
                            connRequest[3] = 0x03
                            connRequest[4] = destHostBytes.size.toByte()
                            System.arraycopy(destHostBytes, 0, connRequest, 5, destHostBytes.size)
                            connRequest[5 + destHostBytes.size] = (destPort shr 8).toByte()
                            connRequest[6 + destHostBytes.size] = destPort.toByte()
                            
                            out.write(connRequest)
                            out.flush()
                            
                            val reply = ByteArray(4)
                            if (readFully(`in`, reply) == 4 && reply[1] == 0x00.toByte()) {
                                val type = reply[3].toInt()
                                if (type == 0x01) readFully(`in`, ByteArray(4))
                                else if (type == 0x03) {
                                    val len = `in`.read()
                                    if (len != -1) readFully(`in`, ByteArray(len))
                                } else if (type == 0x04) readFully(`in`, ByteArray(16))
                                readFully(`in`, ByteArray(2))
                                
                                activeSocket = socket
                                activeIn = `in`
                                activeOut = out
                                upstreamSocketConnected = true
 
                                // Respond to client
                                if (method.lowercase() == "connect") {
                                    val response = "HTTP/1.1 200 Connection Established\r\n\r\n"
                                    clientOut.write(response.toByteArray(StandardCharsets.US_ASCII))
                                    clientOut.flush()
                                } else {
                                    activeOut.write(requestHeader.toByteArray(StandardCharsets.UTF_8))
                                    activeOut.flush()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    try { socket?.close() } catch (ex: Exception) {}
                }
            }

            // Fallback to HTTP proxy if SOCKS5 failed or was not selected
            if (!upstreamSocketConnected) {
                val httpPort = upstreamPort
                val socket = Socket()
                socket.keepAlive = true
                vpnService.protect(socket)
                socket.connect(java.net.InetSocketAddress(upstreamHost, httpPort), 5000)
                
                val out = socket.getOutputStream()
                val `in` = socket.getInputStream()
                
                val modifiedHeader = addProxyAuthHeader(requestHeader, username, password)
                out.write(modifiedHeader.toByteArray(StandardCharsets.UTF_8))
                out.flush()
                
                activeSocket = socket
                activeIn = `in`
                activeOut = out
                upstreamSocketConnected = true
            }

            if (!upstreamSocketConnected || activeSocket == null || activeIn == null || activeOut == null) {
                Log.e(TAG, "Upstream connection failed")
                return
            }

            upstreamSocket = activeSocket
            val upstreamIn = activeIn
            val upstreamOut = activeOut

            // 3. Start bi-directional piping
            val clientToUpstream = thread(start = true, isDaemon = true, name = "PipeClientToUpstream") {
                pipe(clientSocket, upstreamSocket, clientIn, upstreamOut)
            }
            val upstreamToClient = thread(start = true, isDaemon = true, name = "PipeUpstreamToClient") {
                pipe(upstreamSocket, clientSocket, upstreamIn, clientOut)
            }

            clientToUpstream.join()
            upstreamToClient.join()

        } catch (e: Exception) {
            // Log connection error
        } finally {
            try { clientSocket.close() } catch (e: Exception) {}
            try { upstreamSocket?.close() } catch (e: Exception) {}
        }
    }

    private fun readHeader(inputStream: InputStream): String? {
        val headerBuilder = StringBuilder()
        val buffer = ByteArray(1)
        while (inputStream.read(buffer) != -1) {
            val char = buffer[0].toInt().toChar()
            headerBuilder.append(char)
            if (headerBuilder.endsWith("\r\n\r\n")) {
                break
            }
        }
        return if (headerBuilder.isEmpty()) null else headerBuilder.toString()
    }

    private fun readFully(inputStream: InputStream, buffer: ByteArray): Int {
        var totalBytesRead = 0
        while (totalBytesRead < buffer.size) {
            val bytesRead = inputStream.read(buffer, totalBytesRead, buffer.size - totalBytesRead)
            if (bytesRead == -1) break
            totalBytesRead += bytesRead
        }
        return totalBytesRead
    }

    private fun addProxyAuthHeader(header: String, user: String, pass: String): String {
        val auth = "$user:$pass"
        val base64Auth = Base64.getEncoder().encodeToString(auth.toByteArray(StandardCharsets.UTF_8))
        
        val lines = header.split("\r\n").toMutableList()
        if (lines.isNotEmpty() && lines.last().isEmpty()) {
            lines.removeAt(lines.lastIndex)
        }

        val index = lines.indexOfFirst { it.startsWith("Proxy-Authorization:", ignoreCase = true) }
        if (index != -1) {
            lines[index] = "Proxy-Authorization: Basic $base64Auth"
        } else {
            lines.add("Proxy-Authorization: Basic $base64Auth")
        }

        return lines.joinToString("\r\n") + "\r\n\r\n"
    }

    private fun pipe(socket1: Socket, socket2: Socket, inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(32768)
        var bytesRead: Int
        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                outputStream.flush()
            }
        } catch (e: Exception) {
            // connection ended
        } finally {
            try { socket1.close() } catch (e: Exception) {}
            try { socket2.close() } catch (e: Exception) {}
        }
    }
}

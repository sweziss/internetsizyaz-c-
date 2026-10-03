package com.example.data.network

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

data class DiscoveredPc(
    val ip: String,
    val port: Int,
    val hostname: String,
    val os: String
)

class NetworkDiscovery(private val context: Context) {

    suspend fun discoverViaUdp(timeoutMs: Int = 2500): List<DiscoveredPc> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<DiscoveredPc>()
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = timeoutMs

            val message = "WIFI_PRINTER_DISCOVERY".toByteArray()
            val broadcastAddress = getBroadcastAddress() ?: InetAddress.getByName("255.255.255.255")
            val packet = DatagramPacket(message, message.size, broadcastAddress, 9100)
            socket.send(packet)

            val buffer = ByteArray(1024)
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                try {
                    val receivePacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(receivePacket)
                    val responseStr = String(receivePacket.data, 0, receivePacket.length)
                    val json = JSONObject(responseStr)
                    if (json.optString("type") == "WIFI_PRINTER_SERVER") {
                        val ip = receivePacket.address.hostAddress ?: ""
                        val port = json.optInt("port", 8080)
                        val hostname = json.optString("hostname", "PC-$ip")
                        val os = json.optString("os", "Windows")
                        if (discovered.none { it.ip == ip && it.port == port }) {
                            discovered.add(DiscoveredPc(ip, port, hostname, os))
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    break
                }
            }
        } catch (e: Exception) {
            // Ignore UDP broadcast failures on certain restricted networks
        } finally {
            socket?.close()
        }
        discovered
    }

    private fun getBroadcastAddress(): InetAddress? {
        return try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcp = wifi?.dhcpInfo ?: return null
            val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
            val quads = ByteArray(4)
            for (k in 0..3) {
                quads[k] = ((broadcast shr k * 8) and 0xFF).toByte()
            }
            InetAddress.getByAddress(quads)
        } catch (e: Exception) {
            null
        }
    }
}

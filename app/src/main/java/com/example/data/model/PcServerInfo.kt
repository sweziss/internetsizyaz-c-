package com.example.data.model

data class PcServerInfo(
    val ipAddress: String = "192.168.1.100",
    val port: Int = 8080,
    val name: String = "Ev / Ofis Bilgisayarı",
    val osName: String = "Windows 11 (PC)",
    val isConnected: Boolean = false,
    val isSimulated: Boolean = false,
    val pingMs: Long = 0L,
    val lastSeenTimestamp: Long = 0L,
    val printers: List<PrinterInfo> = emptyList()
) {
    val baseUrl: String
        get() = "http://$ipAddress:$port"
}

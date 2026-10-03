package com.example.data.model

enum class PrinterStatus(val label: String, val isReady: Boolean) {
    READY("Hazır / Çevrimiçi", true),
    PRINTING("Yazdırıyor...", true),
    IDLE("Beklemede", true),
    BUSY("Meşgul", false),
    PAPER_JAM("Kağıt Sıkışması!", false),
    OUT_OF_PAPER("Kağıt Bitti!", false),
    LOW_INK("Mürekkep / Toner Düşük", true),
    DOOR_OPEN("Kapak Açık", false),
    OFFLINE("Çevrimdışı / Bağlantı Yok", false)
}

data class PrinterInfo(
    val id: String,
    val name: String,
    val isDefault: Boolean = false,
    val status: PrinterStatus = PrinterStatus.READY,
    val isColorSupported: Boolean = true,
    val isDuplexSupported: Boolean = true,
    val driver: String = "Generic / Windows Spooler",
    val portName: String = "USB001 / WSD",
    val comment: String = "",
    val inkLevelPercent: Int = 85
)

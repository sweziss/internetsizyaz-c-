package com.example.data.model

enum class JobStatus(val label: String) {
    QUEUED("Kuyrukta"),
    CONNECTING("PC'ye Bağlanıyor"),
    SENDING("Veri Aktarılıyor"),
    SPOOLING("Yazıcı Kuyruğuna İletildi"),
    PRINTING("Yazdırılıyor..."),
    COMPLETED("Baskı Tamamlandı"),
    FAILED("Hata Oluştu"),
    CANCELLED("İptal Edildi")
}

data class PrintJob(
    val id: String,
    val title: String,
    val itemType: PrintItemType,
    val timestamp: Long = System.currentTimeMillis(),
    val status: JobStatus = JobStatus.QUEUED,
    val progressPercent: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val printerName: String,
    val hostAddress: String,
    val copies: Int = 1,
    val colorMode: ColorMode = ColorMode.MONOCHROME,
    val paperSize: PaperSize = PaperSize.A4,
    val duplexMode: DuplexMode = DuplexMode.OFF,
    val statusMessage: String = "Baskı işi hazırlanıyor...",
    val previewSnippet: String = ""
)

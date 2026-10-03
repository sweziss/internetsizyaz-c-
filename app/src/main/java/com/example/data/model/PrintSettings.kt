package com.example.data.model

enum class ColorMode(val title: String) {
    MONOCHROME("Siyah-Beyaz (Gri Tonlama)"),
    COLOR("Renkli (Tam Renk)")
}

enum class PageOrientation(val title: String) {
    PORTRAIT("Dikey"),
    LANDSCAPE("Yatay"),
    AUTO("Otomatik")
}

enum class PaperSize(val title: String, val dimensions: String, val widthMm: Float, val heightMm: Float) {
    A4("A4 Standart", "210 × 297 mm", 210f, 297f),
    A5("A5 Küçük Belge", "148 × 210 mm", 148f, 210f),
    A3("A3 Büyük Çizim", "297 × 420 mm", 297f, 420f),
    LETTER("Letter (Mektup)", "216 × 279 mm", 215.9f, 279.4f),
    LEGAL("Legal (Resmi)", "216 × 356 mm", 215.9f, 355.6f),
    PHOTO_10X15("Fotoğraf 10×15 cm", "100 × 150 mm (4×6 inç)", 100f, 150f),
    ENVELOPE_DL("Zarf DL", "110 × 220 mm", 110f, 220f)
}

enum class DuplexMode(val title: String, val description: String) {
    OFF("Tek Taraflı", "Sadece sayfanın ön yüzüne basılır"),
    LONG_EDGE("Çift Taraflı (Uzun Kenar)", "Standart kitap tarzı çevirme"),
    SHORT_EDGE("Çift Taraflı (Kısa Kenar)", "Takvim tarzı yukarıdan çevirme")
}

enum class PrintQuality(val title: String, val dpi: Int) {
    DRAFT("Taslak (150 DPI - Hızlı / Mürekkep Tasarrufu)", 150),
    NORMAL("Normal (300 DPI - Standart Ofis)", 300),
    HIGH("Yüksek (600 DPI - Net Metin ve Grafikler)", 600),
    PHOTO("Fotoğraf / Ultra (1200 DPI - Maksimum Detay)", 1200)
}

enum class MarginMode(val title: String, val marginMm: Int) {
    NORMAL("Standart (15 mm)", 15),
    NARROW("Dar (5 mm - Daha Çok İçerik)", 5),
    WIDE("Geniş (25 mm)", 25),
    BORDERLESS("Kenarlıksız (0 mm - Fotoğraf)", 0)
}

enum class PageScaling(val title: String) {
    FIT_PAGE("Sayfaya Sığdır (Önerilen)"),
    ACTUAL_SIZE("Gerçek Boyut (%100)"),
    FILL_PAGE("Sayfayı Doldur (Kırpma Olabilir)"),
    CUSTOM("Özel Ölçekleme (%)")
}

enum class PageRangeType(val title: String) {
    ALL("Tüm Sayfalar"),
    ODD_ONLY("Yalnızca Tek Sayfalar (1, 3, 5...)"),
    EVEN_ONLY("Yalnızca Çift Sayfalar (2, 4, 6...)"),
    CUSTOM("Özel Sayfa Aralığı")
}

data class PrintSettings(
    val printerId: String = "default",
    val printerName: String = "Varsayılan PC Yazıcısı",
    val copies: Int = 1,
    val colorMode: ColorMode = ColorMode.MONOCHROME,
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val paperSize: PaperSize = PaperSize.A4,
    val duplex: DuplexMode = DuplexMode.OFF,
    val quality: PrintQuality = PrintQuality.NORMAL,
    val margins: MarginMode = MarginMode.NORMAL,
    val scaling: PageScaling = PageScaling.FIT_PAGE,
    val customScalePercent: Int = 100,
    val pageRangeType: PageRangeType = PageRangeType.ALL,
    val customPageRange: String = "1-1",
    val pagesPerSheet: Int = 1, // 1, 2, 4
    val printHeaderDate: Boolean = false,
    val printPageNumbers: Boolean = true
)

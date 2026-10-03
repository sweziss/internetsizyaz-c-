package com.example.data.model

import android.net.Uri

enum class PrintItemType(val title: String) {
    DOCUMENT("Belge / PDF"),
    PHOTO("Fotoğraf / Resim"),
    NOTE("Not & Metin Belgesi"),
    WEB_PAGE("Web Sayfası"),
    CLIPBOARD("Pano İçeriği")
}

sealed class PrintItem(
    val type: PrintItemType,
    val title: String,
    val summary: String,
    val totalEstimatedPages: Int = 1
) {
    data class DocumentItem(
        val uri: Uri,
        val fileName: String,
        val mimeType: String,
        val fileSizeBytes: Long,
        val pages: Int = 1
    ) : PrintItem(
        type = PrintItemType.DOCUMENT,
        title = fileName,
        summary = "$mimeType • ${formatSize(fileSizeBytes)} • $pages Sayfa",
        totalEstimatedPages = pages
    )

    data class PhotoItem(
        val uri: Uri,
        val fileName: String,
        val width: Int = 1920,
        val height: Int = 1080
    ) : PrintItem(
        type = PrintItemType.PHOTO,
        title = fileName,
        summary = "Yüksek Çözünürlüklü Resim (${width}×${height})",
        totalEstimatedPages = 1
    )

    data class TextNoteItem(
        val noteTitle: String,
        val content: String,
        val templateName: String = "Serbest Not"
    ) : PrintItem(
        type = PrintItemType.NOTE,
        title = if (noteTitle.isNotBlank()) noteTitle else "Hızlı Not",
        summary = "$templateName • ${content.length} karakter",
        totalEstimatedPages = maxOf(1, (content.length / 1500) + 1)
    )

    data class WebPageItem(
        val url: String,
        val pageTitle: String
    ) : PrintItem(
        type = PrintItemType.WEB_PAGE,
        title = pageTitle.ifBlank { url },
        summary = "Web Sayfası Baskısı • $url",
        totalEstimatedPages = 2
    )

    data class ClipboardItem(
        val text: String
    ) : PrintItem(
        type = PrintItemType.CLIPBOARD,
        title = "Pano Metni",
        summary = "${text.take(60)}... (${text.length} karakter)",
        totalEstimatedPages = maxOf(1, (text.length / 1500) + 1)
    )

    companion object {
        private fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format("%.1f MB", mb)
                kb >= 1.0 -> String.format("%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}

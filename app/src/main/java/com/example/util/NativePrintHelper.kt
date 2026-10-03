package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayOutputStream

object NativePrintHelper {

    fun printImage(context: Context, imageUri: Uri, jobName: String = "WiFi_Yazici_Baski") {
        try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        @page { size: auto; margin: 0; }
                        body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; }
                        img { max-width: 100%; max-height: 100vh; object-fit: contain; }
                    </style>
                </head>
                <body>
                    <img src="data:image/jpeg;base64,$base64Image" />
                </body>
                </html>
            """.trimIndent()

            printHtmlOrText(context, jobName, htmlContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printHtmlOrText(context: Context, title: String, contentHtml: String) {
        val webView = WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter: PrintDocumentAdapter = view.createPrintDocumentAdapter(title)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("res1", "300dpi", 300, 300))
                        .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()

                    printManager?.print(title, printAdapter, attributes)
                }
            }
        }
        webView.loadDataWithBaseURL(null, contentHtml, "text/html", "UTF-8", null)
    }
}

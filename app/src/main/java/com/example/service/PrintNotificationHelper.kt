package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob

class PrintNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "wifi_printer_status_channel"
        const val CHANNEL_NAME = "Baskı ve Yazıcı Bildirimleri"
        const val NOTIFICATION_ID = 2024
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Yazıcı baskı durumu ve anlık ilerleme bildirimleri"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showJobProgress(job: PrintJob) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (job.status) {
            JobStatus.QUEUED -> "Kuyrukta Bekliyor: ${job.title}"
            JobStatus.CONNECTING -> "PC'ye Bağlanıyor..."
            JobStatus.SENDING -> "Veri Aktarılıyor: ${job.progressPercent}%"
            JobStatus.SPOOLING -> "Yazıcıya İletildi (${job.printerName})"
            JobStatus.PRINTING -> "Basılıyor: Sayfa ${job.currentPage}/${job.totalPages}"
            JobStatus.COMPLETED -> "Baskı Başarıyla Tamamlandı!"
            JobStatus.FAILED -> "Yazdırma Hatası!"
            JobStatus.CANCELLED -> "Baskı İptal Edildi"
        }

        val content = "${job.printerName} • ${job.paperSize.title} • ${job.copies} Kopya"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)

        if (job.status == JobStatus.SENDING || job.status == JobStatus.PRINTING || job.status == JobStatus.SPOOLING) {
            builder.setProgress(100, job.progressPercent, false)
            builder.setOngoing(true)
        } else {
            builder.setProgress(0, 0, false)
            builder.setOngoing(false)
            builder.setAutoCancel(true)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

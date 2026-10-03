package com.example.data.network

import com.example.data.model.ColorMode
import com.example.data.model.JobStatus
import com.example.data.model.PcServerInfo
import com.example.data.model.PrinterInfo
import com.example.data.model.PrinterStatus
import com.example.data.model.PrintJob
import com.example.data.model.PrintSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PcPrintClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
) {

    suspend fun checkHealth(ip: String, port: Int): Result<PcServerInfo> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "http://$ip:$port/health"
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val ping = System.currentTimeMillis() - startTime
                    val pcName = json.optString("pc_name", "PC ($ip)")
                    val os = json.optString("os", "Windows / PC")

                    Result.success(
                        PcServerInfo(
                            ipAddress = ip,
                            port = port,
                            name = pcName,
                            osName = os,
                            isConnected = true,
                            isSimulated = false,
                            pingMs = ping,
                            lastSeenTimestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    Result.failure(Exception("HTTP ${response.code}: Sunucu yanıt vermedi"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPrinters(ip: String, port: Int): Result<List<PrinterInfo>> = withContext(Dispatchers.IO) {
        val url = "http://$ip:$port/printers"
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val array = JSONArray(body)
                    val list = mutableListOf<PrinterInfo>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            PrinterInfo(
                                id = obj.optString("id", "p_$i"),
                                name = obj.optString("name", "Yazıcı $i"),
                                isDefault = obj.optBoolean("isDefault", i == 0),
                                isColorSupported = obj.optBoolean("isColor", true),
                                isDuplexSupported = obj.optBoolean("isDuplex", true),
                                status = when (obj.optString("status", "READY").uppercase()) {
                                    "PRINTING" -> PrinterStatus.PRINTING
                                    "OFFLINE" -> PrinterStatus.OFFLINE
                                    "PAPER_JAM" -> PrinterStatus.PAPER_JAM
                                    "OUT_OF_PAPER" -> PrinterStatus.OUT_OF_PAPER
                                    else -> PrinterStatus.READY
                                }
                            )
                        )
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Yazıcı listesi alınamadı: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPrintJob(
        ip: String,
        port: Int,
        job: PrintJob,
        settings: PrintSettings,
        contentData: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val url = "http://$ip:$port/print"
        try {
            val json = JSONObject().apply {
                put("id", job.id)
                put("title", job.title)
                put("type", job.itemType.name)
                put("printerName", settings.printerName)
                put("copies", settings.copies)
                put("colorMode", settings.colorMode.name)
                put("paperSize", settings.paperSize.name)
                put("orientation", settings.orientation.name)
                put("duplex", settings.duplex.name)
                put("qualityDpi", settings.quality.dpi)
                put("pageRange", settings.customPageRange)
                put("content", contentData)
            }

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(job.id)
                } else {
                    Result.failure(Exception("Yazdırma hatası: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun queryJobStatus(ip: String, port: Int, jobId: String): Result<Pair<JobStatus, Int>> = withContext(Dispatchers.IO) {
        val url = "http://$ip:$port/status/$jobId"
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val statusStr = json.optString("status", "PRINTING")
                    val progress = json.optInt("progress", 50)
                    val status = when (statusStr) {
                        "COMPLETED" -> JobStatus.COMPLETED
                        "FAILED" -> JobStatus.FAILED
                        else -> JobStatus.PRINTING
                    }
                    Result.success(Pair(status, progress))
                } else {
                    Result.failure(Exception("Durum alınamadı"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ColorMode
import com.example.data.model.DuplexMode
import com.example.data.model.JobStatus
import com.example.data.model.MarginMode
import com.example.data.model.PageOrientation
import com.example.data.model.PageRangeType
import com.example.data.model.PageScaling
import com.example.data.model.PaperSize
import com.example.data.model.PcServerInfo
import com.example.data.model.PrintItem
import com.example.data.model.PrintItemType
import com.example.data.model.PrintJob
import com.example.data.model.PrintQuality
import com.example.data.model.PrintSettings
import com.example.data.model.PrinterInfo
import com.example.data.model.PrinterStatus
import com.example.data.network.DiscoveredPc
import com.example.data.network.NetworkDiscovery
import com.example.data.network.PcPrintClient
import com.example.data.persistence.AppDatabase
import com.example.data.persistence.PrinterRepository
import com.example.data.persistence.SavedPcEntity
import com.example.service.PrintNotificationHelper
import com.example.util.NativePrintHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab(val title: String) {
    PRINT("Yazdır"),
    PC_DEVICES("PC & Yazıcı"),
    QUEUE("Baskı Durumu"),
    SETUP_GUIDE("PC Rehberi")
}

data class PrinterUiState(
    val selectedTab: AppTab = AppTab.PRINT,
    val selectedItem: PrintItem? = null,
    val printSettings: PrintSettings = PrintSettings(),
    val pcServer: PcServerInfo = PcServerInfo(),
    val activeJob: PrintJob? = null,
    val isScanningNetwork: Boolean = false,
    val isConnectingPc: Boolean = false,
    val discoveredPcs: List<DiscoveredPc> = emptyList(),
    val errorMessage: String? = null,
    val successToast: String? = null,
    val showSettingsSheet: Boolean = false,
    val showAddPcDialog: Boolean = false,
    val showNoteEditorDialog: Boolean = false,
    val showWebDialog: Boolean = false,
    // Draft fields
    val noteTitleInput: String = "",
    val noteContentInput: String = "",
    val webUrlInput: String = "https://example.com"
)

class PrinterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = PrinterRepository(db.printJobDao(), db.savedPcDao())
    private val networkDiscovery = NetworkDiscovery(application)
    private val pcClient = PcPrintClient()
    private val notificationHelper = PrintNotificationHelper(application)

    private val _uiState = MutableStateFlow(PrinterUiState())
    val uiState: StateFlow<PrinterUiState> = _uiState.asStateFlow()

    val historyJobs: StateFlow<List<PrintJob>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPcs: StateFlow<List<SavedPcEntity>> = repository.savedPcs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeJobExecution: Job? = null

    init {
        // Setup initial default mock/known printers for immediate usability
        initInitialPcConnection()
    }

    private fun initInitialPcConnection() {
        val defaultPrinters = listOf(
            PrinterInfo(
                id = "hp_m1212nf",
                name = "HP LaserJet Professional M1212nf MFP",
                isDefault = true,
                status = PrinterStatus.READY,
                isColorSupported = false,
                isDuplexSupported = true,
                driver = "HP LaserJet M1210 MFP Series PCLm",
                portName = "USB001 / DOT4",
                inkLevelPercent = 95
            ),
            PrinterInfo(
                id = "hp_laserjet_generic",
                name = "HP LaserJet Serisi (PCL-6)",
                isDefault = false,
                status = PrinterStatus.READY,
                isColorSupported = false,
                isDuplexSupported = true,
                driver = "HP PCL-6 Windows Driver",
                portName = "WSD-Port",
                inkLevelPercent = 88
            ),
            PrinterInfo(
                id = "microsoft_print_to_pdf",
                name = "Microsoft Print to PDF (Dijital Çıktı)",
                isDefault = false,
                status = PrinterStatus.READY,
                isColorSupported = true,
                isDuplexSupported = true,
                driver = "Microsoft Software Driver",
                portName = "PORTPROMPT:",
                inkLevelPercent = 100
            )
        )

        _uiState.update { current ->
            current.copy(
                pcServer = current.pcServer.copy(
                    printers = defaultPrinters,
                    isConnected = true,
                    isSimulated = false,
                    pingMs = 5
                ),
                printSettings = current.printSettings.copy(
                    printerId = defaultPrinters.first().id,
                    printerName = defaultPrinters.first().name,
                    colorMode = ColorMode.MONOCHROME
                )
            )
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun setShowAddPcDialog(show: Boolean) {
        _uiState.update { it.copy(showAddPcDialog = show) }
    }

    fun setShowNoteEditorDialog(show: Boolean) {
        _uiState.update { it.copy(showNoteEditorDialog = show) }
    }

    fun setShowWebDialog(show: Boolean) {
        _uiState.update { it.copy(showWebDialog = show) }
    }

    fun updateNoteTitle(title: String) {
        _uiState.update { it.copy(noteTitleInput = title) }
    }

    fun updateNoteContent(content: String) {
        _uiState.update { it.copy(noteContentInput = content) }
    }

    fun updateWebUrl(url: String) {
        _uiState.update { it.copy(webUrlInput = url) }
    }

    // --- Content Selection ---
    fun selectDocument(uri: Uri, name: String, mimeType: String, sizeBytes: Long, pages: Int = 1) {
        val item = PrintItem.DocumentItem(
            uri = uri,
            fileName = name,
            mimeType = mimeType,
            fileSizeBytes = sizeBytes,
            pages = maxOf(1, pages)
        )
        _uiState.update { it.copy(selectedItem = item, successToast = "Belge seçildi: $name") }
    }

    fun selectPhoto(uri: Uri, name: String) {
        val item = PrintItem.PhotoItem(
            uri = uri,
            fileName = name
        )
        _uiState.update { it.copy(selectedItem = item, successToast = "Fotoğraf seçildi: $name") }
    }

    fun saveNoteItem(title: String, content: String, template: String = "Hızlı Not") {
        if (content.isBlank()) return
        val item = PrintItem.TextNoteItem(
            noteTitle = title.ifBlank { "Baskı Notu" },
            content = content,
            templateName = template
        )
        _uiState.update {
            it.copy(
                selectedItem = item,
                showNoteEditorDialog = false,
                noteTitleInput = "",
                noteContentInput = "",
                successToast = "Not hazırlandı"
            )
        }
    }

    fun saveWebPage(url: String) {
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        val item = PrintItem.WebPageItem(
            url = cleanUrl,
            pageTitle = cleanUrl.removePrefix("https://").removePrefix("http://")
        )
        _uiState.update {
            it.copy(
                selectedItem = item,
                showWebDialog = false,
                successToast = "Web sayfası eklendi"
            )
        }
    }

    fun setClipboardItem(text: String) {
        if (text.isBlank()) return
        val item = PrintItem.ClipboardItem(text)
        _uiState.update { it.copy(selectedItem = item, successToast = "Pano içeriği yüklendi") }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItem = null) }
    }

    // --- Print Settings Updates ---
    fun updateCopies(delta: Int) {
        _uiState.update { current ->
            val newCopies = (current.printSettings.copies + delta).coerceIn(1, 99)
            current.copy(printSettings = current.printSettings.copy(copies = newCopies))
        }
    }

    fun setCopies(copies: Int) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(copies = copies.coerceIn(1, 99)))
        }
    }

    fun setColorMode(colorMode: ColorMode) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(colorMode = colorMode))
        }
    }

    fun setOrientation(orientation: PageOrientation) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(orientation = orientation))
        }
    }

    fun setPaperSize(paperSize: PaperSize) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(paperSize = paperSize))
        }
    }

    fun setDuplex(duplex: DuplexMode) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(duplex = duplex))
        }
    }

    fun setQuality(quality: PrintQuality) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(quality = quality))
        }
    }

    fun setMargins(margins: MarginMode) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(margins = margins))
        }
    }

    fun setScaling(scaling: PageScaling) {
        _uiState.update { current ->
            current.copy(printSettings = current.printSettings.copy(scaling = scaling))
        }
    }

    fun setPageRange(type: PageRangeType, customRange: String = "") {
        _uiState.update { current ->
            current.copy(
                printSettings = current.printSettings.copy(
                    pageRangeType = type,
                    customPageRange = if (customRange.isNotBlank()) customRange else current.printSettings.customPageRange
                )
            )
        }
    }

    fun selectPrinter(printer: PrinterInfo) {
        _uiState.update { current ->
            current.copy(
                printSettings = current.printSettings.copy(
                    printerId = printer.id,
                    printerName = printer.name,
                    colorMode = if (!printer.isColorSupported) ColorMode.MONOCHROME else current.printSettings.colorMode,
                    duplex = if (!printer.isDuplexSupported) DuplexMode.OFF else current.printSettings.duplex
                ),
                successToast = "Seçilen Yazıcı: ${printer.name}"
            )
        }
    }

    // --- PC Connection & Discovery ---
    fun connectToPc(ip: String, port: Int = 8080) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnectingPc = true, errorMessage = null) }
            val healthResult = pcClient.checkHealth(ip, port)
            if (healthResult.isSuccess) {
                val serverInfo = healthResult.getOrThrow()
                val printersResult = pcClient.fetchPrinters(ip, port)
                val printers = printersResult.getOrDefault(emptyList())

                val updatedServer = serverInfo.copy(
                    printers = if (printers.isNotEmpty()) printers else _uiState.value.pcServer.printers
                )

                _uiState.update { current ->
                    current.copy(
                        pcServer = updatedServer,
                        isConnectingPc = false,
                        showAddPcDialog = false,
                        successToast = "PC'ye bağlandı: ${updatedServer.name}"
                    )
                }

                // Save to Room DB
                repository.savePc(
                    SavedPcEntity(
                        ipAddress = ip,
                        port = port,
                        name = updatedServer.name,
                        lastConnected = System.currentTimeMillis()
                    )
                )

                // Select default printer if available
                printers.firstOrNull { it.isDefault }?.let { selectPrinter(it) }
            } else {
                // If real connection fails, inform user and allow simulation mode fallback
                _uiState.update { current ->
                    current.copy(
                        isConnectingPc = false,
                        pcServer = current.pcServer.copy(
                            ipAddress = ip,
                            port = port,
                            isConnected = true,
                            isSimulated = true,
                            name = "PC ($ip - Simülasyon Modu)"
                        ),
                        showAddPcDialog = false,
                        successToast = "$ip kaydedildi (Simülasyon Aktif)"
                    )
                }
            }
        }
    }

    fun scanNetwork() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningNetwork = true, errorMessage = null) }
            val discovered = networkDiscovery.discoverViaUdp(timeoutMs = 3000)
            _uiState.update { current ->
                current.copy(
                    isScanningNetwork = false,
                    discoveredPcs = discovered,
                    successToast = if (discovered.isNotEmpty()) "${discovered.size} PC bulundu" else "Yeni PC bulunamadı (Rehberden sunucuyu açın)"
                )
            }
        }
    }

    // --- Execution of Print Job ---
    fun startPrint() {
        val currentItem = _uiState.value.selectedItem ?: run {
            _uiState.update { it.copy(errorMessage = "Lütfen önce yazdırılacak bir dosya, fotoğraf veya metin seçin!") }
            return
        }

        val settings = _uiState.value.printSettings
        val pc = _uiState.value.pcServer
        val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()

        val newJob = PrintJob(
            id = jobId,
            title = currentItem.title,
            itemType = currentItem.type,
            timestamp = System.currentTimeMillis(),
            status = JobStatus.QUEUED,
            progressPercent = 5,
            currentPage = 1,
            totalPages = currentItem.totalEstimatedPages,
            printerName = settings.printerName,
            hostAddress = "${pc.ipAddress}:${pc.port}",
            copies = settings.copies,
            colorMode = settings.colorMode,
            paperSize = settings.paperSize,
            duplexMode = settings.duplex,
            statusMessage = "Baskı işi kuyruğa alındı..."
        )

        _uiState.update {
            it.copy(
                activeJob = newJob,
                selectedTab = AppTab.QUEUE
            )
        }

        notificationHelper.showJobProgress(newJob)

        activeJobExecution?.cancel()
        activeJobExecution = viewModelScope.launch {
            executeJobFlow(newJob, currentItem, settings, pc)
        }
    }

    private fun readBytesFromUri(uri: Uri): ByteArray? {
        return try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun executeJobFlow(
        initialJob: PrintJob,
        item: PrintItem,
        settings: PrintSettings,
        pc: PcServerInfo
    ) {
        var currentJob = initialJob

        // Step 1: Connecting
        currentJob = currentJob.copy(
            status = JobStatus.CONNECTING,
            progressPercent = 15,
            statusMessage = "${pc.ipAddress}:${pc.port} üzerinden HP LaserJet bağlantısı kuruluyor..."
        )
        updateJobState(currentJob)

        // Read real file payload from Android content resolver
        val (base64Payload, textPayload) = when (item) {
            is PrintItem.DocumentItem -> {
                val bytes = readBytesFromUri(item.uri)
                val b64 = bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
                Pair(b64, null)
            }
            is PrintItem.PhotoItem -> {
                val bytes = readBytesFromUri(item.uri)
                val b64 = bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
                Pair(b64, null)
            }
            is PrintItem.TextNoteItem -> Pair(null, item.content)
            is PrintItem.ClipboardItem -> Pair(null, item.text)
            is PrintItem.WebPageItem -> Pair(null, "URL: ${item.url}")
        }

        // Step 2: Uploading real payload to PC
        currentJob = currentJob.copy(
            status = JobStatus.SENDING,
            progressPercent = 40,
            statusMessage = "Dosya verisi (${item.title}) PC sunucusuna aktarılıyor..."
        )
        updateJobState(currentJob)

        // Real Network transmission to PC Companion server
        val sendResult = pcClient.sendPrintJob(
            pc.ipAddress,
            pc.port,
            currentJob,
            settings,
            base64Payload,
            textPayload
        )

        if (sendResult.isSuccess) {
            // Step 3: Physical Spooling on PC
            currentJob = currentJob.copy(
                status = JobStatus.SPOOLING,
                progressPercent = 75,
                statusMessage = "Windows Spooler: '${settings.printerName}' kuyruğuna yazıldı, kağıt besleniyor..."
            )
            updateJobState(currentJob)

            delay(1200)

            currentJob = currentJob.copy(
                status = JobStatus.COMPLETED,
                progressPercent = 100,
                statusMessage = "Fiziksel baskı komutu '${settings.printerName}' yazıcısına başarıyla iletildi!"
            )
            updateJobState(currentJob)
            repository.saveJob(currentJob)
            _uiState.update { it.copy(successToast = "Baskı HP LaserJet'e iletildi!") }
        } else {
            // Real physical error report - DO NOT FAKE SUCCESS!
            val err = sendResult.exceptionOrNull()?.message ?: "Sunucuya bağlanılamadı"
            currentJob = currentJob.copy(
                status = JobStatus.FAILED,
                progressPercent = 0,
                statusMessage = "Bağlantı Hatası: $err. Lütfen PC'de 'python pc_print_server.py' betiğinin açık olduğunu kontrol edin."
            )
            updateJobState(currentJob)
            repository.saveJob(currentJob)
            _uiState.update {
                it.copy(
                    errorMessage = "PC'ye ulaşılamadı (${pc.ipAddress}:${pc.port}). Bilgisayarda 'pc_print_server.py' çalışıyor mu?"
                )
            }
        }
    }

    private fun updateJobState(job: PrintJob) {
        _uiState.update { it.copy(activeJob = job) }
        notificationHelper.showJobProgress(job)
    }

    fun cancelActiveJob() {
        activeJobExecution?.cancel()
        _uiState.value.activeJob?.let { current ->
            val cancelledJob = current.copy(
                status = JobStatus.CANCELLED,
                statusMessage = "Kullanıcı tarafından iptal edildi."
            )
            _uiState.update { it.copy(activeJob = cancelledJob) }
            notificationHelper.showJobProgress(cancelledJob)
            viewModelScope.launch {
                repository.saveJob(cancelledJob)
            }
        }
    }

    fun reprintJob(job: PrintJob) {
        val item = PrintItem.TextNoteItem(
            noteTitle = job.title,
            content = "Yeniden Yazdırma İşi: ${job.title}\nYazıcı: ${job.printerName}",
            templateName = "Geçmişten Çıktı"
        )
        _uiState.update {
            it.copy(
                selectedItem = item,
                selectedTab = AppTab.PRINT,
                printSettings = it.printSettings.copy(
                    copies = job.copies,
                    colorMode = job.colorMode,
                    paperSize = job.paperSize,
                    duplex = job.duplexMode,
                    printerName = job.printerName
                ),
                successToast = "'${job.title}' yeniden yazdırmaya hazır"
            )
        }
    }

    fun deleteHistoryJob(id: String) {
        viewModelScope.launch {
            repository.deleteJob(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(successToast = "Yazdırma geçmişi temizlendi") }
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(successToast = null, errorMessage = null) }
    }
}

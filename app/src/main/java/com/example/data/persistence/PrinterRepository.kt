package com.example.data.persistence

import com.example.data.model.ColorMode
import com.example.data.model.DuplexMode
import com.example.data.model.JobStatus
import com.example.data.model.PaperSize
import com.example.data.model.PrintItemType
import com.example.data.model.PrintJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrinterRepository(
    private val printJobDao: PrintJobDao,
    private val savedPcDao: SavedPcDao
) {
    val allJobs: Flow<List<PrintJob>> = printJobDao.getAllJobs().map { list ->
        list.map { it.toDomain() }
    }

    val savedPcs: Flow<List<SavedPcEntity>> = savedPcDao.getAllSavedPcs()

    suspend fun saveJob(job: PrintJob) {
        printJobDao.insertOrUpdate(job.toEntity())
    }

    suspend fun updateJob(job: PrintJob) {
        printJobDao.insertOrUpdate(job.toEntity())
    }

    suspend fun deleteJob(id: String) {
        printJobDao.deleteById(id)
    }

    suspend fun clearHistory() {
        printJobDao.clearAll()
    }

    suspend fun savePc(pc: SavedPcEntity) {
        savedPcDao.insert(pc)
    }

    suspend fun removePc(id: Int) {
        savedPcDao.deleteById(id)
    }

    private fun PrintJobEntity.toDomain(): PrintJob {
        return PrintJob(
            id = id,
            title = title,
            itemType = try { PrintItemType.valueOf(itemType) } catch (e: Exception) { PrintItemType.DOCUMENT },
            timestamp = timestamp,
            status = try { JobStatus.valueOf(status) } catch (e: Exception) { JobStatus.COMPLETED },
            progressPercent = progressPercent,
            currentPage = currentPage,
            totalPages = totalPages,
            printerName = printerName,
            hostAddress = hostAddress,
            copies = copies,
            colorMode = try { ColorMode.valueOf(colorMode) } catch (e: Exception) { ColorMode.MONOCHROME },
            paperSize = try { PaperSize.valueOf(paperSize) } catch (e: Exception) { PaperSize.A4 },
            duplexMode = try { DuplexMode.valueOf(duplexMode) } catch (e: Exception) { DuplexMode.OFF },
            statusMessage = statusMessage,
            previewSnippet = previewSnippet
        )
    }

    private fun PrintJob.toEntity(): PrintJobEntity {
        return PrintJobEntity(
            id = id,
            title = title,
            itemType = itemType.name,
            timestamp = timestamp,
            status = status.name,
            progressPercent = progressPercent,
            currentPage = currentPage,
            totalPages = totalPages,
            printerName = printerName,
            hostAddress = hostAddress,
            copies = copies,
            colorMode = colorMode.name,
            paperSize = paperSize.name,
            duplexMode = duplexMode.name,
            statusMessage = statusMessage,
            previewSnippet = previewSnippet
        )
    }
}

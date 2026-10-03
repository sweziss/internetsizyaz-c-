package com.example.data.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "print_jobs")
data class PrintJobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val itemType: String,
    val timestamp: Long,
    val status: String,
    val progressPercent: Int,
    val currentPage: Int,
    val totalPages: Int,
    val printerName: String,
    val hostAddress: String,
    val copies: Int,
    val colorMode: String,
    val paperSize: String,
    val duplexMode: String,
    val statusMessage: String,
    val previewSnippet: String
)

@Entity(tableName = "saved_pc_servers")
data class SavedPcEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ipAddress: String,
    val port: Int,
    val name: String,
    val lastConnected: Long,
    val isFavorite: Boolean = false
)

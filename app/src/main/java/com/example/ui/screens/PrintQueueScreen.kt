package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob
import com.example.ui.components.LiveStatusBanner
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCardElevated
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PrinterUiState
import com.example.ui.viewmodel.PrinterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrintQueueScreen(
    uiState: PrinterUiState,
    historyJobs: List<PrintJob>,
    viewModel: PrinterViewModel,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("print_queue_screen")
    ) {
        // Active Print Job Section (if running)
        if (uiState.activeJob != null) {
            item {
                Text(
                    text = "CANLI BASKI DURUMU",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                LiveStatusBanner(
                    job = uiState.activeJob,
                    onCancel = { viewModel.cancelActiveJob() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // History Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YAZDIRMA GEÇMİŞİ (${historyJobs.size})",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (historyJobs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.size(32.dp).testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Geçmişi Temizle",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (historyJobs.isEmpty() && uiState.activeJob == null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AmoledCardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Henüz bir baskı işi yapılmadı",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Belge veya fotoğraf seçip yazdırmaya başlayın",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        items(historyJobs, key = { it.id }) { job ->
            HistoryJobItem(
                job = job,
                onReprint = { viewModel.reprintJob(job) },
                onDelete = { viewModel.deleteHistoryJob(job.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = AmoledCardSurface,
            title = {
                Text("Geçmişi Temizle", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Tüm yazdırma kayıtlarını silmek istediğinize emin misiniz?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError, contentColor = Color.White)
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun HistoryJobItem(
    job: PrintJob,
    onReprint: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (job.status) {
        JobStatus.COMPLETED -> StatusSuccess
        JobStatus.FAILED, JobStatus.CANCELLED -> StatusError
        JobStatus.PRINTING -> CyanNeon
        else -> StatusWarning
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("tr")) }
    val formattedDate = remember(job.timestamp) { dateFormat.format(Date(job.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AmoledCardSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("history_item_${job.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (job.status) {
                                JobStatus.COMPLETED -> Icons.Default.CheckCircle
                                JobStatus.FAILED, JobStatus.CANCELLED -> Icons.Default.ErrorOutline
                                else -> Icons.Default.Print
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = job.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formattedDate,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = job.status.label,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${job.printerName} • ${job.paperSize.title} • ${job.copies} Kopya",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onReprint,
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF003844)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tekrar Yazdır", color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.PrinterInfo
import com.example.data.model.PrinterStatus
import com.example.data.persistence.SavedPcEntity
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

@Composable
fun PcConnectionScreen(
    uiState: PrinterUiState,
    savedPcs: List<SavedPcEntity>,
    viewModel: PrinterViewModel,
    modifier: Modifier = Modifier
) {
    var showManualIpDialog by remember { mutableStateOf(false) }
    var inputIp by remember { mutableStateOf(uiState.pcServer.ipAddress) }
    var inputPort by remember { mutableStateOf(uiState.pcServer.port.toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("pc_connection_screen")
    ) {
        // Active PC Server Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AmoledCardSurface)
                    .border(
                        1.dp,
                        if (uiState.pcServer.isConnected) Color(0xFF005466) else BorderSubtle,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF003844)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = uiState.pcServer.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.pcServer.ipAddress}:${uiState.pcServer.port}",
                                color = CyanNeon,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.pcServer.isConnected) Color(0x2200E676) else Color(0x22FFB300)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (uiState.pcServer.isConnected) "BAĞLI" else "BEKLİYOR",
                            color = if (uiState.pcServer.isConnected) StatusSuccess else StatusWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "İşletim Sistemi", color = TextMuted, fontSize = 11.sp)
                        Text(text = uiState.pcServer.osName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Column {
                        Text(text = "WiFi Gecikmesi", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${uiState.pcServer.pingMs} ms", color = StatusSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Mod", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = if (uiState.pcServer.isSimulated) "Simülasyon / Test" else "Canlı Köprü",
                            color = if (uiState.pcServer.isSimulated) Color(0xFF38BDF8) else CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons: Scan LAN & Enter Manual IP
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.scanNetwork() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("scan_network_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmoledCardSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    if (uiState.isScanningNetwork) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyanNeon,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isScanningNetwork) "Taranıyor..." else "Ağda PC Ara",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { showManualIpDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("manual_ip_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF003844),
                        contentColor = CyanNeon
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manuel IP Gir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Discovered PCs on network (if any found)
        if (uiState.discoveredPcs.isNotEmpty()) {
            item {
                Text(
                    text = "AĞDA BULUNAN BİLGİSAYARLAR",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
            }

            items(uiState.discoveredPcs) { discovered ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmoledCardSurface)
                        .border(1.dp, Color(0xFF233045), RoundedCornerShape(12.dp))
                        .clickable { viewModel.connectToPc(discovered.ip, discovered.port) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = discovered.hostname, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${discovered.ip}:${discovered.port} • ${discovered.os}", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(text = "Bağlan", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Section: Connected PC Printers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BİLGİSAYARDAKİ YAZICILAR (${uiState.pcServer.printers.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Dokun ve Seç",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        items(uiState.pcServer.printers) { printer ->
            val isSelected = uiState.printSettings.printerId == printer.id
            PrinterCardItem(
                printer = printer,
                isSelected = isSelected,
                onClick = { viewModel.selectPrinter(printer) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Saved PC Servers (Room DB)
        if (savedPcs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "KAYITLI BİLGİSAYAR PROFİLLERİ",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            items(savedPcs) { saved ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmoledCardSurface)
                        .clickable { viewModel.connectToPc(saved.ipAddress, saved.port) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = saved.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${saved.ipAddress}:${saved.port}", color = TextSecondary, fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = { viewModel.connectToPc(saved.ipAddress, saved.port) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Bağlan", tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    // Manual IP Entry Dialog
    if (showManualIpDialog) {
        AlertDialog(
            onDismissRequest = { showManualIpDialog = false },
            containerColor = AmoledCardSurface,
            title = {
                Text(
                    text = "Bilgisayar IP ve Port Girin",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bilgisayarınızın yerel WiFi ağındaki IP adresini ve sunucu portunu girin:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputIp,
                        onValueChange = { inputIp = it },
                        label = { Text("PC IP Adresi") },
                        placeholder = { Text("192.168.1.100") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_pc_ip")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputPort,
                        onValueChange = { inputPort = it },
                        label = { Text("Port (Varsayılan: 8080)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_pc_port")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val portNum = inputPort.toIntOrNull() ?: 8080
                        viewModel.connectToPc(inputIp, portNum)
                        showManualIpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_connect_pc")
                ) {
                    Text("Bağlan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualIpDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PrinterCardItem(
    printer: PrinterInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) CyanNeon else BorderSubtle
    val bgColor = if (isSelected) Color(0xFF0C2430) else AmoledCardSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("printer_card_${printer.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CyanNeon else AmoledCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = printer.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (printer.isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF003844))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Varsayılan", color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "${printer.driver} • ${printer.portName}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seçildi",
                        tint = CyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Printer specs & Ink level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = printer.status)
                    if (printer.isColorSupported) {
                        FeatureTag("Renkli")
                    }
                    if (printer.isDuplexSupported) {
                        FeatureTag("Çift Taraflı")
                    }
                }

                // Ink level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (printer.inkLevelPercent > 20) CyanNeon else StatusWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Toner: %${printer.inkLevelPercent}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: PrinterStatus) {
    val (bg, textColor) = when (status) {
        PrinterStatus.READY, PrinterStatus.IDLE -> Pair(Color(0x2200E676), StatusSuccess)
        PrinterStatus.PRINTING -> Pair(Color(0x2200E5FF), CyanNeon)
        PrinterStatus.LOW_INK -> Pair(Color(0x22FFB300), StatusWarning)
        else -> Pair(Color(0x22FF5252), StatusError)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = status.label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeatureTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AmoledCardElevated)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

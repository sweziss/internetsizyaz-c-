package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorMode
import com.example.data.model.DuplexMode
import com.example.data.model.MarginMode
import com.example.data.model.PageOrientation
import com.example.data.model.PageRangeType
import com.example.data.model.PageScaling
import com.example.data.model.PaperSize
import com.example.data.model.PrintQuality
import com.example.data.model.PrintSettings
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCardElevated
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintSettingsSheet(
    settings: PrintSettings,
    onSettingsChanged: (PrintSettings) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var localSettings by remember(settings) { mutableStateOf(settings) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("print_settings_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Yazıcı & Sayfa Ayarları",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Kopya Sayısı (Copies Stepper)
            SettingSectionHeader(title = "KOPYA SAYISI")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmoledCardSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${localSettings.copies} Adet Kopya",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Her bir sayfanın basılacak adedi",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = {
                            if (localSettings.copies > 1) {
                                localSettings = localSettings.copy(copies = localSettings.copies - 1)
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmoledCardElevated),
                        modifier = Modifier.size(40.dp).testTag("copies_minus_btn")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Azalt", tint = TextPrimary)
                    }

                    Text(
                        text = "${localSettings.copies}",
                        color = CyanNeon,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    FilledTonalButton(
                        onClick = {
                            if (localSettings.copies < 99) {
                                localSettings = localSettings.copy(copies = localSettings.copies + 1)
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmoledCardElevated),
                        modifier = Modifier.size(40.dp).testTag("copies_plus_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Arttır", tint = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Renk Modu (Color vs Monochrome)
            SettingSectionHeader(title = "RENK MODU")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorMode.values().forEach { mode ->
                    val isSelected = localSettings.colorMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF00333E) else AmoledCardSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyanNeon else BorderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { localSettings = localSettings.copy(colorMode = mode) }
                            .padding(14.dp)
                            .testTag("color_mode_${mode.name}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (mode == ColorMode.MONOCHROME) "Siyah-Beyaz" else "Renkli",
                                    color = if (isSelected) CyanNeon else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = if (mode == ColorMode.MONOCHROME) "Toner tasarrufu" else "Fotoğraf ve grafik",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Yönlendirme (Orientation)
            SettingSectionHeader(title = "SAYFA YÖNLENDİRMESİ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PageOrientation.values().forEach { orientation ->
                    val isSelected = localSettings.orientation == orientation
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF00333E) else AmoledCardSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyanNeon else BorderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { localSettings = localSettings.copy(orientation = orientation) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orientation.title,
                            color = if (isSelected) CyanNeon else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Kağıt Boyutu (Paper Size)
            SettingSectionHeader(title = "KAĞIT BOYUTU")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmoledCardSurface)
            ) {
                PaperSize.values().take(5).forEachIndexed { idx, size ->
                    val isSelected = localSettings.paperSize == size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { localSettings = localSettings.copy(paperSize = size) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = size.title,
                                color = if (isSelected) CyanNeon else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = size.dimensions,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (idx < 4) {
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Çift Taraflı Baskı (Duplex)
            SettingSectionHeader(title = "ÇİFT TARAFLI BASKI (DUPLEX)")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmoledCardSurface)
            ) {
                DuplexMode.values().forEachIndexed { idx, mode ->
                    val isSelected = localSettings.duplex == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { localSettings = localSettings.copy(duplex = mode) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mode.title,
                                color = if (isSelected) CyanNeon else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = mode.description,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (idx < DuplexMode.values().size - 1) {
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Kalite ve Kenar Boşlukları (Quality & Margins)
            SettingSectionHeader(title = "BASKI KALİTESİ (DPI)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrintQuality.values().forEach { q ->
                    val isSelected = localSettings.quality == q
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00333E) else AmoledCardSurface)
                            .border(1.dp, if (isSelected) CyanNeon else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { localSettings = localSettings.copy(quality = q) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${q.dpi} DPI",
                            color = if (isSelected) CyanNeon else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Kenar Boşlukları (Margins)
            SettingSectionHeader(title = "KENAR BOŞLUKLARI (MARGINS)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MarginMode.values().forEach { m ->
                    val isSelected = localSettings.margins == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00333E) else AmoledCardSurface)
                            .border(1.dp, if (isSelected) CyanNeon else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { localSettings = localSettings.copy(margins = m) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = m.title.split(" ").first(),
                            color = if (isSelected) CyanNeon else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Apply Button
            Button(
                onClick = {
                    onSettingsChanged(localSettings)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_settings_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Ayarları Kaydet ve Uygula", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

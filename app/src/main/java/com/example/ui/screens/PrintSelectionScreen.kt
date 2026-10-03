package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorMode
import com.example.data.model.PrintItem
import com.example.data.model.PrintItemType
import com.example.ui.components.PagePreviewCard
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCardElevated
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PrinterUiState
import com.example.ui.viewmodel.PrinterViewModel

@Composable
fun PrintSelectionScreen(
    uiState: PrinterUiState,
    viewModel: PrinterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Android Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = queryFileName(context, uri) ?: "Fotoğraf_${System.currentTimeMillis()}.jpg"
            viewModel.selectPhoto(uri, name)
        }
    }

    // Android Document Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = queryFileName(context, uri) ?: "Belge.pdf"
            val mime = context.contentResolver.getType(uri) ?: "application/pdf"
            viewModel.selectDocument(uri, name, mime, 1024 * 180, pages = 3)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("print_selection_screen")
    ) {
        // Active Printer Quick Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AmoledCardSurface)
                .border(1.dp, Color(0xFF1E2430), RoundedCornerShape(14.dp))
                .clickable { viewModel.setShowSettingsSheet(true) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF003844)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Aktif Yazıcı:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = uiState.printSettings.printerName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Değiştir",
                    color = CyanNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Selection Grid Title
        Text(
            text = "YAZDIRILACAK İÇERİK SEÇİN",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // 4 Fast Action Buttons (Belge, Fotoğraf, Hızlı Not, Web / Pano)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Document Picker
            ContentOptionButton(
                icon = Icons.Default.Description,
                title = "Belge / PDF",
                subtitle = "PDF, TXT, DOCX",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                onClick = {
                    documentPickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "text/plain",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                }
            )

            // Photo Picker
            ContentOptionButton(
                icon = Icons.Default.Image,
                title = "Fotoğraf",
                subtitle = "Galeri & Resim",
                color = Color(0xFF00E676),
                modifier = Modifier.weight(1f),
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Note Editor
            ContentOptionButton(
                icon = Icons.Default.EditNote,
                title = "Hızlı Not",
                subtitle = "Metin Yaz & Bas",
                color = CyanNeon,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setShowNoteEditorDialog(true) }
            )

            // Web / Clipboard
            ContentOptionButton(
                icon = Icons.Default.ContentPaste,
                title = "Pano / URL",
                subtitle = "Kopyalanan Metin",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                    if (clipText.isNotBlank()) {
                        viewModel.setClipboardItem(clipText)
                    } else {
                        viewModel.setShowWebDialog(true)
                    }
                }
            )
        }

        // Active Selection Card
        AnimatedVisibility(
            visible = uiState.selectedItem != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.selectedItem?.let { item ->
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF101C2A))
                        .border(1.dp, Color(0xFF1D4ED8), RoundedCornerShape(14.dp))
                        .padding(14.dp),
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    PrintItemType.DOCUMENT -> Icons.Default.Description
                                    PrintItemType.PHOTO -> Icons.Default.Image
                                    PrintItemType.NOTE -> Icons.Default.EditNote
                                    PrintItemType.WEB_PAGE -> Icons.Default.Language
                                    PrintItemType.CLIPBOARD -> Icons.Default.ContentPaste
                                },
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.summary,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearSelection() },
                        modifier = Modifier.size(32.dp).testTag("clear_selection_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = TextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Page Preview Card
        PagePreviewCard(
            item = uiState.selectedItem,
            settings = uiState.printSettings,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Settings Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AmoledCardSurface)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Copies stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.updateCopies(-1) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AmoledCardElevated)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "${uiState.printSettings.copies}",
                        color = CyanNeon,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Kopya", color = TextMuted, fontSize = 10.sp)
                }

                IconButton(
                    onClick = { viewModel.updateCopies(1) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AmoledCardElevated)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Arttır", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }
            }

            // Color Mode Toggle Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmoledCardElevated)
                    .clickable {
                        viewModel.setColorMode(
                            if (uiState.printSettings.colorMode == ColorMode.MONOCHROME) ColorMode.COLOR else ColorMode.MONOCHROME
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.printSettings.colorMode == ColorMode.MONOCHROME) "Siyah-Beyaz" else "Renkli",
                    color = if (uiState.printSettings.colorMode == ColorMode.COLOR) CyanNeon else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // More Settings Button
            IconButton(
                onClick = { viewModel.setShowSettingsSheet(true) },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF003844))
                    .testTag("open_settings_button")
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Tüm Ayarlar", tint = CyanNeon, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BIG Tactile Print CTA (Optimized for Samsung A34 5G thumb reach)
        Button(
            onClick = { viewModel.startPrint() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_print_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanNeon,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "WiFi ile PC Üzerinden Yazdır",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Android native print service link
        TextButton(
            onClick = {
                uiState.selectedItem?.let { item ->
                    if (item is PrintItem.PhotoItem) {
                        com.example.util.NativePrintHelper.printImage(context, item.uri, item.fileName)
                    } else {
                        com.example.util.NativePrintHelper.printHtmlOrText(
                            context,
                            item.title,
                            "<h1>${item.title}</h1><p>${item.summary}</p>"
                        )
                    }
                } ?: run {
                    com.example.util.NativePrintHelper.printHtmlOrText(
                        context,
                        "Test Sayfası",
                        "<h1>WiFi Yazıcı Test Çıktısı</h1><p>Samsung A34 5G üzerinden doğrudan Android yazdırma servisi.</p>"
                    )
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("direct_android_print_btn")
        ) {
            Text(
                text = "Veya Android Dahili Yazdırma Hizmetini Kullan",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Quick Note Editor Dialog
    if (uiState.showNoteEditorDialog) {
        NoteEditorDialog(
            title = uiState.noteTitleInput,
            content = uiState.noteContentInput,
            onTitleChange = { viewModel.updateNoteTitle(it) },
            onContentChange = { viewModel.updateNoteContent(it) },
            onSave = { title, content, template ->
                viewModel.saveNoteItem(title, content, template)
            },
            onDismiss = { viewModel.setShowNoteEditorDialog(false) }
        )
    }

    // Web URL Dialog
    if (uiState.showWebDialog) {
        WebPageDialog(
            url = uiState.webUrlInput,
            onUrlChange = { viewModel.updateWebUrl(it) },
            onConfirm = { viewModel.saveWebPage(it) },
            onDismiss = { viewModel.setShowWebDialog(false) }
        )
    }
}

@Composable
private fun ContentOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AmoledCardSurface)
            .border(1.dp, Color(0xFF1E2430), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("content_btn_$title")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun NoteEditorDialog(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTemplate by remember { mutableStateOf("Serbest Not") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AmoledCardSurface,
        title = {
            Text(
                text = "Hızlı Not & Metin Yazdır",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Templates row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Not", "Fatura", "Liste", "Dilekçe").forEach { t ->
                        val isSelected = selectedTemplate == t
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanNeon else AmoledCardElevated)
                                .clickable {
                                    selectedTemplate = t
                                    if (t == "Fatura") {
                                        onTitleChange("Fatura / Makbuz Belgesi")
                                        onContentChange("FATURA NO: #2026-0042\nTARİH: ${java.time.LocalDate.now()}\nMÜŞTERİ: Sayın İlgili\n----------------------------------\n1. Hizmet Bedeli ....... 1.250,00 TL\n2. KDV (%20) ............. 250,00 TL\n----------------------------------\nTOPLAM: 1.500,00 TL\nÖdeme Durumu: Ödendi")
                                    } else if (t == "Liste") {
                                        onTitleChange("Yapılacaklar / Malzeme Listesi")
                                        onContentChange("[ ] A4 Fotokopi Kağıdı (5 Paket)\n[ ] Siyah Toner Kartuşu\n[ ] Renkli Mürekkep Seti\n[ ] Şeffaf Dosya ve Zımba Teli")
                                    } else if (t == "Dilekçe") {
                                        onTitleChange("Resmi Dilekçe")
                                        onContentChange("İLGİLİ MAKAMA,\n\nKonu: Bilgi ve Belge Talebi\n\nYukarıda belirtilen hususlar doğrultusunda gerekli işlemlerin yapılmasını saygılarımla arz ederim.\n\nİsim Soyisim: \nİmza: ")
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = t,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Başlık") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Yazdırılacak Metin İçeriği") },
                    minLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("note_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, selectedTemplate) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Baskıya Ekle", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun WebPageDialog(
    url: String,
    onUrlChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AmoledCardSurface,
        title = {
            Text("Web Sayfası Yazdır", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Yazdırmak istediğiniz web sitesi veya belge bağlantısını girin:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("URL Adresi") },
                    placeholder = { Text("https://example.com/belge.html") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("web_url_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black)
            ) {
                Text("Ekle", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondary)
            }
        }
    )
}

private fun queryFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

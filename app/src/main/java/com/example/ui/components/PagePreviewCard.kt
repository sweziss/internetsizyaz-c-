package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ColorMode
import com.example.data.model.PageOrientation
import com.example.data.model.PrintItem
import com.example.data.model.PrintSettings
import com.example.ui.theme.AmoledCardElevated
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PagePreviewCard(
    item: PrintItem?,
    settings: PrintSettings,
    modifier: Modifier = Modifier
) {
    val isLandscape = settings.orientation == PageOrientation.LANDSCAPE
    val paperAspectRatio = if (isLandscape) 1.414f else 0.707f // A4 proportion

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AmoledCardSurface)
            .border(1.dp, Color(0xFF202736), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("page_preview_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Baskı Önizlemesi",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Specs badge
            Text(
                text = "${settings.paperSize.title} • ${settings.colorMode.title.take(12)}",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulated Paper Sheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090B0F)),
            contentAlignment = Alignment.Center
        ) {
            // Paper
            val paperBg = if (settings.colorMode == ColorMode.MONOCHROME) Color(0xFFF1F5F9) else Color.White
            Box(
                modifier = Modifier
                    .fillMaxSize(0.88f)
                    .aspectRatio(paperAspectRatio)
                    .shadow(8.dp, RoundedCornerShape(4.dp))
                    .background(paperBg, RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(
                        when (settings.margins.marginMm) {
                            0 -> 0.dp
                            5 -> 4.dp
                            25 -> 14.dp
                            else -> 8.dp
                        }
                    )
            ) {
                if (item == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Henüz bir içerik seçilmedi",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = item,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "previewContent"
                    ) { targetItem ->
                        when (targetItem) {
                            is PrintItem.PhotoItem -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = targetItem.uri,
                                        contentDescription = "Fotoğraf Önizleme",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (settings.colorMode == ColorMode.MONOCHROME) {
                                        // Monochrome overlay simulation
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0x33000000))
                                        )
                                    }
                                }
                            }
                            is PrintItem.TextNoteItem -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = targetItem.title,
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = targetItem.content,
                                        color = Color(0xFF334155),
                                        fontSize = 8.sp,
                                        lineHeight = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            is PrintItem.DocumentItem -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFAFAFA))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = if (settings.colorMode == ColorMode.COLOR) CyanNeon else Color.DarkGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = targetItem.fileName,
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Simulated lines of text on page
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        repeat(9) { index ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(if (index % 3 == 0) 0.65f else 0.95f)
                                                    .height(3.dp)
                                                    .background(Color(0xFFCBD5E1), RoundedCornerShape(1.dp))
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Sayfa 1 / ${targetItem.pages}",
                                        color = Color(0xFF64748B),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                            is PrintItem.WebPageItem -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .padding(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = targetItem.url,
                                            color = Color(0xFF2563EB),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    repeat(8) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .padding(vertical = 1.dp)
                                                .background(Color(0xFFE2E8F0))
                                        )
                                    }
                                }
                            }
                            is PrintItem.ClipboardItem -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "Pano Metni",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = targetItem.text,
                                        color = Color.DarkGray,
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Orientation indicator badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isLandscape) "Yatay" else "Dikey",
                    color = CyanNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

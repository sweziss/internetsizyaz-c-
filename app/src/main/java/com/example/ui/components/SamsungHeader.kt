package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PcServerInfo
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SamsungHeader(
    title: String,
    subtitle: String,
    pcServer: PcServerInfo,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AmoledBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top status pill (One UI style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AmoledCardSurface)
                    .clickable { onStatusClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("pc_status_pill")
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pcServer.isConnected) StatusSuccess else StatusWarning
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (pcServer.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = if (pcServer.isConnected) CyanNeon else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (pcServer.isConnected) "${pcServer.name} (${pcServer.ipAddress})" else "PC Bağlantısı Yok",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Samsung A34 Optimized Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141923))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "A34 5G • 120Hz",
                    color = CyanNeon.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large One UI Title
        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

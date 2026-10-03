package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob
import com.example.ui.theme.AmoledCardElevated
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveStatusBanner(
    job: PrintJob?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = job != null,
        modifier = modifier
    ) {
        if (job == null) return@AnimatedVisibility

        val isRunning = job.status == JobStatus.QUEUED ||
                job.status == JobStatus.CONNECTING ||
                job.status == JobStatus.SENDING ||
                job.status == JobStatus.SPOOLING ||
                job.status == JobStatus.PRINTING

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alphaGlow by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseGlow"
        )

        val accentColor = when (job.status) {
            JobStatus.COMPLETED -> StatusSuccess
            JobStatus.FAILED, JobStatus.CANCELLED -> StatusError
            JobStatus.PRINTING -> CyanNeon
            JobStatus.SENDING, JobStatus.CONNECTING -> Color(0xFF38BDF8)
            else -> StatusWarning
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AmoledCardSurface)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isRunning) alphaGlow else 0.4f),
                            Color(0xFF263042)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .animateContentSize()
                .padding(16.dp)
                .testTag("live_status_banner")
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
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (job.status) {
                                    JobStatus.COMPLETED -> Icons.Default.CheckCircle
                                    JobStatus.FAILED, JobStatus.CANCELLED -> Icons.Default.ErrorOutline
                                    JobStatus.PRINTING -> Icons.Default.Print
                                    else -> Icons.Default.Sync
                                },
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .alpha(if (isRunning) alphaGlow else 1f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = job.status.label,
                                    color = accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isRunning) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "%${job.progressPercent}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = job.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    if (isRunning) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StatusError
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp).testTag("cancel_job_button")
                        ) {
                            Text("İptal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                if (isRunning) {
                    LinearProgressIndicator(
                        progress = { job.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = AmoledCardElevated,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Subtitle details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = job.statusMessage,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (isRunning && job.totalPages > 1) {
                        Text(
                            text = "Sayfa ${job.currentPage} / ${job.totalPages}",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

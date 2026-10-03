package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCardElevated
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ServerScriptTemplate

@Composable
fun PcSetupGuideScreen(
    onCopySuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("pc_setup_guide_screen")
    ) {
        // Hero Guide Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AmoledCardSurface)
                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF003844)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PC Sunucusu Nasıl Kurulur?",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Her defasında PC ile uğraşmaya son!",
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Bu uygulama, bilgisayarınıza USB veya kabloyla bağlı olan yazıcıyı telefonunuzdan doğrudan kullanabilmeniz için hafif bir yerel WiFi köprüsü kullanır.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Steps
        Text(
            text = "3 ADIMDA KOLAY BAŞLATMA",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        StepCard(
            stepNumber = "1",
            title = "Aynı WiFi Ağına Bağlanın",
            description = "Samsung telefonunuz ve bilgisayarınız aynı ev/ofis modemine veya kablosuz ağına bağlı olmalıdır."
        )

        Spacer(modifier = Modifier.height(8.dp))

        StepCard(
            stepNumber = "2",
            title = "PC Sunucu Betiğini Çalıştırın",
            description = "Aşağıdaki hazır Python kodunu bilgisayarınızda bir metin dosyasına (pc_print_server.py) kaydedip terminalden 'python pc_print_server.py' yazın."
        )

        Spacer(modifier = Modifier.height(8.dp))

        StepCard(
            stepNumber = "3",
            title = "Telefonunuzdan Bağlanın",
            description = "Uygulamanın 'PC & Yazıcı' sekmesinde 'Ağda PC Ara'ya basın. Bilgisayarınız ve bağlı yazıcılar anında listelenecektir!"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Copy Python Script Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PC KÖPRÜ SUNUCU BETİĞİ (PYTHON)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("PC Print Server Script", ServerScriptTemplate.pythonScript))
                    onCopySuccess()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(34.dp).testTag("copy_script_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kodu Kopyala", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Code Viewer Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF07090D))
                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "# pc_print_server.py (Windows / Mac / Linux)",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = ServerScriptTemplate.pythonScript.take(600) + "\n\n# ... (Tamamı 'Kodu Kopyala' ile panoya alınır)",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Helpful FAQs
        Text(
            text = "SIK SORULAN SORULAR & İPUÇLARI",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        FaqItem(
            question = "Python kurulu değilse ne yapmalıyım?",
            answer = "python.org adresinden ücretsiz indirebilir veya Microsoft Store üzerinden tek tıkla Python 3 yükleyebilirsiniz."
        )

        Spacer(modifier = Modifier.height(8.dp))

        FaqItem(
            question = "Windows Güvenlik Duvarı uyarısı çıkarsa?",
            answer = "İlk çalıştırmada çıkan 'Özel Ağlar' onay kutusuna izin verin; böylece telefonunuz yerel ağdan PC'ye bağlanabilir."
        )

        Spacer(modifier = Modifier.height(8.dp))

        FaqItem(
            question = "Test / Simülasyon modu nedir?",
            answer = "Bilgisayarınız henüz açık olmasa bile tüm sayfa ayarlarını, önizlemeleri ve yazdırma akışını test edebilmeniz için yerleşik bir simülatördür."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmoledCardSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF003844)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stepNumber, color = CyanNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AmoledCardSurface)
            .padding(12.dp)
    ) {
        Text(text = question, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = answer, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

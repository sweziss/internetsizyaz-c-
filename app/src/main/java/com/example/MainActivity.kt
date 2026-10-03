package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SamsungHeader
import com.example.ui.screens.PcConnectionScreen
import com.example.ui.screens.PcSetupGuideScreen
import com.example.ui.screens.PrintQueueScreen
import com.example.ui.screens.PrintSelectionScreen
import com.example.ui.screens.PrintSettingsSheet
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.PrinterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PrinterViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: PrinterViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyJobs by viewModel.historyJobs.collectAsStateWithLifecycle()
    val savedPcs by viewModel.savedPcs.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle toast/snackbars
    LaunchedEffect(uiState.successToast, uiState.errorMessage) {
        uiState.successToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.dismissToast()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissToast()
        }
    }

    // Handle back button when on sub-tabs
    BackHandler(enabled = uiState.selectedTab != AppTab.PRINT) {
        viewModel.selectTab(AppTab.PRINT)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBackground),
        containerColor = AmoledBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = AmoledBackground,
                contentColor = TextPrimary,
                tonalElevation = 0.dp
            ) {
                val tabs = listOf(
                    Triple(AppTab.PRINT, Icons.Filled.Print, Icons.Outlined.Print),
                    Triple(AppTab.PC_DEVICES, Icons.Filled.Computer, Icons.Outlined.Computer),
                    Triple(AppTab.QUEUE, Icons.Filled.History, Icons.Outlined.History),
                    Triple(AppTab.SETUP_GUIDE, Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
                )

                tabs.forEach { (tab, filledIcon, outlinedIcon) ->
                    val isSelected = uiState.selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) filledIcon else outlinedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyanNeon else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) CyanNeon else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            unselectedIconColor = TextMuted,
                            indicatorColor = Color(0xFF003844)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Samsung One UI Top Header
            val (headerTitle, headerSubtitle) = when (uiState.selectedTab) {
                AppTab.PRINT -> Pair("WiFi Yazıcı", "PC'ye bağlı yazıcıdan anında çıktı alın")
                AppTab.PC_DEVICES -> Pair("PC ve Yazıcılar", "Ağdaki bilgisayarı ve yazıcıları yönetin")
                AppTab.QUEUE -> Pair("Baskı ve Kuyruk", "Canlı yazdırma durumu ve iş geçmişi")
                AppTab.SETUP_GUIDE -> Pair("Kurulum Rehberi", "Bilgisayar köprü sunucusunu 1 dakikada kurun")
            }

            SamsungHeader(
                title = headerTitle,
                subtitle = headerSubtitle,
                pcServer = uiState.pcServer,
                onStatusClick = { viewModel.selectTab(AppTab.PC_DEVICES) }
            )

            // Animated Screen Content Area
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        AppTab.PRINT -> PrintSelectionScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        AppTab.PC_DEVICES -> PcConnectionScreen(
                            uiState = uiState,
                            savedPcs = savedPcs,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        AppTab.QUEUE -> PrintQueueScreen(
                            uiState = uiState,
                            historyJobs = historyJobs,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        AppTab.SETUP_GUIDE -> PcSetupGuideScreen(
                            onCopySuccess = {
                                Toast.makeText(context, "Python sunucu betiği panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Full Printer & Page Settings
    if (uiState.showSettingsSheet) {
        PrintSettingsSheet(
            settings = uiState.printSettings,
            onSettingsChanged = { newSettings ->
                viewModel.setCopies(newSettings.copies)
                viewModel.setColorMode(newSettings.colorMode)
                viewModel.setOrientation(newSettings.orientation)
                viewModel.setPaperSize(newSettings.paperSize)
                viewModel.setDuplex(newSettings.duplex)
                viewModel.setQuality(newSettings.quality)
                viewModel.setMargins(newSettings.margins)
                viewModel.setScaling(newSettings.scaling)
                viewModel.setPageRange(newSettings.pageRangeType, newSettings.customPageRange)
            },
            onDismiss = { viewModel.setShowSettingsSheet(false) },
            sheetState = sheetState
        )
    }
}

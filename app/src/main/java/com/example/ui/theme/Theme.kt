package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = CyanNeon,
    secondary = SkyBlue,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF132B45),
    onSecondaryContainer = SkyBlue,
    tertiary = StatusSuccess,
    onTertiary = Color.Black,
    background = AmoledBackground,
    onBackground = TextPrimary,
    surface = AmoledSurface,
    onSurface = TextPrimary,
    surfaceVariant = AmoledCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderAccent,
    error = StatusError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force true dark for AMOLED black theme request
    dynamicColor: Boolean = false, // Keep AMOLED black consistency
    content: @Composable () -> Unit
) {
    val colorScheme = AmoledDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AmoledBackground.toArgb()
            window.navigationBarColor = AmoledBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

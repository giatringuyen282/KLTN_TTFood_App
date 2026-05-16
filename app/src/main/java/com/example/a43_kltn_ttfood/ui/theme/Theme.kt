package com.example.a43_kltn_ttfood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange600,
    secondary = Red500,
    onSecondary = White,
    secondaryContainer = Color(0xFFFFEBEE),
    onSecondaryContainer = Red600,
    tertiary = WarningYellow,
    onTertiary = Black,
    background = White,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    outlineVariant = Gray200,
    error = ErrorRed,
    onError = White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Red600
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    onPrimary = Black,
    primaryContainer = Orange600,
    onPrimaryContainer = Orange100,
    secondary = Red400,
    onSecondary = Black,
    secondaryContainer = Red600,
    onSecondaryContainer = Color(0xFFFFCDD2),
    tertiary = WarningYellow,
    onTertiary = Black,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Gray400,
    outline = DarkBorder,
    outlineVariant = Gray800,
    error = Color(0xFFEF9A9A),
    onError = Red600,
    errorContainer = Red600,
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun TTFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
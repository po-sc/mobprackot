package ru.mirea.prac9.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF81C784),
    errorContainer = Color(0xFFE57373),
    onPrimary = Color.White,
    onErrorContainer = Color.White,
    surface = Color(0xFFF5F5F5),
    surfaceContainer = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),
    secondaryContainer = Color(0xFF4A744C),
    errorContainer = Color(0xFFD32F2F),
    onPrimary = Color.Black,
    onErrorContainer = Color.Black,
    surface = Color(0xFF424242),
    surfaceContainer = Color(0xFF303030)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
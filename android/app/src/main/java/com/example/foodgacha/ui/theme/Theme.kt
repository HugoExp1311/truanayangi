package com.example.foodgacha.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5A93C),
    onPrimary = Color(0xFF1E1705),
    primaryContainer = Color(0xFF3B2F10),
    onPrimaryContainer = Color(0xFFFFDEA3),
    secondary = Color(0xFFD4C4A8),
    onSecondary = Color(0xFF382F1D),
    background = Color(0xFF131315),
    onBackground = Color(0xFFE5E2DA),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE5E2DA),
    surfaceVariant = Color(0xFF2E2D32),
    onSurfaceVariant = Color(0xFFC7C5D0)
)

@Composable
fun FoodGachaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

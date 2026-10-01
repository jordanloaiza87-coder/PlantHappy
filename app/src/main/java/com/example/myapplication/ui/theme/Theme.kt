package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    isWaterLow: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dynamic theme: lush green garden when healthy, warm earthy/amber tones when thirsty
    val colorScheme = if (isWaterLow) {
        lightColorScheme(
            primary = Color(0xFFC0392B), // Alert red/brown
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFADBD8),
            onPrimaryContainer = Color(0xFF641E16),
            secondary = Color(0xFFD35400),
            secondaryContainer = Color(0xFFFDEBD0),
            background = Color(0xFFFCF3CF), // Dry warm background
            surface = Color.White,
            onSurface = Color(0xFF78281F),
            surfaceVariant = Color(0xFFF9EBEA),
            onSurfaceVariant = Color(0xFF922B21)
        )
    } else {
        lightColorScheme(
            primary = ForestGreen,
            onPrimary = Color.White,
            primaryContainer = MintGreen,
            onPrimaryContainer = Color(0xFF081C15),
            secondary = LeafGreen,
            secondaryContainer = Color(0xFFD8F3DC),
            background = SoftCream,
            surface = Color.White,
            onSurface = Color(0xFF1B4332),
            surfaceVariant = Color(0xFFE9F5EC),
            onSurfaceVariant = Color(0xFF2D6A4F)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

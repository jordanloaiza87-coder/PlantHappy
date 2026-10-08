package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    isWaterLow: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic theme: lush green garden when healthy, warm earthy/amber tones when thirsty
    val colorScheme = when {
        darkTheme && isWaterLow -> darkColorScheme(
            primary = Color(0xFFE74C3C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF641E16),
            onPrimaryContainer = Color(0xFFFADBD8),
            secondary = Color(0xFFE67E22),
            secondaryContainer = Color(0xFF873600),
            background = Color(0xFF2C1010),
            surface = Color(0xFF1C1313),
            onSurface = Color(0xFFF9EBEA),
            surfaceVariant = Color(0xFF3A2323),
            onSurfaceVariant = Color(0xFFE6B0AA)
        )
        darkTheme && !isWaterLow -> darkColorScheme(
            primary = Color(0xFF2ECC71),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF1B4332),
            onPrimaryContainer = Color(0xFFD8F3DC),
            secondary = Color(0xFF27AE60),
            secondaryContainer = Color(0xFF081C15),
            background = Color(0xFF0B1914),
            surface = Color(0xFF12201A),
            onSurface = Color(0xFFD8F3DC),
            surfaceVariant = Color(0xFF1B3026),
            onSurfaceVariant = Color(0xFF95D5B2)
        )
        !darkTheme && isWaterLow -> lightColorScheme(
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
        else -> lightColorScheme(
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

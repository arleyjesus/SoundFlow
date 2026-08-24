package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Esquema Oscuro AMOLED
private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    secondary = LightLavender,
    tertiary = PastelPink,
    background = Color.Black, // Fondo negro puro #000000
    surface = Color(0xFF121212), // Reemplazamos el error por un gris oscuro real
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E)
)

// 2. Esquema Claro
private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    secondary = LightLavender,
    tertiary = PastelPink,
    background = Color(0xFFFBFBFE),
    surface = Color.White, // Reemplazamos el error por color blanco real
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0E6FA)
)

@Composable
fun ComarleyjesusaetheraudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
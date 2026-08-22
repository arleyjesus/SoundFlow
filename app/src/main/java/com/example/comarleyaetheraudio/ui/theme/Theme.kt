package com.example.comarleyaetheraudio.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    secondary = SoftPink,
    background = PureBlack,
    surface = DarkSurface,
    onPrimary = PureBlack,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = DarkCard
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleLight,
    background = Color(0xFFFBFBFE),
    surface = Color.White,
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
        typography = Typography, // Asegúrate de tener tu archivo Typography
        content = content
    )
}
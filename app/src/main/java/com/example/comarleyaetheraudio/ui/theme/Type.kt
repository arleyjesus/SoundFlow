package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ⚡ TRÍO TIPOGRÁFICO ARMONIOSO Y MODERNO (SIN CURSIVAS NI MONOESPACIADAS)
val DisplayFontFamily = FontFamily.SansSerif
val MainFontFamily = FontFamily.SansSerif

val Typography = Typography(
    // Títulos de Gran Impacto (Reproductor Pantalla Completa, SoundFlow, Nombre Playlist)
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    ),

    // Nombres de Canción en Reproductor Abierto y Pantalla Principal
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),

    // Encabezados de Secciones ("Recently Played", "Tus Playlists", etc.)
    titleMedium = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),

    // Nombres de Canciones en Listados y MiniPlayer
    bodyLarge = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.15.sp
    ),

    // Nombres de Artistas y Textos Secundarios
    bodyMedium = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.25.sp
    ),

    // Metadatos (Duración, Nro. Canciones, Subtextos)
    labelMedium = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)